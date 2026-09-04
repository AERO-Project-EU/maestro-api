#!/usr/bin/env bash

#  volume
VOLUME_PATH="/home/ubuntu/data"

# backup
BACKUP_PATH="/home/ubuntu/data/backup"

#------------------------------------------------------------------------------configure policy engine
mkdir -p $VOLUME_PATH/policy-engine/rules

#------------------------------------------------------------------------------configure prometheus
mkdir -p $VOLUME_PATH/prometheus/config
cp ../../../ext-deps/prometheus/configs/prometheus.yml $VOLUME_PATH/prometheus/config
cp ../../../ext-deps/prometheus/configs/policy_rules.yml $VOLUME_PATH/prometheus/config

#-----------------------------------------------------------------------------configure alertManager
mkdir -p $VOLUME_PATH/alertManager/config
cp ../../../ext-deps/alertManager/configuration.yml $VOLUME_PATH/alertManager/config

#-----------------------------------------------------------------------------configure grafana
cp -r ../../../ext-deps/grafana/ $VOLUME_PATH/grafana
chmod -R 777 $VOLUME_PATH/grafana

#-----------------------------------------------------------------------------configure consul
mkdir -p $VOLUME_PATH/consul/data
mkdir -p $VOLUME_PATH/consul/config

#-----------------------------------------------------------------------------configure database
mkdir -p $VOLUME_PATH/database/data

#-----------------------------------------------------------------------------configure landoop
mkdir -p $VOLUME_PATH/landoop/data

#-----------------------------------------------------------------------------configure backup
mkdir -p $BACKUP_PATH
#-----------------------------------------------------------------------------configure backup