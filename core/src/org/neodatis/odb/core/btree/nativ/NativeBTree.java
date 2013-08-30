/**
 * 
 */
package org.neodatis.odb.core.btree.nativ;

import java.util.Iterator;

import org.neodatis.OrderByConstants;
import org.neodatis.btree.IBTreeNode;
import org.neodatis.btree.IBTreeNodeOneValuePerKey;
import org.neodatis.btree.IBTreePersister;
import org.neodatis.btree.IBTreeSingleValuePerKey;
import org.neodatis.btree.impl.AbstractBTree;

/**
 * @author olivier
 * 
 */
public class NativeBTree extends AbstractBTree implements IBTreeSingleValuePerKey {
	protected Long id;
	protected Long nextNodeId;

	public NativeBTree(Long id, int degree, IBTreePersister persister, Long nextNodeId) {
		super("btree", degree, persister);
		setId(id);
		persister.setBTree(this);
	}

	public Long getNextNodeId() {
		return nextNodeId;
	}

	public IBTreeNode buildNode() {
		if (nextNodeId == null) {
			nextNodeId = new Long(1);
		}
		IBTreeNode node = new NativeNode(nextNodeId, this);
		nextNodeId = new Long(nextNodeId.longValue() + 1);
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
		this.id = (Long) id;

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
