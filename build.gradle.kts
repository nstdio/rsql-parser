@file:Suppress("UnstableApiUsage")

plugins {
  groovy
  jacoco
  `java-library`
  `jvm-test-suite`
  `maven-publish`
  signing
  id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
  id("org.javacc.javacc") version "4.0.1"
  id("net.researchgate.release") version "3.1.0"
}

repositories {
  mavenCentral()
}

java {
  withSourcesJar()
  withJavadocJar()

  toolchain {
    languageVersion.set(JavaLanguageVersion.of(8))
  }
}

sourceSets {
  main {
    java.srcDirs(project.layout.buildDirectory.dir("generated/javacc"))
  }
}

testing {
  suites {
    val test by getting(JvmTestSuite::class) {
      useJUnitJupiter("5.13.1")

      dependencies {
        implementation("nl.jqno.equalsverifier:equalsverifier:3.19.3")
        implementation("org.spockframework:spock-core:2.4-M6-groovy-4.0")
      }
    }

    register<JvmTestSuite>("fuzzTest") {
      dependencies {
        implementation(project())
        implementation("junit:junit:4.13.2")
        implementation("edu.berkeley.cs.jqf:jqf-fuzz:2.1")
        runtimeOnly("org.junit.vintage:junit-vintage-engine:5.13.1")
      }

      targets {
        all {
          testTask.configure {
            shouldRunAfter(test)
          }
        }
      }
    }
  }
}

dependencies {
  compileOnly("net.jcip:jcip-annotations:1.0")

  javacc("net.java.dev.javacc:javacc:7.0.13")
}

group = "io.github.nstdio"
description = "RSQL-parser"

publishing {
  publications.create<MavenPublication>("java") {
    from(components["java"])

    pom {
      name.set("RSQL-parser")
      description.set("Parser of RSQL / FIQL (query language for RESTful APIs) written in JavaCC.")
      url.set("https://github.com/nstdio/rsql-parser")
      inceptionYear.set("2011")

      licenses {
        license {
          name.set("MIT")
          url.set("https://opensource.org/licenses/MIT")
        }
      }

      developers {
        developer {
          id.set("jirutka")
          name.set("Jakub Jirutka")
          organization.set("CTU in Prague")
          organizationUrl.set("https://www.cvut.cz")
        }
        developer {
          id.set("nstdio")
          name.set("Edgar Asatryan")
          email.set("nstdio@gmail.com")
        }
      }

      scm {
        connection.set("scm:git:git@github.com:nstdio/rsql-parser.git")
        developerConnection.set("scm:git:git@github.com:nstdio/rsql-parser.git")
        url.set("https://github.com/nstdio/rsql-parser")
      }

      ciManagement {
        system.set("GitHub Actions")
        url.set("https://github.com/nstdio/rsql-parser/actions")
      }

      issueManagement {
        system.set("GitHub Issues")
        url.set("https://github.com/nstdio/rsql-parser/issues")
      }
    }
  }
}

signing {
  isRequired = (version as String).endsWith("SNAPSHOT")

  val signingKey = findProperty("signingKey") as String?
  val signingPassword = findProperty("signingPassword") as String?
  useInMemoryPgpKeys(signingKey, signingPassword)

  sign(publishing.publications["java"])
}

nexusPublishing {
  repositories {
    sonatype {
      val baseUri = uri("https://s01.oss.sonatype.org")
      nexusUrl.set(baseUri.resolve("/service/local/"))
      snapshotRepositoryUrl.set(baseUri.resolve("/content/repositories/snapshots/"))
    }
  }
}

release {
  tagTemplate.set("v\${version}")
  git {
    requireBranch.set("master")
    pushToRemote.set("origin")
  }
}

tasks {
  test {
    useJUnitPlatform()
  }

  withType<JacocoReport> {
    reports {
      val isCI = System.getenv("CI").toBoolean()
      xml.required.set(isCI)
      html.required.set(!isCI)
    }
  }

  withType<Test> {
    finalizedBy(named("jacocoTestReport"))
  }

  withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
  }

  withType<Javadoc>().configureEach {
    options.encoding = "UTF-8"
    (options as? StandardJavadocDocletOptions)?.tags(
      "apiNote:a:API Note:"
    )
  }

  withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
  }

  compileJavacc {
    outputDirectory = outputDirectory.resolve("cz/jirutka/rsql/parser")
  }

  named("sourcesJar") {
    dependsOn(compileJavacc)
  }

  named("afterReleaseBuild") {
    dependsOn("publishToSonatype", "closeAndReleaseSonatypeStagingRepository")
  }

  named("check") {
    dependsOn(testing.suites.named("fuzzTest"))
  }
}

tasks.named<JavaCompile>("compileFuzzTestJava") {
  javaCompiler.set(javaToolchains.compilerFor {
    languageVersion.set(JavaLanguageVersion.of(21))
  })
}

tasks.named<Test>("fuzzTest") {
  javaLauncher.set(javaToolchains.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(21))
  })
}
