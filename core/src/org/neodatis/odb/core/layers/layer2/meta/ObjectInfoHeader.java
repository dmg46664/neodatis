
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
import org.neodatis.odb.ObjectOid;
import org.neodatis.tool.wrappers.OdbTime;

/**
 * Some basic info about an object info like position, its class info,...
 * @author osmadja
 *
 */
public class ObjectInfoHeader implements Serializable {
	
    private ClassOid classInfoId;
    /** Can be position(for native object) or id(for non native object, positions are positive e ids are negative*/
    private AttributeIdentification [] attributesIdentification;
    private ObjectOid oid;
    private long creationDate;
    private long updateDate;
    private long objectVersion;
    private int attributePosition;

    public ObjectInfoHeader(ClassOid classInfoId, AttributeIdentification [] attributesIdentification) {
		this.oid = null;
		this.classInfoId = classInfoId;
        this.attributesIdentification = attributesIdentification;
        this.objectVersion = 0;
        this.creationDate = OdbTime.getCurrentTimeInMs();
    }
	public ObjectInfoHeader() {
		super();
		this.oid = null;
		this.objectVersion = 1;
		this.creationDate = OdbTime.getCurrentTimeInMs();
	}
	public int getNbAttributes(){
		return attributesIdentification.length;
	}

	public int getAttributePosition() {
		return attributePosition;
	}
	public void setAttributePosition(int position) {
		this.attributePosition = position;
	}
    
	public ClassOid getClassInfoId() {
		return classInfoId;
	}

	public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("oid=").append(oid).append(" - ");//.append("class info id=").append(classInfoId);
        buffer.append(" - attr position=").append(attributePosition);
        buffer.append(" attrs =[");
        if(attributesIdentification!=null){
            for(int i=0;i<attributesIdentification.length;i++){
                buffer.append(attributesIdentification[i]).append( " ");
            }
        }else{
            buffer.append(" nulls ");
        }
        buffer.append(" ]");
        return buffer.toString();
    }

    public AttributeIdentification[] getAttributesIdentification() {
        return attributesIdentification;
    }

    public void setAttributesIdentification(AttributeIdentification[] attributesIdentification) {
        this.attributesIdentification = attributesIdentification;
    }

    public ObjectOid getOid() {
        return oid;
    }

    public void setOid(ObjectOid oid) {
        this.oid = oid;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
    }

    /** Return the attribute identification (position or id) from the attribute id
     * 
     *FIXME Remove dependency from StorageEngineConstant
     * @param attributeId
     * @return -1 if attribute with this id does not exist
     */
    public AttributeIdentification getAttributeIdentificationFromId(int attributeId){
        if(attributesIdentification==null){
        	return null;
        }
    	for(int i=0;i<attributesIdentification.length;i++){
            if(attributesIdentification[i].id==attributeId){
                return attributesIdentification[i];
            }
        }
        return null;
    }
    public int getAttributeId(int attributeIndex){
        return attributesIdentification[attributeIndex].id;
    }
	
	public void setClassInfoId(ClassOid classInfoId2) {
		this.classInfoId = classInfoId2;		
	}
	public long getObjectVersion() {
		return objectVersion;
	}
	public void setObjectVersion(long objectVersion) {
		this.objectVersion = objectVersion;
	}
	public int hashCode() {
		if(oid==null){
			System.out.println("debug:null oid");
		}
		return oid.hashCode();
	}
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ObjectInfoHeader other = (ObjectInfoHeader) obj;
		if (!oid.equals(other.oid)){
			return false;
		}
		return true;
	}
	public void incrementVersionAndUpdateDate(){
		objectVersion++;
		updateDate = OdbTime.getCurrentTimeInMs();
		
	}
	
	public ObjectInfoHeader duplicate(){
		ObjectInfoHeader oih = new ObjectInfoHeader();
		oih.setAttributesIdentification(attributesIdentification);
		oih.setClassInfoId(classInfoId);
		oih.setCreationDate(creationDate);
		oih.setObjectVersion(objectVersion);
		oih.setOid(oid);
		oih.setAttributePosition(attributePosition);
		oih.setUpdateDate(updateDate);
		return oih;
		
	}
	
}

