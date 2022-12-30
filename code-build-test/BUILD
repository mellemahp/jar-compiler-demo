load("@rules_jvm_external//:defs.bzl", "artifact")

JAVAC_OPTS = [
     # Sets nullaway errors to break build
     "-Xep:NullAway:ERROR",
     # sets packages for nullaway to run on
     "-XepOpt:NullAway:AnnotatedPackages=com.hmellema",
]

java_plugin(
    name = "nullaway_plugin",
    deps = [
        artifact("com.uber.nullaway:nullaway")
    ],
    visibility = ["//:__subpackages__"]
)

java_library(
    name = "nullaway",
    exported_plugins = ["nullaway_plugin"],
    neverlink = True,
    visibility = ["//:__subpackages__"],
)

java_binary(
    name = "codebuild",
    srcs = glob(["src/main/java/com/hmellema/codebuild/**/*.java"]),
    deps = [
        ":nullaway",
        # pulls in nullable annotation
        artifact("com.google.code.findbugs:jsr305"),
        artifact("com.squareup:javapoet")
    ],
    javacopts = JAVAC_OPTS,
    main_class = "com.hmellema.codebuild.Runner",
    visibility = ["//visibility:public"],
)