/**
 * 
 */
package org.neodatis.odb.core.refactor;

import java.util.Iterator;
import java.util.Map;

import org.neodatis.odb.NeoDatisEventType;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.event.EventManager;
import org.neodatis.odb.core.event.MetaModelHasChangedEvent;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfo;
import org.neodatis.odb.core.session.Session;
import org.neodatis.tool.DLogger;
import org.neodatis.tool.wrappers.list.IOdbList;

/**
 * A class that manages schema evolution
 * 
 * @author olivier
 * 
 */
public class MetaModelEvolutionManagerImpl implements MetaModelEvolutionManager {
	protected Session session;
	protected EventManager eventManager;
	protected boolean verbose;
	protected boolean abortIfChangesAreNotCompatible;

	public MetaModelEvolutionManagerImpl(Session session) {
		this.session = session;
		this.eventManager = session.getEventManager();
	}

	/**
	 * Check if the database meta model is equal with the java classes metamodel
	 * 
	 * @param updateDatabaseMetaModelIfPossible
	 *            if True, NeoDatis, if possible, will update the stored meta
	 *            model.
	 * @param abortIfChangesAreNotCompatible
	 *            If true, throws an exception if some meta model change is not
	 *            compatible
	 * @param verbose
	 *            If true, log what's happening
	 * @return
	 */
	public CheckMetaModelResult check(boolean updateDatabaseMetaModelIfPossible, boolean abortIfChangesAreNotCompatible, boolean verbose) {
		this.abortIfChangesAreNotCompatible = abortIfChangesAreNotCompatible;
		this.verbose = verbose;
		// Get the database meta model classes
		IOdbList<ClassInfo> databaseCIs = session.getMetaModel().getAllClasses();
		// Then retrieve class definition held by the current class loader =>
		// java class definitions
		Map<String, ClassInfo> javaClasses = session.getEngine().getObjectIntrospector().getClassIntrospector().instrospect(databaseCIs);

		// then, compare both
		CheckMetaModelResult checkMetaModelResult = checkMetaModelCompatibility(javaClasses);

		ClassInfoCompareResult result = null;

		for (int i = 0; i < checkMetaModelResult.size(); i++) {
			result = checkMetaModelResult.getResults().get(i);
			if (result.hasChanged()) {
				DLogger.info("Class " + result.getFullClassName() + " has changed :");
				DLogger.info(result.toString());
			}
		}
		// if update can be made then tries to update meta model
		if (updateDatabaseMetaModelIfPossible) {
			if (!checkMetaModelResult.getResults().isEmpty()) {
				if (checkMetaModelResult.changesAreCompatible()) {
					if (checkMetaModelResult.hasChanged()) {
						session.updateMetaModel();
						checkMetaModelResult.setModelHasBeenUpdated(true);
					}
				} else {
					throw new NeoDatisRuntimeException(NeoDatisError.INCOMPATIBLE_METAMODEL.addParameter(checkMetaModelResult.toString()));
				}
			}
		} else {
			// else fire event to call listeners if some have been registered
			session.getEventManager().fireEvent(new MetaModelHasChangedEvent(checkMetaModelResult));
		}

		return checkMetaModelResult;
	}

	/**
	 * Actually check the database meta model against the java class meta model
	 * 
	 * 
	 * 
	 * 
	 * @param currentCIs
	 *            The class definitions held by the current java class loader
	 * @return The result of the comparison
	 */
	protected CheckMetaModelResult checkMetaModelCompatibility(Map<String, ClassInfo> currentCIs) {
		// The Ci currently persisted in database
		ClassInfo persistedCI = null;
		// The Ci that mapped the current java class
		ClassInfo currentCI = null;

		ClassInfoCompareResult result = null;
		CheckMetaModelResult checkMetaModelResult = new CheckMetaModelResult();
		ClassInfoComparator ciComparator = new ClassInfoComparator();

		// Check User classes
		Iterator<ClassInfo> iterator = session.getMetaModel().getUserClasses().iterator();

		while (iterator.hasNext()) {
			persistedCI = iterator.next();
			currentCI = currentCIs.get(persistedCI.getFullClassName());

			if (verbose) {
				DLogger.info("Analysing class " + persistedCI.getFullClassName());
			}

			// compare the database ci with the java ci
			result = ciComparator.compare(persistedCI, currentCI, true, session.getConfig().getCoreProvider().getClassPool());
			if (abortIfChangesAreNotCompatible && !result.isCompatible()) {
				throw new NeoDatisRuntimeException(NeoDatisError.INCOMPATIBLE_METAMODEL.addParameter(result.toString()));
			}
			checkMetaModelResult.add(result);
		}
		// Check System classes
		iterator = session.getMetaModel().getSystemClasses().iterator();
		while (iterator.hasNext()) {
			persistedCI = iterator.next();
			currentCI = currentCIs.get(persistedCI.getFullClassName());
			result = ciComparator.compare(persistedCI, currentCI, true,session.getConfig().getCoreProvider().getClassPool());
			if (abortIfChangesAreNotCompatible && !result.isCompatible()) {
				throw new NeoDatisRuntimeException(NeoDatisError.INCOMPATIBLE_METAMODEL.addParameter(result.toString()));
			}
			checkMetaModelResult.add(result);
		}

		return checkMetaModelResult;
	}

	public void addListener(ClassHasChangedListener listener) {
		eventManager.addEventListener(NeoDatisEventType.META_MODEL_HAS_CHANGED, listener);
	}

}
