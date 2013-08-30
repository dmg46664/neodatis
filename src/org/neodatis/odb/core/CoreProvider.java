package org.neodatis.odb.core;

import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.OID;
import org.neodatis.odb.Query;
import org.neodatis.odb.core.layers.layer1.ClassIntrospector;
import org.neodatis.odb.core.layers.layer1.ObjectIntrospector;
import org.neodatis.odb.core.layers.layer2.instance.ClassPool;
import org.neodatis.odb.core.layers.layer2.instance.InstanceBuilder;
import org.neodatis.odb.core.layers.layer3.DataConverter;
import org.neodatis.odb.core.layers.layer4.BaseIdentification;
import org.neodatis.odb.core.layers.layer4.OidGenerator;
import org.neodatis.odb.core.query.IMatchingObjectAction;
import org.neodatis.odb.core.refactor.RefactorManager;
import org.neodatis.odb.core.server.MessageStreamer;
import org.neodatis.odb.core.session.Session;
import org.neodatis.odb.core.session.SessionEngine;
import org.neodatis.odb.core.trigger.TriggerManager;

/**
 * This is the default Core Object Provider.
 * 
 * 
 * @author olivier
 *
 */
public interface CoreProvider extends ITwoPhaseInit {
	
	DataConverter getByteArrayConverter(boolean debug, String characterEncoding, NeoDatisConfig config);
	
	/**
	 * Returns the Local Instance Builder
	 */
	public InstanceBuilder getLocalInstanceBuilder(Session session, ClassIntrospector classIntrospector, TriggerManager triggerManager);
	public InstanceBuilder getServerInstanceBuilder(Session session, ClassIntrospector classIntrospector, TriggerManager triggerManager);
	
	public ObjectIntrospector getLocalObjectIntrospector(Session session, ClassIntrospector classIntrospector, OidGenerator oidGenerator);
	public TriggerManager getLocalTriggerManager(Session session);
	public ClassIntrospector getClassIntrospector(Session session, OidGenerator oidGenerator);
	
	public RefactorManager getRefactorManager(SessionEngine engine);
	
	// For query result handler
	
	/** Returns the query result handler for normal query result (that return a collection of objects)
	 * 
	 */
	public IMatchingObjectAction getCollectionQueryResultAction(SessionEngine engine, Query query,boolean inMemory, boolean returnObjects);
	
	public ClassPool getClassPool();
	
	public void resetClassDefinitions();
	public void removeLocalTriggerManager(Session session);
	/**
	 * @param fileParameter
	 * @return
	 */
	Session getLocalSession(BaseIdentification baseIdentification);
	Session getClientSession(BaseIdentification baseIdentification);

	/**
	 * @param host
	 * @param port
	 * @param identification
	 * @return
	 */
	MessageStreamer getMessageStreamer(String host, int port, String identification);

	OidGenerator getOidGenerator();

}
