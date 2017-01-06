# webarchive-commons-uri
Configurable URI parsing and normalization library.

## Usage

Parsing a URI consist of four steps:
* Create a UriBuilderConfig object and modify it to your needs. The config object is immutable and safe to reuse by
multiple threads.
* Create a UriBuilder from the UriBuilderConfig. UriBuilders are *not* thread safe and you should create a new one for
every URI you want to parse.
* Set the URI on the UriBuilder or set any individual fields.
* Build an immutable Uri object by calling `build()` on the UriBuilder.

As you will see below, shortcuts exist for common uses.

```java
UriBuilderConfig conf = new UriBuilderConfig(); // Creates a new configuration with default values

Uri uri = conf.builder()                        // Creates a new UriBuilder initialized with the configuration
           .uri("http://example.com/path")      // Let the builder parse a URI
           .build();                            // Build an immutable Uri object from the builder
```

A shortcut for doing the above:

```java
Uri uri = new UriBuilderConfig().buildUri("http://example.com/path");
```

To tweak the configuration there are a lot of options. The modifying methods always creates a new config object
so it is important to create a new assignment for every method call like this:

```java
// OK
UriBuilderConfig conf = new UriBuilderConfig();
conf = conf.pathSegmentNormalization(true);
conf = conf.requireAbsoluteUri(false);

// WRONG
UriBuilderConfig conf = new UriBuilderConfig();
conf.pathSegmentNormalization(true);
conf.requireAbsoluteUri(false);
```
Since all the modifying methods returns a new config object, it is preferred to chain the calls. 

```java
UriBuilderConfig conf = new UriBuilderConfig()
                              .pathSegmentNormalization(true)
                              .requireAbsoluteUri(false)
                              .caseNormalization(true)
                              .schemeBasedNormalization(true)
                              .encodeIllegalCharacters(false)
                              .addNormalizer(new StripSlashAtEndOfPath());
```

As a convenience, several configurations already exist in the UriConfigs class. A common usage:
```java
Uri uri = UriConfigs.WHATWG.buildUri("http://example.com/path");
```

The `UriConfigs.WHATWG`-configuration normalizes URIs in compliance with [WHATWG's URL spec](https://url.spec.whatwg.org/)

