package org.neodatis.odb.plugin.idf.jdbm;

import java.io.IOException;
import java.util.Iterator;

import jdbm.btree.BTree;
import jdbm.helper.Tuple;
import jdbm.helper.TupleBrowser;

import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.layers.layer4.ObjectOidIterator;
import org.neodatis.odb.core.layers.layer4.OidGenerator;

public class JdbmObjectOidIterator implements ObjectOidIterator {
	protected BTree btree;
	protected TupleBrowser browser;
	protected Tuple<byte[], Object> tuple;
	protected Way way;
	protected OidGenerator oidGenerator;
	
	public JdbmObjectOidIterator(BTree btree, Way way, OidGenerator generator) {
		this.oidGenerator = generator;
		this.btree = btree;
		try {
			this.browser = this.btree.browse();
		} catch (IOException e) {
			throw new NeoDatisRuntimeException(e);
		}
		this.way = way;
		tuple = new Tuple<byte[], Object>();
	}
	public boolean hasNext() {
		try{
			return browser.getNext(tuple);	
		}catch (Exception e) {
			throw new NeoDatisRuntimeException(e);
		}
		
	}

	public ObjectOid next() {
		Object o = tuple.getKey();
		
		// when in the cache, the tuple contain an object OID
		if(o instanceof ObjectOid){
			return (ObjectOid) o;
		}
		
		return oidGenerator.buildObjectOID((byte[]) o);
	}

	public void reset() {
		try {
			this.browser = this.btree.browse();
		} catch (IOException e) {
			throw new NeoDatisRuntimeException(e);
		}
		tuple = null;

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
