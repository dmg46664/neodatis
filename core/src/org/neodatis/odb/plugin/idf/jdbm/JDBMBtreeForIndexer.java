package org.neodatis.odb.plugin.idf.jdbm;

import java.io.IOException;

import jdbm.btree.BTree;
import jdbm.helper.TupleBrowser;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.OID;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.plugin.nativ.JDBMBTree;
import org.neodatis.odb.plugin.nativ.OidComparator;

public class JDBMBtreeForIndexer extends JDBMBTree {

	public JDBMBtreeForIndexer(String fileName, boolean withTransaction, boolean withCache) throws IOException {
		super(fileName, withTransaction, withCache);
	}

	@Override
	protected BTree creatreBtree() throws IOException {
		return BTree.createInstance(recordManager, new OidComparator(), new OidSerializer(), new ObjectLocationSerializer());
	}

	public TupleBrowser browse(OID oid) throws IOException {
		if (oid instanceof ObjectOid) {
			ObjectOid ooid = (ObjectOid) oid;
			BTree btree = getBtreeForName(ooid.getClassOid().oidToString());

			return btree.browse();
		}
		if (oid instanceof ClassOid) {
			BTree btree = getBtreeForName(CLASS_OID);
			return btree.browse();
		}
		return null;

	}
}
