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

class ConsentServiceConnection {

    def servicePath = '/hybris/profile-consent/v1'

    def connection = ''
    def tenant = ''
    def accessToken = ''

    ConsentServiceConnection(baseurl, tenant, accessToken) {
        this.connection = new RestServiceConnection(baseurl, servicePath);
        this.accessToken = accessToken
        this.tenant = tenant;
    }

    def getAccessToken() {
        connection.POST(tenant + '/consentReferences', null, 'consent-reference', headers())
    }

    def headers() {
        def httpHeaders = ["Authorization": "Bearer ${accessToken}"]
        return httpHeaders
    }
}
