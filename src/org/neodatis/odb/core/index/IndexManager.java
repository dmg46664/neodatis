/**
 * 
 */
package org.neodatis.odb.core.index;

import org.neodatis.btree.IBTree;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.OID;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfoIndex;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeObjectInfo;
import org.neodatis.odb.core.query.SimpleCompareKey;
import org.neodatis.odb.core.session.Session;
import org.neodatis.tool.wrappers.list.IOdbList;

/**
 * @author olivier
 *
 */
public class IndexManager {
	protected Session session;
	
	public IndexManager(Session session){
		this.session = session;
	}
	
	public int manageIndexesForInsert(OID oid, NonNativeObjectInfo nnoi) {
		IOdbList<ClassInfoIndex> indexes = nnoi.getClassInfo().getIndexes();
		ClassInfoIndex index = null;
		for (int i = 0; i < indexes.size(); i++) {
			index = indexes.get(i);
			try {
				Comparable key = index.computeKey(nnoi);
				if(key instanceof SimpleCompareKey){
					key = ((SimpleCompareKey)key).getKey();
				}
				int hc = key.hashCode();
				index.getBTree().insert(key, oid);
			} catch (Exception e) {
				// rollback what has been done
				// bug #2510966
				session.rollback();
				throw new NeoDatisRuntimeException(NeoDatisError.ERROR_WHILE_MANAGING_INDEX.addParameter(index.getName()),e);
			}
		}
		return indexes.size();
	}
	public int manageIndexesForDelete(OID oid, NonNativeObjectInfo nnoi) {
		IOdbList<ClassInfoIndex> indexes = nnoi.getClassInfo().getIndexes();
		ClassInfoIndex index = null;

		for (int i = 0; i < indexes.size(); i++) {
			index = indexes.get(i);
			// TODO manage collision!
			index.getBTree().delete(index.computeKey(nnoi), oid);
		}

		return indexes.size();
	}

	public int manageIndexesForUpdate(OID oid, NonNativeObjectInfo nnoi, NonNativeObjectInfo oldMetaRepresentation) {
		// takes the indexes from the oldMetaRepresentation because noi comes
		// from the client and is not always
		// in sync with the server meta model (In Client Server mode)
		IOdbList<ClassInfoIndex> indexes = nnoi.getClassInfo().getIndexes();
		ClassInfoIndex index = null;
		Comparable oldKey = null;
		Comparable newKey = null;
		for (int i = 0; i < indexes.size(); i++) {
			index = indexes.get(i);
			oldKey = index.computeKey(oldMetaRepresentation);
			newKey = index.computeKey(nnoi);
			// Only update index if key has changed!
			if (oldKey.compareTo(newKey) != 0) {
				IBTree btree = index.getBTree();
				// TODO manage collision!
				Object old = btree.delete(oldKey, oid);
				// TODO check if old is equal to oldKey
				btree.insert(newKey, oid);
			}
		}
		return indexes.size();
	}

}
