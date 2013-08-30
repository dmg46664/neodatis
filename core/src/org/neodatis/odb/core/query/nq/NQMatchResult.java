/**
 * 
 */
package org.neodatis.odb.core.query.nq;

import org.neodatis.odb.core.layers.layer2.meta.NonNativeObjectInfo;
import org.neodatis.odb.core.query.MatchResult;

/**
 * @author olivier
 * 
 */
public class NQMatchResult extends MatchResult {
	public NonNativeObjectInfo nnoi;
	public Object object;

	public NQMatchResult(NonNativeObjectInfo nnoi, Object object) {
		super(!nnoi.isDeletedObject());
		this.nnoi = nnoi;
		this.object = object;
	}

}
