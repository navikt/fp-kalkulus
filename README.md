![Bygg og deploy](https://github.com/navikt/ft-kalkulus/workflows/Bygg%20og%20deploy/badge.svg)
<!--
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=navikt_ft-kalkulus&metric=alert_status)](https://sonarcloud.io/dashboard?id=navikt_ft-kalkulus) 
[![SonarCloud Coverage](https://sonarcloud.io/api/project_badges/measure?project=navikt_ft-kalkulus&metric=coverage)](https://sonarcloud.io/component_measures/metric/coverage/list?id=navikt_ft-kalkulus)
[![SonarCloud Bugs](https://sonarcloud.io/api/project_badges/measure?project=navikt_ft-kalkulus&metric=bugs)](https://sonarcloud.io/component_measures/metric/reliability_rating/list?id=navikt_ft-kalkulus)
[![SonarCloud Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=navikt_ft-kalkulus&metric=vulnerabilities)](https://sonarcloud.io/component_measures/metric/security_rating/list?id=navikt_ft-kalkulus)
![GitHub](https://img.shields.io/github/license/navikt/ft-kalkulus)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/navikt/ft-kalkulus)
-->

Folketrygdloven - Kalkulus (ft-kalkulus)
===============

Dette er repository for kildkode som dekker applikasjonen som beregner potensiell dagsats ved hjelp av beregnignsreglene i folketrygdloven.

### Struktur
Dette er en kalkulator for foreldrepengeområdet, pleiepenger og svangerskapspenger (Folketrygdloven kapittel 14.7 og kapittel 8).

## Lokal utvikling
Team Duplo


### Spørsmål
* Slack for oppsett og utvikling på laptop: \#ft-kalkulus
* Hjelpeside med oppskrifter for utvikling på laptop på [Confluence - NAV intern](https://confluence.adeo.no/pages/viewpage.action?pageId=329047065)


### Utviklingshåndbok
[Utviklingoppsett](https://confluence.adeo.no/display/LVF/60+Utviklingsoppsett)
[Utviklerhåndbok, Kodestandard, osv](https://confluence.adeo.no/pages/viewpage.action?pageId=190254327)

## How To
* Starte opp lokal database
```
docker-compose up -d
```
* Stanse lokal database
```
docker-compose down
```
* Bygge lokalt

```
./mvnw clean install
```

## Teknologistakk
* Java 11
* Postgres 11.5
* Hibernate
* Weld SE
* Jetty
* Junit 5
* Maven 3.6.3