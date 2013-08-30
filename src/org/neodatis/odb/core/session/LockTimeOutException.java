/**
 * 
 */
package org.neodatis.odb.core.session;

/**
 * @author olivier
 *
 */
public class LockTimeOutException extends RuntimeException {

	/**
	 * @param oidToString
	 */
	public LockTimeOutException(String oidToString) {
		super(oidToString);
	}

}
