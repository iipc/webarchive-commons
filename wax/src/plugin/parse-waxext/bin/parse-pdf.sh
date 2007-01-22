#!/bin/bash
#
# PDF parser run by parse-waxext plugin.  Uses xpdf.  Its bundled as part of
# nutchwax.jar.  Its run by java Runtime.exec doing
# '/bin/sh ./bin/parse-pdfs.sh'.  Depends on CWD being set to unbundled
# job jar.
#
if  [ $# -ne 2 ]; then
  echo Usage:$0 fullPathToTmpDir mimeType >&2
  exit 22 
fi

if [ ! -e $1 ];
then
    echo "fullPathToTmpDir does not exist"
    exit 2 
fi
if [ ! -d $1 ];
then
    echo "fullPathToTmpDir is not a directory."
    # See /usr/include/asm-generic/errno-base.h
    exit 20 
fi
tmpfile="/$1/nutch$$.pdf"
mimetype=$2

# Clean up tmp file.  Called on signal and at end of script.
function cleanup {
    if [ -e $tmpfile ]
    then
        rm $tmpfile
    fi
}

# Trap handler.
trap cleanup ERR INT TERM

exitCode=0
case $mimetype in
    "application/pdf")
        cat > $tmpfile
        pdfinfo $tmpfile
        echo
        pdftotext $tmpfile -
        exitCode=$?
        ;;
    *)
        echo "Can't parse mimeType $mimetype" >&2
        ;;
esac
cleanup
exit $exitCode
