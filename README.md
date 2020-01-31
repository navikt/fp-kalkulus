![](https://github.com/navikt/ft-kalkulus/workflows/Bygg%20og%20release/badge.svg) 
<!--
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=navikt_ft-kalkulus&metric=alert_status)](https://sonarcloud.io/dashboard?id=navikt_ft-kalkulus) 
[![SonarCloud Coverage](https://sonarcloud.io/api/project_badges/measure?project=navikt_ft-kalkulus&metric=coverage)](https://sonarcloud.io/component_measures/metric/coverage/list?id=navikt_ft-kalkulus)
[![SonarCloud Bugs](https://sonarcloud.io/api/project_badges/measure?project=navikt_ft-kalkulus&metric=bugs)](https://sonarcloud.io/component_measures/metric/reliability_rating/list?id=navikt_ft-kalkulus)
[![SonarCloud Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=navikt_ft-kalkulus&metric=vulnerabilities)](https://sonarcloud.io/component_measures/metric/security_rating/list?id=navikt_ft-kalkulus)
-->
![GitHub release (latest by date)](https://img.shields.io/github/v/release/navikt/ft-kalkulus)
![GitHub](https://img.shields.io/github/license/navikt/ft-kalkulus)


Folketrygdloven - Kalkulus (ft-kalkulus)git 
===============

Dette er repository for kildkode som dekker applikasjonen som beregner potensiell dagsats ved hjelp av beregnignsreglene i folketrygdloven.

### Struktur
Dette er en kalkulator for foreldrepengeområdet, pleiepenger og svangerskapspenger (Folketrygdloven kapittel 14.7 og kapittel 8).

## Lokal utvikling
Team Duplo


### Spørsmål
- Slack for oppsett og utvikling på laptop: \#ft-kalkulus
- Hjelpeside med oppskrifter for utvikling på laptop på [Confluence - NAV intern](https://confluence.adeo.no/pages/viewpage.action?pageId=329047065)


### Utviklingshåndbok
[Utviklingoppsett](https://confluence.adeo.no/display/LVF/60+Utviklingsoppsett)
[Utviklerhåndbok, Kodestandard, osv](https://confluence.adeo.no/pages/viewpage.action?pageId=190254327)

## getting-started project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```
./mvnw quarkus:dev
```

## Packaging and running the application

The application is packageable using `./mvnw package`.
It produces the executable `getting-started-1.0-SNAPSHOT-runner.jar` file in `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/getting-started-1.0-SNAPSHOT-runner.jar`.

## Creating a native executable

You can create a native executable using: `./mvnw package -Pnative`.

Or you can use Docker to build the native executable using: `./mvnw package -Pnative -Dquarkus.native.container-build=true`.

You can then execute your binary: `./target/getting-started-1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/building-native-image-guide .
