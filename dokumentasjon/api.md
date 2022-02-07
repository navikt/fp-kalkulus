# API

### Request
POST /start

Starter en beregning med gitt inputdata
```json
{
  "aktivitetGradering": null,
  "refusjonskravDatoer": null,
  "grunnbeløpsatser": [
    {
      "periode": {
        "fom": "2019-05-01",
        "tom": "2099-12-31"
      },
      "gSnitt": 98866,
      "gVerdi": 99858
    }
  ],
  "iayGrunnlag": {
    "arbeidDto": {
      "yrkesaktiviteter": [
        {
          "arbeidsgiver": {
            "identType": "ORGNUMMER",
            "ident": "910909088"
          },
          "abakusReferanse": {
            "abakusReferanse": "3f15eb0a-7d0e-48e0-86b1-299663a7c324"
          },
          "arbeidType": {
            "kodeverk": "ARBEID_TYPE",
            "kode": "ORDINÆRT_ARBEIDSFORHOLD"
          },
          "aktivitetsAvtaler": [
            {
              "periode": {
                "fom": "2016-03-25",
                "tom": "9999-12-31"
              }
            },
            {
              "periode": {
                "fom": "2019-06-25",
                "tom": "9999-12-31"
              },
              "stillingsprosent": 100.00
            }
          ]
        }
      ]
    },
    "inntekterDto": null,
    "ytelserDto": {
      "ytelser": [
        {
          "vedtaksDagsats": null,
          "ytelseAnvist": [
            {
              "anvistPeriode": {
                "fom": "2019-06-25",
                "tom": "2020-03-25"
              },
              "beløp": null,
              "dagsats": null,
              "utbetalingsgradProsent": 100.00
            }
          ],
          "relatertYtelseType": {
            "kodeverk": "RELATERT_YTELSE_TYPE",
            "kode": "FP"
          },
          "periode": {
            "fom": "2019-06-25",
            "tom": "2020-03-25"
          },
          "kilde": {
            "kodeverk": "FAGSYSTEM",
            "kode": "INFOTRYGD"
          },
          "temaUnderkategori": {
            "kodeverk": "TEMA_UNDERKATEGORI",
            "kode": "FØ"
          }
        }
      ]
    },
    "oppgittOpptjening": null,
    "inntektsmeldinger": null,
    "arbeidsforholdInformasjon": {
      "overstyringer": [
        {
          "arbeidsgiver": {
            "identType": "ORGNUMMER",
            "ident": "910909088"
          },
          "arbeidsforholdRefDto": {
            "abakusReferanse": "3f15eb0a-7d0e-48e0-86b1-299663a7c324"
          },
          "handling": {
            "kodeverk": "ARBEIDSFORHOLD_HANDLING_TYPE",
            "kode": "BRUK_UTEN_INNTEKTSMELDING"
          }
        }
      ]
    }
  },
  "opptjeningAktiviteter": {
    "perioder": [
      {
        "opptjeningAktivitetType": {
          "kodeverk": "OPPTJENING_AKTIVITET_TYPE",
          "kode": "ARBEID"
        },
        "periode": {
          "fom": "2016-03-25",
          "tom": "9999-12-31"
        },
        "arbeidsgiver": {
          "identType": "ORGNUMMER",
          "ident": "910909088"
        },
        "abakusReferanse": {
          "abakusReferanse": "3f15eb0a-7d0e-48e0-86b1-299663a7c324"
        }
      },
      {
        "opptjeningAktivitetType": {
          "kodeverk": "OPPTJENING_AKTIVITET_TYPE",
          "kode": "FORELDREPENGER"
        },
        "periode": {
          "fom": "2019-06-25",
          "tom": "2020-03-25"
        },
        "arbeidsgiver": {
          "identType": "ORGNUMMER",
          "ident": "910909088"
        },
        "abakusReferanse": null
      }
    ]
  },
  "ytelsespesifiktGrunnlag": {
    "ytelseType": "PSB",
    "dekningsgrad": 100,
    "kvalifisererTilBesteberegning": false,
    "utbetalingsgradPrAktivitet": [
      {
        "aktivitetDto": {
          "arbeidsgiver": {
            "identType": "ORGNUMMER",
            "ident": "910909088"
          },
          "uttakArbeidType": {
            "kodeverk": "UTTAK_ARBEID_TYPE",
            "kode": "AT"
          }
        },
        "periodeMedUtbetalingsgrad": [
          {
            "periode": {
              "fom": "2020-01-29",
              "tom": "2020-03-18"
            },
            "utbetalingsgrad": 100.00
          }
        ]
      },
      {
        "aktivitetDto": {
          "arbeidsgiver": {
            "identType": "ORGNUMMER",
            "ident": "910909088"
          },
          "uttakArbeidType": {
            "kode": "AT",
            "kodeverk": "UTTAK_ARBEID_TYPE"
          }
        },
        "periodeMedUtbetalingsgrad": [
          {
            "periode": {
              "fom": "2020-03-19",
              "tom": "2020-03-25"
            },
            "utbetalingsgrad": 100.00
          }
        ]
      }
    ]
  },
  "skjæringstidspunkt": "2020-01-29"
}
```

### Response
HTTP 200 - Liste med avklaringsbehov som må løses av sakssystemet (K9-SAK eller FP-SAK)


```json
{
  "avklaringsbehovMedTilstandDto" : [ {
    "beregningAvklaringsbehovDefinisjon" : {
      "kodeverk" : "BEREGNING_AKSJONSPUNKT_DEF",
      "kode" : "5058"
    },
    "venteårsak" : null,
    "ventefrist" : null
  } ]
}

```

### Request
POST /fortsett

Kjører angitt steg av beregningsprosessen

```json
{
  "eksternReferanse" : "af2ef59f-5151-4b50-9e6b-0e32b9ddac87",
  "ytelseSomSkalBeregnes" : {
    "kodeverk" : "YTELSE_TYPE",
    "kode" : "FP"
  },
  "stegType" : {
    "kodeverk" : "STEG_TYPE",
    "kode" : "KOFAKBER"
  }
}
```

### Response
HTTP 200 - Liste med avklaringsbehov som må løses av sakssystemet (K9-SAK eller FP-SAK)

```json
{
  "avklaringsbehovMedTilstandDto" : [ {
    "beregningAvklaringsbehovDefinisjon" : {
      "kodeverk" : "BEREGNING_AKSJONSPUNKT_DEF",
      "kode" : "5058"
    },
    "venteårsak" : null,
    "ventefrist" : null
  } ]
}
```

### Request
POST /oppdater

Oppdater beregningsgrunnlaget med input fra sakssystemet(K9-SAK eller FP-SAK)

```json
{
  "håndterBeregning" : {
    "identType" : "5058",
    "kode" : {
      "kodeverk" : "HÅNDTERING_KODE",
      "kode" : "5058"
    },
    "fakta" : {
      "faktaOmBeregningTilfelleDto" : {
        "tilfeller" : [ {
          "kodeverk" : "FAKTA_OM_BEREGNING_TILFELLE",
          "kode" : "VURDER_BESTEBEREGNING"
        }, {
          "kodeverk" : "FAKTA_OM_BEREGNING_TILFELLE",
          "kode" : "FASTSETT_BESTEBEREGNING_FØDENDE_KVINNE"
        } ]
      },
      "besteberegningAndeler" : {
        "besteberegningAndelListe" : [ {
          "andelsnr" : 1,
          "lagtTilAvSaksbehandler" : false,
          "fastsatteVerdier" : {
            "fastsattBeløp" : 30000,
            "inntektskategori" : {
              "kodeverk" : "INNTEKTSKATEGORI",
              "kode" : "ARBEIDSTAKER"
            }
          }
        } ],
        "nyDagpengeAndel" : {
          "fastsatteVerdier" : {
            "fastsattBeløp" : 10000,
            "inntektskategori" : {
              "kodeverk" : "INNTEKTSKATEGORI",
              "kode" : "ARBEIDSTAKER"
            }
          }
        }
      }
    }
  },
  "eksternReferanse" : "af2ef59f-5151-4b50-9e6b-0e32b9ddac87"
}
```

### Response
HTTP 200 - Liste med endringer i beregningsgrunnlaget benyttet av sakssystemet (K9-SAK eller FP-SAK) til historikk

```json
{
  "beregningsgrunnlagEndring" : {
    "beregningsgrunnlagPeriodeEndringer" : [ {
      "beregningsgrunnlagPrStatusOgAndelEndringer" : [ {
        "inntektEndring" : {
          "tilInntekt" : 360000
        },
        "aktivitetStatus" : {
          "kodeverk" : "AKTIVITET_STATUS",
          "kode" : "AT"
        },
        "arbeidsgiver" : {
          "identType" : "ORGNUMMER",
          "ident" : "994507508"
        },
        "arbeidsforholdRef" : "127b7791-8f38-4910-9424-0d764d7b2298"
      }, {
        "inntektEndring" : {
          "tilInntekt" : 120000
        },
        "inntektskategoriEndring" : {
          "kodeverk" : "INNTEKTSKATEGORI",
          "tilVerdi" : {
            "kodeverk" : "INNTEKTSKATEGORI",
            "kode" : "ARBEIDSTAKER"
          }
        },
        "aktivitetStatus" : {
          "kodeverk" : "AKTIVITET_STATUS",
          "kode" : "DP"
        }
      } ],
      "periode" : {
        "fom" : "2020-01-01",
        "tom" : "9999-12-31"
      }
    } ]
  }
}
```

### Request
POST /beregningsgrunnlag

Hent beregningsgrunnlagDto for angitt behandling som brukes av frontend til sakssystemet

```json
{
  "håndterBeregning" : {
    "identType" : "5058",
    "kode" : {
      "kodeverk" : "HÅNDTERING_KODE",
      "kode" : "5058"
    },
    "fakta" : {
      "faktaOmBeregningTilfelleDto" : {
        "tilfeller" : [ {
          "kodeverk" : "FAKTA_OM_BEREGNING_TILFELLE",
          "kode" : "VURDER_BESTEBEREGNING"
        }, {
          "kodeverk" : "FAKTA_OM_BEREGNING_TILFELLE",
          "kode" : "FASTSETT_BESTEBEREGNING_FØDENDE_KVINNE"
        } ]
      },
      "besteberegningAndeler" : {
        "besteberegningAndelListe" : [ {
          "andelsnr" : 1,
          "lagtTilAvSaksbehandler" : false,
          "fastsatteVerdier" : {
            "fastsattBeløp" : 30000,
            "inntektskategori" : {
              "kodeverk" : "INNTEKTSKATEGORI",
              "kode" : "ARBEIDSTAKER"
            }
          }
        } ],
        "nyDagpengeAndel" : {
          "fastsatteVerdier" : {
            "fastsattBeløp" : 10000,
            "inntektskategori" : {
              "kodeverk" : "INNTEKTSKATEGORI",
              "kode" : "ARBEIDSTAKER"
            }
          }
        }
      }
    }
  },
  "eksternReferanse" : "af2ef59f-5151-4b50-9e6b-0e32b9ddac87"
}
```

### Response
HTTP 200 - BeregningsgrunnlagDto 

```json
{
  "skjaeringstidspunktBeregning" : "2020-01-01",
  "skjæringstidspunkt" : "2020-01-01",
  "aktivitetStatus" : [ {
    "kodeverk" : "AKTIVITET_STATUS",
    "kode" : "AT"
  } ],
  "beregningsgrunnlagPeriode" : [ {
    "beregningsgrunnlagPeriodeFom" : "2020-01-01",
    "beregningsgrunnlagPeriodeTom" : "9999-12-31",
    "beregnetPrAar" : 0,
    "bruttoPrAar" : 0,
    "bruttoInkludertBortfaltNaturalytelsePrAar" : 0,
    "avkortetPrAar" : 0,
    "periodeAarsaker" : [ ],
    "beregningsgrunnlagPrStatusOgAndel" : [ {
      "beregningsgrunnlagFom" : "2019-10-01",
      "beregningsgrunnlagTom" : "2019-12-31",
      "aktivitetStatus" : {
        "kodeverk" : "AKTIVITET_STATUS",
        "kode" : "AT"
      },
      "beregningsperiodeFom" : "2019-10-01",
      "beregningsperiodeTom" : "2019-12-31",
      "beregnetPrAar" : null,
      "overstyrtPrAar" : null,
      "bruttoPrAar" : null,
      "avkortetPrAar" : null,
      "redusertPrAar" : null,
      "erTidsbegrensetArbeidsforhold" : null,
      "erNyIArbeidslivet" : null,
      "lonnsendringIBeregningsperioden" : null,
      "andelsnr" : 1,
      "besteberegningPrAar" : null,
      "inntektskategori" : {
        "kodeverk" : "INNTEKTSKATEGORI",
        "kode" : "ARBEIDSTAKER"
      },
      "arbeidsforhold" : {
        "arbeidsgiverNavn" : "Arbeidsgiveren",
        "arbeidsgiverId" : "994507508",
        "arbeidsgiverIdVisning" : "994507508",
        "startdato" : "2019-07-01",
        "opphoersdato" : "2020-03-31",
        "arbeidsforholdId" : "127b7791-8f38-4910-9424-0d764d7b2298",
        "arbeidsforholdType" : {
          "kodeverk" : "OPPTJENING_AKTIVITET_TYPE",
          "kode" : "ARBEID"
        },
        "belopFraInntektsmeldingPrMnd" : 44733.33
      },
      "fastsattAvSaksbehandler" : false,
      "lagtTilAvSaksbehandler" : false,
      "belopPrMndEtterAOrdningen" : 44733.0,
      "belopPrAarEtterAOrdningen" : 536796.0,
      "dagsats" : null,
      "originalDagsatsFraTilstøtendeYtelse" : null,
      "fordeltPrAar" : null,
      "erTilkommetAndel" : false,
      "skalFastsetteGrunnlag" : false
    } ]
  } ],
  "sammenligningsgrunnlagPrStatus" : [ ],
  "ledetekstBrutto" : "Brutto beregningsgrunnlag",
  "ledetekstAvkortet" : "Avkortet beregningsgrunnlag (6G=599148)",
  "ledetekstRedusert" : "Redusert beregningsgrunnlag (100%)",
  "halvG" : 49929.0,
  "grunnbeløp" : 99858.0,
  "faktaOmBeregning" : {
    "faktaOmBeregningTilfeller" : [ {
      "kodeverk" : "FAKTA_OM_BEREGNING_TILFELLE",
      "kode" : "VURDER_BESTEBEREGNING"
    } ],
    "avklarAktiviteter" : {
      "aktiviteterTomDatoMapping" : [ {
        "tom" : "2020-01-01",
        "aktiviteter" : [ {
          "arbeidsgiverNavn" : "Arbeidsgiveren",
          "arbeidsgiverId" : "994507508",
          "eksternArbeidsforholdId" : "127b7791-8f38-4910-9424-0d764d7b2298",
          "fom" : "2019-07-01",
          "tom" : "2020-03-31",
          "arbeidsforholdId" : "127b7791-8f38-4910-9424-0d764d7b2298",
          "arbeidsforholdType" : {
            "kodeverk" : "OPPTJENING_AKTIVITET_TYPE",
            "kode" : "ARBEID"
          }
        } ]
      }, {
        "tom" : "2019-07-01",
        "aktiviteter" : [ {
          "fom" : "2019-01-01",
          "tom" : "2019-06-30",
          "arbeidsforholdType" : {
            "kodeverk" : "OPPTJENING_AKTIVITET_TYPE",
            "kode" : "DAGPENGER"
          }
        } ]
      } ]
    },
    "vurderBesteberegning" : { },
    "andelerForFaktaOmBeregning" : [ {
      "belopReadOnly" : 44733.33,
      "inntektskategori" : {
        "kodeverk" : "INNTEKTSKATEGORI",
        "kode" : "ARBEIDSTAKER"
      },
      "aktivitetStatus" : {
        "kodeverk" : "AKTIVITET_STATUS",
        "kode" : "AT"
      },
      "refusjonskrav" : 44733.33,
      "visningsnavn" : "Arbeidsgiveren (994507508) ...2298",
      "arbeidsforhold" : {
        "arbeidsgiverNavn" : "Arbeidsgiveren",
        "arbeidsgiverId" : "994507508",
        "arbeidsgiverIdVisning" : "994507508",
        "startdato" : "2019-07-01",
        "opphoersdato" : "2020-03-31",
        "arbeidsforholdId" : "127b7791-8f38-4910-9424-0d764d7b2298",
        "arbeidsforholdType" : {
          "kodeverk" : "OPPTJENING_AKTIVITET_TYPE",
          "kode" : "ARBEID"
        },
        "belopFraInntektsmeldingPrMnd" : 44733.33
      },
      "andelsnr" : 1,
      "skalKunneEndreAktivitet" : false,
      "lagtTilAvSaksbehandler" : false
    } ],
    "vurderMilitaer" : { }
  },
  "hjemmel" : {
    "kodeverk" : "BG_HJEMMEL",
    "kode" : "-"
  },
  "årsinntektVisningstall" : 0,
  "dekningsgrad" : 100
}
```

### Request
POST /deaktiver

Deaktiver et beregningsgrunnlagt i kalkulus, fører til at det ikke lengre finnes et aktivt grunnlag. (Aldri mer en et aktivt grunnlag)

```json
{
  "håndterBeregning": {
    "kode": {
      "kode": "string",
      "kodeverk": "string"
    },
    "identType": "string"
  },
  "eksternReferanse": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
}
```
### Response
HTTP 200 - hvis kalkulus klarte å deaktivere grunnlaget

### Request
POST /fastsatt

Henter fastsatt beregningsgrunnlag (Bare tilgjengelig når alle beregningssteg er ferdig kjørt)

```json
{
  "eksternReferanse": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "ytelseSomSkalBeregnes": {
    "kode": "string",
    "kodeverk": "string"
  }
}
```
### Response
HTTP 200

```json
{
  "skjæringstidspunkt": "2020-01-15",
  "aktivitetStatuser": [
    {
      "kodeverk": "AKTIVITET_STATUS",
      "kode": "AT"
    }
  ],
  "beregningsgrunnlagPerioder": [
    {
      "beregningsgrunnlagPrStatusOgAndelList": [
        {
          "aktivitetStatus": {
            "kodeverk": "AKTIVITET_STATUS",
            "kode": "AT"
          },
          "beregningsperiode": {
            "fom": "2019-10-01",
            "tom": "2019-12-31"
          },
          "arbeidsforholdType": {
            "kodeverk": "OPPTJENING_AKTIVITET_TYPE",
            "kode": "ARBEID"
          },
          "bruttoPrÅr": 360000,
          "redusertRefusjonPrÅr": 0,
          "redusertBrukersAndelPrÅr": 0,
          "dagsatsBruker": 0,
          "dagsatsArbeidsgiver": 0,
          "inntektskategori": {
            "kodeverk": "INNTEKTSKATEGORI",
            "kode": "ARBEIDSTAKER"
          },
          "bgAndelArbeidsforhold": {
            "arbeidsgiver": {
              "arbeidsgiverOrgnr": "910909088"
            },
            "refusjonskravPrÅr": 0,
            "arbeidsperiodeFom": "2016-03-11",
            "arbeidsperiodeTom": "9999-12-31"
          }
        }
      ],
      "periode": {
        "fom": "2020-01-15",
        "tom": "9999-12-31"
      },
      "bruttoPrÅr": 360000,
      "avkortetPrÅr": 0,
      "redusertPrÅr": 0,
      "dagsats": 0,
      "periodeÅrsaker": []
    }
  ],
  "sammenligningsgrunnlag": {
    "sammenligningsperiode": {
      "fom": "2019-01-01",
      "tom": "2019-01-01"
    },
    "rapportertPrÅr": 0,
    "avvikPromilleNy": 1000
  },
  "sammenligningsgrunnlagPrStatusListe": [],
  "faktaOmBeregningTilfeller": [],
  "overstyrt": false
}
```

### Request
POST /grunnlag

```json
{
  "eksternReferanse": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "ytelseSomSkalBeregnes": {
    "kode": "string",
    "koderverk": "string"
  }
}
```
### Response
HTTP 200

```json
{
  "skjæringstidspunkt": "2020-01-23",
  "aktivitetStatuser": [
    {
      "kodeverk": "AKTIVITET_STATUS",
      "kode": "AT"
    }
  ],
  "beregningsgrunnlagPerioder": [
    {
      "beregningsgrunnlagPrStatusOgAndelList": [
        {
          "andelsnr": 1,
          "aktivitetStatus": {
            "kodeverk": "AKTIVITET_STATUS",
            "kode": "AT"
          },
          "beregningsperiode": {
            "fom": "2019-10-01",
            "tom": "2019-12-31"
          },
          "arbeidsforholdType": {
            "kodeverk": "OPPTJENING_AKTIVITET_TYPE",
            "kode": "ARBEID"
          },
          "bruttoPrÅr": 360000,
          "redusertRefusjonPrÅr": 0,
          "redusertBrukersAndelPrÅr": 0,
          "dagsatsBruker": 0,
          "dagsatsArbeidsgiver": 0,
          "inntektskategori": {
            "kodeverk": "INNTEKTSKATEGORI",
            "kode": "ARBEIDSTAKER"
          },
          "bgAndelArbeidsforhold": {
            "arbeidsgiver": {
              "arbeidsgiverOrgnr": "910909088"
            },
            "refusjonskravPrÅr": 0,
            "arbeidsperiodeFom": "2016-03-19",
            "arbeidsperiodeTom": "9999-12-31"
          },
          "overstyrtPrÅr": 360000,
          "avkortetPrÅr": 0,
          "redusertPrÅr": 0,
          "beregnetPrÅr": 60000,
          "maksimalRefusjonPrÅr": 0,
          "avkortetRefusjonPrÅr": 0,
          "avkortetBrukersAndelPrÅr": 0,
          "årsbeløpFraTilstøtendeYtelse": 360000,
          "fastsattAvSaksbehandler": false,
          "lagtTilAvSaksbehandler": false
        }
      ],
      "periode": {
        "fom": "2020-01-23",
        "tom": "2020-03-12"
      },
      "bruttoPrÅr": 360000,
      "avkortetPrÅr": 0,
      "redusertPrÅr": 0,
      "dagsats": 0,
      "periodeÅrsaker": [],
      "regelSporingMap": {
        "BG_PERIODE_REGEL_TYPE<VILKÅR_VURDERING>": {
          "regelEvaluering": "{}",
          "regelInput": "{}",
          "regelType": {
            "kodeverk": "BG_PERIODE_REGEL_TYPE",
            "kode": "VILKÅR_VURDERING"
          }
        },
        "BG_PERIODE_REGEL_TYPE<FORESLÅ>": {
          "regelEvaluering": "{}",
          "regelInput": "{}",
          "regelType": {
            "kodeverk": "BG_PERIODE_REGEL_TYPE",
            "kode": "FORESLÅ"
          }
        },
        "BG_PERIODE_REGEL_TYPE<FASTSETT>": {
          "regelEvaluering": "{}",
          "regelInput": "{}",
          "regelType": {
            "kodeverk": "BG_PERIODE_REGEL_TYPE",
            "kode": "FASTSETT"
          }
        },
        "BG_PERIODE_REGEL_TYPE<FORDEL>": {
          "regelType": {
            "kodeverk": "BG_PERIODE_REGEL_TYPE",
            "kode": "FORDEL"
          }
        },
        "BG_PERIODE_REGEL_TYPE<FINN_GRENSEVERDI>": {
          "regelEvaluering": "{}",
          "regelInput": "{}",
          "regelType": {
            "kodeverk": "BG_PERIODE_REGEL_TYPE",
            "kode": "FINN_GRENSEVERDI"
          }
        }
      }
    },
    {
      "beregningsgrunnlagPrStatusOgAndelList": [
        {
          "andelsnr": 1,
          "aktivitetStatus": {
            "kodeverk": "AKTIVITET_STATUS",
            "kode": "AT"
          },
          "beregningsperiode": {
            "fom": "2019-10-01",
            "tom": "2019-12-31"
          },
          "arbeidsforholdType": {
            "kodeverk": "OPPTJENING_AKTIVITET_TYPE",
            "kode": "ARBEID"
          },
          "bruttoPrÅr": 360000,
          "redusertRefusjonPrÅr": 0,
          "redusertBrukersAndelPrÅr": 360000,
          "dagsatsBruker": 1385,
          "dagsatsArbeidsgiver": 0,
          "inntektskategori": {
            "kodeverk": "INNTEKTSKATEGORI",
            "kode": "ARBEIDSTAKER"
          },
          "bgAndelArbeidsforhold": {
            "arbeidsgiver": {
              "arbeidsgiverOrgnr": "910909088"
            },
            "refusjonskravPrÅr": 0,
            "arbeidsperiodeFom": "2016-03-19",
            "arbeidsperiodeTom": "9999-12-31"
          },
          "overstyrtPrÅr": 360000,
          "avkortetPrÅr": 360000,
          "redusertPrÅr": 360000,
          "beregnetPrÅr": 60000,
          "maksimalRefusjonPrÅr": 0,
          "avkortetRefusjonPrÅr": 0,
          "avkortetBrukersAndelPrÅr": 360000,
          "årsbeløpFraTilstøtendeYtelse": 360000,
          "fastsattAvSaksbehandler": false,
          "lagtTilAvSaksbehandler": false
        }
      ],
      "periode": {
        "fom": "2020-03-13",
        "tom": "9999-12-31"
      },
      "bruttoPrÅr": 360000,
      "avkortetPrÅr": 360000,
      "redusertPrÅr": 360000,
      "dagsats": 1385,
      "periodeÅrsaker": [
        {
          "kodeverk": "PERIODE_AARSAK",
          "kode": "ENDRING_I_AKTIVITETER_SØKT_FOR"
        }
      ],
      "regelSporingMap": {
        "BG_PERIODE_REGEL_TYPE<VILKÅR_VURDERING>": {
          "regelEvaluering": "{}",
          "regelInput": "{}",
          "regelType": "{}",
            "kodeverk": "BG_PERIODE_REGEL_TYPE",
            "kode": "VILKÅR_VURDERING"
          }
        },
        "BG_PERIODE_REGEL_TYPE<FORESLÅ>": {
          "regelEvaluering": "{}",
          "regelInput": "{}",
          "regelType": {
            "kodeverk": "BG_PERIODE_REGEL_TYPE",
            "kode": "FORESLÅ"
          }
        },
        "BG_PERIODE_REGEL_TYPE<FASTSETT>": {
          "regelEvaluering": "{}",
          "regelInput": "{}",
          "regelType": {
            "kodeverk": "BG_PERIODE_REGEL_TYPE",
            "kode": "FASTSETT"
          }
        },
        "BG_PERIODE_REGEL_TYPE<FORDEL>": {
          "regelType": {
            "kodeverk": "BG_PERIODE_REGEL_TYPE",
            "kode": "FORDEL"
          }
        },
        "BG_PERIODE_REGEL_TYPE<FINN_GRENSEVERDI>": {
          "regelEvaluering": "{}",
          "regelInput": "{}",
          "regelType": {
            "kodeverk": "BG_PERIODE_REGEL_TYPE",
            "kode": "FINN_GRENSEVERDI"
          }
        }
      }
    }
  ],
  "sammenligningsgrunnlag": {
    "sammenligningsperiode": {
      "fom": "2019-01-01",
      "tom": "2019-01-01"
    },
    "rapportertPrÅr": 0,
    "avvikPromilleNy": 1000
  },
  "sammenligningsgrunnlagPrStatusListe": [],
  "faktaOmBeregningTilfeller": [],
  "overstyrt": false,
  "regelSporingMap": {
    "BG_REGEL_TYPE<PERIODISERING>": {
      "regelInput": "{}",
      "regelType": "{}",
        "kodeverk": "BG_REGEL_TYPE",
        "kode": "PERIODISERING"
      }
    },
    "BG_REGEL_TYPE<BRUKERS_STATUS>": {
      "regelEvaluering": "{}",
      "regelInput": "{}",
      "regelType": {
        "kodeverk": "BG_REGEL_TYPE",
        "kode": "BRUKERS_STATUS"
      }
    },
    "BG_REGEL_TYPE<SKJÆRINGSTIDSPUNKT>": {
      "regelEvaluering": "{}",
      "regelInput": "{}",
      "regelType": {
        "kodeverk": "BG_REGEL_TYPE",
        "kode": "SKJÆRINGSTIDSPUNKT"
      }
    }
  },
  "grunnbeløp": 99858
}
```

### Request
POST /erEndring

```json
{
  "eksternReferanse1": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "eksternReferanse2": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "ytelseSomSkalBeregnes": {
    "kode": "PSB",
    "kodeverk": "YTELSE_TYPE"
  }
}
```
### Response
HTTP 200 

```json
{

}
```
