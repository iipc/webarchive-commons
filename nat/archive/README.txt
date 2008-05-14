
README.txt
2008-05-06
Aaron Binns


This is the NutchWAX-0.12 source that John Lee handed-off to me.  It
is a work-in-progress.

Compared to NutchWAX-0.10 (and earlier) it is *much* simpler.  The
main WAX-specific code is in just a few files really:

src/java/org/archive/nutchwax/ArcsToSegment.java

  This is the meat of the WAX logic for processing .arc files and
  generating Nutch segments.  Once we use this to generate a set of
  segments for the .arc files, we can use the rest of vanilla
  Nutch-1.0-dev to invert links and index the content with Lucene.

  This conversion code is heavily edited from:

    nutch-1.0-dev/src/java/org/apache/nutch/tools/arc/ArcSegmentCreator.java

  taken from the Nutch SVN head (a.k.a the "1.0-dev" in-development).

  Ours differs in a few important ways:

    o Rather than taking a directory with .arc files as input, we take
      a manifest file with URLs to .arc files.  This way, the manifest
      is split up among the distributed Hadoop jobs and the .arc files
      are processed in whole by each worker.

      In the Nutch-1.0-dev, the ArcSegmentCreator.java expects the
      input directory to contain the .arc files and (AFAICT) splits
      them up and distributes them across the Hadoop workers.  This
      seems really inefficient to me, I think our approach is much
      better -- at least for us.

    o Related to the way input files are split and processed, we use
      the standard Archive ARCReader class just like Heritrix and
      Wayback.

      The ArcSegmentCreator.java in Nutch-1.0-dev doesn't use our
      ARCReader because of licensing imcompatibility.  Ours is under
      GPL and Nutch-1.0-dev forbids the use of GPL code.
      
      We are in the process of re-licensing or dual-licensing with
      Apache License, but until then, our ARCReader code won't be incldued      
      in mainline Nutch.

      This isn's a problem per se, but worth noting in case anyone
      looks at the Nutch-1.0-dev code and wonders why they built their
      own (horribly inefficient) .arc reader.

    o We add metadata fields to the processed document for WAX-specific
      purposes:

        content.getMetadata().set( NutchWax.CONTENT_TYPE_KEY, meta.getMimetype() );
        content.getMetadata().set( NutchWax.ARCNAME_KEY,      meta.getArcFile().getName() ) ;
        content.getMetadata().set( NutchWax.COLLECTION_KEY,   collection);
        content.getMetadata().set( NutchWax.ARCHIVE_DATE_KEY, meta.getDate() );

      The addition of the arcname and collection key is pretty
      obvious.  I don't know why the content-type isn't added in the
      vanilla Nutch-1.0-dev.
      
      Also, we should review the use of the ARCHIVE_DATE_KEY in that
      John Lee mentioned to me that there was possibly duplicate date
      fields put in the index: one that is a plain old Java date, and
      one that is a 14-digit date string for use with Wayback.

src/java/plugin/index-nutchwax/src/java/org/archive/nutchwax/index/NutchWaxIndexingFilter.java
src/java/plugin/index-nutchwax/plugin.xml

  This filter is pretty straightforward.  All it does is take the
  metadata fields that were added to the document (as described above)
  and placed in the Lucene index so that we can make use of them at
  search-time.

src/java/plugin/query-nutchwax/src/java/org/archive/nutchwax/query/MultipleFieldQueryFilter.java
src/java/plugin/query-nutchwax/plugin.xml

  This is a single query filter that can be used for querying single
  fields from a single implementation.  It does *not* allow for
  querying multiple fields as you can already do that via Nutch.

  What this filter does is allows one to more-or-less create query
  filters in a data-driven manner rather than having to code-up a new
  class for each field.  That is, before one would have to create a
  CollectionQueryFilter class to filter on the "collection" field.
  With the MultipleFieldQueryFilter class, you can specify that the
  "collection" field is to be filterable via the plugin.xml file and
  "nutchwax.filter.query" configuration property.

src/java/org/archive/nutchwax/NutchWax.java

  Just a simple enum used by the above two classes for the metadata
  keys.

src/java/org/archive/nutchwax/tools/DumpIndex.java

  A simple command-line utility to dump the contents of a Lucene
  index.  Used for debugging.


