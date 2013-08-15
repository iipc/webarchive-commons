package org.archive.util.binsearch.impl;

import it.unimi.dsi.io.ByteBufferInputStream;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.archive.util.binsearch.SeekableLineReader;
import org.archive.util.binsearch.SeekableLineReaderFactory;

public class MappedSeekableLineReaderFactory implements
        SeekableLineReaderFactory {
    
    final static int DEFAULT_BLOCK_SIZE = 512;

    private File file;
    private FileChannel fc;
    private RandomAccessFile raf;
    private ByteBufferInputStream bbis;
    
    private int blockSize;
    
    public MappedSeekableLineReaderFactory(File file) throws IOException {
        this(file, DEFAULT_BLOCK_SIZE);
    }
    
    public MappedSeekableLineReaderFactory(File file, int blockSize) throws IOException {
        this.file = file;
        this.blockSize = blockSize;
        this.raf = new RandomAccessFile(file,"r");
        this.fc = raf.getChannel();
        this.bbis = ByteBufferInputStream.map(fc);
    }

    public SeekableLineReader get() throws IOException {
        return new MappedSeekableLineReader(bbis.copy(), blockSize);
    }
    
    public void close() throws IOException
    {
        if (raf != null) {
            raf.close();
        }
    }
    
    public long getModTime()
    {
        return file.lastModified();
    }


}
