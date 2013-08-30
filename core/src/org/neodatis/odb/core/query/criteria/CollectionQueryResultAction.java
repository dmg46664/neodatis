package org.neodatis.odb.core.query.criteria;

import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.OID;
import org.neodatis.odb.Objects;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.layers.layer2.instance.InstanceBuilderContext;
import org.neodatis.odb.core.layers.layer2.meta.AttributeValuesMap;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeObjectInfo;
import org.neodatis.odb.core.query.IMatchingObjectAction;
import org.neodatis.odb.core.query.InternalQuery;
import org.neodatis.odb.core.query.MatchResult;
import org.neodatis.odb.core.query.list.objects.InMemoryBTreeCollection;
import org.neodatis.odb.core.query.list.objects.LazyBTreeCollection;
import org.neodatis.odb.core.query.list.objects.LazySimpleListFromOid;
import org.neodatis.odb.core.query.list.objects.SimpleList;
import org.neodatis.odb.core.query.nq.NQMatchResult;
import org.neodatis.odb.core.session.SessionEngine;
import org.neodatis.tool.wrappers.OdbComparable;


/**
 * Class that manage normal query. Query that return a list of objects. For each object
 * That matches the query criteria, the objectMatch method is called and it keeps the objects in the 'objects' instance.
 * @author olivier
 *
 */
public class CollectionQueryResultAction implements IMatchingObjectAction {
	private InternalQuery query;

	private boolean inMemory;

	private long nbObjects;

	private SessionEngine sessionEngine;

	private boolean returnObjects;

	// TODO check if Object is ok here
	private Objects<Object> result;

	private boolean queryHasOrderBy;
	protected InstanceBuilderContext ibc;

	public CollectionQueryResultAction(InternalQuery query, SessionEngine sessionEngine, boolean returnObjects) {
		super();
		this.query = query;
		this.inMemory = query.getQueryParameters().isInMemory();
		this.sessionEngine = sessionEngine;
		this.returnObjects = returnObjects;
		this.queryHasOrderBy = query.hasOrderBy();
		this.ibc = new InstanceBuilderContext(query.getQueryParameters().getLoadDepth());
	}

	public void add(MatchResult matchResult, Comparable orderByKey) {
		
		
		if(matchResult instanceof NnoiMatchResult){
			NnoiMatchResult nnoiMatchResult = (NnoiMatchResult) matchResult;
			if (inMemory) {
				if (returnObjects) {
					if (queryHasOrderBy) {
						result.addWithKey(orderByKey, getInstance(nnoiMatchResult.nnoi));
					} else {
						result.add(getInstance(nnoiMatchResult.nnoi));
					}
				} else {
					if (queryHasOrderBy) {
						result.addWithKey(orderByKey, nnoiMatchResult.nnoi);
					} else {
						result.add(nnoiMatchResult.nnoi);
					}
				}
			}			
		}else if(matchResult instanceof OidMatchResult){
			OidMatchResult oidMatchResult = (OidMatchResult) matchResult;
			if (queryHasOrderBy) {
				result.addWithKey(orderByKey, oidMatchResult.oid);
			} else {
				result.add(oidMatchResult.oid);
			}
		}else if(matchResult instanceof NQMatchResult){
			NQMatchResult nqMatchResult = (NQMatchResult) matchResult;
			if (queryHasOrderBy) {
				result.addWithKey(orderByKey, nqMatchResult.object);
			} else {
				result.add(nqMatchResult.object);
			}
		}
	}

	public void start() {

		if (inMemory) {
			if (query != null && query.hasOrderBy()) {
				result = new InMemoryBTreeCollection((int) nbObjects, query.getOrderByType(), query.getSessionEngine().getSession().getConfig().getDefaultCollectionBTreeDegree());
			} else {
				result = new SimpleList((int) nbObjects);
				// result = new InMemoryBTreeCollection((int) nbObjects);
			}
		} else {
			if (query != null && query.hasOrderBy()) {
				result = new LazyBTreeCollection((int) nbObjects, sessionEngine, returnObjects,query.getSessionEngine().getSession().getConfig().getDefaultCollectionBTreeDegree());
			} else {
				result = new LazySimpleListFromOid((int) nbObjects, sessionEngine, returnObjects,query.getQueryParameters().getLoadDepth());
			}
		}
	}

	public void end() {

	}

	public Object getInstance(NonNativeObjectInfo nnoi)  {
		if(nnoi.getObject()!=null){
			return nnoi.getObject();	
		}
		return sessionEngine.layer2ToLayer1(nnoi, ibc);
		
	}

	public <T>Objects<T> getObjects() {
		return (Objects<T>)result;
	}

	/* (non-Javadoc)
	 * @see org.neodatis.odb.core.query.IMatchingObjectAction#addObject(org.neodatis.odb.OID, java.lang.Object, org.neodatis.tool.wrappers.OdbComparable)
	 */
	public void addObject(OID oid, Object object, OdbComparable orderByKey) {
		throw new NeoDatisRuntimeException(NeoDatisError.NOT_YET_IMPLEMENTED);
	}

	/* (non-Javadoc)
	 * @see org.neodatis.odb.core.query.IMatchingObjectAction#addValues(org.neodatis.odb.OID, org.neodatis.odb.core.layers.layer2.meta.AttributeValuesMap, org.neodatis.tool.wrappers.OdbComparable)
	 */
	public void addValues(OID oid, AttributeValuesMap map, OdbComparable orderByKey) {
		throw new NeoDatisRuntimeException(NeoDatisError.NOT_YET_IMPLEMENTED);
	}
}
