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

import org.neodatis.odb.ObjectOid;


public class GetObjectFromOidMessage extends Message {
	private ObjectOid oid;
	protected int depth;
	
	public GetObjectFromOidMessage(){
		super();
	}
	public GetObjectFromOidMessage(String baseId, String connectionId, ObjectOid oid, int depth){
		super(MessageType.GET_OBJECT_FROM_ID, baseId,connectionId);
		this.oid = oid;
		this.depth = depth;
	}

	public ObjectOid getOid() {
		return oid;
	}
	public String toString() {
		return "GetObjectFromId";
	}
	public void setObjectOid(ObjectOid oid) {
		this.oid = oid;
	}
	public int getDepth() {
		return depth;
	}
	public void setDepth(int depth) {
		this.depth = depth;
	}


}
