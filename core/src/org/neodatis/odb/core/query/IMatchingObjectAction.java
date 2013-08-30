package org.neodatis.odb.core.query;

import org.neodatis.odb.Objects;

/**
 * The interface used to implement the classes that are called by the generic query executor when an object matches the query
 * @author osmadja
 *
 */
public interface IMatchingObjectAction {
	/** Called at the beginning of the query execution - used to prepare result object*/
	void start();
	
	/** Called (by the GenericQueryExecutor) when an object matches with lazy loading, only stores the OID*/
	void add(MatchResult matchResult, Comparable orderByKey);

	/** Called at the end of the query execution - used to clean or finish some task*/
	void end();
	
	/** Returns the resulting objects*/
	<T>Objects<T> getObjects();

}
