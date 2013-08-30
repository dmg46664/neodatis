
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
package org.neodatis.odb.core.server.trigger;

import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.ObjectRepresentation;
import org.neodatis.odb.core.session.ExecutionType;
import org.neodatis.odb.core.trigger.SelectTrigger;

public abstract class ServerSelectTrigger extends SelectTrigger{
	
    public void afterSelect(Object object, ObjectOid oid) {
    	if(object instanceof ObjectRepresentation){
    		afterSelect((ObjectRepresentation) object, oid);
    	}
		// there is a specific case (when using remote process invocation where server triggers may be called on user objects. This situation must be ignored. 
		// Check ExecutionType for more details

	}

	public abstract void afterSelect(ObjectRepresentation objectRepresentation,ObjectOid oid);
	
    @Override
    public int getExecutionType() {
    	return ExecutionType.SERVER;
    }

}
