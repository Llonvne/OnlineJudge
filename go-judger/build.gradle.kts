import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.libsDirectory

plugins {
    kotlin("jvm")
    id("io.ktor.plugin")
    id("com.google.devtools.ksp") version "1.9.21-1.0.15"
    id("de.jensklingenberg.ktorfit")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
}

group = "cn.llonvne"
version = "unspecified"

val ktorfitVersion = "1.10.2"

dependencies {
    implementation(projects.goJudgeApi)
//    ksp("de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorfitVersion")
//    implementation("de.jensklingenberg.ktorfit:ktorfit-lib:1.10.2")
    ksp("de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorfitVersion")
    implementation("de.jensklingenberg.ktorfit:ktorfit-lib:$ktorfitVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:1.12.0")

    // SERVER KTOR
    implementation("io.ktor:ktor-server-netty:2.2.4")
    implementation("io.ktor:ktor-server-resources:2.2.4")
    implementation("io.ktor:ktor-server-auto-head-response:2.2.4")
    implementation("io.ktor:ktor-server-request-validation:2.2.4")
    implementation("io.ktor:ktor-server-content-negotiation:2.2.4")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.2.4")
    implementation("io.ktor:ktor-server-html-builder:2.2.4")
    implementation("io.ktor:ktor-server-auth:2.2.4")
    implementation("io.ktor:ktor-server-compression:2.2.4")
    implementation("io.ktor:ktor-server-core-jvm:2.3.6")
    implementation("io.ktor:ktor-server-double-receive-jvm:2.3.6")
    implementation("io.ktor:ktor-server-call-logging-jvm:2.3.6")
    implementation("io.ktor:ktor-server-host-common-jvm:2.3.6")
    implementation("io.ktor:ktor-server-status-pages-jvm:2.3.6")
    implementation("io.ktor:ktor-server-metrics-micrometer:2.2.4")
    implementation("io.ktor:ktor-server-cors-jvm:2.3.6")
    // CLIENT KTOR
    implementation("io.ktor:ktor-client-content-negotiation:2.2.4")
    implementation("io.ktor:ktor-client-core:2.2.4")
    implementation("ch.qos.logback:logback-classic:1.4.7")
    implementation("io.ktor:ktor-client-okhttp:2.2.4")

    testImplementation(kotlin("test"))
}


tasks.withType<Wrapper> {
    gradleVersion = "8.1.1"
    distributionType = Wrapper.DistributionType.BIN
}

application {
    mainClass = "MainKt"
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}