1.1.10
------
* [WAT extractor: do not fail on missing WARC-Filename in warcinfo record](https://github.com/iipc/webarchive-commons/pull/89)
* [ExtractingParseObserver: extract rel, hreflang and type attributes](https://github.com/iipc/webarchive-commons/pull/86)
* [ExtractingParseObserver: extract links from onClick attributes](https://github.com/iipc/webarchive-commons/pull/85)
* [Update TravisCI config](https://github.com/iipc/webarchive-commons/pull/83)

1.1.9
-----
* [Use commons-collections v3.2.2 to avoid v3.2.1 vulnerability](https://github.com/iipc/webarchive-commons/pull/77)
* [Extract `property` attributes of HTML meta elements](https://github.com/iipc/webarchive-commons/pull/75)
* [Do not add value of preceding HTTP header field if there is no value](https://github.com/iipc/webarchive-commons/pull/74)
* [Fix WAT records corresponding to response records of Wget generated WARCs](https://github.com/iipc/webarchive-commons/pull/74)

1.1.8
-----
* [Improve HTML link extraction](https://github.com/iipc/webarchive-commons/pull/72)
* [Move unit tests over from heritrix3 to webarchive-commons](https://github.com/iipc/webarchive-commons/issues/25)
* [Strip empty port via URLParser](https://github.com/iipc/webarchive-commons/pull/69/)
* [Use CharsetDetector to guess encoding of HTML documents](https://github.com/iipc/webarchive-commons/pull/68/)
* [Fix last header was lost if LF LF](https://github.com/iipc/webarchive-commons/pull/65/)
* [Make regular expression to extract URLs from CSS more restrictive](https://github.com/iipc/webarchive-commons/pull/63)
* [Remove invalid constant `PROFILE_REVISIT_URI_AGNOSTIC_IDENTICAL_DIGEST`](https://github.com/iipc/webarchive-commons/pull/62)

1.1.7
-----
* [Make canonicalizer be able to strip session id params even if they are the first params in the query string](https://github.com/iipc/webarchive-commons/pull/54)
* [Store origin-code of ARC file header](https://github.com/iipc/webarchive-commons/pull/52/)
* [Flush output etc before tallying stats to fix sizeOnDisk calculation](https://github.com/iipc/webarchive-commons/pull/51)
* [Get rid of broken, seemingly unnecessary escapeWhitespace() step of uri fixup](https://github.com/iipc/webarchive-commons/pull/50)

1.1.6
-----
* [Handle empty String argument in CharsetDetector.trimAttrValue](https://github.com/iipc/webarchive-commons/pull/49)
* [WAT extractor: adding information in WAT's warcinfo](https://github.com/iipc/webarchive-commons/issues/47)
* [WAT extractor: missing WARC format version](https://github.com/iipc/webarchive-commons/issues/45)
* [WAT extractor: envelope structure does not conform to the WAT specification](https://github.com/iipc/webarchive-commons/issues/44)
* [WAT extractor: WARC-Date in all records should be the WAT record generation date](https://github.com/iipc/webarchive-commons/issues/43)
* [WAT extractor: WARC-Filename in the WAT warcinfo record should be the WAT filename itself](https://github.com/iipc/webarchive-commons/issues/42)
* [WAT extractor: Entity-Trailing-Slop-Bytes should be called Entity-Trailing-Slop-Length](https://github.com/iipc/webarchive-commons/issues/48)

1.1.5
-----
* [Escape redirect URLs in RealCDXExtractorOutput](https://github.com/iipc/webarchive-commons/pull/36)
* [Tests fail on Windows](https://github.com/iipc/webarchive-commons/issues/2)
* [Test fails on Java 8](https://github.com/iipc/webarchive-commons/issues/31)
* [RecordingOutputStream can affect tcp packets sent in an undesirable way](https://github.com/iipc/webarchive-commons/issues/38)

1.1.4
-----
* [All dates should be independent of locale settings](https://github.com/iipc/webarchive-commons/pull/22)
* [Resolved fastutil conflict in dependencies](https://github.com/iipc/webarchive-commons/pull/24)

1.1.3
-----
* [Synchronised with IA fork](https://github.com/iipc/webarchive-commons/pull/18)
* [Updated to more recent Guava APIs](https://github.com/iipc/webarchive-commons/pull/17)
* [Fixed handling of uncompressed ARC files #13 and #14](https://github.com/iipc/webarchive-commons/pull/14)
* [Avoid pulling in the logback dependency IA#13](https://github.com/internetarchive/webarchive-commons/pull/13)

1.1.2
-----
* [Fixed support for reading uncompressed WARCs, along with some unit testing.](https://github.com/iipc/webarchive-commons/pull/12)

1.1.1
-----
* [Renamed from commons-webarchive to webarchive-commons](https://github.com/iipc/webarchive-commons/pull/8)
* [Cope with malformed GZip extra fields as produced by wget 1.14](https://github.com/iipc/webarchive-commons/pull/10)
* [Switch to httpcomponents, and add IA deployment information.](https://github.com/iipc/webarchive-commons/pull/11)
