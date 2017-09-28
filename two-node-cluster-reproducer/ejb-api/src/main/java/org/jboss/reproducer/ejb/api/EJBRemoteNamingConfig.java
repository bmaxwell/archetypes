/*
 * Copyright 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.reproducer.ejb.api;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.LinkedHashSet;
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

import org.jboss.ejb.client.EJBClient;

/**
 * @author bmaxwell
 *
 */
@XmlRootElement(name="ejb-remote-naming-config", namespace="http://redhat.com/naming")
@XmlAccessorType(XmlAccessType.FIELD)
public class EJBRemoteNamingConfig implements EJBRemoteConfig, Serializable {

    // TODO add multiple provider support
    private static final long serialVersionUID = -7069808435982297185L;

    public enum Version{
        RemoteNamingInitialContextFactory("remote", "org.jboss.naming.remote.client.InitialContextFactory"),
        RemoteNamingHttpInitialContextFactory("http-remoting", "org.jboss.naming.remote.client.InitialContextFactory"),
        WildflyInitialContextFactory("remote+http", "org.wildfly.naming.client.WildFlyInitialContextFactory"),
        None(null, null);

        private String initialContextFactoryClass;
        private String protocol;

        private Version(String protocol, String initialContextFactoryClass) {
            this.protocol = protocol;
            this.initialContextFactoryClass = initialContextFactoryClass;
        }
        public String getInitialContextFactoryClass() {
            return initialContextFactoryClass;
        }
        public String getProtocol() {
            return protocol;
        }
    }

    @XmlAttribute(name="version")
    private Version version;
    @XmlAttribute(name="username")
    private String username;
    @XmlAttribute(name="password")
    private String password;
    @XmlAttribute(name="host")
    private String host;
    @XmlAttribute(name="port")
    private Integer port;
    @XmlAttribute(name="clusterName")
    private String clusterName = null; // defaulting since it is always ejb unless server config is changed
    @XmlAttribute(name="invocationTimeout")
    private Long invocationTimeout;

    public EJBRemoteNamingConfig() {
    }

    public EJBRemoteNamingConfig(Version version) {
        this.version = version;
    }

    public EJBRemoteNamingConfig(TestConfig.SERVER server, TestConfig.CREDENTIAL credentials) {
        this(Version.WildflyInitialContextFactory, server, credentials);
    }

    public EJBRemoteNamingConfig(Version version, TestConfig.SERVER server, TestConfig.CREDENTIAL credentials) {
        this(version);
        setHost(server.host);
        setPort(server.remotingPort);
        setUsername(credentials.username);
        setPassword(credentials.password);
    }

    @XmlElement(name="provider-url")
    private Set<NamingProviderUrl> providerUrls = new LinkedHashSet<>();

    public Long getInvocationTimeout() {
        return invocationTimeout;
    }

    public void setInvocationTimeout(Long invocationTimeout) {
        this.invocationTimeout = invocationTimeout;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
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

    public void setUsernamePassword(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void setHostPort(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    public String getProviderUrl() {
        return String.format("%s://%s:%d", version.getProtocol(), getHost(), getPort());
    }

    public void addProvider(Version version, String host, Integer port) {
        this.providerUrls.add(new NamingProviderUrl(version, host, port));
    }
    public void addProvider(String host, Integer port) {
        this.addProvider(this.version, host, port);
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
    public Properties getConfiguration() {
        Properties env = new Properties();

        // add jboss.naming.client.ejb.context=true if EAP 6/7.0 RemoteNaming, it is not needed with WildflyInitialContex
        if(version == Version.RemoteNamingInitialContextFactory || version == Version.RemoteNamingHttpInitialContextFactory) {
            env.put("jboss.naming.client.ejb.context", "true");
            env.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        }
        if(version.getInitialContextFactoryClass() != null)
            env.put("java.naming.factory.initial", version.getInitialContextFactoryClass());

        Set<String> providerUrlsSet = new LinkedHashSet<>();

        if(version.getProtocol() != null && getHost() != null && getPort() != null)
            providerUrlsSet.add(getProviderUrl());

        for(NamingProviderUrl npu : this.providerUrls)
            providerUrlsSet.add(npu.getProviderUrl());

        if(providerUrlsSet != null)
            env.put("java.naming.provider.url", String.join(",", providerUrlsSet));

        if(getClusterName() != null)
            env.put(EJBClient.CLUSTER_AFFINITY, getClusterName());

        if(getInvocationTimeout() != null) {
            env.put("invocation.timeout", getInvocationTimeout().toString());
        }

        if(getUsername() != null)
            env.put(Context.SECURITY_PRINCIPAL, getUsername());
        if(getPassword() != null)
            env.put(Context.SECURITY_CREDENTIALS, getPassword());
        return env;
    }

    @Override
    public Context getInitialContext() throws NamingException {
//        System.out.println("*** Remote Naming config before creating context ***");
//        listConfiguration(System.out);
//        System.out.println("*** Remote Naming config before creating context ***");
//        System.out.flush();
        return new InitialContext(getConfiguration());
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class NamingProviderUrl implements Serializable{
        @XmlAttribute(name="version")
        private Version version;
        @XmlAttribute(name="host")
        private String host;
        @XmlAttribute(name="port")
        private Integer port;

        public NamingProviderUrl() {

        }
        public NamingProviderUrl(Version version, String host, Integer port) {
            this.version = version;
            this.host = host;
            this.port = port;
        }
        public String getHost() {
            return host;
        }
        public void setHost(String host) {
            this.host = host;
        }
        public Integer getPort() {
            return port;
        }
        public void setPort(Integer port) {
            this.port = port;
        }
        public String getProviderUrl() {
            return String.format("%s://%s:%d", version.getProtocol(), getHost(), getPort());
        }
    }

    @Override
    public ConfigType getConfigType() {
        switch(version) {
            case RemoteNamingInitialContextFactory:
                return EJBRemoteConfig.ConfigType.REMOTE_NAMING;
            case RemoteNamingHttpInitialContextFactory:
                return EJBRemoteConfig.ConfigType.REMOTE_NAMING;
            case WildflyInitialContextFactory:
                return EJBRemoteConfig.ConfigType.WILDFLY_NAMING;
            default:
                return EJBRemoteConfig.ConfigType.IN_VM;
        }
    }

    @Override
    public void close(Context ctx) {
        if(ctx != null) {
            try {
                ctx.close();
            } catch(Exception e) {

            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(String.format("%s - %s\n", this.getClass().getSimpleName(), this.version));
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