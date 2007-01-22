#!/bin/bash
#
# Sample script to start up a slave searcher for distributed search.
# 
# Takes single 'port' argument.
#
# Assumes HADOOP_HOME defined in hadoop-env.sh here and out on slaves.
# Assumes searcher.dir and slave.searcher.dir are under $HADOOP_CONF_DIR.
# Assumes RUNTIME_HOME, where nutchwax jar can be found, is defined in
# hadoop-env.sh 
#
# $Id$
#
if [ "${HADOOP_CONF_DIR}" = "" ]
then
    echo "Set HADOOP_CONF_DIR"
    exit 1
fi
if [ $# = 0 ]
then
    echo "Usage: $0 PORT"
    exit 1
fi
. "${HADOOP_CONF_DIR}"/hadoop-env.sh
nohup bash ${HADOOP_HOME}/bin/hadoop jar \
        ${RUNTIME_HOME}/nutchwax*/nutchwax*.jar class \
        'org.archive.access.nutch.NutchwaxDistributedSearch$Server' $1 \
        /0/search/searcher.dir.$1 &> ${HADOOP_LOG_DIR}/slave-searcher-$1.log &
