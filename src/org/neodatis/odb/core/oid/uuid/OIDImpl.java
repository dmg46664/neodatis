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
package org.neodatis.odb.core.oid.uuid;

import java.util.UUID;

import org.neodatis.odb.OID;

public abstract class OIDImpl implements OID {
 	protected boolean isNew;
	
	public OIDImpl() {
		super();
	}

	public boolean isNew() {
		return isNew;
	}

	public void setIsNew(boolean isNew) {
		this.isNew = isNew;
	}
	
	public int compareTo(Object o) {
		return oidToString().compareTo(o.toString());
	}
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof OID)){
			return false;
		}
		OID oid = (OID) obj;
		return oid.oidToString().equals(oidToString());
	}
	public abstract String oidToString();
}
