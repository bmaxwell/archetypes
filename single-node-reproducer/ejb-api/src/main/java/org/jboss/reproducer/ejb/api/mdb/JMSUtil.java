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

package org.jboss.reproducer.ejb.api.mdb;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.NamingException;

/**
 * @author bmaxwell
 * Note: you should not call close on any of the resources directly, call jmsUtil.close() so that it can keep track of what is closed
 */
public class JMSUtil {

    public static final String DEFAULT_CONNECTION_FACTORY_LOOKUP = "jms/RemoteConnectionFactory";
    private ConnectionFactory connectionFactory;
    private String connectionFactoryLookup = DEFAULT_CONNECTION_FACTORY_LOOKUP;
    private Destination destination;
    private Context context;
    private String jmsUsername;
    private String jmsPassword;
    private Connection connection;
    private Session session;
    private MessageProducer messageProducer;
    private String destinationLookup;

    public String getDestinationLookup() {
        return destinationLookup;
    }

    public void setDestinationLookup(String destinationLookup) {
        this.destinationLookup = destinationLookup;
    }

    public JMSUtil(Context context) {
        this.context = context;
    }

    public ConnectionFactory lookupConnectionFactory() throws NamingException {
        if(this.connectionFactory == null)
            this.connectionFactory = (ConnectionFactory) getContext().lookup(getConnectionFactoryLookup());
        return this.connectionFactory;
    }

    public String getConnectionFactoryLookup() {
        if(this.connectionFactoryLookup == null)
            return DEFAULT_CONNECTION_FACTORY_LOOKUP;
        return this.connectionFactoryLookup;
    }

    public Context getContext() {
        return context;
    }

    public Connection getConnection() throws JMSException, NamingException {
        return connect();
    }

    public Connection connect() throws JMSException, NamingException {
        return connect(jmsUsername, jmsPassword);
    }

    public void setJMSCredentials(String jmsUsername, String jmsPassword) {
        this.jmsUsername = jmsUsername;
        this.jmsPassword = jmsPassword;
    }

    private static void closeSafe(Connection closeable) {
        if(closeable != null) {
            try {
                closeable.close();
            } catch(Exception e) { }
        }
    }
    private static void closeSafe(MessageProducer closeable) {
        if(closeable != null) {
            try {
                closeable.close();
            } catch(Exception e) { }
        }
    }
    private static void closeSafe(Session closeable) {
        if(closeable != null) {
            try {
                closeable.close();
            } catch(Exception e) { }
        }
    }

    public void close() {
        closeSafe(messageProducer);
        this.messageProducer = null;
        closeSafe(session);
        this.session = null;
        closeSafe(connection);
        this.connection = null;
    }

    public ConnectionFactory getConnectionFactory() throws NamingException {
        if(this.connectionFactory == null) {
            this.connectionFactory = lookupConnectionFactory();
        }
        return this.connectionFactory;
    }

    public Connection connect(String jmsUsername, String jmsPassword) throws JMSException, NamingException {
        if(this.connection == null) {
            if(jmsUsername != null && jmsPassword != null)
                this.connection = getConnectionFactory().createConnection(jmsUsername, jmsPassword);
            else
                this.connection = getConnectionFactory().createConnection();
        }
        return this.connection;
    }

    public Session getSession() throws JMSException, NamingException {
        return createSession();
    }

    public Session createSession() throws JMSException, NamingException {
        if(this.session == null) {
            this.session = getConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);
        }
        return this.session;
    }

    public MessageProducer getProducer() throws JMSException, NamingException {
        if(this.messageProducer == null) {
            this.messageProducer = getSession().createProducer(getDestination());
        }
        return this.messageProducer;
    }

    public Destination getDestination() throws NamingException {
        return lookupDestination(destinationLookup);
    }

    public Destination lookupDestination(String destinationLookup) throws NamingException {
        if(this.destination == null)
            this.destination = (Destination) getContext().lookup(destinationLookup);
        return this.destination;
    }

    public void sendTextMessage(String textMessageString) throws JMSException, NamingException {
        TextMessage textMessage = getSession().createTextMessage(textMessageString);
        getProducer().setDeliveryMode(DeliveryMode.PERSISTENT);
        getConnection().start(); // start is ignored if already started
        getProducer().send(textMessage);
    }

    public void sendMessage(Message message) throws JMSException, NamingException {
        getProducer().setDeliveryMode(DeliveryMode.PERSISTENT);
        getConnection().start(); // start is ignored if already started
        getProducer().send(message);
    }
}