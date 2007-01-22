#!/bin/sh
# nutchwax_deploy.sh
# Regression script by David Cathcart cathcart at archive dot org
# for nutchwax. 
# When run, we download the latest nutchwax, hadoop and tomcat deployer, 
# Index some arcs, unpack the nutchwax war, deploy it with the tomcat 
# deployer.
#
# Requirements:
#	Java 1.5
#	A running Tomcat 5.5 (change TOMCAT_VER if different)
#	Standard unix commands:
#		sh, which, echo, basename, awk, gnu tar, getopt
#		lynx, wget, patch
#
############################################################
# Don't be verbose by default
VB=0
# Set error
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

#Check $TAR exits
if [ ! -f `which $TAR` ]; then 
	echo "\"$TAR\" doesn't exist" 1>&2
        exit 1

fi

# Guess JAVA_HOME if not already defined.
if [ -z "$JAVA_HOME" ]
then
    JAVA_HOME=`which java | xargs dirname`
    JAVA_HOME=${JAVA_HOME%/bin}
fi
verb "JAVA_HOME set to $JAVA_HOME"

cd $WORKING_DIR

# Strip beginning of url to get hadoop tarfile
HADOOPFILE=`basename $HADOOP_URL 2>/dev/null`
# Clean up old hadoops -- all but the tar.gz.
find ${WORKING_DIR} -name 'hadoop-*' ! -name ${HADOOPFILE} -exec rm -rf {} \; 2> /dev/null

# Get hadoop 
H="${WORKING_DIR}/${HADOOPFILE}"
if [ -e "${H}" ]
then
    verb "Skipping hadoop download. Reusing ${H}."
else
    verb "running: wget -Nq $HADOOP_URL 2>/dev/null"
    wget -Nq $HADOOP_URL 2>/dev/null
fi
# Exit if failed to get hadoop nightly
if [[ $? -gt 0 ]]; then
	echo "Failed downloading hadoop" 1>&2
	exit 1
fi

# Get Tomcat deployer
TOMCATFILE=`basename $TOMCAT_DURL 2>/dev/null`
# Clean up old tomcat deployers -- all but the tar.gz.
find ${WORKING_DIR} -name 'tomcat-*' ! -name ${TOMCATFILE} -exec rm -rf {} \;
T="${WORKING_DIR}/${TOMCATFILE}"
if [ -e "${T}" ]
then
    verb "Skipping tomcat-deployer download. Reusing ${T}."
else
    verb "running wget -Nq $TOMCAT_DURL 2>/dev/null"
    wget -Nq $TOMCAT_DURL 2>/dev/null
fi
# Exit if failed to get tomcat deployer
if [[ $? -gt 0 ]]; then
	echo "Failed downloading tomcat deployer" 1>&2
        exit 1
fi

# First clean up old nutchwax and nutchwax data if any.
rm -rf "${WORKING_DIR}"/nutchwax* 2>/dev/null

# Get the latest nutch nightly
# First get the lastest build dir from archive-access
verb "Getting latest nutchwax head date..."
NUTCHURL1=`lynx --dump $NUTCHN 2>/dev/null | \
	awk '/. http:\/\/builds.archive.org/ {a=substr($2,length($2)-13); if(a > s) \
	{s=a; ss=$2}}; END{if(s<1) exit 1; else print ss}'`
# Exit if failure
if [[ $? -gt 0 ]]; then
	echo "Failed finding nutchwax nightly" 1>&2
        exit 1
fi
verb "Found $NUTCHURL1"

# Then Get the latest nutch from the builddir
# use -nc instead of -n since timestamping is off on this http server
verb "Downloading..."
NUTCHURL2=`lynx --dump $NUTCHURL1 2>/dev/null | \
	awk '/\. http:\/\/.*nutchwax.*[0-9]+\.tar\.gz/ \
	{r=system("wget -nc -q " $2); print $2; exit r}'`
# Exit if failure
if [[ $? -gt 0 ]]; then
	echo "Failed finding nutchwax nightly" 1>&2
        exit 1
fi

# Strip beginning of url to get nutchwax tarfile
NUTCHFILE=`basename $NUTCHURL2 2>/dev/null`

# Untar the files
cd $WORKING_DIR

verb "untarring hadoop"
HADOOP_D=`$TAR -zvxf $HADOOPFILE 2>&1 | \
	awk "{if(\"$VB\" && ! index(\\$0,\"/\")) print \\$0 > \"/dev/stderr\"; \
	else s=\\$0}; END{split(s,a,\"/\"); print a[1]}"`
if [[ $? -ne 0 ]]; then
        echo "Failed untarring hadoop" 1>&2
        exit 1
fi
verb "untarring nutchwax"
NUTCH_D=`$TAR -zvxf $NUTCHFILE 2>&1 | \
	awk "{if(\"$VB\" && ! index(\\$0,\"/\")) print \\$0 > \"/dev/stderr\"; \
	else s=\\$0;}; END{split(s,a,\"/\"); print a[1]}"`
if [[ $? -ne 0 ]]; then
	echo "Failed untarring nutchwax" 1>&2
        exit 1
fi
verb "untarring tomcat deployer"
TOMCAT_D=`$TAR -zvxf $TOMCATFILE 2>&1 | \
	awk "{if(\"$VB\" && ! index(\\$0,\"/\")) print \\$0 > \"/dev/stderr\"; \
	else s=\\$0}; END{split(s,a,\"/\"); print a[1]}"`
if [[ $? -ne 0 ]]; then
	echo "Failed untarrring tomcat deployer" 1>&2
        exit 1
fi

HADOOP_HOME=$WORKING_DIR/$HADOOP_D
NUTCHWAX_HOME=$WORKING_DIR/$NUTCH_D
TOMCAT_HOME=$WORKING_DIR/$TOMCAT_D

NUTCHWAX_JAR=${NUTCHFILE%.tar.gz}.jar

# Make input and output directories for indexing
verb "Making directory structure for indexing"
mkdir $WORKING_DIR/input 2>/dev/null
rm -f -R $WORKING_DIR/output 2>/dev/null
mkdir $WORKING_DIR/output 2>/dev/null
rm -f $WORKING_DIR/input/arcs.txt 2>/dev/null

verb "Building list of arcs"
for FILE in $ARCS
do 
	case $FILE in
		/*) echo $FILE >> $WORKING_DIR/input/arcs.txt ;;
		 *) echo $ORIG_DIR/$FILE >> $WORKING_DIR/input/arcs.txt ;;
	esac
done

verb "Beginning indexing"
# Indexing steps since all does dedupe which breaks regression
arg_i[1]="import $WORKING_DIR/input $WORKING_DIR/output test-collection"
arg_i[2]="update $WORKING_DIR/output"
arg_i[3]="invert $WORKING_DIR/output"
arg_i[4]="index $WORKING_DIR/output"
arg_i[5]="merge $WORKING_DIR/output"
i=1

while [ $i -le ${#arg_i[@]} ]; do
	if [ $VB -eq 1 ]; then
		env HADOOP_HEAPSIZE=$HADOOP_HEAPSIZE JAVA_HOME=$JAVA_HOME \
		$HADOOP_HOME/bin/hadoop jar $NUTCHWAX_HOME/$NUTCHWAX_JAR \
		${arg_i[$i]}
		if [[ $? -gt 0 ]]; then
			echo "Failed building index of test collection" 1>&2
	        	exit 1
		fi
	else
		ERR=`env HADOOP_HEAPSIZE=$HADOOP_HEAPSIZE JAVA_HOME=$JAVA_HOME \
		$HADOOP_HOME/bin/hadoop jar $NUTCHWAX_HOME/$NUTCHWAX_JAR \
		${arg_i[$i]} 2>&1`
		if [[ $? -gt 0 ]]; then
			echo "Failed building index of test collection" 1>&2
	        	echo "error: $err" 1>&2
			exit 1
		fi	
	fi
	i=$(($i + 1))
done
# Move properties file to tomcat dir
cp $WORKING_DIR/deployer.properties $TOMCAT_HOME 2>&1

# Unpack the nutchwar
verb "unpacking nutch.war"
cd $TOMCAT_HOME
mkdir nutchwax 2>/dev/null
cd nutchwax
jar xf $NUTCHWAX_HOME/nutchwax.war
if [[ $? -gt 0 ]]; then
	echo "Failed unpacking nutchwax war" 1>&2
        exit 1
fi

cd $TOMCAT_HOME
# XXX hack to stop compiler not found error
verb "Adding dependencies to stop compiler errors"
cp -f nutchwax/WEB-INF/lib/commons-* lib/ 2>/dev/null
cp -f nutchwax/WEB-INF/lib/log4j* lib/ 2>/dev/null
cp -f nutchwax/WEB-INF/lib/archive-commons-* lib/ 2>/dev/null
if [[ $? -gt 0 ]]; then
	echo "Failed adding dependencies to stop compiler errors" 1>&2
        exit 1
fi


# Munge the build.xml to exclude jsp pages
verb "Editing files"
patch -s -p0 << EOF
--- build.xml.orig	Wed Jul 19 17:42:57 2006
+++ build.xml	Wed Jul 19 17:44:17 2006
@@ -67,6 +67,7 @@
       </classpath>
       <include name="**" />
       <exclude name="tags/**" />
+      <exclude name="**/more_jsp*" />
     </javac>
 
     <jar destfile="\${webapp.path}.war"
EOF
if [[ $? -gt 0 ]]; then
	echo "Failed patching build.xml" 1>&2
        exit 1
fi


# Modify hadoop-site.xml to specify location of index
patch -s -p0 << EOF
--- nutchwax/WEB-INF/classes/hadoop-site.xml.orig	Thu Jul 20 15:43:18 2006
+++ nutchwax/WEB-INF/classes/hadoop-site.xml	Thu Jul 20 15:45:11 2006
@@ -4,5 +4,9 @@
 <!-- Put site-specific property overrides in this file. -->
 
 <configuration>
+<property>
+  <name>searcher.dir</name>
+  <value>$WORKING_DIR/output</value>
+</property>
 
 </configuration>
EOF
if [[ $? -gt 0 ]]; then
	echo "Failed patching hadoop-site.xml" 1>&2
        exit 1
fi

verb "Starting ant compile of war"
if [ $VB -eq 1 ]; then
	env JAVA_HOME=$JAVA_HOME "${ANT}" compile
else 
	ERR=`env JAVA_HOME=$JAVA_HOME "${ANT}" compile 2>&1`
fi
if [[ $? -gt 0 ]]; then
        echo "Failed compiling war" 1>&2
	echo "error: $ERR" 1>&2 
        exit 1
fi

verb "Starting deployment of war"
if [ $VB -eq 1 ]; then
	env JAVA_HOME=$JAVA_HOME "${ANT}" deploy
else
	ERR=`env JAVA_HOME=$JAVA_HOME "${ANT}" deploy 2>&1`
fi

if [[ $? -gt 0 ]]; then
        echo "Failed deploying webapp" 1>&2           
	echo "error: $ERR" 1>&2
        exit 1
fi

