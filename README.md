![Bygg og deploy](https://github.com/navikt/fp-kalkulus/workflows/Bygg%20og%20deploy/badge.svg)
<!--
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-kalkulus&metric=alert_status)](https://sonarcloud.io/dashboard?id=navikt_fp-kalkulus)
[![SonarCloud Coverage](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-kalkulus&metric=coverage)](https://sonarcloud.io/component_measures/metric/coverage/list?id=navikt_fp-kalkulus)
[![SonarCloud Bugs](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-kalkulus&metric=bugs)](https://sonarcloud.io/component_measures/metric/reliability_rating/list?id=navikt_fp-kalkulus)
[![SonarCloud Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-kalkulus&metric=vulnerabilities)](https://sonarcloud.io/component_measures/metric/security_rating/list?id=navikt_fp-kalkulus)
![GitHub](https://img.shields.io/github/license/navikt/fp-kalkulus)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/navikt/fp-kalkulus)
-->

Folketrygdloven - Kalkulus (fp-kalkulus)
===============

Dette er repository for kildkode som dekker applikasjonen som beregner potensiell dagsats ved hjelp av beregnignsreglene i folketrygdloven.

### Struktur
Dette er en kalkulator for å beregne et *beregningsgrunnlag* for følgende ytelser
 * foreldrepenger [ftrl 14-7](https://lovdata.no/nav/folketrygdloven/kap14/%C2%A714-7)
 * svangerskapspenger [ftrl 14-4](https://lovdata.no/nav/folketrygdloven/kap14/%C2%A714-4)

Reglene baserer seg i hovedsak på beregningsregler fra Sykepenger [ftrl 8](https://lovdata.no/nav/folketrygdloven/kap8/%C2%A78), med noen få særbestemmelser videre angitt under kapitler [ftrl 14](https://lovdata.no/nav/folketrygdloven/kap14)

Beregningsgrunnlaget er utgangspunktet for en maksimal dagsats som kan utbetales bruker i en angitt periode.
Beregningsgrunnlaget består av andeler per aktivitet (arbeidstype, selvstendig næringsdrivende, frilanser) og arbeidsgiver (både privatperson og virksomheter).

## Lokal utvikling
Bruk Docker Compose oppsett i https://github.com/navikt/fp-autotest til å sette opp audit, vtp og postgres.

### Spørsmål
* Slack for oppsett og utvikling på laptop: \#fp-kalkulus
* Hjelpeside med oppskrifter for utvikling på laptop på [Lokal utvikling autotest](https://github.com/navikt/fp-autotest/blob/master/docs/utvikleroppsett/README.md)


## How To
* Starte opp postgres database fra [Fp-autotest](https://github.com/navikt/fp-autotest) og eventuelt vtp/audit
* Bygge lokalt

```
mvn clean install
```

## Teknologistakk
* Java 21
* Postgres 15
* Hibernate
* Weld SE
* Jetty
* Junit
* Maven

## Folketrygdloven
Beregningen av foreldrepenger og svangerskapspenger er definert i folketrygdloven. Som hovedregel bruker disse ytelsene samme beregningsregler som sykepenger.
Kode for implementasjon av beregningsreglene fordeler seg mellom dette repoet (fp-kalkulus) og [folketrygdloven-beregningsgrunnlag-regelmodell](https://github.com/navikt/folketrygdloven-beregningsgrunnlag-regelmodell/)
En oversikt over de mest relevante reglene for beregning finnes i tabellen under, men for fullstendig oversikt bør man se [kapittel 8 av folketrygdloven](https://lovdata.no/dokument/NL/lov/1997-02-28-19/kap8#kap8)

| Lovreferanse                                                                                  |                Beskrivelse                                                            | Merknad                                                                                                                                                              |                                                                                                                                                                                                                                                                                                                                                                                                                             Implementasjon |
|-----------------------------------------------------------------------------------------------|:-------------------------------------------------------------------------------------:|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
| [§8-28 til §8-30](https://lovdata.no/dokument/NL/lov/1997-02-28-19/KAPITTEL_5-4-2#%C2%A78-15) | Beregning av arbeidstakere                                                            | Beregnes enten utifra snittlønn siste 3 måneder eller fra inntektsmelding. Avviksvurderer inntekt i beregningsperioden mot inntekt siste 12 mnd                      | [RegelBeregningsgrunnlagATFL](https://github.com/navikt/folketrygdloven-beregningsgrunnlag-regelmodell/blob/master/src/main/java/no/nav/folketrygdloven/beregningsgrunnlag/arbeidstaker/RegelBeregningsgrunnlagATFL.java)                                                                                                                                                                                                                  |
| [§8-35](https://lovdata.no/dokument/NL/lov/1997-02-28-19/KAPITTEL_5-4-3#%C2%A78-34)           | Beregning av selvstendig næringsdrivende                                              | Beregnes som hovedregel utifra skattegrunnlag siste 3 ferdiglignede år                                                                                               | [RegelBeregningsgrunnlagSN](https://github.com/navikt/folketrygdloven-beregningsgrunnlag-regelmodell/blob/master/src/main/java/no/nav/folketrygdloven/beregningsgrunnlag/selvstendig/RegelBeregningsgrunnlagSN.java)                                                                                                                                                                                                                       |
| [§8-28 til §8-30](https://lovdata.no/dokument/NL/lov/1997-02-28-19/KAPITTEL_5-4-2#%C2%A78-15) | Beregning av frilansere                                                               | Beregnes hovedsakelig likt som arbeidstakere uten inntektsmelding                                                                                                    | [RegelBeregningsgrunnlagATFL](https://github.com/navikt/folketrygdloven-beregningsgrunnlag-regelmodell/blob/master/src/main/java/no/nav/folketrygdloven/beregningsgrunnlag/arbeidstaker/RegelBeregningsgrunnlagATFL.java)                                                                                                                                                                                                                  |
| [§8-40](https://lovdata.no/lov/1997-02-28-19/§8-40)                                           | Beregning av kombinasjonen arbeidstaker og frilans                                    | Beregnes sammen som arbeidstakere, avviksvurdering gjøres samlet                                                                                                     | [RegelBeregningsgrunnlagATFL](https://github.com/navikt/folketrygdloven-beregningsgrunnlag-regelmodell/blob/master/src/main/java/no/nav/folketrygdloven/beregningsgrunnlag/arbeidstaker/RegelBeregningsgrunnlagATFL.java)                                                                                                                                                                                                                  |
| [§8-41](https://lovdata.no/lov/1997-02-28-19/§8-41)                                           | Beregning av kombinasjonen arbeidstaker og selvstendig næringsdrivende                | Beregnes hver for seg, men avviksvurdering følger næringsreglene                                                                                                     | [RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSN](https://github.com/navikt/folketrygdloven-beregningsgrunnlag-regelmodell/blob/master/src/main/java/no/nav/folketrygdloven/beregningsgrunnlag/kombinasjon/RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSN.java)                                                                                                                                                                 |
| [§8-41 og §8-42](https://lovdata.no/lov/1997-02-28-19/§8-41)                                  | Beregning av kombinasjonen frilans og selvstendig næringsdrivende                     | Beregnes hver for seg, frilans beregnes som arbeidstaker, men avviksvurdering følger næringsreglene                                                                  | [RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSN](https://github.com/navikt/folketrygdloven-beregningsgrunnlag-regelmodell/blob/master/src/main/java/no/nav/folketrygdloven/beregningsgrunnlag/kombinasjon/RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSN.java)                                                                                                                                                                 |
| [§8-43](https://lovdata.no/lov/1997-02-28-19/§8-43)                                           | Beregning av kombinasjonen arbeidstaker, frilans og selvstendig næringsdrivende       | Beregnes hver for seg, arbeid og frilans beregnes som arbeidstaker, men avviksvurdering følger næringsreglene                                                        | [RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSN](https://github.com/navikt/folketrygdloven-beregningsgrunnlag-regelmodell/blob/master/src/main/java/no/nav/folketrygdloven/beregningsgrunnlag/kombinasjon/RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSN.java)                                                                                                                                                                 |
| [§8-47](https://lovdata.no/lov/1997-02-28-19/§8-47)                                           | Midlertidig utenfor arbeid                                                            | Deles i to bokstaver, A og B. A beregnes etter skattegrunnlag siste 3 år. B beregnes etter normales beregningsregler, men refusjon til arbeidsgivere innvilges ikke. | [RegelBeregningsgrunnlagInaktiv](https://github.com/navikt/folketrygdloven-beregningsgrunnlag-regelmodell/blob/master/src/main/java/no/nav/folketrygdloven/beregningsgrunnlag/inaktiv/RegelBeregningsgrunnlagInaktiv.java)                                                                                                                                                                                                                 |
| [§14-4 5.ledd](https://lovdata.no/lov/1997-02-28-19/§14-4)                                    | Generell beregning av svangerskapspenger                                              | Beskriver beregning av svangerskapspenger og krav for oppfylling av beregningsgrunnlagsvilkåret                                                                      | [ForeslåBeregningsgrunnlag(beregne)](https://github.com/navikt/fp-kalkulus/blob/master/kalkulator/src/main/java/no/nav/folketrygdloven/kalkulator/steg/foresl%C3%A5/Foresl%C3%A5Beregningsgrunnlag.java) [VurderBeregningsgrunnlagTjeneste(vurdere vilkår)](https://github.com/navikt/fp-kalkulus/blob/master/kalkulator/src/main/java/no/nav/folketrygdloven/kalkulator/steg/fordeling/vilk%C3%A5r/VurderBeregningsgrunnlagTjeneste.java) |
| [§14-7 1.ledd](https://lovdata.no/lov/1997-02-28-19/§14-7)                                    | Generell beregning av foreldrepenger                                                  | Beskriver beregning av foreldrepenger og krav for oppfylling av beregningsgrunnlagsvilkåret                                                                          | [ForeslåBeregningsgrunnlag(beregne)](https://github.com/navikt/fp-kalkulus/blob/master/kalkulator/src/main/java/no/nav/folketrygdloven/kalkulator/steg/foresl%C3%A5/Foresl%C3%A5Beregningsgrunnlag.java) [VurderBeregningsgrunnlagTjeneste(vurdere vilkår)](https://github.com/navikt/fp-kalkulus/blob/master/kalkulator/src/main/java/no/nav/folketrygdloven/kalkulator/steg/fordeling/vilk%C3%A5r/VurderBeregningsgrunnlagTjeneste.java) |
| [§14-7 3.ledd](https://lovdata.no/lov/1997-02-28-19/§14-7)                                    | Besteberegning av foreldrepenger                                                      | Beskriver besteberegning av foreldrepenger for fødende kvinner på dagpenger                                                                                          | [ForeslåBesteberegning](https://github.com/navikt/fp-kalkulus/blob/master/kalkulator/src/main/java/no/nav/folketrygdloven/kalkulator/steg/besteberegning/Foresl%C3%A5Besteberegning.java) |
