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

import java.math.BigInteger;

/**
 * A response to a CountMessage command
 * @author olivier s
 *
 */

public class CountMessageResponse extends Message {
	private BigInteger nbObjects;
	
	public CountMessageResponse(){
		super();
	}
	public CountMessageResponse(String baseId, String connectionId, String error){
		super(MessageType.COUNT_RESPONSE, baseId,connectionId);
		setError(error);
	}

	public CountMessageResponse(String baseId, String connectionId, BigInteger nbObjects){
		super(MessageType.COUNT_RESPONSE, baseId,connectionId);
		this.nbObjects = nbObjects;
	}

	public BigInteger getNbObjects() {
		return nbObjects;
	}
	public void setNbObjects(BigInteger n){
		this.nbObjects = n;
	}

}
