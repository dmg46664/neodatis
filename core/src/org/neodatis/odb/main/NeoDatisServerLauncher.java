/**
 * 
 */
package org.neodatis.odb.main;

import org.neodatis.odb.NeoDatis;
import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.ODBServer;

/** a simple default server
 * @author olivier
 * 
 */
public class NeoDatisServerLauncher {
	public static void main(String[] args) throws Exception {
		int defaultPort = 10001;
		if(args.length==1){
			String sport = args[0];
			defaultPort = Integer.parseInt(sport);
		}
		
		NeoDatisConfig config = NeoDatis.getConfigFromFile();
		ODBServer server = NeoDatis.openServer(defaultPort);
		server.setAutomaticallyCreateDatabase(true);
		server.startServer(true);
	}
}
