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
package org.neodatis.odb.core.layers.layer2.instance;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neodatis.odb.NeoDatisObject;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.OID;
import org.neodatis.odb.core.IError;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.context.NeoDatisContextImpl;
import org.neodatis.odb.core.layers.layer1.ClassIntrospector;
import org.neodatis.odb.core.layers.layer2.meta.AbstractObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.ArrayObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfo;
import org.neodatis.odb.core.layers.layer2.meta.CollectionObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.EnumNativeObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.LazyObjectReference;
import org.neodatis.odb.core.layers.layer2.meta.MapObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.NativeObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeNullObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.ODBType;
import org.neodatis.odb.core.session.Cache;
import org.neodatis.odb.core.session.Session;
import org.neodatis.odb.core.session.cross.CacheFactory;
import org.neodatis.odb.core.session.cross.ICrossSessionCache;
import org.neodatis.odb.core.trigger.TriggerManager;
import org.neodatis.tool.DLogger;
import org.neodatis.tool.wrappers.map.OdbHashMap;

/**
 * Class used to build instance from Meta Object representation. Layer 2 to
 * Layer 1 conversion.
 * 
 * @sharpen.ignore
 * @author osmadja
 * 
 */
public class InstanceBuilderImpl implements InstanceBuilder {
	private static final String LOG_ID = "InstanceBuilder";

	private static final String LOG_ID_DEBUG = "InstanceBuilder.debug";

	private TriggerManager triggerManager;
	private ClassIntrospector classIntrospector;
	private ClassPool classPool;
	protected Session session;
	/**
	 * To resolve cyclic reference while building an instance. Instead of using
	 * session cache for that, we use local cache(wich is smaller to speed up)
	 * instance building
	 * 
	 */
	private Map<OID, Object> localCache;

	public InstanceBuilderImpl(Session session, ClassIntrospector classIntrospector, TriggerManager triggerManager) {
		this.session = session;
		this.triggerManager = triggerManager;
		this.classIntrospector = classIntrospector;
		this.classPool = classIntrospector.getClassPool();
		this.localCache = new OdbHashMap<OID, Object>(50);
	}

	/**
	 * The entry point to build an instance from an object meta representation
	 * @param o The object to be filled, can be null
	 * @param objectInfo
	 * @return
	 */
	public Object buildOneInstance(NonNativeObjectInfo objectInfo, InstanceBuilderContext context) {
		try {
			return internalBuildOneInstance(objectInfo,context,1);
		} finally {
			// Clears the local cache after every creation
			localCache.clear();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.neodatis.odb.core.layers.layer2.instance.IInstanceBuilder#
	 * buildOneInstance
	 * (org.neodatis.odb.core.impl.layers.layer2.meta.AbstractObjectInfo)
	 */
	protected Object internalBuildOneInstance(AbstractObjectInfo objectInfo, InstanceBuilderContext context, int depth) {

		Object object = null;
		if (objectInfo instanceof NonNativeNullObjectInfo) {
			return null;
		}
		if (objectInfo.isNonNativeObject()) {
			object = internalBuildOneInstance((NonNativeObjectInfo) objectInfo, context, depth);
		} else {
			// instantiation cache is not used for native objects
			object = internalBuildOneInstance((NativeObjectInfo) objectInfo, null, context, depth);
		}

		return object;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.neodatis.odb.core.layers.layer2.instance.IInstanceBuilder#
	 * buildCollectionInstance
	 * (org.neodatis.odb.core.impl.layers.layer2.meta.CollectionObjectInfo)
	 */
	protected Object internalBuildCollectionInstance(CollectionObjectInfo coi, InstanceBuilderContext context, int depth) {
		Collection<Object> newCollection = null;
		try {
			newCollection = (Collection<Object>) classPool.getClass(coi.getRealCollectionClassName()).newInstance();
		} catch (Exception e) {
			throw new NeoDatisRuntimeException(NeoDatisError.COLLECTION_INSTANCIATION_ERROR.addParameter(coi.getRealCollectionClassName()), e);
		}
		Iterator iterator = coi.getCollection().iterator();
		AbstractObjectInfo aoi = null;
		while (iterator.hasNext()) {
			aoi = (AbstractObjectInfo) iterator.next();
			if (!aoi.isDeletedObject()) {
				newCollection.add(internalBuildOneInstance(aoi, context,depth));
			}
		}
		return newCollection;
	}

	/**
	 * Builds an instance of an enum
	 * 
	 * @param enumClass
	 */
	protected Object internalBuildEnumInstance(EnumNativeObjectInfo enoi) {
		String className = session.getMetaModel().getClassInfoFromId(enoi.getEnumClassOid()).getFullClassName();
		Class clazz = classPool.getClass(className);
		Object theEnum = Enum.valueOf(clazz, enoi.getEnumName());
		return theEnum;
	}

	/**
	 * Builds an insatnce of an array
	 */
	protected Object internalBuildArrayInstance(ArrayObjectInfo aoi, InstanceBuilderContext context, int depth) {
		// first check if array element type is native (int,short, for example)
		ODBType type = ODBType.getFromName(aoi.getRealArrayComponentClassName());

		Class arrayClazz = type.getNativeClass(classPool);
		Object array = Array.newInstance(arrayClazz, aoi.getArray().length);
		Object object = null;
		AbstractObjectInfo aboi = null;
		for (int i = 0; i < aoi.getArrayLength(); i++) {

			aboi = (AbstractObjectInfo) aoi.getArray()[i];
			if (aboi != null && !aboi.isDeletedObject() && !aboi.isNull()) {
				object = internalBuildOneInstance(aboi, context,depth);
				Array.set(array, i, object);
			}
		}
		return array;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.neodatis.odb.core.layers.layer2.instance.IInstanceBuilder#
	 * buildMapInstance
	 * (org.neodatis.odb.core.impl.layers.layer2.meta.MapObjectInfo)
	 */
	protected Map internalBuildMapInstance(MapObjectInfo mapObjectInfo, InstanceBuilderContext context, int depth) {
		Map<AbstractObjectInfo, AbstractObjectInfo> map = mapObjectInfo.getMap();
		Map newMap;
		try {
			newMap = (Map) classPool.getClass(mapObjectInfo.getRealMapClassName()).newInstance();
		} catch (Exception e) {
			throw new NeoDatisRuntimeException(NeoDatisError.MAP_INSTANCIATION_ERROR.addParameter(map.getClass().getName()));
		}

		Iterator<AbstractObjectInfo> iterator = map.keySet().iterator();
		AbstractObjectInfo key = null;
		while (iterator.hasNext()) {
			key = iterator.next();
			Object realKey = internalBuildOneInstance(key, context,depth);
			Object realValue = internalBuildOneInstance(map.get(key), context,depth);
			newMap.put(realKey, realValue);
		}
		return newMap;
	}

	/**
	 * Main entry point to build an instance from an object meta representation
	 * 
	 */
	protected Object internalBuildOneInstance(NonNativeObjectInfo objectInfo, InstanceBuilderContext context, int depth) {

		// Gets the session cache
		Cache cache = session.getCache();

		// verify if the object is marked as deleted
		if (objectInfo.isDeletedObject()) {
			throw new NeoDatisRuntimeException(NeoDatisError.OBJECT_IS_MARKED_AS_DELETED_FOR_OID.addParameter(objectInfo.getOid()));
		}
		// Then check if object is in session cache
		Object o = null;
		
		if(context.useCache){
			o = cache.getObjectWithOid(objectInfo.getOid());
			if (o != null) {
				return o;
			}

			// Then check if object is in local cache. this is to avoid cyclic
			// reference problem
			o = localCache.get(objectInfo.getOid());
			if (o != null) {
				return o;
			}

		}


		Class instanceClazz = null;
		String className = objectInfo.getClassInfo().getFullClassName();
		instanceClazz = classPool.getClass(className);

		if(context.existingObject==null){
			try {
				o = classIntrospector.newInstanceOf(instanceClazz);
			} catch (Exception e) {
				throw new NeoDatisRuntimeException(NeoDatisError.INSTANCIATION_ERROR.addParameter(className), e);
			}
		}else{
			o = context.existingObject;
		}

		// This can happen if ODB can not create the instance
		// TODO Check if returning null is correct
		if (o == null) {
			return null;
		}

		// Adds this incomplete instance in the cache to manage cyclic reference
		localCache.put(objectInfo.getOid(), o);

		// check if we can associate the NeoDatis Context
		if (o instanceof NeoDatisObject && session.getConfig().reconnectObjectsToSession()) {
			NeoDatisObject no = (NeoDatisObject) o;
			no.setNeoDatisContext(new NeoDatisContextImpl(objectInfo.getOid()));
		}

		if(objectInfo instanceof LazyObjectReference){
			cache.addObject(objectInfo.getOid(), o);
			return o;
		}
		ClassInfo ci = objectInfo.getClassInfo();
		List fields = classIntrospector.getAllFields(className);

		Field field = null;
		AbstractObjectInfo aoi = null;
		Object value = null;
		for (int i = 0; i < fields.size(); i++) {
			field = (Field) fields.get(i);
			// Gets the id of this field
			int attributeId = ci.getAttributeId(field.getName());
			// If attributeId==-1, the attribute does not exist in the metamodel
			if (attributeId == -1) {
				throw new NeoDatisRuntimeException(NeoDatisError.CLASS_INFO_DO_NOT_HAVE_THE_ATTRIBUTE.addParameter(ci.getFullClassName())
						.addParameter(field.getName()));
			}
			aoi = objectInfo.getAttributeValueFromId(attributeId);

			if (aoi != null && (!aoi.isNull())) {

				if (aoi.isNative()) {
					value = internalBuildOneInstance((NativeObjectInfo) aoi, field.getType(), context,depth);
				} else if (aoi.isNonNativeObject()) {
					if (aoi.isDeletedObject()) {
						if (session.getConfig().displayWarnings()) {
							IError warning = NeoDatisError.ATTRIBUTE_REFERENCES_A_DELETED_OBJECT.addParameter(className).addParameter(
									objectInfo.getOid()).addParameter(field.getName());
							DLogger.info(warning.toString());
						}
						value = null;
					} else {
						value = internalBuildOneInstance((NonNativeObjectInfo) aoi, context,depth + 1);
					}
				}

				if (value != null) {

					try {
						field.set(o, value);
					} catch (Exception e) {
						throw new NeoDatisRuntimeException(NeoDatisError.INSTANCE_BUILDER_WRONG_OBJECT_CONTAINER_TYPE.addParameter(
								objectInfo.getClassInfo().getFullClassName()).addParameter(value.getClass().getName()).addParameter(
								field.getType().getName()).addParameter(field.getName()), e);
					}
				}
			}
		}
		if (o != null && !o.getClass().getName().equals(objectInfo.getClassInfo().getFullClassName())) {
			new NeoDatisRuntimeException(NeoDatisError.INSTANCE_BUILDER_WRONG_OBJECT_TYPE.addParameter(
					objectInfo.getClassInfo().getFullClassName()).addParameter(o.getClass().getName()));
		}

		cache.addObject(objectInfo.getOid(), o);// , objectInfo.getHeader());

		if (triggerManager != null) {
			triggerManager.manageSelectTriggerAfter(objectInfo.getClassInfo().getFullClassName(), objectInfo, objectInfo.getOid());
		}

		if (session.getConfig().reconnectObjectsToSession()) {

			ICrossSessionCache crossSessionCache = CacheFactory.getCrossSessionCache(session.getBaseIdentification().getBaseId());
			crossSessionCache.addObject(o, objectInfo.getOid());

		}
		return o;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.neodatis.odb.core.layers.layer2.instance.IInstanceBuilder#
	 * buildOneInstance
	 * (org.neodatis.odb.core.impl.layers.layer2.meta.NativeObjectInfo)
	 */
	protected Object internalBuildOneInstance(NativeObjectInfo objectInfo, Class fieldType, InstanceBuilderContext context, int depth) {
		if (objectInfo.isNull()) {
			return null;
		}
		if (objectInfo.isAtomicNativeObject()) {
			return objectInfo.getObject();
		}

		if (objectInfo.isCollectionObject()) {
			Object value = internalBuildCollectionInstance((CollectionObjectInfo) objectInfo, context,depth);

			if (fieldType == null) {
				fieldType = objectInfo.getObject().getClass();
			}
			// Manage a specific case of Set
			if (Set.class.isAssignableFrom(fieldType) && Collection.class.isAssignableFrom(value.getClass())) {
				Set s = new HashSet();
				s.addAll((Collection) value);
				value = s;
			}
			return value;

		}
		if (objectInfo.isArrayObject()) {
			return internalBuildArrayInstance((ArrayObjectInfo) objectInfo, context,depth);
		}
		if (objectInfo.isMapObject()) {
			return internalBuildMapInstance((MapObjectInfo) objectInfo, context,depth);
		}
		if (objectInfo.isEnumObject()) {
			EnumNativeObjectInfo enoi = (EnumNativeObjectInfo) objectInfo;
			return internalBuildEnumInstance((EnumNativeObjectInfo) objectInfo);
		}

		throw new NeoDatisRuntimeException(NeoDatisError.INSTANCE_BUILDER_NATIVE_TYPE.addParameter(ODBType.getNameFromId(objectInfo
				.getOdbTypeId())));
	}

	public String getSessionId() {
		return session.getId();
	}

	public boolean isLocal() {
		return session.isLocal();
	}

}
