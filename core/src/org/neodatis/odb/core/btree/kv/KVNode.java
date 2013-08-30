/**
 * 
 */
package org.neodatis.odb.core.btree.kv;

import org.neodatis.btree.IBTree;
import org.neodatis.btree.IBTreeNode;
import org.neodatis.btree.exception.BTreeException;
import org.neodatis.btree.impl.singlevalue.BTreeNodeSingleValuePerKey;

/**
 * @author olivier
 *
 */
public class KVNode extends BTreeNodeSingleValuePerKey {
	protected Object id;
	protected Object[] children;
	protected Object parentId;
	protected KVNode parent;
	
	public KVNode(Object id, IBTree btree) {
		super(btree);
		this.id = id;
		children = new Object[btree.getDegree()*2];
	}

	/**
	 * @param btree
	 * @param id2
	 * @param parentId2
	 * @param size
	 * @param keys
	 * @param positions
	 * @param nbChildren 
	 * @param childrens
	 */
	public KVNode(IBTree btree, long id, long parentId, int size, Long[] keys, Position[] positions, Long[] children, int nbKeys, int nbChildren) {
		super(btree);
		this.id = id;
		this.parentId = parentId;
		this.children = children;
		for(int i=0;i<size;i++){
			this.setKeyAndValueAt(keys[i], positions[i], i);
		}
		setNbKeys(nbKeys);
		setNbChildren(nbChildren);
	}

	public IBTreeNode getChildAt(int index, boolean throwExceptionIfNotExist) {
		if(children[index]==null){
			if(throwExceptionIfNotExist){
				throw new BTreeException("Trying to load null child node at index " + index);	
			}
			return null;
		}
		Object id = children[index];
		if(id==null ){
			return null;
		}
		IBTreeNode child = btree.getPersister().loadNodeById(id);
		if(child!=null){
			child.setParent(this);
		}
		return child;
	}

	public IBTreeNode getParent() {
		if(parent==null){
			parent = (KVNode) btree.getPersister().loadNodeById(parentId); 
		}
		return parent; 
	}

	public Object getParentId() {
		return parentId;
	}

	public boolean hasParent() {
		return parent!=null;
	}
	public void deleteChildAt(int index) {
		children[index] = null;
		nbChildren--;
	}
	public void moveChildFromTo(int sourceIndex, int destinationIndex, boolean throwExceptionIfDoesNotExist) {
		
		if(children[sourceIndex]==null && throwExceptionIfDoesNotExist){
			throw new BTreeException("Trying to move null child node at index " + sourceIndex);
		}
		children[destinationIndex] = children[sourceIndex];
	}
	public void setNullChildAt(int childIndex) {
		children[childIndex] = null;
	}
	public Object getChildIdAt(int childIndex, boolean throwExceptionIfDoesNotExist) {
		if(children[childIndex]==null && throwExceptionIfDoesNotExist){
			throw new BTreeException("Trying to move null child node at index " + childIndex);
		}
		return children[childIndex];
	}
	public Object getValueAsObjectAt(int index) {
		return getValueAt(index);
	}

	protected void init() {
	}

	public void setParent(IBTreeNode node) {
		this.parent = (KVNode) node;
		this.parentId = parent.getId();
	}

	public Object getId() {
		return id;
	}

	public void setChildAt(IBTreeNode node, int childIndex, int indexDestination, boolean throwExceptionIfDoesNotExist) {
		IBTreeNode child = node.getChildAt(childIndex, throwExceptionIfDoesNotExist);
		if(child!=null){
			children[indexDestination] = child.getId();
		}
	}

	public void setChildAt(IBTreeNode child, int index) {
		children[index] = child.getId();
	}

	public void setId(Object id) {
		this.id = id;
	}

}
