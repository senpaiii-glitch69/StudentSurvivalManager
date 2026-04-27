# Student Survival Manager

A JavaFX desktop app for tracking expenses and assignments.

## Run locally

If you have Java 17 and Maven installed, run:

```bash
mvn clean javafx:run
```

The provided `run.sh` script expects a local JavaFX SDK installation at `~/javafx/javafx-sdk-17.0.19/lib`.

## GitHub website (Pages)

This repository now includes a fully functional website in `docs/` with:

- Expense tracking
- Assignment tracking with due-date status
- Event tracking with date/location status
- Browser persistence using localStorage

Deployment is automatic via `.github/workflows/pages.yml`.

Desktop JavaFX app support is still available through the Maven build and `.github/workflows/build.yml`.

## Create the GitHub repo

If you have not created the repository yet, initialize git locally, add your remote, and push the current branch to GitHub.