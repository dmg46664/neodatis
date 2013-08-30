package org.neodatis.odb.core.session.cross;


public class CacheFactory {
	
	
	/**
	 * This factory method returns an implementation of {@link ICrossSessionCache}
	 * to take over the objects across the sessions.
	 * @param identification TODO
	 * @return {@link ICrossSessionCache}
	 */
	public static ICrossSessionCache getCrossSessionCache(String identification){
		return CrossSessionCache.getInstance(identification);
	}

}
