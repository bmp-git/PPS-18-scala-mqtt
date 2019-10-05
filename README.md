## PPS-18-scala-mqtt (PPS project a.a. 18-19)
[![Build Status](https://travis-ci.org/bmp-git/PPS-18-scala-mqtt.svg?branch=develop)](https://travis-ci.org/bmp-git/PPS-18-scala-mqtt)
[![codecov](https://codecov.io/gh/bmp-git/PPS-18-scala-mqtt/branch/develop/graph/badge.svg)](https://codecov.io/gh/bmp-git/PPS-18-scala-mqtt)

**scala-mqtt** is a MQTT message broker written in Scala.

**scala-mqtt** implements the [OASIS MQTT Version 3.1.1](http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html) standard

###Features

* [X] Publish with QoS 0
* [X] Wildcard Topic Subscribe Support
* [X] Persistence and Session Management
* [X] Retain Message
* [X] Will Message
* [X] User Authentication
* [X] Configuration file
* [ ] Publish with QoS 1 & QoS2
* [ ] Access control list (acl) file

### Prerequisites
The prerequisite are:
 * [Java jdk-8u221](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
 
### Installation
1. Clone the repository. ```git clone https://github.com/bmp-git/PPS-18-scala-mqtt.git```

1. Move into project directory.  ```cd PPS-18-scala-mqtt/```

1. Launch the broker.  ```.\gradlew run```
                       
By default the application starts the MQTT broker on port 1883 but these settings can be changed by specifying different launch options inside the file "settings.conf".

### Launch options
The launch options can be defined in the file "settings.conf" according to the [Mosquitto.conf](https://mosquitto.org/man/mosquitto-conf-5.html) syntax

Currently, only a small options subset is supported:

```bind_address [address]```: bind the broker on the specified ip address/hostname.

```port [number]```: start the broker on the specified port.

```allow_anonymous [true | false]```: enable/disable anonymous access.

### Users file
The file containing username and passwords is "users.conf".

You can add user's authentication information in the following way:

```username [sha256(password)]```: username with optionally a password hashed using sha256.

### Docker
An docker image of the application can be built through the Dockerfile or can be pulled from [this](linkhere) docker hub repo.

The container exposes port 1883 to access the broker.

Currently the docker container support the default configuration:
```bind_address "localhost"```
```port 1883```
```allow_anonymous true```

### Authors
Barbieri Edoardo, Lorenzo Mondani, Emanuele Pancisi
 
Developed as final project for [81612 - Programming and Development paradigms](https://www.unibo.it/en/teaching/course-unit-catalogue/course-unit/2018/412597) course (academic year 2018/2019).
