plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.benchmark") version "0.4.4"
    id("org.jetbrains.kotlin.plugin.allopen")
}
allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}
repositories {
    mavenCentral()
}
kotlin {
    jvmToolchain(17)
    jvm {

    }
    js {
        nodejs()
        binaries.executable()
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.4")
                implementation(projects.goJudgeApi)
            }
        }
        jsMain {
            dependencies {
            }
        }
        jvmMain {
            dependencies {
            }
        }
    }
}
benchmark {
    targets {
        register("jvm")
        register("js")
    }
}