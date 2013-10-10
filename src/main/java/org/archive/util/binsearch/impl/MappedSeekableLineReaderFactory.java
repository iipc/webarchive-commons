package org.archive.util.binsearch.impl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.archive.util.binsearch.ByteBufferInputStream;
import org.archive.util.binsearch.SeekableLineReader;
import org.archive.util.binsearch.SeekableLineReaderFactory;

public class MappedSeekableLineReaderFactory implements
        SeekableLineReaderFactory {
    
    final static int DEFAULT_BLOCK_SIZE = 512;

    private File file;
    //private FileChannel fc;
    private RandomAccessFile raf;
    private ByteBufferInputStream bbis;
    
    private int blockSize;
    
    public MappedSeekableLineReaderFactory(File file) throws IOException {
        this(file, DEFAULT_BLOCK_SIZE);
    }
    
    public MappedSeekableLineReaderFactory(File file, int blockSize) throws IOException {
        this.file = file;
        this.blockSize = blockSize;
        reload();
    }
    
    protected synchronized ByteBufferInputStream getBbis()
    {
    	return bbis;
    }

    public SeekableLineReader get() throws IOException {    	
        return new MappedSeekableLineReader(getBbis(), blockSize);
    }
    
    public void reload() throws IOException
    {
        RandomAccessFile newRAF = new RandomAccessFile(file, "r");
       
        RandomAccessFile oldRAF = raf;
        
        FileChannel newFc = newRAF.getChannel();
        
        ByteBufferInputStream newBbis = ByteBufferInputStream.map(newFc);
        
        synchronized(this) {
        	bbis = newBbis;
        	raf = newRAF;
        }
        
    	if (oldRAF != null) {
    		oldRAF.close();
    	}
    }
    
    public synchronized void close() throws IOException
    {
    	if (raf != null) {
    		raf.close();
    	}
    	
    	this.bbis = null;
    }
    
    public long getModTime()
    {
        return file.lastModified();
    }
}
