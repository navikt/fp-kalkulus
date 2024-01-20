package no.nav.folketrygdloven.kalkulus.beregning.input;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulus.rest.UgyldigInputException;


class KalkulatorInputTjenesteTest {


    private static final String GYLDIG_INPUT = """
            {
              "aktivitetGradering": null,
              "refusjonskravDatoer": null,
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
                    "utbetalingsgradArbeidsforholdDto": {
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
                    "utbetalingsgradArbeidsforholdDto": {
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
            }""";


    private static final String INPUT_UTEN_SKJÆRINGSTIDSPUNKT = """
            {
              "aktivitetGradering": null,
              "refusjonskravDatoer": null,
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
                    "utbetalingsgradArbeidsforholdDto": {
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
                    "utbetalingsgradArbeidsforholdDto": {
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
              }
            }""";



    private static final String GYLDIG_INPUT_MED_NYTT_FELT = """
            {
              "aktivitetGradering": null,
              "refusjonskravDatoer": null,
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
                    "utbetalingsgradArbeidsforholdDto": {
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
                    "utbetalingsgradArbeidsforholdDto": {
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
              "skjæringstidspunkt": "2020-01-29",
              "nyttFelt": "100.00"
            }""";


    @Test
    void skal_konvertere_input() {
        var dto = KalkulatorInputTjeneste.konverterTilInput(GYLDIG_INPUT, 1L);
    }

    @Test
    void skal_gi_empty_ved_feil() {
        assertThrows(UgyldigInputException.class, () -> KalkulatorInputTjeneste.konverterTilInput(INPUT_UTEN_SKJÆRINGSTIDSPUNKT, 1L));
    }

    @Test
    void skal_konvertere_input_med_nytt_felt() {
        var dto = KalkulatorInputTjeneste.konverterTilInput(GYLDIG_INPUT_MED_NYTT_FELT, 1L);
    }


}
