version = "1.0.0"

plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("com.github.ben-manes.versions") version "0.52.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.guava)
    implementation(libs.jackson)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

application {
    mainClass = "org.pz.polyglot.App"
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    testLogging {
        events("passed")
    }
}

tasks.register<Jar>("fatJar") {
    archiveBaseName.set("pz-polyglot")
    archiveClassifier.set("all")
    manifest {
        attributes["Main-Class"] = "org.pz.polyglot.App"
    }
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register<Exec>("createPortable") {
    dependsOn("fatJar")
    
    doFirst {
        delete("pz-polyglot")
    }
    
    commandLine(
        "jpackage",
        "--input", "build/libs",
        "--name", "pz-polyglot",
        "--main-jar", "pz-polyglot-${version}-all.jar",
        "--type", "app-image",
        "--win-console",
        "--module-path", configurations.runtimeClasspath.get().asPath,
        "--add-modules", "javafx.controls,javafx.fxml"
    )
}

tasks.register<Zip>("packagePortable") {
    dependsOn("createPortable")
    from("pz-polyglot")
    archiveFileName.set("pz-polyglot-${version}-portable.zip")
    destinationDirectory.set(file("build"))
}