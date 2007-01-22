#!/bin/bash
# Script to stop all search-servers.
# Assumes searcher.dir is in ${HADOOP_CONF_DIR}.
# 
# $Id$
#
if [ "${HADOOP_CONF_DIR}" = "" ]
then
    echo "Set HADOOP_CONF_DIR"
    exit 1
fi
. "${HADOOP_CONF_DIR}"/hadoop-env.sh
SEARCHER_DIR="/0/search/frontend.searcher.dir"
for i in `awk '{print $1}' ${SEARCHER_DIR}/search-servers.txt`
do
    echo "Stopping $i"
    ssh $i ". ${HADOOP_CONF_DIR}/hadoop-env.sh; \
        ps -C java -fwwwH|sed -ne '/NutchwaxDistributedSearch/p'| \
        awk '{print \$2}' | xargs kill -9 2> /dev/null"
done
