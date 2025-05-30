#!/bin/zsh

etcd help

echo "Create key-value pair."
etcdctl put rpc lightrpc
etcdctl get rpc
etcdctl del rpc

echo

echo "Create lease with 30s and get leaseId to bind with key-value pair."
echo "After 30s it will auto delete."
etcdctl lease grant 30 | awk '/lease {print $2}' | xargs -I {} etcdctl put rpc lightrpc --lease={}
echo "Check key immediately:"
etcdctl get rpc

echo

echo "Check key after 30s:"
sleep 30
etcdctl get rpc
