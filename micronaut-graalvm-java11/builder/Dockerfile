# ----------------------------------------------------------------------------------------------------------------------
# Build the lambda funciton/application and produces a JAR file
# ----------------------------------------------------------------------------------------------------------------------
FROM gradle:6.8.3-jdk11 as builder
WORKDIR /home/application
COPY --chown=gradle:gradle ../gradle gradle
COPY --chown=gradle:gradle ../gradlew gradlew
COPY --chown=gradle:gradle ../settings.gradle settings.gradle
COPY --chown=gradle:gradle ../build.gradle build.gradle
COPY --chown=gradle:gradle ../bootstrap bootstrap
COPY --chown=gradle:gradle ../src src
RUN ./gradlew shadowJar --no-daemon
# ----------------------------------------------------------------------------------------------------------------------


# ----------------------------------------------------------------------------------------------------------------------
# Downloads the GraalVM binaries and installed the required libraries
# ----------------------------------------------------------------------------------------------------------------------
FROM amazonlinux:2018.03.0.20191014.0 as graalvm
ENV LANG=en_US.UTF-8
RUN yum install -y gcc gcc-c++ libc6-dev zlib1g-dev curl bash zlib zlib-devel zip
ENV GRAAL_VERSION 21.0.0.2
ENV JDK_VERSION java11
ENV GRAAL_FILENAME graalvm-ce-${JDK_VERSION}-linux-amd64-${GRAAL_VERSION}.tar.gz

RUN curl -4 -L https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-${GRAAL_VERSION}/${GRAAL_FILENAME} -o /tmp/${GRAAL_FILENAME}

RUN tar -zxvf /tmp/${GRAAL_FILENAME} -C /tmp \
    && mv /tmp/graalvm-ce-${JDK_VERSION}-${GRAAL_VERSION} /usr/lib/graalvm

RUN rm -rf /tmp/*
CMD ["/usr/lib/graalvm/bin/native-image"]
# ----------------------------------------------------------------------------------------------------------------------


# ----------------------------------------------------------------------------------------------------------------------
# Created the native image
# ----------------------------------------------------------------------------------------------------------------------
FROM graalvm
COPY --from=builder /home/application/ /home/application/
WORKDIR /home/application
RUN /usr/lib/graalvm/bin/gu install native-image
RUN /usr/lib/graalvm/bin/native-image --no-server -cp build/libs/unique-code-serverless-all.jar
RUN chmod 777 bootstrap
RUN chmod 777 unique-code-serverless
RUN zip -j function.zip bootstrap unique-code-serverless
EXPOSE 8080
ENTRYPOINT ["/home/application/unique-code-serverless"]
# ----------------------------------------------------------------------------------------------------------------------
