package org.neodatis.odb.core.server.message.process;


public class AsyncProcessId implements RemoteProcessReturn {
	protected String processId;
	
	public AsyncProcessId(String processId){
		this.processId = processId;
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

}
