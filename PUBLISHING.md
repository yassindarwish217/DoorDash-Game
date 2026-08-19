# Publishing Guide

This project is a JavaFX desktop game. Publish it first as a GitHub portfolio project and, optionally, attach a zip file as a downloadable release.

## Recommended First Publish

1. Create a new GitHub repository.
2. Use a name such as `doordash-java-game` or rename the project first if you want to avoid confusion with DoorDash, Inc.
3. Upload the cleaned project files from this folder.
4. Make sure `README.md` appears on the repository home page.
5. Do not upload generated files such as `bin/`, `.class` files, `.DS_Store`, or private course test files.

## Suggested Repository Description

```text
A JavaFX turn-based board game with custom monsters, cards, powerups, CSV-driven data, and object-oriented game logic.
```

## Suggested Topics

```text
java, javafx, oop, board-game, desktop-game, csv, student-project
```

## Suggested First Commit Message

```text
Publish DoorDasH JavaFX game
```

## Optional Git Commands

Run these from inside the project folder after creating an empty GitHub repository:

```bash
git init
git add .
git commit -m "Publish DoorDasH JavaFX game"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPOSITORY.git
git push -u origin main
```

## Important Note About Web Publishing

This game cannot become playable in a browser just by uploading it, because JavaFX is a desktop GUI framework. To make it browser-playable, the interface would need to be rebuilt using web technologies such as HTML, CSS, and JavaScript while reusing the game rules as a reference.
