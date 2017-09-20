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

package org.jboss.reproducer.ejb.api.path;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author bmaxwell
 *
 */
@XmlRootElement(name = "invocation-path")
@XmlAccessorType(XmlAccessType.FIELD)
public class AbstractPath implements Serializable {

    @XmlAttribute(name = "node-name")
    private String nodeName;

    @XmlAttribute(name = "principal-name")
    private String principalName;

    @XmlAttribute(name = "cluster-name")
    private String clusterName;

    @XmlElement(name="expected-roles")
    private Set<String> expectedRoles = new HashSet<>();

    protected AbstractPath() {
    }

    protected AbstractPath(String nodeName, String principalName) {
        this(nodeName, principalName, null);
    }

    protected AbstractPath(String nodeName, String principalName, String clusterName) {
        this.nodeName = nodeName;
        this.principalName = principalName;
        this.clusterName = clusterName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Set<String> getExpectedRoles() {
        return expectedRoles;
    }

    public void setExpectedRoles(Set<String> expectedRoles) {
        this.expectedRoles = expectedRoles;
    }
}