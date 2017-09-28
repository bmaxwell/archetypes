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

import javax.xml.bind.annotation.XmlSeeAlso;

import org.jboss.reproducer.ejb.api.EJBRequest;

/**
 * @author bmaxwell
 *
 */
@XmlSeeAlso({WorkflowAction.class, EJBAction.class})
public interface Action {

    public EJBRequest invoke(EJBRequest ejbRequest) throws Exception;
    public boolean isWorkflowAction();

}