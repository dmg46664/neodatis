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
package org.neodatis.odb.core.server;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLServerSocketFactory;

import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.server.connection.ClientServerConnection;
import org.neodatis.tool.DLogger;

/**
 * The SSL server implementation implementation for Server mode
 * 
 * @author osmadja
 * 
 */
public class ODBSSLServerImpl extends ODBServerImpl {
	public ODBSSLServerImpl(int port, NeoDatisConfig config) {
		super(port, config.setSSL(true).setMessageStreamerClass(SSLMessageStreamerImpl.class));
	}
	
	protected ServerSocket createSocketServer() throws IOException {
		ServerSocketFactory ssocketFactory = SSLServerSocketFactory.getDefault();
	    ServerSocket server = ssocketFactory.createServerSocket(getPort());

		return server;
	}
	public ClientServerConnection waitForRemoteConnection() throws IOException {
		try{
			return super.waitForRemoteConnection();
		}catch (SSLException e) {
			DLogger.error("NeoDatis: SSL Error : Be sure to set the following ssl properties to start the server java virtual machine : -Djavax.net.ssl.keyStore=<path to key store> -Djavax.net.ssl.keyStorePassword=<keystore password>");
			throw e;
		}
	}

}
