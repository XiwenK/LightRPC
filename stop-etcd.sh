#!/bin/zsh

ETCD_NAME="dev-etcd"
OLD_PID=$(pgrep -f "etcd.*--name ${ETCD_NAME}")

if [[ -n "$OLD_PID" ]]; then
  echo "Stopping etcd (PID: $OLD_PID)..."
  kill "$OLD_PID"
else
  echo "No etcd process found."
fi

