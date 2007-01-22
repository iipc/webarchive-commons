#!/bin/sh
# nutchwax_regress.sh
# Regression script by David Cathcart cathcart at archive dot org
# for nutchwax. 
# Runs nutchwax_deploy.sh then nutchwac_check*.sh 
#
# Requirements:
#	Java 1.5
#	A running Tomcat 5.5 (change TOMCAT_VER if different)
#	Standard unix commands:
#		sh, which, echo, basename, awk, gnu tar, getopt
#		lynx, wget, patch
#
############################################################
# Deploy by default
TEST_ONLY=0
# Get options and config file
args=`getopt vt $*`
if [ $? -ne 0 ]
then
	echo "Usage $1 [-vt] [nutchwax_test_conf]"
	exit 2
fi
set -- $args
for i
do
	case "$i"
	in
		-v)
			VB=1; shift;;
		-t)
			TEST_ONLY=1; shift;;	
		--)
			CONF=$2; shift; break;;
	esac
done

MY_LOC=`dirname $0`

if [ \( -n "$CONF" \) -a \( -f "$CONF" \) ] ; then
	. "$CONF"
	case $CONF in	
		/*) ORIG_DIR=`dirname $CONF` ;;
		*)  ORIG_DIR=$PWD/`dirname $CONF` ;;
	esac
elif [ -f ./nutchwax_test_config ] ; then
	. ./nutchwax_test_config
	ORIG_DIR=$PWD
elif [ -f $MY_LOC/nutchwax_test_config ] ; then
	. $MY_LOC/nutchwax_test_config
	case $MY_LOC in
		/*) ORIG_DIR=$MY_LOC ;;
		*)  ORIG_DIR=$PWD/$MY_LOC ;;
	esac
else
	echo "Could not find config" 1>&2
	exit 1
fi

verb()
{
	if [ -n "$VB" ]; then 
		echo $1
	fi
}
if [ "$TEST_ONLY" -ne 1 ]; then
	verb "Running nutchwax_deploy.sh"
	$MY_LOC/nutchwax_deploy.sh ${VB:+-v} $CONF
	if [ $? -ne 0 ]; then echo "Failure deploying nutchwax" 1>&2; exit 1; fi
fi 
TESTS=`ls $MY_LOC/nutchwax_check*.sh`
for TEST in $TESTS; do
	$TEST ${VB:+-v} $CONF
	if [ $? -ne 0 ]; then
		 echo "Failure running $TEST" 1>&2; exit 1; 
	fi
done
