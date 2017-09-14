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

package org.jboss.reproducer.ejb.api.slsb;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author bmaxwell
 *
 */
public class EJBInvocationSummary implements Serializable {

    private EJBRequest response;

    public EJBInvocationSummary(EJBRequest response) {
        this.response = response;
    }

    public boolean isSticky() {
        String nodeName = null;
        for(InvocationPath invocationPath : response.getInvocationPath()) {
            if(nodeName == null)
                nodeName = invocationPath.getNodeName();
            else
                if(!nodeName.equals(invocationPath.getNodeName()))
                        return false;
        }
        return true;
    }
    public Collection<Integer> getInvocationsPerNode() {
        Map<String, Integer> map = new HashMap<>();
        for(InvocationPath invocationPath : response.getInvocationPath()) {
            Integer i = map.get(invocationPath.getNodeName());
            if(i == null)
                i = 1;
            else
                i++;
            map.put(invocationPath.getNodeName(), i);
        }
        return map.values();
    }
}