package org.neodatis.fs.io;

import org.neodatis.fs.NdfsConfig;
import org.neodatis.fs.NdfsException;
import org.neodatis.odb.NeoDatis;
import org.neodatis.odb.core.layers.layer3.DataConverter;
import org.neodatis.odb.core.layers.layer3.DataConverterImpl;

public class ConverterBuilder {

	public static DataConverter buildByteArrayConverter(NdfsConfig config) throws NdfsException {
		try{
			return new DataConverterImpl(config.debug(), config.getCharacterEncoding(), NeoDatis.getConfig().setDatabaseCharacterEncoding(config.getCharacterEncoding()));
		}catch (Exception e) {
			throw new NdfsException(e);
		}
	}
}
