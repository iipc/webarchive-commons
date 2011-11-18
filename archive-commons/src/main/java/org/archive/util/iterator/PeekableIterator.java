package org.archive.util.iterator;

public interface PeekableIterator<E> extends CloseableIterator<E> {
	public E peek();
}
