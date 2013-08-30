package org.neodatis.odb.core.btree.kv;

import org.neodatis.btree.IBTree;
import org.neodatis.btree.IBTreeNode;
import org.neodatis.btree.IBTreePersister;
import org.neodatis.odb.OID;
import org.neodatis.odb.core.layers.layer4.kv.KVStore;

public class KVBTreePersister implements IBTreePersister {
	
	protected KVStore store;
	
	public KVBTreePersister(KVStore store) {
		this.store = store;
	}

	public void clear() {
		// TODO Auto-generated method stub

	}

	public void close() throws Exception {
		

	}

	public Object deleteNode(IBTreeNode parent) {
		return store.remove((OID) parent.getId());
	}

	public void flush() {
		//

	}

	public IBTree loadBTree(Object id) {
		return (IBTree) store.get((OID) id);
	}

	public IBTreeNode loadNodeById(Object id) {
		return (IBTreeNode) store.get((OID) id);
	}

	public OID saveBTree(IBTree tree) {
		store.put((OID) tree.getId(), tree);
		return (OID) tree.getId();
	}

	public Object saveNode(IBTreeNode node) {
		store.put((OID) node.getId(), node);
		return node.getId();

	}

	public void setBTree(IBTree tree) {
		//?
	}

}
