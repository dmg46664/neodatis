package jdbm;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import junit.framework.TestCase;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.NeoDatis;
import org.neodatis.odb.OID;
import org.neodatis.odb.core.basics.NLong;
import org.neodatis.odb.core.layers.layer3.BytesFactory;
import org.neodatis.odb.core.layers.layer4.OidGenerator;
import org.neodatis.odb.core.layers.layer4.ObjectOidIterator.Way;
import org.neodatis.odb.core.layers.layer4.plugin.jdbm.JdbmObjectOidIterator;
import org.neodatis.odb.core.layers.layer4.plugin.jdbm.OidComparator;
import org.neodatis.odb.core.layers.layer4.plugin.jdbm.PHashMapBTree;
import org.neodatis.odb.core.layers.layer4.plugin.memory.InMemoryStorageEngineFast;
import org.neodatis.odb.core.oid.sequential.SequentialOidGeneratorImpl;
import org.neodatis.odb.core.oid.uuid.UniqueOidGeneratorImpl;

public class TestOid extends TestCase {

	public void test1() throws IOException {

		InMemoryStorageEngineFast e = new InMemoryStorageEngineFast();
		e.open("b", NeoDatis.getConfig());
		e.init(NeoDatis.getConfig());
		OidGenerator generator = new SequentialOidGeneratorImpl();
		generator.init(e, true);
		ClassOid coid = generator.createClassOid();
		int size = 20000;
		String fileName = "test1.neodatis";
		new File(fileName).delete();
		PHashMapBTree hashtable = new PHashMapBTree(fileName, false, false);

		Map<byte[], OID> map = new HashMap<byte[], OID>();
		for (int i = 0; i < size; i++) {
			OID oid = generator.createObjectOid(coid);
			System.out.println(oid);
			map.put(oid.toByte(), oid);
			hashtable.put(oid, BytesFactory.getBytes(oid.toByte()));
		}

		assertEquals(size, map.size());
		assertEquals(size, hashtable.getBtreeForName(coid.oidToString()).size());

		hashtable.close();
		hashtable = new PHashMapBTree(fileName, false, false);
		assertEquals(size, hashtable.getBtreeForName(coid.oidToString()).size());

		JdbmObjectOidIterator iterator = new JdbmObjectOidIterator(hashtable.getBtreeForName(coid.oidToString()), Way.DECREASING, generator);
		int i = 0;
		while (iterator.hasNext()) {
			i++;
			iterator.next();
		}
		assertEquals(size, i);
	}

	public void test2() throws IOException {

		InMemoryStorageEngineFast e = new InMemoryStorageEngineFast();
		e.open("b", NeoDatis.getConfig());
		e.init(NeoDatis.getConfig());
		OidGenerator generator = new SequentialOidGeneratorImpl();
		generator.init(e, true);
		ClassOid coid = generator.createClassOid();
		int size = 20000;

		OID oid = generator.createObjectOid(coid);

		Map<byte[], OID> map = new HashMap<byte[], OID>();
		for (int i = 0; i < size; i++) {
			oid = generator.createObjectOid(coid);
			// System.out.println( i + " = " + oid + " | " +
			// DisplayUtility.byteArrayToString(oid.toByte()) + "  |   " + );
			SimpleObject o = getSimpleObjectInstance(i);
			map.put(oid.toByte(), oid);

		}

		assertEquals(size, map.size());

	}

	public void test2Txt() throws IOException {

		InMemoryStorageEngineFast e = new InMemoryStorageEngineFast();
		e.open("b", NeoDatis.getConfig());
		e.init(NeoDatis.getConfig());
		OidGenerator generator = new UniqueOidGeneratorImpl();
		generator.init(e, true);
		ClassOid coid = generator.createClassOid();
		int size = 2;
		Map<OID, OID> map = new HashMap<OID, OID>();

		OID oid1 = generator.createObjectOid(coid);
		byte[] b1 = oid1.toByte();
		map.put(oid1, oid1);
		
		
		OID oid2 = generator.createObjectOid(coid);
		byte[] b2 = oid2.toByte();
		map.put(oid2, oid2);

		byte[]b11 = oid1.toByte();
		boolean b = OidComparator.compareByteArrays(b1, b11)==0;
		boolean b2o = b1.hashCode() == b11.hashCode();
		OID ooiidd = map.get(oid1);
		assertNotNull(ooiidd);
		assertEquals(size, map.size());

	}

	public void test3() throws IOException {

		InMemoryStorageEngineFast e = new InMemoryStorageEngineFast();
		e.open("b", NeoDatis.getConfig());
		e.init(NeoDatis.getConfig());
		OidGenerator generator = new SequentialOidGeneratorImpl();
		generator.init(e, true);

		String fileName = "test1.neodatis";
		new File(fileName).delete();
		PHashMapBTree hashtable = new PHashMapBTree(fileName, false, true);

		ClassOid coid = generator.createClassOid();
		int size = 20000;

		long start = System.currentTimeMillis();
		Map<byte[], Object> map = new HashMap<byte[], Object>();
		for (int i = 0; i < size; i++) {
			OID oid = generator.createObjectOid(coid);
			// map.put(oid.toByte(), getSimpleObjectInstance(i));
			hashtable.put(oid, BytesFactory.getBytes(getSimpleObjectInstance(i).getName().getBytes()));
		}

		long end = System.currentTimeMillis();

		System.out.println("debug:" + (end - start));
		// assertEquals(size, i);
	}

	public void testTimeWithOnlyMap() throws IOException {

		InMemoryStorageEngineFast e = new InMemoryStorageEngineFast();
		e.open("b", NeoDatis.getConfig());
		e.init(NeoDatis.getConfig());
		OidGenerator generator = new SequentialOidGeneratorImpl();
		generator.init(e, true);

		ClassOid coid = generator.createClassOid();
		int size = 20000;

		long start = System.currentTimeMillis();
		Map<byte[], Object> map = new HashMap<byte[], Object>();
		for (int i = 0; i < size; i++) {
			OID oid = generator.createObjectOid(coid);
			map.put(oid.toByte(), getSimpleObjectInstance(i));
		}

		long end = System.currentTimeMillis();

		System.out.println("debug:" + (end - start));
		assertEquals(size, map.size());
	}

	private SimpleObject getSimpleObjectInstance(int i) {
		SimpleObject so = new SimpleObject();
		so.setDate(new Date());
		so.setDuration(i);
		so.setName("Bonjour, comment allez vous?" + i);
		return so;
	}

	public void testLong() {
		int size = 100000000;

		Long l = new Long(1);
		long start = System.currentTimeMillis();
		for (int i = 0; i < size; i++) {
			l = new Long(l.longValue() + 1);
		}
		long end = System.currentTimeMillis();

		System.out.println("testLong=" + (end - start));
	}

	public void testAtomicLong() {
		int size = 100000000;

		AtomicLong l = new AtomicLong(1);
		long start = System.currentTimeMillis();
		for (int i = 0; i < size; i++) {
			l.addAndGet(1);
		}
		long end = System.currentTimeMillis();

		System.out.println("testAtomicLong=" + (end - start));
	}

	public void testNLong() {
		int size = 100000000;

		NLong l = new NLong(1);
		long start = System.currentTimeMillis();
		for (int i = 0; i < size; i++) {
			l.add(1);
		}
		long end = System.currentTimeMillis();

		System.out.println("testNLong=" + (end - start));
	}
}
