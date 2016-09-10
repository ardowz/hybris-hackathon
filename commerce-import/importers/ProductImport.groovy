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

package importers

import restclient.PriceServiceConnection
import restclient.ProductServiceConnection


class ProductImport {

    def productServiceConnection = ''
    def priceServiceConnection = ''
    def categoryImport = ''

    def ProductImport(baseurl, tenant, access_token, app_identifier) {
        productServiceConnection = new ProductServiceConnection(baseurl, tenant, access_token);
        priceServiceConnection = new PriceServiceConnection(baseurl, tenant, access_token);
        categoryImport = new CategoryImport(baseurl, tenant, access_token, app_identifier)
    }


    def createProduct(product, folder, uploadImages, _categories, _images, _published, _price) {

        def sku = product.sku
        def existingProduct = null
        def productId = null;

        if (sku != null) {

            existingProduct = productServiceConnection.getProductBySku(sku)
            if (existingProduct != null) {                
                productId = existingProduct.id
                println "found product ${sku} (id:  ${productId})"
            }
        }

        if (existingProduct == null) {

            def newProduct = productServiceConnection.createProduct(product)
            if (newProduct != null) productId = newProduct.id
            println 'created product ' + " ${sku} (id: ${productId})"
        }

       
        if (productId == null) {
            println "unable to retrieve or create product ${sku}, skipping"
            return
        }

        // update product with images
        if (uploadImages) {
            def currentImages = null

            _images.each() { image ->

                // download image
                def downloadExternal = false

                def filename = null

                if (image.url) {
                    filename = image.url.split("/")[-1]
                } else if (image.file) {
                    filename = image.file.split('\\.')[0];
                }
                if (filename) {
                    // get current images if not present yet
                    if (currentImages == null) {
                        currentImages = productServiceConnection.getProductMedia(productId)
                    }

                    def foundImage = false
                    for (media in currentImages) {
                        if (media.customAttributes && media.customAttributes.filename && media.customAttributes.filename == filename) {
                            println "found ${filename}, not re-uploading."
                            foundImage = true
                            break
                        }
                    }

                    if (!foundImage) {
                        if (image.url && !downloadExternal) {
                            println "creating linked media from url: ${image.url}"

                            def media = productServiceConnection.createMedia(productId, [
                                    url             : image.url,
                                    customAttributes: [filename: filename]
                            ])
                            def metadata = [customAttributes: [filename: filename]]
                            if (image.disposition) {
                                metadata.tags = [image.disposition]
                            }

                            productServiceConnection.updateMediaMetadata(productId, media.id, metadata)

                        } else if (image.url) { //  download images


                            use(FileBinaryCategory) {
                                try {
                                    def uri = image.url
                                    def file = new File("tmpfile")
                                    file << uri.toURL()
                                    if (file.length() > 0) {
                                        println "creating media from url (uploading to media servce): ${image.url}"
                                        def media = productServiceConnection.createMediaFromFile(productId, file.getPath())
                                        def metadata = [customAttributes: [filename: filename]]
                                        if (image.disposition) {
                                            metadata.tags = [image.disposition]
                                        }
                                        productServiceConnection.updateMediaMetadata(productId, media.id, metadata)
                                    } else {
                                        println "skipping image ${uri} - cannot download"
                                    }
                                    file.delete()
                                } catch (IOException ex) {
                                    println "skipping image ${image.url} - cannot download: ${ex.message}"
                                }
                            }
                        } else if (image.file && !"".equals(image.file.trim()) && !image.file.trim().endsWith('/')) {
                            def imageFile = new File(new File(folder, "images"), image.file)
                            if (imageFile.exists()) {
                                def media = productServiceConnection.createMediaFromFile(productId, imageFile.getPath())

                                if(media != null){

                                    def metadata = [customAttributes: [filename: filename]];

                                    //def metadata = [:];
                                    if (image.disposition) {
                                        println image.disposition;
                                        if(image.disposition == "main"){
                                            metadata.customAttributes.main = true;
                                        }
                                        metadata.tags = [image.disposition]
                                    }
                                    productServiceConnection.updateMediaMetadata(productId, media.id, metadata)
                                }

                            } else {
                                println "skipping image ${image.file} - not found"
                            }

                        }
                    }
                }
            }
        }

        // publish price
        if (_price != null) {
            _price.each() { currency, value ->
                def newPrice = ["productId": productId, "originalAmount": value.replaceAll("[^\\d.]", ""), "currency": currency.toUpperCase()]
                setPrice(newPrice)
            }

        }

        // set images and publish
        def productUpdate = ["published": _published]

        println "publishing product ${sku}"
        productServiceConnection.updateProduct(productId, productUpdate)

        // categorize products
        if (_categories != null) {
            for (productCategory in _categories) {

                def category = categoryImport.findCategory(productCategory)

                if (category == null) {
                    category = categoryImport.createOrUpdateCategory(productCategory)
                }
                categoryImport.assignProductToCategory(category, productId)
            }

        }
    }


    def setPrice(newPrice) {
        def existingPrice = priceServiceConnection.getPriceByProductId(newPrice.productId, newPrice.currency)

        if (existingPrice != null) {
            if (existingPrice.originalAmount != null && existingPrice.originalAmount.equals(newPrice.originalAmount)) {
                // todo why does this not work
                println "price ${existingPrice.currency} ${existingPrice.value} unchanged"
            } else {
                priceServiceConnection.updatePrice(existingPrice.priceId, newPrice)
                println "price ${newPrice.currency} ${newPrice.originalAmount} updated from ${existingPrice.currency} ${existingPrice.originalAmount}"
            }

        } else {
            priceServiceConnection.createPrice(newPrice)
            println "price ${newPrice.currency} ${newPrice.originalAmount} set"
        }
    }

    def deleteProduct(product){
        def returnedProduct = productServiceConnection.getProductBySku(product.sku);
        if (returnedProduct != null && returnedProduct.id != null) {
            
        productServiceConnection.deleteProduct(returnedProduct.id);
         println "product ${product.name} deleted";
        }
        else{
            println 'product not found';
        }
    }

}


class FileBinaryCategory {
    def static leftShift(File file, URL url) {
        url.withInputStream { is ->
            file.withOutputStream { os ->
                def bs = new BufferedOutputStream(os)
                bs << is
            }
        }
    }
}
