plugins {
    kotlin("multiplatform")
    id("io.ktor.plugin")
    id("com.google.devtools.ksp")
    id("de.jensklingenberg.ktorfit")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
}

group = "cn.llonvne"
version = "unspecified"
dependencies {
    implementation("io.ktor:ktor-server-auto-head-response-jvm:2.3.6")
    implementation("io.ktor:ktor-server-core-jvm:2.3.6")
    implementation("io.ktor:ktor-server-host-common-jvm:2.3.6")
    implementation("io.ktor:ktor-server-status-pages-jvm:2.3.6")
    implementation("io.ktor:ktor-server-double-receive-jvm:2.3.6")
    implementation("io.ktor:ktor-server-call-logging-jvm:2.3.6")
    implementation("io.ktor:ktor-server-auth-jvm:2.3.6")
    implementation("io.ktor:ktor-server-compression-jvm:2.3.6")
    implementation("io.ktor:ktor-server-cors-jvm:2.3.6")
    implementation("io.ktor:ktor-server-metrics-micrometer-jvm:2.3.6")
    implementation("io.micrometer:micrometer-registry-prometheus:1.6.3")
    implementation("io.ktor:ktor-client-okhttp-jvm:2.3.6")
}

kotlin {
//    jvmToolchain(17)
    jvm {
        withJava()
        compilations.all {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict")
            }
        }
    }

    val ktorfitVersion = "1.10.2"

    dependencies{
        add("kspCommonMainMetadata", "de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorfitVersion")
        add("kspJvm","de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorfitVersion")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("de.jensklingenberg.ktorfit:ktorfit-lib-light:1.10.2")
                implementation("io.ktor:ktor-server-core")
                implementation("io.ktor:ktor-server-netty")
                implementation("io.ktor:ktor-server-resources")
                implementation("io.ktor:ktor-server-auto-head-response")
                implementation("io.ktor:ktor-server-request-validation")
                implementation("io.ktor:ktor-server-content-negotiation")
                implementation("io.ktor:ktor-client-content-negotiation")
                implementation("io.ktor:ktor-serialization-kotlinx-json")
                implementation("io.ktor:ktor-server-rate-limit")
                implementation("io.ktor:ktor-server-html-builder")
                implementation("io.ktor:ktor-server-auth")
                implementation("io.ktor:ktor-server-compression")
                implementation("io.ktor:ktor-server-cors")
                implementation("io.ktor:ktor-client-core")

                implementation("io.ktor:ktor-server-metrics-micrometer")
                implementation("io.micrometer:micrometer-registry-prometheus:1.12.0")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("ch.qos.logback:logback-classic:1.2.0")
                implementation("io.ktor:ktor-client-okhttp")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "8.1.1"
    distributionType = Wrapper.DistributionType.BIN
}

application {
    mainClass = "MainKt"
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}
