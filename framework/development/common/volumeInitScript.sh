#!/usr/bin/env bash

USER_PATH=$(eval echo ~$USER)

#  volume
VOLUME_PATH="${USER_PATH}/data"

# backup
BACKUP_PATH="${VOLUME_PATH}/backup"

#-----------------------------------------------------------------------------configure backend
mkdir -p $VOLUME_PATH/backend

#-----------------------------------------------------------------------------configure database
mkdir -p $VOLUME_PATH/database/data

#-----------------------------------------------------------------------------configure landoop
mkdir -p $VOLUME_PATH/landoop/data

#-----------------------------------------------------------------------------configure backup
mkdir -p $BACKUP_PATH
