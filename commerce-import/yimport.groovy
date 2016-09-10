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


import importers.CategoryImport
import importers.ProductImport
import importers.CustomerImport
import groovy.json.JsonSlurper


@Grab('com.xlson.groovycsv:groovycsv:1.0')
import static com.xlson.groovycsv.CsvParser.parseCsv

tenant = null
baseurl = "https://api.yaas.io"
access_token = null
client_id = null
client_secret = null

// overwriteSampleData = false;

//defaultProductUrl = "http://products-v4.${globalEnv}.cf.hybris.com"
//productsSuccessfullyCreated = false


/*############################################
#
# Products
#
############################################*/



def yimport(args) {


    def cli = new CliBuilder(usage: 'yimport.groovyvy [-h] [-noimg] -p <project> -baseurl <environment base url> -f <folder> -id <client-token> -secret <client-secret> -clientIdentifier <app-identifier>',
            header: 'parameters:')
    // Create the list of options.
    cli.with {
        h longOpt: 'help', 'show this help information', args: 0, required: false
        p longOpt: 'project', 'your project/tenant id', args: 1, required: true
        f longOpt: 'folder', 'path to your data folder (containing products.json file)', args: 1, required: true
        noimg longOpt: 'no-images', 'do not upload the linked images with the products', args: 0, required: false
        baseurl longOpt: 'env-base-url', 'the yaas environment to use: default is productions - https://api.yaas.io', args: 1, required: false
        id longOpt: 'client-id', 'OAuth2 client id', args: 1, required: true
        secret longOpt: 'client-secret', 'OAuth2 client secret', args: 1, required: true
        clientIdentifier longOpt: 'app-identifier', 'UI Module identifier', args: 1, required: true
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
    scope = 'hybris.product_read_unpublished hybris.product_create hybris.product_update hybris.product_delete hybris.product_publish hybris.product_unpublish hybris.price_owner hybris.category_read_published hybris.category_read_unpublished hybris.category_create hybris.category_update hybris.category_delete hybris.category_publish hybris.category_unpublish hybris.configuration_manage hybris.media_manage hybris.price_manage hybris.price_owner \t\n' +
            'hybris.customer_read hybris.customer_update hybris.customer_create hybris.customer_view_profile hybris.customer_edit_profile';
    uploadImages = !options.noimg
    app_identifier = options.clientIdentifier


    println "-------------------------------------------"
    println "- yaas base url: ${baseurl}"
    println "- project:     ${tenant}"
    println "- data folder:   ${options.f}"
    println "- client id:   ${client_id}"
    println "- client secret:   ${client_secret}"
    println "- App identifier:   ${app_identifier}"
    println "-------------------------------------------"

    // todo get access token
    access_token = authorize(client_id, client_secret, scope, tenant)
    if (!access_token) {
        println "Authorizaiton failed: no access token could be retrieved."
        return null
    }

    // Read Categories

    //def categoryCsvFile = new File(options.f, "categories.csv")

    //New file that contains properties for category active and category image.
    def categoryCsvFile = new File(options.f, "categories-publish.csv")


    if (categoryCsvFile.exists()) {
        println "Categories CSV found: " + categoryCsvFile.getAbsolutePath()
        def csv = categoryCsvFile.newReader()
        def data = parseCsv(csv, autoDetect: true, quoteChar: "\"")

        def categoryImport = new CategoryImport(baseurl, tenant, access_token, app_identifier)

        for (line in data) {

            def category = [:]
            line.columns.each() { column ->

                if (column.key.contains("_")) {
                    def key = column.key.split("_")[0]
                    def locale = column.key.split("_")[-1]
                    if (!category[key]) { category[key] = [:] }
                    category[key][locale] = line[column.key]
                }
                else {
                    if(column.key == 'published'){
                        category[column.key] = true;
                    }
                    else{
                        category[column.key] = line[column.key]
                    }
                }
            };

            categoryImport.createOrUpdateCategory(category, categoryCsvFile.getParentFile())

            ////Deleting category
            // categoryImport.deleteCategory(category.name.en);
        }
    }

    // Read Customers
    def customerCsvFile = new File(options.f, "customers.csv")
    def customerImport = new CustomerImport(baseurl, tenant, access_token, app_identifier)

    if (customerCsvFile.exists()) {
        println "Customer CSV found: " + customerCsvFile.getAbsolutePath()
        def csv = customerCsvFile.newReader()
        def data = parseCsv(csv, autoDetect: true)

        for (line in data) {

            def customer = [:]
            line.columns.each() { column ->

                if (column.key.contains("_")) {
                    def key = column.key.split("_")[0]
                    def locale = column.key.split("_")[-1]
                    if (!customer[key]) { customer[key] = [:] }
                    customer[key][locale] = line[column.key]
                }
                else {
                    if(column.key == 'active'){
                        customer[column.key] = true;
                    }
                    else{
                        customer[column.key] = line[column.key]
                    }
                }
            };

            //Creating customers
            customerImport.createCustomer(customer)
        }

    } else {
        println "Customers CSV not found: " + jsonFile.getAbsolutePath()
        return
    }

    // Read Products
    def productImport = new ProductImport(baseurl, tenant, access_token, app_identifier)

    def jsonFile = new File(options.f, "products.json")
    def csvFile = new File(options.f, "products.csv")
    if (jsonFile.exists()) {
        println "Products json found: " + jsonFile.getAbsolutePath()
        def slurper = new JsonSlurper()
        def result = slurper.parse(jsonFile);

        result.each() { product ->
            productImport.createProduct(product, jsonFile.getParentFile())
        }
    } else if (csvFile.exists()) {
        println "Products CSV found: " + csvFile.getAbsolutePath()
        def csv = csvFile.newReader()
        def data = parseCsv(csv, autoDetect: true)

        for (line in data) {

            def product = [:]
            line.columns.each() { column ->

                if (column.key.contains("_")) {
                    def key = column.key.split("_")[0]
                    def locale = column.key.split("_")[-1]
                    if (!product[key]) { product[key] = [:] }
                    product[key][locale] = line[column.key]
                }
                else {
                    product[column.key] = line[column.key]
                }
            };

            if (product.category && product.category.en) {
                product.categories = product.category.en.split(",")
            }
            else {
                product.categories = []
            }

            if (product.image) {
                product.images = []
                product.image.each() { disposition, filename ->
                    product.images.add([file: filename, disposition: disposition])
                }
                product.image = ""
            }


//Product v1
product.mixins = [inventory:[inStock:"1".equals(product.instock) || "true".equals(product.instock)]]; 
product.metadata =  [mixins: [inventory: 'https://api.yaas.io/hybris/schema/v1/hybriscommerce/inventorySchema-v1']];

            def images = product.images;
            def categories = product.categories;
            def published = "1".equals(product.active) || "true".equals(product.active);
            def price = product.price;

            product.remove('instock');
            product.remove('active');
            product.remove('categories');
            product.remove('category');
            product.remove('color');
            product.remove('image');
            product.remove('images');
            product.remove('price');

            //Creating products
            productImport.createProduct(product, csvFile.getParentFile(), uploadImages, categories, images, published, price)

            //Deleting products
            // productImport.deleteProduct(product);
        }

    } else {
        println "Products json not found: " + jsonFile.getAbsolutePath()
        println "Products CSV not found: " + csvFile.getAbsolutePath()
        return
    }

    println '';
    println 'Finished!';
    println '';

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

yimport(args)
