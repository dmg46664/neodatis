/**
 * 
 */
package org.neodatis.odb.main;

import org.neodatis.odb.NeoDatis;
import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.core.layers.layer4.IOFileParameter;
import org.neodatis.odb.core.refactor.MetaModelEvolutionManager;
import org.neodatis.odb.core.refactor.MetaModelEvolutionManagerImpl;
import org.neodatis.odb.core.session.Session;

/**
 * @author olivier
 *
 */
public class NeoDatisTools {
	public static MetaModelEvolutionManager getMetaModelEvolutionManager(String baseName){
		NeoDatisConfig config = NeoDatis.getConfig().setCheckMetaModelCompatibility(false);
		
		Session session = config.getCoreProvider().getLocalSession(new IOFileParameter(baseName, true, config));
		return new MetaModelEvolutionManagerImpl(session);
	}
}
