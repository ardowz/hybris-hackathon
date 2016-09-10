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

class CategoryServiceConnection {

    def servicePath = '/hybris/category/v1'
    def connection = ''
    def tenant = ''
    def accessToken = ''

    CategoryServiceConnection(baseurl, tenant, accessToken) {
        this.connection = new RestServiceConnection(baseurl, servicePath);
        this.accessToken = accessToken
        this.tenant = tenant;
    }

    def createCategory(category) {
        def categoryId = null
        def categoryJson = (new JsonBuilder(category)).toString()
        connection.POST(this.tenant + '/categories', categoryJson, categoryId, headers());
    }

    def getCategory(id, expand = "") {
        return connection.GET(this.tenant + "/categories/${id}?expand=$expand", headers());
    }

    def getCategories(expand = "") {
        def path = this.tenant + "/categories?expand=$expand"

        def categories = connection.GET(path, headers());
        return categories
    }

    def getProductCategory(productId) {
        def path = this.tenant + "/categories?ref.type=product&ref.id=${productId}"
        return connection.GET(path, headers())
    }

    def addProductToCategory(productId, categoryId) {
        def url = "http://api.yaas.io/product/v2/"+this.tenant+"/${productId}"
        def element = [ref: [url: url, id: productId, type: "product"]]
        def elementId = null
        connection.POST(this.tenant + "/categories/${categoryId}/assignments", (new JsonBuilder(element)).toString(), elementId, headers())
    }

    def updateCategory(categoryId, categoryUpdate) {
        def path = "${this.tenant}/categories"
        def categoryJson = (new JsonBuilder(categoryUpdate)).toString()
        connection.PUT(path, categoryJson, categoryId, headers(), true)
    }

    def deleteCategory(categoryId) {
        connection.DELETE(this.tenant + "/categories", "${categoryId}", headers())
    }


    def headers() {
        def httpHeaders = ["Authorization": "Bearer ${accessToken}"]
        return httpHeaders
    }

}
