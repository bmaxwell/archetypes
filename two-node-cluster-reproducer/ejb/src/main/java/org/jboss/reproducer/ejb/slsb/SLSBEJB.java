/**
 *
 */
package org.jboss.reproducer.ejb.slsb;

import javax.ejb.Stateless;

import org.jboss.reproducer.ejb.api.slsb.SLSBLocal;
import org.jboss.reproducer.ejb.api.slsb.SLSBRemote;
import org.jboss.reproducer.ejb.api.slsb.SLSBRequest;
import org.jboss.reproducer.ejb.api.slsb.SLSBResponse;

/**
 * @author bmaxwell
 *
 */
@Stateless
public class SLSBEJB implements SLSBLocal, SLSBRemote {

	/**
	 *
	 */
	public SLSBEJB() {
	}

	@Override
	public SLSBResponse invoke(SLSBRequest request) {
		return new SLSBResponse();
	}

    @Override
    public String hello(String name) {
        return "Hello " + name;
    }
}