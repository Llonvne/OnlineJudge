plugins {
    kotlin("jvm")
    id("io.ktor.plugin")
    id("com.google.devtools.ksp") version "1.9.21-1.0.15"
    kotlin("plugin.serialization")
    id("de.jensklingenberg.ktorfit")
}

repositories {
    mavenCentral()
}

group = "cn.llonvne"
version = "unspecified"

val ktorfitVersion = "1.10.2"
val ktorVersion = "2.3.7"

fun ktor(name: String) = ("io.ktor:$name")
fun ktorServer(name: String) = ktor("ktor-server-$name")

dependencies {
    // Ktorfit
    implementation("de.jensklingenberg.ktorfit:ktorfit-lib-light:$ktorfitVersion")

    // API
    implementation(projects.goJudgeApi)

    // ARROW
    implementation("io.arrow-kt:arrow-core:1.2.0")
    implementation("io.arrow-kt:arrow-fx-coroutines:1.2.0")
    implementation("io.arrow-kt:suspendapp:0.4.0")
    implementation("io.arrow-kt:suspendapp-ktor:0.4.0")

    // MONITOR
    implementation("io.micrometer:micrometer-registry-prometheus:1.12.0")

    // JSON
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.2.4")

    // SERVER KTOR
    implementation("io.ktor:ktor-server-core:2.3.7")
    implementation("io.ktor:ktor-server-netty:2.3.7")
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
    implementation("io.ktor:ktor-client-okhttp:2.2.4")

    // LOGGING
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
    implementation("org.slf4j:slf4j-api:2.0.5")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.20.0")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    testImplementation("io.ktor:ktor-server-test-host-jvm:2.3.7")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.21")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.6.4")

    // DOT ENV
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    // FOR TEST
    testImplementation(kotlin("test"))

    // DEV CONTAINER
    implementation("org.testcontainers:kafka:1.19.3")
}

tasks.withType<Wrapper> {
    gradleVersion = "8.1.1"
    distributionType = Wrapper.DistributionType.BIN
}

application {
    mainClass = "MainKt"
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}