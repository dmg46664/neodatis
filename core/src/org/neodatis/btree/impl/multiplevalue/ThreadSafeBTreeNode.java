/**
 * 
 */
package org.neodatis.btree.impl.multiplevalue;

import org.neodatis.btree.IBTree;
import org.neodatis.btree.IBTreeNode;
import org.neodatis.btree.IKeyAndValue;

/**
 * @author olivier
 * 
 */
public class ThreadSafeBTreeNode extends InMemoryBTreeNodeMultipleValuesPerKey {

	public ThreadSafeBTreeNode(IBTree btree) {
		super(btree);
	}

	public boolean insertKeyAndValue(Comparable key, Object value) {
		return super.insertKeyAndValue(key, value);
	}

	public IBTreeNode getChildAt(int index, boolean throwExceptionIfNotExist) {
		return super.getChildAt(index, throwExceptionIfNotExist);
	}

	public void deleteChildAt(int index) {
		super.deleteChildAt(index);
	}

	public Object getChildIdAt(int childIndex, boolean throwExceptionIfDoesNotExist) {
		return super.getChildIdAt(childIndex, throwExceptionIfDoesNotExist);
	}

	public void moveChildFromTo(int sourceIndex, int destinationIndex, boolean throwExceptionIfDoesNotExist) {
		super.moveChildFromTo(sourceIndex, destinationIndex, throwExceptionIfDoesNotExist);
	}

	public void setChildAt(IBTreeNode node, int childIndex, int index, boolean throwExceptionIfDoesNotExist) {
		super.setChildAt(node, childIndex, index, throwExceptionIfDoesNotExist);
	}

	public void setChildAt(IBTreeNode child, int index) {
		super.setChildAt(child, index);
	}

	public void setId(Object id) {
		super.setId(id);
	}

	public void setNullChildAt(int childIndex) {
		super.setNullChildAt(childIndex);
	}

	public void setParent(IBTreeNode node) {
		super.setParent(node);
	}

	public void setKeyAndValueAt(IKeyAndValue keyAndValue, int index, boolean shiftIfAlreadyExist, boolean incrementNbKeys) {
		super.setKeyAndValueAt(keyAndValue, index, shiftIfAlreadyExist, incrementNbKeys);
	}

	public IKeyAndValue getMedian() {
		return super.getMedian();
	}

}
