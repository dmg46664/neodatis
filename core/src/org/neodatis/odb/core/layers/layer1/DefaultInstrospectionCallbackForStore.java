/**
 * 
 */
package org.neodatis.odb.core.layers.layer1;

import org.neodatis.odb.OID;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.context.ObjectReconnector;
import org.neodatis.odb.core.layers.layer2.meta.ObjectInfoHeader;
import org.neodatis.odb.core.session.Session;
import org.neodatis.odb.core.session.cross.CacheFactory;
import org.neodatis.odb.core.session.cross.ICrossSessionCache;
import org.neodatis.odb.core.trigger.TriggerManager;

/**
 * @author olivier
 * 
 */
public class DefaultInstrospectionCallbackForStore implements IntrospectionCallback {

	protected TriggerManager triggerManager;
	protected ICrossSessionCache crossSessionCache;
	protected Session session;
	protected ObjectReconnector objectReconnector;

	public DefaultInstrospectionCallbackForStore(Session session, TriggerManager triggerManager) {
		super();
		this.session = session;
		this.triggerManager = triggerManager;
		// Protection for junits
		if(session!=null){
			this.crossSessionCache = CacheFactory.getCrossSessionCache(session.getBaseIdentification().toString());
		}
		if(session.getConfig().reconnectObjectsToSession()){
			this.objectReconnector = new ObjectReconnector();
		}
	}

	public boolean objectFound(Object object, ObjectOid oid) {

		boolean isUpdate = !oid.isNew();
		
		//todo : remove this
		if (session.getConfig().reconnectObjectsToSession()) {
			isUpdate = checkIfObjectMustBeReconnected(object);
		}
		
		if (!isUpdate) {
			if (triggerManager != null) {
				triggerManager.manageInsertTriggerBefore(object.getClass().getName(), object);
			}
		}else{
			if (triggerManager != null) {
				triggerManager.manageUpdateTriggerBefore(object.getClass().getName(), null, object, null);
			}
		}
		if(objectReconnector!=null){
			// Check if we can attach some neodatis info to the object
			objectReconnector.tryToAttachNeoDatisContext(object, oid);
		}
		
		return true;
	}

	/**
	 * Used to check if object must be reconnected to current session
	 * 
	 * <pre>
	 * An object must be reconnected to session if OdbConfiguration.reconnectObjectsToSession() is true
	 * and object is not in local cache and is in cross session cache. In this case
	 * we had it to local cache
	 * 
	 * </pre>
	 * 
	 * @param object
	 */
	private boolean checkIfObjectMustBeReconnected(Object o) {
		if(session==null){
			// This protection is for JUnit
			return false;
		}
		
		// If object is in local cache, no need to reconnect it
		if(session.getCache().existObject(o)){
			return true;
		}
		OID oidCrossSession = crossSessionCache.getOid(o);
		//DLogger.info(String.format("Trying to reconnect object %s, type=%s, hc=%d, OID is null ? %s | hc-cache=%d",o.toString(),o.getClass().getName(), System.identityHashCode(o),oidCrossSession==null?"is null":"not null="+oidCrossSession.toString(),System.identityHashCode(crossSessionCache)));
		if(oidCrossSession!=null){
			// reconnect object
			ObjectInfoHeader oih = session.getObjectInfoHeaderFromOid(oidCrossSession,true);
			session.addObjectToCache(oidCrossSession, o, oih);
			return true;
		}else{
			//DLogger.info(String.format("Cache(hc=%d) is content:\n%s",System.identityHashCode(crossSessionCache), crossSessionCache.toString()));
			//boolean b = crossSessionCache.slowExistObject(o);
			//DLogger.info(String.format("b=%b",b));
		}
		return false;
	}

}
