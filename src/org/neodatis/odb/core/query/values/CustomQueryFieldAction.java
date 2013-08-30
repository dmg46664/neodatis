package org.neodatis.odb.core.query.values;

import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.query.IQueryFieldAction;

public abstract class CustomQueryFieldAction extends AbstractQueryFieldAction implements ICustomQueryFieldAction{
	
	public CustomQueryFieldAction() {
		super(null,null,true);
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	
	public IQueryFieldAction copy() {
		try {
			ICustomQueryFieldAction cqfa = (ICustomQueryFieldAction) getClass().newInstance();
			cqfa.setAttributeName(attributeName);
			cqfa.setAlias(alias);
			return  cqfa;
		} catch (Exception e) {
			throw new NeoDatisRuntimeException(NeoDatisError.VALUES_QUERY_ERROR_WHILE_CLONING_CUSTUM_QFA.addParameter(getClass().getName()));
		}
	}
}
