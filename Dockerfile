FROM ghcr.io/navikt/baseimages/temurin:17-appdynamics
LABEL org.opencontainers.image.source=https://github.com/navikt/ft-kalkulus
ENV APPD_ENABLED=true
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 \
    -Djava.security.egd=file:/dev/urandom"

RUN mkdir webapp

# Export vault properties
COPY --chown=apprunner:root .scripts/03-import-appdynamics.sh /init-scripts/03-import-appdynamics.sh
COPY --chown=apprunner:root .scripts/05-import-users.sh /init-scripts/05-import-users.sh
COPY --chown=apprunner:root .scripts/08-remote-debug.sh /init-scripts/08-remote-debug.sh

# Application Container (Jetty)
COPY web/target/lib/*.jar ./
COPY web/target/app.jar .
