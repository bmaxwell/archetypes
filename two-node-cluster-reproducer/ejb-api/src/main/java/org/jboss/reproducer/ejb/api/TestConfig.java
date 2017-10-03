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

import javax.ejb.TransactionAttributeType;

import org.jboss.reproducer.ejb.api.sfsb.SFSBRemote;
import org.jboss.reproducer.ejb.api.slsb.ClusterSLSBRemote;
import org.jboss.reproducer.ejb.api.slsb.SLSBRemote;

/**
 * @author bmaxwell
 *
 */
public class TestConfig implements Serializable {

    public static final String SECURITY_DOMAIN = "other";

    private static final String SecuritySLSB1_PACKAGE = "org.jboss.as.test.multinode.security.ejb";
    private static final String SecuritySLSB2_PACKAGE = "org.jboss.as.test.multinode.security.ejb";
    private static final String SecurityWebServlet_CLASS = "org.jboss.as.test.multinode.security.web.SecurityWebClientServlet";

//    public static EJBInfo SECURITY_EJB1 = new EJBInfo(null, "SecuritySLSB", "SecuritySLSB", SecuritySLSB1_PACKAGE, SecuritySLSBRemote.class);
//    public static EJBInfo SECURITY_EJB2 = new EJBInfo(null, "SecuritySLSB", "SecuritySLSB2", SecuritySLSB2_PACKAGE, SecuritySLSBRemote.class);
//
//    public static ServletInfo SERVLET_1 = new ServletInfo(SecurityWebServlet_CLASS, SECURITY_WEB_ROLE, SecuritySLSBRemote.class.getPackage(), EJBUtil.class.getPackage());


    public enum APPLICATION {

        // TODO these are all wrong:
        SLSBTest("SLSBTest"),
        SLSBTest_JBOSS_EJB_CLIENT_XML("SLSBTest-JBoss-EJB-Client-Xml"),
        SLSBTest_WILDFLY_CLIENT_XML("SLSBTest-Wildfly-Client-Xml"),

        // This is correct
        REPRODUCER("reproducer-ear");

        public String name;

        private APPLICATION(String name) {
            this.name = name;
        }
    }

    public enum SERVLET {
//        SERVLET_1(new ServletInfo(SecurityWebServlet_CLASS, "securityWebRole", ClusterSLSBRemote.class.getPackage(), EJBUtil.class.getPackage())),
        CLIENT1(new ServletInfo("org.jboss.reproducer.web.ClientServlet", "clientServlet1", null, null, ClusterSLSBRemote.class.getPackage(), EJBUtil.class.getPackage())),
        CLIENT2(new ServletInfo("org.jboss.reproducer.web.ClientServlet", "clientServlet2", null, null, ClusterSLSBRemote.class.getPackage(), EJBUtil.class.getPackage())),
        CLIENT3(new ServletInfo("org.jboss.reproducer.web.ClientServlet", "clientServlet3", null, null, ClusterSLSBRemote.class.getPackage(), EJBUtil.class.getPackage())),
        // SECURED_CLIENT3(new ServletInfo("org.jboss.reproducer.web.ClientServlet", "clientWebRole", "clientServlet3", ClusterSLSBRemote.class.getPackage(), EJBUtil.class.getPackage()));
        SECURED(new ServletInfo("org.jboss.reproducer.web.SecuredServlet", "secured", "other", "securityWebRole", ClusterSLSBRemote.class.getPackage(), EJBUtil.class.getPackage()));
        public ServletInfo info;
        private SERVLET(ServletInfo info) {
            this.info = info;
        }

        public static SERVLET fromContextPath(String contextRoot) {
            for(SERVLET s : SERVLET.values())
                if(s.info.getContextRoot().equals(contextRoot))
                    return s;
            return null;
        }

    }


    public enum EJBS {

//        reproducer-ear/reproducer-ejb/ClusterSLSBEJB!org.jboss.reproducer.ejb.api.slsb.ClusterSLSBRemote

        SLSB(new EJBInfo(APPLICATION.REPRODUCER.name, "reproducer-ejb", "SLSBEJB", "org.jboss.reproducer.ejb.slsb", SLSBRemote.class)),

        CLUSTERED_EJB1(new EJBInfo(APPLICATION.REPRODUCER.name, "reproducer-ejb", "ClusterSLSBEJB", "org.jboss.reproducer.ejb.slsb", ClusterSLSBRemote.class)),
        SFSB(new EJBInfo(APPLICATION.REPRODUCER.name, "reproducer-ejb", "SFSBEJB", "org.jboss.reproducer.ejb.sfsb", SFSBRemote.class)),

        // TODO these are all wrong
        CLUSTERED_EJB1_JBOSS_EJB_CLIENT_XML(new EJBInfo(APPLICATION.REPRODUCER.name, "SLSBEJB", "ClusterSLSBEJB", "org.jboss.reproducer.ejb.slsb", ClusterSLSBRemote.class)),
        CLUSTERED_EJB1_WILDFLY_CLIENT_XML(new EJBInfo(APPLICATION.REPRODUCER.name, "SLSBEJB", "ClusterSLSBEJB", "org.jboss.reproducer.ejb.slsb", ClusterSLSBRemote.class));

        public EJBInfo info;

        private EJBS(EJBInfo info) {
            this.info = info;
        }
    }

    public enum SERVER {
        UNAVAILABLE_NODE("127.0.0.2", 0, "unavailableNode"),
        NODE1("localhost", 0, "node1"),
        NODE2("localhost", 100, "node2"),
        NODE3("localhost", 150, "node3"),

        CLIENT_NODE("localhost", 0, "client"),
        CLUSTER1_NODE1("localhost", 100, "cluster1-node1"),
        CLUSTER1_NODE2("localhost", 150, "cluster1-node2"),
        CLUSTER2_NODE1("localhost", 200, "cluster2-node1"),
        CLUSTER2_NODE2("localhost", 250, "cluster2-node2");

        public String host;
        public Integer mgmtPort, httpPort, remotingPort;
        public String nodeName;

        // these use EAP 7 default ports
        private SERVER(String host, Integer portOffset, String nodeName) {
            this(host, 9990+portOffset, 8080+portOffset, 8080+portOffset, nodeName);
        }
        private SERVER(String host, Integer mgmtPort, Integer httpPort, Integer remotingPort, String nodeName) {
            this.host = host;
            this.mgmtPort = mgmtPort;
            this.httpPort = httpPort;
            this.remotingPort = remotingPort;
            this.nodeName = nodeName;
        }
    }

    public enum CREDENTIAL {
        // bin/add-user.sh -a -u ejbuser -p redhat1! -g guest
        EJBUSER("ejbuser", "redhat1!", "guest"),
        EJB("ejbuser", "redhat1!", "guest"),
        SERVLET1("webuser", "redhat1!", "guest"),
        ANONYMOUS("anonymous", null, null),
        NONE(null, null, new String[0]);

        public String username;
        public String password;
        public String[] roles;

        private CREDENTIAL(String username, String password, String...roles) {
            this.username = username;
            this.password = password;
            this.roles = roles;
            if(this.roles == null)
                this.roles = new String[0];
        }
    }

    public enum CLUSTER {
        CLUSTER1("ejb", "ejb", "redhat1!"),
        NONE(null, null, null);

        public String name;
        public String username;
        public String password;

        private CLUSTER(String name, String username, String password) {
            this.name = name;
            this.username = username;
            this.password = password;
        }
    }

    public enum Tx {
        MANDATORY(TransactionAttributeType.MANDATORY),
        NEVER(TransactionAttributeType.NEVER),
        NOT_SUPPORTED(TransactionAttributeType.NOT_SUPPORTED),
        REQUIRED(TransactionAttributeType.REQUIRED),
        REQUIRES_NEW(TransactionAttributeType.REQUIRES_NEW),
        SUPPORTS(TransactionAttributeType.SUPPORTS);
        private TransactionAttributeType txAttr;
        private Tx(TransactionAttributeType txAttr) {
            this.txAttr = txAttr;
        }
    }

    public enum TxMgmt {
        CMT,
        BMP;
    }

    public static class Credentials {
        private String username;
        private String password;
        private String[] roles;

        public Credentials(String username, String password) {
            this(username, password, new String[0]);
        }

        public Credentials(String username, String password, String...roles) {
            this.username = username;
            this.password = password;
            this.roles = roles;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getPrincipalName() {
            return username;
        }
        public String[] getRoles() {
            return roles;
        }
    }

    public static class ExpectedIdentity {

        private String principalName;
        private String[] roles;

        public ExpectedIdentity(String principalName, String...roles) {
            this.principalName = principalName;
            this.roles = roles;
        }

        public String getPrincipalName() {
            return principalName;
        }

        public String[] getRoles() {
            return roles;
        }
    }
}