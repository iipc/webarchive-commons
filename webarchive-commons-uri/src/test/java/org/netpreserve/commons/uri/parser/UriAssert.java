/*
 * Copyright 2016 The International Internet Preservation Consortium.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netpreserve.commons.uri.parser;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.util.Objects;
import org.netpreserve.commons.uri.Uri;
import org.netpreserve.commons.uri.UriFormat;

/**
 * Assertion class for Uri.
 */
public class UriAssert extends AbstractAssert<UriAssert, Uri> {

    private UriFormat uriFormat;

    String errorMsg = "Expecting %s of: <%s> to be: <%s> but was: <%s>";

    /**
     * Default constructor.
     * <p>
     * @param actual the object to test
     */
    public UriAssert(Uri actual) {
        super(actual, UriAssert.class);
        uriFormat = actual.getDefaultFormat();
    }

    /**
     * Entry point for UriAssert. Use it with static import.
     * @param actual the object under test
     * @return the assertion
     */
    public static UriAssert assertThat(Uri actual) {
        return new UriAssert(actual);
    }

    /**
     * Verifies that the actual Uri's getScheme is equal to the given one.
     * <p>
     * @param value the value to compare the actual Uri's getScheme to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Uri's getScheme is not equal to the given one.
     */
    public UriAssert hasScheme(String value) {
        isNotNull();

        String actualValue = actual.getScheme();
        if (!Objects.areEqual(actualValue, value)) {
            failWithMessage(errorMsg, "scheme", actual, value, actualValue);
        }

        return this;
    }

    /**
     * Verifies that the actual Uri's getAuthority is equal to the given one.
     * <p>
     * @param value the value to compare the actual Uri's getAuthority to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Uri's getAuthority is not equal to the given one.
     */
    public UriAssert hasAuthority(String value) {
        isNotNull();

        String actualValue = actual.getAuthority();
        if (!Objects.areEqual(actualValue, value)) {
            failWithMessage(errorMsg, "authority", actual, value, actualValue);
        }

        return this;
    }

    /**
     * Verifies that the actual Uri's getUserinfo is equal to the given one.
     * <p>
     * @param value the value to compare the actual Uri's getUserinfo to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Uri's getUserinfo is not equal to the given one.
     */
    public UriAssert hasUserinfo(String value) {
        isNotNull();

        String actualValue = actual.getUserinfo();
        if (!Objects.areEqual(actualValue, value)) {
            failWithMessage(errorMsg, "userinfo", actual, value, actualValue);
        }

        return this;
    }

    /**
     * Verifies that the actual Uri's getHost is equal to the given one.
     * <p>
     * @param value the value to compare the actual Uri's getHost to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Uri's getHost is not equal to the given one.
     */
    public UriAssert hasHost(String value) {
        isNotNull();

        String actualValue = actual.getHost();
        if (!Objects.areEqual(actualValue, value)) {
            failWithMessage(errorMsg, "host", actual, value, actualValue);
        }

        return this;
    }

    /**
     * Verifies that the actual Uri's getDecodedHost is equal to the given one.
     * <p>
     * @param value the value to compare the actual Uri's getDecodedHost to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Uri's getDecodedHost is not equal to the given one.
     */
    public UriAssert hasDecodedHost(String value) {
        isNotNull();

        String actualValue = actual.getDecodedHost();
        if (!Objects.areEqual(actualValue, value)) {
            failWithMessage(errorMsg, "decodedHost", actual, value, actualValue);
        }

        return this;
    }

    /**
     * Verifies that the actual Uri's getPort is equal to the given one.
     * <p>
     * @param value the value to compare the actual Uri's getPort to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Uri's getPort is not equal to the given one.
     */
    public UriAssert hasPort(int value) {
        isNotNull();

        int actualValue = actual.getPort();
        if (!Objects.areEqual(actualValue, value)) {
            failWithMessage(errorMsg, "port", actual, value, actualValue);
        }

        return this;
    }

    /**
     * Verifies that the actual Uri's getDecodedPort is equal to the given one.
     * <p>
     * @param value the value to compare the actual Uri's getDecodedPort to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Uri's getDecodedPort is not equal to the given one.
     */
    public UriAssert hasDecodedPort(int value) {
        isNotNull();

        int actualValue = actual.getDecodedPort();
        if (!Objects.areEqual(actualValue, value)) {
            failWithMessage(errorMsg, "decodedPort", actual, value, actualValue);
        }

        return this;
    }

    /**
     * Verifies that the actual Uri's getPath is equal to the given one.
     * <p>
     * @param value the value to compare the actual Uri's getPath to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Uri's getPath is not equal to the given one.
     */
    public UriAssert hasPath(String value) {
        isNotNull();

        String actualValue = actual.getPath();
        if (!Objects.areEqual(actualValue, value)) {
            failWithMessage(errorMsg, "path", actual, value, actualValue);
        }

        return this;
    }

    /**
     * Verifies that the actual Uri's getDecodedPath is equal to the given one.
     * <p>
     * @param value the value to compare the actual Uri's getDecodedPath to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Uri's getDecodedPath is not equal to the given one.
     */
    public UriAssert hasDecodedPath(String value) {
        isNotNull();

        String actualValue = actual.getDecodedPath();
        if (!Objects.areEqual(actualValue, value)) {
            failWithMessage(errorMsg, "decodedPath", actual, value, actualValue);
        }

        return this;
    }

    /**
     * Verifies that the actual Uri's getQuery is equal to the given one.
     * <p>
     * @param value the value to compare the actual Uri's getQuery to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Uri's getQuery is not equal to the given one.
     */
    public UriAssert hasQuery(String value) {
        isNotNull();

        String actualValue = actual.getQuery();
        if (!Objects.areEqual(actualValue, value)) {
            failWithMessage(errorMsg, "query", actual, value, actualValue);
        }

        return this;
    }

    /**
     * Verifies that the actual Uri's getFragment is equal to the given one.
     * <p>
     * @param value the value to compare the actual Uri's getFragment to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Uri's getFragment is not equal to the given one.
     */
    public UriAssert hasFragment(String value) {
        isNotNull();

        String actualValue = actual.getFragment();
        if (!Objects.areEqual(actualValue, value)) {
            failWithMessage(errorMsg, "fragment", actual, value, actualValue);
        }

        return this;
    }

    /**
     * Verifies that the actual Uri's isAbsolute is equal to the given one.
     * <p>
     * @param value the value to compare the actual Uri's isAbsolute to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Uri's isAbsolute is not equal to the given one.
     */
    public UriAssert isAbsolute(boolean value) {
        isNotNull();

        boolean actualValue = actual.isAbsolute();
        if (!Objects.areEqual(actualValue, value)) {
            failWithMessage(errorMsg, "isAbsolute", actual, value, actualValue);
        }

        return this;
    }

    /**
     * Verifies that the actual Uri's isAbsolutePath is equal to the given one.
     * <p>
     * @param value the value to compare the actual Uri's isAbsolutePath to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Uri's isAbsolutePath is not equal to the given one.
     */
    public UriAssert isAbsolutePath(boolean value) {
        isNotNull();

        boolean actualValue = actual.isAbsolutePath();
        if (!Objects.areEqual(actualValue, value)) {
            failWithMessage(errorMsg, "isAbsolutePath", actual, value, actualValue);
        }

        return this;
    }

    /**
     * Verifies that the actual Uri's isIPv4address is equal to the given one.
     * <p>
     * @param value the value to compare the actual Uri's isIPv4address to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Uri's isIPv4address is not equal to the given one.
     */
    public UriAssert isIPv4address(boolean value) {
        isNotNull();

        boolean actualValue = actual.isIPv4address();
        if (!Objects.areEqual(actualValue, value)) {
            failWithMessage(errorMsg, "isIPv4address", actual, value, actualValue);
        }

        return this;
    }

    /**
     * Verifies that the actual Uri's isIPv6reference is equal to the given one.
     * <p>
     * @param value the value to compare the actual Uri's isIPv6reference to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Uri's isIPv6reference is not equal to the given one.
     */
    public UriAssert isIPv6reference(boolean value) {
        isNotNull();

        boolean actualValue = actual.isIPv6reference();
        if (!Objects.areEqual(actualValue, value)) {
            failWithMessage(errorMsg, "isIPv6reference", actual, value, actualValue);
        }

        return this;
    }

    /**
     * Verifies that the actual Uri's isRegistryName is equal to the given one.
     * <p>
     * @param value the value to compare the actual Uri's isRegistryName to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Uri's isRegistryName is not equal to the given one.
     */
    public UriAssert isRegistryName(boolean value) {
        isNotNull();

        boolean actualValue = actual.isRegistryName();
        if (!Objects.areEqual(actualValue, value)) {
            failWithMessage(errorMsg, "isRegistryName", actual, value, actualValue);
        }

        return this;
    }

    /**
     * Set the format to use for testing toCustomString.
     *
     * @param format the format to use.
     * @return  this assertion object.
     */
    public UriAssert usingCustomUriFormat(UriFormat format) {
        this.uriFormat = format;
        return this;
    }

    /**
     * Verifies that the actual Uri's toCustomString is equal to the given one.
     * <p>
     * @param value the value to compare the actual Uri's toCustomString to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Uri's toCustomString is not equal to the given one.
     */
    public UriAssert hasToCustomString(String value) {
        isNotNull();

        String actualValue = actual.toCustomString(uriFormat);
        if (!Objects.areEqual(actualValue, value)) {
            failWithMessage(errorMsg, "toCustomString", actual, value, actualValue);
        }

        return this;
    }

    /**
     * Verifies that the actual Uri's toDecodedString is equal to the given one.
     * <p>
     * @param value the value to compare the actual Uri's toDecodedString to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Uri's toDecodedString is not equal to the given one.
     */
    public UriAssert hasToDecodedString(String value) {
        isNotNull();

        String actualValue = actual.toDecodedString();
        if (!Objects.areEqual(actualValue, value)) {
            failWithMessage(errorMsg, "toDecodedString", actual, value, actualValue);
        }

        return this;
    }
}
