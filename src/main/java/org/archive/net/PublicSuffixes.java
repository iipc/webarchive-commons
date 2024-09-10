/*
 *  This file is part of the Heritrix web crawler (crawler.archive.org).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.archive.net;


import com.google.common.net.InternetDomainName;

/**
 * Utility class for making use of the information about 'public suffixes' at
 * http://publicsuffix.org.
 * 
 * The public suffix list (once known as 'effective TLDs') was motivated by the
 * need to decide on which broader domains a subdomain was allowed to set
 * cookies. For example, a server at 'www.example.com' can set cookies for
 * 'www.example.com' or 'example.com' but not 'com'. 'www.example.co.uk' can set
 * cookies for 'www.example.co.uk' or 'example.co.uk' but not 'co.uk' or 'uk'.
 * The number of rules for all top-level-domains and 2nd- or 3rd- level domains
 * has become quite long; essentially the broadest domain a subdomain may assign
 * to is the one that was sold/registered to a specific name registrant.
 * 
 * This concept should be useful in other contexts, too. Grouping URIs (or
 * queues of URIs to crawl) together with others sharing the same registered
 * suffix may be useful for applying the same rules to all, such as assigning
 * them to the same queue or crawler in a multi- machine setup.
 * 
 * As of Heritrix3, we prefer the term 'Assignment Level Domain' (ALD) 
 * for such domains, by analogy to 'Top Level Domain' (TLD) or '2nd Level 
 * Domain' (2LD), etc. 
 * 
 * @author Gojomo
 * 
 * This version of PublicSuffixes uses Google Guava's class for resolving public suffixes.
 */
public class PublicSuffixes {
    /**
     * Truncate SURT to its topmost assigned domain segment; that is, 
     * the public suffix plus one segment, but as a SURT-ordered prefix. 
     * 
     * if the pattern doesn't match, the passed-in SURT is returned.
     * 
     * @param surt SURT to truncate
     * @return truncated-to-topmost-assigned SURT prefix
     */
    public static String reduceSurtToAssignmentLevel(String surt) {
        String host = surtToHost(surt);
        String result;

        try {
            InternetDomainName idn = InternetDomainName.from(host);
            if (idn.hasPublicSuffix()) {
                // Has ALD, return the portion of this domain name that is one level beneath the public suffix.
                host = idn.topPrivateDomain().toString();
                result = hostToSurt(host);
            } else {
                // For any new/unknown TLDs assumes the second-level domain is assignable. (Eg: 'zzz,example,').
                int idx = surt.indexOf(',');
                if (idx > -1) {
                    idx = surt.indexOf(',', idx + 1);
                    if (idx > -1) {
                        result = surt.substring(0, idx + 1);
                    } else {
                        // Only one level domain, return unchanged (eg. 'exmaple,').
                        result = surt;
                    }
                } else {
                    // Found no commas. This is not legal for surt. Return unchanged.
                    result = surt;
                }
            }
        } catch (IllegalArgumentException e) {
            // Ip-address or illegal domain. Return unchanged;
            result = surt;
        }

        return result;
    }

    /**
     * Helper method for converting a host in SURT format to ordinary hostname format.
     * @param surt the SURT to convert
     * @return the converted hostname
     */
    static String surtToHost(String surt) {
        int len = surt.length() - 1;
        char[] surtArray = surt.toCharArray();
        char[] hostArray = new char[len];

        int partIdx = 0;
        for (int i = 0; i < len; i++) {
            if (surtArray[i] == ',') {
                hostArray[len - i - 1] = '.';
                System.arraycopy(surtArray, partIdx, hostArray, len - i, i - partIdx);
                partIdx = i + 1;
            }
        }
        System.arraycopy(surtArray, partIdx, hostArray, 0, len - partIdx);

        return String.valueOf(hostArray);
    }

    /**
     * Helper method for converting a hostname to SURT host format.
     * @param host the hostname to convert
     * @return the converted SURT version of hostname
     */
    static String hostToSurt(String host) {
        int len = host.length();
        char[] hostArray = host.toCharArray();
        char[] surtArray = new char[len + 1];
        int partIdx = 0;
        for (int i = 0; i < len; i++) {
            if (hostArray[i] == '.') {
                surtArray[len - i - 1] = ',';
                System.arraycopy(hostArray, partIdx, surtArray, len - i, i - partIdx);
                partIdx = i + 1;
            }
        }
        System.arraycopy(hostArray, partIdx, surtArray, 0, len - partIdx);
        surtArray[len] = ',';
        
        return String.valueOf(surtArray);
    }
}
