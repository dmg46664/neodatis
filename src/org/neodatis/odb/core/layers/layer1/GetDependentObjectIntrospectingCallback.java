/**
 * 
 */
package org.neodatis.odb.core.layers.layer1;

import java.util.Collection;

import org.neodatis.odb.ObjectOid;
import org.neodatis.tool.wrappers.map.OdbHashMap;

/**
 * @author olivier
 *
 */
public class GetDependentObjectIntrospectingCallback implements IntrospectionCallback{
	private OdbHashMap<Object, Object> objects;
	
	public GetDependentObjectIntrospectingCallback(){
		objects = new OdbHashMap<Object, Object>();
	}
	/* (non-Javadoc)
	 * @see org.neodatis.odb.core.layers.layer1.introspector.IIntrospectionCallback#objectFound(java.lang.Object)
	 */
	public boolean objectFound(Object o, ObjectOid oid) {
		if(o==null){
			return false;
		}
		if(objects.containsKey(o)){
			return false;
		}
		objects.put(o, o);
		return true;
	}
	public Collection<Object> getObjects() {
		return objects.values();
	}
}
