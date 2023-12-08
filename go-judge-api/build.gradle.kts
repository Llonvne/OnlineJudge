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
                api("de.jensklingenberg.ktorfit:ktorfit-annotations:1.10.2")
            }
        }
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "8.1.1"
    distributionType = Wrapper.DistributionType.BIN
}

