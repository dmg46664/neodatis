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

package org.neodatis.odb.core;

import java.net.Socket;
import java.util.Map;

import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.Query;
import org.neodatis.odb.core.layers.layer1.ClassIntrospector;
import org.neodatis.odb.core.layers.layer1.IntrospectorFactory;
import org.neodatis.odb.core.layers.layer1.ObjectIntrospector;
import org.neodatis.odb.core.layers.layer2.instance.ClassPool;
import org.neodatis.odb.core.layers.layer2.instance.ClassPoolImpl;
import org.neodatis.odb.core.layers.layer2.instance.InstanceBuilder;
import org.neodatis.odb.core.layers.layer2.instance.InstanceBuilderImpl;
import org.neodatis.odb.core.layers.layer3.DataConverter;
import org.neodatis.odb.core.layers.layer3.DataConverterImpl;
import org.neodatis.odb.core.layers.layer4.BaseIdentification;
import org.neodatis.odb.core.layers.layer4.OidGenerator;
import org.neodatis.odb.core.query.IMatchingObjectAction;
import org.neodatis.odb.core.refactor.RefactorManager;
import org.neodatis.odb.core.refactor.RefactorManagerImpl;
import org.neodatis.odb.core.server.ClientSessionImpl;
import org.neodatis.odb.core.server.MessageStreamer;
import org.neodatis.odb.core.session.Session;
import org.neodatis.odb.core.session.SessionEngine;
import org.neodatis.odb.core.session.SessionImpl;
import org.neodatis.odb.core.trigger.TriggerManager;
import org.neodatis.odb.core.trigger.TriggerManagerImpl;
import org.neodatis.tool.wrappers.OdbSystem;
import org.neodatis.tool.wrappers.io.MessageStreamerBuilder;
import org.neodatis.tool.wrappers.map.OdbHashMap;

/**
 * The is the default implementation of ODB
 * 
 * @author olivier
 * 
 */
public class CoreProviderImpl implements CoreProvider {

	private ClassPool classPool;
	//private static ISessionManager sessionManager = new SessionManager();
	private static Map<Session, TriggerManager> triggerManagers = new OdbHashMap<Session, TriggerManager>();
	private static OidGenerator oidGenerator;

	protected NeoDatisConfig neoDatisConfig;
	
	public CoreProviderImpl(NeoDatisConfig config){
		this.neoDatisConfig = config;
		this.classPool = new ClassPoolImpl(neoDatisConfig);
	}
	
	
	private boolean osIsAndroid() {
		String javaVendor = OdbSystem.getProperty("java.vendor");
		if (javaVendor != null && javaVendor.equals("The Android Project")) {
			return true;
		}
		return false;
	}

	public void resetClassDefinitions() {
		//classIntrospector.reset();
		classPool.reset();

	}

	

	
	public DataConverter getByteArrayConverter(boolean debug, String characterEncoding, NeoDatisConfig config) {
		return new DataConverterImpl(debug,characterEncoding, config);
	}

	
	/**
	 * Returns the Local Instance Builder
	 */
	public InstanceBuilder getLocalInstanceBuilder(Session session, ClassIntrospector classIntrospector, TriggerManager triggerManager) {
		return new InstanceBuilderImpl(session, classIntrospector,triggerManager);
	}

	/**
	 * Returns the Server Instance Builder
	 */
	public InstanceBuilder getServerInstanceBuilder(Session session, ClassIntrospector classIntrospector, TriggerManager triggerManager) {
		return new InstanceBuilderImpl(session,classIntrospector,triggerManager);
	}

	public ObjectIntrospector getLocalObjectIntrospector(Session session, ClassIntrospector classIntrospector, OidGenerator oidGenerator) {
		return IntrospectorFactory.getObjectIntrospector(session, classIntrospector, oidGenerator);
	}

	public TriggerManager getLocalTriggerManager(Session session) {
		// First check if trigger manager has already been built for the engine
		TriggerManager triggerManager = triggerManagers.get(session);
		if(triggerManager!=null){
			return triggerManager;
		}
		triggerManager = new TriggerManagerImpl(session);
		triggerManagers.put(session, triggerManager);
		return triggerManager;
	}
	
	public void removeLocalTriggerManager(Session session) {
		triggerManagers.remove(session);
	}

	public ClassIntrospector getClassIntrospector(Session session, OidGenerator oidGenerator) {
		return IntrospectorFactory.getClassIntrospector(session, oidGenerator);
	}



	public RefactorManager getRefactorManager(SessionEngine engine) {
		return new RefactorManagerImpl(engine);
	}

	// For query result handler

	public IMatchingObjectAction getCollectionQueryResultAction(
			SessionEngine engine, Query query, boolean inMemory,
			boolean returnObjects) {
		throw new NeoDatisRuntimeException(NeoDatisError.NOT_YET_IMPLEMENTED);
		/*
		return new CollectionQueryResultAction(query, inMemory, engine,
				returnObjects, engine.getObjectReader().getInstanceBuilder());
				*/
	}

	public ClassPool getClassPool() {
		return classPool;
	}


	public OidGenerator getOidGenerator() {
		try{
			Class clazz = neoDatisConfig.getOidGeneratorClass();
			return (OidGenerator) clazz.newInstance();
		}catch (Exception e) {
			throw new NeoDatisRuntimeException(e);
		}
	}

	public void setoidGenerator(OidGenerator newoidGenerator) {
		this.oidGenerator = newoidGenerator;
	}

	public Session getLocalSession(BaseIdentification baseIdentification) {
		Session session = new SessionImpl(baseIdentification);
		return session;
	}
	public Session getClientSession(BaseIdentification baseIdentification) {
		Session session = new ClientSessionImpl(baseIdentification);
		return session;
	}

	/** (non-Javadoc)
	 * @see org.neodatis.odb.core.ICoreProvider#getMessageStreamer(java.net.Socket)
	 * 
	 */
	public MessageStreamer getMessageStreamer(Socket socket) {
		return MessageStreamerBuilder.getMessageStreamer(socket,neoDatisConfig);
	}
	/**
	 * 
	 */
	public MessageStreamer getMessageStreamer(String host, int port, String name) {
		return MessageStreamerBuilder.getMessageStreamer(host, port, name,neoDatisConfig);
	}


	public void init2() {
	}


	
}
