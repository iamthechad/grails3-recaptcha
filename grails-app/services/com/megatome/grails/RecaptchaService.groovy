package com.megatome.grails

import com.megatome.grails.recaptcha.ReCaptcha
import com.megatome.grails.recaptcha.net.AuthenticatorProxy
import grails.core.GrailsApplication
import grails.util.Environment
import groovy.transform.CompileStatic
import org.grails.config.PropertySourcesConfig

import javax.servlet.http.HttpSession

/**
 * Copyright 2010-2015 Megatome Technologies
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
@CompileStatic
class RecaptchaService {
    boolean transactional = false;
    GrailsApplication grailsApplication
    private PropertySourcesConfig recaptchaConfig = null
    private ReCaptcha recap = null

    /**
     * Gets the ReCaptcha config.
     */
    private PropertySourcesConfig getRecaptchaConfig() {
        if (recaptchaConfig == null) {
            if (grailsApplication.config.recaptcha) {
                recaptchaConfig = new PropertySourcesConfig()
                recaptchaConfig.putAll(grailsApplication.config.getProperty('recaptcha', Map))
            } else {
                ClassLoader parent = getClass().getClassLoader()
                GroovyClassLoader loader = new GroovyClassLoader(parent)
                try {
                    def rc = loader.loadClass("RecaptchaConfig")
                    Map cfg = new ConfigSlurper(Environment.current.name).parse(rc).flatten()
                    recaptchaConfig = new PropertySourcesConfig()
                    recaptchaConfig.putAll(cfg)
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("ReCaptcha configuration not specified. Run the quickstart script.")
                }
            }
            if (!recaptchaConfig.containsKey('publicKey') || recaptchaConfig.getProperty('publicKey', String).length() == 0) {
                throw new IllegalArgumentException("ReCaptcha Public Key must be specified in RecaptchaConfig")
            }
            if (!recaptchaConfig.containsKey('privateKey') || recaptchaConfig.getProperty('privateKey', String).length() == 0) {
                throw new IllegalArgumentException("ReCaptcha Private Key must be specified in RecaptchaConfig")
            }
        }
        recaptchaConfig
    }

    private ReCaptcha getRecaptchaInstance() {
        if (!recap) {
            // Public key, private key, include noscript, include script, proxy config
            PropertySourcesConfig proxyConfig = getRecaptchaConfig().getProperty('proxy', PropertySourcesConfig) ?: new PropertySourcesConfig()
            AuthenticatorProxy proxy = new AuthenticatorProxy(
                    server: getConfigProperty(proxyConfig, 'server', String),
                    port: proxyConfig.containsKey('port') ? Integer.parseInt(getConfigProperty(proxyConfig, 'port', String)) : 80,
                    username: getConfigProperty(proxyConfig, 'username', String),
                    password: getConfigProperty(proxyConfig, 'password', String) ?: ""
            )
            recap = new ReCaptcha(
                    publicKey: getConfigProperty('publicKey', String),
                    privateKey: getConfigProperty('privateKey', String),
                    includeNoScript: getConfigProperty('includeNoScript', true),
                    includeScript: getConfigProperty('includeScript', true),
                    proxy: proxy)
        }
        recap
    }

    private <T> T getConfigProperty(String key, Class<T> targetType) {
        getConfigProperty(getRecaptchaConfig(), key, targetType)
    }

    private Boolean getConfigProperty(String key, Boolean defaultValue) {
        PropertySourcesConfig config = getRecaptchaConfig()
        if(config.containsKey(key)){
            getConfigProperty(config, key, Boolean)
        } else {
            log.error("Tried to access missing ReCaptcha value '" + key + "'. Using default value of '" + defaultValue + "'")
            defaultValue
        }
    }

    private <T> T getConfigProperty(PropertySourcesConfig config, String key, Class<T> targetType) {
        if (config.containsKey(key)) {
            config.getProperty(key, targetType)
        } else {
            null
        }
    }

    /**
     * Creates HTML containing all necessary markup for displaying a ReCaptcha object. This method is most
     * commonly called by the ReCaptcha tag library and not by other users.
     *
     * @param props Options for rendering, <code>theme</code>, <code>lang</code>, <code>type</code>, and <code>tabindex</code> are currently supported by recaptcha.
     *  The <code>includeScript</code> can also be specified and will override the global configuration setting.
     *
     * @return HTML code, suitable for embedding into a webpage.
     */
    String createCaptcha(Map props) {
        return getRecaptchaInstance().createRecaptchaHtml(props)
    }

    /**
     * Creates HTML containing all necessary markup for displaying a ReCaptcha object using explicit mode. This method is most
     * commonly called by the ReCaptcha tag library and not by other users.
     *
     * @param props Options for rendering; <code>lang</code>, and <code>loadCallback</code> are currently supported by recaptcha.
     * @return HTML code, suitable for embedding into a webpage.
     */
    String createCaptchaExplicit(Map props) {
        return getRecaptchaInstance().createRecaptchaExplicitHtml(props)
    }

    /**
     * Create a JSON-like string containing parameters to be passed to the ReCaptcha JavScript object when using explicit mode.
     * @param props Options for rendering; <code>theme</code>, <code>type</code>, <code>tabindex</code>, <code>callback</code>, <code>expired-callback</code> are currently supported
     * @return
     */
    String createRenderParameters(Map props) {
        return getRecaptchaInstance().createRenderParameters(props)
    }

    /**
     * Creates HTML containing the script tag definition necessary for displaying a ReCaptcha object. This method is most
     * commonly called by the ReCaptcha tag library and not by other users.
     *
     * @param props Options for creating the tag. Only <code>lang</code> is supported.
     *
     * @return HTML code, suitable for embedding into a webpage.
     */
    String createScriptEntry(Map props) {
        return getRecaptchaInstance().createScriptTag(props)
    }

    /**
     * Verify a ReCaptcha answer.
     *
     * @param session The current session.
     * @param remoteAddress The address of the browser submitting the answer.
     * @param params Parameters supplied by the browser.
     *
     * @return True if the supplied answer is correct, false otherwise. Returns true if ReCaptcha support is disabled.
     */
    Boolean verifyAnswer(HttpSession session, String remoteAddress, Map<String, Object> params) {
        if (!isEnabled()) {
            return true
        }

        Boolean success = getRecaptchaInstance().checkAnswer(remoteAddress, params.get("g-recaptcha-response").toString().trim())
        session["recaptcha_error"] = success ? null : true
        return success
    }

    /**
     * Get a value indicating if the ReCaptcha plugin should be enabled.
     */
    Boolean isEnabled() {
        return getConfigProperty('enabled', true)
    }

    /**
     * Get a value indicating if the previous verification attempt failed.
     *
     * @param session The current session
     */
    Boolean validationFailed(HttpSession session) {
        return (session["recaptcha_error"] != null)
    }

    /**
     * Cleanup resources associated with the session. This does have to be called, but not calling it will leave the recaptcha
     * object in memory until the session expires.
     *
     * @param session The current session.
     */
    void cleanUp(HttpSession session) {
        session["recaptcha_error"] = null
    }
}
