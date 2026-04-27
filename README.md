# Student Survival Manager

A JavaFX desktop app for tracking expenses and assignments.

## Run locally

If you have Java 17 and Maven installed, run:

```bash
mvn clean javafx:run
```

The provided `run.sh` script expects a local JavaFX SDK installation at `~/javafx/javafx-sdk-17.0.19/lib`.

## GitHub deployment

This is a desktop JavaFX app, so it cannot be deployed to GitHub Pages like a web app. The practical GitHub deployment path is:

1. Push the source code to a GitHub repository.
2. Let GitHub Actions build the project on every push.
3. Download the packaged JAR from the workflow artifact, or attach it to a GitHub Release.

The workflow in [`.github/workflows/build.yml`](.github/workflows/build.yml) already does the build and uploads the JAR artifact.

## Create the GitHub repo

If you have not created the repository yet, initialize git locally, add your remote, and push the current branch to GitHub.