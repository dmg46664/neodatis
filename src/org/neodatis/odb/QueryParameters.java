/**
 * 
 */
package org.neodatis.odb;

import java.io.Serializable;

/**A class to contain some query execution parameters
 * @author olivier
 *
 */
public class QueryParameters implements Serializable{
	/** to specify the level of objects that be loaded. Default is 0 which means: load all
	 * <p>
	 * 1: will load only the first level object
	 * <br>
	 * 2: will load the first 2 levels
	 * </p>
	 * 
	 * <pre>
	 * Example: 
	 * class User{
	 * 	String name;
	 *  Profile profile;
	 * }
	 * class Profile{
	 *   String name;
	 *   List<Function> functions;
	 * }
	 * class Function{
	 * 	String name;
	 * }
	 * 
	 * executing a query on User with loadDepth=1 with return object of type User with attribute profile = null. 
	 * executing a query on User with loadDepth=2 with return object of type User with attribute profile with functions (list) as null.
	 * </pre>
	 * */
	protected int loadDepth;
	
	/** To tell NeoDatis to only load lists on demand
	 * <br>
	 * Default value is false*/
	protected boolean lazyLists;
	
	/** To tell to NeoDatis if objects must be loaded into memory. 
	 * 
	 * <br>
	 * default is true
	 * 
	 * If false, only oid are loaded and then, on demand, objects will be lazily loaded
	 */
	protected boolean inMemory;

	/** To indicate if NeoDatis must scan objects from the start of the objects or from the end.
	 * <br>
	 * If you work with real time system and need to work on the most recent objects, specify false.
	 * 
	 * <br>
	 * default is true.
	 */
	protected boolean scanFromStart;
	
	protected int startIndex;
	protected int endIndex;
	public QueryParameters() {
		loadDepth = 0;
		lazyLists = false;
		inMemory = true;
		scanFromStart = true;
		startIndex = -1;
		endIndex = -1;
	}
	public int getLoadDepth() {
		return loadDepth;
	}
	public QueryParameters setLoadDepth(int loadDepth) {
		this.loadDepth = loadDepth;
		return this;
	}
	public boolean isLazyLists() {
		return lazyLists;
	}
	public QueryParameters setLazyLists(boolean lazyLists) {
		this.lazyLists = lazyLists;
		return this;
	}
	public boolean isInMemory() {
		return inMemory;
	}
	public QueryParameters setInMemory(boolean inMemory) {
		this.inMemory = inMemory;
		return this;
	}
	public boolean isScanFromStart() {
		return scanFromStart;
	}
	public QueryParameters setScanFromStart(boolean scanFromStart) {
		this.scanFromStart = scanFromStart;
		return this;
	}
	public int getStartIndex() {
		return startIndex;
	}
	public QueryParameters setStartIndex(int startIndex) {
		this.startIndex = startIndex;
		return this;
	}
	public int getEndIndex() {
		return endIndex;
	}
	public QueryParameters setEndIndex(int endIndex) {
		this.endIndex = endIndex;
		return this;
	}
	
	public QueryParameters setStartAndEndIndex(int start, int end){
		this.startIndex = start;
		this.endIndex = end;
		return this;
	}
	
}
