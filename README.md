# Maze Game

Maze Game is a configurable 2D maze and puzzle game engine developed in Java. The project demonstrates software architecture and extensible design concepts including plugins, scripting, domain-specific languages (DSLs), Gradle multi-module builds, internationalisation, and modular game systems.

## Features

- 2D grid-based maze gameplay
- Configurable game worlds using a custom DSL
- Hidden and visible grid exploration system
- Item collection and inventory management
- Obstacles requiring specific inventory items
- Goal-based puzzle progression
- Dynamic plugin loading using reflection
- Python scripting support via Jython
- Internationalisation and locale switching
- UTF-8 / UTF-16 / UTF-32 input file support
- Modular and extensible architecture

## Gameplay Overview

The player explores a hidden maze by moving:

- Up
- Down
- Left
- Right

As the player moves:

- Adjacent grid squares become visible
- Items can be collected into the inventory
- Obstacles may block movement unless required items are owned
- The game ends once the player reaches the goal location

## Technologies Used

- Java
- Gradle
- JavaCC / ANTLR
- Jython
- Java Reflection
- Resource Bundles
- Unicode Normalisation
- Object-Oriented Design

## Input File DSL

The game world is configured through a custom domain-specific language.

Example declarations include:

```text
size (10,10)
start (1,5)
goal (9,8)

item "Wooden Sword" {
    at (0,0), (2,3)
    message "Use wisely."
}

obstacle {
    at (3,3)
    requires "Wooden Sword"
}

plugin edu.curtin.gameplugins.Teleport
```

The parser supports:

- Grid size declarations
- Player start and goal positions
- Items and obstacles
- Plugin declarations
- Embedded Python scripts

## Plugin & Script System

The engine supports runtime extensibility through plugins and scripts.

### Plugin Features

- Dynamically loaded using reflection
- Fully-qualified class loading
- Event callback support
- Access to the game API

### Script Features

- Embedded Python scripts using Jython
- Custom game logic integration
- Callback event handling

## Implemented Plugins / Scripts

The project includes implementations such as:

- Teleport Plugin
- Penalty Obstacle Plugin
- Reveal Map Plugin
- Prize Reward Plugin

These demonstrate the extensibility of the game engine architecture.

## Internationalisation

The engine supports:

- Dynamic locale switching using IETF language tags
- Multiple language translations
- Localised in-game dates
- Unicode compatibility normalisation
- UTF-8 / UTF-16 / UTF-32 encoded map files

## Architecture Highlights

The project emphasises:

- Modular design
- Separation of concerns
- Extensible APIs
- Plugin-based architecture
- Script integration
- Parser-driven configuration

## Learning Outcomes

This project demonstrates understanding of:

- Software architecture principles
- Extensible system design
- Plugin and scripting systems
- DSL parsing and grammar design
- Internationalisation
- Reflection and dynamic loading
- Gradle project organisation

## Running the Project

1. `./gradlew clean :app:javaccMyparser`
2. `./gradlew build`
3. `./gradlew check` (optional)
4. `./gradlew run --args="input.utf8.map"` (or `utf16`/`utf32`, but ensure they are actually encoded as such. The included input files should be.)

## Assignment Information

**Unit:** COMP3003 Software Architecture and Extensible Design

**Assignment:** Assignment 2

**Semester:** 2025, Semester 2

<p align="center">
  <img width="1192" height="823" alt="Screenshot 2026-05-10 160411" src="https://github.com/user-attachments/assets/d021b2e3-0699-4374-8949-d25a00f32ea6" />
</p>



