# Safe Home compilation

Safe Home is an application for the bachelor thesis "Home alarm system". This folder contains source code and libraries for the application. In this README we describe how to compile the project.

## Getting Started

Download the package for Safe Home from https://github.com/katrikken/safehome.git
Follow the instructions in this README.

### Prerequisites

To compile Safe Home, you need to have installed:
1) Java 1.8 or higher
2) Eclipse OXYGEN IDE with J2EE
3) Maven
2) Tomcat 8 or higher
3) Oracle database
4) Android Studio 3.0.1 or higher

### Compilation

1) Import the projects RpiServer and SafeHome to Eclipse IDE as Java dynamic web applications.
2) Import the com.kuryshee.safehome.rpi and RequestProcessor as existing projects to Eclipse IDE.
3) Import the SafeHomeClient to Android Studio.

Build classpaths in Eclipse IDE:

1) For RequestProcessor you will need "javax.json-1.0.jar", "org.apache.commons.io.jar" and Tomcat binaries, which can be found at "safehome/Source code/com.kuryshee.safehome.rpi/dist/lib/" and "safehome/Source code/SafeHome/WebContent/WEB-INF/lib/"
2) For com.kuryshee.safehome.rpi you will need the dependency on RequestProcessor project and pi4j library, which is situated in "https://github.com/katrikken/safehome/tree/master/Source%20code/com.kuryshee.safehome.rpi/dist/lib"
3) For RpiServer compilation use Maven.
4) For SafeHome you will need the dependency on RequestProcessor project and on Tomcat library, which can be found at "https://github.com/katrikken/safehome/tree/master/Source%20code/SafeHome/WebContent/WEB-INF/lib"

Now Eclipse IDE will be able to build from the source code.

In Android Studio:
1) Configure dependencies by adding the .jar files from the "https://github.com/katrikken/safehome/tree/master/Source%20code/SafeHomeClient/app/libs" folder. 
2) Build the project with Gradle builds.

Now you have compiled the Android application for the project.

## Authors

* **Ekaterina Kurysheva** (https://github.com/katrikken)
