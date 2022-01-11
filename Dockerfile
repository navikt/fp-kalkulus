FROM navikt/java:16-appdynamics
LABEL org.opencontainers.image.source=https://github.com/navikt/ft-kalkulus
ENV APPD_ENABLED=true

RUN mkdir lib
RUN mkdir webapp
RUN mkdir conf

# Config
COPY web/target/classes/jetty/jaspi-conf.xml conf/

# Application Container (Jetty)
COPY web/target/app.jar .
COPY web/target/lib/*.jar ./

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 \
    -Djava.security.egd=file:/dev/urandom"

# Export vault properties
COPY .scripts/03-import-appdynamics.sh /init-scripts/03-import-appdynamics.sh
COPY .scripts/05-import-users.sh /init-scripts/05-import-users.sh
