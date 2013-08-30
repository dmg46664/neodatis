package org.neodatis.odb.plugin.idf.jdbm;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.neodatis.odb.OID;
import org.neodatis.odb.core.layers.layer4.ClassOidIterator;
import org.neodatis.odb.core.layers.layer4.ObjectOidIterator;
import org.neodatis.odb.core.layers.layer4.ObjectOidIterator.Way;
import org.neodatis.odb.core.layers.layer4.plugin.jdbm.JdbmClassOidIterator;
import org.neodatis.odb.core.layers.layer4.plugin.jdbm.JdbmObjectOidIterator;
import org.neodatis.odb.core.oid.uuid.UniqueOidGeneratorImpl;
import org.neodatis.odb.plugin.idf.Indexer;
import org.neodatis.odb.plugin.idf.ObjectLocation;
import org.neodatis.odb.plugin.nativ.JDBMBTree;
import org.neodatis.tool.DLogger;

public class JDBMIndexer implements Indexer {
	protected JDBMBTree btree;
	protected String fileName;
	protected String id;
	protected boolean isObjectIndeer;
	protected boolean debug;

	public void close() throws IOException {
		// btree.commit();
		if (btree != null) {
			btree.close();
			btree = null;
		}
	}

	public ObjectLocation get(OID oid) throws ParseException, IOException {
		ObjectLocation ol = (ObjectLocation) btree.get(oid);
		if (debug) {
			DLogger.info("\tIndexer "+ id + ":Read oid " + oid.oidToString() + " => Object Location is " +ol);
		}

		return ol;
	}

	public void put(OID oid, ObjectLocation objectLocation) throws CorruptIndexException, IOException {
		if (debug) {
			DLogger.info("\tIndexer "+ id + ":Write oid " + oid.oidToString() + " => Object Location is " + objectLocation);
		}
		btree.put(oid, objectLocation);
	}

	public void init(String id, String fileName) throws IOException {
		this.id = id;
		this.fileName = fileName;
		btree = new JDBMBtreeForIndexer(fileName, true, true);
	}

	public void commit() {
		// TODO Auto-generated method stub

	}

	public void rollback() {
		// TODO Auto-generated method stub

	}

	public ObjectLocation delete(OID oid) {
		ObjectLocation ol = (ObjectLocation) btree.remove(oid);
		return ol;
	}

	public boolean existKey(OID oid) {
		return btree.containsKey(oid);
	}

	public ClassOidIterator getClassOidIterator() {
		return new JdbmClassOidIterator(btree.getBtreeForName(id), new UniqueOidGeneratorImpl());
	}

	public ObjectOidIterator getObjectOidIterator() {
		return new JdbmObjectOidIterator(btree.getBtreeForName(id), Way.INCREASING, new UniqueOidGeneratorImpl());
	}

	public void setDebug(boolean yes) {
		this.debug = yes;
	}

}
