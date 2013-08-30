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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocketFactory;

import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.layers.layer3.DataConverter;
import org.neodatis.odb.core.layers.layer3.DataConverterImpl;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.BytesFactory;
import org.neodatis.odb.core.layers.layer3.ReadSize;
import org.neodatis.odb.core.server.message.Message;
import org.neodatis.odb.core.server.message.serialization.AllSerializers;
import org.neodatis.tool.DLogger;

/**
 * @sharpen.ignore
 * @author olivier
 * 
 */
public class SSLMessageStreamerImpl extends MessageStreamerImpl {

	
	
	public SSLMessageStreamerImpl() {
		super();
	}

	public SSLMessageStreamerImpl(Socket socket, NeoDatisConfig config)
			throws IOException {
		super(socket, config);
	}

	protected Socket createSocket() throws UnknownHostException, IOException{
		SocketFactory socketFactory = SSLSocketFactory.getDefault();
		Socket socket = socketFactory.createSocket(getHost(), getPort());
		return socket;
	}
	
	
	public Message read() throws Exception {
		try{
			return super.read();
		}catch (SSLHandshakeException e) {
			DLogger.error("NeoDatis: SSL Error : Be sure to set the following ssl properties to start the client java virtual machine : -Djavax.net.ssl.trustStore=<path to truststore> -Djavax.net.ssl.trustStorePassword=<trust store password>");
			throw e;
		}
	}
}
