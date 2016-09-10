# Hacking with YaaS.io and Hybris Profile

Use the power and flexibility of YaaS microservices around commerce and customer profile graphs to react and interact with customers in a much more personal way. Create magic moments for consumers by understanding a customer's motivation and intent, in real-time. Predict, Analyze, Conclude.

[SAP Hybris Profile](https://devportal.yaas.io/solutions/saphybrisprofile/index.html) collects and connects customer profiles in a graph, allowing you to draw conclusions and discover relations - to delivers magical moments to customers or help business to better understand their customers.

# Quickstart

1.  [Sign up to YaaS](https://www.yaas.io/register/)

2.  One person in each team should create an organization, specifying _Non-commercial_. You can also chose to use it commercially, but then we'll need your credit card information. Or, if you already have an organization, just use that one.

3.  Create a project for your team, and invite your team mates to it. You can also create more than one, if you want to.

4.  If you want to use the import script, and we really recommend it, subscribe to the beta _commerce packages_ and _profile packages_ on the YaaS Market. You'll need:

     *   Cart
     *   Checkout
     *   Coupon Management
     *   Customer Accounts
     *   Media
     *   Order Management
     *   Product Content
     *   Site Management
     *   Profile Core
     *   Profile Commerce

   Now that sounds like a lot of stuff, but hey, it's for free! And allows you to build all kinds of commerce and profile solutions as well. You can also chose more packages, as you go.
   **NOTE: You can read about each commerce package [here](https://devportal.yaas.io/tools/commerceasaserviceguide/index.html#CommercePackages).**

5.  Create a client within your project:

    *   Navigate to your project page, and go to the **Clients** section
    *   Click **+ CLIENT**, and select all **Required Scopes** you need. If you're undecided what you want, you can _Select All_ . Don't do this in the real world, as this gives your client access rights it shouldn't have.
    *   You'll need the _Identifier_ <CLIENT_IDENTIFIER>, _Client ID_, and _Client Secret_, so be sure to copy those
    
6.  Run scripts to ingest data into your system.

    *   [Install Groovy](http://groovy-lang.org/install.html) on your machine
    *   Get the scripts in this project
    *   Ingest a working data set: **_import_** product, customer, and category data into your YaaS project; then **_populate_** the graph with this new project data.
        You can choose to work with a small data set and/or a large data set. The small data set will **_import_** quickly; the large data set may take up to an hour to **_import_**.

        NOTE that you can, if desired:

        1.  **_Import_** and **_populate_** the small data set
        2.  **_Import_** the large data set and _work with the small data set while waiting for this command to complete_
        3.  Once #2 is complete, **_populate_** the graph with the large data set and start working with itThe **_import_** command: use '-f sampledata/hackathon_small' to import the small data set; use '-f sampledata/hackathon' to import the large data set

        `groovy yimport.groovy -p <TENANT> -f [sampledata/hackathon_small OR sampledata/hackathon] -id <CLIENT_ID> -secret <CLIENT SECRET> -clientIdentifier <CLIENT_IDENTIFIER>`

        The **_populate_** command

        `groovy EdgeImport.groovy -p <TENANT> -id <CLIENT_ID> -secret <CLIENT_SECRET> -baseurl https://api.yaas.io`
   
   If you don't want to install groovy, just check out the commands the script is making and transfer it to a language of your liking.

7.  Bonus, but neat to have: Set up a [fully-functional online storefront](https://devportal.yaas.io/gettingstarted/setupastorefront/index.html). This storefront will also submit data to the SAP Hybris Profile system including: user identities, login events, page views, keyword searches, category views, product views, shopping cart events. Configure your storefront to send data to the SAP Hybris Profile system. Add the following code snippet before the closing </body> tag of the storefront index.html file:

    `<script async defer src="js/vendor-static/piwik.js"></script><div ytracking></div>`

# You're all set now, let’s hack...

**NOTE: Links to documentation for services referenced in these instructions can be found [here](https://devportal.yaas.io/services).**

In order to access SAP Hybris APIs, you need to **obtain a token** that can be used for all subsequent API calls. It's all standard OAuth 2.0, but we list the calls here because we know you'd rather copy and paste. Execute the following command to obtain a token:

```
curl -X POST -H "Content-Type: application/x-www-form-urlencoded" -d 'grant_type=client_credentials&scope=hybris.org_project_manage%20hybris.account_manage%20hybris.account_view%20hybris.org_members%20sap.subscription_provider_view%20hybris.profile_consent_manage%20hybris.profile_consent_view%20hybris.profile_metamodel_manage%20hybris.profile_metamodel_view%20hybris.profile_tracking_manage%20hybris.profile_graph_manage%20hybris.profile_graph_view%20hybris.profile_context_manage%20hybris.profile_context_view%20hybris.tenant=<TENANT>&client_id=<CLIENT_ID>&client_secret=<CLIENT_SECRET>' "https://api.yaas.io/hybris/oauth2/v1/token"

```

The value of the returned _access_token_ attribute will have a form similar to this string: _012-3abc45d6-7890-1e23-fg45-6h7ijkl890m1_. In subsequent instructions, this _access_token_ value will be referenced as `<ACCESS_TOKEN>`.

## “Hello, Graph!”

To visually get an idea of what your inital customer graph contains the Graph Explorer documented here: https://devportal.yaas.io/solutions/saphybrisprofile/index.html#GraphExplorer provides a quick way to query for specific graph nodes and their neighbours. From there you can query for things like products using the schema 'commerce/Product'.

The following section guides you in writing a node to, and then reading the node from, the SAP Hybris Profile graph. The Secure Graph Service, documented [here](https://devportal.yaas.io/services/securegraph/latest/index.html), provides more endpoints for writing to and reading from the graph.

1.  Fetch all metadata for your tenant to verify schemas:

    `curl -X GET -H "Content-Type: application/json" -H "Authorization: Bearer <ACCESS_TOKEN>" "https://api.yaas.io/hybris/profile-enr-auth/v1/tenants/<TENANT>/schemas"`

2.  Execute a PUT command to write a Product node, using previously-registered _nodes/commerce/Product_ schema:

    `curl -X PUT -H "Content-Type: application/json" -H "Authorization: Bearer <ACCESS_TOKEN>" -H "consent-reference: dummy" -d '{ "name" : "Hello, Graph!", "description": "A simple example." }' "https://api.yaas.io/hybris/profile-secured-graph/v1/<TENANT>/nodes/commerce/Product/<PRODUCT_ID>" -i`

3.  Execute a GET command to read the list of products:

    `curl -X GET -H "Content-Type: application/json" -H "Authorization: Bearer <ACCESS_TOKEN>" "https://api.yaas.io/hybris/product/v1/<TENANT>/products"`

4.  Execute a GET command to read the Product node from the graph. Use one of the <PRODUCT_ID>s from the results of the previous command

    `curl -X GET -H "Content-Type: application/json" -H "Authorization: Bearer <ACCESS_TOKEN>" "https://api.yaas.io/hybris/profile-secured-graph/v1/<TENANT>/nodes/commerce/Product/<PRODUCT_ID>"`

5.  Execute a GET command to return the neighbors of a specific Node:

    `curl -X GET -H "Content-Type: application/json" -H "Authorization: Bearer <ACCESS_TOKEN>" "https://api.yaas.io/hybris/profile-secured-graph/v1/<TENANT>/neighbours/commerce/Product/<PRODUCT_ID>`

And that's about all you need to know.
