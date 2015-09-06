package com.megatome.grails

import grails.plugins.Plugin

class RecaptchaGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.0.1 > *"
    // resources that are excluded from plugin packaging
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

    // URL to the plugin's documentation
    def documentation = "http://iamthechad.github.com/grails-recaptcha/"

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Location of the plugin's issue tracker.
    def issueManagement = [ system: "Github", url: "https://github.com/iamthechad/grails-recaptcha/issues" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/iamthechad/grails-recaptcha" ]
}
