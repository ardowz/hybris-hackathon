<!DOCTYPE html><html><head><meta charset="utf-8"><title>Untitled Document.md</title><style></style></head><body id="preview">
<h1><a id="Hybris_Profile_Hackathons__Sep_2016_0"></a>Hybris Profile Hackathons - Sep 2016</h1>
<p>SAP Hybris Profile allows businesses to create, maintain, and continually extend customer profiles from a range of data sources. A profile is a collection of attributes, conclusions, and classifications about a customer derived from customer interactions with the company. A customer profile can include:</p>
<ul>
<li>Commerce-related data</li>
<li>Product, Category, and Brand affinities</li>
<li>Purchase history</li>
<li>Browsing history and behavior, including basic behavioral attributes</li>
<li>Technical attributes and classifications, such as device and browser-specific attributes</li>
</ul>
<p>The SAP Hybris Profile platform leverages a graph database that allows dynamic extension of the data domain without explicitly redefining the structure of the database. Consuming applications benefit from rich, contextually consistent and relevant representations of customers that evolve over time.</p>
<h1><a id="Prerequisites_11"></a>Prerequisites</h1>
<ol>
<li>
<p><a href="https://www.yaas.io/register/">Sign up to YaaS</a></p>
</li>
<li>
<p>Create an organization, specifying <em>Non-commercial</em>.</p>
</li>
<li>
<p>Create a Project:</p>
<ul>
<li>In the left-hand navigation column, select <strong>Administration</strong></li>
<li>In the left-hand navigation column, select <strong>Projects</strong></li>
<li>Click <strong>+PROJECT</strong></li>
<li>Follow the project-creation wizard</li>
</ul>
</li>
<li>
<p>To allow you to subscribe to free <em>SAP Hybris Profile</em> beta packages, provide on-site Hybris Staff with your &lt;TENANT&gt; value in order to whitelist your project. Access your <TENANT> value as follows:</TENANT></p>
<ul>
<li>Navigate to your project page</li>
<li>In the left-hand navigation column, click <strong>Administration</strong>.</li>
<li>Copy the value of the <em>Identifier</em> field. This is your &lt;TENANT&gt; value.</li>
</ul>
</li>
<li>
<p>Once your project has been whitelisted, subscribe to public <em>commerce packages</em>:</p>
<ul>
<li>Navigate to your project page</li>
<li>In the left-hand navigation column, click <strong>Administration</strong> and then <strong>Subscription</strong>.</li>
<li>Click the <strong>+SUBSCRIPTION</strong> link and then subscribe to each of the following public <em>commerce packages</em>.
<ul>
<li>Cart</li>
<li>Checkout</li>
<li>Coupon Management</li>
<li>Customer Accounts</li>
<li>Media</li>
<li>Order Management</li>
<li>Product Content</li>
<li>Site Management</li>
</ul>
</li>
</ul>
<p><strong>NOTE: You can read about each commerce package <a href="https://devportal.yaas.io/tools/commerceasaserviceguide/index.html#CommercePackages">here</a>.</strong></p>
</li>
<li>
<p>Subscribe to private <em>Hybris Profile packages</em>:</p>
<ul>
<li>Navigate to your project page</li>
<li>In the left-hand navigation column, click <strong>Administration</strong> and then <strong>Subscription</strong>.</li>
<li>Subscribe to each of the following <em>private packages</em> by clicking the <strong>+PRIVATE PACKAGE</strong> link for each package and entering the associated Version ID:
<ul>
<li>Profile Core Service Package: Version ID <em>579732baca4e04001dae50bb</em></li>
<li>Profile Services for Commerce Package Version: Version ID <em>5799b698ca4e04001dae5cfb</em></li>
</ul>
</li>
</ul>
<p><strong>NOTE: There may be a delay before your packages are listed in the Subscriptions section; refresh your browser to reload the listing.</strong></p>
</li>
<li>
<p>Create a client within your project:</p>
<ul>
<li>Navigate to your project page</li>
<li>Click <strong>Clients</strong> in the left-hand navigation column</li>
<li>Click <strong>+ CLIENT</strong> to create a client</li>
<li>On the <strong>Required Scopes</strong> page, click <em>Select All</em> to choose all scopes (permissions)</li>
<li>Click <strong>NEXT</strong> in the upper-right corner to complete the client-creation wizard</li>
<li>Copy the <em>Identifier</em> value as your &lt;CLIENT_IDENTIFIER&gt;</li>
<li>In the <strong>Client Authorization</strong> section on your client page:
<ul>
<li>Click <strong>SHOW</strong> and copy the <em>Client ID</em> value as your &lt;CLIENT_ID&gt;, and copy the <em>Client Secret</em> value as your &lt;CLIENT_SECRET&gt;</li>
</ul>
</li>
</ul>
</li>
<li>
<p>Run scripts to ingest data into your system.</p>
<ul>
<li>
<p><a href="http://groovy-lang.org/install.html">Install Groovy</a> on your machine</p>
</li>
<li>
<p>On a command line, enter the hybris_hackathon/commerce-import directory that was extracted from your flash drive</p>
</li>
<li>
<p>Ingest a working data set: <strong><i>import</i></strong> product, customer, and category data into your YaaS project; then <strong><i>populate</i></strong> the graph with this new project data.
<br/>
You can choose to work with a small data set and/or a large data set. The small data set will <strong><i>import</i></strong> quickly; the large data set may take up to an hour to <strong><i>import</i></strong>.
<br/><br/>
NOTE that you can, if desired:
    <ol>
    <li><strong><i>Import</i></strong> and <strong><i>populate</i></strong> the small data set</li>
    <li><strong><i>Import</i></strong> the large data set and <i>work with the small data set while waiting for this command to complete</i></li>
    <li>Once #2 is complete, <strong><i>populate</i></strong> the graph with the large data set and start working with it</li>
    </ol>
</p>
The <strong><i>import</i></strong> command: use '-f sampledata/hackathon_small' to import the small data set; use '-f sampledata/hackathon' to import the large data set
<br/>
<p><code>groovy yimport.groovy -p &lt;TENANT&gt; -f [sampledata/hackathon_small OR sampledata/hackathon] -id &lt;CLIENT_ID&gt; -secret &lt;CLIENT SECRET&gt; -clientIdentifier &lt;CLIENT_IDENTIFIER&gt;</code></p>
The <strong><i>populate</i></strong> command
<br/>
<p><code>groovy EdgeImport.groovy -p &lt;TENANT&gt; -id &lt;CLIENT_ID&gt; -secret &lt;CLIENT_SECRET&gt; -baseurl https://api.yaas.io</code></p>
</li>
</ul>
</li>
<li>
<p>Set up a <a href="https://devportal.yaas.io/gettingstarted/setupastorefront/index.html">fully-functional online storefront</a>. This storefront will also submit data to the SAP Hybris Profile system including: user identities, login events, page views, keyword searches, category views, product views, shopping cart events.  Configure your storefront to send data to the SAP Hybris Profile system. Add the following code snippet before the closing &lt;/body&gt; tag of the storefront index.html file:</p>
<p><code>&lt;script async defer src=&quot;js/vendor-static/piwik.js&quot;&gt;&lt;/script&gt;&lt;div ytracking&gt;&lt;/div&gt;</code></p>
</li>
</ol>
<h1><a id="Lets_hack_81"></a>Let’s hack…</h1>
<p><strong>NOTE: Links to documentation for services referenced in these instructions can be found <a href="https://devportal.yaas.io/services">here</a>.</strong></p>
<p>In order to access SAP Hybris APIs, you need to <strong>obtain a token</strong> that can be used for all subsequent API calls. Execute the following command to obtain a token:</p>
<pre><code>curl -X POST -H &quot;Content-Type: application/x-www-form-urlencoded&quot; -d 'grant_type=client_credentials&amp;scope=hybris.org_project_manage%20hybris.account_manage%20hybris.account_view%20hybris.org_members%20sap.subscription_provider_view%20hybris.profile_consent_manage%20hybris.profile_consent_view%20hybris.profile_metamodel_manage%20hybris.profile_metamodel_view%20hybris.profile_tracking_manage%20hybris.profile_graph_manage%20hybris.profile_graph_view%20hybris.profile_context_manage%20hybris.profile_context_view%20hybris.tenant=&lt;TENANT&gt;&amp;client_id=&lt;CLIENT_ID&gt;&amp;client_secret=&lt;CLIENT_SECRET&gt;' &quot;https://api.yaas.io/hybris/oauth2/v1/token&quot;
</code></pre>
<p>The value of the returned <em>access_token</em> attribute will have a form similar to this string: <em>012-3abc45d6-7890-1e23-fg45-6h7ijkl890m1</em>.
In subsequent instructions, this <em>access_token</em> value will be referenced as <code>&lt;ACCESS_TOKEN&gt;</code>.</p>
<h2><a id="Hello_Graph_93"></a>“Hello, Graph!”</h2>
<p>This section guides you in writing a node to, and then reading the node from, the SAP Hybris Profile graph. The Secure Graph Service, documented <a href="https://devportal.yaas.io/services/securegraph/latest/index.html">here</a>, provides more endpoints for writing to and reading from the graph.</p>
<ol>
<li>
<p>Fetch all metadata for your tenant to verify schemas:</p>
<p><code>curl -X GET -H &quot;Content-Type: application/json&quot; -H &quot;Authorization: Bearer &lt;ACCESS_TOKEN&gt;&quot; &quot;https://api.yaas.io/hybris/profile-enr-auth/v1/tenants/&lt;TENANT&gt;/schemas&quot;</code></p>
</li>
    <li>
        <p>Execute a PUT command to write a Product node, using previously-registered <em>nodes/commerce/Product</em> schema:</p>
        <p><code>curl -X PUT -H &quot;Content-Type: application/json&quot; -H &quot;Authorization: Bearer &lt;ACCESS_TOKEN&gt;&quot; -H &quot;consent-reference: dummy&quot; -d '{ &quot;name&quot; : &quot;Hello, Graph!&quot;, &quot;description&quot;: &quot;A simple example.&quot; }' &quot;https://api.yaas.io/hybris/profile-secured-graph/v1/&lt;TENANT&gt;/nodes/commerce/Product/&lt;PRODUCT_ID&gt;&quot; -i</code></p>
    </li>
<li>
<p>Execute a GET command to read the list of products:</p>
<p><code>curl -X GET -H &quot;Content-Type: application/json&quot; -H &quot;Authorization: Bearer &lt;ACCESS_TOKEN&gt;&quot; &quot;https://api.yaas.io/hybris/product/v1/&lt;TENANT&gt;/products&quot;</code></p>
</li>
<li>
<p>Execute a GET command to read the Product node from the graph. Use one of the &lt;PRODUCT_ID&gt;s from the results of the previous command</p>
<p><code>curl -X GET -H &quot;Content-Type: application/json&quot; -H &quot;Authorization: Bearer &lt;ACCESS_TOKEN&gt;&quot; &quot;https://api.yaas.io/hybris/profile-secured-graph/v1/&lt;TENANT&gt;/nodes/commerce/Product/&lt;PRODUCT_ID&gt;&quot;</code></p>
</li>
<li>
<p>Execute a GET command to return the neighbors of a specific Node:</p>
<p><code>curl -X GET -H &quot;Content-Type: application/json&quot; -H &quot;Authorization: Bearer &lt;ACCESS_TOKEN&gt;&quot; &quot;https://api.yaas.io/hybris/profile-secured-graph/v1/&lt;TENANT&gt;/neighbours/commerce/Product/&lt;PRODUCT_ID&gt;</code></p>
</li>
</ol>
<p>For more in-depth information, please see our <a href="https://devportal.yaas.io/solutions/saphybrisprofile/index.html">Hybris Profile Solutions</a> page.</p>

</body></html>
