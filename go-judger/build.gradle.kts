plugins {
    kotlin("jvm")
    id("io.ktor.plugin")
    id("com.google.devtools.ksp") version "1.9.21-1.0.15"
    kotlin("plugin.serialization")
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
    implementation(projects.goJudgeApi)

    // ARROW
    implementation("io.arrow-kt:arrow-core:1.2.0")
    implementation("io.arrow-kt:arrow-fx-coroutines:1.2.0")
    implementation("io.arrow-kt:suspendapp:0.4.0")
    implementation("io.arrow-kt:suspendapp-ktor:0.4.0")


    implementation("io.micrometer:micrometer-registry-prometheus:1.12.0")
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
    implementation("ch.qos.logback:logback-classic:1.4.7")
    implementation("io.ktor:ktor-client-okhttp:2.2.4")

    // KATFA
    implementation("io.github.nomisrev:kotlin-kafka:0.3.0")
    implementation("org.apache.kafka:connect-runtime:3.6.1")
    implementation("org.apache.kafka:kafka-clients:3.6.1")
    implementation("org.apache.kafka:kafka-streams:3.6.1")
    implementation("org.testcontainers:kafka:1.19.3")


    // COPYKAT
    val kopyKatVersion = "1.0.4"
    ksp("at.kopyk:kopykat-ksp:$kopyKatVersion")
    compileOnly("at.kopyk:kopykat-annotations:$kopyKatVersion")

    // FOR TEST
    testImplementation(kotlin("test"))
}

ksp {
    arg("generate", "annotated")
}


tasks.withType<Wrapper> {
    gradleVersion = "8.1.1"
    distributionType = Wrapper.DistributionType.BIN
}

application {
    mainClass = "MainKt"
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}