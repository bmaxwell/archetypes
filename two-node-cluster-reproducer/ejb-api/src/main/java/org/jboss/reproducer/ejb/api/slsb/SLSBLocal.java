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

	String hello(String name);

}