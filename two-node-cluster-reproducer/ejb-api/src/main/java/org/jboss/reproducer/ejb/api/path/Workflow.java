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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.reproducer.ejb.api.EJBAction;
import org.jboss.reproducer.ejb.api.EJBRequest;
import org.jboss.reproducer.ejb.api.RemoteEJBConfig;
import org.jboss.reproducer.ejb.api.TestConfig;

/**
 * @author bmaxwell
 *
 */
@XmlRootElement(name="workflow")
@XmlAccessorType(XmlAccessType.FIELD)
public class Workflow implements Serializable {

    private static final long serialVersionUID = 2328222418770622921L;

    @XmlElement(name="ejb-action")
    private List<EJBAction> actions = new ArrayList<>();

    @XmlElement(name="invocation-path")
    private List<InvocationPath> invocationPath = new ArrayList<>();

    @XmlElement(name="expected-invocation-path")
    private List<ExpectedPath> expectedInvocationPath = new ArrayList<>();

    @XmlAttribute(name="current-action-index")
    private Integer currentActionIndex = 0;

    @XmlElement(name="expectations")
    private Set<Expectation> expectations = new HashSet<>();

    public Workflow() {
    }

    public Workflow(Expectation... expectations) {
        this.expectations.addAll(Arrays.asList(expectations));
    }

    public Workflow(EJBAction...actions) {
        for(EJBAction action : actions)
            this.actions.add(action);
    }

    public List<EJBAction> getActions() {
        return actions;
    }

    public Workflow addAction(RemoteEJBConfig remoteEJBConfig, TestConfig.EJBS ejbToInvoke, String...expectedRoles) {
        this.actions.add(new EJBAction(remoteEJBConfig, ejbToInvoke.info, expectedRoles));
        return this;
    }

    public Workflow addAction(boolean resuseCachedInitialContextIfAvailable, RemoteEJBConfig remoteEJBConfig, TestConfig.EJBS ejbToInvoke, String...expectedRoles) {
        this.actions.add(new EJBAction(resuseCachedInitialContextIfAvailable, remoteEJBConfig, ejbToInvoke.info, expectedRoles));
        return this;
    }

    public Workflow addAction(boolean resuseCachedInitialContextIfAvailable, RemoteEJBConfig remoteEJBConfig, TestConfig.EJBS ejbToInvoke, TestConfig.Tx tx, String...expectedRoles) {
        this.actions.add(new EJBAction(resuseCachedInitialContextIfAvailable, remoteEJBConfig, ejbToInvoke.info, tx, expectedRoles));
        return this;
    }


    public List<ExpectedPath> getExpectedInvocationPath() {
        return expectedInvocationPath;
    }

    public EJBRequest invokeNextAction(EJBRequest ejbRequest) throws Exception {
        try {
            System.out.println("Workflow invoking action: " + currentActionIndex);
            return actions.get(currentActionIndex).invoke(ejbRequest);
        } finally {
            // currentActionIndex++;
        }
    }

    public void addCurrentInvocationPathAndIncrementActionIndex(InvocationPath invocationPath) {
        this.invocationPath.add(invocationPath);
        currentActionIndex++;
        System.out.println("Incremented CurrentActionIndex to: " + currentActionIndex);
    }

    public boolean hasNextAction() {
        return currentActionIndex < this.actions.size();
    }

    public EJBAction getCurrentAction() {
        return this.getActions().get(currentActionIndex);
    }

    public void resetActionIndex() {
        this.currentActionIndex = 0;
    }

    public List<InvocationPath> getInvocationPath() {
        return invocationPath;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int i = 0, j = 0;
        sb.append(String.format("Workflow\n", i));
        for (EJBAction action : this.getActions()) {
            sb.append(String.format("- [%d] Action: %s\n", j, action.toString()));
            j++;
        }
        i++;
        return sb.toString();
    }
}