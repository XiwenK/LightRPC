package com.sean.lightrpc.registry;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.sean.lightrpc.config.RegistryConfig;
import com.sean.lightrpc.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class EtcdRegistry implements Registry {

    private Client etcdClient;

    private KV kvClient;

    /**
     *  Local Node Keys Set (used for lease renewal)
     *   - Only services recorded in set will do lease renewal
     */
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();

    /**
     *  Cache for service in registry (used for service discovery)
     */
    private final RegistryServiceMultiCache registryServiceMultiCache = new RegistryServiceMultiCache();

    /**
     *  Monitoring keys set (used for watch-or-not check)
     */
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();

    /**
     *  Root path for Etcd key storage
     */
    private static final String ETCD_ROOT_PATH = "/rpc/";

    @Override
    public void init(RegistryConfig registryConfig) {
        etcdClient = Client.builder()
                .endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
                .build();
        kvClient = etcdClient.getKVClient();

        // Start cronjob to do heartbeat check and lease renewal
        heartBeat();
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        // Get leaseClient from etcdClient
        Lease leaseClient = etcdClient.getLeaseClient();

        // Create a 30s lease
        long leaseId = leaseClient.grant(30).get().getID();

        // Set a ServiceInfoString - ServiceMetaInfo pair to be stored
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        ByteSequence key = ByteSequence.from(registerKey, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);

        // correlate key-value pair with the lease
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        kvClient.put(key, value, putOption).get();

        // Add into local cache
        localRegisterNodeKeySet.add(registerKey);
    }

    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        kvClient.delete(ByteSequence.from(registerKey, StandardCharsets.UTF_8));

        // Remove from local cache
        localRegisterNodeKeySet.remove(registerKey);
    }

    /**
     *  Query all serviceMetaInfos for specified service key
     */
    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        // First try to fetch from cache
        List<ServiceMetaInfo> cachedServiceMetaInfoList = registryServiceMultiCache.readCache(serviceKey);
        if (cachedServiceMetaInfoList != null) {
            return cachedServiceMetaInfoList;
        }

        // Build prefix
        String searchPrefix = ETCD_ROOT_PATH + serviceKey + "/";

        try {
            // Query with service prefix from etcd
            GetOption getOption = GetOption.builder().isPrefix(true).build();
            List<KeyValue> keyValues = kvClient.get(
                            ByteSequence.from(searchPrefix, StandardCharsets.UTF_8),
                            getOption)
                    .get()
                    .getKvs();

            // Convert keyValues into ServiceMetaInfo object
            List<ServiceMetaInfo> serviceMetaInfoList = keyValues.stream()
                    .map(keyValue -> {
                        String key = keyValue.getKey().toString(StandardCharsets.UTF_8);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);

                        watch(key);

                        return JSONUtil.toBean(value, ServiceMetaInfo.class);
                    }).collect(Collectors.toList());

            // Update into cache
            registryServiceMultiCache.writeCache(serviceKey, serviceMetaInfoList);

            return serviceMetaInfoList;
        } catch (Exception e) {
            log.info("Failed to discover service {} from etcd: {}", serviceKey, e.getMessage());
        }

        return List.of();
    }

    /**
     *  Check service health status while renew release for still-living services
     */
    @Override
    public void heartBeat() {
        // renew lease every 10s
        CronUtil.schedule("*/10 * * * * *", new Task() {
            @Override
            public void execute() {
                // Iterate all nodes of current node
                for (String key : localRegisterNodeKeySet) {
                    try {
                        List<KeyValue> keyValues = kvClient.get(ByteSequence.from(key, StandardCharsets.UTF_8))
                                .get()
                                .getKvs();

                        // Current node already outdated (need to re-init)
                        if (CollUtil.isEmpty(keyValues)) {
                            continue;
                        }

                        // Renew lease by register again
                        KeyValue keyValue = keyValues.get(0);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(value, ServiceMetaInfo.class);
                        register(serviceMetaInfo);
                    } catch (Exception e) {
                        log.info("Failed to renew service {} from etcd: {}", key, e.getMessage());
                    }
                }
            }
        });

        // Support seconds-level cronjob
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }

    /**
     *  Add specified service into watchlist (Used by service discovery)
     */
    @Override
    public void watch(String serviceNodeKey) {
        Watch watchClient = etcdClient.getWatchClient();

        // Check if add to watchlist or not
        boolean newWatch = watchingKeySet.add(serviceNodeKey);
        if (newWatch) {
            watchClient.watch(ByteSequence.from(serviceNodeKey, StandardCharsets.UTF_8), response -> {
                for (WatchEvent event : response.getEvents()) {
                    switch (event.getEventType()) {
                        // Trigger when key deleted
                        case DELETE:
                            // Clear outdated service cache, then next time service discovery will update from etcd
                            String serviceKey = serviceNodeKey.substring(0, serviceNodeKey.lastIndexOf("/"));
                            registryServiceMultiCache.clearCache(serviceKey);
                            break;
                        case PUT:
                        default:
                            break;
                    }
                }
            });
        }
    }

    @Override
    public void destroy() {
        log.info("Current etcd registry node is down ...");

        // Remove all keys of current node from etcd
        for (String key : localRegisterNodeKeySet) {
            try {
                kvClient.delete(ByteSequence.from(key, StandardCharsets.UTF_8)).get();
            } catch (Exception e) {
                log.error("Failed to clean etcd node {}: {}", key, e.getMessage());
            }
        }

        // Release resources
        if (kvClient != null) {
            kvClient.close();
        }
        if (etcdClient != null) {
            etcdClient.close();
        }
    }
}
