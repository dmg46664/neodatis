package org.neodatis.odb.plugin.idf;

import junit.framework.TestCase;

import org.neodatis.odb.NeoDatis;
import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.ODB;
import org.neodatis.odb.Objects;

public class TestPluginIDF extends TestCase {

	public void test1() {

		String s = "test1-" + System.currentTimeMillis();
		NeoDatisConfig config = NeoDatis.getConfig().setStorageEngineClass(IDFPlugin.class).setBaseDirectory("unit-test-data");

		ODB odb = NeoDatis.open(s, config);
		odb.store(new Function("function"));
		odb.close();

		odb = NeoDatis.open(s, config);
		Objects<Function> functions = odb.query(Function.class).objects();
		assertEquals(1, functions.size());
		odb.close();

	}

	public void test2() {
		long t0 = System.currentTimeMillis();
		String s = "test1-" + System.currentTimeMillis();
		NeoDatisConfig config = NeoDatis.getConfig().setStorageEngineClass(IDFPlugin.class).setBaseDirectory("unit-test-data");

		ODB odb = NeoDatis.open(s, config);
		int size = 100000;
		
		for(int i=0;i<size;i++){
			odb.store(new Function("function"));
			if(i%1000==0){
				System.out.println(i);
			}
		}
		odb.close();

		odb = NeoDatis.open(s, config);
		Objects<Function> functions = odb.query(Function.class).objects();
		assertEquals(size, functions.size());
		odb.close();
		long t1 = System.currentTimeMillis();
		System.out.println((t1-t0)+"ms");
	}

}
