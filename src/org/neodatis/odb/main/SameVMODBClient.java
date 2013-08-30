
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
package org.neodatis.odb.main;

import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.ODBServer;


/**
 * The client implementation of ODB.
 * @author osmadja
 *
 */
public class SameVMODBClient extends ODBAdapter {

	/** TODO set the constructor as protected*/
	public SameVMODBClient(ODBServer server, String baseIdentifier) {
		super(null);//new SameVmClientEngine(server, baseIdentifier));
		throw new RuntimeException("Not yet implemented");
	}
	
	/**
	 * @param odbServerImpl
	 * @param baseIdentifier
	 * @param config
	 */
	public SameVMODBClient(ODBServer odbServer, String baseIdentifier, NeoDatisConfig config) {
		super(null);//new SameVmClientEngine(server, baseIdentifier));
		throw new RuntimeException("Not yet implemented");
	}

	public void close() {
		super.close();
	}
}
