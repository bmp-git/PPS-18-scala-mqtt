FROM openjdk:8u222-jdk

ENV WORKINGDIR=/root/app

RUN	mkdir -p ${WORKINGDIR} && chmod 666 ${WORKINGDIR}

WORKDIR ${WORKINGDIR}

ADD ./ ${WORKINGDIR}

RUN	./gradlew build

VOLUME "${WORKINGDIR}/config"

EXPOSE 1883

CMD ./gradlew run --args='--no_stdin'
