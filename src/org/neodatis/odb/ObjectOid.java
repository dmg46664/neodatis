/**
 * 
 */
package org.neodatis.odb;


/**
 * @author olivier
 *
 */
public interface ObjectOid extends OID {

	ClassOid getClassOid();
	void setClassOid(ClassOid classOid);
}
