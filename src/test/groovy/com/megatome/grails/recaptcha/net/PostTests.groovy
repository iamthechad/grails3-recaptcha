package com.megatome.grails.recaptcha.net

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import spock.lang.Specification

/**
 * Copyright 2010-2018 Megatome Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class PostTests extends Specification {

    RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder()

    def "Test basic POST"(){
        given: "A rest client instance"
        def restTemplate = restTemplateBuilder.build()
        and: "url parameters"
        def secret = "myprivatekey"
        def response = "someresponse"
        def remoteip = "127.0.0.1"
        and: "a mock server"
        final mockServer = MockRestServiceServer.createServer(restTemplate)
        mockServer.expect(MockRestRequestMatchers.requestTo("http://www.google.com?secret=$secret&response=$response&remoteip=$remoteip"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withSuccess('{"success":"true"}', MediaType.APPLICATION_JSON))
        and: "a post"
        def post = new Post(url: "http://www.google.com", restTemplate: restTemplate)
        post.queryParams.with {
            add("secret", secret)
            add("response", response)
            add("remoteip", remoteip)
        }

        when:
        def resp = post.response

        then:
        mockServer.verify()
        resp
    }

    def "Test proxy configuration"() {
        when:
        def authProxy = new AuthenticatorProxy(server: "localhost", port: 8080)
        def post = new Post(url: "http://www.google.com", proxy: authProxy)
        def proxyAddress = post.restTemplate.requestFactory?.@proxy?.address()

        then:"The proxy settings are correct"
        proxyAddress != null
        proxyAddress.hostName == "localhost"
        proxyAddress.port == 8080
    }

    def "Test with illegal arguments"() {
        when:
        def post = new Post(url: "http://www.google.com", foo:"bar")

        then:
        post.url == "http://www.google.com"
    }

    def "Test illegal URL"() {
        when:
        def post = new Post(url: "abc123")

        then:
        post.response == null

        when:
        def authProxy = new AuthenticatorProxy(server: "localhost", port: 8080, username: "foo", password: "bar")
        post = new Post(url: "abc123", proxy: authProxy)

        then:
        post.response == null

        when:
        authProxy = new AuthenticatorProxy(server: "localhost", port: 8080)
        post = new Post(url: "abc123", proxy: authProxy)

        then:
        post.response == null
    }

    def "Test with overridden timeouts"() {
        when:
        def post = new Post(url: "http://www.google.com", connectTimeout: 1234, readTimeout: 5678)

        then:
        post.url == "http://www.google.com"
        post.restTemplate.requestFactory?.connectTimeout == 1234
        post.restTemplate.requestFactory?.readTimeout == 5678
    }

    def "Test with overridden connect timeout"() {
        when:
        def post = new Post(url: "http://www.google.com", connectTimeout: 1234)

        then:
        post.url == "http://www.google.com"
        post.restTemplate.requestFactory?.connectTimeout == 1234
        post.restTemplate.requestFactory?.readTimeout == 1000
    }

    def "Test with overridden read timeout"() {
        when:
        def post = new Post(url: "http://www.google.com", readTimeout: 5678)

        then:
        post.url == "http://www.google.com"
        post.restTemplate.requestFactory?.connectTimeout == 10000
        post.restTemplate.requestFactory?.readTimeout == 5678
    }
}
