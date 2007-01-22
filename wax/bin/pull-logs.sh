#!/bin/sh
#
# Pull logs local from cluster.
#
# $Id$
#
if [ "$1" = "" ]
then
    echo "Usage: $0 LOGSDIR"
    exit 1
fi
if [ "${HADOOP_CONF_DIR}" = "" ]
then
    echo "Set HADOOP_CONF_DIR environment variable."
    exit 1
fi
source ${HADOOP_CONF_DIR}/hadoop-env.sh
for i in `cat ${HADOOP_CONF_DIR}/slaves`
do
    rsync -av $i:${HADOOP_LOG_DIR}/ $1 &
done
