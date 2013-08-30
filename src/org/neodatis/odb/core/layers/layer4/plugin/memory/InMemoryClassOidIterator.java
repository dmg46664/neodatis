package org.neodatis.odb.core.layers.layer4.plugin.memory;

import java.util.Iterator;
import java.util.Map;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.OID;
import org.neodatis.odb.core.layers.layer3.OidAndBytes;
import org.neodatis.odb.core.layers.layer4.ClassOidIterator;

public class InMemoryClassOidIterator implements ClassOidIterator {
	protected Map<OID, OidAndBytes> store;
	protected Iterator<OID> iterator;

	public InMemoryClassOidIterator(Map<OID, OidAndBytes> store) {
		this.store = store;
		this.iterator = store.keySet().iterator();
	}

	public boolean hasNext() {
		return iterator.hasNext();
	}

	public ClassOid next() {
		return (ClassOid) iterator.next();
	}

	public void reset() {
		this.iterator = store.keySet().iterator();

	}

	public Iterator<ClassOid> iterator() {
		return (Iterator<ClassOid>) this;
	}

}
