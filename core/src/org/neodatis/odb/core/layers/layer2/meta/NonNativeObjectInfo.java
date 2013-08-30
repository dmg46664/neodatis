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

import java.util.Map;

import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.OID;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.tool.wrappers.OdbClassUtil;
import org.neodatis.tool.wrappers.OdbString;
import org.neodatis.tool.wrappers.list.IOdbList;
import org.neodatis.tool.wrappers.list.OdbArrayList;

/**
 * To keep info about a non native object
 * 
 * <pre>
 * 
 *  - Keeps its class info : a meta information to describe its type
 *  - All its attributes values
 *  - Its Pointers : its position, the previous object OID, the next object OID
 *  - The Object being represented by The meta information
 * </pre>
 * 
 * @author olivier s
 * 
 */
public class NonNativeObjectInfo extends AbstractObjectInfo {
	/** The object being represented */
	protected transient Object object;
	/** The class info is the class meta information */
	private ClassInfo classInfo;
	/** The header of the meta representation */
	private ObjectInfoHeader objectHeader;
	/** All the attributes */
	private AbstractObjectInfo[] attributeValues;
	/**
	 * To keep track of all non native objects , It will be used in layer 3 to
	 * optimize the conversion of NonNativeObjectInfo to bytes
	 */
	private IOdbList<NonNativeObjectInfo> directNonNativeAttributes;
	/**
	 * To keep track of all non native objects contained in the hierarchy of the
	 * objects but not as direct attributes, objects contained in lists, arrays,
	 * and non native attributes
	 */
	private IOdbList<NonNativeObjectInfo> nonDirectNonNativeObjects;
	private int maxNbattributes;

	public NonNativeObjectInfo() {
		super(null);
		this.objectHeader = new ObjectInfoHeader();
	}

	public NonNativeObjectInfo(ObjectInfoHeader oip, ClassInfo classInfo) {
		super(null);
		this.classInfo = classInfo;
		this.objectHeader = oip;
		if (classInfo != null) {
			this.maxNbattributes = classInfo.getMaxAttributeId();
			this.attributeValues = new AbstractObjectInfo[maxNbattributes];
		}
		this.directNonNativeAttributes = new OdbArrayList<NonNativeObjectInfo>();
		this.nonDirectNonNativeObjects = new OdbArrayList<NonNativeObjectInfo>();
	}

	public NonNativeObjectInfo(ClassInfo classInfo) {
		super(null);
		this.classInfo = classInfo;
		this.objectHeader = new ObjectInfoHeader((classInfo != null ? classInfo.getOid() : null), null);
		if (classInfo != null) {
			this.maxNbattributes = classInfo.getMaxAttributeId();
			this.attributeValues = new AbstractObjectInfo[maxNbattributes];
		}
		this.directNonNativeAttributes = new OdbArrayList<NonNativeObjectInfo>();
		this.nonDirectNonNativeObjects = new OdbArrayList<NonNativeObjectInfo>();
	}

	public NonNativeObjectInfo(Object object, ClassInfo ci, AbstractObjectInfo[] values, AttributeIdentification[] attributesIdentification) {
		super(ODBType.getFromName(ci.getFullClassName()));
		this.object = object;
		this.classInfo = ci;
		this.attributeValues = values;
		this.maxNbattributes = classInfo.getMaxAttributeId();
		if (attributeValues == null) {
			this.attributeValues = new AbstractObjectInfo[maxNbattributes];
		}
		this.objectHeader = new ObjectInfoHeader((classInfo != null ? classInfo.getOid() : null), attributesIdentification);
		this.directNonNativeAttributes = new OdbArrayList<NonNativeObjectInfo>();
		this.nonDirectNonNativeObjects = new OdbArrayList<NonNativeObjectInfo>();
	}

	public ObjectInfoHeader getHeader() {
		return objectHeader;
	}

	public AbstractObjectInfo getAttributeValueFromId(int attributeId) {
		return attributeValues[attributeId - 1];
	}

	public ClassInfo getClassInfo() {
		return classInfo;
	}

	public void setClassInfo(ClassInfo classInfo) {
		if (classInfo != null) {
			this.classInfo = classInfo;
			this.objectHeader.setClassInfoId(classInfo.getOid());
		}
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer(classInfo.getFullClassName()).append("(").append(getOid()).append(")=");

		if (attributeValues == null) {
			buffer.append("null attribute values");
			return buffer.toString();
		}

		for (int i = 0; i < attributeValues.length; i++) {
			if (i != 0) {
				buffer.append(",");
			}

			String attributeName = "?";
			try {
				attributeName = (classInfo != null ? (classInfo.getAttributeInfo(i)).getName() : "?");
			} catch (Exception e) {
				continue;
			}
			buffer.append(attributeName).append("=");
			Object object = attributeValues[i];
			if (object == null) {
				buffer.append(" null java object - should not happen , ");
			} else {
				ODBType type = ODBType.getFromClass(attributeValues[i].getClass());
				if (object instanceof NonNativeNullObjectInfo) {
					buffer.append("null");
					continue;
				}
				if (object instanceof NonNativeDeletedObjectInfo) {
					buffer.append("deleted object");
					continue;
				}
				if (object instanceof NativeObjectInfo) {
					NativeObjectInfo noi = (NativeObjectInfo) object;
					buffer.append(noi.toString());
					continue;
				}
				if (object instanceof ObjectReference) {
					buffer.append(object.toString());
					continue;
				}
				if (object instanceof NonNativeObjectInfo) {
					NonNativeObjectInfo nnoi = (NonNativeObjectInfo) object;
					buffer.append("@").append(nnoi.getClassInfo().getFullClassName()).append("(id=").append(nnoi.getOid()).append(")");
					continue;
				}
				buffer.append("@").append(OdbClassUtil.getClassName(type.getName()));
			}
		}
		return buffer.toString();
	}

	public Object getObject() {
		return object;
	}

	public Object getValueOf(String attributeName) {
		int attributeId = -1;
		boolean isRelation = attributeName.indexOf(".") != -1;
		if (!isRelation) {
			attributeId = getClassInfo().getAttributeId(attributeName);
			return getAttributeValueFromId(attributeId).getObject();
		}
		int firstDotIndex = attributeName.indexOf(".");
		String firstAttributeName = OdbString.substring(attributeName, 0, firstDotIndex);
		attributeId = getClassInfo().getAttributeId(firstAttributeName);
		Object object = attributeValues[attributeId];
		if (object instanceof NonNativeObjectInfo) {
			NonNativeObjectInfo nnoi = (NonNativeObjectInfo) object;
			return nnoi.getValueOf(OdbString.substring(attributeName, firstDotIndex + 1, attributeName.length()));
		}
		throw new NeoDatisRuntimeException(NeoDatisError.CLASS_INFO_DO_NOT_HAVE_THE_ATTRIBUTE.addParameter(getClassInfo().getFullClassName()).addParameter(
				attributeName));
	}

	public AbstractObjectInfo getMetaValueOf(String attributeName) {
		int attributeId = -1;
		boolean isRelation = attributeName.indexOf(".") != -1;
		if (!isRelation) {
			attributeId = getClassInfo().getAttributeId(attributeName);
			try{
				return getAttributeValueFromId(attributeId);
			}catch (ArrayIndexOutOfBoundsException e) {
				throw new NeoDatisRuntimeException("Attribute " + attributeName+ " does not exist on object "+ this);
			}
		}
		int firstDotIndex = attributeName.indexOf(".");
		String firstAttributeName = OdbString.substring(attributeName,0, firstDotIndex);
		attributeId = getClassInfo().getAttributeId(firstAttributeName);
		Object object = attributeValues[attributeId];
		if (object instanceof NonNativeObjectInfo) {
			NonNativeObjectInfo nnoi = (NonNativeObjectInfo) object;
			return nnoi.getMetaValueOf(OdbString.substring(attributeName,firstDotIndex + 1, attributeName.length()));
		}
		throw new NeoDatisRuntimeException(NeoDatisError.CLASS_INFO_DO_NOT_HAVE_THE_ATTRIBUTE.addParameter(getClassInfo().getFullClassName())
				.addParameter(attributeName));
	}

	/**
	 * Used to change the value of an attribute
	 * 
	 * @param attributeName
	 * @param aoi
	 */
	public void setValueOf(String attributeName, AbstractObjectInfo aoi) {
		int attributeId = -1;
		boolean isRelation = attributeName.indexOf(".") != -1;
		if (!isRelation) {
			attributeId = getClassInfo().getAttributeId(attributeName);
			setAttributeValue(attributeId, aoi);
			return;
		}
		int firstDotIndex = attributeName.indexOf(".");
		String firstAttributeName = OdbString.substring(attributeName, 0, firstDotIndex);
		attributeId = getClassInfo().getAttributeId(firstAttributeName);
		Object object = attributeValues[attributeId];
		if (object instanceof NonNativeObjectInfo) {
			NonNativeObjectInfo nnoi = (NonNativeObjectInfo) object;
			nnoi.setValueOf(OdbString.substring(attributeName, firstDotIndex + 1, attributeName.length()), aoi);
		}
		throw new NeoDatisRuntimeException(NeoDatisError.CLASS_INFO_DO_NOT_HAVE_THE_ATTRIBUTE.addParameter(getClassInfo().getFullClassName()).addParameter(
				attributeName));
	}

	public ObjectOid getOid() {
		if (getHeader() == null) {
			throw new NeoDatisRuntimeException(NeoDatisError.UNEXPECTED_SITUATION.addParameter("Null Object Info Header"));
		}
		return getHeader().getOid();
	}

	public void setOid(ObjectOid oid) {
		if (getHeader() != null) {
			getHeader().setOid(oid);
		}
	}

	public boolean isNonNativeObject() {
		return true;
	}

	public boolean isNull() {
		return false;
	}

	public void clear() {
		attributeValues = null;
	}

	/**
	 * Create a copy oh this meta object
	 * 
	 * @param onlyData
	 *            if true, only copy attributes values
	 * @return
	 */
	public AbstractObjectInfo createCopy(Map<OID, AbstractObjectInfo> cache, boolean onlyData) {
		NonNativeObjectInfo nnoi = (NonNativeObjectInfo) cache.get(objectHeader.getOid());
		if (nnoi != null) {
			return nnoi;
		}

		if (onlyData) {
			ObjectInfoHeader oih = new ObjectInfoHeader();
			nnoi = new NonNativeObjectInfo(object, classInfo, null, oih.getAttributesIdentification());
		} else {
			nnoi = new NonNativeObjectInfo(object, classInfo, null, objectHeader.getAttributesIdentification());
			nnoi.getHeader().setOid(getHeader().getOid());
		}
		AbstractObjectInfo[] newAttributeValues = new AbstractObjectInfo[attributeValues.length];
		for (int i = 0; i < attributeValues.length; i++) {
			newAttributeValues[i] = attributeValues[i].createCopy(cache, onlyData);
		}
		nnoi.attributeValues = newAttributeValues;
		cache.put(objectHeader.getOid(), nnoi);

		return nnoi;
	}

	public void setAttributeValue(int attributeId, AbstractObjectInfo aoi) {
		attributeValues[attributeId - 1] = aoi;

		// Keep track of all non native objects.
		if (aoi.isNonNativeObject()) {
			directNonNativeAttributes.add((NonNativeObjectInfo) aoi);
		} else if (aoi.isGroup()) {
			// isGroup = if IsCollection || IsMap || IsArray
			GroupObjectInfo goi = (GroupObjectInfo) aoi;
			nonDirectNonNativeObjects.addAll(goi.getNonNativeObjects());
		}
	}

	public AbstractObjectInfo[] getAttributeValues() {
		return attributeValues;
	}

	public int getMaxNbattributes() {
		return maxNbattributes;
	}

	/**
	 * The performance of this method is bad. But it is not used by the engine,
	 * only in the ODBExplorer
	 * 
	 * @param aoi
	 * @return
	 */
	public int getAttributeId(AbstractObjectInfo aoi) {
		for (int i = 0; i < attributeValues.length; i++) {
			if (aoi == attributeValues[i]) {
				return i + 1;
			}
		}
		return -1;
	}

	/**
	 * Return the position where the position of an attribute is stored.
	 * 
	 * <pre>
	 * 	If a object has 3 attributes and if it is stored at position x
	 * Then the number of attributes (3) is stored at x+StorageEngineConstant.OBJECT_OFFSET_NB_ATTRIBUTES
	 * and first attribute id definition is stored at x+StorageEngineConstant.OBJECT_OFFSET_NB_ATTRIBUTES+size-of(int)
	 * and first attribute position is stored at x+StorageEngineConstant.OBJECT_OFFSET_NB_ATTRIBUTES+size-of(int)+size-of(int)
	 * 
	 * the second attribute id is stored at x+StorageEngineConstant.OBJECT_OFFSET_NB_ATTRIBUTES+size-of(int)+size-of(int)+size-of(long)
	 * the second attribute position is stored at x+StorageEngineConstant.OBJECT_OFFSET_NB_ATTRIBUTES+size-of(int)+size-of(int)+size-of(long)+size-of(int)
	 * 
	 * <pre>
	 * FIXME Remove dependency of StorageEngineConstant!
	 * 
	 * @param attributeId
	 * @return The position where this attribute is stored
	 */
	public long getAttributeDefinitionPosition(int attributeId) {
		throw new NeoDatisRuntimeException(NeoDatisError.NOT_YET_IMPLEMENTED);
		/*
		 * long offset = StorageEngineConstant.OBJECT_OFFSET_NB_ATTRIBUTES; //
		 * delta = // Skip NbAttribute (int) + // Delta attribute
		 * (attributeId-1) * attribute definition size = // INT+LONG // Skip
		 * attribute Id (int) long delta = ODBType.INTEGER.getSize() +
		 * (attributeId - 1) * (ODBType.INTEGER.getSize() +
		 * ODBType.LONG.getSize()) + ODBType.INTEGER.getSize(); return
		 * getPosition() + offset + delta;
		 */
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	public int hashCode() {
		// This happens when the object is deleted
		if (objectHeader == null) {
			return -1;
		}
		return objectHeader.hashCode();
	}

	/**
	 * @param header
	 */
	public void setHeader(ObjectInfoHeader header) {
		this.objectHeader = header;
	}

	public IOdbList<NonNativeObjectInfo> getDirectNonNativeAttributes() {
		return directNonNativeAttributes;
	}

	public void setDirectNonNativeAttributes(IOdbList<NonNativeObjectInfo> directNonNativeAttributes) {
		this.directNonNativeAttributes = directNonNativeAttributes;
	}

	public IOdbList<NonNativeObjectInfo> getNonDirectNonNativeObjects() {
		return nonDirectNonNativeObjects;
	}

	public void setNonDirectNonNativeObjects(IOdbList<NonNativeObjectInfo> nonDirectNonNativeObjects) {
		this.nonDirectNonNativeObjects = nonDirectNonNativeObjects;
	}

}
