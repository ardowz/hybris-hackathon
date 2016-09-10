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

class MediaRepositoryServiceConnection {


    //def servicePath = '/media-repository/v2'
    def servicePath = '/hybris/media/v1'

    def connection = ''
    def tenant = ''
    def accessToken = ''
    def clientIdentifier = ''
    
    MediaRepositoryServiceConnection(baseurl, tenant, accessToken,  app_identifier) {

        this.connection = new RestServiceConnection(baseurl, servicePath);
        this.accessToken = accessToken
        this.tenant = tenant;
        this.clientIdentifier = app_identifier;
    }

    def createCategoryMediaFromFile(filepath) {
        def media = null;

        def binaryFile = new File(filepath); 

        media = connection.POST_MULTIPART("${this.tenant}/${this.clientIdentifier}/media", filepath, binaryFile, headers())

        return media
    }

    def deleteData() {
        connection.DELETE(tenant, "", headers())
    }


    def headers() {
        def httpHeaders = ["Authorization": "Bearer ${accessToken}"]
        return httpHeaders
    }
}
