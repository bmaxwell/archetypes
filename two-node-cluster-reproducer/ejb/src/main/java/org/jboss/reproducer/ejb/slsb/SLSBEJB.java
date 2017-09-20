
/**
 *
 */
package org.jboss.reproducer.ejb.slsb;

import java.util.Date;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import org.jboss.reproducer.ejb.api.AbstractEJB;
import org.jboss.reproducer.ejb.api.slsb.SLSBLocal;
import org.jboss.reproducer.ejb.api.slsb.SLSBRemote;

/**
 * @author bmaxwell
 *
 */
@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
public class SLSBEJB extends AbstractEJB implements SLSBLocal, SLSBRemote {

	/**
	 *
	 */
	public SLSBEJB() {
	}

    @Override
    public String hello(String name) {
        return "Hello " + name;
    }

    @Override
    public String sleep(long sleepMilliSeconds) {
        Date date = new Date();
        try {
            Thread.sleep(sleepMilliSeconds);
        } catch(Exception e) { }
        return "Slept " + (new Date().getTime() - date.getTime()) + " ms";
    }
}