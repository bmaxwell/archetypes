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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author bmaxwell
 *
 */
@XmlRootElement(name = "expected-path")
@XmlAccessorType(XmlAccessType.FIELD)
public class ExpectedPath extends AbstractPath implements Serializable {

    public ExpectedPath() {
        super();
    }

    public ExpectedPath(String nodeName, String principalName) {
        super(nodeName, principalName, null);
    }

    public ExpectedPath(String nodeName, String principalName, String clusterName) {
        super(nodeName, principalName, clusterName);
    }
}