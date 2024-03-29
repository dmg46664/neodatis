package org.neodatis.odb.core.btree;

import org.neodatis.btree.IBTreeNode;
import org.neodatis.btree.IBTreePersister;
import org.neodatis.btree.impl.multiplevalue.BTreeMultipleValuesPerKey;
import org.neodatis.odb.OID;


/**
 * The NeoDatis ODB BTree. It extends the DefaultBTree implementation to add the ODB OID generated by the ODB database.
 * @author osmadja
 *
 */
public class ODBBTreeMultiple extends BTreeMultipleValuesPerKey {
	public ODBBTreeMultiple() {
		super();
	}
	public ODBBTreeMultiple(String name, int degree, IBTreePersister persister) {
		super(name,degree, persister);
	}

	protected OID oid;

	public IBTreeNode buildNode() {
		return new ODBBTreeNodeMultiple(this);
	}

	public Object getId() {
		return oid;
	}

	public void setId(Object id) {
		this.oid = (OID) id;
	}
}
