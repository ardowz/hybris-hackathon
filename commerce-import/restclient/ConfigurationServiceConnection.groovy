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

class ConfigurationServiceConnection {

    def servicePath = '/hybris/configuration/v1'
    def connection = ''
    def tenant = ''
    def accessToken = ''

    ConfigurationServiceConnection(baseurl, tenant, accessToken) {
        this.connection = new RestServiceConnection(baseurl, servicePath);
        this.accessToken = accessToken
        this.tenant = tenant;
    }

    def createConfiguration(config) {
        def key = config.key
        def configJson = (new JsonBuilder(config)).toString()
        connection.POST('configurations', configJson, key, headers());
    }

    def getConfiguration(key) {
        return connection.GET("configurations/${key}", headers());
    }


    def deleteConfiguration(key) {
        connection.DELETE("configurations", "${key}", headers())
    }

    def updateConfiguration(key, value) {
        connection.PUT("configurations", ["value": value], key, headers());
    }


    def headers() {
        def httpHeaders = ["Authorization": "Bearer ${accessToken}"]
        return httpHeaders
    }


}
