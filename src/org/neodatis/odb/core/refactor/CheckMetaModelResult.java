/**
 * 
 */
package org.neodatis.odb.core.refactor;

import java.io.Serializable;

import org.neodatis.tool.wrappers.list.IOdbList;
import org.neodatis.tool.wrappers.list.OdbArrayList;

/**
 * @author olivier
 *
 */
public class CheckMetaModelResult implements Serializable{
	private boolean modelHasBeenUpdated;
	private IOdbList<ClassInfoCompareResult> results;
	private boolean changesAreCompatible;
	private boolean hasChanged;
	
	public CheckMetaModelResult(){
		this.modelHasBeenUpdated = false;
		this.results = new OdbArrayList<ClassInfoCompareResult>();
	}

	public boolean isModelHasBeenUpdated() {
		return modelHasBeenUpdated;
	}

	public void setModelHasBeenUpdated(boolean modelHasBeenUpdated) {
		this.modelHasBeenUpdated = modelHasBeenUpdated;
	}

	public IOdbList<ClassInfoCompareResult> getResults() {
		return results;
	}

	public void setResults(IOdbList<ClassInfoCompareResult> results) {
		this.results = results;
	}
	
	public void add(ClassInfoCompareResult result){
		this.results.add(result);
		this.changesAreCompatible = this.changesAreCompatible || result.isCompatible();
		this.hasChanged = result.hasChanged();
	}
	
	public int size(){
		return this.results.size();
	}

	public boolean changesAreCompatible() {
		return changesAreCompatible;
	}

	public boolean hasChanged() {
		return hasChanged;
	}
}
