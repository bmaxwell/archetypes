/**
 *
 */
package org.jboss.reproducer.ejb.sfsb;

import java.util.logging.Logger;

import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import org.jboss.reproducer.ejb.api.AbstractEJB;
import org.jboss.reproducer.ejb.api.sfsb.SFSBLocal;
import org.jboss.reproducer.ejb.api.sfsb.SFSBRemote;

/**
 * @author bmaxwell
 *
 */
@Stateful
public class SFSBEJB extends AbstractEJB implements SFSBLocal, SFSBRemote {

	private static Logger log = Logger.getLogger(SFSBEJB.class.getName());

	/**
	 *
	 */
	public SFSBEJB() {
	}


	@PrePassivate
	public void prePassivate() {
		log.info("prePassivate");
	}

	@PostActivate
	public void postActivate() {
		log.info("postActivate");
	}

	@Override
    @Remove
	public void remove() {
		log.info("remove");
	}
}