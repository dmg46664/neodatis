package org.neodatis.odb.core.server.connection;

import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.ODB;
import org.neodatis.odb.core.server.ServerSession;
import org.neodatis.odb.core.server.message.process.RemoteProcess;
import org.neodatis.odb.core.server.message.process.RemoteProcessMessage;
import org.neodatis.odb.core.server.message.process.RemoteProcessReturn;
import org.neodatis.odb.core.session.ExecutionType;
import org.neodatis.odb.core.session.SessionWrapper;
import org.neodatis.odb.main.ODBForTrigger;

/**
 * A class that extends thread used to execute asychronous remote processes
 * @author olivier
 *
 */
public class ThreadToExecuteRemoteProcess extends Thread{
	protected ODB odb;
	protected RemoteProcessMessage message;	
	protected ServerSession session;
	protected NeoDatisConfig serverConfig;
	public ThreadToExecuteRemoteProcess(ODB odb, ServerSession session, RemoteProcessMessage message, NeoDatisConfig config) {
		super();
		
		this.odb = odb;
		this.session = session;
		this.message = message;
		this.serverConfig = config;
	}

	
	@Override
	public void run() {
		//System.out.println("Starting ThreadToExecuteRemoteProcess " + getName());
		// 	we create an odb that wraps the session odb. But in this case, eventhough the session is a server session, the 
		// the execution happens as if it where a local odb. That is why we wrap the session to change the execution type
		odb = new ODBForTrigger(new SessionWrapper(session, ExecutionType.LOCAL_CLIENT));
		RemoteProcess rp = message.getProcess();
		rp.setClientIp(message.getClientIp());
		rp.setOdb(odb);
		rp.setServerConfig(serverConfig);
		RemoteProcessReturn r = rp.execute();
	
		odb = null;
		rp = null;
		//System.out.println("Ending ThreadToExecuteRemoteProcess " + getName());
		//return new RemoteProcessMessageResponse(baseIdentifier, message.getSessionId(), r);
	}
}
