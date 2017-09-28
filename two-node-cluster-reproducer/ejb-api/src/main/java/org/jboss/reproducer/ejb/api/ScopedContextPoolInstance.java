/**
 * 
 */
package org.jboss.reproducer.ejb.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 */
public class ScopedContextPoolInstance implements PoolInstance {

    private EJBRemoteScopedContextConfig ejbRemoteConfig;
    private Context initialContext = null;

    private Map<String, Object> ejbProxies = new ConcurrentHashMap<String, Object>();

    public ScopedContextPoolInstance(EJBRemoteScopedContextConfig ejbRemoteConfig) {
        this.ejbRemoteConfig = ejbRemoteConfig;
    }

    public Context getInitialContext() throws Exception {
        if(initialContext == null) {
            synchronized (this) {
                if(initialContext == null) {
                    this.ejbRemoteConfig.setScopedContext(true);
                    initialContext = new InitialContext(ejbRemoteConfig.getConfiguration());
                }
            }
        }
        return initialContext;
    }

    public Object getEjbProxy(String jndiPath) throws NamingException, Exception {
        Object ejbProxy = this.ejbProxies.get(jndiPath);
        if(ejbProxy == null) {
            synchronized (this) {
                ejbProxy = this.ejbProxies.get(jndiPath);
                if(ejbProxy == null) {
                    if(jndiPath.split("/").length < 3)
                        ejbProxy = getInitialContext().lookup("ejb:/" + jndiPath);
                    else
                        ejbProxy = getInitialContext().lookup("ejb:" + jndiPath);
                    this.ejbProxies.put(jndiPath, ejbProxy);
                }
            }
        }
        return ejbProxy;
    }
}