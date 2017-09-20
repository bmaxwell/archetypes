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

import javax.transaction.Status;

/**
 * @author bmaxwell
 */
public enum TransactionStatus {

        ACTIVE(Status.STATUS_ACTIVE),
        COMMITTED(Status.STATUS_COMMITTED),
        COMMITTING(Status.STATUS_COMMITTING),
        MARKED_ROLLBACK(Status.STATUS_MARKED_ROLLBACK),
        NO_TRANSACTION(Status.STATUS_NO_TRANSACTION),
        PREPARED(Status.STATUS_PREPARED),
        PREPARING(Status.STATUS_PREPARING),
        ROLLEDBACK(Status.STATUS_ROLLEDBACK),
        ROLLING_BACK(Status.STATUS_ROLLING_BACK),
        UNKNOWN(Status.STATUS_UNKNOWN);

    // TODO skip description, need to modify shrinkwrap to include the properties file
//        private ResourceBundle bundle = ResourceBundle.getBundle("transaction-status-descriptions");
        private int statusCode;
        private String description;

        TransactionStatus(int statusCode) {
            this.statusCode = statusCode;
//            this.description = bundle.getString(this.name());
        }

        TransactionStatus(int statusCode, String key) {
            this.statusCode = statusCode;
//            this.description = bundle.getString(this.name());
        }

        public String getDescription() {
            return description;
        }
        public int getStatusCode() {
            return statusCode;
        }
//        @Override
//        public String toString() {
//            return this.name() + " code: " + statusCode + " description: " + description;
//        }
        @Override
        public String toString() {
            return this.name();
        }

        public static TransactionStatus getStatusForCode(int code) {
            return getStatusForCode(code, null);
        }

        public static boolean isTransactionExists(int transactionStatusCode) {
            /*
            Status.STATUS_ACTIVE
            STATUS_COMMITTED
            STATUS_MARKED_ROLLBACK
            STATUS_PREPARED
            STATUS_PREPARING
            STATUS_ROLLEDBACK
            STATUS_ROLLING_BACK
            STATUS_UNKNOWN
            */
            // This is the only status when no tx exists STATUS_NO_TRANSACTION
            return !(Status.STATUS_NO_TRANSACTION == transactionStatusCode);
        }


       public static TransactionStatus getStatusForCode(int code, String key) {
        for(TransactionStatus s : TransactionStatus.values()) {
            if(s.getStatusCode() == code) {
                return s;
            }
        }
        throw new IllegalArgumentException("Status code: " + code + " is not a valid Transaction Status code");
    }

// http://docs.oracle.com/javaee/5/api/javax/transaction/Status.html
//static int     STATUS_ACTIVE
//    A transaction is associated with the target object and it is in the active state.
//static int  STATUS_COMMITTED
//    A transaction is associated with the target object and it has been committed.
//static int  STATUS_COMMITTING
//    A transaction is associated with the target object and it is in the process of committing.
//static int  STATUS_MARKED_ROLLBACK
//    A transaction is associated with the target object and it has been marked for rollback, perhaps as a result of a setRollbackOnly operation.
//static int  STATUS_NO_TRANSACTION
//    No transaction is currently associated with the target object.
//static int  STATUS_PREPARED
//    A transaction is associated with the target object and it has been prepared.
//static int  STATUS_PREPARING
//    A transaction is associated with the target object and it is in the process of preparing.
//static int  STATUS_ROLLEDBACK
//    A transaction is associated with the target object and the outcome has been determined to be rollback.
//static int  STATUS_ROLLING_BACK
//    A transaction is associated with the target object and it is in the process of rolling back.
//static int  STATUS_UNKNOWN
//    A transaction is associated with the target object but its current status cannot be determined.

}
