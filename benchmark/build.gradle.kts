plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlinx.benchmark") version "0.4.10"
    kotlin("plugin.allopen")
}
allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

group = "cn.llonvne"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(projects.goJudgeApi)
    implementation("io.ktor:ktor-client-okhttp-jvm:2.3.6")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.6")

    implementation("io.ktor:ktor-serialization-kotlinx-json:2.2.4")
    implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.10")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

benchmark {
    targets {
        register("main")
    }
}
