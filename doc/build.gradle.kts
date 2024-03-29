plugins {
    id("org.asciidoctor.jvm.convert") version "3.3.2"
}

tasks {
    "asciidoctor"(org.asciidoctor.gradle.jvm.AsciidoctorTask::class) {
        sourceDir(".")
        baseDirIsProjectDir()
        resources(delegateClosureOf<CopySpec> {
            from("img") {
                include("**/*.jpeg")
                include("**/*.png")
                include("**/*.svg")
                into("img")
            }
          from("concept/img") {
            include("**/*.jpeg")
            include("**/*.png")
            include("**/*.svg")
            into("concept/img")
          }
        })
    }
}

asciidoctorj {
    modules {
        diagram.use()
        diagram.setVersion("1.5.16")
    }
}
