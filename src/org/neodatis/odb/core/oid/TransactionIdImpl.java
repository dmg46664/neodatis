package org.neodatis.odb.core.oid;

import org.neodatis.odb.TransactionId;


public class TransactionIdImpl implements TransactionId {

	protected String id;
	public TransactionIdImpl(String id) {
		super();
		this.id = id;
	}

		
	public String toString() {
		StringBuffer buffer = new StringBuffer(id);
		return buffer.toString();
	}

	public boolean equals(Object object) {
		if(object==null || object.getClass()!=TransactionIdImpl.class ){
			return false;
		}
		TransactionIdImpl tid = (TransactionIdImpl) object;
		return id==tid.id;
	}


	public String getId() {
		return id;
	}
	
	

}
