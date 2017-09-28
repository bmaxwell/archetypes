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
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * @author bmaxwell
 *
 */
@XmlSeeAlso({EJBRemoteNamingConfig.class, EJBRemoteScopedContextConfig.class})
public interface EJBRemoteConfig extends Serializable {

    public enum ConfigType {
        REMOTE_NAMING,
        WILDFLY_NAMING,
        SCOPED_CONTEXT,
        REMOTING_EJB_RECEIVER,
        IN_VM;
    }

    public Properties getConfiguration();
    public Context getInitialContext() throws NamingException;
    public void close(Context ctx);
    public void listConfiguration(PrintStream ps);
    public ConfigType getConfigType();
}