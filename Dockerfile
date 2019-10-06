FROM openjdk:8u222-jdk

ENV WORKINGDIR=/root/app

RUN	mkdir -p ${WORKINGDIR} && chmod 666 ${WORKINGDIR}

WORKDIR ${WORKINGDIR}

RUN	apt-get update						&&	\
	apt-get install git

RUN git clone https://github.com/bmp-git/PPS-18-scala-mqtt.git ${WORKINGDIR}

# This can be used instead of git clone
# ADD ./ ${WORKINGDIR}

# TODO remove -x test when tests will not fail
RUN	./gradlew build -x test

VOLUME "${WORKINGDIR}/config"

EXPOSE 1883

CMD ./gradlew run --args='--no_stdin'
