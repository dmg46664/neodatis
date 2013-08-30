package jdbm;

import java.io.File;
import java.io.IOException;

import jdbm.helper.FastIterator;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.layers.layer4.OidGenerator;
import org.neodatis.odb.core.layers.layer4.plugin.jdbm.PHashMap;
import org.neodatis.odb.core.oid.uuid.UniqueOidGeneratorImpl;

/**
 * Sample JDBM application to demonstrate the use of basic JDBM operations.
 *
 * @author <a href="mailto:boisvert@intalio.com">Alex Boisvert</a>
 * @version $Id: MyJdbmTest.java,v 1.3 2010/02/19 04:26:52 olivier_smadja Exp $
 */
public class MyJdbmTest
{
   


    public void runDemo()
        throws IOException
    {
    	
        // insert keys and values
        System.out.println();
        System.out.println( "Starting" );
        String fileName = "test1.neodatis";
        new File(fileName).delete();
        PHashMap hashtable = new PHashMap(fileName);
        
        int size = 20000;
        OidGenerator oidGenerator = new UniqueOidGeneratorImpl();
        ClassOid coid = oidGenerator.createClassOid();
        long startInsert = System.currentTimeMillis();
        for(int i=0;i<size;i++){
        	ObjectOid oid = oidGenerator.createObjectOid(coid);
            hashtable.put( oid.oidToString(), ("banana-banana-banana-banana-jhsdfkjldhflkjadhflkdjfhgldjhgjlkdhgkldjdgklgdfghdflghldfkghdfskjghdfskhgdfhgldkfghkldshgdkfjghkhkfjghfskfshglksdfhgkljdshgdkfjlhgdfkjhgdfjkhgdfkj"+i).getBytes() );
            
            if(i%10000==0){
            	System.out.println(i);
            	hashtable.commit();
            }
        }
        long endInsert = System.currentTimeMillis();
        hashtable.commit();
        
        try {
            // Thread.sleep( 10 * 1000 );
        } catch ( Exception except ) {
            // ignore
        }

        // iterate over remaining objects
        System.out.println( "Get objects" );
        long startGet = System.currentTimeMillis();

        int nbObjects = 0;
        FastIterator iter = hashtable.keys();
        String key = (String) iter.next();
        while ( key != null ) {
            String value = new String((byte[]) hashtable.get( key ));
            //System.out.println( key + " => " + value );
            key = (String) iter.next();
            nbObjects++;
        }
        long endGet = System.currentTimeMillis();
        // cleanup
        hashtable.close();
        
        
        
        System.out.println("got " + nbObjects + " objects");
        System.out.println("time for insert=" + (endInsert-startInsert));
        System.out.println("time for get=" + (endGet-startGet));
    }


    public static void main( String[] args )
    {
        try {
            MyJdbmTest basket = new MyJdbmTest();
            basket.runDemo();
        } catch ( IOException ioe ) {
            ioe.printStackTrace();
        }
    }

}
