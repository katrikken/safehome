# Safe Home Raspberry Pi module

Safe Home is an application for the bachelor thesis "Home alarm system". This part of the project is a Raspberry Pi security module, which is responsible for motion detection, taking pictures and registering RFID cards.

## Getting Started

Download installation package for Safe Home from https://github.com/katrikken/safehome.git
Follow the instructions in this README.

### Prerequisites

To run Safe Home security module on Raspberry Pi, you will need:

1) Raspberry Pi computer model B or newer compatible with model B with the installed Raspbian operating system. Look at the "User guide.doc" for detailed instructions on how to set up your Raspberry Pi or at https://www.raspberrypi.org/
2) A MIFARE RFID-RC522 card reader.
3) A PIR motion sensor.
4) A USB camera.
5) A LED.
6) A 330-ohm resistor.
7) Breadboard and Male-Female jumper wires.
8) An Ethernet cable.
9) A micro-USB cable.

### Installing

Make sure your Raspberry Pi is properly set up by checking up with the "User guide.doc".

Connect the hardware elements to the Raspberry Pi following the scheme illustrated in "Raspberry Pi circuit scheme.png" file.

Connect Raspberry Pi to the Internet via Ethernet cable. Connect it to the power supply via micro-USB cable.

Connect to the Raspberry Pi via WinSCP or similar program, allowing you to copy files to your Raspberry Pi. Copy folder "Installation" to the "/home/pi/" directory.

The installation process can take up to several hours to finish. The longest part of the installation is updating Raspbian software. You can avoid this longevity by running following commands before the security module installation:

```
sudo apt-get update -y
sudo apt-get dist-upgrade -y 
```

On Raspberry Pi terminal (via "putty", for example) run commands: 

```
cd Installation
chmod +x install.sh
./install.sh 
```

When the script has finished, shut down your Raspberry Pi and turn it on again. Within a couple of minutes, the LED will blink three times. This will mean that installation finished correctly and the security module is ready to work.

## Authors

* **Ekaterina Kurysheva** (https://github.com/katrikken)
