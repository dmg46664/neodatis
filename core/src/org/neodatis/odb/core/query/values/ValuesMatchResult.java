/**
 * 
 */
package org.neodatis.odb.core.query.values;

import org.neodatis.odb.core.layers.layer2.meta.AttributeValuesMap;
import org.neodatis.odb.core.query.MatchResult;

/**
 * @author olivier
 *
 */
public class ValuesMatchResult extends MatchResult {
	public AttributeValuesMap valuesMap;
	
	public ValuesMatchResult(AttributeValuesMap map){
		super(true);
		this.valuesMap = map;
	}

	/**
	 * @param b
	 */
	public ValuesMatchResult(boolean b) {
		super(b);
	}
	

}
