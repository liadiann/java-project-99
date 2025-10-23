plugins {
	java
    jacoco
    checkstyle
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"
    id("io.freefair.lombok") version "8.6"
    id("org.sonarqube") version "6.2.0.5505"
    kotlin("kapt") version "1.9.25"
}

group = "hexlet.code"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

sonar {
    properties {
        property("sonar.projectKey", "liadiann_java-project-99")
        property("sonar.organization", "liadiann")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-devtools")
    implementation ("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:3.2.2")
    testImplementation ("org.instancio:instancio-junit:5.5.1")
    implementation ("org.springframework.boot:spring-boot-starter-validation")
    implementation("net.datafaker:datafaker:2.5.1")
    implementation ("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor ("org.mapstruct:mapstruct-processor:1.6.3")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    implementation ("org.postgresql:postgresql:42.7.7")
    implementation ("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")
    runtimeOnly("com.h2database:h2")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestReport)
}
