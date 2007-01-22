#!/bin/sh
#
# Run a df across the cluster.
#
# $Id$
#
if [ "${HADOOP_CONF_DIR}" = "" ]
then
    echo "Set HADOOP_CONF_DIR"
    exit 1
fi
if [ ! -d ${HADOOP_LOG_DIR} ]
then
    echo Set HADOOP_LOG_DIR
    exit 1
fi
for i in `cat ${HADOOP_CONF_DIR}/slaves `
do 
    ssh $i 'hostname; df -h'
done
