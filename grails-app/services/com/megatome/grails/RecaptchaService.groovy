package com.megatome.grails

import com.megatome.grails.recaptcha.ReCaptcha
import com.megatome.grails.recaptcha.net.AuthenticatorProxy
import com.megatome.grails.util.ConfigHelper
import grails.util.Environment
import org.grails.config.NavigableMap

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

class RecaptchaService {
    boolean transactional = false
    def grailsApplication
    private NavigableMap recaptchaConfig = null
    private ReCaptcha recap = null

    /**
     * Gets the ReCaptcha config.
     */
    private def getRecaptchaConfig() {
        if (this.recaptchaConfig == null) {
            if (grailsApplication.config.recaptcha) {
                this.recaptchaConfig = grailsApplication.config.recaptcha
            } else {
                ClassLoader parent = getClass().getClassLoader()
                GroovyClassLoader loader = new GroovyClassLoader(parent)
                try {
                    def rc = loader.loadClass("RecaptchaConfig")
                    def cfg = new ConfigSlurper(Environment.current.name).parse(rc)
                    this.recaptchaConfig = cfg.recaptcha
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("ReCaptcha configuration not specified.")
                }
            }
            if (!this.recaptchaConfig.publicKey || this.recaptchaConfig.publicKey.length() == 0) {
                throw new IllegalArgumentException("ReCaptcha Public Key must be specified in RecaptchaConfig")
            }
            if (!this.recaptchaConfig.privateKey || this.recaptchaConfig.privateKey.length() == 0) {
                throw new IllegalArgumentException("ReCaptcha Private Key must be specified in RecaptchaConfig")
            }
        }
        return this.recaptchaConfig
    }

    private ReCaptcha getRecaptchaInstance() {
        if (!recap) {
            // Public key, private key, include noscript, include script, proxy config
            def config = getRecaptchaConfig()
            def proxyConfig = config.proxy
            def proxy = new AuthenticatorProxy(
                    server: proxyConfig.containsKey('server') ? proxyConfig.server : null,
                    port: proxyConfig.containsKey('port') ? Integer.parseInt(proxyConfig.port) : 80,
                    username: proxyConfig.containsKey('username') ? proxyConfig.username : null,
                    password: proxyConfig.containsKey('password') ? proxyConfig.password : ""
            )
            recap = new ReCaptcha(
                    publicKey: config.publicKey,
                    privateKey: config.privateKey,
                    includeScript: safeGetConfigValue('includeScript', true),
                    timeoutConfig: config.timeoutConfig,
                    proxy: proxy)
        }
        recap
    }

    private boolean safeGetConfigValue(String value, boolean defaultValue) {
        def config = getRecaptchaConfig()
        if (config.containsKey(value)) {
            return ConfigHelper.booleanValue(config[value])
        }
        log.error("Tried to access missing ReCaptcha value '" + value + "'. Using default value of '" + defaultValue + "'")
        defaultValue
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
    String createCaptcha(props) {
        return getRecaptchaInstance().createRecaptchaHtml(props)
    }

    /**
     * Creates HTML containing all necessary markup for displaying a ReCaptcha object using explicit mode. This method is most
     * commonly called by the ReCaptcha tag library and not by other users.
     *
     * @param props Options for rendering; <code>lang</code>, and <code>loadCallback</code> are currently supported by recaptcha.
     * @return HTML code, suitable for embedding into a webpage.
     */
    String createCaptchaExplicit(props) {
        return getRecaptchaInstance().createRecaptchaExplicitHtml(props)
    }

    /**
     * Create a JSON-like string containing parameters to be passed to the ReCaptcha JavScript object when using explicit mode.
     * @param props Options for rendering; <code>theme</code>, <code>type</code>, <code>tabindex</code>, <code>callback</code>, <code>expired-callback</code> are currently supported
     * @return
     */
    String createRenderParameters(props) {
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
    String createScriptEntry(props) {
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
    boolean verifyAnswer(session, remoteAddress, params) {
        if (!isEnabled()) {
            return true
        }

        def success = getRecaptchaInstance().checkAnswer(remoteAddress, params["g-recaptcha-response"]?.trim())
        session["recaptcha_error"] = success ? null : true
        return success
    }

    /**
     * Get a value indicating if the ReCaptcha plugin should be enabled.
     */
    boolean isEnabled() {
        return safeGetConfigValue('enabled', true)
    }

    /**
     * Get a value indicating if the previous verification attempt failed.
     *
     * @param session The current session
     */
    boolean validationFailed(session) {
        return (session["recaptcha_error"] != null)
    }

    /**
     * Cleanup resources associated with the session. This does have to be called, but not calling it will leave the recaptcha
     * object in memory until the session expires.
     *
     * @param session The current session.
     */
    void cleanUp(session) {
        session["recaptcha_error"] = null
    }
}
