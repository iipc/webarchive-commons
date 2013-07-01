package org.archive.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.archive.streamcontext.HDFSStream;
import org.archive.streamcontext.HTTP11Stream;
import org.archive.streamcontext.RandomAccessFileStream;
import org.archive.streamcontext.Stream;
import org.archive.util.binsearch.SeekableLineReaderFactory;
import org.archive.util.binsearch.impl.HDFSSeekableLineReaderFactory;
import org.archive.util.binsearch.impl.HTTPSeekableLineReaderFactory;
import org.archive.util.binsearch.impl.NIOSeekableLineReaderFactory;
import org.archive.util.binsearch.impl.RandomAccessFileSeekableLineReaderFactory;

public class GeneralURIStreamFactory {
	
	private static final Logger LOGGER = Logger.getLogger(
			GeneralURIStreamFactory.class.getName());
	
	public static class HDFSDefaultUri
	{
		public HDFSDefaultUri(String defaultUri)
		{
			GeneralURIStreamFactory.defaultFSURI = defaultUri;
		}
	}
	
	private GeneralURIStreamFactory()
	{
		
	}
	
	private static String defaultFSURI;
	private static FileSystem fs;
	
	protected static FileSystem initHdfs() throws IOException
	{
		if (fs != null) {
			return fs;
		}
		
		if (defaultFSURI == null) {
			Configuration c = new Configuration();
			fs = FileSystem.get(c);
		} else {
			Configuration c = new Configuration();
			c.set(FileSystem.FS_DEFAULT_NAME_KEY, defaultFSURI);
			try {
				fs = FileSystem.get(new URI(defaultFSURI),c);
			} catch (URISyntaxException e) {
				LOGGER.warning("Bad URI: " + e);
			}
		}
		
		return fs;
	}
	
	public static boolean isHdfs(String uri)
	{
		return uri.startsWith("hdfs://");
	}
	
	public static boolean isHttp(String uri)
	{
		return uri.startsWith("http://");
	}
	
	public static boolean isFileURI(String uri)
	{
		return uri.startsWith("file://");
	}
	
	public static Stream createStream(String uri) throws IOException
	{
		if (isHttp(uri)) {
			return new HTTP11Stream(new URL(uri));
		} else if (isHdfs(uri)) {
			return new HDFSStream(initHdfs().open(new Path(uri)));	
		} else {
			return new RandomAccessFileStream(new File(uri));
		}
	}
	
	public static Stream createStream(String uri, long offset) throws IOException {
		if (isHttp(uri)) {
			return new HTTP11Stream(new URL(uri), offset);
		} else if (isHdfs(uri)) {
			return new HDFSStream(initHdfs().open(new Path(uri)), offset);
		} else {
			return new RandomAccessFileStream(new File(uri), offset);
		}
	}
	
	public static SeekableLineReaderFactory createSeekableStreamFactory(String uri, boolean useNio) throws IOException
	{
		if (isHttp(uri)) {
			return HTTPSeekableLineReaderFactory.getHttpFactory(uri);
		} else if (isHdfs(uri)) {
			return new HDFSSeekableLineReaderFactory(initHdfs(), new Path(uri));
		} else {
			
			File file = null;
			
			if (isFileURI(uri)) {
				try {
					file = new File(new URI(uri));
				} catch (URISyntaxException e) {
					file = new File(uri);
				}
			} else {
				file = new File(uri);
			}
			
			if (useNio) {
				return new NIOSeekableLineReaderFactory(file);
			} else {
				return new RandomAccessFileSeekableLineReaderFactory(file);
			}
		}
	}
}
