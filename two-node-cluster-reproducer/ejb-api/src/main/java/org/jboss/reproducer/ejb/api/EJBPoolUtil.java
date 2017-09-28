/**
 * 
 */
package org.jboss.reproducer.ejb.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.logging.Logger;

/**
 *
 */
public class EJBPoolUtil {

	private String providerUrl;
	private String username;
	private String password;
	private final int poolSize;
	private boolean initialized = false;
	private EJBRemoteScopedContextConfig ejbRemoteConfig;

	private Map<Integer, PoolInstance> initialContextPool;

	private Logger log = Logger.getLogger(EJBPoolUtil.class);

	public EJBPoolUtil(EJBRemoteScopedContextConfig ejbRemoteConfig, int poolSize) {
        this.ejbRemoteConfig = ejbRemoteConfig;
        this.poolSize = poolSize;
        this.initialContextPool = new ConcurrentHashMap<Integer,PoolInstance>(this.poolSize);
        for(int i=0; i<poolSize; i++)
            this.initialContextPool.put(i, new ScopedContextPoolInstance(ejbRemoteConfig));
        initContexts();
    }

	public EJBPoolUtil(String providerUrl, String username, String password, int poolSize) {
	    this.providerUrl = providerUrl;
	    this.username = username;
	    this.password = password;
	    this.poolSize = poolSize;
	    this.initialContextPool = new ConcurrentHashMap<Integer,PoolInstance>(this.poolSize);
        for(int i=0; i<poolSize; i++)
            this.initialContextPool.put(i, new RemoteNamingPoolInstance(providerUrl, username, password));
        initContexts();
	}

	private void initContexts() {
	    if(!initialized) {
	        synchronized (this) {
	            if(!initialized) {
	                try {
	                    for(PoolInstance instance : initialContextPool.values())
	                        instance.getInitialContext();
	                } catch(Exception e) {
	                    e.printStackTrace();
	                }
	                log.info("Prefilled IniitalContexts");
	                initialized = true;
	            }
            }
	    }
 	}

	public Context getInitialContext() throws Exception {
	    int random = ThreadLocalRandom.current().nextInt(poolSize);
	    return initialContextPool.get(random).getInitialContext();
	}

	public Object getEjbProxy(String jndiPath) throws NamingException, Exception {
	    int random = ThreadLocalRandom.current().nextInt(poolSize);
	    return initialContextPool.get(random).getEjbProxy(jndiPath);
	}
}