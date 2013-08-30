package org.neodatis.odb.core.oid;

import org.neodatis.odb.DatabaseId;

public class DatabaseIdImpl implements DatabaseId {
	private String id;

	public DatabaseIdImpl() {
		super();
	}

	public DatabaseIdImpl(String id) {
		super();
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see org.neodatis.odb.impl.core.oid.DatabaseId#getIds()
	 */
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String toString() {
		return id;
	}
	public static DatabaseId fromString(String sid) {
		return new DatabaseIdImpl(sid);
	}
	
	public boolean equals(Object object) {
		if(object==null || object.getClass()!=DatabaseIdImpl.class ){
			return false;
		}
		DatabaseIdImpl dbId = (DatabaseIdImpl) object;
		return dbId.id.equals(id);
	}
}
