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
package org.neodatis.odb.core.btree;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.neodatis.btree.BTreeError;
import org.neodatis.btree.IBTree;
import org.neodatis.btree.IBTreeNode;
import org.neodatis.btree.IBTreePersister;
import org.neodatis.odb.ClassOid;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.ODB;
import org.neodatis.odb.OID;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.layers.layer2.instance.InstanceBuilderContext;
import org.neodatis.odb.core.layers.layer4.engine.Dummy;
import org.neodatis.odb.core.session.SessionEngine;
import org.neodatis.odb.core.trigger.CommitListener;
import org.neodatis.tool.DLogger;
import org.neodatis.tool.wrappers.OdbTime;
import org.neodatis.tool.wrappers.list.IOdbList;
import org.neodatis.tool.wrappers.list.OdbArrayList;
import org.neodatis.tool.wrappers.map.OdbHashMap;

/**
 * Class that persists the BTree and its node into the NeoDatis ODB Database.
 * 
 * @author osmadja
 * 
 */
public class LazyODBBTreePersister implements IBTreePersister, CommitListener {
	public static final String LOG_ID = "LazyODBBTreePersister";

	// See the map strategy performance test at
	// test/org.neodatis.odb.test.performance.TestMapPerf
	/** All loaded nodes */
	private Map<ObjectOid,Object> oids;

	/**
	 * All modified nodes : the map is used to avoid duplication The key is the
	 * oid, the value is the position is the list
	 */
	private OdbHashMap<Object, Integer> modifiedObjectOids;
	/**
	 * The list is used to keep the order. Deleted object will be replaced by
	 * null value, to keep the positions
	 */
	private IOdbList<ObjectOid> modifiedObjectOidList;

	/** The odb interface */
	private SessionEngine engine;

	/** The tree we are persisting */
	private IBTree tree;

	private static Map<ObjectOid, Object> smap = null;

	private static Map<Object, Integer> smodifiedObjects = null;

	// TODO create a boolean value to know if data must be saved on update or
	// only at the end
	public static int nbSaveNodes = 0;

	public static int nbSaveNodesInCache = 0;

	public static int nbSaveTree = 0;

	public static int nbLoadNodes = 0;

	public static int nbLoadTree = 0;

	public static int nbLoadNodesFromCache = 0;

	private int nbPersist;
	
	private Map<Class, ClassOid> classOids;
	
	protected InstanceBuilderContext ibc;
	
	protected boolean debug;

	public LazyODBBTreePersister(ODB odb) {
		this(Dummy.getEngine(odb));
		
	}

	public LazyODBBTreePersister(SessionEngine engine) {
		oids = new HashMap<ObjectOid, Object>();
		classOids = new HashMap<Class, ClassOid>();
		modifiedObjectOids = new OdbHashMap<Object, Integer>();
		modifiedObjectOidList = new OdbArrayList<ObjectOid>(500);
		this.engine = engine;
		this.engine.getSession().addCommitListener(this);

		smap = oids;
		smodifiedObjects = modifiedObjectOids;
		this.ibc = new InstanceBuilderContext();
		this.debug = engine.getSession().getConfig().isDebugEnabled(LOG_ID);
	}

	/**
	 * Loads a node from its id. Tries to get if from memory, if not present
	 * then loads it from odb storage
	 * 
	 * @param id
	 *            The id of the nod
	 * @return The node with the specific id
	 * 
	 */
	public IBTreeNode loadNodeById(Object id) {
		ObjectOid oid = (ObjectOid) id;

		// Check if node is in memory
		IBTreeNode node = (IBTreeNode) oids.get(oid);

		if (node != null) {
			nbLoadNodesFromCache++;
			return node;
		}
		nbLoadNodes++;

		// else load from odb
		try {
			if (debug) {
				DLogger.debug("Loading node with id " + oid);
			}
			if (oid == null) {
				throw new NeoDatisRuntimeException(BTreeError.INVALID_ID_FOR_BTREE
						.addParameter(oid));
			}
			IBTreeNode pn = (IBTreeNode) engine.getObjectFromOid(oid,true,ibc);
			pn.setId(oid);

			if (tree != null) {
				pn.setBTree(tree);
			}
			// Keep the node in memory
			oids.put(oid, pn);
			return pn;
		} catch (Exception e) {
			throw new NeoDatisRuntimeException(BTreeError.INTERNAL_ERROR, e);
		}
	}

	/**
	 * saves the bree node Only puts the current node in an 'modified Node' map
	 * to be saved on commit
	 * 
	 */
	public Object saveNode(IBTreeNode node) {
		ObjectOid oid = null;

		// Here we only save the node if it does not have id,
		// else we just save into the hashmap
		if (node.getId() == null) {
			try {
				nbSaveNodes++;
				// first get the oid. : -2:it could be any value
				oid = getNextIdFor(node.getClass());
				node.setId(oid);
				oid = engine.store(oid, node);
				if (debug) {
					DLogger.debug("Saved node id " + oid + " : class " + node.getClass().getName() );
				}
				if (tree != null && node.getBTree() == null) {
					node.setBTree(tree);
				}
				oids.put(oid, node);
				return oid;
			} catch (Exception e) {
				throw new NeoDatisRuntimeException(BTreeError.INTERNAL_ERROR
						.addParameter("While saving node"), e);
			}
		}
		nbSaveNodesInCache++;
		oid = (ObjectOid) node.getId();
		oids.put(oid, node);
		addModifiedOid(oid);

		return oid;

	}

	public void close() throws Exception {
		persist();
		engine.getSession().commit();
		engine.close();
	}

	public IBTree loadBTree(Object id) {
		nbLoadTree++;
		ObjectOid oid = (ObjectOid) id;
		try {
			if (debug) {
				DLogger.debug("Loading btree with id " + oid);
			}
			if (oid == null) {
				throw new NeoDatisRuntimeException(BTreeError.INVALID_ID_FOR_BTREE
						.addParameter("null"));
			}
			tree = (IBTree) engine.getObjectFromOid(oid,true,ibc);
			tree.setId(oid);
			tree.setPersister(this);
			IBTreeNode root = tree.getRoot();
			root.setBTree(tree);
			return tree;
		} catch (Exception e) {
			throw new NeoDatisRuntimeException(BTreeError.INTERNAL_ERROR.addParameter(e.getMessage()), e);
		}
	}

	public OID saveBTree(IBTree treeToSave) {
		nbSaveTree++;
		try {
			ObjectOid oid = (ObjectOid) treeToSave.getId();
			if (oid == null) {
				oid = getNextIdFor(treeToSave.getClass());
				treeToSave.setId(oid);
				oid = engine.store(oid, treeToSave);
				if (debug) {
					DLogger.debug("Saved btree " + treeToSave.getId()
							+ " with id " + oid + " and  root "
							+ treeToSave.getRoot());
				}
				if (this.tree == null) {
					this.tree = treeToSave;
				}
				oids.put(oid, treeToSave);
			} else {
				oids.put(oid, treeToSave);
				addModifiedOid(oid);
			}
			return oid;
		} catch (Exception e) {
			throw new NeoDatisRuntimeException(BTreeError.INTERNAL_ERROR, e);
		}
	}

	public ObjectOid getNextIdFor(Class clazz) throws IOException {
		ClassOid coid = classOids.get(clazz);
		if(coid==null){
			coid = engine.getSession().getClassInfo(clazz.getName()).getOid();
			classOids.put(clazz, coid);
		}
		
		return engine.getStorageEngine().getOidGenerator().createObjectOid(coid);
	}

	public void persist() {
		nbPersist++;
		if (debug) {
			DLogger.debug("persist " + nbPersist + " : Saving "
					+ modifiedObjectOids.size() + " objects - " + hashCode());
		}
		OID oid = null;
		int nbCommited = 0;
		long t0 = 0;
		long t1 = 0;
		int i = 0;
		int size = modifiedObjectOids.size();
		Iterator iterator = modifiedObjectOidList.iterator();

		while (iterator.hasNext()) {
			oid = (OID) iterator.next();

			if (oid != null) {
				nbCommited++;
				try {
					t0 = OdbTime.getCurrentTimeInMs();
					Object o = oids.get(oid);
					engine.store(o);
					t1 = OdbTime.getCurrentTimeInMs();
				} catch (Exception e) {
					throw new NeoDatisRuntimeException(
							BTreeError.INTERNAL_ERROR
									.addParameter("Error while storing object with oid "
											+ oid), e);
				}
				if (debug) {
					DLogger.debug("Committing oid " + oid + " | " + i + "/"
							+ size + " | " + (t1 - t0));
				}
				i++;
			}
		}
		if (debug) {
			DLogger.debug(nbCommited + " commits / " + size);
		}

	}

	public void afterCommit() {
		// nothing to do
	}

	public void beforeCommit() {
		persist();
		clear();
	}

	public Object deleteNode(IBTreeNode o) {

		OID oid = engine.delete(o,true);
		oids.remove(oid);

		Integer position = modifiedObjectOids.remove2(oid);
		if (position != null) {
			// Just replace the element by null, to not modify all the other
			// positions
			modifiedObjectOidList.set(position.intValue(), null);
		}

		return o;
	}

	public void setBTree(IBTree tree) {
		this.tree = tree;

	}

	public static void resetCounters() {
		nbSaveNodes = 0;
		nbSaveTree = 0;
		nbSaveNodesInCache = 0;
		nbLoadNodes = 0;
		nbLoadTree = 0;
		nbLoadNodesFromCache = 0;
	}

	public static StringBuffer counters() {
		StringBuffer buffer = new StringBuffer("save nodes=").append(
				nbSaveNodes).append(",").append(nbLoadNodesFromCache).append(
				" | save tree=").append(nbSaveTree).append(" | loadNodes=")
				.append(nbLoadNodes).append(",").append(nbLoadNodesFromCache)
				.append(" | load tree=").append(nbLoadTree);
		if (smap != null && smodifiedObjects != null) {
			buffer.append(" | map size=").append(smap.size()).append(
					" | modObjects size=").append(smodifiedObjects.size());
		}
		return buffer;
	}

	public void clear() {
		oids.clear();
		modifiedObjectOids.clear();
		modifiedObjectOidList.clear();
	}

	public void clearModified() {
		modifiedObjectOids.clear();
		modifiedObjectOidList.clear();
	}

	public void flush() {
		persist();
		clearModified();
	}

	protected void addModifiedOid(ObjectOid oid) {
		Object o = modifiedObjectOids.get(oid);
		if (o != null) {
			// Object is already in the list
			return;
		}
		modifiedObjectOidList.add(oid);
		// Keep the position of the oid in the list as the value of the map.
		// Used for the delete.
		modifiedObjectOids.put(oid, new Integer(
				modifiedObjectOidList.size() - 1));
	}
}
