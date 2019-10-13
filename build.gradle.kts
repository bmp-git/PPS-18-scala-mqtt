group = "org.bmp.pps.scala-mqtt"
version = "1.0.0"

plugins {
    scala
    application
    id("org.scoverage") version "3.0.0"
    id("com.github.maiflai.scalatest") version "0.21"
    id("cz.augi.gradle.wartremover") version "0.9.7"
}

application {
    mainClassName = "RxMain"
}

val run by tasks.getting(JavaExec::class) {
    standardInput = System.`in`
}

tasks {
    register("fatJar", Jar::class.java) {
        archiveClassifier.set("all")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest {
            attributes("Main-Class" to "RxMain")
        }
        from(configurations.runtimeClasspath.get()
                .onEach { println("add from dependencies: ${it.name}") }
                .map { if (it.isDirectory) it else zipTree(it) })
        val sourcesMain = sourceSets.main.get()
        sourcesMain.allSource.forEach { println("add from sources: ${it.name}") }
        from(sourcesMain.output)
    }
}


repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("org.scala-lang:scala-library:2.12.2")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("com.typesafe.scala-logging:scala-logging_2.12:3.9.2")

    compile("org.scala-lang:scala-library:2.12.2")
    compile("io.reactivex:rxscala_2.12:0.26.5")
    compile("com.lihaoyi:fastparse_2.12:2.1.2")
    testImplementation("org.scalatest:scalatest_2.12:3.0.1")
    testImplementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.0")
    testRuntimeOnly("org.pegdown:pegdown:1.4.2")
    scoverage("org.scoverage:scalac-scoverage-plugin_2.12:1.3.1")
    scoverage("org.scoverage:scalac-scoverage-runtime_2.12:1.3.1")
}

wartremover {
    //errorWarts.remove("wartremover:Any")
    warningWarts.remove("Any")
    warningWarts.remove("NonUnitStatements")
    warningWarts.remove("StringPlusAny")
}