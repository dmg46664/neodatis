/**
 * 
 */
package org.neodatis.odb.core.query;


/**
 * @author olivier
 *
 */
public abstract class MatchResult {
	public boolean match;
	
	public MatchResult(boolean match){
		this.match = match;
	}
	

}
