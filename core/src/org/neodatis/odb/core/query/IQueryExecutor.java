package org.neodatis.odb.core.query;

import org.neodatis.odb.Objects;

public interface IQueryExecutor {

	/**
	 * The main query execution method
	 * 
	 * @param queryResultAction
	 * @return
	 * @throws Exception
	 */
	<T>Objects<T> execute(IMatchingObjectAction queryResultAction);

}