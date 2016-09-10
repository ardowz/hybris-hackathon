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

import groovy.json.JsonSlurper

public class RestServiceConnection {

    def serviceUrl = '';


    RestServiceConnection(baseurl, servicePath) {
        serviceUrl = baseurl + servicePath
    }


    public GET(path, headers, printerror = false) {
        def url = "${serviceUrl}/${path}"
        def con = url.toURL().openConnection()
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Content-Type", "application/json")
        headers.each() {
            key, value -> con.setRequestProperty("${key}", "${value}");
        }
        con.requestMethod = 'GET'
        def found = con.responseCode == 200
        if (found) {
            def responseBody = con.inputStream.text
            return new JsonSlurper().parseText(responseBody)
        } else {
            if (printerror) {
                println "Object not fetched : Response - ${con.responseCode}"
                if (con.errorStream != null) {
                    println con.errorStream.text
                }
            }
            return null
        }
    }

    public POST(path, body, objectId, headers) {
        def url = "${serviceUrl}/${path}"
        def con = url.toURL().openConnection()
        con.doOutput = true
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Content-Type", "application/json")
        headers.each() {
            key, value -> con.setRequestProperty("${key}", "${value}");
        }
        con.requestMethod = 'POST'
        con.outputStream << body
        if (con.responseCode == 409) {
            println "Warn: Not creating object ${objectId} - already exists";
            return null
        } else if (con.responseCode == 201 || con.responseCode == 200) {
            //println "New object ${objectId} created"
            def responseBody = con.inputStream.text
            def response = (responseBody == null || responseBody.size() == 0) ? null : new JsonSlurper().parseText(responseBody)
            return response
        } else {
            println "ERROR: New object ${objectId} NOT created : response : ${con.responseCode}"
            println "POST url: " + url
            println "POST body: " + body
            if (con.errorStream != null) {
                println con.errorStream.text
            }

            return null
        }
    }


    public POST_MULTIPART(path, filename, binaryFile, headers) {
        def url = "${serviceUrl}/${path}"
        def con = url.toURL().openConnection()

        def CRLF = "\r\n"; // Line separator required by multipart/form-data.

        con.doOutput = true
        con.setRequestProperty("Accept", "*/*");
                          \
        def boundary = Long.toHexString(System.currentTimeMillis())
        con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        headers.each() {
            key, value -> con.setRequestProperty("${key}", "${value}");
        }
        con.requestMethod = 'POST'


        def charset = "UTF-8";

        def output = con.getOutputStream()
        def writer = new PrintWriter(new OutputStreamWriter(output, charset), true)



        //Send normal param.
        writer.append("--" + boundary).append(CRLF);
        writer.append("Content-Disposition: form-data; name=\"metadata\"").append(CRLF);
        
        writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
        writer.append(CRLF).append("{\"contentType\": \""+URLConnection.guessContentTypeFromName(binaryFile.getName())+"\" }").append(CRLF).flush();


         // Send binary file.
        writer.append("--" + boundary).append(CRLF);
        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + binaryFile.getName() + "\"").append(CRLF);
        
        writer.append("Content-Type: " + "application/octet-stream").append(CRLF);
        
        writer.append(CRLF).flush();

        def fileInputStream = new FileInputStream(binaryFile)

        byte[] buffer = new byte[4096];
        int len;
        while ((len = fileInputStream.read(buffer)) > 0) {
            output.write(buffer, 0, len);
        }

        output.flush(); // Important before continuing with writer!

        writer.append(CRLF); // CRLF is important! It indicates end of boundary.

        // End of multipart/form-data.
        writer.append("--" + boundary + "--").append(CRLF).flush();

        if (con.responseCode == 201) {
            println "New object ${filename} created"
            def responseBody = con.inputStream.text
            def response = (responseBody == null || responseBody.size() == 0) ? null : new JsonSlurper().parseText(responseBody)
            return response
        } else {
            println "New object ${filename} NOT created : response : ${con.responseCode}"
            println con.errorStream.text

            return null
        }
    }

    public PUT(path, body, objectId, headers, partial = false) {
        def url = "${serviceUrl}/${path}/${objectId}?partial=${partial}"
        def con = url.toURL().openConnection()
        con.doOutput = true
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Content-Type", "application/json")
        headers.each() {
            key, value -> con.setRequestProperty("${key}", "${value}");
        }
        con.requestMethod = 'PUT'
        con.outputStream << body

        def updated = con.responseCode == 200 || con.responseCode == 201 || con.responseCode == 204;
        // price service returns 201, config 204 for some reason :/
        if (updated) {
            println "Object ${objectId} updated"
            return true
        } else {
            println "Object ${objectId} not updated : Response ${con.responseCode}"
            println body.toString()
            println con.errorStream.text
            return false
        }
    }


    public DELETE(path, objectId, headers) {
        def url = "${serviceUrl}/${path}/${objectId}"
        def con = url.toURL().openConnection()
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Content-Type", "application/json")
        headers.each() {
            key, value -> con.setRequestProperty("${key}", "${value}");
        }
        con.requestMethod = 'DELETE'
        def deleted = con.responseCode == 204;
        if (deleted) {
            println "Object ${objectId} deleted"
        } else {
            println "Object ${objectId} not deleted : Response ${con.responseCode}"
            println con.errorStream.text
        }
        return deleted;
    }

    boolean isCollectionOrArray(object) {
        [Collection, Object[]].any { it.isAssignableFrom(object.getClass()) }
    }


}