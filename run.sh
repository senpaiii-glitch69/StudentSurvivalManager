#!/bin/bash

java -Dprism.order=sw \
--module-path ~/javafx/javafx-sdk-17.0.19/lib \
--add-modules javafx.controls \
-cp target/classes \
com.student.StudentManagerFinal

