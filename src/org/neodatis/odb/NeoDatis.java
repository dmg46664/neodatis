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
package org.neodatis.odb;

import org.neodatis.odb.core.config.NeoDatisConfigImpl;
import org.neodatis.odb.core.server.ODBSSLServerImpl;
import org.neodatis.odb.core.server.ODBServerImpl;
import org.neodatis.odb.core.server.SSLMessageStreamerImpl;
import org.neodatis.odb.main.LocalODB;
import org.neodatis.odb.main.RemoteODBClient;

/**
 * The NeoDatis main entry point class. This class allows to open a database (locally and remotely, open a server (secure or not secure) and get NeoDatis config instances
 * 
 * @author osmadja
 * 
 */
public class NeoDatis {

	/**
	 * A private constructor to avoid instantiation
	 * 
	 * 
	 */
	private NeoDatis() {
	}

	/**
	 * Open an ODB database protected by a user and password
	 * 
	 * @param fileName
	 *            The name of the ODB database
	 * @param user
	 *            The user of the database
	 * @param password
	 *            The password of the user
	 * @return The ODB database
	 */
	public static ODB open(String fileName, String user, String password) {
		NeoDatisConfig config = NeoDatis.getConfig();
		if (user != null && password != null) {
			config.setUser(user);
			config.setPassword(password);
		}
		ODB odBase = LocalODB.getInstance(fileName, config);
		return odBase;
	}

	/**
	 * Opens a local database with user, password and config
	 * @param fileName
	 * @param user
	 * @param password
	 * @param properties
	 * @return
	 */
	public static ODB open(String fileName, String user, String password, NeoDatisConfig config) {
		if (user != null && password != null) {
			config.setUser(user);
			config.setPassword(password);
		}

		ODB odBase = LocalODB.getInstance(fileName, config);
		return odBase;
	}

	/**
	 * Open a non password protected ODB database
	 * 
	 * @param fileName
	 *            The ODB database name
	 * @return A local ODB implementation
	 */
	public static ODB open(String fileName) {
		ODB odBase = LocalODB.getInstance(fileName, NeoDatis.getConfig());
		return odBase;
	}

	/**
	 * Opens a local database
	 * @param fileName
	 * @param config The database config
	 * @return
	 */
	public static ODB open(String fileName, NeoDatisConfig config) {
		ODB odBase = LocalODB.getInstance(fileName, config);
		return odBase;
	}

	/**
	 * Open an ODB server on the specific port. This will the socketServer on
	 * the specified port. Must call startServer of the ODBServer to actually
	 * start the server
	 * 
	 * @param port
	 *            The server port
	 * @return The server
	 */
	public static ODBServer openServer(int port) {
		return new ODBServerImpl(port,NeoDatis.getConfig());
	}

	/**
	 * Open an ODB server on the specific port. This will the socketServer on
	 * the specified port. Must call startServer of the ODBServer to actually
	 * start the server
	 * 
	 * @param port
	 *            The server port
	 * @param config The server config
	 * @return The server
	 */
	public static ODBServer openServer(int port, NeoDatisConfig config) {
		return new ODBServerImpl(port,config);
	}
	
	/**
	 * Open a secure (ssl) ODB server on the specific port. This will create the sslsocketServer on
	 * the specified port. Must call startServer of the ODBServer to actually
	 * start the server
	 * 
	 * @param port
	 *            The server port
	 * @return The server
	 */
	public static ODBServer openSSLServer(int port) {
		return new ODBSSLServerImpl(port, NeoDatis.getConfig());
	}

	/**
	 * Open a secure (ssl) ODB server on the specific port. This will the socketServer on
	 * the specified port. Must call startServer of the ODBServer to actually
	 * start the server
	 * 
	 * @param port
	 *            The server port
	 * @param config The server config
	 * @return The server
	 */
	public static ODBServer openSSLServer(int port, NeoDatisConfig config) {
		return new ODBSSLServerImpl(port,config);
	}
	/**
	 * Open a neodatis Client connecting to the specified server
	 * 
	 * @param hostName
	 * @param port
	 * @param baseIdentifier
	 *            The base identifier : The alias used by the server to declare
	 *            database
	 * @return The ODB
	 */
	public static ODB openClient(String hostName, int port, String baseIdentifier) {
		NeoDatisConfig config = NeoDatis.getConfig().setHostAndPort(hostName, port);
		return new RemoteODBClient(baseIdentifier,config);
	}

	/**
	 * Open a neodatis Client connecting to the server specified in the config.host and config port 
	 * 
	 * @param baseIdentifier
	 * @param config The Neodatis config, must have host and port set
	 * @return The ODB
	 */
	public static ODB openClient(String baseIdentifier, NeoDatisConfig config) {
		try{
			return new RemoteODBClient(baseIdentifier, config);
		}catch (NeoDatisRuntimeException e) {
			throw e;
		}
	}

	/**
	 * Opens a secure client (using SSL) to the specified server. Check documentation to see how to specify SSL certificates
	 * 
	 * @param hostName
	 * @param port
	 * @param baseIdentifier
	 *            The base identifier : The alias used by the server to declare
	 *            database
	 * @return The ODB
	 */
	public static ODB openSSLClient(String hostName, int port, String baseIdentifier) {
		NeoDatisConfig config = NeoDatis.getConfig().setHostAndPort(hostName, port);
		config.setMessageStreamerClass(SSLMessageStreamerImpl.class);
		return new RemoteODBClient(baseIdentifier,config);
	}

	/**
	 * Opens a secure client (using SSL) to the specified server(defined in config). Check documentation to see how to specify SSL certificates
	 * 
	 * @param baseIdentifier
	 * @param config The Neodatis config, must have host and port set
	 * @return The ODB
	 */
	public static ODB openSSLClient(String baseIdentifier, NeoDatisConfig config) {
		config.setMessageStreamerClass(SSLMessageStreamerImpl.class);
		return new RemoteODBClient(baseIdentifier, config);
	}
	/**
	 * Return a neoDatis Config instance with default values
	 * 
	 * @return
	 */
	public static NeoDatisConfig getConfig() {
		return new NeoDatisConfigImpl(true);
	}

	/**
	 * Returns the global NeoDatis Config. This object is a static configuration of NeoDatis and holds for all NeoDatis instances of the current JVM
	 * 
	 * @return
	 */
	public static NeoDatisConfig getGlobalConfig() {
		return NeoDatisGlobalConfig.get();
	}

	/**
	 * Returns a NeoDatisConfig instance with with values loaded from
	 * neodatis.properties. Values that are not defined in the file are default
	 * values
	 * 
	 * @return
	 * @throws Exception 
	 */
	public static NeoDatisConfig getConfigFromFile() throws Exception {
		return getConfigFromFile("neodatis.properties");
	}

	/**
	 * Returns a NeoDatisConfig instance with with values loaded from
	 * the file specified as parameter. Values that are not defined in the file are default
	 * values
	 * @param The file name to load properties from
	 * @return
	 * @throws Exception 
	 */
	public static NeoDatisConfig getConfigFromFile(String fileName) throws Exception {
		NeoDatisConfigImpl config = new NeoDatisConfigImpl(true);
		config.updateFromFile(fileName);
		return config;
	}
	
	public static String version(){
		return Release.RELEASE_NUMBER + " ("+Release.RELEASE_DATE+")";
	}
}
