/**
 * 
 */
package org.neodatis.odb.core.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author olivier
 *
 */
public class ConfigFileReader {
	public static Properties read(String fileName) throws IOException{
		Properties properties = new Properties();
		InputStream is = null; 
		File file = new File(fileName);
		if(file.exists()){
			is = new FileInputStream(fileName); 
		}else{
			is = String.class.getResourceAsStream("/"+fileName);	
		}
		
		if(is!=null){
			properties.load(is);
		}
		
		return properties;

	}
}
