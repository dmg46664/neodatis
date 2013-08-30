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
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.layers.layer2.instance.ClassPool;
import org.neodatis.tool.wrappers.list.IOdbList;
import org.neodatis.tool.wrappers.list.OdbArrayList;
import org.neodatis.tool.wrappers.map.OdbHashMap;

/**
 * The database meta-model
 * 
 * @author olivier s
 * 
 */
public abstract class MetaModel implements Serializable {

	/** A hash map to speed up the access of classinfo by full class name */
	private Map<String,ClassInfo> rapidAccessForUserClassesByName;

	private Map <String,ClassInfo>rapidAccessForSystemClassesByName;
	
	private Map <ClassOid,ClassInfo>rapidAccessForClassesByOid;

	/** A simple list to hold all class infos. It is redundant with the maps, but in some cases, we need sequential access to classes :-(*/
	private IOdbList<ClassInfo> allClassInfos;
	
	/** to identify if meta model has changed */
	private boolean hasChanged;
	
	protected transient ClassPool classPool;
	protected NeoDatisConfig neoDatisConfig;

	public MetaModel(NeoDatisConfig config) {
		this.neoDatisConfig = config;
		this.classPool = neoDatisConfig.getCoreProvider().getClassPool();
		rapidAccessForUserClassesByName = new OdbHashMap<String, ClassInfo>(10);
		rapidAccessForSystemClassesByName = new OdbHashMap<String, ClassInfo>(10);
		rapidAccessForClassesByOid = new OdbHashMap<ClassOid, ClassInfo>(10);
		allClassInfos = new OdbArrayList<ClassInfo>();
	}

	public void addClass(ClassInfo classInfo, boolean forceAdd) {
		boolean existClass = existClass(classInfo.getFullClassName());
		if(!forceAdd && existClass){
			return;
		}
		
		
		if(existClass){
			allClassInfos.remove(classInfo);
		}
		if (classInfo.isSystemClass()) {
			rapidAccessForSystemClassesByName.put(classInfo.getFullClassName(), classInfo);
		} else {
			rapidAccessForUserClassesByName.put(classInfo.getFullClassName(), classInfo);
		}
		rapidAccessForClassesByOid.put(classInfo.getOid(), classInfo);
		allClassInfos.add(classInfo);
	}

	public void addClasses(ClassInfoList ciList) {
		Iterator iterator = ciList.getClassInfos().iterator();

		while (iterator.hasNext()) {
			addClass((ClassInfo) iterator.next(), false);
		}
	}
	
	public boolean existClass(String fullClassName) {
		// Check if it is a system class
		boolean exist = rapidAccessForSystemClassesByName.containsKey(fullClassName);

		if (exist) {
			return true;
		}
		// Check if it is user class
		exist = rapidAccessForUserClassesByName.containsKey(fullClassName);
		return exist;
	}

	public String toString() {
		return rapidAccessForUserClassesByName.values() + "/" + rapidAccessForSystemClassesByName.values();
	}

	public IOdbList<ClassInfo> getAllClasses() {
		return allClassInfos;
	}

	public Collection<ClassInfo> getUserClasses() {
		return rapidAccessForUserClassesByName.values();
	}

	public Collection<ClassInfo> getSystemClasses() {
		return rapidAccessForSystemClassesByName.values();
	}

	public int getNumberOfClasses() {
		return allClassInfos.size();
	}

	public int getNumberOfUserClasses() {
		return rapidAccessForUserClassesByName.size();
	}

	public int getNumberOfSystemClasses() {
		return rapidAccessForSystemClassesByName.size();
	}

	/**
	 * Gets the class info from the OID. 
	 * 
	 * @param id
	 * @return the class info with the OID
	 */
	public ClassInfo getClassInfoFromId(ClassOid coid) {
		return rapidAccessForClassesByOid.get(coid);
	}

	public ClassInfo getClassInfo(String fullClassName, boolean throwExceptionIfDoesNotExist) {
		// Check if it is a system class
		ClassInfo ci = rapidAccessForSystemClassesByName.get(fullClassName);
		if (ci != null) {
			return ci;
		}
		// Check if it is user class
		ci = rapidAccessForUserClassesByName.get(fullClassName);
		if (ci != null) {
			return ci;
		}
		if (throwExceptionIfDoesNotExist) {
			throw new NeoDatisRuntimeException(NeoDatisError.META_MODEL_CLASS_NAME_DOES_NOT_EXIST.addParameter(fullClassName));
		}
		return null;
	}

	/**
	 * 
	 * 
	 * @return The Last class info
	 */
	public ClassInfo getLastClassInfo() {
		return allClassInfos.get(allClassInfos.size()-1);
	}

	/**
	 * This method is only used by the odb explorer. So there is no too much
	 * problem with performance issue.
	 * 
	 * @param ci
	 * @return The index of the class info
	 */
	public int slowGetUserClassInfoIndex(ClassInfo ci) {
		Iterator iterator = rapidAccessForUserClassesByName.values().iterator();
		int i=0;
		ClassInfo ci2 = null;
		while(iterator.hasNext()){
			ci2 = (ClassInfo) iterator.next();
			if(ci2.getOid()==ci.getOid()){
				return i;
			}
			i++;
		}
		throw new NeoDatisRuntimeException(NeoDatisError.CLASS_INFO_DOES_NOT_EXIST_IN_META_MODEL.addParameter(ci.getFullClassName()));
	}

	/**
	 * @param index The index of the class info to get
	 * @return The class info at the specified index
	 */
	public ClassInfo getClassInfo(int index) {
		return allClassInfos.get(index);
	}
	
	/**
	 * The method is slow nut it is only used in the odb explorer.
	 * @param index
	 * @return
	 */
	public ClassInfo slowGetUserClassInfo(int index) {
		Iterator iterator = rapidAccessForUserClassesByName.values().iterator();
		int i=0;
		ClassInfo ci = null;
		while(iterator.hasNext()){
			ci = (ClassInfo) iterator.next();
			if(i==index){
				return ci;
			}
			i++;
		}
		throw new NeoDatisRuntimeException(NeoDatisError.CLASS_INFO_DOES_NOT_EXIST_IN_META_MODEL.addParameter(" with index "+index));
	}

	public void clear() {
		rapidAccessForSystemClassesByName.clear();
		rapidAccessForUserClassesByName.clear();
		rapidAccessForSystemClassesByName = null;
		rapidAccessForUserClassesByName = null;
		allClassInfos.clear();
	}

	public boolean hasChanged() {
		return hasChanged;
	}

	public void setHasChanged(boolean hasChanged) {
		this.hasChanged = hasChanged;
	}

	public abstract Collection<ClassInfo> getChangedClassInfo();

	public abstract void resetChangedClasses();
	
	/**
	 * Saves the fact that something has changed in the class (number of
	 * objects or last object oid)
	 * @param ci
	 */
	public abstract void addChangedClass(ClassInfo ci);
	

	/** Builds a meta model from a list of class infos
	 * 
	 * @param classInfos
	 * @return The new Metamodel
	 */
	public static MetaModel fromClassInfos(IOdbList<ClassInfo> classInfos, NeoDatisConfig neoDatisConfig){
		MetaModel metaModel = new MetaModelImpl(neoDatisConfig);
		int nbClasses = classInfos.size();
		
		for(int i=0;i<nbClasses;i++){
			metaModel.addClass(classInfos.get(i), false);
		}
		return metaModel;
	}
	
	/**
	 * Gets all the persistent classes that are subclasses or equal to the identification class
	 * @param fullClassName
	 * @return The list of class info of persistent classes that are subclasses or equal to the class
	 */
	public IOdbList<ClassInfo> getPersistentSubclassesOf(String fullClassName){
		IOdbList<ClassInfo> result = new OdbArrayList<ClassInfo>();
		
		Iterator<String> classNames = rapidAccessForUserClassesByName.keySet().iterator();
		String oneClassName = null;
		Class theClass = classPool.getClass(fullClassName);
		Class oneClass = null;
		while(classNames.hasNext()){
			oneClassName = classNames.next();
			if(oneClassName.equals(fullClassName)){
				result.add(getClassInfo(oneClassName, true));
			}else{
				oneClass = classPool.getClass(oneClassName);
				if(theClass.isAssignableFrom(oneClass)){
					result.add(getClassInfo(oneClassName, true));
				}
			}
		}
		
		return result;
	}

	/**
	 * @return
	 */
	public abstract MetaModel duplicate();
	
	/**
	 * To detect if a class has cyclic reference
	 * 
	 * @return true if this class info has cyclic references
	 */
	public boolean hasCyclicReference(ClassInfo ci) {
		return hasCyclicReference(ci, new OdbHashMap<String, ClassInfo>());
	}

	/**
	 * To detect if a class has cyclic reference
	 * 
	 * @param alreadyVisitedClasses
	 *            A hashmap containing all the already visited classes
	 * @return true if this class info has cyclic references
	 */
	private boolean hasCyclicReference(ClassInfo ci, Map<String, ClassInfo> alreadyVisitedClasses) {
		ClassAttributeInfo cai = null;
		boolean hasCyclicRef = false;
		String fullClassName = ci.getFullClassName();
		if (alreadyVisitedClasses.get(fullClassName) != null) {
			return true;
		}
		Map<String, ClassInfo> localMap = new OdbHashMap<String, ClassInfo>();
		alreadyVisitedClasses.put(fullClassName, ci);
		for (int i = 0; i < ci.getAttributes().size(); i++) {
			cai = ci.getAttributeInfo(i);
			if (!cai.isNative()) {
				localMap = new OdbHashMap<String, ClassInfo>(alreadyVisitedClasses);
				hasCyclicRef = hasCyclicReference(this.getClassInfoFromId(cai.getAttributeClassOid()),localMap);
				if (hasCyclicRef) {
					return true;
				}
			}
		}
		return false;
	}
}
