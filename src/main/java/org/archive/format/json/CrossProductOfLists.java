package org.archive.format.json;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

public class CrossProductOfLists<T> {
	private static final Logger LOG =
		Logger.getLogger(CrossProductOfLists.class.getName());
	
	public List<List<T>> crossProduct(List<List<List<T>>> listOfLists) {

		if(LOG.isLoggable(Level.INFO)) {
			int count = listOfLists.size();
			LOG.info(String.format("Total of (%d) lists to cross product",count));
			for(int i = 0; i < count; i++) {
				LOG.info(String.format("Field (%d) is (%d) deep",i,listOfLists.get(i).size()));
				for(List<T> inner : listOfLists.get(i)) {
					LOG.info(
							String.format("----(%d):(%s)"
									,i,StringUtils.join(inner.toArray(),",") ) );
				}
			}
		}
		ArrayList<List<T>> results = new ArrayList<List<T>>();
		
		Stack<T> current = new Stack<T>();
		Deque<List<List<T>>> remainder = new ArrayDeque<List<List<T>>>(listOfLists); 
		recurse(remainder,current,results);
		return results;
	}
	private void recurse(Deque<List<List<T>>> remainder,
			Stack<T> current, ArrayList<List<T>> accumulation) {
		if(remainder.isEmpty()) {
			// all done:
//			dump(new ArrayList<T>(current));
			accumulation.add(new ArrayList<T>(current));
			
		} else {
			List<List<T>> cur = remainder.removeFirst();
			for(List<T> o : cur) {
				current.addAll(o);
				recurse(remainder,current,accumulation);
				for(int i = 0; i < o.size(); i++) {
					current.pop();
				}
				
			}
			remainder.addFirst(cur);
		}
	}
//	private void dump(ArrayList<T> a) {
//		StringBuilder sb = new StringBuilder();
//		boolean first = false;
//		for(T o : a) {
//			if(first) {
//				first = false;
//			} else {
//				sb.append(",");
//			}
//			sb.append(o.toString());
//		}
//		//System.out.println("CrossOutput:" + sb.toString());
//	}

}
