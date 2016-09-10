package importers

import groovy.json.JsonSlurper
import restclient.CartServiceConnection
import restclient.CategoryServiceConnection
import restclient.EdgeServiceConnection
import restclient.ConsentServiceConnection
import restclient.PriceServiceConnection

@Grab(group='jline', module='jline', version='0.9.9')
import jline.*

import static java.util.UUID.randomUUID

class EdgeEvents {

    def edgeServiceConnection = ''
    def consentServiceConnection = ''
    def cartServiceConnection = ''
    def categoryServiceConnection = ''
    def priceServiceConnection = ''
    def customers = ''
    def products = ''
    def userAgents = new JsonSlurper().parseText(new File('sampledata/useragents', 'useragent-data.json').text)
    def random = new Random()
    def clearChar = ANSIBuffer.ANSICodes.left(9999)

    def EdgeEvents(baseurl, tenant, access_token, customers, products) {
        this.edgeServiceConnection = new EdgeServiceConnection(baseurl, tenant, access_token)
        this.consentServiceConnection = new ConsentServiceConnection(baseurl, tenant, access_token)
        this.cartServiceConnection = new CartServiceConnection(baseurl, tenant, access_token)
        this.categoryServiceConnection = new CategoryServiceConnection(baseurl, tenant, access_token)
        this.priceServiceConnection = new PriceServiceConnection(baseurl, tenant, access_token)
        this.customers = assignExtraCustomerData(customers)
        this.products = assignExtraProductData(products)
    }

    def assignExtraCustomerData(customers) {
        int counter = 0
        int size = customers.size()
        customers.each { customer ->
            counter++
            int completed = counter / size * 100
            print "assigning extra customer data to ${customer.customerNumber}, ${completed.toString()}% completed"
            print clearChar

            // random piwik id
            customer._id = randomUUID() as String

            // customer cart (create if nonexistent)
            def cart = cartServiceConnection.getCart(customer.customerNumber)
            customer.cartId = (cart!=null) ? cart.id : cartServiceConnection.createCart(customer.customerNumber).cartId

            // random user agent string
            customer.userAgent = userAgents[random.nextInt(userAgents.size())].userAgent

            // unique consent reference
            customer.consentReference = consentServiceConnection.getAccessToken().id

            // empty list of viewed products
            customer.viewed = []

            // empty list of viewed categories
            customer.categoryViewed = []

            // empty list of products in cart
            customer.cart = []
        }
        println()
        return customers
    }

    def assignExtraProductData(products) {
        int counter = 0
        int size = products.size()
        products.each { product ->
            counter++
            int completed = counter / size * 100
            print "assigning extra product data to ${product.id}, ${completed.toString()}% completed"
            print clearChar

            // category from YaaS
            product.category = categoryServiceConnection.getProductCategory(product.id)

            // price from YaaS
            product.price = priceServiceConnection.getPriceByProductId(product.id, 'USD').effectiveAmount
        }
        println()
        return products
    }

    def createPiwikAndSessionNodes() {
        int counter = 0
        int size = customers.size()
        customers.each { customer ->
            counter++
            int completed = counter / size * 100
            print "${customer.customerNumber} (${customer._id}) sending PageViewEvent, ${completed.toString()}% completed"
            print clearChar
            edgeServiceConnection.send('PageViewEvent', customer, null)
            sleep(50)
        }
        println()
    }

    def viewAllProducts() {
        int counter = 0
        int size = products.size()
        products.each { product ->
            counter++
            int completed = counter / size * 100
            def randomCustomer = customers[random.nextInt(customers.size())]
            print "${randomCustomer.customerNumber} (${randomCustomer._id}) sending ProductDetailPageViewEvent for ${product.id}, ${completed.toString()}% completed"
            print clearChar
            edgeServiceConnection.send('ProductDetailPageViewEvent', randomCustomer, product)
            randomCustomer.viewed.add(product)
            sleep(50)
        }
        println()
    }

    def viewAllCategories() {
        int counter = 0
        int size = products.size()
        customers.each { customer ->
            counter++
            int completed = counter / size * 100
            customer.viewed.each { product ->
                print "${customer.customerNumber} (${customer._id}) sending CategoryPageViewEvent, ${completed.toString()}% completed"
                print clearChar
                edgeServiceConnection.send('CategoryPageViewEvent', customer, product)
                customer.categoryViewed.add(product.category.name[0].toString())
                sleep(50)
            }
        }
        println()
    }

    def generateSearches() {
        int counter = 0
        int size = customers.size()
        customers.each { customer ->
            def randomCategory = customer.categoryViewed[random.nextInt(size)]
            counter++
            int completed = counter / size * 100
            if (customer.categoryViewed) {
                print "${customer.customerNumber} (${customer._id}) sending SearchEvent, ${completed.toString()}% completed"
                print clearChar
                edgeServiceConnection.send('SearchEvent', customer, null)
                sleep(50)
            }
        }
        println()
    }

    def addProductsToCarts() {
        int counter = 0
        int size = products.size()
        customers.each { customer ->
            counter++
            int completed = counter / size * 100
            customer.viewed.each { product ->
                if (true) { // todo randomize?
                    print "${customer.customerNumber} (${customer._id}) sending AddToCartEvent for ${product.id}, cart id: ${customer.cartId}, ${completed.toString()}% completed"
                    print clearChar
                    edgeServiceConnection.send('AddToCartEvent', customer, product)
                    customer.cart.add(product)
                    sleep(50)
                }
            }
        }
        println()
    }

    def orderCarts() {
        int counter = 0
        int size = products.size()
        customers.each { customer ->
            counter++
            int completed = counter / size * 100
            customer.cart.each { product ->
                if (true) { // todo randomize?
                    print "${customer.customerNumber} (${customer._id}) sending ProceedToCheckoutEvent and OrderEvent for ${product.id}, cart id: ${customer.cartId}, ${completed.toString()}% completed"
                    print clearChar
                    edgeServiceConnection.send('ProceedToCheckoutEvent', customer, product)
                    edgeServiceConnection.send('OrderEvent', customer, product)
                    sleep(50)
                }
            }
        }
        println()
    }
}
