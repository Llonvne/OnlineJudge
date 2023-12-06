plugins {
    kotlin("multiplatform")
}

group = "cn.llonvne"
version = "unspecified"

kotlin {
    jvm {
    }
    js(IR) {
        browser {

        }
        binaries.executable()
    }
}

repositories {
    mavenCentral()
}
