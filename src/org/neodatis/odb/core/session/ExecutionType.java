package org.neodatis.odb.core.session;

public class ExecutionType {
	public static final int CLIENT = 10;
	public static final int LOCAL_CLIENT = 11;
	public static final int REMOTE_CLIENT = 12;
	public static final int REMOTE_SAMEVM_CLIENT = 13;
	public static final int SERVER = 20;
	public static boolean isClient(int executionType) {
		return executionType==CLIENT||executionType==LOCAL_CLIENT||executionType==REMOTE_CLIENT||executionType==REMOTE_SAMEVM_CLIENT;
	}
	public static boolean isServer(int executionType) {
		return executionType == SERVER;
	}

}
