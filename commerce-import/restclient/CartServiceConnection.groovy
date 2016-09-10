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

class CartServiceConnection {


    def servicePath = '/hybris/cart/v1'

    def connection = null
    def tenant = null
    def accessToken = null

    CartServiceConnection(baseurl, tenant, accessToken) {
        this.connection = new RestServiceConnection(baseurl, servicePath)
        this.accessToken = accessToken
        this.tenant = tenant
    }

    def createCart(customerId) {
        def cart = ["currency"  : "USD",
                    "customerId": customerId]
        def cartJson = (new JsonBuilder(cart)).toString()
        connection.POST(this.tenant + '/carts', cartJson, "cart for ${customerId}", headers())
    }

    def getCarts() {
        connection.GET(this.tenant + "/carts", headers())
    }

    def getCart(customerId) {
        connection.GET(this.tenant + "/carts?customerId=${customerId}", headers())
    }

    def headers() {
        def httpHeaders = ["Authorization": "Bearer ${accessToken}"]
        return httpHeaders
    }


}
