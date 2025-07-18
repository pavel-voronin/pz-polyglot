# Product Overview
pz-polyglot is a modern Java desktop application for managing translations in Project Zomboid mods. It provides a UI for viewing, editing, and organizing translation files across multiple sources, languages, and game versions. The project uses JavaFX for the UI and is built with Gradle.

# General Guidelines
Always use English for code, comments, and documentation.
Use the latest Java features available in the project (records, pattern matching, etc.).
Do not introduce new libraries or dependencies unless explicitly requested.
Follow the existing project architecture and coding style.
Use only technologies and frameworks already present in the project (Java, JavaFX, Gradle).

# Workflow and Agent Behavior
Do not ask for confirmation before making changes. If a task is requested, implement it directly.
Never ask if the user wants to apply a change—just do it.
Do not repeat or explain your code in the chat unless explicitly asked. The user reviews code directly.
When a task is given, immediately provide the code or make the change as requested.
Do not be lazy—always perform the requested action fully and proactively.
If a task is ambiguous, make a reasonable assumption and proceed.
If a change requires updating multiple files, do so in one go.

# Architecture and Structure
Respect the modular structure: keep UI, logic, and data management separated as in the current codebase.
Place new features or classes in the most appropriate existing package.
Use records for immutable data structures.
Prefer modern Java idioms and concise code.

# Technologies
Java 21
JavaFX 21
Gradle
No external libraries unless already present

# Build and Run
The only command to build the project is: ./gradlew build