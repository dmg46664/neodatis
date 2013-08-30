/**
 * 
 */
package org.neodatis.odb.core.query;

import org.neodatis.odb.core.query.criteria.CriteriaQuery;
import org.neodatis.odb.core.query.criteria.CriteriaQueryImpl;
import org.neodatis.odb.core.query.criteria.Criterion;

/**
 * @author olivier
 *
 */
public class QueryFactory {
	public static CriteriaQuery query(Class clazz){
		return new CriteriaQueryImpl(clazz);
	}
	public static CriteriaQuery query(Class clazz, Criterion c){
		return new CriteriaQueryImpl(clazz, c);
	}
	public static CriteriaQuery query(String className){
		return new CriteriaQueryImpl(className);
	}
	public static CriteriaQuery query(String className, Criterion c){
		return new CriteriaQueryImpl(className, c);
	}
}
