package org.archive.util.iterator;

import java.io.Closeable;
import java.util.Iterator;

/**
 * Iterator with a close method that frees up any resources associated with 
 * the Iterator.
 *
 * @author brad
 * @version $Date$, $Revision$
 * @param <E> 
 */
public interface CloseableIterator<E> extends Iterator<E>, Closeable {
}
