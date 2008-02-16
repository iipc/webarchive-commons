package org.archive.surt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The new SURT tokenizer breaks a SURT up into tokens.
 * 
 * For example "(org,archive,www,)/path/file.html?query#anchor" is broken up into:
 * 
 * ["(" 
 *  "org,"
 *  "archive," 
 *  "www," 
 *  ")/"
 *  "path/"
 *  "file.html"
 *  "?query"
 *  "#anchor"]
 * 
 * @author aosborne
 *
 */
public class NewSurtTokenizer implements Iterable<String> {
    private String surt;
    private int endOfAuthority;
    private int endOfPath;
    private int surtLength;

    public NewSurtTokenizer(String surt) {
        super();
        this.surt = surt;
        surtLength = surt.length();
        endOfAuthority = surt.indexOf(')');
        if (endOfAuthority == -1) {
            endOfAuthority = surtLength;
        }
        
        int hash = surt.indexOf('#');
        int question = surt.indexOf('?');
        if (hash == -1) {
            endOfPath = question;
        } else if (question == -1) {
            endOfPath = hash;
        } else {
            endOfPath = hash < question ? hash : question;
        }
        if (endOfPath == -1) {
            endOfPath = surtLength;
        }
        
    }

    private class NewSurtTokenizerIterator implements Iterator<String> {
        int pos = 0;

        public boolean hasNext() {
            return pos < surtLength;
        }

        private int nextPieceEnd() {
            // ROOT: "(..."
            if (pos == 0) {
                return 1; // "("
            }
            // Host components: "foo,..."
            if (pos < endOfAuthority || endOfAuthority == -1) {
                int endOfHostComponent = surt.indexOf(',', pos);
                if (endOfHostComponent == -1) {
                    return surtLength;
                } else {
                    return endOfHostComponent + 1;
                }
            } 
            
            // Host index: ")/..."
            if (pos == endOfAuthority) {
                return pos + 2;
            }
            
            // Path segments: "directory/"
            if (pos < endOfPath || endOfPath == -1) {
                int endOfPathSegment = surt.indexOf('/', pos);
                if (endOfPathSegment < endOfPath && endOfPathSegment != -1) {
                    return endOfPathSegment + 1;
                } else if (endOfPath != -1) { // file: "hello.html"
                    return endOfPath;
                } else {
                    return surtLength;
                }   
            }
            
            // Query string
            if (surt.charAt(pos) == '?') {
                int endOfQuery = surt.indexOf('#');
                if (endOfQuery != -1) {
                    return endOfQuery;
                } else {
                    return surtLength;
                }
            }
            
            // Anchor "#boo"
            return surtLength;
        }

        public String next() {
            int pieceEnd = nextPieceEnd();
            String piece = surt.substring(pos, pieceEnd);
            pos = pieceEnd;
            return piece;
        }

        public void remove() {
            // TODO Auto-generated method stub

        }

    }

    public Iterator<String> iterator() {
        return new NewSurtTokenizerIterator();
    }

    public List<String> toList() {
        List<String> list = new ArrayList<String>();
        for (String piece: this) {
            list.add(piece);
        }
        return list;
    }
    public String[] toArray() {
        return (String[]) toList().toArray();
    }

}
