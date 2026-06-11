# KopiLang (v1.0.0)

> **The Entrance to Java.** A language designed to facilitate a smooth transition into the Java ecosystem

KopiLang is an open-source language built specifically for beginners. **The core objective of KopiLang is not to replace Java, but to help developers transition to it.** We're offering an approachable syntax that eliminates intimidating boilerplate, KopiLang allows learners to focus entirely on core programming logic. Because it transpiles directly into standard Java source and bytecode, you interact with the Java Virtual Machine (JVM) and existing Java libraries natively from day one, making your eventual move to raw Java completely seamless and not shocking.

---

## Technical Features

* **Zero Toolchain Configuration:** Execute the compiler jarfile directly using any standard JVM installation. There is no need for external SDK configurations or separate random tooling ecosystems.
* **Native Java Integration:** Transpiles source code directly into Java, allowing developers to leverage the extensive Java ecosystem, existing tooling, and standard libraries natively.
* **Clean Syntax:** Semicolons are removed as statement terminators. Functions default to auto-void execution, and redundant verbose keywords are heavily optimized.
* **Predictable Workflow:** Write your source code, transpile it to a native `.class` file or transpile to `.java` file, and execute the generated output natively on the JVM.

---

## Getting Started
### System Requirements
* **Java 26 or higher** is required to run the compiler and execute the transpiled output.

### Compilation and Execution Lifecycle
1. **Download the jarfile** Download the latest version of KopiLang on [the official website](https://kopilang.pages.dev/#download)
2. **Initialize Source** Create a source file named `MyProgram.kpi`. The `.kpi` file extension is mandatory. Code your program logic within this file
3. **Transpile and Compile** Invoke the compiler via the command-line interface to generate the bytecode artifacts:
```bash
java -jar KopiLang.jar MyProgram.kpi
```
1. **Execute Bytecode** Run the resulting Java .class file using the standard Java runtime execution tool
```bash
java MyProgram
```

---

## Documentation and Community
* **Technical Reference** Refer to [the official docs](https://kopilang.pages.dev/docs.html) for complete documentation, language specifications, and syntax guides
* **Support Channel** For community support, development discussions, or technical inquiries, join the official [Discord Server]

## Licensing and Governance
KopiLang is distributed as open-source software under the terms of the GNU GPL v3 to ensure transparency and encourage community contributions. Pull requests and forks are welcome

---

**Disclaimer: KopiLang is an independent project maintained by WatermanMC. It is not operated, sponsored, or affiliated with Oracle Corporation**
