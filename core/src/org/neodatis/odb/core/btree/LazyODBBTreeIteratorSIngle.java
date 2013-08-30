package org.neodatis.odb.core.btree;

import org.neodatis.OrderByConstants;
import org.neodatis.btree.BTreeError;
import org.neodatis.btree.BTreeIteratorSingleValuePerKey;
import org.neodatis.btree.IBTree;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.layers.layer2.instance.InstanceBuilderContext;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeObjectInfo;
import org.neodatis.odb.core.session.SessionEngine;

/**
 * A Lazy BTree Iterator : It iterate on the object OIDs and lazy load objects from them (OIDs) 
 * Used by the LazyBTreeCollection
 * @author osmadja
 *
 */
public class LazyODBBTreeIteratorSIngle extends BTreeIteratorSingleValuePerKey{
	private SessionEngine sessionEngine;
    private boolean returnObjects;
    protected InstanceBuilderContext ibc;
	
    
    /**
     * 
     * @param tree
     * @param orderByType
     * @param sessionEngine
     * @param returnObjects
     */
    public LazyODBBTreeIteratorSIngle(IBTree tree, OrderByConstants orderByType, SessionEngine storageEngine, boolean returnObjects) {
		super(tree, orderByType);
		this.sessionEngine = storageEngine;
		this.returnObjects = returnObjects;
		this.ibc = new InstanceBuilderContext();
	}
	public Object next() {
		ObjectOid oid = (ObjectOid) super.next();
		try {
			return loadObject(oid);
		} catch (Exception e) {
			throw new NeoDatisRuntimeException(BTreeError.LAZY_LOADING_NODE.addParameter(oid),e);
		}
	}
    
	private Object loadObject(ObjectOid oid) throws Exception {
        // true = to use cache
		NonNativeObjectInfo nnoi = sessionEngine.getMetaObjectFromOid(oid,true,ibc); 
        if(returnObjects){
        	Object o = nnoi.getObject();
        	if(o!=null){
        		return o;
        	}
        	return sessionEngine.layer2ToLayer1(nnoi,ibc);
        }
        return nnoi;
	}
}
