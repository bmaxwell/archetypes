/**
 *
 */
package org.jboss.reproducer.ejb.api.slsb;

import javax.ejb.Remote;

/**
 * @author bmaxwell
 *
 */
@Remote
public interface SLSBRemote {

	SLSBResponse invoke(SLSBRequest request);

	String hello(String name);
}