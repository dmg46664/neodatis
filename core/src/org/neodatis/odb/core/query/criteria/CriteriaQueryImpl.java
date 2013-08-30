/**
 * 
 */
package org.neodatis.odb.core.query.criteria;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Map;

import org.neodatis.odb.Objects;
import org.neodatis.odb.core.layers.layer2.meta.AbstractObjectInfo;
import org.neodatis.odb.core.query.AbstractQuery;
import org.neodatis.odb.core.query.IQueryExecutionPlan;
import org.neodatis.tool.wrappers.OdbClassUtil;

/**
 * @author olivier
 * 
 */
public class CriteriaQueryImpl extends AbstractQuery implements CriteriaQuery{
	String fullClassName;
	Criterion criterion;

	public boolean hasCriteria() {
		return criterion != null;
	}

	public boolean match(AbstractObjectInfo aoi) {
		if (criterion == null) {
			return true;
		}
		return criterion.match(aoi);
	}

	public boolean match(Map map) {
		if (criterion == null) {
			return true;
		}
		return criterion.match(map);
	}

	public CriteriaQueryImpl(Class aClass, Criterion criteria) {
		this(OdbClassUtil.getFullName(aClass), criteria);

	}

	public CriteriaQueryImpl(Class aClass) {
		this(OdbClassUtil.getFullName(aClass));
	}

	public CriteriaQueryImpl(String aFullClassName) {
		super();
		this.fullClassName = aFullClassName;
		this.criterion = null;
	}

	public CriteriaQueryImpl(String aFullClassName, Criterion criteria) {
		super();
		this.fullClassName = aFullClassName;
		if (criteria != null) {
			this.criterion = criteria;
			this.criterion.setQuery(this);
		}
	}

	public String getFullClassName() {
		return fullClassName;
	}

	public Criterion getCriteria() {
		return criterion;
	}

	public String toString() {
		if (criterion == null) {
			return "no criterion";
		}
		return criterion.toString();
	}

	public HashSet<String> getAllInvolvedFields() {
		if (criterion == null) {
			return new HashSet<String>();
		}
		return criterion.getAllInvolvedFields();
	}

	public void setCriterion(Criterion criterion) {
		this.criterion = criterion;
	}

	public void setExecutionPlan(IQueryExecutionPlan plan) {
		executionPlan = plan;
	}

	public <T> Objects<T> objects() {
		return this.getSessionEngine().execute(this);
	}
	public BigInteger count() {
		return this.getSessionEngine().count(this);
	}
}