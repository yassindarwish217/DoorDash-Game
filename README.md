# DoorDasH

A JavaFX board game where two sides race across a 100-cell board while managing energy, roles, powerups, special cards, and cell effects.

> Note: This is an educational Java project and is not affiliated with DoorDash, Inc.

## Overview

DoorDasH: Scare vs Laugh Touchdown is a turn-based desktop game inspired by board-game movement and monster abilities. The player chooses a side, then competes against an opponent to reach cell 99 with at least 1000 energy.

## Features

- JavaFX graphical interface with start, game, and game-over screens
- 10x10 board with special cells and visual player/opponent markers
- Player role selection between `SCARER` and `LAUGHER`
- Dice-based movement and turn progression
- Monster types with different abilities:
  - Dasher
  - Dynamo
  - MultiTasker
  - Schemer
- Powerup system with energy costs
- Card system with effects such as position swap, shield, energy steal, confusion, and start-over cards
- CSV-driven game data for cards, cells, and monsters
- Custom exceptions for invalid moves, invalid turns, energy errors, and CSV loading issues

## Technologies

- Java
- JavaFX
- Object-oriented programming
- CSV file loading
- Eclipse project structure

## Project Structure

```text
DoorDash
├── src/game/engine
│   ├── cards
│   ├── cells
│   ├── dataloader
│   ├── exceptions
│   ├── interfaces
│   └── monsters
├── src/game/gui
│   ├── controller
│   └── view
├── cards.csv
├── cells.csv
├── monsters.csv
└── run.sh
```

## Requirements

- JDK 17 or newer
- JavaFX SDK / OpenJFX

On macOS with Homebrew:

```bash
brew install openjdk openjfx
```

## How to Run

Set `JAVAFX_HOME` to your JavaFX installation, then run the script:

```bash
export JAVAFX_HOME="$(brew --prefix openjfx)/libexec"
chmod +x run.sh
./run.sh
```

If you installed JavaFX manually, set `JAVAFX_HOME` to the folder that contains the JavaFX `lib` directory:

```bash
export JAVAFX_HOME="/path/to/javafx-sdk"
./run.sh
```

## Game Objective

Reach cell 99 with at least 1000 energy.

## Gameplay Summary

Each turn, the current monster may use a powerup if it has enough energy, then rolls a six-sided die to move. Landing on different cells can reward energy, reduce energy, move the monster, draw a card, or trigger monster interactions.

## Portfolio Notes

This project demonstrates:

- Encapsulation through engine classes and monster state
- Inheritance through monster, card, and cell hierarchies
- Polymorphism through shared behavior across cards, cells, and monsters
- Exception handling for invalid game actions
- Separation between engine logic, controller logic, and JavaFX views
- Data-driven setup using CSV files
