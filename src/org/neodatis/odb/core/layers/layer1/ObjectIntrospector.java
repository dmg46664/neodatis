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

import org.neodatis.odb.core.layers.layer2.meta.AbstractObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.AttributeIdentification;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfo;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeObjectInfo;


/**
 * Interface for ObjectInstropector. It has local and Client/Server implementation.
 * @author osmadja
 *
 */
public interface ObjectIntrospector {

	/**
	 * retrieve object data
	 * 
	 * @param object The object to get meta representation
	 * @param alreadyReadObjects A map with already read object, to avoid cyclic reference problem
	 * @param callback The callback is used to keep tracks of found objects
	 * @return The object info
	 */
	public abstract NonNativeObjectInfo getMetaRepresentation(Object object, IntrospectionCallback callback);
	/**
	 * Same as getMetaRepresentation except it does not assume that resulting meta is non native
	 * @param object
	 * @param callback
	 * @return
	 */
	public abstract AbstractObjectInfo getGenericMetaRepresentation(Object object, IntrospectionCallback callback);

	public abstract NonNativeObjectInfo buildNnoi(Object object, ClassInfo classInfo, AbstractObjectInfo[] values,
			AttributeIdentification[] attributesIdentification);

	public abstract void clear();
	
	public ClassIntrospector getClassIntrospector();

}