package no.nav.folketrygdloven.kalkulus.beregning.input;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;


class KalkulatorInputLegacyTjenesteTest {


    private static final String LEGACY_INPUT = """
            {
              "aktivitetGradering": null,
              "refusjonskravDatoer": null,
              "iayGrunnlag": {
                "arbeidDto": {
                  "yrkesaktiviteter": [
                    {
                      "arbeidsgiver": {
                        "identType": "ORGNUMMER",
                        "ident": "974652269"
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
                      "vedtaksDagsats": {},
                      "ytelseAnvist": [
                        {
                          "anvistPeriode": {
                            "fom": "2019-06-25",
                            "tom": "2020-03-25"
                          },
                          "beløp": 0,
                          "dagsats": {
                             "verdi": 0
                          },
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
                        "ident": "974652269"
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
                      "ident": "974652269"
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
                      "ident": "974652269"
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
                        "ident": "974652269"
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
                        "ident": "974652269"
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

    @Test
    void skal_konvertere_input() {
        KalkulatorInputTjeneste.konverterTilInput(LEGACY_INPUT, 1L);
    }

    @Test
    void skal_lese_kalkulator_input_json() throws Exception {
        var input = Optional.ofNullable(lesEksempelfil()).orElseThrow();
        KalkulatorInputDto grunnlag = KalkulatorInputTjeneste.konverterTilInput(input, 123L);

        assertThat(grunnlag).isNotNull();
        assertThat(grunnlag.getIayGrunnlag()).isNotNull();
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var violations = validator.validate(grunnlag);
            assertThat(violations).isEmpty();
        }
    }

    private String lesEksempelfil() throws IOException {
        try (var in = KalkulatorInputLegacyTjenesteTest.class.getResourceAsStream("/input/eksempel-legacy-input.json")) {
            return in != null ? new String(in.readAllBytes()) : null;
        }
    }


}
