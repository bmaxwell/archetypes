/**
 *
 */
package org.jboss.reproducer.ejb.api.slsb;

import javax.ejb.Local;

/**
 * @author bmaxwell
 *
 */
@Local
public interface SLSBLocal {

	SLSBResponse invoke(SLSBRequest request);

	String hello(String name);

}