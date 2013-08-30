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

import org.neodatis.odb.core.layers.layer3.OidAndBytes;
import org.neodatis.tool.wrappers.list.IOdbList;

/** A StoreMessage is used by the Client/Server mode to store an object
 * 
 * @author olivier s
 *
 */
public class StoreClassInfoMessage extends Message {
	private IOdbList<OidAndBytes> oabs;
	
	public StoreClassInfoMessage(){
		super();
	}
	public StoreClassInfoMessage(String baseId, String sessionId,IOdbList<OidAndBytes> oabs){
		super(MessageType.STORE_CLASS_INFO, baseId,sessionId);
		this.oabs = oabs;
	}

	public String toString() {
		return "StoreClassInfo";
	}
	/**
	 * @param oabs
	 */
	public void setOabs(IOdbList<OidAndBytes> oabs) {
		this.oabs = oabs;
		
	}
	public IOdbList<OidAndBytes> getOabs() {
		return oabs;
	}
}
