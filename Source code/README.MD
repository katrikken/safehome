# Safe Home compillation

Safe Home is an application for the bachelor thesis "Home alarm system". This folder contains source code and libraries for the application. In this README we describe how to compile the project.

## Getting Started

Download the package for Safe Home from https://github.com/katrikken/safehome.git
Follow the instructions in this README.

### Prerequisites

To compile Safe Home, you need to have installed:
1) Java 1.8 or higher
2) Eclipde OXYGEN IDE with J2EE
3) Maven
2) Tomcat 8 or higher
3) Oracle database

### Compillation

1) Import the projects RpiServer and SafeHome to Eclipse IDE as Java dynamic web applications.
2) Import the com.kuryshee.safehome.rpi and RequestProcessor as existing projects.

Build classpaths:

1) For RequestProcessor you will need "javax.json-1.0.jar", "org.apache.commons.io.jar" and Tomcat binaries, which can be found at "safehome/Source code/com.kuryshee.safehome.rpi/dist/lib/" and "safehome/Source code/SafeHome/WebContent/WEB-INF/lib/"
2) For com.kuryshee.safehome.rpi you will need dependency on RequestProcessor project and pi4j library, which is situated in "https://github.com/katrikken/safehome/tree/master/Source%20code/com.kuryshee.safehome.rpi/dist/lib"
3) For RpiServer compillation use Maven.
4) For SafeHome you will need dependency on RequestProcessor project and on Tomcat library, which can be found at "https://github.com/katrikken/safehome/tree/master/Source%20code/SafeHome/WebContent/WEB-INF/lib"

Now Eclipse IDE will be able to build from the source code.

## Authors

* **Ekaterina Kurysheva** (https://github.com/katrikken)
