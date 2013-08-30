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
package org.neodatis.odb.core.server.connection;

import org.neodatis.odb.ODBServer;
import org.neodatis.odb.core.server.ServerSession;
import org.neodatis.odb.core.server.message.Message;

/**
 * A class to manage client server connections being executed in the same Vm. In
 * this case, we don't use network IO.
 * 
 * @author olivier s
 * 
 */
public class SameVmClientServerConnection extends ClientServerConnection {
	private static final String LOG_ID = "SameVmClientServerConnection";

	public SameVmClientServerConnection(String baseIdentifier, ODBServer server, boolean automaticallyCreateDatabase) {
		super(server);
	}

	public String getName() {
		return "Same vm client ";
	}

	public ServerSession getSession(String baseIdentifier) {
		throw new RuntimeException("Not yet implemented");
	}

	public Message manageMessage(Message message) {

		Message response = super.manageMessage(message);

		return response;
	}

	public void clearMessageStreamerCache() {
		// nothing to do
	}
}
