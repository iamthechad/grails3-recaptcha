package com.megatome.grails

import com.megatome.grails.mailhide.security.MailhideEncryption
import grails.test.mixin.TestFor
import groovy.mock.interceptor.StubFor
import org.grails.plugins.codecs.Base64Codec
import spock.lang.Specification

@TestFor(MailhideService)
class MailhideServiceTest extends Specification {

    void "load with no config"() {
        when:
        service.createMailhideURL("a@b.com")

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("configuration not specified")
    }

    void "load with empty public key"() {
        setup:
        config.mailhide.publicKey = ""
        config.mailhide.privateKey = ""

        when:
        service.createMailhideURL("a@b.com")

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("Public Key must be specified")
    }

    void "load with empty private key"() {
        setup:
        config.mailhide.publicKey = "ABC"
        config.mailhide.privateKey = ""

        when:
        service.createMailhideURL("a@b.com")

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("Private Key must be specified")
    }

    void "test create URL"() {
        setup:
        def encryptStub = new StubFor(MailhideEncryption.class)
        encryptStub.demand.encrypt() { s -> s.getBytes() }
        mockCodec(Base64Codec)
        mockCodec(URLSafeBase64Codec)
        config.mailhide.publicKey = "ABC"
        config.mailhide.privateKey = "123"
        def response = null

        when:
        encryptStub.use {
            response = service.createMailhideURL("a@b.com")
        }

        then:
        response.contains("http://www.google.com/recaptcha/mailhide")
        response.contains("k=ABC")
        response.contains("c=")

        when: "next invocation should access cache"
        response = service.createMailhideURL("a@b.com").toString()

        then:
        response.contains("http://www.google.com/recaptcha/mailhide")
        response.contains("k=ABC")
        response.contains("c=")
    }
}
