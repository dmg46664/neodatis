/*
 NeoDatis ODB : Native Object Database (odb.info@neodatis.org)
 Copyright (C) 2007 NeoDatis Inc. http://www.neodatis.org

 "This file is part of the NeoDatis ODB open source object database".

 NeoDatis ODB is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 NeoDatis ODB is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.neodatis.btree.impl.singlevalue;

import java.util.Iterator;

import org.neodatis.OrderByConstants;
import org.neodatis.btree.BTreeIteratorSingleValuePerKey;
import org.neodatis.btree.IBTreeNode;
import org.neodatis.btree.IBTreeNodeOneValuePerKey;
import org.neodatis.btree.IBTreePersister;
import org.neodatis.btree.IBTreeSingleValuePerKey;
import org.neodatis.btree.impl.AbstractBTree;
import org.neodatis.btree.impl.InMemoryPersister;

public class InMemoryBTreeSingleValuePerKey extends AbstractBTree implements IBTreeSingleValuePerKey{

	protected static int nextId = 1;
	protected Integer id;
	protected boolean replaceOnDuplicate;
	
	public InMemoryBTreeSingleValuePerKey() {
		super();
	}

	public InMemoryBTreeSingleValuePerKey(String name, int degree, IBTreePersister persister) {
		super(name, degree, persister);
	}

	public Object search(Comparable key) {
		IBTreeNodeOneValuePerKey theRoot = (IBTreeNodeOneValuePerKey) getRoot();
		return theRoot.search(key);
	}	
	
	public InMemoryBTreeSingleValuePerKey(String name, int degree) {
		super(name,degree, new InMemoryPersister());
		this.id = new Integer(nextId++);
	}

	public IBTreeNode buildNode() {
		IBTreeNodeOneValuePerKey node = new InMemoryBTreeNodeSingleValuePerkey(this);
		node.setReplaceOnDuplicate(replaceOnDuplicate);
		return node;
	}

	public Object getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = (Integer) id;
	}

	public void clear() {
		
	}

	/* (non-Javadoc)
	 * @see org.neodatis.btree.IBTree#iterator()
	 */
	public Iterator iterator(OrderByConstants orderBy) {
		return new BTreeIteratorSingleValuePerKey(this,orderBy);
	}

	/* (non-Javadoc)
	 * @see org.neodatis.btree.IBTreeSingleValuePerKey#setReplaceOnDuplicate(boolean)
	 */
	public void setReplaceOnDuplicate(boolean yesNo) {
		this.replaceOnDuplicate = yesNo;
		((IBTreeNodeOneValuePerKey)getRoot()).setReplaceOnDuplicate(yesNo);
	}
}
