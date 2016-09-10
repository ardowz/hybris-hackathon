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

class ProductServiceConnection {

    //def servicePath = '/product/v3'
    //def servicePath = '/product/v4'
    def servicePath = '/hybris/product/v1'

    def connection = ''
    def tenant = ''
    def accessToken = ''

    ProductServiceConnection(baseurl, tenant, accessToken) {
        this.connection = new RestServiceConnection(baseurl, servicePath)
        this.accessToken = accessToken
        this.tenant = tenant
    }

    def createProduct(product) {
        def productId = product.sku
        def productJson = (new JsonBuilder(product)).toString()
        connection.POST(this.tenant + '/products', productJson, productId, headers())
    }

    def getProducts(limit) {
        return connection.GET(this.tenant + "/products?pageNumber=1&pageSize=${limit}", headers())
    }

    def getProduct(id) {
        return connection.GET(this.tenant + "/products/${id}", headers())
    }

    def getProductBySku(sku) {
        def path = this.tenant + "/products?q=sku:${sku}"
        def products = connection.GET(path, headers())
        if (products != null && products.size() > 0) return products[0]
        return null
    }


    def deleteProduct(id) {
        connection.DELETE(this.tenant + "/products", "${id}", headers())
    }

    def updateProduct(id, productUpdate) {
        def productJson = (new JsonBuilder(productUpdate)).toString()
        connection.PUT(this.tenant + "/products", productJson, id, headers(), true)
    }

    def publishProduct(id, productUpdate) {
        def productJson = (new JsonBuilder(productUpdate)).toString()
        connection.PUT(this.tenant + "/products", productJson, id, headers(), true)
    }

    def createMedia(productId, media) {
        def mediaId = null
        def mediaJson = (new JsonBuilder(media)).toString()

        connection.POST("${this.tenant}/products/${productId}/media", mediaJson, mediaId, headers())
    }

    def createMediaFromFile(productId, filepath) {
        def media = null

        def binaryFile = new File(filepath)  

        media = connection.POST_MULTIPART("${this.tenant}/products/${productId}/media", filepath, binaryFile, headers())

        return media
    }

    def updateMediaMetadata(productId, mediaId, mediaUpdate) {
        def path = "${this.tenant}/products/${productId}/media"
        def mediaJson = (new JsonBuilder(mediaUpdate)).toString()
        connection.PUT(path, mediaJson, mediaId, headers(), true)
    }

    def getProductMedia(productId) {
        def path = this.tenant + "/products/${productId}?expand=media"
        def product = connection.GET(path, headers())
        if (product == null) {
            return null
        }
        return product.media
    }

    def createSKUindex() {
        def indexId = "tenant index ${tenant}"
        connection.POST("listeners/tenant", "{\"tenant\": \"${tenant}\"}", indexId, headers())
    }


    def headers() {
        def httpHeaders = ["Authorization": "Bearer ${accessToken}"]
        return httpHeaders
    }


}
