/**
 *
 */
package org.jboss.reproducer.ejb.api;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
* @author bmaxwell
*
*/
@XmlRootElement(name="ejb-remote-scoped-context-config", namespace="http://redhat.com/scoped-context")
@XmlAccessorType(XmlAccessType.FIELD)
public class EJBRemoteScopedContextConfig implements EJBRemoteConfig, Serializable {

	private static final String REMOTE_CONNECTION = "remote.connection.";
	private static final String REMOTE_CLUSTER = "remote.cluster.";
	private static final String SSL_ENABLED = "remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED"; // true/false , default false

	@XmlAttribute(name="scoped-context")
	private boolean scopedContext = false;
	@XmlAttribute(name="ssl-enabled")
	private Boolean sslEnabled = false;
	@XmlAttribute(name="unnamed-connection-number")
	private int unnamedConnectionNumber = 0;

	@XmlElement(name="env")
	private Properties env = new Properties();
	@XmlElement(name="connection")
	private Map<String, Connection> connections = new HashMap<String, Connection>();
	@XmlElement(name="connection-defaults")
	private final ConnectionDefaults connectionDefaults = new ConnectionDefaults();
	@XmlElement(name="cluster")
	private Map<String, Cluster> clusters = new HashMap<String, Cluster>();

    protected static final String MAX_INBOUND_CHANNELS = ".connect.options.org.jboss.remoting3.RemotingOptions.MAX_INBOUND_CHANNELS";
    protected static final String MAX_OUTBOUND_CHANNELS = ".connect.options.org.jboss.remoting3.RemotingOptions.MAX_OUTBOUND_CHANNELS";
    protected static final String MAX_OUTBOUND_MESSAGES = ".channel.options.org.jboss.remoting3.RemotingOptions.MAX_OUTBOUND_MESSAGES";
    protected static final String MAX_INBOUND_MESSAGES = ".channel.options.org.jboss.remoting3.RemotingOptions.MAX_INBOUND_MESSAGES";

    protected static final String SSL_STARTTLS = ".connect.options.org.xnio.Options.SSL_STARTTLS"; // true/false
    protected static final String SASL_POLICY_NOANONYMOUS = ".connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS"; // true/false
    protected static final String SASL_POLICY_NOPLAINTEXT = ".connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT"; // true/false
    protected static final String SASL_DISALLOWED_MECHANISMS = ".connect.options.org.xnio.Options.SASL_DISALLOWED_MECHANISMS"; // JBOSS-LOCAL-USER

    public EJBRemoteScopedContextConfig() {
    }

    @XmlAccessorType(XmlAccessType.FIELD)
	public static class ConnectionDefaults implements Serializable {

        @XmlAttribute(name="sslStartTls")
		protected Boolean sslStartTls = null;
        @XmlAttribute(name="saslPolicyNoAnonymous")
		protected Boolean saslPolicyNoAnonymous = null;
        @XmlAttribute(name="saslPolicyNoPlainText")
		protected Boolean saslPolicyNoPlainText = null;
        @XmlAttribute(name="saslDisallowedMechanism")
		protected String saslDisallowedMechanism = null;

        @XmlAttribute(name="maxInboundChannels")
		protected Integer maxInboundChannels = null;
        @XmlAttribute(name="maxOutboundChannels")
		protected Integer maxOutboundChannels = null;
        @XmlAttribute(name="maxInboundMessages")
		protected Integer maxInboundMessages = null;
        @XmlAttribute(name="maxOutboundMessages")
		protected Integer maxOutboundMessages = null;

		public Integer getMaxInboundChannels() {
			return maxInboundChannels;
		}
		public void setMaxInboundChannels(Integer maxInboundChannels) {
			this.maxInboundChannels = maxInboundChannels;
		}
		public Integer getMaxOutboundChannels() {
			return maxOutboundChannels;
		}
		public void setMaxOutboundChannels(Integer maxOutboundChannels) {
			this.maxOutboundChannels = maxOutboundChannels;
		}
		public Integer getMaxInboundMessages() {
			return maxInboundMessages;
		}
		public void setMaxInboundMessages(Integer maxInboundMessages) {
			this.maxInboundMessages = maxInboundMessages;
		}
		public Integer getMaxOutboundMessages() {
			return maxOutboundMessages;
		}
		public void setMaxOutboundMessages(Integer maxOutboundMessages) {
			this.maxOutboundMessages = maxOutboundMessages;
		}

		public Boolean getSslStartTls() {
			return sslStartTls;
		}
		public void setSslStartTls(Boolean sslStartTls) {
			this.sslStartTls = sslStartTls;
		}
		public Boolean getSaslPolicyNoAnonymous() {
			return saslPolicyNoAnonymous;
		}
		public void setSaslPolicyNoAnonymous(Boolean saslPolicyNoAnonymous) {
			this.saslPolicyNoAnonymous = saslPolicyNoAnonymous;
		}
		public Boolean getSaslPolicyNoPlainText() {
			return saslPolicyNoPlainText;
		}
		public void setSaslPolicyNoPlainText(Boolean saslPolicyNoPlainText) {
			this.saslPolicyNoPlainText = saslPolicyNoPlainText;
		}
		public String getSaslDisallowedMechanism() {
			return saslDisallowedMechanism;
		}
		public void setSaslDisallowedMechanism(String saslDisallowedMechanism) {
			this.saslDisallowedMechanism = saslDisallowedMechanism;
		}
	}


	public Connection createConnection() {
	    // create a connection, set the defaults
	    Connection connection = new Connection();

	    connection.setMaxInboundChannels(connectionDefaults.getMaxInboundChannels());
	    connection.setMaxOutboundChannels(connectionDefaults.getMaxOutboundChannels());
	    connection.setMaxInboundMessages(connectionDefaults.getMaxInboundMessages());
	    connection.setMaxOutboundMessages(connectionDefaults.getMaxOutboundMessages());

	    connection.setSaslDisallowedMechanism(connectionDefaults.getSaslDisallowedMechanism());
	    connection.setSaslPolicyNoAnonymous(connectionDefaults.getSaslPolicyNoAnonymous());
	    connection.setSaslPolicyNoPlainText(connectionDefaults.getSaslPolicyNoPlainText());
	    connection.setSslStartTls(connectionDefaults.getSslStartTls());

	    return connection;
	}

  public Connection createConnection(String host, String port, String username, String password) {
      Connection connection = createConnection();
      connection.setHost(host);
      connection.setPort(port);
      connection.setUsername(username);
      connection.setPassword(password);
      connection.setName("connection" + unnamedConnectionNumber);
      unnamedConnectionNumber ++;
      return connection;
  }

  @XmlAccessorType(XmlAccessType.FIELD)
	public static class Connection extends ConnectionDefaults implements Serializable {

      @XmlAttribute(name="name")
		private String name;
      @XmlAttribute(name="host")
		private String host;
      @XmlAttribute(name="port")
		private String port;
      @XmlAttribute(name="username")
		private String username;
      @XmlAttribute(name="password")
		private String password;

		private Connection() {

		}

		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getHost() {
			return host;
		}
		public void setHost(String host) {
			this.host = host;
		}
		public String getPort() {
			return port;
		}
		public void setPort(String port) {
			this.port = port;
		}
		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}

		public Properties getConfiguration() {
			Properties env = new Properties();
			// connection specific configurations

			if (sslStartTls != null)
				env.put(REMOTE_CONNECTION + name + SSL_STARTTLS, sslStartTls.toString());

			if (saslPolicyNoAnonymous != null)
				env.put(REMOTE_CONNECTION + name + SASL_POLICY_NOANONYMOUS, saslPolicyNoAnonymous.toString());
			if (saslPolicyNoPlainText != null)
				env.put(REMOTE_CONNECTION + name + SASL_POLICY_NOPLAINTEXT, saslPolicyNoPlainText.toString());
			if (saslDisallowedMechanism != null)
				env.put(REMOTE_CONNECTION + name + SASL_DISALLOWED_MECHANISMS, saslDisallowedMechanism.toString());

			// channels
			if(maxInboundChannels != null)
				env.put(REMOTE_CONNECTION + name + MAX_INBOUND_CHANNELS, maxInboundChannels.toString());
			if(maxOutboundChannels != null)
				env.put(REMOTE_CONNECTION + name + MAX_OUTBOUND_CHANNELS, maxOutboundChannels.toString());

			// messages
			if(maxInboundMessages != null)
				env.put(REMOTE_CONNECTION + name + MAX_INBOUND_MESSAGES, maxInboundMessages.toString());
			if(maxOutboundMessages != null)
				env.put(REMOTE_CONNECTION + name + MAX_OUTBOUND_MESSAGES, maxOutboundMessages.toString());

			// host / port
			if(host != null)
				env.put(REMOTE_CONNECTION + name + ".host", host);
			if(port != null)
				env.put(REMOTE_CONNECTION + name + ".port", port);

			// username / password
			if(username != null)
				env.put(REMOTE_CONNECTION + name + ".username", username);
			if(password != null)
				env.put(REMOTE_CONNECTION + name + ".password", password);

			return env;
		}
	}


  @XmlAccessorType(XmlAccessType.FIELD)
    public static class Cluster implements Serializable {

      @XmlAttribute(name="name")
        private String name;
      @XmlAttribute(name="username")
        private String username;
      @XmlAttribute(name="password")
        private String password;

      @XmlAttribute(name="saslPolicyNoAnonymous")
        protected Boolean saslPolicyNoAnonymous = null;
      @XmlAttribute(name="saslPolicyNoPlainText")
        protected Boolean saslPolicyNoPlainText = null;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Boolean getSaslPolicyNoAnonymous() {
            return saslPolicyNoAnonymous;
        }

        public void setSaslPolicyNoAnonymous(Boolean saslPolicyNoAnonymous) {
            this.saslPolicyNoAnonymous = saslPolicyNoAnonymous;
        }

        public Boolean getSaslPolicyNoPlainText() {
            return saslPolicyNoPlainText;
        }

        public void setSaslPolicyNoPlainText(Boolean saslPolicyNoPlainText) {
            this.saslPolicyNoPlainText = saslPolicyNoPlainText;
        }

        public Properties getConfiguration() {
            Properties env = new Properties();
            // connection specific configurations

            if (saslPolicyNoAnonymous != null)
                env.put(REMOTE_CLUSTER + name + SASL_POLICY_NOANONYMOUS, saslPolicyNoAnonymous.toString());
            if (saslPolicyNoPlainText != null)
                env.put(REMOTE_CLUSTER + name + SASL_POLICY_NOPLAINTEXT, saslPolicyNoPlainText.toString());

            // username / password
            if (username != null)
                env.put(REMOTE_CLUSTER + name + ".username", username);
            if (password != null)
                env.put(REMOTE_CLUSTER + name + ".password", password);

            return env;
        }
    }

   public void addCluster(String name, String username, String password, Boolean saslPolicyNoAnonymous, Boolean saslPolicyNoPlainText) {
       Cluster cluster = createCluster(name, username, password, saslPolicyNoAnonymous, saslPolicyNoPlainText);
       this.clusters.put(cluster.getName(), cluster);
   }

   public Cluster createCluster(String name, String username, String password, Boolean saslPolicyNoAnonymous, Boolean saslPolicyNoPlainText) {
       Cluster cluster = new Cluster();
       cluster.setUsername(username);
       cluster.setPassword(password);
       cluster.setName(name);
       cluster.setSaslPolicyNoAnonymous(saslPolicyNoAnonymous);
       cluster.setSaslPolicyNoPlainText(saslPolicyNoPlainText);
       return cluster;
   }

	public Boolean getSslEnabled() {
		return sslEnabled;
	}
	public void setSslEnabled(Boolean sslEnabled) {
		this.sslEnabled = sslEnabled;
	}

	public ConnectionDefaults getConnectionDefaults() {
		return connectionDefaults;
	}

	public void addConnection(Connection connection) {
		this.connections.put(connection.getName(), connection);
	}

	public Connection getConnection(String name) {
		return this.connections.get(name);
	}

	public Collection<Connection> getConnections() {
		return this.connections.values();
	}

	public void addConnection(String host, String port, String username, String password) {
		Connection connection = createConnection(host, port, username, password);
		this.connections.put(connection.getName(), connection);
	}
	   public void addConnection(String host, Integer port, String username, String password) {
	        Connection connection = createConnection(host, port.toString(), username, password);
	        this.connections.put(connection.getName(), connection);
	    }


	@Override
    public Properties getConfiguration() {
		Properties env = new Properties();

		// set the url prefix, so it will be recognized via the ejb client
		env.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");

		// create options
		env.put(SSL_ENABLED, sslEnabled.toString());

		// enable scoped context
		if (scopedContext)
			env.put("org.jboss.ejb.client.scoped.context", "true");

		// add the connections
		if(connections != null && connections.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for(Connection connection : connections.values()) {
				env.putAll(connection.getConfiguration());
				sb.append(connection.getName() + ",");
			}
			// add specify the connections to be used
			sb.setLength(sb.length()-1);
			env.put("remote.connections", sb.toString());
		}

	      // add the clusters
        if(clusters != null && clusters.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for(Cluster cluster : clusters.values()) {
                env.putAll(cluster.getConfiguration());
                sb.append(cluster.getName() + ",");
            }
            // add specify the connections to be used
            sb.setLength(sb.length()-1);
            env.put("remote.clusters", sb.toString());
        }

		return env;
	}

	@Override
    public void listConfiguration(PrintStream ps) {
	    Properties p = getConfiguration();
	    Enumeration<String> names = (Enumeration<String>) p.propertyNames();
	    Set<String> namesSet = new TreeSet<>();
	    while(names.hasMoreElements()) {
	        namesSet.add(names.nextElement());
	    }
	    for(String name : namesSet) {
	        ps.printf("%s=%s\n", name, p.getProperty(name));
	    }
	}

    @Override
    public Context getInitialContext() throws NamingException {
        return new InitialContext(getConfiguration());
    }

    @Override
    public void close(Context ctx) {
        try {
            if(ctx != null) {
                ((Context)ctx.lookup("ejb:")).close();
            }
        } catch(Exception e) { }
    }

    @Override
    public ConfigType getConfigType() {
        return EJBRemoteConfig.ConfigType.SCOPED_CONTEXT;
    }

	public boolean isScopedContext() {
		return scopedContext;
	}
	public void setScopedContext(boolean scopedContext) {
		this.scopedContext = scopedContext;
	}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(String.format("%s\n", this.getClass().getSimpleName()));
        Properties p = getConfiguration();
        Enumeration<String> names = (Enumeration<String>) p.propertyNames();
        Set<String> namesSet = new TreeSet<>();
        while(names.hasMoreElements()) {
            namesSet.add(names.nextElement());
        }
        for(String name : namesSet) {
            sb.append(String.format("%s=%s\n", name, p.getProperty(name)));
        }
        return sb.toString();
    }

}