/**
 * 
 */
package org.neodatis.odb;

import org.neodatis.odb.core.config.NeoDatisConfigImpl;


/**
 * @author olivier
 *
 */
public class NeoDatisGlobalConfig {

	protected static NeoDatisConfig instance = new NeoDatisConfigImpl(false);
	
	public static NeoDatisConfig get(){
		return instance;
	}
}
