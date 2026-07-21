plugins {
    kotlin("jvm") version "2.3.20"
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    jacoco
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
}

javafx {
    version = "25"
    modules = listOf("javafx.controls", "javafx.graphics")
}

application {
    // A plain launcher (not an Application subclass) avoids the
    // "JavaFX runtime components are missing" error on non-modular runs.
    mainClass.set("MainKt")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

/** Coverage scoped to student-authored pattern / program / panel code. */
val studentCoverageClasses =
        sourceSets.main.get().output.asFileTree.matching {
            include(
                    "observer/**",
                    "command/**",
                    "api/FollowLineProgram*",
                    "api/StudentPrograms*",
                    "ui/LabelObserver*",
                    "ui/TelemetryPanel*",
                    "ui/ControlPanel*",
            )
        }

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    classDirectories.setFrom(studentCoverageClasses)
    reports {
        html.required.set(true)
        xml.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    classDirectories.setFrom(studentCoverageClasses)
    violationRules {
        rule {
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
        }
        rule {
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

tasks.check { dependsOn(tasks.jacocoTestCoverageVerification) }

