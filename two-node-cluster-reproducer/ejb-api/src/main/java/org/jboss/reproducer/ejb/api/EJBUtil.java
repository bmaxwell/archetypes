/**
 * 
 */
package org.jboss.reproducer.ejb.api;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.reproducer.ejb.api.EJBRemoteConfig.Connection;

/**
 * @author bmaxwell
 *
 */
public class EJBUtil <T> {

	public enum Mode {
		SINGLE_REMOTE_NAMING_NEW_EJB_PROXY_EACH_CALL, SINGLE_REMOTE_NAMING_CACHED_EJB_PROXY, REMOTE_NAMING_PER_THREAD, SINGLE_SCOPED_CONTEXT_CACHE_EJB_PROXY,
		// SCOPED_CONTEXT_PER_THREAD,
		// EJB_CLIENT
	}

	private static Mode mode;
	private static String ejbLookup;
	private RemoteEJBUtil<T> remoteEJBUtil = null;
	private EJBRemoteConfig ejbRemoteConfig;

	public EJBUtil(Mode mode, EJBRemoteConfig ejbRemoteConfig, String ejbLookup) {
		EJBUtil.mode = mode;
		this.ejbRemoteConfig = ejbRemoteConfig;
		this.ejbLookup = ejbLookup;
		switch (mode) {
		case SINGLE_REMOTE_NAMING_CACHED_EJB_PROXY:
			this.remoteEJBUtil = new SingleRemoteNamingMode(ejbRemoteConfig, true);
			break;
		case SINGLE_REMOTE_NAMING_NEW_EJB_PROXY_EACH_CALL:
			this.remoteEJBUtil = new SingleRemoteNamingMode(ejbRemoteConfig, false);
			break;
		case REMOTE_NAMING_PER_THREAD:
			this.remoteEJBUtil = new RemoteNamingPerThreadMode(ejbRemoteConfig);
			break;
		case SINGLE_SCOPED_CONTEXT_CACHE_EJB_PROXY:
			this.remoteEJBUtil = new SingleScopedContextMode(ejbRemoteConfig, true);
			break;
		// case SCOPED_CONTEXT_PER_THREAD:
		// this.remoteEJBUtil = new ();
		// break;
		// case EJB_CLIENT:
		// this.remoteEJBUtil = new ();
		// break;
		default:
		}
	}

	// static init used by everything
	// public static void init(String host, String port, String username, String
	// password, String ejbLookup) {
	// EJBUtil.host = host;
	// EJBUtil.port = port;
	// EJBUtil.username = username;
	// EJBUtil.password = password;
	// EJBUtil.ejbLookup = ejbLookup;
	// }

	public Context getInitialContext() throws Exception {
		return remoteEJBUtil.getInitialContext();
	}

	public T getEJBProxy() throws Exception {
		return remoteEJBUtil.getEJBProxy();
	}

	private static interface RemoteEJBUtil<T> {

		public Context getInitialContext() throws Exception;

		public T getEJBProxy() throws Exception;
	}

	private static class SingleRemoteNamingMode<T> implements RemoteEJBUtil<T> {

		// use 1 initialContext, sync to ensure it is only created once, then
		// after that all threads will use the same initialContext
		private static Context initialContext;
		private EJBRemoteConfig ejbRemoteConfig;
		private boolean cacheEjbProxy;
		private T ejbProxy = null;

		public SingleRemoteNamingMode(EJBRemoteConfig ejbRemoteConfig, boolean cacheEjbProxy) {
			this.ejbRemoteConfig = ejbRemoteConfig;
			this.cacheEjbProxy = cacheEjbProxy;
		}

		@Override
		public Context getInitialContext() throws Exception {
			if (initialContext == null) {
				synchronized (this) {
					if (initialContext == null) {
						Connection connection = ejbRemoteConfig.getConnections().iterator().next();
						initialContext = newRemoteNamingInitialContext(
								"remote://" + connection.getHost() + ":" + connection.getPort(),
								connection.getUsername(), connection.getPassword());
					}
				}
			}
			return initialContext;
		}

		@Override
		public T getEJBProxy() throws Exception {
			if (cacheEjbProxy) {
				if (ejbProxy == null) {
					ejbProxy = (T) getInitialContext().lookup(ejbLookup);
				}
				return ejbProxy;
			} else {
				return (T) getInitialContext().lookup(ejbLookup);
			}
		}
	}

	private static class RemoteNamingPerThreadMode<T> implements RemoteEJBUtil<T> {

		// each thread will create a remote naming context (connection) and use
		// it
		private Context initialContext;
		private EJBRemoteConfig ejbRemoteConfig;

		public RemoteNamingPerThreadMode(EJBRemoteConfig ejbRemoteConfig) {
			this.ejbRemoteConfig = ejbRemoteConfig;
		}

		@Override
		public Context getInitialContext() throws Exception {
			if (initialContext == null) {
				Connection connection = ejbRemoteConfig.getConnections().iterator().next();
				initialContext = newRemoteNamingInitialContext(
						"remote://" + connection.getHost() + ":" + connection.getPort(), connection.getUsername(),
						connection.getPassword());
			}
			return initialContext;
		}

		@Override
		public T getEJBProxy() throws Exception {
			return (T) getInitialContext().lookup(ejbLookup);
		}
	}

	private static class SingleScopedContextMode<T> implements RemoteEJBUtil<T> {

		private Context scopedInitialContext;
		private EJBRemoteConfig ejbRemoteConfig;
		private boolean cacheEjbProxy = false;
		private T cachedEjbProxy = null;

		public SingleScopedContextMode(EJBRemoteConfig ejbRemoteConfig, boolean cacheEjbProxy) {
			this.ejbRemoteConfig = ejbRemoteConfig;
			this.cacheEjbProxy = cacheEjbProxy;
		}

		@Override
		public Context getInitialContext() throws Exception {
			if (this.scopedInitialContext == null) {
				synchronized (this) {
					if (this.scopedInitialContext == null) {
						this.ejbRemoteConfig.setScopedContext(true);
						this.scopedInitialContext = new InitialContext(ejbRemoteConfig.getConfiguration());
					}
				}
			}
			return this.scopedInitialContext;
		}

		@Override
		public T getEJBProxy() throws Exception {
			if (cacheEjbProxy) {
				if (cachedEjbProxy == null)
					cachedEjbProxy = (T) getInitialContext().lookup("ejb:/" + ejbLookup);
				return cachedEjbProxy;
			} else {
				return (T) getInitialContext().lookup("ejb:/" + ejbLookup);
			}
		}
	}

	private static Context newRemoteNamingInitialContext(String providerUrl, String username, String password)
			throws Exception {
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
		env.put("java.naming.factory.initial", "org.jboss.naming.remote.client.InitialContextFactory");
		env.put("java.naming.provider.url", providerUrl);
		env.put("jboss.naming.client.ejb.context", "true");

		if (username != null)
			env.put(Context.SECURITY_PRINCIPAL, username);
		if (password != null)
			env.put(Context.SECURITY_CREDENTIALS, password);

		for (String key : env.keySet()) {
			System.out.println(key + " => " + env.get(key));
		}

		return new InitialContext(env);
	}

	private Context newScopedContext() throws Exception {
		ejbRemoteConfig.setScopedContext(true);
		return new InitialContext(ejbRemoteConfig.getConfiguration());
	}
}
