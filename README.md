# Student Survival Manager

A JavaFX desktop app for tracking expenses and assignments.

## Run locally

If you have Java 17 and Maven installed, run:

```bash
mvn clean javafx:run
```

The provided `run.sh` script expects a local JavaFX SDK installation at `~/javafx/javafx-sdk-17.0.19/lib`.

## GitHub website (Pages)

This repository now includes a GitHub Pages website in `docs/`.

- Website source: `docs/index.html`
- Pages workflow: `.github/workflows/pages.yml`
- Build workflow for desktop JAR: `.github/workflows/build.yml`

Important: the JavaFX app itself is still a desktop app, so it does not run inside the browser. The website is a landing/download page for the project.

## Create the GitHub repo

If you have not created the repository yet, initialize git locally, add your remote, and push the current branch to GitHub.