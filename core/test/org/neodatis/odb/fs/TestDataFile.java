package org.neodatis.odb.fs;

import java.io.File;

import junit.framework.TestCase;

import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.BytesFactory;
import org.neodatis.odb.plugin.idf.DataFile;

public class TestDataFile extends TestCase{

	boolean debug = false;
	
	public void test1(){
		String name = "name";
		String fileName = "file.txt";
		new File(fileName).delete();
		int nbBuffers = 4;
		int bufferSize = 4*1024;
		DataFile df = new DataFile(name, fileName, nbBuffers, bufferSize, debug);
		Bytes bb = BytesFactory.getBytes();
		bb.set(0, (byte) 'a');
		df.write(0, bb);
		assertEquals(1, df.length());
		df.close();
		
		df = new DataFile(name, fileName, nbBuffers, bufferSize, debug);
		assertEquals(1,df.length());
		df.close();
		
	}

	public void testBigFile(){
		String name = "name";
		//String fileName = "/Volumes/Work/ndfs/data/file.big.txt";
		String fileName = "unit-test-data/a1/file.big.txt";
		new File(fileName).getParentFile().mkdirs();
		new File(fileName).delete();
		int nbBuffers = 4;
		int bufferSize = 4*1024;
		DataFile df = new DataFile(name, fileName, nbBuffers, bufferSize, debug);
		long size = 1024*100;
		long max = Long.MAX_VALUE;
		long start = System.currentTimeMillis();
		for(long i=0;i<size;i++){
			byte[]b = buildBlock(8*1024);
			long l = i*b.length;
			df.write(l, BytesFactory.getBytes(b));
			assertEquals((i+1)*b.length ,df.length());
			//System.out.println("Lenght " + (i+1)*b.length + " => " + df.length());
			if(i%(1024*10)==0){
				try {
						long t2 = System.currentTimeMillis();
						
						long t0 = System.currentTimeMillis();
						df.flush();
						long t1 = System.currentTimeMillis();
						
						
						System.out.println(i + " : " + (l) + " / "+ (max-l) + " | " + (t2-start)+ " ms  | flush time=" + (t1-t0)+"ms");
						start = t2;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				};
			}
		}
		df.close();
		
		df = new DataFile(name, fileName, nbBuffers, bufferSize, debug);
		assertEquals(size*8*1024,df.length());
		df.close();
		
	}
	
	public void testLong(){
		int nbBuffers = 4;
		int bufferSize = 4*1024;
		int size = 1024*1024;
		long max = Long.MAX_VALUE;
		for(long i=0;i<size;i++){
			
			long l = i* 8*1024;
			if(i%1024==0){
				System.out.print(i/1024 + " / " );
				System.out.println(l + " | "+ (max-l));
			}
		}
		
	}
	public void testGetBigFile(){
		String name = "name";
		String fileName = "file.big.txt";
		int nbBuffers = 4;
		int bufferSize = 4*1024;
		long t0 = System.currentTimeMillis();
		DataFile df = new DataFile(name, fileName, nbBuffers, bufferSize, debug);
		Bytes b = df.read(99*8*1024, 8*1024);
		long t1 = System.currentTimeMillis();
		df.close();
		System.out.println("Time to get "+ (t1-t0)+"ms");
		assertEquals(0,b.get(0));
		
	}

	private byte[] buildBlock(int size) {
		byte[] b = new byte[size];
		for(int i=0;i<size;i++){
			b[i] = (byte) i;
		}
		return b;
	}

}
