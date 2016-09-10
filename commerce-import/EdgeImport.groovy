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

import importers.EdgeEvents
import restclient.ProductServiceConnection
import restclient.CustomerServiceConnection

import groovy.json.JsonSlurper

def edgeImport(args) {
    def cli = new CliBuilder(usage: 'yimport.groovy [-h] -p <project> -baseurl <environment base url> -id <client-token> -secret <client-secret>',
            header: 'parameters:')
    // Create the list of options.
    cli.with {
        h longOpt: 'help', 'show this help information', args: 0, required: false
        p longOpt: 'project', 'your project/tenant id', args: 1, required: true
        baseurl longOpt: 'env-base-url', 'the yaas environment to use: default is productions - https://api.yaas.io', args: 1, required: false
        id longOpt: 'client-id', 'OAuth2 client id', args: 1, required: true
        secret longOpt: 'client-secret', 'OAuth2 client secret', args: 1, required: true
    }

    def options = cli.parse(args)
    if (!options) {
        return
    }
    // Show usage text when -h or --help option is used.
    if (options.h) {
        cli.usage()
        return
    }

    if (options.baseurl) {
        baseurl = options.baseurl
    }

    tenant = options.p
    client_id = options.id
    client_secret = options.secret
    scope = 'hybris.product_read_unpublished hybris.product_create hybris.product_update hybris.product_delete hybris.product_publish hybris.product_unpublish hybris.price_owner hybris.category_read_published hybris.category_read_unpublished hybris.category_create hybris.category_update hybris.category_delete hybris.category_publish hybris.category_unpublish hybris.configuration_manage hybris.media_manage hybris.price_manage hybris.price_owner hybris.customer_read hybris.customer_update hybris.customer_create hybris.customer_view_profile hybris.customer_edit_profile hybris.cart_manage';

    println "-------------------------------------------"
    println "- yaas base url: ${baseurl}"
    println "- project:     ${tenant}"
    println "- client id:   ${client_id}"
    println "- client secret:   ${client_secret}"
    println "-------------------------------------------"

    access_token = authorize(client_id, client_secret, scope, tenant)
    if (!access_token) {
        println "Authorization failed: no access token could be retrieved."
        return 1
    }

    println 'Importing products and customers from YaaS'
    def productServiceConnection = new ProductServiceConnection(baseurl, tenant, access_token)
    def customerServiceConnection = new CustomerServiceConnection(baseurl, tenant, access_token)
    def maxProducts = 10000
    def maxCustomers = 10000
    def products = productServiceConnection.getProducts(maxProducts)
    def customers = customerServiceConnection.getCustomers(maxCustomers)
    if (!products) {
        println "YaaS import failed: no products found"
        return 1
    }
    if (!customers) {
        println "YaaS import failed: no customers found"
        return 1
    }
    println "${products.size()} products, ${customers.size()} customers found\n"

    println 'Setting up EdgeEvents importer'
    def importer = new EdgeEvents(baseurl, tenant, access_token, customers, products)
    println()

    println 'Sending Edge events'
    // customer scenarios
    importer.createPiwikAndSessionNodes()
    importer.viewAllProducts()
    importer.viewAllCategories()
    importer.generateSearches()
    importer.addProductsToCarts()
    importer.orderCarts()

    println()
    println 'Finished!'
    println()
    return 0
}

def authorize(client_id, client_secret, scope, tenant) {
    def url = "${baseurl}/hybris/oauth2/v1/token"
    def con = url.toURL().openConnection()

    con.doOutput = true
    con.setRequestProperty("Accept", "application/json");
    con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
    con.requestMethod = 'POST'

    con.outputStream << "grant_type=client_credentials&scope=${scope}&client_id=${client_id}&client_secret=${client_secret}"
    if (con.responseCode == 201 || con.responseCode == 200) {
        def responseBody = con.inputStream.text
        def response = (responseBody == null || responseBody.size() == 0) ? null : new JsonSlurper().parseText(responseBody)

        return response.access_token
    } else {
        println "Authorizaiton failed: ${con.responseCode}"
        println "Endpoint: " + url
        if (con.errorStream != null) {
            println con.errorStream.text
        }
        return null
    }
}

edgeImport(args)