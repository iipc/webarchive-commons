package org.archive.util.iterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

public class CloseableCompositeIterator<E> implements CloseableIterator<E> {

	protected LinkedList<CloseableIterator<E>> iters;
	protected Iterator<CloseableIterator<E>> iterPtr;
	protected CloseableIterator<E> currIter;
	
    public CloseableCompositeIterator()
	{
		iters = new LinkedList<CloseableIterator<E>>();
	}
	
	public void addFirst(CloseableIterator<E> e)
	{
		iters.addFirst(e);
	}
	
	public void addLast(CloseableIterator<E> e)
	{
		iters.addLast(e);
	}
	
	@Override
    public boolean hasNext() {
		
		if (iterPtr == null) {
			iterPtr = iters.iterator();
			currIter = iterPtr.next();
		}
		
		if (currIter == null) {
			return false;
		}
		
		while (currIter != null) {
			if (currIter.hasNext()) {
				return true;
			}
			
			currIter = (iterPtr.hasNext() ? iterPtr.next() : null);
		}
		
		return false;
    }

	@Override
    public E next() {
		return currIter.next();
    }

	@Override
    public void remove() {
		currIter.remove();
    }

	@Override
    public void close() throws IOException {
		for (CloseableIterator<E> e : iters) {
			if (e != null) {
				try {
					e.close();
				} catch (IOException io) {
					
				}
			}
		}
    }
}
