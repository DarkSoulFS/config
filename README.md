# configurate [![Build Status](https://travis-ci.org/SpongePowered/configurate.svg?branch=master)](https://travis-ci.org/SpongePowered/configurate)
configurate is a simple configuration library for Java applications that provides a node-based representation of data, able to handle a wide variety of configuration formats.

Want to talk to us about configurate? Join us in the `#dev` channel on our [Discord](https://discord.gg/PtaGRAs) or the `#spongedev` channel on `irc.esper.net`.

The current supported formats are:

* [JSON](https://www.json.org/)
* [HOCON](https://github.com/lightbend/config/blob/master/HOCON.md)
* [YAML](http://yaml.org/)
* [XML](https://www.w3.org/XML/)

## Project Structure
The project is split into different modules.

#### configurate core
configurate-core is the base of the library, containing the main APIs used to manipulate configurations. It is generic, and does not depend on any specific format of configuration.

#### configurate loaders
Each distinct configuration format is implemented as a "configuration loader", in a separate module.

A number of loader implementations are provided as standard in this project, however it is possible to implement a custom loader for a new format separately.

The current supported loaders provided by the project are:

* `configurate-gson` - Implementation for the JSON format, using the [Gson](https://github.com/google/gson) library for parsing and generation
* `configurate-hocon` - Implementation for the HOCON format, using the [lightbend config](https://github.com/lightbend/config) library for parsing and generation
* `configurate-jackson` - Implementation for the JSON format, using the [Jackson](https://github.com/FasterXML/jackson-core) library for parsing and generation
* `configurate-xml` - Implementation for the XML format, using the [JAXP](https://docs.oracle.com/javase/tutorial/jaxp/index.html) library for parsing and generation
* `configurate-yaml` - Implementation for the YAML format, using the [SnakeYAML](https://bitbucket.org/asomov/snakeyaml) library for parsing and generation


## Usage

* To use configurate, your project must be configured to use Java 8 or higher.
* Release and snapshot artifacts are hosted on SpongePowered's Maven Repository, available at https://repo.spongepowered.org/maven/.

The latest release is: `3.6`

If your project uses Maven or Gradle, just add the following to your build scripts.

#### Gradle

```groovy
repositories {
    mavenCentral()
    maven {
        name = 'sponge'
        url = 'https://repo.spongepowered.org/maven'
    }
}

dependencies {
    // Modify this line to target the loader you wish to use.
    compile 'org.spongepowered:configurate-hocon:3.6'
}
```

#### Maven

```xml
<dependencies>
    <dependency>
        <groupId>org.spongepowered</groupId>
        <!-- Modify this line to target the loader you wish to use. -->
        <artifactId>configurate-hocon</artifactId>
        <version>3.6</version>
    </dependency>
</dependencies>

<repositories>
    <repository>
        <id>sponge</id>
        <url>https://repo.spongepowered.org/maven</url>
    </repository>
</repositories>
```

More detailed usage instructions can be found in the [configurate wiki](https://github.com/SpongePowered/configurate/wiki).

## Contributing

#### Clone
The following steps will ensure your project is cloned properly.

1. `git clone https://github.com/SpongePowered/configurate.git`
2. `cd configurate`

#### Building
**Note:** If you do not have [Gradle](https://www.gradle.org/) installed then use ./gradlew for Unix systems or Git Bash and gradlew.bat for Windows systems in place of any 'gradle' command.

In order to build configurate you simply need to run the `gradle build` command. You can find the compiled JAR files in `./build/libs`  (found in each subproject) labeled similarly to 'configurate-subproject-x.x-SNAPSHOT.jar'.

#### Pull Requests
We love PRs! However, when contributing, here are some things to keep in mind:

- Take a look at open issues first before you get too far in -- someone might already be working on what you were planning on doing
- In general, we follow the Oracle style guidelines for code style
- Please, please, please test PRs. It makes the process a lot easier for everybody :)
