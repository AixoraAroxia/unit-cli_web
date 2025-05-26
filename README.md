
# **UnitCLI** – Unit Test Generator for Java

**UnitCLI** is a powerful command-line tool designed for Java developers to **streamline and accelerate** the creation of unit tests. It automatically generates well-structured test cases using **JUnit** and **Mockito**, allowing you to focus on building robust applications while maintaining high code quality.

With just a few commands, UnitCLI helps you:

* Quickly generate unit tests for your classes.
* Choose between **Mockito** or plain **JUnit** based testing.
* Analyze your project to get a summary of all classes.
* Explore your project's folder structure visually via the CLI.
* Improve test coverage and code reliability.

### 🚀 **Benefits of Using UnitCLI**

* **Faster development cycles:** Automate the creation of repetitive test code.
* **Increased code quality:** Catch bugs earlier with consistent unit testing.
* **Safe refactoring:** Make changes confidently, knowing that automated tests cover your code.
* **Documentation through tests:** Unit tests act as living documentation for how your code behaves.
* **Simplified test setup:** Ready-to-use CLI for macOS and Windows makes integration easy.

---

## 📁 Project Structure

### `unit-test-dist.zip`

```
unit-cli/
├── cli/
│   ├── unit                 # Shell script (macOS/Linux)
│   └── unit.bat             # Batch script (Windows)
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── unitTestGenerator/
│                   └── App.java
├── pom.xml                  # Maven build configuration
└── Makefile                 # Build and packaging script
```

### `cli-dist.zip`

```
cli/
├── unit                     # Shell script
├── unit.bat                 # Batch script
├── unit-1.0.0-jar-with-dependencies.jar
└── Makefile
```

---

## 🛠️ Build Instructions

To build and package the application:

```bash
# Step 1: Build with Maven
mvn clean package

# Step 2: Create distribution
make dist

# Step 3: Unzip the packaged CLI
unzip unit-test-dist.zip
```

---

## 🍎 macOS Installation

### Unpack and Install

```bash
unzip unit-test-dist.zip -d unit-test-dist
cd unit-test-dist/cli
sudo ./unit install
```

### Usage (macOS/Linux)

```bash
unit [command] [options]
```

**Available Commands:**

* `install`             Install the application
* `uninstall`          Remove the application
* `--version`, `-v`    Show version
* `--help`, `-h`        Show this help message

---

## 🪟 Windows Installation

### Run as Administrator

Open **CMD** or **PowerShell** as Administrator and run:

```bash
cli\unit.bat install
```

### Usage (Windows)

```bash
unit [command]
```

**Available Commands:**

* `install`       Install the application
* `uninstall`    Remove the application
* `--version`    Show version
* `--help`      Show help message

---

## 📦 Future Enhancements (optional section)

* Support for test coverage metrics
* Integration with CI/CD tools
* Interactive test configuration
* IDE plugins for IntelliJ and Eclipse


Download
You can download the file here:
👉 [Download ZIP](https://github.com/AixoraAroxia/unit_cli/releases/download/version/unit-test-dist.zip)


## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](https://www.apache.org/licenses/LICENSE-2.0) file for details.