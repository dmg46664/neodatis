/**
 * 
 */
package org.neodatis.odb.core.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.neodatis.odb.OID;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.layers.layer4.BaseIdentification;
import org.neodatis.odb.core.session.ExecutionType;
import org.neodatis.odb.core.session.Session;
import org.neodatis.odb.core.session.SessionImpl;

/**
 * @author olivier
 * 
 */
public class ServerSession extends SessionImpl implements Session {

	protected List<ReturnValue> valuesToReturn;

	/**
	 * @param parameter
	 */
	public ServerSession(BaseIdentification parameter) {
		super(parameter);
		this.valuesToReturn = new ArrayList<ReturnValue>();
	}

	/**
	 * @param sessionManager
	 * @param sessionId
	 * @param transactional
	 */
	public ServerSession(BaseIdentification parameter, String sessionId) {
		super(parameter, sessionId);
		this.valuesToReturn = new ArrayList<ReturnValue>();
	}

	/**
	 * @param clientIds
	 */
	public void setClientIds(OID[] clientIds) {
		// TODO Auto-generated method stub

	}

	/**
	 * @return
	 */
	public ObjectOid[] getServerIds() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param actionClose
	 */
	public void setCurrentAction(int action) {

	}

	/**
	 * 
	 */
	public void endCurrentAction() {

	}

	/**
	 * 
	 */
	public void resetClassInfoIds() {

	}

	/**
	 * Used to receive notification for info we need to send back to the client
	 * 
	 */
	@Override
	public void update(Observable o, Object value) {
		if (value != null) {
			storeReturnValue((ReturnValue) value);
		}
	}

	/**
	 * @param arg
	 */
	private void storeReturnValue(ReturnValue returnValue) {
		valuesToReturn.add(returnValue);
	}

	public List<ReturnValue> getValuesToReturn() {
		return valuesToReturn;
	}

	public void clearValuesToReturn() {
		valuesToReturn = new ArrayList<ReturnValue>();
	}
	@Override
	public boolean isLocal() {
		return false;
	}
	@Override
	public int getExecutionType() {
		return ExecutionType.SERVER;
	}
	
}
