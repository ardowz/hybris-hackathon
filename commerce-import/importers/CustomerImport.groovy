/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2014 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 */

package importers

import restclient.CustomerServiceConnection


class CustomerImport {

    def customerServiceConnection = ''

    def CustomerImport(baseurl, tenant, access_token, app_identifier) {
        customerServiceConnection = new CustomerServiceConnection(baseurl, tenant, access_token);
    }


    def createCustomer(customer) {

        def firstName = customer.firstName
        def existingCustomer = null
        def customerId = null;

        if (firstName != null) {

            existingCustomer = customerServiceConnection.getCustomerByFirstName(firstName)
            if (existingCustomer != null) {
                customerId = existingCustomer.id
                println "found customer ${firstName} (id:  ${customerId})"
            }
        }

        if (existingCustomer == null) {

            def newCustomer = customerServiceConnection.createCustomer(customer)
            if (newCustomer != null) customerId = newCustomer.id
            println 'created customer ' + " ${firstName} (id: ${customerId})"
        }


        if (customerId == null) {
            println "unable to retrieve or create customer ${firstName}, skipping"
            return
        }

    }


    def deleteCustomer(customer){
        def returnedCustomer = customerServiceConnection.getCustomerByFirstName(customer.firstName);
        if (returnedCustomer != null && returnedCustomer.id != null) {

            customerServiceConnection.deleteCustomer(returnedCustomer.id);
            println "customer ${customer.name} deleted";
        }
        else{
            println 'customer not found';
        }
    }

}
