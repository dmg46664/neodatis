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
package org.neodatis.odb.core.layers.layer1;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.context.ObjectReconnector;
import org.neodatis.odb.core.layers.layer2.meta.AbstractObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.ArrayObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.AtomicNativeObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.AttributeIdentification;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfo;
import org.neodatis.odb.core.layers.layer2.meta.CollectionObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.EnumNativeObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.MapObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeNullObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.NullNativeObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.ODBType;
import org.neodatis.odb.core.layers.layer2.meta.ObjectInfoHeader;
import org.neodatis.odb.core.layers.layer2.meta.ObjectReference;
import org.neodatis.odb.core.layers.layer4.OidGenerator;
import org.neodatis.odb.core.session.Cache;
import org.neodatis.odb.core.session.Session;
import org.neodatis.odb.core.trigger.TriggerManager;
import org.neodatis.tool.wrappers.OdbReflection;
import org.neodatis.tool.wrappers.list.IOdbList;
import org.neodatis.tool.wrappers.map.OdbHashMap;

/**
 * The local implementation of the Object Introspector.
 * 
 * @author osmadja
 * 
 */
public class ObjectIntrospectorImpl implements ObjectIntrospector {
	protected ClassIntrospector classIntrospector;
	protected Session session;
	protected OidGenerator oidGenerator;
	protected ObjectReconnector objectReconnector;
	protected TriggerManager triggerManager;

	public ObjectIntrospectorImpl(Session session, ClassIntrospector classIntrospector, OidGenerator oidGenerator) {
		this.session = session;
		this.classIntrospector = classIntrospector;
		this.oidGenerator = oidGenerator;
		if (session.getConfig().reconnectObjectsToSession()) {
			this.objectReconnector = new ObjectReconnector();
		}
	}

	public NonNativeObjectInfo getMetaRepresentation(Object object, IntrospectionCallback callback) {
		return (NonNativeObjectInfo) getObjectInfoInternal(object, new HashMap<Object, NonNativeObjectInfo>(), callback);
	}

	public AbstractObjectInfo getGenericMetaRepresentation(Object object, IntrospectionCallback callback) {
		return getObjectInfoInternal(object, new HashMap<Object, NonNativeObjectInfo>(), callback);
	}

	/**
	 * Build a meta representation of an object
	 * 
	 * <pre>
	 * warning: When an object has two fields with the same name (a private field with the same name in a parent class, the deeper field (of the parent) is ignored!)
	 * </pre>
	 * 
	 * @param nnoi
	 *            The NonNativeObjectInfo to fill
	 * @param object
	 * @param ci
	 * @param recursive
	 * @return The ObjectInfo
	 */
	protected AbstractObjectInfo getObjectInfoInternal(Object object, Map<Object, NonNativeObjectInfo> alreadyReadObjects, IntrospectionCallback callback) {

		Object value = null;

		if (object == null) {
			return NullNativeObjectInfo.getInstance();
		}
		// retrieve object class info
		Class clazz = object.getClass();
		ODBType type = ODBType.getFromClass(clazz);
		String className = clazz.getName();

		if (type.isNative()) {
			return getNativeObjectInfoInternal(type, object, alreadyReadObjects, callback);
		}

		ClassInfo ci = getClassInfo(className);

		NonNativeObjectInfo mainAoi = buildNnoi(object, ci, null, null);

		boolean isRootObject = false;
		if (alreadyReadObjects == null) {
			alreadyReadObjects = new OdbHashMap<Object, NonNativeObjectInfo>();
			isRootObject = true;
		}

		if (object != null) {
			NonNativeObjectInfo cachedNnoi = alreadyReadObjects.get(object);
			if (cachedNnoi != null) {
				ObjectReference or = new ObjectReference(cachedNnoi);
				return or;
			}
			objectFound(object, mainAoi.getOid(), callback);
		}

		alreadyReadObjects.put(object, mainAoi);

		IOdbList<Field> fields = classIntrospector.getAllFields(className);
		AbstractObjectInfo aoi = null;
		int attributeId = -1;
		// For all fields
		for (int i = 0; i < fields.size(); i++) {
			Field field = fields.get(i);

			try {
				value = field.get(object);
				attributeId = ci.getAttributeId(field.getName());

				if (attributeId == -1) {
					throw new NeoDatisRuntimeException(NeoDatisError.OBJECT_INTROSPECTOR_NO_FIELD_WITH_NAME.addParameter(ci.getFullClassName()).addParameter(
							field.getName()));
				}
				ODBType valueType = null;
				if (value == null) {
					// If value is null, take the type from the field type
					// declared in the class
					valueType = ODBType.getFromClass(field.getType());
				} else {
					// Else take the real attribute type!
					valueType = ODBType.getFromClass(value.getClass());
				}

				// for native fields
				if (valueType.isNative()) {
					aoi = getNativeObjectInfoInternal(valueType, value, alreadyReadObjects, callback);
					mainAoi.setAttributeValue(attributeId, aoi);
				} else {
					// Non Native Objects
					ClassInfo clai = getClassInfo(valueType.getName());

					if (value == null) {
						aoi = new NonNativeNullObjectInfo();
						mainAoi.setAttributeValue(attributeId, aoi);
					} else {
						aoi = getObjectInfoInternal(value, alreadyReadObjects, callback);
						mainAoi.setAttributeValue(attributeId, aoi);
					}
				}
			} catch (IllegalArgumentException e) {
				throw new NeoDatisRuntimeException(NeoDatisError.INTERNAL_ERROR.addParameter("in getObjectInfoInternal"), e);
			} catch (IllegalAccessException e) {
				throw new NeoDatisRuntimeException(NeoDatisError.INTERNAL_ERROR.addParameter("getObjectInfoInternal"), e);
			}
		}
		if (isRootObject) {
			alreadyReadObjects.clear();
			alreadyReadObjects = null;
		}
		return mainAoi;

	}

	protected void objectFound(Object object, ObjectOid objectOid, IntrospectionCallback callback) {
		if (callback != null) {
			callback.objectFound(object, objectOid);
		}
	}

	protected TriggerManager getTriggerManager() {
		if (triggerManager == null) {
			triggerManager = session.getEngine().getTriggerManager();
		}
		return triggerManager;
	}

	/**
	 * Builds the meta representation object from the object
	 * 
	 */
	public NonNativeObjectInfo buildNnoi(Object object, ClassInfo classInfo, AbstractObjectInfo[] values, AttributeIdentification[] attributesIdentification) {

		NonNativeObjectInfo nnoi = new NonNativeObjectInfo(object, classInfo, values, attributesIdentification);

		if (session != null) {// for unit test purpose
			Cache cache = session.getCache();
			// Check if object is in the cache, if so sets its oid
			ObjectOid oid = (ObjectOid) cache.getOid(object, false);
			if (oid != null) {
				// oid is not new anymore
				oid.setIsNew(false);
				nnoi.setOid(oid);
				// TODO we reload the header from the db here, is it good. In
				// 1.9, we had a cache of ObjectHeader
				ObjectInfoHeader oih = null;// session.getEngine().getMetaHeaderFromOid(oid,false,true);
				// Here oih can be null if object has not been stored yet In
				// this, we don't need to set object version,....
				// TODO if oih is null, maybe we should return an object
				// reference instead of a nnoi.
				if (oih != null) {
					nnoi.getHeader().setObjectVersion(oih.getObjectVersion());
					nnoi.getHeader().setUpdateDate(oih.getUpdateDate());
					nnoi.getHeader().setCreationDate(oih.getCreationDate());
				}
			} else {

				boolean reconnected = false;

				if (objectReconnector != null) {
					reconnected = objectReconnector.tryToReconnect(object, nnoi);
				}

				if (reconnected) {
					return nnoi;
				}

				// we need a new OID
				oid = getNextObjectOid(classInfo);
				// To indicate that this oid has been created now!
				oid.setIsNew(true);
				nnoi.setOid(oid);
				// we need to put the object in the cache
				cache.addObject(oid, object);

				String fullClassName = classInfo.getFullClassName();
				if (getTriggerManager().hasOidTriggersFor(fullClassName)) {
					getTriggerManager().manageOidTrigger(fullClassName, object, oid);
				}

			}
		}
		return nnoi;
	}

	/**
	 * @param classInfo
	 * @return
	 */
	private ObjectOid getNextObjectOid(ClassInfo classInfo) {
		if (oidGenerator == null) {
			oidGenerator = session.getEngine().getStorageEngine().getOidGenerator();
		}
		return oidGenerator.createObjectOid(classInfo.getOid());
	}

	/**
	 * 
	 * @param type
	 *            The odb type of the object
	 * @param object
	 *            The object to be introspected
	 * @param alreadyReadObjects
	 *            All the objects that has already been read
	 * @param callback
	 * @return
	 */
	protected AbstractObjectInfo getNativeObjectInfoInternal(ODBType type, Object object, Map<Object, NonNativeObjectInfo> alreadyReadObjects,
			IntrospectionCallback callback) {

		AbstractObjectInfo aoi = null;

		if (type.isAtomicNative()) {
			if (object == null) {
				return new NullNativeObjectInfo(type.getId());
			}
			return new AtomicNativeObjectInfo(object, type.getId());
		}

		if (type.isCollection()) {
			return introspectCollection((Collection) object, alreadyReadObjects, type, callback);
		}

		if (type.isArray()) {
			if (object == null) {
				return new ArrayObjectInfo(null);
			}
			// Gets the type of the elements of the array
			String realArrayClassName = object.getClass().getComponentType().getName();
			ArrayObjectInfo aroi = null;
			aroi = introspectArray(object, alreadyReadObjects, type, callback);
			aroi.setRealArrayComponentClassName(realArrayClassName);
			return aroi;
		}

		if (type.isMap()) {
			if (object == null) {
				return new MapObjectInfo(null, type, type.getDefaultInstanciationClass().getName());
			}

			MapObjectInfo moi = introspectMap((Map) object, alreadyReadObjects, callback);

			if (moi.getRealMapClassName().indexOf("$") != -1) {
				moi.setRealMapClassName(type.getDefaultInstanciationClass().getName());
			}
			return moi;

		}

		if (type.isEnum()) {
			Enum enumObject = (Enum) object;

			if (enumObject == null) {
				return new NullNativeObjectInfo(type.getId());
			}

			String enumClassName = enumObject == null ? null : enumObject.getClass().getName();

			// Here we must check if the enum is already in the meta model.
			// Enum must be stored in the meta
			// model to optimize its storing as we need to keep track of the
			// enum class
			// for each enum stored. So instead of storing the enum class
			// name, we can store enum class id, a long
			// instead of the full enum class name string
			ClassInfo ci = getClassInfo(enumClassName);
			// while introspecting, we get the enumName (enum.name()) and
			// not the toString() representation
			// So what will be stored is the name of the enum and not the
			// value (toString)
			// Check EqualCrtiterion too
			String enumValue = enumObject == null ? null : enumObject.name();
			return new EnumNativeObjectInfo(ci, enumValue);
		}

		throw new NeoDatisRuntimeException(NeoDatisError.INTERNAL_ERROR.addParameter(String.format("Unsupported type %s", type.getId())));
	}

	private CollectionObjectInfo introspectCollection(Collection collection, Map<Object, NonNativeObjectInfo> alreadyReadObjects, ODBType type,
			IntrospectionCallback callback) {

		if (collection == null) {
			return new CollectionObjectInfo();
		}

		// A collection that contain all meta representations of the collection
		// objects
		Collection<AbstractObjectInfo> collectionCopy = new ArrayList<AbstractObjectInfo>(collection.size());
		// A collection to keep references all all non native objects of the
		// collection
		// This will be used later to get all non native objects contained in an
		// object
		Collection<NonNativeObjectInfo> nonNativesObjects = new ArrayList<NonNativeObjectInfo>(collection.size());

		AbstractObjectInfo aoi = null;
		Iterator iterator = collection.iterator();

		while (iterator.hasNext()) {
			Object o = iterator.next();
			ClassInfo ci = null;
			// Null objects are not inserted in list
			if (o != null) {
				aoi = getObjectInfoInternal(o, alreadyReadObjects, callback);
				collectionCopy.add(aoi);
				if (aoi.isNonNativeObject()) {
					// This is a non native object
					nonNativesObjects.add((NonNativeObjectInfo) aoi);
				}
			}
		}

		CollectionObjectInfo coi = new CollectionObjectInfo(collectionCopy, nonNativesObjects);
		String realCollectionClassName = collection.getClass().getName();
		if (realCollectionClassName.indexOf("$") != -1) {
			coi.setRealCollectionClassName(type.getDefaultInstanciationClass().getName());
		} else {
			coi.setRealCollectionClassName(realCollectionClassName);
		}
		return coi;
	}

	/**
	 * Introspect a map
	 * 
	 * @param map
	 * @param alreadyReadObjects
	 * @param callback
	 * @return
	 */
	private MapObjectInfo introspectMap(Map map, Map<Object, NonNativeObjectInfo> alreadyReadObjects, IntrospectionCallback callback) {

		Map<AbstractObjectInfo, AbstractObjectInfo> mapCopy = new OdbHashMap<AbstractObjectInfo, AbstractObjectInfo>();

		// A collection to keep references all all non native objects of the
		// collection
		// This will be used later to get all non native objects contained in an
		// object
		Collection<NonNativeObjectInfo> nonNativeObjects = new ArrayList<NonNativeObjectInfo>(map.size() * 2);

		Collection keySet = map.keySet();
		Iterator keys = keySet.iterator();
		ClassInfo ciKey = null;
		ClassInfo ciValue = null;
		AbstractObjectInfo aoiForKey = null;
		AbstractObjectInfo aoiForValue = null;
		while (keys.hasNext()) {
			Object key = keys.next();
			Object value = map.get(key);

			if (key != null) {
				ciKey = getClassInfo(key.getClass().getName());
				if (value != null) {
					ciValue = getClassInfo(value.getClass().getName());
				}
				aoiForKey = getObjectInfoInternal(key, alreadyReadObjects, callback);
				aoiForValue = getObjectInfoInternal(value, alreadyReadObjects, callback);
				mapCopy.put(aoiForKey, aoiForValue);

				if (aoiForKey.isNonNativeObject()) {
					// This is a non native object
					nonNativeObjects.add((NonNativeObjectInfo) aoiForKey);
				}

				if (aoiForValue.isNonNativeObject()) {
					// This is a non native object
					nonNativeObjects.add((NonNativeObjectInfo) aoiForValue);
				}
			}
		}
		MapObjectInfo mapObjectInfo = new MapObjectInfo(mapCopy, map.getClass().getName());
		mapObjectInfo.setNonNativeObjects(nonNativeObjects);
		return mapObjectInfo;
	}

	private ClassInfo getClassInfo(String fullClassName) {
		return session.getClassInfo(fullClassName);
	}

	private ArrayObjectInfo introspectArray(Object array, Map<Object, NonNativeObjectInfo> alreadyReadObjects, ODBType valueType, IntrospectionCallback callback) {

		int length = OdbReflection.getArrayLength(array);
		Class elementType = array.getClass().getComponentType();
		ODBType type = ODBType.getFromClass(elementType);
		if (type.isAtomicNative()) {
			return intropectAtomicNativeArray(array, type);
		}
		AbstractObjectInfo[] arrayCopy = new AbstractObjectInfo[length];

		// This will be used later to get all non native objects contained in an
		// object
		Collection<NonNativeObjectInfo> nonNativeObjects = new ArrayList<NonNativeObjectInfo>(length);

		for (int i = 0; i < length; i++) {
			Object o = OdbReflection.getArrayElement(array, i);
			ClassInfo ci = null;
			if (o != null) {
				AbstractObjectInfo aoi = getObjectInfoInternal(o, alreadyReadObjects, callback);
				arrayCopy[i] = aoi;
				if (aoi.isNonNativeObject()) {
					// This is a non native object
					nonNativeObjects.add((NonNativeObjectInfo) aoi);
				}
			} else {
				arrayCopy[i] = new NonNativeNullObjectInfo();
				// This is a non native object
				nonNativeObjects.add((NonNativeObjectInfo) arrayCopy[i]);
			}
		}
		ArrayObjectInfo arrayOfAoi = new ArrayObjectInfo(arrayCopy, valueType, type.getId());
		arrayOfAoi.setNonNativeObjects(nonNativeObjects);
		return arrayOfAoi;
	}

	private ArrayObjectInfo intropectAtomicNativeArray(Object array, ODBType type) {

		int length = OdbReflection.getArrayLength(array);
		AtomicNativeObjectInfo anoi = null;
		AbstractObjectInfo[] arrayCopy = new AbstractObjectInfo[length];
		int typeId = 0;
		for (int i = 0; i < length; i++) {
			Object o = OdbReflection.getArrayElement(array, i);
			if (o != null) {
				// If object is not null, try to get the exact type
				typeId = ODBType.getFromClass(o.getClass()).getId();
				anoi = new AtomicNativeObjectInfo(o, typeId);
				arrayCopy[i] = anoi;
			} else {
				// Else take the declared type
				arrayCopy[i] = new NullNativeObjectInfo(type.getId());
			}
		}
		ArrayObjectInfo aoi = new ArrayObjectInfo(arrayCopy, ODBType.ARRAY, type.getId());
		return aoi;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.odb.core.introspector.IObjectIntrospector#clear()
	 */
	public void clear() {

	}

	public ClassIntrospector getClassIntrospector() {
		return classIntrospector;
	}

}
