/**
 *
 */
package org.jboss.reproducer.ejb.mdb;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.MessageListener;

import org.jboss.reproducer.ejb.api.mdb.MDBStats;

/**
 * @author bmaxwell
 *
 */
@MessageDriven(name = "MDB", activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/queue/TestQueue"),
		@ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
		@ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
		@ActivationConfigProperty(propertyName = "clientId", propertyValue = "MDB_CONSUMER_1"),
		// @ActivationConfigProperty(propertyName = "maxSession", propertyValue = "1")
		@ActivationConfigProperty(propertyName = "useDLQ", propertyValue = "false"),
		@ActivationConfigProperty(propertyName = "hA", propertyValue = "true"),
		})
public class MDB extends AbstractMessageConsumer implements MessageListener {

	public static MDBStats mdbStats = new MDBStats(MDB.class.getSimpleName() + ":MDB_CONSUMER_1");

	/**
	 *
	 */
	public MDB() {
		super(mdbStats, true);
	}
}