#!/bin/bash
sudo apt-get update -y
sudo apt-get dist-upgrade -y 

sudo apt-get install fswebcam

wget http://apache.miloslavbrada.cz/tomcat/tomcat-8/v8.5.29/bin/apache-tomcat-8.5.29.tar.gz
tar xvf apache-tomcat-8.5.29.tar.gz

sudo cp RpiServer.war /home/pi/Installation/apache-tomcat-8.5.29/webapps/RpiServer.war

sudo chmod +x safehomestart.sh
sudo chmod +x safehomestop.sh
sudo chmod +x safehome
sudo cp safehome /etc/init.d/safehome


