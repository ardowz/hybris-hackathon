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

class PriceServiceConnection {

    def servicePath = '/hybris/price/v1'
    def connection = ''
    def tenant = ''
    def accessToken = ''

    PriceServiceConnection(baseurl, tenant, accessToken) {
        this.connection = new RestServiceConnection(baseurl, servicePath);
        this.accessToken = accessToken
        this.tenant = tenant;
    }


    def createPrice(price) {
        def priceId = price.id
        def priceJson = (new JsonBuilder(price)).toString()

        connection.POST(this.tenant + '/prices', priceJson, priceId, headers());
    }

    def getPrice(id) {
        return connection.GET(this.tenant + "/prices/${id}", headers());
    }

    def getPriceByProductId(productId, currency) {
        def response = connection.GET(this.tenant + "/prices?productId=${productId}&currency=${currency}", headers());
        if (response != null && response.size() > 0) return response[0]
        return null
    }


    def deletePrice(id) {
        connection.DELETE(this.tenant + "/prices", "${id}", headers())
    }

    def updatePrice(id, priceUpdate) {       
        def priceJson = (new JsonBuilder(priceUpdate)).toString()
        connection.PUT(this.tenant + "/prices", priceJson, id, headers());
    }


    def headers() {
        def httpHeaders = ["Authorization": "Bearer ${accessToken}"]
        return httpHeaders
    }

}
