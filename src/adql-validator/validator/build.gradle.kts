plugins {
    application
}

version = "1.0"

/* *************************************************************************
 * * DEPENDENCIES                                                          *
 * ************************************************************************* */

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.jcommander)
    implementation(libs.json)

    testImplementation(libs.bundles.junit.jupiter)
    testImplementation(libs.awaitibility)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

/* *************************************************************************
 * * CLASS TO RUN                                                          *
 * ************************************************************************* */

application {
    mainClass = "cds.adql.validation.ADQLValidatorRunner"
}

/* *************************************************************************
 * * JAVA VERSION                                                          *
 * ************************************************************************* */

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

/* *************************************************************************
 * * JAR MANIFEST                                                          *
 * ************************************************************************* */

tasks.jar {
    manifest {
        attributes(
            "Implementation-Version" to project.version,
            "Main-Class" to "cds.adql.validation.ADQLValidatorRunner",
            "Class-Path" to configurations.runtimeClasspath.get().joinToString(" ") { it.name }
        )
    }
}
