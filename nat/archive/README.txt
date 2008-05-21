
README.txt
2008-05-20
Aaron Binns

Welcome to NutchWAX 0.12!

NutchWAX is a set of add-ons to Nutch in order to index and search
archived web data.

These add-ons are developed and maintained by the Internet Archive Web
Team in conjunction with a broad community of contributors, partners
and end-users.

The name "NutchWAX" stands for "Nutch (W)eb (A)rchive e(X)tensions".

Since NutchWAX is a set of add-ons to Nutch, you should already be
familiar with Nutch before using NutchWAX.

======================================================================

The goal of NutchWAX is to enable full-text indexing and searching of
documents stored in web archive file formats (ARC and WARC).

The way we achieve that goal is by providing add-on tools and plugins
to Nutch to read documents directly from ARC/WARC files.  We call this
process "importing" archive files.

Importing produces a Nutch segment, the same as if Nutch had actually
crawled the documents itself.  In this scenario, document importing
replaces the conventional "generate/fetch/update" cycle of Nutch.

Once the archival documents have been imported into a segment, the
regular Nutch commands to update the 'crawldb', invert the links and
index the document contents can proceed as normal.

======================================================================

The NutchWAX add-ons consist of:

 bin/nutchwax

   A shell script that is used to run the NutchWAX command-line tools,
   such as document importing.

   This is patterned after the 'bin/nutch' shell script.

 plugins/index-nutchwax

   Indexing plugin which adds NutchWAX-specific metadata fields to the
   indexed document.

 plugins/query-nutchwax

   Query plugin which allows for querying against the metadata fields
   added by 'index-nutchwax'.

There is no separate 'lib/nutchwax.jar' file for NutchWAX.  NutchWAX
is distributed in source code form and is intended to be built in
conjunction with Nutch.

See "INSTALL.txt" for details on building NutchWAX and Nutch.

See "HOWTO.txt" for a quick tutorial on importing, indexing and
searching a set of documents in a web archive file.

======================================================================

This 0.12 release of NutchWAX is radically different in source-code
form compared to the previous release, 0.10.

One of the design goals of 0.12 was to reduce or even eliminate the
"copy/paste/edit" approach of 0.10.  The 0.10 (and prior) NutchWAX
releases had to copy/paste/edit large chunks of Nutch source code in
order to add the NutchWAX features.

Also, the NutchWAX 0.12 sources and build are designed to one day be
added into mainline Nutch as a proper "contrib" package; then
eventually be fully integrated into the core Nutch source code.

======================================================================

Most of the NutchWAX source code is relatively straightfoward to those
already familiar with the inner workings of Nutch.  Still, special
attention on one class is worth while:

  src/java/org/archive/nutchwax/ArcsToSegment.java

This is where ARC/WARC files are read and their documents are imported
into a Nutch segment.

It is inspired by:

  nutch/src/java/org/apache/nutch/tools/arc/ArcSegmentCreator.java

on the Nutch SVN head.

Our implementation differs in a few important ways:

  o Rather than taking a directory with ARC files as input, we take a
    manifest file with URLs to ARC files.  This way, the manifest is
    split up among the distributed Hadoop jobs and the ARC files are
    processed in whole by each worker.

    In the Nutch SVN, the ArcSegmentCreator.java expects the input
    directory to contain the ARC files and (AFAICT) splits them up and
    distributes them across the Hadoop workers.

  o We use the standard Internet Archive ARCReader and WARCReader
    classes.  Thus, NutchWAX can read both ARC and WARC files, whereas
    the ArcSegmentCreator class can only read ARC files.

  o We add metadata fields to the document, which are then available
    to the "index-nutchwax" plugin at indexing-time.

    ArcsToSegment.importRecord()
      ...
      contentMetadata.set( NutchWax.CONTENT_TYPE_KEY, meta.getMimetype()          );
      contentMetadata.set( NutchWax.ARCNAME_KEY,      meta.getArcFile().getName() );
      contentMetadata.set( NutchWax.COLLECTION_KEY,   collectionName              );
      contentMetadata.set( NutchWax.DATE_KEY,         meta.getDate()              );
      ...
