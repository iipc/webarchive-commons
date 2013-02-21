package org.archive.util.iterator;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.logging.Logger;


public class SortedCompositeIterator<E> implements CloseableIterator<E> {
	
	private final static Logger LOGGER = Logger.getLogger(SortedCompositeIterator.class.getName());
	private static final int DEFAULT_CAPACITY = 10;
	PriorityQueue<PeekableIterator<E>> q = null;

	public SortedCompositeIterator(Comparator<E> comparator) {
		this(DEFAULT_CAPACITY,comparator);
	}
	public SortedCompositeIterator(int capacity, Comparator<E> comparator) {
		q = new PriorityQueue<PeekableIterator<E>>(capacity, 
				new PeekableIteratorComparator<E>(comparator));
	}
	public void addAll(Collection<Iterator<E>> toAdd) {
		for(Iterator<E> e : toAdd) {
			addIterator(e);
		}
	}
	public void addIterator(Iterator<E> itr) {
		PeekableIterator<E> i = null;
		if(itr instanceof PeekableIterator) {
			i = (PeekableIterator<E>) itr;
		} else {
			i = AbstractPeekableIterator.wrap(itr);
		}
		if(i.hasNext()) {
			q.add(i);
		}
	}

	public boolean hasNext() {
		return (q.peek() != null);
	}

	public E next() {
		PeekableIterator<E> i = q.poll();
		if(i == null) {
			throw new NoSuchElementException("Call hasNext!");
		}
		E tmp = i.next();
		if(i.hasNext()) {
			q.add(i);
		} else {
			try {
				i.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return tmp;
	}
	public void remove() {
		throw new UnsupportedOperationException("No remove");
	}
	
	private class PeekableIteratorComparator<J> implements Comparator<PeekableIterator<J>> {
		private Comparator<J> comparator = null;
		public PeekableIteratorComparator(Comparator<J> comparator) {
			this.comparator = comparator;
		}

		public int compare(PeekableIterator<J> o1, PeekableIterator<J> o2) {
			return comparator.compare(o1.peek(), o2.peek());
		}
	}

	public void close() throws IOException {
		for(PeekableIterator<E> i : q) {
			try {
				i.close();
			} catch (IOException io) {
				LOGGER.warning(io.toString());
			}
		}
	}	
}
