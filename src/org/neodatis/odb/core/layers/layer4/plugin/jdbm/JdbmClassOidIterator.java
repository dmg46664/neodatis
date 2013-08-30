package org.neodatis.odb.core.layers.layer4.plugin.jdbm;

import java.io.IOException;
import java.util.Iterator;

import jdbm.btree.BTree;
import jdbm.helper.Tuple;
import jdbm.helper.TupleBrowser;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.core.layers.layer4.ClassOidIterator;
import org.neodatis.odb.core.layers.layer4.OidGenerator;

public class JdbmClassOidIterator implements ClassOidIterator {
	protected BTree btree;
	protected TupleBrowser browser;
	protected Tuple<byte[], Object> tuple;
	protected OidGenerator oidGenerator;

	public JdbmClassOidIterator(BTree btree, OidGenerator oidGenerator) {
		this.oidGenerator = oidGenerator;
		this.btree = btree;
		try {
			this.browser = this.btree.browse();
		} catch (Exception e) {
			throw new NeoDatisRuntimeException(e);
		}
		tuple = new Tuple<byte[], Object>();
	}

	public boolean hasNext() {
		try {
			return browser.getNext(tuple);
		} catch (Exception e) {
			throw new NeoDatisRuntimeException(e);
		}

	}

	public ClassOid next() {
		return oidGenerator.buildClassOID(tuple.getKey());
	}

	public void reset() {
		try {
			this.browser = this.btree.browse();
		} catch (IOException e) {
			throw new NeoDatisRuntimeException(e);
		}
		tuple = null;

	}

	public Iterator<ClassOid> iterator() {
		return (Iterator<ClassOid>) this;
	}

}
