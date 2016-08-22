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
package org.netpreserve.commons.uri;

import java.util.Arrays;
import java.util.Comparator;

/**
 * A parsed query part of a URI.
 * <p>
 * This class parses the query part of a URI according to how it is specified for HTTP.
 */
public class ParsedQuery {

    private final Entry[] data;

    private final int size;

    private final Comparator<Entry> entryComparator = new Comparator<Entry>() {
        @Override
        public int compare(Entry o1, Entry o2) {
            return o1.key.compareTo(o2.key);
        }

    };

    private ParsedQuery(final Entry[] data) {
        this.data = data;
        this.size = data.length;
        Arrays.sort(data, entryComparator);
    }

    public ParsedQuery(final String queryString) {
        if (queryString == null || queryString.isEmpty()) {
            data = new Entry[0];
            size = 0;
            return;
        }

        String[] query = queryString.split("&");
        Arrays.sort(query);
        data = new Entry[query.length];
        int dataSize = query.length;

        int dataIdx = -1;

        for (int i = 0; i < query.length; i++) {
            String[] keyValue = query[i].split("=", 2);
            if (dataIdx > -1 && keyValue.length > 1 && keyValue[0].equals(data[dataIdx].key)) {
                data[dataIdx] = data[dataIdx].add(keyValue[1]);
                dataSize--;
            } else {
                dataIdx++;
                data[dataIdx] = new Entry(keyValue);
            }
        }

        size = dataSize;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean containsKey(String key) {
        return get(key) != null;
    }

    public Entry get(String key) {
        for (int i = 0; i < size; i++) {
            if (data[i].key.equals(key)) {
                return data[i];
            }
        }
        return null;
    }

    public ParsedQuery put(Entry entry) {
        for (int i = 0; i < size; i++) {
            if (data[i].key.equals(entry.key)) {
                if (entry.size() == 0) {
                    return this;
                }

                Entry[] newData = Arrays.copyOf(data, size);
                newData[i] = entry;
                return new ParsedQuery(newData);
            }
        }

        Entry[] newData = Arrays.copyOf(data, size + 1);
        newData[size] = entry;
        return new ParsedQuery(newData);
    }

    public ParsedQuery add(Entry entry) {
        for (int i = 0; i < size; i++) {
            if (data[i].key.equals(entry.key)) {
                if (entry.size() == 0) {
                    return this;
                }

                Entry[] newData = Arrays.copyOf(data, size);
                newData[i] = newData[i].add(entry);
                return new ParsedQuery(newData);
            }
        }

        Entry[] newData = Arrays.copyOf(data, size + 1);
        newData[size] = entry;
        return new ParsedQuery(newData);
    }

    public ParsedQuery remove(String key) {
        for (int i = 0; i < size; i++) {
            if (data[i].key.equals(key)) {
                Entry[] newData = new Entry[size - 1];
                System.arraycopy(data, 0, newData, 0, i);
                System.arraycopy(data, i + 1, newData, i, size - i - 1);
                return new ParsedQuery(newData);
            }
        }
        return this;
    }

    @Override
    public String toString() {
        if (size > 0) {
            StringBuilder query = new StringBuilder();
            for (int i = 0; i < size; i++) {
                data[i].format(query);
            }
            return query.toString();
        } else {
            return null;
        }
    }

    public static class Entry {

        private final String key;

        private final String[] data;

        public Entry(final String key, final String value) {
            this.key = key;
            if (value == null) {
                this.data = new String[0];
            } else {
                this.data = new String[1];
                this.data[0] = value;
            }
        }

        public Entry(final String key, final String[] value) {
            this.key = key;
            this.data = value;
            Arrays.sort(this.data);
        }

        private Entry(final String[] data) {
            this.key = data[0];
            if (data.length == 1) {
                this.data = new String[0];
            } else {
                this.data = new String[1];
                this.data[0] = data[1];
            }
        }

        public Entry add(final String value) {
            if (value == null) {
                return this;
            }

            String[] newData = Arrays.copyOf(data, data.length + 1);
            newData[data.length] = value;
            return new Entry(key, newData);
        }

        public Entry add(final Entry value) {
            if (value.data.length == 0) {
                return this;
            }

            String[] newData = Arrays.copyOf(data, data.length + value.data.length);
            System.arraycopy(value.data, 0, newData, data.length, value.data.length);
            return new Entry(key, newData);
        }

        public int size() {
            return data.length;
        }

        public String get(int index) {
            return data[index];
        }

        public String getSingle() {
            if (data.length == 0) {
                return null;
            }
            return data[0];
        }

        @Override
        public String toString() {
            return format(new StringBuilder()).toString();
        }

        private StringBuilder format(StringBuilder sb) {
            if (data.length == 0) {
                if (sb.length() > 0) {
                    sb.append('&');
                }
                sb.append(key);
            } else {
                for (String value : data) {
                    if (sb.length() > 0) {
                        sb.append('&');
                    }
                    sb.append(key).append('=').append(value);
                }
            }
            return sb;
        }

    }

}
