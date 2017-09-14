/**
 * 
 */
package org.jboss.reproducer.ejb.api;

import javax.naming.Context;
import javax.naming.NamingException;

/**
 * @author bmaxwell
 *
 */
public interface PoolInstance {

    public Context getInitialContext() throws Exception;
    public Object getEjbProxy(String jndiPath) throws NamingException, Exception;
}