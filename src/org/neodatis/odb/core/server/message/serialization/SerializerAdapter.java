package org.neodatis.odb.core.server.message.serialization;

import org.neodatis.odb.NeoDatisConfig;

public abstract class SerializerAdapter implements ISerializer{
	private NeoDatisConfig config;

	public SerializerAdapter(NeoDatisConfig config) {
		this.config = config;
	}

	public NeoDatisConfig getConfig() {
		return config;
	}

	public void setConfig(NeoDatisConfig config) {
		this.config = config;
	}
	
}
