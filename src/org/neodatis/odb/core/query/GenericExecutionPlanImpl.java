/**
 * 
 */
package org.neodatis.odb.core.query;

import org.neodatis.odb.core.layers.layer2.meta.ClassInfoIndex;

/**
 * @author olivier
 *
 */
public class GenericExecutionPlanImpl implements IQueryExecutionPlan {

	
	
	private boolean useIndex;
	private long duration;
	private String details;

	
	
	public GenericExecutionPlanImpl(boolean useIndex, long duration, String details) {
		super();
		this.useIndex = useIndex;
		this.duration = duration;
		this.details = details;
	}

	public void end() {
	}

	public String getDetails() {
		return details;
	}

	public long getDuration() {
		return duration;
	}

	public ClassInfoIndex getIndex() {
		throw new RuntimeException("Not implemented");
	}

	public void start() {
	}

	public boolean useIndex() {
		return useIndex;
	}
}
