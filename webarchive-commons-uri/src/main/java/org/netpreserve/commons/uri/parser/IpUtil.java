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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.netpreserve.commons.uri.UriBuilder;
import org.netpreserve.commons.uri.UriException;

import static org.netpreserve.commons.uri.parser.Rfc3986Parser.HEX;

/**
 * Utility methods for parsing IP addresses.
 */
public class IpUtil {
    private IpUtil() {
    }
    public static String checkAndNormalizeIpv6(String ipv6) {
        List<String> ipv6Parts = split(':', ipv6);
        if (ipv6Parts.size() < 8) {
            List<String> ipv4Parts = split('.', ipv6Parts.get(ipv6Parts.size() - 1));
            if (ipv4Parts.size() == 4) {
                ipv6Parts.set(ipv6Parts.size() - 1, Integer.toHexString(
                        (Integer.parseInt(ipv4Parts.get(0), 10) << 8) + Integer.parseInt(ipv4Parts.get(1), 10)));
                ipv6Parts.add(Integer.toHexString(
                        (Integer.parseInt(ipv4Parts.get(2), 10) << 8) + Integer.parseInt(ipv4Parts.get(3), 10)));
            }
        }

        boolean canConcatenate = ipv6Parts.size() == 8;
        boolean hasConcatenated = false;
        for (int i = 0; i < ipv6Parts.size(); i++) {
            int part;
            if (ipv6Parts.get(i).isEmpty()) {
                part = 0;
            } else {
                try {
                    part = Integer.parseInt(ipv6Parts.get(i), 16);
                } catch (NumberFormatException e) {
                    throw new UriException("Illegal IPv6 address: " + ipv6);
                }
            }
            if (canConcatenate) {
                if (hasConcatenated && part > 0) {
                    canConcatenate = false;
                    ipv6Parts.add(i++, ":");
                } else if (part == 0) {
                    ipv6Parts.remove(i--);
                    hasConcatenated = true;
                }
            }
        }
        return String.join(":", ipv6Parts);
    }

    public static String checkIpv4(String ipv4Address) {
        String[] octets = ipv4Address.split("\\.");
        if (octets.length != 4) {
            return null;
        }
        for (String octet : octets) {
            try {
                int value = Integer.parseInt(octet);
                if (value < 0 || value > 255) {
                    return null;
                }
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return ipv4Address;
    }

    public static String checkAndNormalizeIpv4(String ipv4Address) {
        try {
            ipv4Address = URLDecoder.decode(ipv4Address, "UTF-8");
        } catch (IllegalArgumentException e) {
            throw new UriException(e.getMessage());
        } catch (UnsupportedEncodingException ex) {
            // Should never happen
            throw new RuntimeException(ex);
        }

        String[] octets = ipv4Address.split("\\.");
        int octetCount = octets.length;
        if (octets[octetCount - 1].isEmpty()) {
            octetCount--;
        }
        if (octetCount <= 0 || octetCount > 4) {
            return null;
        }
        int[] numbers = new int[octetCount];
        for (int i = 0; i < octetCount; i++) {
            String octet = octets[i];
            if (octet.isEmpty() || !HEX.get(octet.charAt(0))) {
                return null;
            }
            try {
                numbers[i] = Integer.decode(octet);
                if (numbers[i] > 255) {
                    if (i < (octetCount - 1)) {
                        throw new UriException("Illegal IPv4 address: " + ipv4Address);
                    } else if (numbers[i] >= Math.pow(256, 5 - octetCount)) {
                        throw new UriException("Illegal IPv4 address: " + ipv4Address);
                    }
                }
            } catch (NumberFormatException e) {
                return null;
            }
        }

        long ipv4Number = numbers[octetCount - 1];
        for (int i = 0; i < octetCount - 1; i++) {
            ipv4Number += numbers[i] * Math.pow(256, 3 - i);
        }
        StringBuilder serializedIpv4Number = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            serializedIpv4Number.insert(0, ipv4Number % 256);
            if (i < 3) {
                serializedIpv4Number.insert(0, ".");
            }
            ipv4Number = ipv4Number / 256;
        }

        return serializedIpv4Number.toString();
    }

    static List<String> split(char splitChar, String src) {
        int off = 0;
        int next = 0;
        ArrayList<String> list = new ArrayList<>();
        while ((next = src.indexOf(splitChar, off)) != -1) {
            list.add(src.substring(off, next));
            off = next + 1;
        }
        list.add(src.substring(off, src.length()));
        return list;
    }

}
