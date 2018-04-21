#! /bin/bash

# stop server
/home/pi/Installation/apache-tomcat-8.5.29/bin/shutdown.sh
	    
# stop safe home
pkill -f 'java -jar'