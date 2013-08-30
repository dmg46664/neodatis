package org.neodatis.odb.core.query.list.objects;

import java.util.ArrayList;
import java.util.Iterator;

import org.neodatis.OrderByConstants;
import org.neodatis.odb.Objects;

/**
 * A simple list to hold query result. It is used when no index and no order by is used and inMemory = true
 * @author osmadja
 *
 */
public class SimpleList<E> extends ArrayList<E> implements Objects<E> {

	private int currentPosition;
	
	
	public SimpleList() {
		super();
	}

	public SimpleList(int initialCapacity) {
		super(initialCapacity);
	}
	
	
	public boolean addWithKey(Comparable key, E o) {
		add( o);
		return true;
	}

	public boolean addWithKey(int key, E o) {
		add( o);
		return true;
	}

	public E first() {
		return get(0);
	}

	public boolean hasNext() {
		return currentPosition < size();
	}

	/** The orderByType in not supported by this kind of list
	 * 
	 */
	public Iterator<E> iterator(OrderByConstants orderByType) {
		return iterator();
	}

	public E next() {
		return get(currentPosition++);
	}

	public void reset() {
		currentPosition = 0;

	}

}
