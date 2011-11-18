package org.archive.util.iterator;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;


public abstract class AbstractPeekableIterator<E> implements PeekableIterator<E> {
	private E cachedNext = null; 
	private boolean done = false;

	// returns next E, or null if hasNext() would return false;
	public abstract E getNextInner();
	public abstract void close() throws IOException;
	public boolean hasNext() {
		if(cachedNext != null) {
			return true;
		}
		if(done) {
			return false;
		}
		cachedNext = getNextInner();
		if(cachedNext == null) {
			done = true;
		}
		return (cachedNext != null);
	}

	public E next() {
		if(cachedNext == null) {
			if(!hasNext()) {
				throw new NoSuchElementException("Call hasNext!");
			}
		}
		E tmp = cachedNext;
		cachedNext = null;
		return tmp;
	}

	public void remove() {
		throw new UnsupportedOperationException("No remove");
	}

	public E peek() {
		if(cachedNext == null) {
			if(!hasNext()) {
				throw new NoSuchElementException("Call hasNext!");
			}
		}
		return cachedNext;
	}
	public static <T> PeekableIterator<T> wrap(Iterator<T> itr) {
		return new IteratorWrappedPeekableIterator<T>(itr);
	}
	public static PeekableIterator<String> wrapReader(BufferedReader reader) {
		return new BufferedReaderPeekableIterator(reader);
	}
	
	private static class IteratorWrappedPeekableIterator<C> extends AbstractPeekableIterator<C> {
		private Iterator<C> wrapped = null;
		public IteratorWrappedPeekableIterator(Iterator<C> wrapped) {
			this.wrapped = wrapped;
		}
		@Override
		public C getNextInner() {
			C next = null;
			if(wrapped != null) {
				if(wrapped.hasNext()) {
					next = wrapped.next();
				}
			}
			return next;
		}
		@Override
		public void close() throws IOException {
			CloseableIteratorUtil.attemptClose(wrapped);
		}
	}
	private static class BufferedReaderPeekableIterator extends AbstractPeekableIterator<String> {
		private BufferedReader reader = null;
		public BufferedReaderPeekableIterator(BufferedReader reader) {
			this.reader = reader;
		}
		@Override
		public String getNextInner() {
			String next = null;
			if(reader != null) {
				try {
					next = reader.readLine();
					if(next == null) {
						reader.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
			return next;
		}
		@Override
		public void close() throws IOException {
			reader.close();
		}
	}
	public static <K> Comparator<PeekableIterator<K>> getComparator(Comparator<K> comparator) {
		return new PeekableIteratorComparator<K>(comparator);
	}
	private static class PeekableIteratorComparator<J> implements Comparator<PeekableIterator<J>> {
		private Comparator<J> comparator = null;
		public PeekableIteratorComparator(Comparator<J> comparator) {
			this.comparator = comparator;
		}

		public int compare(PeekableIterator<J> o1, PeekableIterator<J> o2) {
			return comparator.compare(o1.peek(), o2.peek());
		}
	}
}
