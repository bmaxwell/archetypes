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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jboss.reproducer.ejb.api.TestConfig;
import org.jboss.reproducer.ejb.api.path.InvocationPath;

/**
 * @author bmaxwell
 *
 */
public class ExpectedResult implements Serializable {

    private Boolean expectClustered;
    private Boolean expectSticky;
    private Integer expectedUniqueNodesInvoked;

    private List<InvocationPath> expectedPath = new ArrayList<>();

    public static ExpectedResult builder() {
        return new ExpectedResult();
    }
    public ExpectedResult addInvocationPath(InvocationPath invocationPath) {
        this.expectedPath.add(invocationPath);
        return this;
    }
    public ExpectedResult addInvocationPath(String nodeName, String principalName) {
        this.expectedPath.add(new InvocationPath(nodeName, principalName));
        return this;
    }
    public ExpectedResult addInvocationPath(TestConfig.SERVER server, TestConfig.CREDENTIAL principal) {
        this.expectedPath.add(new InvocationPath(server.nodeName, principal.username));
        return this;
    }

    public ExpectedResult expectClustered() {
        this.expectClustered = true;
        return this;
    }
    public ExpectedResult expectNotClustered() {
        this.expectClustered = false;
        return this;
    }
    public ExpectedResult expectSticky() {
        this.expectSticky = true;
        return this;
    }
    public ExpectedResult expectNotSticky() {
        this.expectSticky = false;
        return this;
    }
    public Boolean getExpectClustered() {
        return expectClustered;
    }
    public void setExpectClustered(Boolean expectClustered) {
        this.expectClustered = expectClustered;
    }
    public Boolean getExpectSticky() {
        return expectSticky;
    }
    public void setExpectSticky(Boolean expectSticky) {
        this.expectSticky = expectSticky;
    }
    public List<InvocationPath> getExpectedPath() {
        return expectedPath;
    }
    public void setExpectedPath(List<InvocationPath> expectedPath) {
        this.expectedPath = expectedPath;
    }
    public Integer getExpectedUniqueNodesInvoked() {
        return expectedUniqueNodesInvoked;
    }
    public void setExpectedUniqueNodesInvoked(Integer expectedUniqueNodesInvoked) {
        this.expectedUniqueNodesInvoked = expectedUniqueNodesInvoked;
    }

}