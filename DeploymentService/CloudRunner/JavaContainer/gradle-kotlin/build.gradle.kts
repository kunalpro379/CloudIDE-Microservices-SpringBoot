plugins {
    kotlin("jvm") version "1.6.21"
    application
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
}

application {
    mainClass.set("com.example.gradlekotlin.AppKt")
}
