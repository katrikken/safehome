# !/bin/bash

# update SW on Raspberry Pi 
# /home/pi/update.sh
# start server
/home/pi/Installation/apache-tomcat-8.5.29/bin/startup.sh
       
# start safe home
java -Dpi4j.linking=dynamic -jar /home/pi/Installation/dist/com.kuryshee.safehome.rpi.jar /home/pi/Installation/dist/config.json /home/pi/Installation/dist/keys.json
    
