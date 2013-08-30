package org.neodatis.odb.core.layers.layer4.plugin.memory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.neodatis.odb.OID;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.layers.layer3.OidAndBytes;
import org.neodatis.odb.core.layers.layer4.ObjectOidIterator;

public class InMemoryObjectOidIterator implements ObjectOidIterator {
	protected Map<OID, OidAndBytes> store;
	protected Iterator<OID> iterator;

	public InMemoryObjectOidIterator(Map<OID, OidAndBytes> store) {
		this.store = store;
		if(store==null){
			store = new HashMap<OID, OidAndBytes>();
		}
		
		this.iterator = store.keySet().iterator();
	}

	public boolean hasNext() {
		return iterator.hasNext();
	}

	public ObjectOid next() {
		return (ObjectOid) iterator.next();
	}

	public void reset() {
		this.iterator = store.keySet().iterator();

	}

	public Iterator<ObjectOid> iterator() {
		return (Iterator<ObjectOid>) this;
	}

	public void startAtTheBeginning() {
		// TODO Auto-generated method stub
		
	}

	public void startAtTheEnd() {
		// TODO Auto-generated method stub
		
	}

}
