plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
}

group = "cn.llonvne"
version = "unspecified"

kotlin {
    jvm {
    }
    js(IR) {
        browser()
        nodejs()
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.2")
                implementation("io.ktor:ktor-client-resources:2.3.2")
                implementation("io.ktor:ktor-client-core:2.3.2")
                implementation("com.benasher44:uuid:0.8.2")
            }
        }
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "8.1.1"
    distributionType = Wrapper.DistributionType.BIN
}

