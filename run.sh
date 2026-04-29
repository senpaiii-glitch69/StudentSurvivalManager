#!/bin/bash

java \
--module-path ~/javafx/javafx-sdk-17.0.19/lib \
--add-modules javafx.controls,javafx.fxml \
-cp target/classes:$(find ~/.m2/repository -name "gson-*.jar" | head -1) \
com.student.StudyHubPro

