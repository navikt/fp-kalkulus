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
Dette er en kalkulator for å beregne et *beregningsgrunnlag* for følgende ytelser
 * foreldrepenger [ftrl 14-7](https://lovdata.no/nav/folketrygdloven/kap14/%C2%A714-7)
 * svangerskapspenger [ftrl 14-4](https://lovdata.no/nav/folketrygdloven/kap14/%C2%A714-4)
 * pleiepenger for sykt barn [ftrl 9-10](https://lovdata.no/nav/folketrygdloven/kap9/%C2%A79-10)
 * pleiepenger for pleie av nærstående [ftrl 9-13](https://lovdata.no/nav/folketrygdloven/kap9/%C2%A79-13)
 * omsorgspenger [ftrl 9-5](https://lovdata.no/nav/folketrygdloven/kap9/%C2%A79-5)
 * opplæringspenger [ftrl 9-14](https://lovdata.no/nav/folketrygdloven/kap9/%C2%A79-14)
  
Reglene baserer seg i hovedsak på beregningsregler fra Sykepenger [ftrl 8](https://lovdata.no/nav/folketrygdloven/kap8/%C2%A78), med noen få særbestemmelser videre angitt under kapitler [ftrl 14](https://lovdata.no/nav/folketrygdloven/kap14) og [ftrl 9](https://lovdata.no/nav/folketrygdloven/kap9)

Beregningsgrunnlaget er utgangspunktet for en maksimal dagsats som kan utbetales bruker i en angitt periode.
Beregningsgrunnlaget består av andeler per aktivitet (arbeidstype, selvstendig næringsdrivende, frilanser) og arbeidsgiver (både privatperson og virksomheter).

## Lokal utvikling
* Team Foreldrepenger
* Team K9Saksbehandling


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
* Java 16
* Postgres 12
* Hibernate
* Weld SE
* Jetty
* Junit 5
* Maven 3.6.3
