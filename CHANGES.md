Unreleased
----------

3.0.2 (2025-11-14)
------------------

### Fixes

- Avoid relying on the default locale or charset. [#128](https://github.com/iipc/webarchive-commons/pull/128)
- BasicURLCanonicalizer: more efficient normalization of dots in host names. [#129](https://github.com/iipc/webarchive-commons/pull/129)

### Dependency upgrades

* **commons-cli**: 1.10.0 → 1.11.0
* **commons-codec**: 1.19.0 → 1.20.0
* **commons-io**: 2.20.0 → 2.21.0
* **junit-jupiter**: 5.13.3 → 5.14.1
* **maven-release-plugin**: 3.1.1 → 3.2.0

3.0.1 (2025-10-27)
------------------

### Fixes

* Fixed a file handle leak in `FileUtils.pagedLines()` and `FileUtils.appendTo()` that could occur during I/O errors.

### Dependency Upgrades

* **commons-codec**: 1.18.0 → 1.19.0
* **commons-lang3**: 3.18.0 → 3.19.0
* **commons-cli**: 1.9.0 → 1.10.0
* **guava**: 33.4.8-jre → 33.5.0-jre
* **hadoop**: 3.4.1 → 3.4.2
* **pig**: 0.17.0 → 0.18.0

3.0.0 (2025-07-21)
------------------

### Changes

`FileUtils.pagedLines()` and `FileUtils.expandRange()` now return the Apache Commons Lang 3 version of `LongRange`.
Users of these methods may need to make the following changes:

| Old                                             | New                                         |
|-------------------------------------------------|---------------------------------------------|
| `import org.apache.commons.lang.math.LongRange` | `import org.apache.commons.lang3.LongRange` |
| `new LongRange(min, max)`                       | `LongRange.of(min, max)`                    |
| `longRange.getMaximumLong()`                    | `longRange.getMaximum()`                    |
| `longRange.getMinimumLong()`                    | `longRange.getMinimum()`                    |

### Dependency upgrades

- **commons-io**: 2.19.0 → 2.20.0
- **commons-lang**: 2.6 → 3.18.0

2.0.2 (2025-07-15)
------------------

### Fixes

* Fixes for `org.archive.net.PublicSuffixes` [#110](https://github.com/iipc/webarchive-commons/pull/110)
  * Updated to the latest version of the public suffix list.
  * Fixed parsing failures with newer list versions.
  * Moved `effective_tld_names.dat` to `org/archive/effective_tld_names.dat` to prevent conflict with `crawler-commons`.

2.0.1 (2025-05-21)
------------------

### Changes

* Re-added `Reporter.shortReportLineTo(PrintWriter)` as it turned out to be important to Heritrix.


2.0.0 (2025-05-21)
------------------

### New features

- Added `RecordingInputStream.asOutputStream()` for direct writing of recorded data without an input stream. [#108](https://github.com/iipc/webarchive-commons/pull/108)

### Removals

#### Removed Apache HttpClient 3.1

`HTTPSeekableLineReaderFactory` and `ZipNumBlockLoader` now default to HttpClient 4.3.

| Removed                                                   | Replacement                          |
|-----------------------------------------------------------|--------------------------------------|
| `org.apache.commons.httpclient.URIException`              | `org.archive.url.URIException`       |
| `org.apache.commons.httpclient.Header`                    | `org.archive.format.http.HttpHeader` |
| `org.archive.httpclient.HttpRecorderGetMethod`            |                                      |
| `org.archive.httpclient.HttpRecorderMethod`               |                                      |
| `org.archive.httpclient.HttpRecorderPostMethod`           |                                      |
| `org.archive.httpclient.SingleHttpConnectionManager`      |                                      |
| `org.archive.httpclient.ThreadLocalHttpConnectionManager` |                                      |

#### Removed deprecated versions of renamed classes

| Removed                                       | Replacement                                      |
|-----------------------------------------------|--------------------------------------------------|
| `org.archive.io.ArchiveFileConstants`         | `org.archive.format.ArchiveFileConstants`        |
| `org.archive.io.GzipHeader`                   | `org.archive.util.zip.GzipHeader`                |
| `org.archive.io.GZIPMembersInputStream`       | `org.archive.util.zip.GZIPMembersInputStream`    |
| `org.archive.io.NoGzipMagicException`         | `org.archive.util.zip.NoGzipMagicException`      |
| `org.archive.io.arc.ARCConstants`             | `org.archive.format.arc.ARCConstants`            |
| `org.archive.io.warc.WARCConstants`           | `org.archive.format.warc.WARCConstants`          |
| `org.archive.url.DefaultIACanonicalizerRules` | `org.archive.url.AggressiveIACanonicalizerRules` |
| `org.archive.url.DefaultIAURLCanonicalizer`   | `org.archive.url.AggressiveIAURLCanonicalizer`   |
| `org.archive.url.GoogleURLCanonicalizer`      | `org.archive.url.BasicURLCanonicalizer`          |

#### Removed deprecated methods

| Removed                                       | Replacement                               |
|-----------------------------------------------|-------------------------------------------|
| `ANVLRecord(int)`                             | `ANVLRecord()`                            |
| `DevUtils.betterPrintStack(RuntimeException)` | `Throwable.printStackStrace()`            |
| `Recorder.getReplayCharSequence()`            | `Recorder.getContentReplayCharSequence()` |
| `Reporter.shortReportLineTo(PrintWriter)`     | `Reporter.reportTo(PrintWriter)`          |

##### Removed usages of constant interfaces

Static imports should be used instead.

* `ArchiveFileConstants` is no longer implemented by:
  * `ArchiveReader`
  * `ArchiveReaderFactory`
  * `WARCWriter`
  * `WriterPool`
  * `WriterPoolMember`
* `ARCConstants` is no longer implemented by:
  * `ARCReader`
  * `ARCReaderFactory`
  * `ARCRecord`
  * `ARCRecordMetaData`
  * `ARCUtils`
  * `ARCWriter`
* `WARCConstants` is no longer implemented by:
  * `WARCReader`
  * `WARCReaderFactory`
  * `WARCRecord`
  * `WARCWriter`

### Dependency upgrades

- **commons-io**: 2.18.0 → 2.19.0
- **guava**: 33.3.1-jre → 33.4.8-jre
- **json**: 20240303 → 20250517
- **junit**: 4.13.2 → 5.12.2

1.3.0 (2024-12-20)
------------------

#### URL Canonicalization Changed

The output of WaybackURLKeyMaker and other canonicalizers based on BasicURLCanonicalizer has changed for URLs that
contain non UTF-8 percent encoded sequences. For example when a URL contains "%C3%23" it will now be normalised to
"%c3%23" whereas previous releases produced "%25c3%23". This change brings webarchive-commons more inline with pywb,
surt (Python), warcio.js and RFC 3986. While CDX file compatibility with these newer tools should improve, note that CDX
files generated by the new release which contain such URLs may not work correctly with existing versions of 
OpenWayback that use the older webarchive-commons. [#102](https://github.com/iipc/webarchive-commons/pull/102)

#### Bug fixes

* WAT: Duplicated payload metadata values for "Actual-Content-Length" and "Trailing-Slop-Length" [#103](https://github.com/iipc/webarchive-commons/pull/103)
* ObjectPlusFilesOutputStream.hardlinkOrCopy now uses `Files.createLink()` instead of executing `ln`. This
  prevents the potential for security vulnerabilities from command line option injection and improves portability.

#### Dependency upgrades

* fastutil removed
* dsiutils removed

#### Deprecations

The following classes and enum members have been marked deprecated as a step towards removal of the dependency on
Apache Commons HttpClient 3.1.

* org.archive.httpclient.HttpRecorderGetMethod
* org.archive.httpclient.HttpRecorderMethod
* org.archive.httpclient.HttpRecorderPostMethod
* org.archive.httpclient.SingleHttpConnectionManager
* org.archive.httpclient.ThreadLocalHttpConnectionManager
* org.archive.util.binsearch.impl.http.ApacheHttp31SLR
* org.archive.util.binsearch.impl.http.ApacheHttp31SLRFactory
* org.archive.util.binsearch.impl.http.HTTPSeekableLineReaderFactory.HttpLibs.APACHE_31

1.2.0 (2024-11-29)
------------------

#### New features

* MetaData is now multivalued to support repeated WARC and HTTP headers. [#98](https://github.com/iipc/webarchive-commons/pull/98/files)

#### Dependency upgrades

* commons-io 2.18.0
* commons-lang 2.6
* guava 33.3.1-jre
* hadoop 3.4.1
* htmlparser 2.1
* httpcore 4.4.16
* json 20240303
* junit 4.13.2

1.1.11 (2024-11-27)
-------------------

#### Bug fixes

* Fixed URLParser and WaybackURLKeyMaker failing on URLs with IPv6 address hostnames [#100](https://github.com/iipc/webarchive-commons/pull/100)

1.1.10 (2024-10-15)
-------------------

#### Bug fixes

* [WAT extractor: do not fail on missing WARC-Filename in warcinfo record](https://github.com/iipc/webarchive-commons/pull/89)
* [ExtractingParseObserver: extract rel, hreflang and type attributes](https://github.com/iipc/webarchive-commons/pull/86)
* [ExtractingParseObserver: extract links from onClick attributes](https://github.com/iipc/webarchive-commons/pull/85)

#### Dependency Upgrades

* commons-collections 3.2.2
* commons-io 2.7
* dsiutils 2.2.8
* guava 33.3.0-jre
* hadoop 3.4.0 (now optional)
* pig 0.17.0
* org.json 20231013

#### Dependency Removals

* joda-time (was unused)

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
