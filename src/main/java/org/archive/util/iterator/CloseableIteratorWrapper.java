package org.archive.util.iterator;

import java.io.IOException;
import java.util.Iterator;

/**
 * Wrap a regular Iterator&lt;S&gt; to create a CloseableIterator&lt;S&gt; where the close() is a no-op
 * @author ilya
 *
 * @param <S>
 */

public class CloseableIteratorWrapper<S> implements CloseableIterator<S>
{
	protected Iterator<S> iter;
	
	public CloseableIteratorWrapper(Iterator<S> iter)
	{
		this.iter = iter;
	}
	
	@Override
    public boolean hasNext() {
		return this.iter.hasNext();
    }

	@Override
    public S next() {
		return this.iter.next();
    }

	@Override
    public void remove() {
		this.iter.remove();
        
    }

	@Override
    public void close() throws IOException {
        //No Op
    }		
}