import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
    kotlin("plugin.serialization")
    kotlin("multiplatform")
    id("io.spring.dependency-management")
    id("org.springframework.boot")
    kotlin("plugin.spring")
    id("io.kvision")
    id("de.jensklingenberg.ktorfit")
}

version = "1.0.0-SNAPSHOT"
group = "com.example"

repositories {
    mavenCentral()
    mavenLocal()
}

// Versions
val kotlinVersion: String by System.getProperties()
val kvisionVersion: String by System.getProperties()
val coroutinesVersion: String by project
val r2dbcPostgresqlVersion: String by project
val r2dbcH2Version: String by project
val e4kVersion: String by project
val ktorfitVersion = "1.10.2"
val komapperVersion = "1.15.0"

dependencies {
    implementation("com.benasher44:uuid:0.7.0")
}
rootProject.plugins.withType(org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin::class.java) {
    rootProject.the<YarnRootExtension>().yarnLockMismatchReport =
        YarnLockMismatchReport.WARNING // NONE | FAIL
    rootProject.the<YarnRootExtension>().reportNewYarnLock = false // true
    rootProject.the<YarnRootExtension>().yarnLockAutoReplace = false // true
}

extra["kotlin.version"] = kotlinVersion
extra["kotlin-coroutines.version"] = coroutinesVersion

kotlin {
    jvmToolchain(17)
    jvm {
        withJava()
        compilations.all {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict")
            }
        }
    }
    js(IR) {
        browser {
            runTask {
                mainOutputFileName = "main.bundle.js"
                sourceMaps = false
                devServer =
                    KotlinWebpackConfig.DevServer(
                        open = false,
                        port = 3000,
                        proxy =
                            mutableMapOf(
                                "/kv/*" to "http://localhost:8080",
                                "/login" to "http://localhost:8080",
                                "/logout" to "http://localhost:8080",
                                "/kvws/*" to mapOf("target" to "ws://localhost:8080", "ws" to true),
                            ),
                        static = mutableListOf("${layout.buildDirectory.asFile.get()}/processedResources/js/main"),
                    )
            }
            webpackTask {
                mainOutputFileName = "main.bundle.js"
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        binaries.executable()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api("io.kvision:kvision-server-spring-boot:$kvisionVersion")
                implementation(projects.goJudgeApi)
                implementation("de.jensklingenberg.ktorfit:ktorfit-lib:$ktorfitVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("reflect"))
                implementation("org.springframework.boot:spring-boot-starter")
                implementation("org.springframework.boot:spring-boot-devtools")
                implementation("org.springframework.boot:spring-boot-starter-webflux")
                implementation("org.springframework.boot:spring-boot-starter-security")
                implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
                implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

                implementation("org.komapper:komapper-spring-boot-starter-r2dbc:$komapperVersion")
                runtimeOnly("org.postgresql:postgresql")
                runtimeOnly("org.postgresql:r2dbc-postgresql")
                implementation("org.komapper:komapper-dialect-postgresql-r2dbc:$komapperVersion")
                implementation("org.springframework.boot:spring-boot-docker-compose")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$coroutinesVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutinesVersion")

                implementation("io.ktor:ktor-client-okhttp:2.2.4")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.6")

                implementation("io.ktor:ktor-serialization-kotlinx-json:2.2.4")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation("org.springframework.boot:spring-boot-starter-test")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("io.kvision:kvision-toastify:$kvisionVersion")
                implementation("io.kvision:kvision:$kvisionVersion")
                implementation("io.kvision:kvision-bootstrap:$kvisionVersion")
                implementation("io.kvision:kvision-state:$kvisionVersion")
                implementation("io.kvision:kvision-fontawesome:$kvisionVersion")
                implementation("io.kvision:kvision-i18n:$kvisionVersion")
                implementation("io.kvision:kvision-routing-navigo-ng:$kvisionVersion")
                implementation("io.kvision:kvision-richtext:$kvisionVersion")
                implementation("io.kvision:kvision-chart:$kvisionVersion")
                implementation("io.kvision:kvision-tabulator:$kvisionVersion")
                implementation("io.kvision:kvision-tom-select:$kvisionVersion")
                implementation("io.kvision:kvision-jquery:$kvisionVersion")
                implementation("io.kvision:kvision-datetime:$kvisionVersion")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
                implementation("io.kvision:kvision-testutils:$kvisionVersion")
            }
        }
    }
}
dependencies {
    add("kspCommonMainMetadata", "org.komapper:komapper-processor:$komapperVersion")
    add("kspJvm", "org.komapper:komapper-processor:$komapperVersion")
}

springBoot {
    mainClass = "cn.llonvne.MainKt"
}

tasks.configureEach {
    if (name == "kspKotlinJvm") {
        mustRunAfter(tasks.getByName("kspCommonMainKotlinMetadata"))
    }
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions.freeCompilerArgs +=
            listOf(
                "-opt-in=org.komapper.annotation.KomapperExperimentalAssociation",
                "-Xcontext-receivers",
            )
    }
}

ksp {
    arg("komapper.enableEntityMetamodelListing", "true")
    arg("komapper.enableEntityProjection", "true")
}
