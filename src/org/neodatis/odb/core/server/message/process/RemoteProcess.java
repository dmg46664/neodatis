package org.neodatis.odb.core.server.message.process;

import java.io.Serializable;

import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.ODB;
import org.neodatis.odb.core.layers.layer4.engine.Dummy;

public abstract class RemoteProcess implements Serializable{
	protected transient ODB odb;
	protected transient NeoDatisConfig serverConfig;
	protected transient String clientIp;
	
	public abstract RemoteProcessReturn execute();
	public ODB getOdb() {
		return odb;
	}
	public void setOdb(ODB odb) {
		this.odb = odb;
	}
	public void setServerConfig(NeoDatisConfig config) {
		this.serverConfig = config;
	}
	public NeoDatisConfig getServerConfig() {
		return this.serverConfig;
	}
	public void setSessionParameter(String name, Object o){
		Dummy.getEngine(odb).getSession().setUserParameter(name, o);
	}
	public Object getSessionParameter(String name, boolean remove){
		return Dummy.getEngine(odb).getSession().getUserParameter(name, remove);
	}
	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}
	public String getClientIp(){
		return this.clientIp;
	}
}
