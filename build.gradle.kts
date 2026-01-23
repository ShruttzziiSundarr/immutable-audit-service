plugins {
    id("org.springframework.boot") version "3.2.2"  // <--- Changed from 4.0.2 to 3.2.2
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.spring") version "1.9.20"
    kotlin("plugin.jpa") version "1.9.20"
}

group = "com.fin"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
    // 1. The Web Server (for your API)
    implementation("org.springframework.boot:spring-boot-starter-web")

    // 2. The Database Tools (Fixes the @Entity and @Table errors)
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql") // The driver for Postgres

    // 3. Kafka (For listening to transactions)
    implementation("org.springframework.kafka:spring-kafka")

    // 4. Redis (For fast caching later)
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // 5. Kotlin Helpers (JSON parsing & Reflection)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // 6. Testing Tools
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
