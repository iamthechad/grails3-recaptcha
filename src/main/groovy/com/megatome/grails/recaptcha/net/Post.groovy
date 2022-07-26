package com.megatome.grails.recaptcha.net

import org.apache.commons.logging.LogFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

import java.time.Duration

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

class Post {
    private static final log = LogFactory.getLog(this)
    String url
    QueryParams queryParams = new QueryParams(null)
    int connectTimeout = 10000
    int readTimeout = 1000
    AuthenticatorProxy proxy = null
    RestTemplateBuilder restTemplateBuilder = null
    RestTemplate restTemplate = null

    Post(Map options) {
        options.each { k, v -> if (this.hasProperty(k) && v) { this."$k" = v } }
        if (!restTemplateBuilder) {
            restTemplateBuilder = new RestTemplateBuilder()
        }
        if (!restTemplate) {
            SimpleClientHttpRequestFactory requestFactory = (SimpleClientHttpRequestFactory) restTemplateBuilder.requestFactory(SimpleClientHttpRequestFactory).buildRequestFactory()
            if (proxy?.isConfigured()) {
                requestFactory.proxy = proxy.proxy
            }
            restTemplate = restTemplateBuilder
                    .requestFactory({ requestFactory })
                    .setConnectTimeout(Duration.ofMillis(connectTimeout))
                    .setReadTimeout(Duration.ofMillis(readTimeout))
                    .build()
        }
    }

    def getResponse() {
        try {
            return restTemplate.postForObject(url, null, Map, queryParams.params)
        } catch (Exception e) {
            def message = "Failed to connect to ${url}."
            if (proxy?.isConfigured()) {
                message += "\n\tAttempting to use proxy ${proxy.server}:${proxy.port}"
                if (proxy.username != null) {
                    message += "\n\tProxy username: ${proxy.username}. (Be sure that password is correct)"
                }
            }
            log.error(message, e)
        }
        return null
    }
}