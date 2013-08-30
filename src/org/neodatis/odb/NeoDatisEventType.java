/**
 * 
 */
package org.neodatis.odb;

/**Some event the user can lister to
 * @author olivier
 *
 */
public class NeoDatisEventType {
	protected String name;
	protected int type;
	/** Event fired when NeoDatis detects that the meta model of the database is different from the classes in the current JVM classpath
	 * 
	 */
	public static final NeoDatisEventType META_MODEL_HAS_CHANGED = new NeoDatisEventType("MetaModelHasChanged", 10);
	
	/** Event fired when a database is opened
	 * 
	 */
	public static final NeoDatisEventType DATABASE_OPEN = new NeoDatisEventType("DatabaseOpen", 20);
	
	/** Event fired when a database is opened
	 * 
	 */
	public static final NeoDatisEventType NEW_CLASS_INTROSPECTOR = new NeoDatisEventType("NewClassIntrospector", 30);
	
	private NeoDatisEventType(String name, int type){
		this.name = name;
		this.type = type;
	}
}
