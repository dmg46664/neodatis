/**
 * 
 */
package jdbm;

import java.io.File;
import java.util.Date;

import org.neodatis.odb.NeoDatis;
import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.ODB;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.Objects;
import org.neodatis.odb.core.layers.layer4.plugin.jdbm.NeoDatisJdbmPlugin;

/**
 * @author olivier
 * 
 */
public class TestNeoDatisJDBM {
	public static void main(String[] args) {
		
		NeoDatisConfig config = NeoDatis.getConfig();
		String baseName = "unit-test-data/test-jdbm.neodatis";
		System.out.println(new File(baseName).delete());
		config.setStorageEngineClass(NeoDatisJdbmPlugin.class).setDebugLayers(false);
		new File("unit-test-data").mkdirs();
		ODB odb = NeoDatis.open(baseName,config);
		ObjectOid oid = odb.store(new Human("Karine", 1, new Date()));
		odb.close();
		
		
		odb = NeoDatis.open(baseName,config);
		
		Human h = (Human) odb.getObjectFromId(oid);
		System.out.println(h);
		
		Objects<Human> humans = odb.query(Human.class).objects();
		odb.close();
		System.out.println(humans.size());
		System.out.println(humans);
		
		
		
		
	}
}
