# Chapter 26: Build Tools & Project Structure -- Java

A real Java project is rarely a single `.java` file you compile by hand. It is dozens or hundreds of source files, a tree of third-party libraries (each with their *own* dependencies), test code that must compile separately, resources to bundle, and a final artifact (a JAR, a container image, a native binary) that must be produced the same way on every developer's machine and on the continuous-integration server. A **build tool** automates all of this: it compiles, resolves and downloads dependencies, runs tests, packages, and publishes — driven by a declarative project description rather than a hand-maintained shell script. The two that dominate the Java ecosystem are **Maven** (XML-based, convention-driven, since 2004) and **Gradle** (script-based, highly programmable, since 2009). This chapter explains why build tools exist, the standard project layout they all assume, how Maven and Gradle each work, how dependency management actually resolves versions, and how to package your application — culminating in `jlink` custom runtimes and a mention of GraalVM native images.

> **C++ contrast:** This chapter's analogue in the C++ world is **Make / CMake** plus a package manager like **Conan** or **vcpkg**. CMake is closest in spirit to Gradle (a programmable build description that generates the actual build), while Maven's rigid convention-over-configuration model has no exact C++ counterpart. The crucial difference: Maven and Gradle have a *single, universal* package repository (Maven Central) and a standard dependency-coordinate scheme baked in, whereas C++ dependency management has historically been fragmented across system packages, vendored sources, and competing managers.

> **Note:** Unlike most chapters in this book, Chapter 26 has **no standalone `.java` code example** — it is about tooling and configuration, not language features. All the relevant snippets (`pom.xml`, `build.gradle.kts`, shell commands) are shown inline below.

## 26.1 Why Build Tools

Imagine compiling a small application by hand. You write `javac` invocations listing every source file, then a `-cp` (classpath) flag naming every JAR your code depends on, then *their* JARs, and so on transitively. You manually download those JARs from various websites, hoping the versions are compatible. You write another script to run JUnit, another to assemble everything into a deliverable JAR with a correct `MANIFEST.MF`. Every new dependency, every new developer, every new machine breaks something. This does not scale.

```bash
# The manual approach — unmanageable beyond a toy project
javac -cp "libs/guava-33.4.0.jar:libs/jackson-databind-2.18.2.jar:..." \
      -d out $(find src/main/java -name "*.java")
# ...then manually fetch every transitive dependency, build the classpath
# for tests, and craft the jar by hand. Repeat on every machine. Fragile.
```

A build tool replaces all of that with a single declarative file describing *what* your project is (its coordinates, dependencies, and packaging), and provides standard commands (`mvn package`, `gradle build`) that do the rest. The four jobs every Java build tool performs:

| Job | What it means |
|-----|---------------|
| **Compile** | Run `javac` over the right sources with the right classpath and target release |
| **Resolve dependencies** | Download declared libraries *and their transitive dependencies* from repositories |
| **Test** | Compile and run the test suite (JUnit, etc.), failing the build on test failure |
| **Package** | Assemble a JAR/WAR (or fat JAR, container, native image) ready to ship |

The overriding goal is a **repeatable build**: the same inputs produce the same outputs anywhere, with no "works on my machine" surprises.

---

## 26.2 Standard Project Layout

Both Maven and Gradle assume the same conventional directory structure (Maven coined it; Gradle adopted it). Following the convention means the tools need almost no configuration to find your code, your tests, and your resources.

```text
my-app/
├── pom.xml                 (Maven)  — or  build.gradle.kts (Gradle)
├── src/
│   ├── main/
│   │   ├── java/           production source code (.java)
│   │   │   └── com/example/App.java
│   │   └── resources/      non-code files bundled onto the classpath
│   │       └── application.properties
│   └── test/
│       ├── java/           test source code (compiled & run separately)
│       │   └── com/example/AppTest.java
│       └── resources/      test-only resources
└── target/ (Maven)  or  build/ (Gradle)   — generated output (never commit)
```

The key distinctions: `src/main/java` (production code) and `src/test/java` (tests) are compiled into separate classpaths, so test-only dependencies never leak into your shipped artifact. Anything under `src/main/resources` (config files, templates, `.properties` bundles) is copied onto the runtime classpath and read with `getResourceAsStream(...)`. Generated output goes to `target/` (Maven) or `build/` (Gradle), which you add to `.gitignore` and never commit.

> **C++ contrast:** C++ has no enforced layout — `src/`, `include/`, `lib/` are common conventions but every project differs, and CMake must be told explicitly where everything is. Java's universal `src/main/java` convention means any developer can clone any Maven/Gradle project and immediately know where the code lives.

---

## 26.3 Maven: pom.xml and Coordinates

Maven is configured by a **Project Object Model** file, `pom.xml`. Every artifact in the Maven universe — yours and every library — is identified by three **coordinates**: `groupId` (an organization namespace, reverse-DNS like `com.fasterxml.jackson.core`), `artifactId` (the project name, e.g. `jackson-databind`), and `version` (e.g. `2.18.2`). These three values uniquely locate a JAR in a repository.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- This project's own coordinates -->
    <groupId>com.example</groupId>
    <artifactId>my-app</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>          <!-- jar (default), war, pom... -->

    <properties>
        <!-- Target Java 21 LTS; UTF-8 is the default since Java 18 -->
        <maven.compiler.release>21</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
</project>
```

The `<packaging>` element decides the artifact type (`jar`, `war` for web apps, or `pom` for a parent/aggregator project). `maven.compiler.release` pins the Java release the bytecode targets — preferred over the older `source`/`target` pair because it also checks you only use APIs available in that release.

---

## 26.4 Maven: Dependencies and Scopes

Dependencies are declared by coordinates inside `<dependencies>`. Maven downloads each one *and everything it transitively needs* from a repository (Maven Central by default) into your local cache (`~/.m2/repository`). A **scope** controls *when* a dependency is on the classpath — crucially keeping test-only libraries out of your shipped artifact.

```xml
<dependencies>
    <!-- compile scope (default): available everywhere, packaged into the artifact -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.18.2</version>
    </dependency>

    <!-- test scope: on the test classpath only, NOT shipped -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.11.4</version>
        <scope>test</scope>
    </dependency>

    <!-- provided scope: needed to compile, but supplied at runtime by the container -->
    <dependency>
        <groupId>jakarta.servlet</groupId>
        <artifactId>jakarta.servlet-api</artifactId>
        <version>6.1.0</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

| Scope | Compile classpath | Test classpath | Runtime / packaged | Typical use |
|-------|:---:|:---:|:---:|-------------|
| `compile` (default) | ✅ | ✅ | ✅ | Normal libraries |
| `provided` | ✅ | ✅ | ❌ | Servlet API, supplied by the server |
| `runtime` | ❌ | ✅ | ✅ | JDBC drivers, loaded at runtime |
| `test` | ❌ | ✅ | ❌ | JUnit, Mockito, AssertJ |

---

## 26.5 Maven: The Build Lifecycle

Maven's defining idea is a fixed, ordered **lifecycle** of *phases*. Running any phase runs every phase before it. You almost never invoke a plugin directly — you invoke a phase, and Maven runs the plugins bound to it.

```text
validate → compile → test → package → verify → install → deploy
```

| Phase | What happens |
|-------|--------------|
| `validate` | Check the project is correct and all info is available |
| `compile` | Compile `src/main/java` into `target/classes` |
| `test` | Run unit tests (Surefire plugin); build fails if any fail |
| `package` | Bundle compiled code into a JAR/WAR in `target/` |
| `verify` | Run integration tests / quality checks (Failsafe plugin) |
| `install` | Copy the artifact into the local repo (`~/.m2`) for other local projects |
| `deploy` | Upload the artifact to a remote/shared repository |

```bash
mvn clean            # delete target/ (the 'clean' lifecycle)
mvn compile          # compile main sources
mvn test             # compile + run tests
mvn package          # compile + test + build the jar  (the everyday command)
mvn verify           # + integration tests
mvn install          # + install to local ~/.m2 repository
mvn clean package    # combine: clean first, then build  (very common)
mvn -DskipTests package   # build the jar but skip running tests
```

> **C++ contrast:** Make has *targets* you wire together with explicit dependency rules; you decide the names and the order. Maven inverts this with a *standard, named lifecycle* every project shares — `mvn package` means the same thing in every Maven project on earth, whereas a Makefile's `make all` could mean anything the author chose.

---

## 26.6 Maven: Plugins and Fat JARs

Maven itself is just a plugin executor; the actual work is done by **plugins** bound to lifecycle phases. The three you meet first:

- **Compiler plugin** — runs `javac` (configured via `maven.compiler.release`).
- **Surefire plugin** — runs unit tests during the `test` phase.
- **Shade** or **Assembly plugin** — builds a *fat JAR* (a.k.a. uber JAR): a single JAR containing your code *plus all dependencies unpacked inside it*, so it runs with a plain `java -jar app.jar` and no external classpath.

```xml
<build>
    <plugins>
        <!-- Build a runnable fat/uber jar with all dependencies included -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.6.0</version>
            <executions>
                <execution>
                    <phase>package</phase>           <!-- bind to the package phase -->
                    <goals><goal>shade</goal></goals>
                    <configuration>
                        <transformers>
                            <!-- Set the Main-Class in the jar's manifest -->
                            <transformer
                              implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                <mainClass>com.example.App</mainClass>
                            </transformer>
                        </transformers>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

```bash
mvn clean package          # produces target/my-app-1.0.0.jar (a runnable fat jar)
java -jar target/my-app-1.0.0.jar
```

A plain `jar` packaging produces a "thin" JAR with *only your* classes — running it requires putting every dependency on the classpath yourself. The shade/assembly plugins solve that by inlining everything.

---

## 26.7 Maven: Repositories and Maven Central

Maven resolves dependencies from **repositories**. Resolution order is: the **local repository** (`~/.m2/repository`, a cache on your machine), then any **remote repositories** — with **Maven Central** (`repo.maven.apache.org`) configured by default. The first time you build, declared dependencies (and their transitive dependencies) are downloaded and cached locally; subsequent builds are offline-fast.

```xml
<!-- Most projects need NO <repositories> block — Maven Central is built in.
     Add one only for libraries hosted elsewhere: -->
<repositories>
    <repository>
        <id>my-company-repo</id>
        <url>https://nexus.example.com/repository/maven-public/</url>
    </repository>
</repositories>
```

Companies typically run an internal repository manager (Nexus, Artifactory) that proxies Central and hosts private artifacts. You publish to it with `mvn deploy`.

---

## 26.8 Gradle: build.gradle(.kts), Plugins, Dependencies

Gradle replaces XML with a *programmable build script*, written in either the **Groovy DSL** (`build.gradle`) or the **Kotlin DSL** (`build.gradle.kts`). The Kotlin DSL is recommended today: it gives IDE autocompletion, type checking, and clearer errors. A Gradle build applies **plugins** (e.g. `java`, `application`), declares **repositories** and **dependencies** with configuration names (`implementation`, `testImplementation` — Gradle's equivalent of scopes), and is built from **tasks**.

```kotlin
// build.gradle.kts — Kotlin DSL (recommended)
plugins {
    java
    application                         // adds 'run' task and an app distribution
}

group = "com.example"
version = "1.0.0"

java {
    toolchain {                         // download/use a specific JDK
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()                      // resolve dependencies from Maven Central
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    testImplementation(platform("org.junit:junit-bom:5.11.4"))  // a BOM (see 26.11)
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass = "com.example.App"
}

tasks.test {
    useJUnitPlatform()                  // run with the JUnit 5 engine
}
```

Gradle's configuration names map onto Maven scopes: `implementation` ≈ `compile` (but hidden from downstream consumers), `testImplementation` ≈ `test`, `runtimeOnly` ≈ `runtime`, `compileOnly` ≈ `provided`. Note the *compact* dependency notation `"group:artifact:version"` — the same three coordinates as Maven, just colon-separated.

> **Groovy vs Kotlin DSL:** Groovy (`build.gradle`) is dynamically typed and terser in places (`implementation 'g:a:v'` without parentheses); Kotlin (`build.gradle.kts`) is statically typed with full IDE support and is the direction the Gradle team recommends for new projects. The concepts are identical; only the syntax differs.

---

## 26.9 Gradle: Tasks and the Wrapper

Where Maven has a fixed lifecycle, Gradle is a **directed graph of tasks**. Plugins contribute tasks (`compileJava`, `test`, `jar`, `build`, `run`), and each task declares which others it depends on, so Gradle runs only what's needed and caches up-to-date results (incremental builds).

```bash
gradle tasks         # list available tasks
gradle build         # compile, test, and assemble (the everyday command)
gradle test          # run tests only
gradle run           # run the application (from the 'application' plugin)
gradle clean         # delete build/
```

Critically, you almost never run a globally-installed `gradle`. Instead you run the **Gradle Wrapper** — a small `gradlew`/`gradlew.bat` script plus `gradle/wrapper/gradle-wrapper.properties` that pins an exact Gradle version and downloads it on first use. Checking the wrapper into version control means every developer and the CI server build with the *identical* Gradle version, with nothing to install.

```bash
./gradlew build          # Linux/macOS — uses the project's pinned Gradle version
gradlew.bat build        # Windows

# Generate or upgrade the wrapper (commit the result):
gradle wrapper --gradle-version 8.12
```

> Maven has an analogous **Maven Wrapper** (`./mvnw`, `mvnw.cmd`), introduced more recently. The principle is the same: pin the tool version in the repo so builds are reproducible.

---

## 26.10 Maven vs Gradle

Both tools solve the same problems; the choice is largely about philosophy and ecosystem.

| Aspect | Maven | Gradle |
|--------|-------|--------|
| **Config format** | XML (`pom.xml`) — declarative, verbose | Groovy/Kotlin DSL — programmable, concise |
| **Build model** | Fixed lifecycle of phases | Task graph (flexible, customizable) |
| **Flexibility** | Convention over configuration; rigid | Highly scriptable; can do anything |
| **Performance** | Slower; limited incrementality | Build cache + incremental builds + daemon = fast |
| **Learning curve** | Gentle; predictable structure | Steeper; more concepts |
| **Customization** | Write/configure a plugin | Inline task logic in the script |
| **Wrapper** | `mvnw` (newer) | `gradlew` (long-standing, idiomatic) |
| **Best for** | Standard projects, easy onboarding, stability | Large/multi-module builds, Android, custom logic |

A reasonable default: **Maven** if you want convention, simplicity, and a build that looks like every other Java build; **Gradle** if you need speed on a large codebase or custom build logic (and it is mandatory for Android). Both pull from the same Maven Central repository and use the same coordinate scheme, so dependencies are portable between them.

---

## 26.11 Dependency Management: Transitive Deps, Conflicts, BOMs

The hardest part of dependency management is *transitivity*. When you depend on library A, and A depends on B v1.2, and you also depend on C which depends on B v1.5, you have a **version conflict** — but only one version of B can be on the classpath. Both tools resolve this automatically but with different rules:

- **Maven** uses *nearest-wins*: the version closest to your project in the dependency tree is chosen.
- **Gradle** uses *highest-version-wins* by default: it picks the highest requested version.

You inspect the resolved tree to see what actually won:

```bash
mvn dependency:tree                       # Maven: print the full dependency tree
./gradlew dependencies                    # Gradle: print resolved dependencies
```

A **BOM** (Bill of Materials) is a special POM that declares a coherent *set* of versions that are tested to work together. You import it once and then reference member artifacts *without versions*, so everything stays aligned (common for Jackson, JUnit, Spring).

```xml
<!-- Maven: import a BOM in <dependencyManagement> -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.fasterxml.jackson</groupId>
            <artifactId>jackson-bom</artifactId>
            <version>2.18.2</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
<!-- Now jackson-databind etc. can be declared with NO <version> -->
```

```kotlin
// Gradle: import a BOM with platform(...)
dependencies {
    implementation(platform("com.fasterxml.jackson:jackson-bom:2.18.2"))
    implementation("com.fasterxml.jackson.core:jackson-databind") // version from BOM
}
```

Versions follow **semantic versioning** (`MAJOR.MINOR.PATCH`): a bump in MAJOR signals breaking API changes, MINOR adds features compatibly, PATCH is bug fixes only. Treat a MAJOR upgrade as a migration, not a routine bump.

> **C++ contrast:** Transitive dependency resolution is something C++ historically lacked entirely — you tracked and built each dependency's dependencies yourself, or relied on system packages. Conan and vcpkg brought version resolution to C++, but Java has had automatic, repository-backed transitive resolution as a baseline since 2004.

---

## 26.12 Packaging: JAR, Fat JAR, jlink, Native Image

The end product of a build is a deployable artifact. The options, from simplest to most advanced:

**Thin JAR** — your compiled classes plus a manifest, but *no* dependencies. Smallest, but you must supply the classpath at runtime.

```bash
java -cp "my-app.jar:libs/*" com.example.App   # dependencies provided externally
```

**Fat / uber JAR** — your classes *and* all dependencies in one runnable JAR (built with shade/assembly in Maven, or the `application`/Shadow plugin in Gradle). The standard way to ship a self-contained Java application that runs with `java -jar`.

**`jlink` custom runtime** — `jlink` (JDK 9+) assembles a *minimal*, self-contained Java runtime image containing only the JDK modules your application actually uses. The result is a directory with its own `bin/java` and no need for a pre-installed JDK — far smaller than bundling the full JDK.

```bash
# Create a trimmed runtime image with only the modules you need
jlink --add-modules java.base,java.sql \
      --output myapp-runtime \
      --strip-debug --compress=2 --no-header-files --no-man-pages

./myapp-runtime/bin/java -m com.example/com.example.App
```

**Native image (GraalVM)** — `native-image` ahead-of-time compiles a Java application into a *single native executable* with near-instant startup and a tiny memory footprint, with no JVM at runtime. It is the basis of fast-starting frameworks (Quarkus, Spring Native, Micronaut), at the cost of longer build times and some reflection-configuration effort. Mentioned here as the cutting edge of Java packaging — see GraalVM's tooling for details.

| Artifact | Contains | Needs a JDK installed? | Startup |
|----------|----------|:---:|:---:|
| Thin JAR | Your classes only | Yes (+ classpath) | Normal |
| Fat/uber JAR | Your classes + all deps | Yes | Normal |
| `jlink` image | App + trimmed JVM | No (self-contained) | Normal |
| GraalVM native image | Single native binary | No | Near-instant |

---

## 26.13 The Module System Interplay (Brief)

Since Java 9, the **Java Platform Module System** (JPMS) lets a project declare a `module-info.java` naming the modules it `requires` and the packages it `exports`. Build tools understand modules: if a `module-info.java` is present, Maven and Gradle compile and run on the *module path* rather than the classpath. Modular projects are what make `jlink` possible — `jlink` walks the `requires` graph to decide which JDK modules to include. Most application code still runs perfectly well on the classpath without a `module-info.java`; modules become important mainly for libraries that want strong encapsulation and for `jlink`-based packaging. (The module system is covered in depth in the advanced features chapter.)

---

## 26.14 Continuous Integration (Brief)

Build tools shine in **CI** (continuous integration): a server (GitHub Actions, GitLab CI, Jenkins) that checks out the code and runs the build on every push, catching breakage early. Because the build is fully described by `pom.xml`/`build.gradle.kts` and pinned by the wrapper, the CI run is identical to a local one.

```yaml
# .github/workflows/build.yml — a minimal GitHub Actions build
name: build
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
      - run: ./gradlew build          # or: mvn -B clean verify
```

---

## 26.15 Best Practices

```text
# ✅ Check the wrapper (gradlew / mvnw + wrapper files) into version control
#    → every developer and CI uses the identical, pinned tool version

# ✅ Pin exact dependency versions; avoid "latest"/dynamic versions
#    → reproducible builds; no surprise upgrades breaking the build

# ✅ Use a BOM to keep related libraries' versions aligned (Jackson, JUnit, Spring)

# ✅ Put test-only libraries in test scope (Maven) / testImplementation (Gradle)
#    → they never leak into the shipped artifact

# ✅ Keep dependencies minimal — every dependency is attack surface + transitive weight
#    audit with:  mvn dependency:tree   /   ./gradlew dependencies

# ✅ Pin the Java release explicitly (maven.compiler.release / toolchain) → Java 21 LTS

# ✅ Add target/ and build/ to .gitignore — never commit generated output

# ✅ Run the full build (incl. tests) in CI on every push
```

The themes: pin everything (tool version, dependency versions, Java release) so builds are reproducible; keep the dependency footprint small and scoped correctly; and let the build tool — not a hand-written script — own compilation, dependency resolution, testing, and packaging.

---

## Summary

| Concept | Maven | Gradle |
|---------|-------|--------|
| **Config file** | `pom.xml` (XML) | `build.gradle.kts` (Kotlin) / `build.gradle` (Groovy) |
| **Coordinates** | `groupId` / `artifactId` / `version` | `"group:artifact:version"` |
| **Dependency scopes** | `compile` / `provided` / `runtime` / `test` | `implementation` / `compileOnly` / `runtimeOnly` / `testImplementation` |
| **Build model** | Fixed lifecycle (validate→…→deploy) | Task graph |
| **Everyday command** | `mvn clean package` | `./gradlew build` |
| **Wrapper** | `./mvnw` | `./gradlew` |
| **Conflict rule** | Nearest-wins | Highest-version-wins |
| **Repository** | Maven Central (`~/.m2` cache) | Maven Central (Gradle cache) |
| **Fat JAR** | Shade / Assembly plugin | `application` / Shadow plugin |
| **Trimmed runtime** | `jlink` | `jlink` |
| **Native binary** | GraalVM `native-image` | GraalVM `native-image` |

---

## Next Steps
- Scaffold a project with the standard `src/main/java` / `src/test/java` layout
- Write a `pom.xml` (or `build.gradle.kts`), add a dependency, and run `mvn package` / `./gradlew build`
- Inspect transitive dependencies with `mvn dependency:tree` / `./gradlew dependencies` and align them with a BOM
- Produce a runnable fat JAR, then experiment with a `jlink` custom runtime
- Wire the build into CI so it runs on every push
- (No standalone `.java` example for this chapter — the configuration snippets above are the deliverables.)
- Move to [Chapter 27: Internationalization, Formatting & BigDecimal](../27_internationalization/README.md)
