package org.archive.util.binsearch.impl;

import it.unimi.dsi.io.ByteBufferInputStream;

import java.io.IOException;
import java.io.InputStream;

import org.archive.util.binsearch.AbstractSeekableLineReader;

import com.google.common.io.ByteStreams;

public class MappedSeekableLineReader extends AbstractSeekableLineReader {

    private ByteBufferInputStream bbis;

    public MappedSeekableLineReader(ByteBufferInputStream bbis) throws IOException {
        this.bbis = bbis;
    }
    
    public long getOffset() throws IOException
    {
        if (closed) {
            return 0;
        }
        
        return bbis.position();
    }
    
    @Override
    protected InputStream doSeekLoad(long offset, int maxLength)
            throws IOException {
        
        bbis.position(offset);
        
        if (maxLength > 0) {
            return ByteStreams.limit(bbis, maxLength); 
        } else {
            return bbis;
        }
    }

    @Override
    public long getSize() throws IOException {
        return bbis.length();
    }

    @Override
    protected void doClose() throws IOException {
        //bbis = null;
        //bbis.close();
    }
}
