#!/bin/sh
# nutchwax_check_arcname.sh
# Regression script by David Cathcart cathcart at archive dot org
# for nutchwax. 
# When run, we grep the arcfiles given in the configuration for the origional 
# arc name and then test that a arcname: query returns the currect number of 
# matches 
#
# Requirements:
#	Java 1.5
#	A running Tomcat 5.5 (change TOMCAT_VER if different)
#	Standard unix commands:
#		sh, which, echo, basename, awk, gnu tar, getopt
#		lynx, wget, cut, zgrep
#
############################################################
# Don't be verbose by default
VB=0
# Set paths to rss and search fields
RSS_URL='/opensearch?query='
QUERY_URL='/search.jsp?query='
ARCNAME_STRING='arcname:'
# Get options and config file
args=`getopt v $*`
if [ $? -ne 0 ]
then
	echo "Usage $1 [-v] [nutchwax_test_conf]"
	exit 2
fi
set -- $args
for i
do
	case "$i"
	in
		-v)
			VB=1; shift;;
		--)
			CONF=$2; shift; break;;
	esac
done

if [ \( -n "$CONF" \) -a \( -f "$CONF" \) ] ; then
	. "$CONF"
	case $CONF in
		/*) ORIG_DIR=`dirname $CONF` ;;
		*)  ORIG_DIR=`pwd`/`dirname $CONF` ;;
	esac
elif [ -f ./nutchwax_test_config ] ; then
	. ./nutchwax_test_config
	ORIG_DIR=`pwd`
else
	echo "Could not find config" 1>&2
	exit 1
fi

verb()
{
	if [ $VB -eq 1 ]; then 
		echo $1
	fi
}

# Move to temp directory
cd $WORKING_DIR

# Work out url for deployed nutchwax
DEPLOY_URL=`fgrep url= $WORKING_DIR/deployer.properties`
if [ $? -ne 0 ]; then
	NUTCH_URL='http://localhost:8080'
else
	NUTCH_URL=`echo $DEPLOY_URL | \
		sed -e 's/url=\(http:\/\/[^\/]*\)\/.*$/\1/'`
fi

NUTCH_DEPLOY_PATH=`awk '/^path=/ {a=split($0,b,"="); print b[2]} \
	END{if(a != "2") exit 1;}' $WORKING_DIR/deployer.properties`
if [ $? -ne 0 ]; then echo "Failed to get url of deployed war" 1>&2; exit 1; fi

DEPLOYED_WAR_URL=$NUTCH_URL$NUTCH_DEPLOY_PATH

# Check we have arcs
if [ -z "$ARCS" ]; then echo "No ARC files specified" 1>&2; exit 1; fi
# Go through arc files
for FILE in $ARCS
do
    # See if ARC file path is absolute.
    case $FILE in 
        /*) ;;
        *) FILE="$ORIG_FILE/$FILE";;
    esac
	# Get arcname
	ARCNAME=
	ARCNAME=`zgrep -a '^filedesc://' $FILE | cut -d ' ' -f 1`
	if [ -z "$ARCNAME" ]; 
		then echo "No arcname found in $$FILE" 2>&1; exit 1
	fi
	ARCNAME=${ARCNAME%.arc}
	ARCNAME=${ARCNAME#filedesc://}

	# How man documents are in the arc?
	DOCS=0
	DOCS=`zegrep -a '^http(s)?://' $FILE | wc -l | awk '{print $1}'`
	if [ "$DOCS" -le "0" ]; then
		echo "Empty arc / failure counting docs in arc: $FILE" 2>&1;
		exit 1
	fi

	QUERY="$DEPLOYED_WAR_URL$QUERY_URL$ARCNAME_STRING$ARCNAME"
	verb "testing search query $QUERY"
	RES=`lynx --source "$QUERY" | \
		awk "/Hits <b>/ {i++; if(\\$6 != \"$DOCS\") \
		{ print \"Found \" \\$6 \" matches\"; exit 1};} \
		END{if(i != \"1\") {print \"Malformed output\"; exit 1};}"`
	if [ $? -ne 0 ]; then
		echo "Failure searching for arcname:$ARCNAME" 1>&2
		echo "Looking for $DOCS matches, output: $RES" 1>&2
		exit 1
	fi
	QUERY="$DEPLOYED_WAR_URL$RSS_URL$ARCNAME_STRING$ARCNAME"
	verb "testing rss query $QUERY"
	RES=`lynx --source "$QUERY" | \
		awk -F "[<,>]" "/<opensearch:totalResults>/ \
		{i++; print \\$3;if(\\$3 != \"$DOCS\") \
		{ print  \"Found \" \\$3 \" matches\"; exit 1};} \
		END{if(i != \"1\") {print \"Malformed output\"; exit 1};}"`
	if [ $? -ne 0 ]; then
		echo "Failure rss feed arcname:$ARCNAME" 1>&2
		echo "Looking for $DOCS matches, output: $RES" 1>&2
		exit 1
	fi
done
