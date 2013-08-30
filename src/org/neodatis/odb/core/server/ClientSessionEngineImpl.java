/**
 * 
 */
package org.neodatis.odb.core.server;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.OID;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.Objects;
import org.neodatis.odb.Query;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.layers.layer2.instance.InstanceBuilderContext;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfo;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfoList;
import org.neodatis.odb.core.layers.layer2.meta.MetaModel;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeDeletedObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeObjectInfo;
import org.neodatis.odb.core.layers.layer3.OidAndBytes;
import org.neodatis.odb.core.layers.layer4.IOSocketParameter;
import org.neodatis.odb.core.oid.sequential.CSSequentialOidGeneratorImpl;
import org.neodatis.odb.core.oid.sequential.SequentialOidGeneratorImpl;
import org.neodatis.odb.core.query.InternalQuery;
import org.neodatis.odb.core.query.criteria.CriteriaQuery;
import org.neodatis.odb.core.query.list.objects.LazySimpleListFromOid;
import org.neodatis.odb.core.query.list.objects.LazySimpleListOfAOI;
import org.neodatis.odb.core.query.list.objects.SimpleList;
import org.neodatis.odb.core.query.values.ValuesCriteriaQuery;
import org.neodatis.odb.core.server.message.AddIndexMessage;
import org.neodatis.odb.core.server.message.AddIndexMessageResponse;
import org.neodatis.odb.core.server.message.CloseMessage;
import org.neodatis.odb.core.server.message.CloseMessageResponse;
import org.neodatis.odb.core.server.message.CommitMessage;
import org.neodatis.odb.core.server.message.CommitMessageResponse;
import org.neodatis.odb.core.server.message.ConnectMessage;
import org.neodatis.odb.core.server.message.ConnectMessageResponse;
import org.neodatis.odb.core.server.message.CountMessage;
import org.neodatis.odb.core.server.message.CountMessageResponse;
import org.neodatis.odb.core.server.message.DeleteObjectMessage;
import org.neodatis.odb.core.server.message.DeleteObjectMessageResponse;
import org.neodatis.odb.core.server.message.GetObjectFromIdMessageResponse;
import org.neodatis.odb.core.server.message.GetObjectFromOidMessage;
import org.neodatis.odb.core.server.message.GetObjectsMessage;
import org.neodatis.odb.core.server.message.GetObjectsMessageResponse;
import org.neodatis.odb.core.server.message.Message;
import org.neodatis.odb.core.server.message.RollbackMessage;
import org.neodatis.odb.core.server.message.RollbackMessageResponse;
import org.neodatis.odb.core.server.message.StoreClassInfoMessage;
import org.neodatis.odb.core.server.message.StoreClassInfoMessageResponse;
import org.neodatis.odb.core.server.message.StoreObjectMessage;
import org.neodatis.odb.core.server.message.StoreObjectMessageResponse;
import org.neodatis.odb.core.session.Cache;
import org.neodatis.odb.core.session.Session;
import org.neodatis.odb.core.session.SessionEngineImpl;
import org.neodatis.odb.core.session.cross.CacheFactory;
import org.neodatis.odb.core.session.cross.ICrossSessionCache;
import org.neodatis.tool.DLogger;
import org.neodatis.tool.wrappers.list.IOdbList;
import org.neodatis.tool.wrappers.list.OdbArrayList;
import org.neodatis.tool.wrappers.net.NeoDatisIpAddress;

/**
 * @author olivier
 * 
 */
public class ClientSessionEngineImpl extends SessionEngineImpl {

	protected MessageStreamer messageStreamer;
	protected IOdbList<OidAndBytes> pendingOabsForMetaModelDefinition;
	
	/** this is used to process values sent from the server
	 * 
	 */
	protected List<ReturnValueProcessor> returnValueProcessors;

	/**
	 * @param session
	 */
	public ClientSessionEngineImpl(Session session) {
		super(session);
	}

	protected void initLayer4() {
		IOSocketParameter parameters = null;
		try {

			String localhost = null;

			localhost = NeoDatisIpAddress.get("localhost");
			parameters = (IOSocketParameter) session.getBaseIdentification();

			this.messageStreamer = session.getConfig().getCoreProvider().getMessageStreamer(parameters.getDestinationHost(),
					parameters.getPort(), parameters.getBaseId());

			ConnectMessage msg = new ConnectMessage(parameters.getBaseId(), session.getId(), localhost, parameters.getUserName(),
					parameters.getPassword(), parameters.getConfig().isTransactional());
			msg.setUserInfo(session.getConfig().getUserInfo());
			ConnectMessageResponse rmsg = (ConnectMessageResponse) messageStreamer.sendAndReceive(msg);

			if (rmsg.hasError()) {
				throw new NeoDatisRuntimeException(NeoDatisError.SERVER_SIDE_ERROR.addParameter(
						"Error while getting a connection from ODB Server").addParameter(rmsg.getError()));
			}
			// sets the oid generator class on the client. 
			String oidGeneratorClassName = rmsg.getOidGeneratorClassName();
			
			// If it is the seuqential id geneator just replace it with the ClientServer version
			if(oidGeneratorClassName.equals(SequentialOidGeneratorImpl.class.getName())){
				oidGeneratorClassName = CSSequentialOidGeneratorImpl.class.getName();
			}
			getSession().getConfig().setOidGeneratorClass(Class.forName(oidGeneratorClassName));
			pendingOabsForMetaModelDefinition = rmsg.getOabsOfMetaModel();

			session.setId(rmsg.getSessionId());

			storageEngine = new ClientStorageEngine(session, messageStreamer);
			
		} catch (Exception e) {
			String m = "While connecting to server ";
			if (parameters != null) {
				m = m + parameters.getDestinationHost();
			}
			throw new NeoDatisRuntimeException(e, m);
		}
	}

	protected List<ReturnValueProcessor> getReturnValuesProcessors(){
		if(this.returnValueProcessors==null){
			this.returnValueProcessors = new ArrayList<ReturnValueProcessor>();
			this.returnValueProcessors.add(new ChangedValueProcessor(session.getEngine().getObjectIntrospector().getClassIntrospector()));
		}
		return this.returnValueProcessors;
	}
	public OidAndBytes layer4ToLayer3(OID oid) {
		if (debug) {
			DLogger.debug("<start layer4 to layer3 oid=" + oid.oidToString() + ">");
		}

		OidAndBytes oab = storageEngine.read(oid, true);

		if (debug) {
			DLogger.debug("<end layer4 to layer3 oid=" + oid.oidToString() + ">");
		}

		return oab;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.neodatis.odb.core.session.SessionEngine#addClasses(org.neodatis.odb
	 * .core.layers.layer2.meta.ClassInfoList)
	 */
	public void storeClassInfos(ClassInfoList ciList) {
		Collection<ClassInfo> c = ciList.getClassInfos();
		IOdbList<OidAndBytes> oidsAndBytes = new OdbArrayList<OidAndBytes>();
		for (ClassInfo ci : c) {
			oidsAndBytes.addAll(layer3Writer.classInfoToBytes(ci));
		}
		layer3ToLayer4(oidsAndBytes);
	}

	public OID layer3ToLayer4(IOdbList<OidAndBytes> oidsAndBytes) {

		OID oid = oidsAndBytes.get(0).oid;
		try {
			if (oid instanceof ObjectOid) {
				// Remember that the client creates its own id: client ids.
				// These ids are sent to the server but the
				// server will create its own ids. The server ids are sent back
				// to the client then the client synchronize client and server
				// ids
				ObjectOid[] clientIds = new ObjectOid[oidsAndBytes.size()];
				for (int i = 0; i < clientIds.length; i++) {
					clientIds[i] = (ObjectOid) oidsAndBytes.get(i).oid;
				}
				StoreObjectMessage message = new StoreObjectMessage(session.getBaseIdentification().getBaseId(), session.getId(),
						oidsAndBytes, clientIds);
				StoreObjectMessageResponse rmsg = (StoreObjectMessageResponse) messageStreamer.sendAndReceive(message);

				if (rmsg.hasError()) {
					throw new NeoDatisRuntimeException(NeoDatisError.SERVER_SIDE_ERROR.addParameter("Error while storing object")
							.addParameter(rmsg.getError()));
				}
				ObjectOid mainOid = synchronizeIds(clientIds, rmsg.getServerIds());
				
				for(ReturnValue rv: rmsg.getReturnValues()){
					for(ReturnValueProcessor rvp: getReturnValuesProcessors()){
						try {
							rvp.process(rv, cache.getObjectWithOid(rv.getObjectOid()));
						} catch (Exception e) {
							throw new NeoDatisRuntimeException(NeoDatisError.ERROR_IN_RETURN_VALUE_PROCESSOR.addParameter(rvp.getClass().getName()).addParameter(rv.toString()),e);
						}
					}
				}
				
				return mainOid;
			}
			if (oid instanceof ClassOid) {
				StoreClassInfoMessage message = new StoreClassInfoMessage(session.getBaseIdentification().getBaseId(), session
						.getId(), oidsAndBytes);
				
				StoreClassInfoMessageResponse rmsg = (StoreClassInfoMessageResponse) messageStreamer.sendAndReceive(message);

				if (rmsg.hasError()) {
					throw new NeoDatisRuntimeException(NeoDatisError.SERVER_SIDE_ERROR.addParameter("Error while storing classinfo")
							.addParameter(rmsg.getError()));
				}
				return oidsAndBytes.get(0).oid;
			}
			throw new NeoDatisRuntimeException(NeoDatisError.INTERNAL_ERROR.addParameter("unknown Oid type"));

		} catch (Exception e) {
			throw new NeoDatisRuntimeException(e, "Layer3ToLayer4 on client side");
		}
	}

	/**
	 * @param clientIds
	 * @param serverIds
	 */
	private ObjectOid synchronizeIds(ObjectOid[] clientIds, ObjectOid[] serverIds) {
		if (clientIds.length != serverIds.length) {
			throw new NeoDatisRuntimeException(NeoDatisError.CLIENT_SERVER_SYNCHRONIZE_IDS.addParameter(serverIds.length).addParameter(
					clientIds.length));
		}
		Cache cache = getSession().getCache();
		ObjectOid clientOid = null;
		ObjectOid serverOid = null;
		Object o = null;
		ICrossSessionCache crossSessionCache = CacheFactory.getCrossSessionCache(session.getBaseIdentification().getBaseId());

		for (int i = 0; i < clientIds.length; i++) {
			clientOid = clientIds[i];
			serverOid = serverIds[i];

			// Server ids may be null when an object or part of an object has
			// been updated.
			// In these case local objects have already the correct ids
			if (serverOid != null && !serverOid.isNull() && (clientOid.isNew() || !(serverOid.equals(clientOid)))) {
				o = cache.getObjectWithOid(clientOid);
				cache.addObject(serverOid, o);

				if (session.getConfig().reconnectObjectsToSession()) {
					crossSessionCache.addObject(o, serverOid);
				}
			}
		}
		// return the main id : which is the first in the list
		// If serverIds[0]==null (because it was an update), just return the
		// clientIds[0]
		ObjectOid mainOid = serverIds[0];
		if (mainOid == null || mainOid.isNull()) {
			mainOid = clientIds[0];
		}

		return mainOid;
	}

	public void close() {
		super.close();

		CloseMessage msg = null;
		CloseMessageResponse rmsg = null;

		try {
			msg = new CloseMessage(session.getBaseIdentification().getBaseId(), session.getId());

			rmsg = (CloseMessageResponse) messageStreamer.sendAndReceive(msg);

			messageStreamer.close();

		} catch (Exception e) {
			throw new NeoDatisRuntimeException(e, "close on client side");
		}

		if (rmsg.hasError()) {
			throw new NeoDatisRuntimeException(NeoDatisError.SERVER_SIDE_ERROR.addParameter("Error while closing on server side")
					.addParameter(rmsg.getError()));
		}

	}

	public void commit() {
		CommitMessage msg = null;
		CommitMessageResponse rmsg = null;

		try {
			msg = new CommitMessage(session.getBaseIdentification().getBaseId(), session.getId());

			rmsg = (CommitMessageResponse) messageStreamer.sendAndReceive(msg);

		} catch (Exception e) {
			throw new NeoDatisRuntimeException(e, "commit on client side");
		}

		if (rmsg.hasError()) {
			throw new NeoDatisRuntimeException(NeoDatisError.SERVER_SIDE_ERROR.addParameter("Error while committing on server side")
					.addParameter(rmsg.getError()));
		}

	}

	public void rollback() {
		RollbackMessage msg = null;
		RollbackMessageResponse rmsg = null;

		try {
			msg = new RollbackMessage(session.getBaseIdentification().getBaseId(), session.getId());

			rmsg = (RollbackMessageResponse) messageStreamer.sendAndReceive(msg);

		} catch (Exception e) {
			throw new NeoDatisRuntimeException(e, "rollback on client side");
		}

		if (rmsg.hasError()) {
			throw new NeoDatisRuntimeException(NeoDatisError.SERVER_SIDE_ERROR.addParameter("Error while rollbacking on server side")
					.addParameter(rmsg.getError()));
		}

	}

	public <T> Objects<T> execute(InternalQuery query) {
		GetObjectsMessageResponse rmsg = null;

		if (query instanceof ValuesCriteriaQuery) {
			throw new NeoDatisRuntimeException(NeoDatisError.VALUES_QUERY_MUST_USE_GET_VALUES);
		}

		GetObjectsMessage msg = new GetObjectsMessage(session.getBaseIdentification().getBaseId(), session.getId(), query);
		msg.setInMemory(query.getQueryParameters().isInMemory());
		msg.setStartIndex(query.getQueryParameters().getStartIndex());
		msg.setEndIndex(query.getQueryParameters().getEndIndex());
		try {
			rmsg = (GetObjectsMessageResponse) messageStreamer.sendAndReceive(msg);
		} catch (Exception e) {
			throw new NeoDatisRuntimeException(e, "getObjects on client side");
		}
		if (rmsg.hasError()) {
			throw new NeoDatisRuntimeException(NeoDatisError.SERVER_SIDE_ERROR.addParameter("Error while getting objects on server side")
					.addParameter(rmsg.getError()));
		}

		query.setExecutionPlan(rmsg.getPlan());

		if (rmsg.isOnlyOids()) {
			return new LazySimpleListFromOid(this, true, rmsg.getObjectOids(), query.getQueryParameters().getLoadDepth());
		}

		Collection<IOdbList<OidAndBytes>> listOfOabs = rmsg.getListOfOabs();
		// check if we can use lazy instantiation
		if (session.getConfig().useLazyInstantiationInServerMode()) {
			Objects oos = new LazySimpleListOfAOI(instanceBuilder, true, new InstanceBuilderContext(query.getQueryParameters()
					.getLoadDepth()));
			for (IOdbList<OidAndBytes> oabs : listOfOabs) {
				oos.add(layer3ToLayer2(oabs, true, null, query.getQueryParameters().getLoadDepth()));
			}
			return oos;
		} else {
			Objects oos = new SimpleList();
			for (IOdbList<OidAndBytes> oabs : listOfOabs) {
				NonNativeObjectInfo nnoi = layer3ToLayer2(oabs, true, null, query.getQueryParameters().getLoadDepth());
				Object o = layer2ToLayer1(nnoi, new InstanceBuilderContext(true, null, query.getQueryParameters().getLoadDepth()));
				oos.add(o);
			}
			return oos;
		}
	}

	@Override
	public NonNativeObjectInfo getMetaObjectFromOid(ObjectOid oid, boolean throwExceptionIfNotExist, InstanceBuilderContext context) {

		GetObjectFromIdMessageResponse rmsg = null;

		GetObjectFromOidMessage msg = new GetObjectFromOidMessage(session.getBaseIdentification().getBaseId(), session.getId(),
				oid, context.depth);
		try {
			rmsg = (GetObjectFromIdMessageResponse) messageStreamer.sendAndReceive(msg);
		} catch (Exception e) {
			throw new NeoDatisRuntimeException(e, "getObjectFromId on client side");
		}
		if (rmsg.hasError()) {
			throw new NeoDatisRuntimeException(NeoDatisError.SERVER_SIDE_ERROR.addParameter(
					"Error while getting object from id on server side").addParameter(rmsg.getError()));
		}

		if (rmsg.getOabs() == null) {
			if (throwExceptionIfNotExist) {
				throw new NeoDatisRuntimeException(NeoDatisError.OBJECT_WITH_OID_DOES_NOT_EXIST.addParameter(oid.oidToString()));
			}
			return new NonNativeDeletedObjectInfo(oid);
		}

		NonNativeObjectInfo nnoi = layer3ToLayer2(rmsg.getOabs(), true, null, context.depth);

		return nnoi;
	}

	@Override
	public <T> Objects<T> getMetaObjects(InternalQuery query) {
		GetObjectsMessageResponse rmsg = null;

		if (query instanceof ValuesCriteriaQuery) {
			throw new NeoDatisRuntimeException(NeoDatisError.VALUES_QUERY_MUST_USE_GET_VALUES);
		}

		GetObjectsMessage msg = new GetObjectsMessage(session.getBaseIdentification().getBaseId(), session.getId(), query);
		msg.setInMemory(query.getQueryParameters().isInMemory());
		msg.setStartIndex(query.getQueryParameters().getStartIndex());
		msg.setEndIndex(query.getQueryParameters().getEndIndex());
		try {
			rmsg = (GetObjectsMessageResponse) messageStreamer.sendAndReceive(msg);
		} catch (Exception e) {
			throw new NeoDatisRuntimeException(e, "getObjects on client side");
		}
		if (rmsg.hasError()) {
			throw new NeoDatisRuntimeException(NeoDatisError.SERVER_SIDE_ERROR.addParameter("Error while getting objects on server side")
					.addParameter(rmsg.getError()));
		}

		query.setExecutionPlan(rmsg.getPlan());

		if (rmsg.isOnlyOids()) {
			return new LazySimpleListFromOid(this, false, rmsg.getObjectOids(), query.getQueryParameters().getLoadDepth());
		}
		Collection<IOdbList<OidAndBytes>> listOfOabs = rmsg.getListOfOabs();

		Objects oos = new LazySimpleListOfAOI(instanceBuilder, false, new InstanceBuilderContext(query.getQueryParameters().getLoadDepth()));
		for (IOdbList<OidAndBytes> oabs : listOfOabs) {
			oos.add(layer3ToLayer2(oabs, true, null, query.getQueryParameters().getLoadDepth()));
		}
		return oos;
	}

	public MetaModel loadMetaModel(MetaModel metaModel) {
		// first pre-load class infos
		for (OidAndBytes oab : pendingOabsForMetaModelDefinition) {
			ClassInfo ci = classInfoFromBytes(oab, false);
			metaModel.addClass(ci, false);
		}
		// Then load full definition, As some attribute may reference Class
		// info, we need to have all class info in the meta model before loading
		// attributes
		for (OidAndBytes oab : pendingOabsForMetaModelDefinition) {
			ClassInfo ci = classInfoFromBytes(oab, true);
			metaModel.getClassInfoFromId(ci.getOid()).setAttributes(ci.getAttributes());
		}
		session.setMetaModel(metaModel);
		return metaModel;

	}

	protected void internalDeleteObjectWithOid(ObjectOid oid, boolean cascade, boolean callTriggerAndIndex) {
		DeleteObjectMessage msg = new DeleteObjectMessage(session.getBaseIdentification().getBaseId(), session.getId(), oid,
				cascade);
		DeleteObjectMessageResponse rmsg = null;

		try {
			rmsg = (DeleteObjectMessageResponse) messageStreamer.sendAndReceive(msg);
		} catch (Exception e) {
			throw new NeoDatisRuntimeException(e, "Layer3ToLayer4 on client side");
		}
		if (rmsg.hasError()) {
			throw new NeoDatisRuntimeException(NeoDatisError.SERVER_SIDE_ERROR.addParameter(
					"Error while deleting object with OID " + oid.oidToString()).addParameter(rmsg.getError()));
		}

		cache.remove(oid);

		if (callTriggerAndIndex) {
			ClassInfo ci = session.getMetaModel().getClassInfoFromId(oid.getClassOid());
			boolean withIndex = !ci.getIndexes().isEmpty();

			if (withIndex) {
				NonNativeObjectInfo nnoi = getMetaObjectFromOid(oid, true, new InstanceBuilderContext(true, null, 1));
				indexManager.manageIndexesForDelete(oid, nnoi);
			}
			triggerManager.manageDeleteTriggerAfter(session.getMetaModel().getClassInfoFromId(oid.getClassOid()).getFullClassName(), null,
					oid);
		}

	}

	public BigInteger count(Query query) {
		checkClose();

		if (!(query instanceof CriteriaQuery)) {
			throw new NeoDatisRuntimeException("count only works with Criteria queries for instance :-(");
		}
		CountMessage msg = new CountMessage(session.getBaseIdentification().getBaseId(), session.getId(), (CriteriaQuery) query);
		CountMessageResponse rmsg = null;

		try {
			rmsg = (CountMessageResponse) messageStreamer.sendAndReceive(msg);
		} catch (Exception e) {
			throw new NeoDatisRuntimeException(e, "count on client side");
		}
		if (rmsg.hasError()) {
			throw new NeoDatisRuntimeException(NeoDatisError.SERVER_SIDE_ERROR.addParameter("Error while count objects").addParameter(
					rmsg.getError()));
		}

		return rmsg.getNbObjects();
	}

	/**
	 * Adds an index to a class
	 * 
	 */
	public void addIndexOn(String className, String indexName, String[] indexFields, boolean verbose, boolean acceptMultipleValuesForSameKey) {
		checkClose();

		AddIndexMessage msg = new AddIndexMessage(session.getBaseIdentification().getBaseId(), session.getId(), className,
				indexName, indexFields, acceptMultipleValuesForSameKey, verbose);
		AddIndexMessageResponse rmsg = null;

		try {
			rmsg = (AddIndexMessageResponse) messageStreamer.sendAndReceive(msg);
		} catch (Exception e) {
			throw new NeoDatisRuntimeException(e, "addIndex on client side");
		}
		if (rmsg.hasError()) {
			throw new NeoDatisRuntimeException(NeoDatisError.SERVER_SIDE_ERROR.addParameter("Error while adding index " + indexName)
					.addParameter(rmsg.getError()));
		}
	}

	public Message sendMessage(Message msg) {
		checkClose();

		Message rmsg = null;

		if(msg.getBaseIdentifier()==null){
			msg.setBaseIdentifier(session.getBaseIdentification().getBaseId());
			msg.setSessionId(session.getId());
		}
		try {
			rmsg = messageStreamer.sendAndReceive(msg);
			
		} catch (Exception e) {
			throw new NeoDatisRuntimeException(e, "sendMessage on client side");
		}
		if (rmsg.hasError()) {
			throw new NeoDatisRuntimeException(NeoDatisError.SERVER_SIDE_ERROR.addParameter("Error while sending message " + msg.getClass().getName())
					.addParameter(rmsg.getError()));
		}
		return rmsg;
	}

}
