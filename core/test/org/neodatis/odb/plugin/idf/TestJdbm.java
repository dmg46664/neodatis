package org.neodatis.odb.plugin.idf;

import junit.framework.TestCase;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.layers.layer4.OidGenerator;
import org.neodatis.odb.core.oid.uuid.UniqueOidGeneratorImpl;
import org.neodatis.odb.plugin.idf.jdbm.JDBMIndexer;

public class TestJdbm extends TestCase {
	
	/** A simple insert an the index
	 * 
	 * @throws Exception
	 */
	public void test1() throws Exception{
		
		Indexer indexer = new JDBMIndexer();
		indexer.init(String.valueOf(System.currentTimeMillis()), "unit-test-data");
		
		OidGenerator generator = new UniqueOidGeneratorImpl();
		ClassOid coid = generator.createClassOid();
		ObjectOid ooid = generator.createObjectOid(coid);
		
		ObjectLocation ol1 = new ObjectLocation(1,1524,10); 
		indexer.put(ooid, ol1);
		
		ObjectLocation ol2 = indexer.get(ooid);
		
		indexer.close();
		assertEquals(ol1, ol2);
		
	}

	/** 100000 inserts in the index
	 * 
	 * @throws Exception
	 */
	public void test100000Inserts() throws Exception{
		
		Indexer indexer = new JDBMIndexer();
		indexer.init(String.valueOf(System.currentTimeMillis()), "unit-test-data");
		
		OidGenerator generator = new UniqueOidGeneratorImpl();
		ClassOid coid = generator.createClassOid();
		ObjectOid oid2Search = null;
		int size = 100000;
		for(int i=0;i<size;i++){
			ObjectOid ooid = generator.createObjectOid(coid);
			ObjectLocation ol1 = new ObjectLocation(i,i+1,i+2); 
			indexer.put(ooid, ol1);
			
			if(i%1000==0){
				System.out.println(i);
			}
			
			if(i==95000){
				oid2Search = ooid;
			}
		}
		long t0 = System.currentTimeMillis();
		ObjectLocation ol2 = indexer.get(oid2Search);
		long t1 = System.currentTimeMillis();
		System.out.println("Time for search=" + (t1-t0));
		indexer.close();
		assertEquals(95000, ol2.getFileId());
		assertEquals(95001, ol2.getPosition());
		
		
	}
	
	/** 100000 inserts in the index
	 * 
	 * @throws Exception
	 */
	public void test100000InsertsAndGetAfterClose() throws Exception{
		long id = 100000;
		String directory = "/Volumes/Work/ndfs/data/big1";
		//String directory = "unit-test-data/aaa";
		
		Indexer indexer = new JDBMIndexer();
		indexer.init(String.valueOf(id), directory);
		
		OidGenerator generator = new UniqueOidGeneratorImpl();
		ClassOid coid = generator.createClassOid();
		ObjectOid oid2Search = null;
		int size = 100000000;
		long t0 = System.currentTimeMillis();
		for(int i=0;i<size;i++){
			ObjectOid ooid = generator.createObjectOid(coid);
			ObjectLocation ol1 = new ObjectLocation(i,i+1,i+2); 
			indexer.put(ooid, ol1);
			
			if(i%10000==0){
				System.out.println(i +" : " + (System.currentTimeMillis() - t0));
				t0 = System.currentTimeMillis();
				indexer.close();
				indexer = new JDBMIndexer();
				indexer.init(String.valueOf(id), directory);
			}
			
			if(i==9500000){
				oid2Search = ooid;
			}
			
		}
		indexer.close();
		
		indexer = new JDBMIndexer();
		indexer.init(String.valueOf(id), directory);
		
		t0 = System.currentTimeMillis();
		ObjectLocation ol2 = indexer.get(oid2Search);
		long t1 = System.currentTimeMillis();
		System.out.println("Time for search=" + (t1-t0));
		
		assertEquals(9500000, ol2.getFileId());
		assertEquals(9500001, ol2.getPosition());
		
		
	}

}
