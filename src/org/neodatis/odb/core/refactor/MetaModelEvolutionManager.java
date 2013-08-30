/**
 * 
 */
package org.neodatis.odb.core.refactor;


/**
 * @author olivier
 *
 */
public interface MetaModelEvolutionManager {

	/**
	 * Check if the database meta model is equal with the java classes metamodel
	 * @param updateDatabaseMetaModelIfPossible if True, NeoDatis, if possible, will update the stored meta model.
	 * @param abortIfChangesAreNotCompatible If true, throws an exception if some meta model change is not compatible
	 * @param verbose If true, log what's happening 
	 * @return
	 */
	public CheckMetaModelResult check(boolean updateDatabaseMetaModelIfPossible, boolean abortIfChangesAreNotCompatible,  boolean verbose);


	public void addListener(ClassHasChangedListener listener);

}
