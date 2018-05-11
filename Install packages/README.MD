# Safe Home installation

Safe Home is an application for the bachelor thesis "Home alarm system". This folder contains everything for the installation of the whole system.

## Getting Started

Download installation package for Safe Home from https://github.com/katrikken/safehome.git
Follow the instructions in this README.

### Prerequisites

To run Safe Home Server module, you need a computer with:
1) Java 1.8 or higher
2) Tomcat 9 server container
3) Oracle database

To run the Android application, you need an Android device with API from 19 up to 27.
For the full list of prerequisites for Raspberry Pi security module, check the README file in the "Installation" folder.

### Installing

#### Server installation

In your Oracle database create a "safehome" user running the following script:

```
create user safehome identified by safehome;
grant CREATE SESSION, ALTER SESSION, CREATE DATABASE LINK, CREATE MATERIALIZED VIEW, CREATE PROCEDURE, CREATE PUBLIC SYNONYM, CREATE ROLE, CREATE SEQUENCE, CREATE SYNONYM, CREATE TABLE, CREATE TRIGGER, CREATE TYPE, CREATE VIEW, UNLIMITED TABLESPACE to safehome;
```

Login as "safehome" user and run the scripts from the "Database" folder.

```
create_schema.sql
insert_data.sql
```

Deploy the "SafeHome.war" on your Tomcat server.

#### Android application installation

Install the "app-debug.apk" on your Android device.

## Configure the parts of the project

After you have installed the server, the application and the Raspberry Pi security module, configure the server address on your Raspberry Pi and the Android application.
Edit the file at "/home/pi/Installation/dist/config.json" on your Raspberry Pi by changing the host name and port of the "server" variable. 
During your first logging in to the Android application, you will be asked to enter the server address. Enter the host name and port as "http://hostname:port" into the field.

Safe Home is ready for usage. Check the User Manual for instructions on how to start using the application.

## Authors

* **Ekaterina Kurysheva** (https://github.com/katrikken)
