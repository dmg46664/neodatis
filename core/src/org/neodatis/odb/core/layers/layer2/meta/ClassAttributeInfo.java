
/*
NeoDatis ODB : Native Object Database (odb.info@neodatis.org)
Copyright (C) 2007 NeoDatis Inc. http://www.neodatis.org

"This file is part of the NeoDatis ODB open source object database".

NeoDatis ODB is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

NeoDatis ODB is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package org.neodatis.odb.core.layers.layer2.meta;

import java.io.Serializable;

import org.neodatis.odb.ClassOid;

/**
 * to keep informations about an attribute of a class :
 * 
 * <pre>
 *   - Its type
 *   - its name
 *   - If it is an index
 * </pre>
 * 
 * @author olivier s
 * 
 */
public class ClassAttributeInfo implements Serializable{

	//private transient static int nb=0;
	private int id;
	//private ClassInfo classInfo;
	/** The oid of the class that owns this attribute */
	private ClassOid ownerClassInfoOid;

	private String className;

	private String name;

	private boolean isIndex;
	
	private ODBType attributeType;
	/** can be null if attribute is non native*/
	private Class nativeClass;
	/** The attribute class oid : the type of the attribute. Null is attribute is native */
	private ClassOid attributeClassOid;

	public ClassAttributeInfo() {
	}
	public ClassAttributeInfo(int attributeId , String name, String fullClassName, ClassOid attributeCiOid, ClassOid ownerCiOid) {
		this(attributeId,name,null,fullClassName,attributeCiOid,ownerCiOid);
	}

	public ClassAttributeInfo(int attributeId , String name, Class nativeClass, String fullClassName, ClassOid attributeCiOid, ClassOid ownerCiOid) {
		super();
		this.id = attributeId;
		this.name = name;
		this.nativeClass = nativeClass;
		this.className = fullClassName;
		if(nativeClass!=null){
			attributeType = ODBType.getFromClass(nativeClass);
		}else{
			if(fullClassName!=null){
				attributeType = ODBType.getFromName(fullClassName);
			}
		}
		if(attributeCiOid!=null){
			attributeClassOid = attributeCiOid;
		}
		if(ownerCiOid!=null){
			ownerClassInfoOid = ownerCiOid;
		}
		//classInfo = info;
		isIndex = false;
	}

	public boolean isIndex() {
		return isIndex;
	}

	public void setIndex(boolean isIndex) {
		this.isIndex = isIndex;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isNative() {
		return attributeType.isNative();
	}
    public boolean isNonNative() {
        return !attributeType.isNative();
    }

    public ClassOid getAttributeClassOid(){
    	return attributeClassOid;
    }
    public void setAttributeClassOid(ClassOid coid){
    	this.attributeClassOid = coid;
    }

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("id=").append(id).append(" name=").append(name).append(" | is Native=").append(isNative()).append(" | type=").append(getClassName()).append(" | isIndex=").append(isIndex);
		return buffer.toString();
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setAttributeType(ODBType attributeType) {
		this.attributeType = attributeType;
	}
	public ODBType getAttributeType() {
		return attributeType;
	}
	public Class getNativeClass() {
		return nativeClass;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @param ciId
	 */
	public void setOwnerClassInfoOid(ClassOid ciId) {
		this.ownerClassInfoOid = ciId;
	}
	public ClassOid getOwnerClassInfoOid() {
		return this.ownerClassInfoOid;
	}
	
}
