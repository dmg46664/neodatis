/**
 * 
 */
package org.neodatis.odb.core.layers.layer1;

import org.neodatis.odb.core.layers.layer4.OidGenerator;
import org.neodatis.odb.core.session.Session;
import org.neodatis.tool.wrappers.OdbSystem;

/**
 * @author olivier
 *
 */
public class IntrospectorFactory {
	
	public static ObjectIntrospector getObjectIntrospector(Session session, ClassIntrospector classIntrospector, OidGenerator oidGenerator){
		return new ObjectIntrospectorImpl(session, classIntrospector, oidGenerator);
	}
	
	public static ClassIntrospector getClassIntrospector(Session session, OidGenerator oidGenerator){
		
		if (osIsAndroid()) {
			// One feature is currently not supported on Android : dynamic empty
			// constructor creation
			return new AndroidClassIntrospector(session, oidGenerator);
		} else {
			return new DefaultClassIntrospector(session, oidGenerator);
		}
	}

	private static boolean osIsAndroid() {
		String javaVendor = OdbSystem.getProperty("java.vendor");
		if (javaVendor != null && javaVendor.equals("The Android Project")) {
			return true;
		}
		return false;
	}

}
