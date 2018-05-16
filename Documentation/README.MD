# Safe Home documentation

Safe Home is an application for the bachelor thesis "Home alarm system". This folder contains generated documentation from the source code with extending commentaries.

## Contents

The Android Javadoc folder contains generated documentation for the Android application.
The Javadoc folder contains the rest of the Java code documentation for the project, including documentation for the server implementation, Raspberry Pi security application, Raspberry Pi web application and supporting libraries.

### Package introduction

In the generated documentation you will find the description of the following packages.

**com.kuryshee.safehome.rpi** package contains the classes for the Raspberry Pi security module, which controls the hardware on the computer.
**RpiServer** package contains the classes of the web application for user management on Raspberry Pi.
**SafeHome** package contains the classes of the server, which stores and shares the data between the Raspberry Pi security module and the mobile application.
**SafeHomeClient** package contains the classes of the Android application for browsing the data from the Raspberry Pi security module and delivering the notifications about the intrusions. 
**RequestProcessor** package contains supporting library for HTTP requests processing, which other packages actively use.

## Authors

* **Ekaterina Kurysheva** (https://github.com/katrikken)
