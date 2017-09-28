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

import java.io.Serializable;
import java.util.Properties;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author bmaxwell
 *
 */
@XmlRootElement(name="remote-ejb-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteEJBConfig implements Serializable {

    private static final long serialVersionUID = -8489192600396323793L;

    public enum ConfigType {
        REMOTE_NAMING,
        WILDFLY_NAMING,
        SCOPED_CONTEXT,
        REMOTING_EJB_RECEIVER,
        IN_VM;
    }

    @XmlAttribute(name="host")
    private String host = "localhost";
    @XmlAttribute(name="port")
    private Integer port = 8080;
    @XmlAttribute(name="username")
    private String username;
    @XmlAttribute(name="password")
    private String password;
    @XmlAttribute(name="remote")
    private boolean remote = true;
    @XmlAttribute(name="jboss-ejb-client-xml")
    private boolean jbossEjbClientXml = false;
    @XmlAttribute(name="node-name")
    private String nodeName;
    @XmlAttribute(name="config-type")
    private ConfigType configType;

    @XmlElement(name="environment")
    private Properties environment = new Properties();

    @XmlElement(name="ejb-remote-config")
    private EJBRemoteConfig ejbRemoteConfig;

    public RemoteEJBConfig() {
    }

//    public RemoteEJBConfig(ServerConfig remoteServerConfig, Credentials credentials) {
//        this(remoteServerConfig.getHost(), remoteServerConfig.getRemotingPort(), credentials.getUsername(), credentials.getPassword());
//    }

    public RemoteEJBConfig(TestConfig.SERVER server, TestConfig.CREDENTIAL credentials) {
        this(server.host, server.remotingPort, credentials.username, credentials.password);
    }

    public RemoteEJBConfig(String host, Integer port, String username, String password) {
        if(host != null)
            this.host = host;
        if(port != null)
            this.port = port;
        if(username != null)
            this.username = username;
        if(password != null)
            this.password = password;
    }

    // this will not work with just client jars
//    public RemoteEJBConfig(ServletRequest request) {
//        String value = request.getParameter("ejbRemote");
//        if(value != null)
//            remote = true;
//        value = request.getParameter("jbossEjbClientXml");
//        if(value != null)
//            jbossEjbClientXml = true;
//        value = request.getParameter("ejbRemoteHost");
//        if(value != null)
//            host = value;
//        value = request.getParameter("ejbRemotePort");
//        if(value != null)
//            port = Integer.parseInt(value);
//        value = request.getParameter("ejbRemoteUsername");
//        if(value != null)
//            username = value;
//        value = request.getParameter("ejbRemotePassword");
//        if(value != null)
//            password = value;
//    }

    public boolean isJbossEjbClientXml() {
        return jbossEjbClientXml;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isRemote() {
        return remote;
    }

    public String getNodeName() {
        return nodeName;
    }

    public Properties getEnvironment() {
        return environment;
    }

    public String getURLParams() {
        StringBuilder sb = new StringBuilder();
        sb.append("ejbRemote=true&");
        if(host != null)
            sb.append("ejbRemoteHost=" + host + "&");
        if(port != null)
            sb.append("ejbRemotePort=" + port + "&");
        if(username != null)
            sb.append("ejbRemoteUsername=" + username + "&");
        if(password != null)
            sb.append("ejbRemotePassword=" + password + "&");
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public ConfigType getConfigType() {
        return configType;
    }

    public void setConfigType(ConfigType configType) {
        this.configType = configType;
    }

//    public Context getInitialContext() throws NamingException {
//
//        if(ejbRemoteConfig != null)
//            return ejbRemoteConfig.getInitialContext();

//        switch(configType) {
//            case REMOTE_NAMING:
//                EJBRemoteNamingConfig remoteNamingConfig = new EJBRemoteNamingConfig(Version.RemoteNamingHttpInitialContextFactory);
//                remoteNamingConfig.setHost(getHost());
//                remoteNamingConfig.setPort(getPort());
//                remoteNamingConfig.setUsername(getUsername());
//                remoteNamingConfig.setPassword(getPassword());
//                return new InitialContext(remoteNamingConfig.getConfiguration());
//            case WILDFLY_NAMING:
//                EJBRemoteNamingConfig config = new EJBRemoteNamingConfig(Version.WildflyInitialContextFactory);
//                config.setHost(getHost());
//                config.setPort(getPort());
//                config.setUsername(getUsername());
//                config.setPassword(getPassword());
//                return new InitialContext(config.getConfiguration());
//            case SCOPED_CONTEXT:
//                EJBRemoteScopedContextConfig scopedContextConfig = new EJBRemoteScopedContextConfig();
//                scopedContextConfig.addConnection(getHost(), getPort(), getUsername(), getPassword());//
////                scopedContextConfig.addCluster(cluster.name, cluster.username, cluster.password, false, false); // TODO later
//                scopedContextConfig.setScopedContext(true);
//                scopedContextConfig.getConfiguration().list(System.out);
//                return new InitialContext(scopedContextConfig.getConfiguration());
//            case REMOTING_EJB_RECEIVER:
//                Properties env = new Properties();
//                env.putAll(this.getEnvironment());
//                env.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
//                return new InitialContext(env);
//            case IN_VM:
//                return new InitialContext(getEnvironment());
//            default:
//                return new InitialContext(getEnvironment());
//        }
//    }

    public EJBRemoteConfig getEjbRemoteConfig() {
        return ejbRemoteConfig;
    }

    public void setEjbRemoteConfig(EJBRemoteConfig ejbRemoteConfig) {
        this.ejbRemoteConfig = ejbRemoteConfig;
    }

    @Override
    public String toString() {
        return String.format("configType=%s remote=%s host=%s port=%d user=%s pass=%s jbossEjbClientXml=%s", configType, remote, host, port, username, password, jbossEjbClientXml);
    }
}