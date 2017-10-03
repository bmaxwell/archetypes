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
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author bmaxwell
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ClusterInfo implements Serializable {

    @XmlAttribute(name="name")
    private String name;

    @XmlAttribute(name="this-node")
    private String thisNode;

    @XmlAttribute(name="coordinator-node")
    private String coordinatorNode;

    @XmlElement(name="members")
    private Set<String> members = new TreeSet<>();

    public ClusterInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThisNode() {
        return thisNode;
    }

    public void setThisNode(String thisNode) {
        this.thisNode = thisNode;
    }

    public String getCoordinatorNode() {
        return coordinatorNode;
    }

    public void setCoordinatorNode(String coordinatorNode) {
        this.coordinatorNode = coordinatorNode;
    }

    public Set<String> getMembers() {
        return members;
    }

    public void setMembers(Set<String> members) {
        this.members = members;
    }

    @Override
    public String toString() {
        return String.format("Cluster: %s members: %s", this.getName(), getMembers());
    }
}