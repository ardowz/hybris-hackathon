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

package restclient

import groovy.json.JsonBuilder

class CustomerServiceConnection {


    def servicePath = '/hybris/customer/v1'

    def connection = ''
    def tenant = ''
    def accessToken = ''

    CustomerServiceConnection(baseurl, tenant, accessToken) {
        this.connection = new RestServiceConnection(baseurl, servicePath)
        this.accessToken = accessToken
        this.tenant = tenant
    }

    def createCustomer(customer) {
        def customerId = customer.firstName
        def customerJson = (new JsonBuilder(customer)).toString()
        connection.POST(this.tenant + '/customers', customerJson, customerId, headers())
    }

    def getCustomers(limit) {
        connection.GET(this.tenant + "/customers?pageNumber=1&pageSize=${limit}", headers())
    }

    def getCustomer(id) {
        connection.GET(this.tenant + "/customers/${id}", headers())
    }

    def deleteCustomer(id) {
        connection.DELETE(this.tenant + "/customers", "${id}", headers())
    }

    def updateCustomer(id, customerUpdate) {
        def customerJson = (new JsonBuilder(customerUpdate)).toString()
        connection.PUT(this.tenant + "/customers", customerJson, id, headers(), true)
    }

    def getCustomerByFirstName(firstName) {
        def path = this.tenant + "/customers?q=firstName:${firstName}"
        def customers = connection.GET(path, headers())
        if (customers != null && customers.size() > 0) return customers[0]
        return null
    }

    def headers() {
        def httpHeaders = ["Authorization": "Bearer ${accessToken}"]
        return httpHeaders
    }


}
