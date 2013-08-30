package org.neodatis.odb.core.trigger;

public interface CommitListener {
	void beforeCommit();
	void afterCommit();
}
