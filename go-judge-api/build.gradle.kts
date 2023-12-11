plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
    id("de.jensklingenberg.ktorfit")
}

repositories {
    mavenCentral()
}

group = "cn.llonvne"
version = "unspecified"

val ktorfitVersion = "1.10.2"
val kopyKatVersion = "1.0.4"

kotlin {
    jvm {
    }
    js {
        browser {
            binaries.executable()
        }
        nodejs {
            binaries.executable()
        }
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.2")
                implementation("io.ktor:ktor-client-resources:2.3.2")
                implementation("io.ktor:ktor-client-core:2.3.2")
                implementation("com.benasher44:uuid:0.8.2")
                implementation("de.jensklingenberg.ktorfit:ktorfit-lib:$ktorfitVersion")

                implementation(project.dependencies.platform("io.arrow-kt:arrow-stack:1.2.0"))
                implementation("io.arrow-kt:arrow-optics:1.2.0")
            }
        }
        jvmMain {
            dependencies {

            }
        }
        jsMain {
            dependencies {

            }
        }
    }
}



dependencies {
//    add("kspCommonMainMetadata", "at.kopyk:kopykat-ksp:$kopyKatVersion")
//    add("kspJs", "at.kopyk:kopykat-ksp:$kopyKatVersion")
//    add("kspJvm", "at.kopyk:kopykat-ksp:$kopyKatVersion")

    add("kspCommonMainMetadata", "de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorfitVersion")
    add("kspJvm", "de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorfitVersion")
    add("kspJs", "de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorfitVersion")

    add("kspCommonMainMetadata", "io.arrow-kt:arrow-optics-ksp-plugin:1.2.0")
    add("kspJs", "io.arrow-kt:arrow-optics-ksp-plugin:1.2.0")
    add("kspJvm", "io.arrow-kt:arrow-optics-ksp-plugin:1.2.0")
}

ksp {
    arg("generate", "annotated")
}

tasks.withType<Wrapper> {
    gradleVersion = "8.1.1"
    distributionType = Wrapper.DistributionType.BIN
}

