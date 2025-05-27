package com.sean.lightrpc.spi;

import cn.hutool.core.io.resource.ResourceUtil;
import com.sean.lightrpc.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SpiLoader {
    /**
     *  Loaded Classes - Interface type --> Map (key --> ClassName)
     *  e.g. 'com.sean.lightrpc.serializer.Serializer' --> Map('jdk' --> 'com.sean.lightrpc.serializer.JdkSerializer')
     */
    private static final Map<String, Map<String, Class<?>>> loaderMap = new ConcurrentHashMap<>();

    /**
     *  Loaded ClassInstance - ClassName --> ClassInstance
     *  e.g. 'com.sean.lightrpc.serializer.JdkSerializer' --> JdkSerializer jdkSerializer
     */
    private static final Map<String, Object> instanceCache = new ConcurrentHashMap<>();

    /**
     *  System-default classes SPI load path
     */
    private static final String RPC_SYSTEM_SPI_DIR = "META-INF/rpc/system/";

    /**
     *  User-defined classes SPI load path
     */
    private static final String RPC_CUSTOM_SPI_DIR = "META-INF/rpc/custom/";

    /**
     *  scan path
     */
    private static final String[] SCAN_DIRS = new String[]{RPC_SYSTEM_SPI_DIR, RPC_CUSTOM_SPI_DIR};

    /**
     *  Dynamic Loading Interface List
     */
    private static final List<Class<?>> LOAD_CLASS_LIST = List.of(Serializer.class);

    /**
     *  Get instance of class of interface type
     *   - Lazy loading
     *   - Caching
     */
    @SuppressWarnings("unchecked")
    public static <T> T getInstance(Class<?> tClass, String key) {
        String tClassName = tClass.getName();
        Map<String, Class<?>> keyClassMap = loaderMap.get(tClassName);

        if (keyClassMap == null) {
            log.info("Classes of type {} not loaded by SPI", tClassName);
            return null;
        }
        if (!keyClassMap.containsKey(key)) {
            log.info("Class of type {} and key {} not exist", tClassName, key);
            return null;
        }

        // Get class from cache
        Class<?> implClass = keyClassMap.get(key);
        String implClassName = implClass.getName();

        // Get instance from cache first, or else create a new instance and update cache
        if (!instanceCache.containsKey(implClassName)) {
            try {
                instanceCache.put(implClassName, implClass.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                log.info("Instance of class {} failed: {}", implClassName, e.getMessage());
            }
        }

        return (T) instanceCache.get(implClassName);
    }


    /**
     *  Load all classes of specified interfaces
     *  @param loadClass - interface to be loaded (e.g. Serializer.class)
     */
    public static Map<String, Class<?>> load(Class<?> loadClass) {
        log.info("Loading SPI {} type ...", loadClass.getName());

        // First scan user-defined classes SPI path, then system-default one
        Map<String, Class<?>> keyClassMap = new HashMap<>();
        for (String scanDir : SCAN_DIRS) {
            // Get specified file resource by file path
            List<URL> resources = ResourceUtil.getResources(scanDir + loadClass.getName());

            // Read the file resource line by line (e.g. jdk=com.sean.lightrpc.serializer.JdkSerializer)
            for (URL resource : resources) {
                try {
                    InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    String line = null;
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] strArray = line.split("=");
                        if (strArray.length > 1) {
                            String key = strArray[0];
                            String className = strArray[1];
                            keyClassMap.put(key, Class.forName(className));
                        }
                    }
                } catch (Exception e) {
                    log.info("SPI classes load error: {}", e.getMessage());
                }
            }
        }

        loaderMap.put(loadClass.getName(), keyClassMap);
        return keyClassMap;
    }

    public static void loadAll() {
        log.info("Load All SPI Classes ...");
        for (Class<?> aClass : LOAD_CLASS_LIST) {
            load(aClass);
        }
    }
}
