
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
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.layers.layer4.IOFileParameter;
import org.neodatis.odb.core.query.values.ValuesCriteriaQuery;

/**
 * The Local ODB implementation.
 * @author osmadja
 *
 */
public class LocalODB extends ODBAdapter {
	
	public static LocalODB getInstance(String fileName, NeoDatisConfig config) {
		return new LocalODB(fileName,config);
	}

	 /**protected Constructor with user and password
	  * 
	  * @param fileName
	  * @param user
	  * @param password
	  * @throws Exception
	  */
	protected LocalODB(String fileName, NeoDatisConfig config) {
		super(config.getCoreProvider().getLocalSession(new IOFileParameter(fileName, true,config)));
	}

	
}
