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
package org.neodatis.odb.core.server.message;

import org.neodatis.odb.ClassOid;


/**
 * A StoreClassInfoMessageResponse is used by the Client/Server mode to answer a StoreClassInfoMessage
 * 
 * @author olivier s
 * 
 */
public class StoreClassInfoMessageResponse extends Message {
	private ClassOid coid;
	
	public StoreClassInfoMessageResponse() {
		super();
	}

	public StoreClassInfoMessageResponse(String baseId, String sessionid, String error) {
		super(MessageType.STORE_CLASS_INFO_RESPONSE, baseId,sessionid);
		setError(error);
	}
	public StoreClassInfoMessageResponse(String baseId, String sessionId, ClassOid oid) {
		super(MessageType.STORE_CLASS_INFO_RESPONSE, baseId,sessionId);
		this.coid = oid;
	}

	public ClassOid getCoid() {
		return coid;
	}

	public void setCoid(ClassOid coid) {
		this.coid = coid;
	}
	
}
