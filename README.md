# KopiLang
Write simple syntax that becomes Java code.

KopiLang is not a replacement for Java; it is designed to help beginners transition to Java with a more approachable and easy syntax. It also allows Java libraries to be seamlessly integrated. KopiLang is open-source under the GNU GPL v3 license for transparency and community contributions.

## Features
- **No external ecosystem required:** Ships as a simple executable JAR. Use it directly with any JVM installation.
- **Runs on Java:** Transpiles source code into Java, allowing you to leverage the full Java ecosystem and tooling you already know.
- **Simple workflow:** Write source, transpile, build to a `.class` or `.java` file, and execute.

## Example
```
pub class Main {
    # This is a comment
    // still valid comment

    pub stc main(String[] args) { # Automatically void
        # Semicolons disabled as statement terminator
        # You have to make statements in one line
        println("Hello World! Imma gonna count from 1 to 100.")
        for (int i = 1; i <= 100; i++) {
            println("Number: " + i)
        }
    }
}
```

## How to Use
1. **Create a source file:** Write your code in a file with the mandatory `.kpi` extension (e.g., `MyProgram.kpi`).
2. **Transpile:** Run the KopiLang JAR to convert your code:
```java KopiLang MyProgram.kpi```
3. **Execute:** Run the generated Java `.class` file on the JVM:
```java MyProgram```

## Requirements
Java 26 or higher

## Documentation and Resources
- [Documentation](https://kopilang.pages.dev/docs)
- [Discord](https://discord.gg/Zjd5ucYV3)

## License
KopiLang is licensed under the GNU GPL v3.

Copyright WatermanMC 2026. This project is not operated, sponsored, or affiliated with Oracle Corporation.
