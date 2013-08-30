/**
 * 
 */
package org.neodatis.btree.impl.multiplevalue;

import org.neodatis.btree.IBTreeNode;
import org.neodatis.btree.IBTreePersister;

/**
 * @author olivier
 * 
 */
public class ThreadSafeBTree extends InMemoryBTreeMultipleValuesPerKey {

	public ThreadSafeBTree() {
		super();
	}

	public ThreadSafeBTree(String name, int degree, IBTreePersister persister) {
		super(name, degree, persister);
	}

	public ThreadSafeBTree(String name, int degree) {
		super(name, degree);
	}

	public synchronized void insert(Comparable key, Object value) {
		super.insert(key, value);
	}

	protected synchronized void newRoot() {
		super.newRoot();
	}

	public synchronized void split(IBTreeNode parent, IBTreeNode node2Split, int childIndex) {
		synchronized (this) {
			super.split(parent, node2Split, childIndex);
		}
	}

	protected boolean insertNonFull(IBTreeNode node, Comparable key, Object value) {
		return super.insertNonFull(node, key, value);
	}

	public IBTreeNode buildNode() {
		return new ThreadSafeBTreeNode(this);
	}

}
