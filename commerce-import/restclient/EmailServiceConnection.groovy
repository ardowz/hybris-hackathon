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

class EmailServiceConnection {

    def servicePath = '/email/v1'
    def connection = ''
    def tenant = ''
    def accessToken = ''

    EmailServiceConnection(baseurl, tenant, accessToken) {
        this.connection = new RestServiceConnection(baseurl, servicePath);
        this.accessToken = accessToken
        this.tenant = tenant;
    }

    def createEmailTemplate(projectId, serviceId, templateId, description, name) {
        def body = """{
            "name": "${name}",
            "description": "${description}",
            "templateId": "${templateId}"
        }"""
        def result = connection.POST('templates', body, templateId, headers())
        return getEmailTemplate(projectId, serviceId, templateId)

    }

    def getEmailTemplate(projectId, serviceId, templateId) {
        return connection.GET("templates/${templateId}", headers())
    }

    def headers() {
        def httpHeaders = ["Authorization": "Bearer ${accessToken}"]
        return httpHeaders
    }


}
