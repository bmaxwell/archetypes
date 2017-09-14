/**
 * 
 */
package org.jboss.reproducer.ejb.api;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 */
public class RemoteNamingPoolInstance implements PoolInstance {

    private String providerUrl;
    private String username;
    private String password;
    private Context initialContext = null;

    private Map<String, Object> ejbProxies = new ConcurrentHashMap<String, Object>();

    public RemoteNamingPoolInstance(String providerUrl, String username, String password) {
        this.providerUrl = providerUrl;
        this.username = username;
        this.password = password;
    }

    public Context getInitialContext() throws Exception {
        if(initialContext == null) {
            synchronized (this) {
                if(initialContext == null)
                    initialContext = newRemoteNamingInitialContext(providerUrl, username, password);
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
                    ejbProxy = getInitialContext().lookup(jndiPath);
                    this.ejbProxies.put(jndiPath, ejbProxy);
                }
            }
        }
        return ejbProxy;
    }

    private static Context newRemoteNamingInitialContext(String providerUrl, String username, String password) throws Exception {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        env.put("java.naming.factory.initial", "org.jboss.naming.remote.client.InitialContextFactory");
        env.put("java.naming.provider.url", providerUrl);
        env.put("jboss.naming.client.ejb.context", "true");

        if(username != null)
            env.put(Context.SECURITY_PRINCIPAL, username);
        if(password != null)
            env.put(Context.SECURITY_CREDENTIALS, password);
        return new InitialContext(env);
    }

}
