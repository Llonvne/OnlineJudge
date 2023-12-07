plugins {
    val kotlinVersion: String by System.getProperties()
    kotlin("plugin.serialization") version kotlinVersion apply false
    kotlin("multiplatform") version kotlinVersion apply false
    id("io.spring.dependency-management") version System.getProperty("dependencyManagementPluginVersion") apply false
    id("org.springframework.boot") version System.getProperty("springBootVersion") apply false
    kotlin("plugin.spring") version kotlinVersion apply false
    val kvisionVersion: String by System.getProperties()
    id("io.kvision") version kvisionVersion apply false
    id("de.jensklingenberg.ktorfit") version "1.10.2" apply false
    id("io.ktor.plugin") version "2.3.6" apply false
    id("com.google.devtools.ksp") version "1.9.21-1.0.15" apply false
}
