package com.megatome.grails

import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(RecaptchaTagLib)
class RecaptchaTagLibTest extends Specification {
    def recapMock = Mock(RecaptchaService.class)
    def mailhideMock = Mock(MailhideService.class)

    def setup() {
        mailhideMock.createMailhideURL(_ as String) >> "http://fake.url.com"
        tagLib.recaptchaService = recapMock
        tagLib.mailhideService = mailhideMock
    }

    void "test recaptcha tag"() {
        when:
        tagLib.recaptcha()

        then:
        1 * recapMock.createCaptcha([:])

        when:
        tagLib.recaptcha(theme:"dark", lang:"fr", type:"audio", size: "normal", successCallback: "successCB", expiredCallback: "expiredCB", tabindex: 1, includeScript: true)

        then:
        1 * recapMock.createCaptcha([theme:"dark", lang:"fr", type:"audio", size: "normal", successCallback: "successCB", expiredCallback: "expiredCB", tabindex: 1, includeScript: true])

        when:
        tagLib.recaptcha(theme:"dark", foo:"bar")

        then:
        1 * recapMock.createCaptcha([theme: "dark"])
    }

    void "test recaptchaExplicit tag"() {
        when:
        tagLib.recaptchaExplicit()

        then:
        1 * recapMock.createCaptchaExplicit([:])

        when:
        tagLib.recaptchaExplicit(lang:"fr", loadCallback: "loadCB")

        then:
        1 * recapMock.createCaptchaExplicit([lang:"fr", loadCallback: "loadCB"])

        when:
        tagLib.recaptchaExplicit(lang:"fr", foo:"bar")

        then:
        1 * recapMock.createCaptchaExplicit([lang:"fr"])
    }

    void "test renderParameters tag"() {
        when:
        tagLib.renderParameters()

        then:
        1 * recapMock.createRenderParameters([:])

        when:
        tagLib.renderParameters(theme:"dark", type:"audio", size: "normal", successCallback: "successCB", expiredCallback: "expiredCB", tabindex: 1)

        then:
        1 * recapMock.createRenderParameters([theme:"dark", type:"audio", size: "normal", successCallback: "successCB", expiredCallback: "expiredCB", tabindex: 1])

        when:
        tagLib.renderParameters(theme:"dark", lang:"fr", foo:"bar")

        then:
        1 * recapMock.createRenderParameters([theme:"dark"])
    }

    void "test script tag"() {
        when:
        tagLib.script()

        then:
        1 * recapMock.createScriptEntry([:])

        when:
        tagLib.script(lang:"fr")

        then:
        1 * recapMock.createScriptEntry([lang:"fr"])

        when:
        tagLib.script(lang:"fr", foo:"bar")

        then:
        1 * recapMock.createScriptEntry([lang:"fr"])
    }

    void "test ifEnabled tag"() {
        setup:
        recapMock.isEnabled() >> [true, false]

        when:
        def response = tagLib.ifEnabled(null, { "Enabled" }).toString()

        then:
        1 * recapMock.isEnabled() >> true
        response == "Enabled"

        when:
        response = tagLib.ifEnabled(null, { "Enabled" }).toString()

        then:
        1 * recapMock.isEnabled() >> false
        response == ""

        when:
        tagLib.ifEnabled()

        then:
        1 * recapMock.isEnabled() >> false
    }

    void "test ifDisabled tag"() {
        setup:
        recapMock.isEnabled() >> [false, true]

        when:
        def response = tagLib.ifDisabled(null, { "Disabled" }).toString()

        then:
        1 * recapMock.isEnabled() >> false
        response == "Disabled"

        when:
        response = tagLib.ifDisabled(null, { "Disabled" }).toString()

        then:
        1 * recapMock.isEnabled() >> true
        response == ""
    }

    void "test ifFailed tag"() {
        setup:
        recapMock.validationFailed(_) >> [true, false]

        when:
        def response = tagLib.ifFailed(null, { "Failed" }).toString()

        then:
        1 * recapMock.validationFailed(_) >> true
        response == "Failed"

        when:
        response = tagLib.ifFailed(null, { "Success" }).toString()

        then:
        1 * recapMock.validationFailed(_) >> false
        response.toString() == ""
    }

    void "test mailhide tag"() {
        when:
        tagLib.mailhide()

        then:
        thrown(IllegalArgumentException)

        when:
        def response = tagLib.mailhide(emailAddress: 'a@b.com', { "Link wrap text" }).toString()

        then:
        1 * mailhideMock.createMailhideURL("a@b.com") >> "http://fake.url.com"
        response.contains("http://fake.url.com")
        response.contains("Link wrap text")
    }

    void "test mailhideURL tag"() {
        when:
        tagLib.mailhideURL()

        then:
        thrown(IllegalArgumentException)

        when:
        def response = tagLib.mailhideURL(emailAddress: 'a@b.com', { var -> var }).toString()

        then:
        1 * mailhideMock.createMailhideURL("a@b.com") >> "http://fake.url.com"
        response.contains("mailhideURL:")

        when:
        response = tagLib.mailhideURL([emailAddress: 'a@b.com', var: 'foo'], { var -> var }).toString()

        then:
        1 * mailhideMock.createMailhideURL("a@b.com") >> "http://fake.url.com"
        response.contains("foo:")
    }
}
