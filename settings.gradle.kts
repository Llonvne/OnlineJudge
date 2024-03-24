plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
include("go-judge-api")
include("online-judge-web")
// enable type safe accessor
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include("go-judger")
include("benchmark")
