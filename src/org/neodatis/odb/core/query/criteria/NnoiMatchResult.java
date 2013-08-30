/**
 * 
 */
package org.neodatis.odb.core.query.criteria;

import org.neodatis.odb.core.layers.layer2.meta.NonNativeObjectInfo;
import org.neodatis.odb.core.query.MatchResult;

/**
 * @author olivier
 * 
 */
public class NnoiMatchResult extends MatchResult {
	public NonNativeObjectInfo nnoi;

	public NnoiMatchResult(NonNativeObjectInfo nnoi) {
		super(!nnoi.isDeletedObject());
		this.nnoi = nnoi;
	}

}
