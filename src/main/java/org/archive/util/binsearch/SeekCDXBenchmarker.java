package org.archive.util.binsearch;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.archive.url.WaybackURLKeyMaker;
import org.archive.util.binsearch.impl.MappedSeekableLineReaderFactory;
import org.archive.util.binsearch.impl.NIOSeekableLineReaderFactory;
import org.archive.util.binsearch.impl.NIOSeekableLineReaderFactory.NIOType;
import org.archive.util.binsearch.impl.RandomAccessFileSeekableLineReaderFactory;
import org.archive.util.iterator.CloseableIterator;


public class SeekCDXBenchmarker {

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        
        long startTime = 0;
        
        try {
            startTime = System.currentTimeMillis();
            
            String uri = args[0];
            String type = "bio";
            int blocksize = SeekableLineReaderFactory.BINSEARCH_BLOCK_SIZE;
            
            if (args.length > 1) {
                type = args[1];
            }
            
            if (args.length > 2) {
                blocksize = Integer.parseInt(args[2]);
            }
            
            SeekableLineReaderFactory factory = null;
            
            if (type.equals("bio")) {
                factory = new RandomAccessFileSeekableLineReaderFactory(new File(uri), blocksize);
            } else if (type.equals("nio")) {
                factory = new NIOSeekableLineReaderFactory(new File(uri), blocksize, NIOType.PLAIN);
            } else if (type.equals("mmap")) {
                factory = new NIOSeekableLineReaderFactory(new File(uri), blocksize, NIOType.MMAP);
            } else if (type.equals("bigmap")) {
                factory = new MappedSeekableLineReaderFactory(new File(uri), blocksize);
            }
            
            SortedTextFile sorted = new SortedTextFile(factory);
            sorted.setBinsearchBlockSize(blocksize);
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, Charset.defaultCharset()));
            
            WaybackURLKeyMaker keymaker = new WaybackURLKeyMaker(true);
            
            String next = null;
            
            while ((next = reader.readLine()) != null) {
                
                try {
                    next = keymaker.makeKey(next);
                } catch (Exception e) {
                    continue;
                }
                
                CloseableIterator<String> iter = sorted.getRecordIterator(next);
                if (iter.hasNext()) {
                    System.out.println(iter.next());
                }
                iter.close();
            }
        } finally {
            System.out.println("=========");
            System.out.println("Total Time: " + (System.currentTimeMillis() - startTime));
        }
    }
}
