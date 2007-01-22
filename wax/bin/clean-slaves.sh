#!/bin/sh
#
# Sample clean up slaves script. It will kill errant java processes, 
# remove moved-aside bad files, clean tmp directoris and remove all logs.
# Use with caution.  The below has hardcoded presumptions about where
# things can be found on slaves.  Amend to suit your environment. Removes
# checksum bad files, cleans logs, and kills an errant java processes.
# Use with caution.
#
# $Id$
#
if [ "${HADOOP_CONF_DIR}" = "" ]
then
    echo "Set HADOOP_CONF_DIR"
    exit 1
fi
. ${HADOOP_CONF_DIR}/hadoop-env.sh
if [ ! -d ${HADOOP_LOG_DIR} ]
then
    echo "Set HADOOP_LOG_DIR"
    exit 1
fi
for i in `cat ${HADOOP_CONF_DIR}/slaves `
do 
    echo $i
    ssh -o SendEnv=HADOOP_LOG_DIR $i \
        'killall java; rm -rf ${HADOOP_LOG_DIR}/*; rm -rf /[0-3]/bad_files'
    ssh $i 'find /[0-3]/hadoop/tmp -type d -maxdepth 1 -exec rm -rf {} \;'
done
