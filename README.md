# gradle-json-schema

[![Release](https://api.bintray.com/packages/michaelbull/maven/gradle-json-schema/images/download.svg)](https://bintray.com/michaelbull/maven/gradle-json-schema/_latestVersion) [![License](https://img.shields.io/github/license/michaelbull/gradle-json-schema.svg)](https://github.com/michaelbull/gradle-json-schema/blob/master/LICENSE)

A Gradle custom task for validating a JSON document using JSON Schema.

## Installation

```kotlin
buildscript {
    repositories {
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://dl.bintray.com/michaelbull/maven")
    }

    dependencies {
        classpath("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.7")
        classpath("com.michael-bull.gradle-json-schema:gradle-json-schema:1.0.0")
    }
}
```

## Usage

```kotlin
import com.github.michaelbull.gradle.json.ValidateSchema

val validateExamples by tasks.registering(ValidateSchema::class) {
    description = "Validate example files"

    schema.set(file("example.schema.json"))
    inputDir.set(layout.projectDirectory.dir("input"))
    report.set(reporting.baseDirectory.file("example-report.json"))

    validator {
        failEarly()
    }
}
```

## Contributing

Bug reports and pull requests are welcome on [GitHub][github].

## License

This project is available under the terms of the ISC license. See the
[`LICENSE`](LICENSE) file for the copyright information and licensing terms.

[github]: https://github.com/michaelbull/gradle-json-schema
