/**
 * 
 */
package org.neodatis.odb.core.query.criteria;

import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.query.MatchResult;

/**
 * @author olivier
 * 
 */
public class OidMatchResult extends MatchResult {
	public ObjectOid oid;

	public OidMatchResult(ObjectOid oid) {
		super(true);
		this.oid = oid;
	}

}
