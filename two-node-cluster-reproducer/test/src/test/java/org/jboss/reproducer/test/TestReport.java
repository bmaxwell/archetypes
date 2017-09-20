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

package org.jboss.reproducer.test;

import java.io.File;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.jboss.reproducer.ejb.api.EJBRequest;

/**
 * @author bmaxwell
 *
 */
@XmlRootElement(name="test-report")
@XmlAccessorType(XmlAccessType.FIELD)
public class TestReport implements Serializable {

    public enum ClientType {
        StandaloneApp,
        Deployment
    }
    public enum EjbConfigMethod {
        RemoteNaming,
        WildflyNaming,
        WildflyConfigXml,
        EjbClientScopedContext,
        JBossEjbClientXml;
    }

    @XmlAttribute(name="name")
    private String name;

    @XmlAttribute(name="client-type")
    private ClientType clientType;

    @XmlAttribute(name="ejb-config-method")
    private EjbConfigMethod ejbConfigMethod;

    @XmlElement(name="ejb-lookup-path")
    private String ejbLookupPath;

    @XmlElement(name="configuration")
    @XmlJavaTypeAdapter(PropertiesAdapter.class)
    private Properties configuration;

    @XmlElement(name="exception")
    @XmlJavaTypeAdapter(ThrowableAdapter.class)
    private Throwable exception;

    @XmlElement(name="expected-success")
    private boolean expectedSuccess = true;

    @XmlElement(name="success")
    private boolean success = true;

    @XmlElement(name="response")
    private EJBRequest response;

    // client Type: standalone / deployment
    // remote ejb lookup method:
    // configuration:
    // ejb lookup: ...
    // exception:
    // success / fail
    // expected success / fail

    public TestReport() {

    }
    public TestReport(String name, ClientType clientType, EjbConfigMethod ejbConfigMethod, String ejbLookupPath) {
        this.name = name;
        this.clientType = clientType;
        this.ejbConfigMethod = ejbConfigMethod;
        this.ejbLookupPath = ejbLookupPath;
    }

    public void marshall(File output) throws JAXBException {
        JAXBContext ctx = JAXBContext.newInstance(TestReport.class);
        Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(this, output);
    }

    public void marshall() throws JAXBException {
        marshall(new File(name + ".xml"));
    }
    public void marshallSafe() {
        try {
            marshall(new File(name + ".xml"));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public ClientType getClientType() {
        return clientType;
    }
    public void setClientType(ClientType clientType) {
        this.clientType = clientType;
    }
    public EjbConfigMethod getEjbConfigMethod() {
        return ejbConfigMethod;
    }
    public void setEjbConfigMethod(EjbConfigMethod ejbConfigMethod) {
        this.ejbConfigMethod = ejbConfigMethod;
    }
    public String getEjbLookupPath() {
        return ejbLookupPath;
    }
    public void setEjbLookupPath(String ejbLookupPath) {
        this.ejbLookupPath = ejbLookupPath;
    }
    public Properties getConfiguration() {
        return configuration;
    }
    public void setConfiguration(Properties configuration) {
        this.configuration = configuration;
    }
    public Throwable getException() {
        return exception;
    }
    public void setException(Throwable exception) {
        this.exception = exception;
        setSuccess(false);
    }
    public boolean isExpectedSuccess() {
        return expectedSuccess;
    }
    public void setExpectedSuccess(boolean expectedSuccess) {
        this.expectedSuccess = expectedSuccess;
    }
    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean shouldFail() {
        return expectedSuccess != success;
    }

    public EJBRequest getResponse() {
        return response;
    }
    public void setResponse(EJBRequest response) {
        this.response = response;
    }

    public static class ThrowableAdapter extends XmlAdapter<String, Throwable> {
        private HexBinaryAdapter hexAdapter = new HexBinaryAdapter();

        @Override
        public String marshal(Throwable v) throws Exception {
            StringWriter sw = new StringWriter();
            v.printStackTrace(new PrintWriter(sw));
            return sw.toString();
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            ObjectOutputStream oos = new ObjectOutputStream(baos);
//            oos.writeObject(v);
//            oos.close();
//            byte[] serializedBytes = baos.toByteArray();
//            return hexAdapter.marshal(serializedBytes);
        }

        @Override
        public Throwable unmarshal(String v) throws Exception {
            // this is a hack if we really need to unmarshall, would need to recreate the stack
            return new Throwable(v);
//            byte[] serializedBytes = hexAdapter.unmarshal(v);
//            ByteArrayInputStream bais = new ByteArrayInputStream(serializedBytes);
//            ObjectInputStream ois = new ObjectInputStream(bais);
//            Throwable result = (Throwable) ois.readObject();
//            return result;
        }
    }

    public static class PropertiesAdapter extends XmlAdapter<String, Properties> {
        private HexBinaryAdapter hexAdapter = new HexBinaryAdapter();

        @Override
        public String marshal(Properties v) throws Exception {
            StringBuilder sb = new StringBuilder();
            Enumeration<String> en = (Enumeration<String>) v.propertyNames();
            while(en.hasMoreElements()) {
                String key = en.nextElement();
                sb.append(String.format("%s=%s\n", key, v.getProperty(key)));
            }
            return sb.toString();
        }

        @Override
        public Properties unmarshal(String v) throws Exception {
            Properties properties = new Properties();
            String lines[] = v.split("\n");
            for(String line : lines) {
                String split[] = line.split("=");
                properties.put(split[0], split[1]);
            }
            return properties;
        }
    }
}