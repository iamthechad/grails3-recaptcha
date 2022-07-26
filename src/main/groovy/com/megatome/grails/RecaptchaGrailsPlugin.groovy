package com.megatome.grails

import grails.plugins.Plugin

class RecaptchaGrailsPlugin extends Plugin {

    def grailsVersion = "5.0.0 > *"
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    def title = "ReCaptcha support for Grails" // Headline display name of the plugin
    def author = "Chad Johnston"
    def authorEmail = "cjohnston@megatome.com"
    def description = '''\
Protect your website from spam and abuse while letting real people pass through with ease.
'''
    def profiles = ['web']
    def documentation = "http://iamthechad.github.com/grails3-recaptcha/"
    def license = "APACHE"
    def issueManagement = [system: "Github", url: "https://github.com/iamthechad/grails3-recaptcha/issues"]
    def scm = [url: "https://github.com/iamthechad/grails3-recaptcha"]
}