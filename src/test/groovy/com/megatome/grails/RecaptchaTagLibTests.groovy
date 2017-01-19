package com.megatome.grails

/**
 * Copyright 2010-2017 Megatome Technologies
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

import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(RecaptchaTagLib)
class RecaptchaTagLibTests extends Specification {
    def recapMock = Mock(RecaptchaService.class)

    def setup() {
        tagLib.recaptchaService = recapMock
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
}
