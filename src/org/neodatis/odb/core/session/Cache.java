package org.neodatis.odb.core.session;

import org.neodatis.odb.OID;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.layers.layer2.meta.ObjectInfoHeader;

public interface Cache {

	boolean existObject(Object object);

	ObjectOid getOid(Object object, boolean throwExceptionIfNotFound);

	ObjectInfoHeader getObjectInfoHeaderFromOid(OID oid, boolean throwExceptionIfNotFound);

	Object getObjectWithOid(ObjectOid oid);

	void addObject(ObjectOid oid, Object o);

	int getSize();

	/**
	 * @return
	 */
	void clear();

	/**
	 * @param o
	 * @param oid
	 */
	void remove(Object o, ObjectOid oid);

	/**
	 * @param oid
	 */
	void remove(ObjectOid oid);
}
