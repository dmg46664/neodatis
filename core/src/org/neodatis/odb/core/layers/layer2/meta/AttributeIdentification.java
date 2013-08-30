package org.neodatis.odb.core.layers.layer2.meta;

import org.neodatis.odb.ObjectOid;


/** To keep informations about an attribute of a non native object
 * 
 *  An attribute can be native then we keep the offset position of the attribute, or non native then we keep the oid  
 * 
 * @author Olivier
 *
 */
public class AttributeIdentification {
	public int id;
	public boolean isNative;
	public int offset;
	public int size;
	public ObjectOid oid;
	
	public AttributeIdentification(int offset, int size){
		this.isNative = true;
		this.offset = offset;
		this.size = size;
		this.id = -1;
	}
	public AttributeIdentification(ObjectOid oid){
		this.isNative = false;
		this.oid = oid;
		this.id = -1;
		this.size = 0;
	}
	
	public String toString() {
		if(isNative){
			return String.format("ai:native= offset=%d size=%d id=%d",offset,size,id);
		}
		return String.format("ai:non-native= oid=%s id=%d",oid,id);
	}
}
