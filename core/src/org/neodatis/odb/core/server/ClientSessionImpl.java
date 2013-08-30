/**
 * 
 */
package org.neodatis.odb.core.server;

import org.neodatis.odb.core.layers.layer2.meta.MetaModel;
import org.neodatis.odb.core.layers.layer4.BaseIdentification;
import org.neodatis.odb.core.session.ExecutionType;
import org.neodatis.odb.core.session.SessionEngine;
import org.neodatis.odb.core.session.SessionImpl;

/**
 * @author olivier
 * 
 */
public class ClientSessionImpl extends SessionImpl {

	/**
	 * @param parameter
	 */
	public ClientSessionImpl(BaseIdentification parameter) {
		super(parameter);
	}

	protected SessionEngine buildSessionEngine() {
		return new ClientSessionEngineImpl(this);
	}

	public void setMetaModel(MetaModel metaModel) {
		this.metaModel = metaModel;
	}

	@Override
	public boolean isLocal() {
		return false;
	}
	@Override
	public int getExecutionType() {
		return ExecutionType.REMOTE_CLIENT;
	}
	
}
