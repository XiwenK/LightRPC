#!/bin/zsh

# etcd instance name 
ETCD_NAME="dev-etcd"

# etcd tmp data directory
ETCD_DATA_DIR="/tmp/etcd-data"

# etcd client listening address
ETCD_CLIENT_URL="http://127.0.0.1:2379"

# etcd log output directory
ETCD_LOG="/tmp/etcd.log"

# first shutdown old etcd instance if exists
OLD_PID=$(pgrep -f "etcd.*--name ${ETCD_NAME}")
if [[ -n "$OLD_PID" ]]; then
  echo "Stopping existing etcd process (PID: $OLD_PID)..."
  kill "$OLD_PID"
  sleep 1
fi

# clean old data
rm -rf "$ETCD_DATA_DIR"

# start new etcd instance
echo "Starting etcd..."
etcd \
  --name "${ETCD_NAME}" \
  --data-dir "${ETCD_DATA_DIR}" \
  --listen-client-urls "${ETCD_CLIENT_URL}" \
  --advertise-client-urls "${ETCD_CLIENT_URL}" \
  > "${ETCD_LOG}" 2>&1 &

ETCD_PID=$!
echo "Etcd started with PID: $ETCD_PID"
echo "Logs: tail -f ${ETCD_LOG}"

