#!/bin/bash
#
# Sample script to start up slave searchers for distributed search.
#
# Assumes searcher.dir under $HADOOP_CONF_DIR.
# Assumes RUNTIME_HOME is defined in hadoop-env.sh.  Below uses it to find
# the script start-slave-searcher.sh (singular).  Read head of that script
# to see what it requirers.
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
cat "${SEARCHER_DIR}/search-servers.txt" | while read line
do
    echo "Starting ${line}"
    server=`echo $line|awk '{print $1}'`
    port=`echo $line|awk '{print $2}'`
    ssh ${server} "HADOOP_CONF_DIR=${HADOOP_CONF_DIR} nohup bash ${RUNTIME_HOME}/bin/start-slave-searcher.sh ${port}"
done
