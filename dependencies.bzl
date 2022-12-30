"""
This module contains all of the dependencies installed via rules_jvm_external
"""

#####################
# JAVA DEPENDENCIES #
#####################
MAVEN_REPOS = [
    "https://repo1.maven.org/maven2",
]

NULLAWAY_VERSION = "0.10.2"
FINDBUGS_VERSION = "3.0.2"

JAVA_DEPENDENCIES = [
    # nullaway
    "com.google.code.findbugs:jsr305:%s" % FINDBUGS_VERSION,
    "com.uber.nullaway:nullaway:%s" % NULLAWAY_VERSION,
    ####### codegen ###########
    "com.squareup:javapoet:1.13.0",
]