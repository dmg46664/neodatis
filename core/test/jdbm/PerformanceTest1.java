package jdbm;

import java.util.Date;

import org.neodatis.odb.NeoDatis;
import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.ODB;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.Objects;
import org.neodatis.odb.Query;
import org.neodatis.odb.core.layers.layer2.instance.FullInstantiationHelper;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeObjectInfo;
import org.neodatis.odb.core.layers.layer4.plugin.jdbm.NeoDatisJdbmPlugin;
import org.neodatis.odb.core.layers.layer4.plugin.memory.InMemoryStorageEngine;
import org.neodatis.odb.core.layers.layer4.plugin.memory.InMemoryStorageEngineFast;
import org.neodatis.odb.core.oid.uuid.UniqueOidGeneratorImpl;
import org.neodatis.tool.IOUtil;
import org.neodatis.tool.wrappers.OdbTime;

public class PerformanceTest1 {
	public static int TEST_SIZE = 20000;
	public static final String ODB_FILE_NAME = "perf.neodatis";

	boolean firstTest;
	float nbTests;
	float totalTime;
	float inserTime;
	float selectTime;
	float updateTime;
	float deleteTime;

	public PerformanceTest1() {
		firstTest = true;
	}

	public void testEmpty() {
		// to avoid junit junit.framework.AssertionFailedError: No tests found
		// in ...
	}

	public void testInsertSimpleObjectODB(boolean force) throws Exception {

		// OdbConfiguration.setReconnectObjectsToSession(false);
		// Thread.sleep(20000);
		boolean doUpdate = true;
		boolean doDelete = true;
		boolean doSelect = true;

		// Configuration.setDatabaseCharacterEncoding(null);
		// LogUtil.logOn(FileSystemInterface.LOG_ID,true);
		// LogUtil.logOn(ObjectReader.LOG_ID,true);
		// Configuration.setUseLazyCache(true);
		boolean inMemory = true;
		// Configuration.monitorMemory(true);
		// Configuration.setUseModifiedClass(true);
		// Deletes the database file
		IOUtil.deleteFile(ODB_FILE_NAME);
		IOUtil.deleteFile(ODB_FILE_NAME + ".lg");
		long t1 = 0, t2 = 0, t3 = 0, t4 = 0, t5 = 0, t6 = 0, t7 = 0, t77 = 0, t8 = 0;
		ODB odb = null;
		Objects<SimpleObject> l = null;
		SimpleObject so = null;
		NeoDatisConfig config = NeoDatis.getConfig().setStorageEngineClass(NeoDatisJdbmPlugin.class).setDebugStorageEngine(false).setOidGeneratorClass(
				UniqueOidGeneratorImpl.class).setUseStorageEngineCache(true).setOidGeneratorUseCache(true);
		//config.setStorageEngineClass(InMemoryStorageEngineFast.class);
		// Insert TEST_SIZE objects
		System.out.println("Inserting " + TEST_SIZE + " objects");

		t1 = OdbTime.getCurrentTimeInMs();
		odb = NeoDatis.open(ODB_FILE_NAME, config);
		int i = 0;
		// odb.getClassRepresentation(SimpleObject.class).addFullInstantiationHelper(new
		// SimpleObjectFullInstantiationHelper());

		for (i = 0; i < TEST_SIZE; i++) {
			Object o = getSimpleObjectInstance(i);
			ObjectOid oid = odb.store(o);
			// System.out.println("i="+i + " - oid="+oid);
			if (i % 100 == 0) {
				// System.out.println("i="+i + " - oid="+oid);
				// Monitor.displayCurrentMemory(""+i,true);
				// System.out.println("Cache="+Dummy.getEngine(odb).getSession().getCache().toString());
			}
		}
		t2 = OdbTime.getCurrentTimeInMs();

		if (!isInMemory(config)) {
			// Closes the database
			odb.close();
		}

		// if(true)return;
		t3 = OdbTime.getCurrentTimeInMs();

		System.out.println("Retrieving " + TEST_SIZE + " objects");
		// Reopen the database
		if (!isInMemory(config)) {
			odb = NeoDatis.open(ODB_FILE_NAME, config);
		}
		if(true){
			//return;
		}

		if (doSelect) {
			// Gets the TEST_SIZE objects
			Query q = odb.query(SimpleObject.class);
			q.getQueryParameters().setInMemory(inMemory);
			q.orderByAsc("duration");
			l = q.objects();
			t4 = OdbTime.getCurrentTimeInMs();

			System.out.println("real nb objects after select " + l.size());
			/*
			 * // Actually get objects while (l.hasNext()) { Object o =
			 * l.next(); }
			 */
			i = 0;
			int d = 12;
			while (l.hasNext()) {
				SimpleObject o = l.next();
				if (i % 10000 == 0) {
					// Monitor.displayCurrentMemory("select "+i,true);
				}
				// System.out.println("Selecting oid "+ odb.gtObjectId(o));
				if (d != o.getDuration()) {
					// System.out.println("Selecting " + o.getDuration() + " - "
					// + odb.getObjectId(o) + "  - " + d + "!="+
					// o.getDuration());
					d = o.getDuration();
				} else {
					// System.out.println(o.getDuration()+" - " +
					// odb.getObjectId(o));
				}
				i++;
				d++;
			}
		}
		t5 = OdbTime.getCurrentTimeInMs();

		if (doUpdate) {
			System.out.println("Updating " + TEST_SIZE + " objects");
			i = 0;
			so = null;
			l.reset();
			while (l.hasNext()) {
				so = (SimpleObject) l.next();
				so.setName(so.getName() + " updated");
				odb.store(so);
				if (i % 10000 == 0) {
					// Monitor.displayCurrentMemory(""+i);
				}
				i++;
			}
		}

		t6 = OdbTime.getCurrentTimeInMs();

		if (!isInMemory(config)) {
			// Closes the database
			odb.close();
		}
		t7 = OdbTime.getCurrentTimeInMs();

		if (doDelete) {

			System.out.println("Deleting " + TEST_SIZE + " objects");
			if (!isInMemory(config)) {
				odb = NeoDatis.open(ODB_FILE_NAME, config);
			}

			Query q = odb.query(SimpleObject.class);
			q.getQueryParameters().setInMemory(inMemory);
			l = odb.getObjects(q);
			t77 = OdbTime.getCurrentTimeInMs();
			// System.out.println("After getting objects - before delete");

			i = 0;

			while (l.hasNext()) {
				so = (SimpleObject) l.next();
				if (!so.getName().endsWith("updated")) {
					throw new RuntimeException("Update  not ok for " + so.getName());
				}
				odb.delete(so);
				// if (i % 10000 == 0) {

				// }
				i++;
			}
			odb.close();

		}
		t8 = OdbTime.getCurrentTimeInMs();

		displayResult("ODB " + TEST_SIZE + " SimpleObject objects ", t1, t2, t3, t4, t5, t6, t7, t77, t8);
		System.out.println(NeoDatisJdbmPlugin.stats());
	}

	private boolean isInMemory(NeoDatisConfig config) {
		return config.getStorageEngineClass() == InMemoryStorageEngineFast.class || config.getStorageEngineClass() == InMemoryStorageEngine.class;
	}

	private SimpleObject getSimpleObjectInstance(int i) {
		SimpleObject so = new SimpleObject();
		so.setDate(new Date());
		so.setDuration(i);
		so.setName("Bonjour, comment allez vous?" + i);
		return so;
	}

	private void displayResult(String string, long t1, long t2, long t3, long t4, long t5, long t6, long t7, long t77, long t8) {

		String s1 = " total=" + (t8 - t1);
		String s2 = " total insert=" + (t3 - t1) + " -- " + "insert=" + (t2 - t1) + " commit=" + (t3 - t2) + " o/s=" + (float) TEST_SIZE / (float) ((t3 - t1))
				* 1000;
		String s3 = " total select=" + (t5 - t3) + " -- " + "select=" + (t4 - t3) + " get=" + (t5 - t4) + " o/s=" + (float) TEST_SIZE / (float) ((t5 - t3))
				* 1000;
		String s4 = " total update=" + (t7 - t5) + " -- " + "update=" + (t6 - t5) + " commit=" + (t7 - t6) + " o/s=" + (float) TEST_SIZE / (float) ((t7 - t5))
				* 1000;
		String s5 = " total delete=" + (t8 - t7) + " -- " + "select=" + (t77 - t7) + " - delete=" + (t8 - t77) + " o/s=" + (float) TEST_SIZE
				/ (float) ((t8 - t7)) * 1000;

		System.out.println(string + s1 + " | " + s2 + " | " + s3 + " | " + s4 + " | " + s5);

		long ttotal = t8 - t1;
		long tinsert = t3 - t1;
		long tselect = t5 - t3;
		long tupdate = t7 - t5;
		long tdelete = t8 - t7;

		if (firstTest) {
			//discard first tests
			firstTest = false;
		} else {
			totalTime += ttotal;
			inserTime += tinsert;
			selectTime += tselect;
			updateTime += tupdate;
			deleteTime += tdelete;
			nbTests++;
		}

	}

	private void displayTotal() {
		String s = String.format("Total=%f\tInsert=%f\tSelect=%f\tUpdate=%f\tDelete=%f", totalTime / nbTests, inserTime / nbTests, selectTime / nbTests,
				updateTime / nbTests, deleteTime / nbTests);
		System.out.println(s);
	}

	public static void main(String[] args) throws Exception {
		PerformanceTest1 pt = new PerformanceTest1();
		for (int i = 0; i < 10; i++) {
			// Thread.sleep(15000);
			// OdbConfiguration.setMessageStreamerClass(HessianMessageStreamer.class);

			pt.testInsertSimpleObjectODB(true);

		}
		System.out.println("\n");
		pt.displayTotal();
	}

}

class SimpleObjectFullInstantiationHelper implements FullInstantiationHelper {

	public Object instantiate(NonNativeObjectInfo nnoi) {
		SimpleObject so = new SimpleObject();
		/*
		 * so.setDate((Date) nnoi.getValueOf("date")); so.setDuration(((Integer)
		 * nnoi.getValueOf("duration")).intValue()); so.setName((String)
		 * nnoi.getValueOf("name"));
		 */
		return so;
	}

}
