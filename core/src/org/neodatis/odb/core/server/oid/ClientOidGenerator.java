package org.neodatis.odb.core.server.oid;

import org.neodatis.odb.core.layers.layer4.OidGenerator;
import org.neodatis.odb.core.server.MessageStreamer;
import org.neodatis.odb.core.server.message.Message;
import org.neodatis.odb.core.session.Session;

public interface ClientOidGenerator extends OidGenerator {
	void set(OidGenerator oidGenerator, Session session, MessageStreamer streamer);

}
