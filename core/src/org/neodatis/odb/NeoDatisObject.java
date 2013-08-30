/**
 * 
 */
package org.neodatis.odb;

import java.io.Serializable;

/** an interface that user classes may implement to be able to use some advanced NeoDatis features like object reconnect, lazy loading,...
 * @author olivier
 *
 */
public interface NeoDatisObject extends Serializable{
	void setNeoDatisContext(NeoDatisContext context);
	NeoDatisContext getNeoDatisContext();
}
