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
val ktorVersion = "2.3.6"

fun ktor(name: String) = ("io.ktor:$name:$version")
fun ktorServer(name: String) = ktor("ktor-server-$name")

dependencies {
    implementation(projects.goJudgeApi)
//    ksp("de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorfitVersion")
//    implementation("de.jensklingenberg.ktorfit:ktorfit-lib:1.10.2")
    ksp("de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorfitVersion")
    implementation("de.jensklingenberg.ktorfit:ktorfit-lib:$ktorfitVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:1.12.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.2.4")
    // SERVER KTOR
    implementation(ktorServer("netty"))
    implementation(ktorServer("resources"))
    implementation(ktorServer("auto-head-response"))
    implementation(ktorServer("request-validation"))
    implementation(ktorServer("content-negotiation"))
    implementation(ktorServer("html-builder"))
    implementation(ktorServer("auth"))
    implementation(ktorServer("compression"))
    implementation(ktorServer("core-jvm"))
    implementation(ktorServer("double-receive"))
    implementation(ktorServer("call-logging"))
    implementation(ktorServer("host-common"))
    implementation(ktorServer("status-pages"))
    implementation(ktorServer("metrics-micrometer"))
    implementation(ktorServer("cors"))
    implementation(ktorServer("rate-limit"))
    // CLIENT KTOR
    implementation("io.ktor:ktor-client-resources:2.3.6")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.6")
    implementation("io.ktor:ktor-client-core:2.3.6")
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