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
import java.math.BigInteger;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.netpreserve.commons.uri.UriException;

import static org.netpreserve.commons.uri.parser.Rfc3986Parser.HEX;

/**
 * Utility methods for parsing IP addresses.
 */
public final class IpUtil {

    private static final BigInteger RADIX_85 = BigInteger.valueOf(85);

    private static final BigInteger RADIX_0X10000 = BigInteger.valueOf(0x10000);

    private final static char[] base85Charset = new char[85];

    static {
        int j = 0;
        for (char i = '0'; i <= '9'; i++) {
            base85Charset[j++] = i;
        }
        for (char i = 'A'; i <= 'Z'; i++) {
            base85Charset[j++] = i;
        }
        for (char i = 'a'; i <= 'z'; i++) {
            base85Charset[j++] = i;
        }
        base85Charset[j++] = '!';
        base85Charset[j++] = '#';
        base85Charset[j++] = '$';
        base85Charset[j++] = '%';
        base85Charset[j++] = '&';
        base85Charset[j++] = '(';
        base85Charset[j++] = ')';
        base85Charset[j++] = '*';
        base85Charset[j++] = '+';
        base85Charset[j++] = '-';
        base85Charset[j++] = ';';
        base85Charset[j++] = '<';
        base85Charset[j++] = '=';
        base85Charset[j++] = '>';
        base85Charset[j++] = '?';
        base85Charset[j++] = '@';
        base85Charset[j++] = '^';
        base85Charset[j++] = '_';
        base85Charset[j++] = '`';
        base85Charset[j++] = '{';
        base85Charset[j++] = '|';
        base85Charset[j++] = '}';
        base85Charset[j++] = '~';
    }

    /**
     * Private constructor to avoid instantiation of utility class.
     */
    private IpUtil() {
    }

    public static BigInteger parseIpv6(String ipv6String) {
        BigInteger ipv6Number;
        List<String> ipv6Parts = split(':', ipv6String);

        if (ipv6Parts.size() > 8) {
            throw new UriException("Too many segments in IPv6 address: " + ipv6String);
        }

        // Check if base85 encoded
        if (ipv6Parts.size() == 1) {
            if (ipv6String.length() != 20) {
                throw new UriException("Illegal IPv6 address: " + ipv6String);
            }

            ipv6Number = BigInteger.ZERO;
            for (int i = 0; i < 20; i++) {
                int val = -1;
                for (int j = 0; j < base85Charset.length; j++) {
                    if (ipv6String.charAt(i) == base85Charset[j]) {
                        val = j;
                        break;
                    }
                }
                ipv6Number = ipv6Number.add(BigInteger.valueOf(val).multiply(RADIX_85.pow(19 - i)));
            }
            return ipv6Number;
        }

        // Used to check that there are enough parts in the IPv6 string
        int minimumNumberOfParts = 8;

        if (ipv6Parts.size() < 8) {
            List<String> ipv4Parts = split('.', ipv6Parts.get(ipv6Parts.size() - 1));
            if (ipv4Parts.size() == 4) {
                ipv6Parts.set(ipv6Parts.size() - 1, Integer.toHexString(
                        (Integer.parseInt(ipv4Parts.get(0), 10) << 8) + Integer.parseInt(ipv4Parts.get(1), 10)));
                ipv6Parts.add(Integer.toHexString(
                        (Integer.parseInt(ipv4Parts.get(2), 10) << 8) + Integer.parseInt(ipv4Parts.get(3), 10)));
                minimumNumberOfParts = 7;
            }
        }

        long[] numbers = new long[8];
        int pIdx = 0;
        int nIdx = 0;
        while (pIdx < ipv6Parts.size()) {
            String part = ipv6Parts.get(pIdx);
            if (part.isEmpty()) {
                minimumNumberOfParts = 0;
                while (pIdx < ipv6Parts.size() && ipv6Parts.get(pIdx).isEmpty()) {
                    ipv6Parts.remove(pIdx);
                }
                for (int j = ipv6Parts.size(); j < 8; j++) {
                    numbers[nIdx++] = 0;
                }
            } else {
                try {
                    numbers[nIdx++] = Long.parseLong(part, 16);
                    pIdx++;
                } catch (NumberFormatException e) {
                    throw new UriException("Illegal IPv6 address: " + ipv6String);
                }
            }
        }

        if (ipv6Parts.size() < minimumNumberOfParts) {
            throw new UriException("Illegal IPv6 address: " + ipv6String);
        }

        ipv6Number = BigInteger.valueOf(numbers[numbers.length - 1]);
        for (int i = 6; i >= 0; i--) {
            ipv6Number = ipv6Number.add(BigInteger.valueOf(numbers[i]).multiply(RADIX_0X10000.pow(7 - i)));
        }

        return ipv6Number;
    }

    public static String serializeIpv6(BigInteger ipv6Number, boolean normalizeCase) {
        BigInteger[] numbers = new BigInteger[8];
        int nullRunStart = -1;
        int longestNullRunStart = -1;
        int nullRunLength = -1;
        int longestNullRunLength = -1;
        for (int i = 7; i >= 0; i--) {
            numbers[7 - i] = ipv6Number.shiftRight(i * 16).and(BigInteger.valueOf(0xffff));
            if (numbers[7 - i].equals(BigInteger.ZERO)) {
                if (nullRunStart < 0) {
                    nullRunStart = 7 - i;
                    nullRunLength = 1;
                } else {
                    nullRunLength++;
                }
            } else {
                if (nullRunStart >= 0) {
                    if (nullRunLength > longestNullRunLength) {
                        longestNullRunStart = nullRunStart;
                        longestNullRunLength = nullRunLength;
                    }
                    nullRunStart = -1;
                    nullRunLength = -1;
                }
            }
        }
        if (nullRunLength > longestNullRunLength) {
            longestNullRunStart = nullRunStart;
            longestNullRunLength = nullRunLength;
        }

        StringBuilder result = new StringBuilder();
        if (longestNullRunLength > 1) {
            // Handle consecutive nulls
            if (longestNullRunStart > 0) {
                result.append(numberToHex(numbers[0], normalizeCase));
            }
            for (int i = 1; i < longestNullRunStart; i++) {
                result.append(':').append(numberToHex(numbers[i], normalizeCase));
            }
            result.append("::");
            if (longestNullRunStart + longestNullRunLength < 8) {
                result.append(numberToHex(numbers[longestNullRunStart + longestNullRunLength], normalizeCase));
            }
            for (int i = longestNullRunStart + longestNullRunLength + 1; i < 8; i++) {
                result.append(':').append(numberToHex(numbers[i], normalizeCase));
            }
        } else {
            result.append(numberToHex(numbers[0], normalizeCase));
            for (int i = 1; i < 8; i++) {
                result.append(':').append(numberToHex(numbers[i], normalizeCase));
            }
        }
        return result.toString();
    }

    private static String numberToHex(BigInteger num, boolean normalizeCase) {
        if (normalizeCase) {
            return num.toString(16).toUpperCase();
        } else {
            return num.toString(16);
        }
    }

    public static String serializeIpv6Base85(BigInteger ipv6Number) {
        System.out.println(ipv6Number);
        BigInteger val85 = BigInteger.valueOf(85);
        BigInteger curVal = ipv6Number;
        char[] buf = new char[20];
        for (int i = 0; i < 20; i++) {
            BigInteger[] divideAndRemainder = curVal.divideAndRemainder(val85);
            buf[19 - i] = base85Charset[divideAndRemainder[1].intValue()];
            curVal = divideAndRemainder[0];
        }

        return new String(buf);
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
