package org.neodatis.odb.main;

import org.neodatis.odb.ClassRepresentation;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.refactor.RefactorManager;
import org.neodatis.odb.core.session.Session;
import org.neodatis.odb.core.trigger.DeleteTrigger;
import org.neodatis.odb.core.trigger.InsertTrigger;
import org.neodatis.odb.core.trigger.SelectTrigger;
import org.neodatis.odb.core.trigger.UpdateTrigger;

public class ODBForTrigger extends ODBAdapter {

	public ODBForTrigger(Session session) {
		super(session);
	}

	public void addDeleteTrigger(DeleteTrigger trigger) {
		throw new NeoDatisRuntimeException(NeoDatisError.OPERATION_NOT_ALLOWED_IN_TRIGGER);
	}

	public void addInsertTrigger(InsertTrigger trigger) {
		throw new NeoDatisRuntimeException(NeoDatisError.OPERATION_NOT_ALLOWED_IN_TRIGGER);
	}

	public void addSelectTrigger(SelectTrigger trigger) {
		throw new NeoDatisRuntimeException(NeoDatisError.OPERATION_NOT_ALLOWED_IN_TRIGGER);
	}

	public void addUpdateTrigger(UpdateTrigger trigger) {
		throw new NeoDatisRuntimeException(NeoDatisError.OPERATION_NOT_ALLOWED_IN_TRIGGER);
	}

	public void close() {
		throw new NeoDatisRuntimeException(NeoDatisError.OPERATION_NOT_ALLOWED_IN_TRIGGER);
	}

	public void commit() {
		throw new NeoDatisRuntimeException(NeoDatisError.OPERATION_NOT_ALLOWED_IN_TRIGGER);
	}

	public void commitAndClose() {
		throw new NeoDatisRuntimeException(NeoDatisError.OPERATION_NOT_ALLOWED_IN_TRIGGER);
	}

	public void defragmentTo(String newFileName) {
		throw new NeoDatisRuntimeException(NeoDatisError.OPERATION_NOT_ALLOWED_IN_TRIGGER);
	}

	public void disconnect(Object object) {
		throw new NeoDatisRuntimeException(NeoDatisError.OPERATION_NOT_ALLOWED_IN_TRIGGER);
	}

	public ClassRepresentation getClassRepresentation(Class clazz) {
		throw new NeoDatisRuntimeException(NeoDatisError.OPERATION_NOT_ALLOWED_IN_TRIGGER);
	}

	public ClassRepresentation getClassRepresentation(String fullClassName) {
		throw new NeoDatisRuntimeException(NeoDatisError.OPERATION_NOT_ALLOWED_IN_TRIGGER);
	}

	public RefactorManager getRefactorManager() {
		throw new NeoDatisRuntimeException(NeoDatisError.OPERATION_NOT_ALLOWED_IN_TRIGGER);
	}

	public void reconnect(Object object) {
		throw new NeoDatisRuntimeException(NeoDatisError.OPERATION_NOT_ALLOWED_IN_TRIGGER);
	}

	public void rollback() {
		throw new NeoDatisRuntimeException(NeoDatisError.OPERATION_NOT_ALLOWED_IN_TRIGGER);
	}

	public void run() {
		throw new NeoDatisRuntimeException(NeoDatisError.OPERATION_NOT_ALLOWED_IN_TRIGGER);
	}

}
