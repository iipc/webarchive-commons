#!/bin/sh
# nutchwax_check_types.sh
# Regression script by David Cathcart cathcart at archive dot org
# for nutchwax. 
# When run, we grep the arcfiles given in the configuration for content-types
# and then test that all the content-types can be successfully queried from the
# searchbox and rss feed and that the number of matches for each is equal to the
# number that exist in the arc file.
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
TYPE_STRING='type:'
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
	# Get all types 
	ARCTYPES=
	ARCTYPES=`zegrep -a '^http(s)?://' $FILE | \
		cut -d ' ' -f 4 | sort | uniq`
	if [ -z "$ARCTYPES" ]; 
		then echo "No content-types found in $FILE" 2>&1; exit 1
	fi
	# Need to specify arcname: in case we've indexed more than one arc
	ARCNAME=
	ARCNAME=`zgrep -a '^filedesc://' $FILE | cut -d ' ' -f 1`
	if [ -z "$ARCNAME" ];
		then echo "No arcname found in $FILE" 2>&1; exit 1
	fi
	ARCNAME=${ARCNAME%.arc}
	ARCNAME=${ARCNAME#filedesc://}

	# Test types
	for TYPE in $ARCTYPES
	do	
		QUERY="$DEPLOYED_WAR_URL$QUERY_URL$TYPE_STRING$TYPE%20$ARCNAME_STRING$ARCNAME"
		# Find the number of documents in arc matching $TYPE
		TYPE_DOCS=`zegrep -ac "^http(s)?://.*$TYPE" $FILE`
		if [ -z "$ARCTYPES" ];
			# Should not be reachable
			then echo "No $TYPE documents found in $FILE" 2>&1
			exit 1
		fi
		verb "testing search query $QUERY"
		RES=`lynx --source "$QUERY" | \
			awk "/Hits <b>/ {i++; if(\\$6 != \"$TYPE_DOCS\") \
			{ print \"Found \" \\$6 \" matches\"; exit 1};} \
			END{if(i != \"1\") {print \"Malformed output\"; exit 1};}"`
		if [ $? -ne 0 ]; then
			echo "Failure searching for type:$TYPE" 1>&2
			echo "Looking for $TYPE_DOCS matches, output: $RES" 1>&2
			exit 1
		fi
		QUERY="$DEPLOYED_WAR_URL$RSS_URL$TYPE_STRING$TYPE%20$ARCNAME_STRING$ARCNAME"
		verb "testing rss query $QUERY"
		RES=`lynx --source "$QUERY" | \
			awk -F "[<,>]" "/<opensearch:totalResults>/ \
			{i++; print \\$3;if(\\$3 != \"$TYPE_DOCS\") \
			{ print  \"Found \" \\$3 \" matches\"; exit 1};} \
			END{if(i != \"1\") {print \"Malformed output\"; exit 1};}"`
		if [ $? -ne 0 ]; then
			echo "Failure rss feed type:$TYPE" 1>&2
			echo "Looking for $TYPE_DOCS matches, output: $RES" 1>&2
			exit 1
		fi
	done
done
