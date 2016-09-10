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

import java.time.Instant
import groovy.json.JsonBuilder

class EdgeServiceConnection {

    def servicePath = '/hybris/profile-edge/v1'

    def connection = ''
    def tenant = ''
    def accessToken = ''
    def random = new Random()

    EdgeServiceConnection(baseurl, tenant, accessToken) {
        this.connection = new RestServiceConnection(baseurl, servicePath);
        this.accessToken = accessToken
        this.tenant = tenant;
    }

    def send(event, customer, product) {
        connection.POST('events', body(event, customer, product), event, headers(customer));
    }

    def url(product) {
        // todo generate url with brand and category info
        return "null"
    }
    def cvar(product, cart) {
        return "\"{\"1\":[\"cart_id\",\"${cart}\"],\"2\":[\"_pkp\",\"${product.price}\"],\"3\":[\"_pks\",\"${product.id}\"],\"4\":[\"_pkn\",\"${product.name}\"],\"5\":[\"_pkc\",\"${product.category.name[0]}\"]}\""
    }

    def ec_items(product) {
        return new JsonBuilder([[product.id, product.name, product.category.name[0].toString(), product.price, "1"]]).toString()
    }

    def body(action_name, customer, product) {
        // todo should events use the current time? a previous time? a random time?? seems like using the current time would be confusing as everything would happen (more or less) at once
        def httpBody = ["action_name": action_name,
                        "idsite"     : "${tenant}.default", // todo is this correct? on the apparel-uk storefront the idsite is "apparel-uk"
                        "rec"        : "1",
                        "_id"        : customer._id,
                        "_idts"      : customer._idts ?: "1", // UNIX timestamp for customer's first visit todo better default or populate field beforehand randomly
                        "_viewts"    : customer._viewts ?: "1", // UNIX timestamp for customer's last visit todo better default or populate field beforehand (maybe randomly)
                        "send_image" : "0",
                        "url"        : (product!=null) ? url(product) : "none", // todo better default
                        "date"       : Instant.now().toEpochMilli().toString()]
        if (product) {
            if (action_name=='AddToCartEvent') {
                httpBody['cvar'] = cvar(product, customer.cartId)
                httpBody['ec_items'] = ec_items(product)

                if (action_name=='OrderEvent') {
                    httpBody['idgoal'] = "0"
                    httpBody['revenue'] = product.price.toString()
                }
            }
            else {
                httpBody['cvar'] = cvar(product, customer.cartId)
            }
        }
        if (action_name=='SearchEvent') {
            String randomSearch = customer.categoryViewed[random.nextInt(customer.categoryViewed.size())]
            httpBody['search'] = randomSearch
            httpBody['search_cat'] = randomSearch
            httpBody['search_count'] = "1"
        }
        return new JsonBuilder(httpBody).toString()
    }

    def headers(customer) {
        def httpHeaders = ["Authorization"    : "Bearer ${accessToken}",
                           "hybris-tenant"    : tenant,
                           "event-type"       : "piwik",
                           "consent-reference": customer.consentReference,
                           "User-Agent"       : customer.userAgent]
        return httpHeaders
    }
}
