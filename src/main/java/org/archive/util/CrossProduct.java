package org.archive.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Stack;

public class CrossProduct <T> {
	public List<List<T>> crossProduct(List<List<T>> listOfLists) {

		ArrayList<List<T>> results = new ArrayList<List<T>>();
		
		Stack<T> current = new Stack<T>();
		Deque<List<T>> remainder = new ArrayDeque<List<T>>(listOfLists); 
		recurse(remainder,current,results);
		return results;
	}
	private void recurse(Deque<List<T>> remainder,
			Stack<T> current, ArrayList<List<T>> accumulation) {
		if(remainder.isEmpty()) {
			// all done:
			dump(new ArrayList<T>(current));
			accumulation.add(new ArrayList<T>(current));
			
		} else {
			List<T> cur = remainder.removeFirst();
			for(T o : cur) {
				current.push(o);
				recurse(remainder,current,accumulation);
				current.pop();
			}
			remainder.addFirst(cur);
		}
	}
	private void dump(ArrayList<T> a) {
		StringBuilder sb = new StringBuilder();
		boolean first = false;
		for(T o : a) {
			if(first) {
				first = false;
			} else {
				sb.append(",");
			}
			sb.append(o.toString());
		}
		System.out.println("CrossOutput:" + sb.toString());
	}
}
