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

/**
 * @author bmaxwell
 *
 */
public class TransactionInfo implements Serializable {

    private String key;
    private boolean active;
    private boolean rollbackOnly;
    private String status;

    private String keyStyle = "";
    private String activeStyle = "";
    private String rollbackOnlyStyle=  "";
    private String StatusStyle = "";

    public String getKeyStyle() {
        return keyStyle;
    }

    public void setKeyStyle(String keyStyle) {
        this.keyStyle = keyStyle;
    }

    public String getActiveStyle() {
        return activeStyle;
    }

    public void setActiveStyle(String activeStyle) {
        this.activeStyle = activeStyle;
    }

    public String getRollbackOnlyStyle() {
        return rollbackOnlyStyle;
    }

    public void setRollbackOnlyStyle(String rollbackOnlyStyle) {
        this.rollbackOnlyStyle = rollbackOnlyStyle;
    }

    public String getStatusStyle() {
        return StatusStyle;
    }

    public void setStatusStyle(String statusStyle) {
        StatusStyle = statusStyle;
    }

    /**
     *
     */
    public TransactionInfo() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setKey(Object key) {
        if(key != null) {
            this.key = key.toString();
        } else {
            this.key = "null";
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isRollbackOnly() {
        return rollbackOnly;
    }

    public void setRollbackOnly(boolean rollbackOnly) {
        this.rollbackOnly = rollbackOnly;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStatus(TransactionStatus status) {
        if(status != null) {
            this.status = status.toString();
        } else {
            this.status = "null";
        }
    }

//    @Override
//    public String toString() {
//        String msg = "";
//        msg = msg + "transactionKey: " + key;
//        msg = msg + ", tx is active ";
//        msg = msg + ", rollbackOnly: " + isRollbackOnly();
//        msg = msg + ", transactionStatus: " + isActive();
//        return msg;
//    }

    @Override
    public String toString() {
        return String.format("status: %s rollbackOnly: %s key: %s", getStatus(), isRollbackOnly(), getKey());
    }
}