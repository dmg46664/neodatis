/**
 * 
 */
package org.neodatis.odb.core.btree.kv;

import java.util.Iterator;
import java.util.UUID;

import org.neodatis.OrderByConstants;
import org.neodatis.btree.IBTreeNode;
import org.neodatis.btree.IBTreeNodeOneValuePerKey;
import org.neodatis.btree.IBTreePersister;
import org.neodatis.btree.IBTreeSingleValuePerKey;
import org.neodatis.btree.impl.AbstractBTree;
import org.neodatis.odb.core.oid.StringOidImpl;

/**
 * @author olivier
 * 
 */
public class KVBTree extends AbstractBTree implements IBTreeSingleValuePerKey {
	protected Object id;
	public KVBTree(Object id, int degree, IBTreePersister persister) {
		super("btree", degree, persister);
		setId(id);
		persister.setBTree(this);
	}

	public Object getNextNodeId() {
		return new StringOidImpl(UUID.randomUUID().toString());
	}

	public IBTreeNode buildNode() {
	
		Object nextId = getNextNodeId();
		IBTreeNode node = new KVNode(nextId, this);
		return node;
	}

	public Iterator iterator(OrderByConstants orderBy) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.btree.IBTree#getId()
	 */
	public Object getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.btree.IBTree#setId(java.lang.Object)
	 */
	public void setId(Object id) {
		this.id = id;

	}

	/**
	 * @param size
	 */
	public void setSize(long size) {
		super.setSize(size);

	}

	public void setRoot(IBTreeNode root) {
		super.setRoot(root);
	}

	public Object search(Comparable key) {
		IBTreeNodeOneValuePerKey theRoot = (IBTreeNodeOneValuePerKey) getRoot();
		return theRoot.search(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.neodatis.btree.IBTreeSingleValuePerKey#setReplaceOnDuplicate(boolean)
	 */
	public void setReplaceOnDuplicate(boolean yesNo) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param height
	 */
	public void setHeight(int height) {
		super.setHeight(height);

	}

}
