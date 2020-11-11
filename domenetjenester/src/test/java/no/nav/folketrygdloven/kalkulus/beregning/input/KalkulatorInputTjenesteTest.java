package no.nav.folketrygdloven.kalkulus.beregning.input;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;


class KalkulatorInputTjenesteTest {


    private String GYLDIG_INPUT = "{\n" +
            "  \"aktivitetGradering\": null,\n" +
            "  \"refusjonskravDatoer\": null,\n" +
            "  \"iayGrunnlag\": {\n" +
            "    \"arbeidDto\": {\n" +
            "      \"yrkesaktiviteter\": [\n" +
            "        {\n" +
            "          \"arbeidsgiver\": {\n" +
            "            \"identType\": \"ORGNUMMER\",\n" +
            "            \"ident\": \"910909088\"\n" +
            "          },\n" +
            "          \"abakusReferanse\": {\n" +
            "            \"abakusReferanse\": \"3f15eb0a-7d0e-48e0-86b1-299663a7c324\"\n" +
            "          },\n" +
            "          \"arbeidType\": {\n" +
            "            \"kodeverk\": \"ARBEID_TYPE\",\n" +
            "            \"kode\": \"ORDINÆRT_ARBEIDSFORHOLD\"\n" +
            "          },\n" +
            "          \"aktivitetsAvtaler\": [\n" +
            "            {\n" +
            "              \"periode\": {\n" +
            "                \"fom\": \"2016-03-25\",\n" +
            "                \"tom\": \"9999-12-31\"\n" +
            "              }\n" +
            "            },\n" +
            "            {\n" +
            "              \"periode\": {\n" +
            "                \"fom\": \"2019-06-25\",\n" +
            "                \"tom\": \"9999-12-31\"\n" +
            "              },\n" +
            "              \"stillingsprosent\": 100.00\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    \"inntekterDto\": null,\n" +
            "    \"ytelserDto\": {\n" +
            "      \"ytelser\": [\n" +
            "        {\n" +
            "          \"vedtaksDagsats\": null,\n" +
            "          \"ytelseAnvist\": [\n" +
            "            {\n" +
            "              \"anvistPeriode\": {\n" +
            "                \"fom\": \"2019-06-25\",\n" +
            "                \"tom\": \"2020-03-25\"\n" +
            "              },\n" +
            "              \"beløp\": null,\n" +
            "              \"dagsats\": null,\n" +
            "              \"utbetalingsgradProsent\": 100.00\n" +
            "            }\n" +
            "          ],\n" +
            "          \"relatertYtelseType\": {\n" +
            "            \"kodeverk\": \"RELATERT_YTELSE_TYPE\",\n" +
            "            \"kode\": \"FP\"\n" +
            "          },\n" +
            "          \"periode\": {\n" +
            "            \"fom\": \"2019-06-25\",\n" +
            "            \"tom\": \"2020-03-25\"\n" +
            "          },\n" +
            "          \"kilde\": {\n" +
            "            \"kodeverk\": \"FAGSYSTEM\",\n" +
            "            \"kode\": \"INFOTRYGD\"\n" +
            "          },\n" +
            "          \"temaUnderkategori\": {\n" +
            "            \"kodeverk\": \"TEMA_UNDERKATEGORI\",\n" +
            "            \"kode\": \"FØ\"\n" +
            "          }\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    \"oppgittOpptjening\": null,\n" +
            "    \"inntektsmeldinger\": null,\n" +
            "    \"arbeidsforholdInformasjon\": {\n" +
            "      \"overstyringer\": [\n" +
            "        {\n" +
            "          \"arbeidsgiver\": {\n" +
            "            \"identType\": \"ORGNUMMER\",\n" +
            "            \"ident\": \"910909088\"\n" +
            "          },\n" +
            "          \"arbeidsforholdRefDto\": {\n" +
            "            \"abakusReferanse\": \"3f15eb0a-7d0e-48e0-86b1-299663a7c324\"\n" +
            "          },\n" +
            "          \"handling\": {\n" +
            "            \"kodeverk\": \"ARBEIDSFORHOLD_HANDLING_TYPE\",\n" +
            "            \"kode\": \"BRUK_UTEN_INNTEKTSMELDING\"\n" +
            "          }\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  },\n" +
            "  \"opptjeningAktiviteter\": {\n" +
            "    \"perioder\": [\n" +
            "      {\n" +
            "        \"opptjeningAktivitetType\": {\n" +
            "          \"kodeverk\": \"OPPTJENING_AKTIVITET_TYPE\",\n" +
            "          \"kode\": \"ARBEID\"\n" +
            "        },\n" +
            "        \"periode\": {\n" +
            "          \"fom\": \"2016-03-25\",\n" +
            "          \"tom\": \"9999-12-31\"\n" +
            "        },\n" +
            "        \"arbeidsgiver\": {\n" +
            "          \"identType\": \"ORGNUMMER\",\n" +
            "          \"ident\": \"910909088\"\n" +
            "        },\n" +
            "        \"abakusReferanse\": {\n" +
            "          \"abakusReferanse\": \"3f15eb0a-7d0e-48e0-86b1-299663a7c324\"\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"opptjeningAktivitetType\": {\n" +
            "          \"kodeverk\": \"OPPTJENING_AKTIVITET_TYPE\",\n" +
            "          \"kode\": \"FORELDREPENGER\"\n" +
            "        },\n" +
            "        \"periode\": {\n" +
            "          \"fom\": \"2019-06-25\",\n" +
            "          \"tom\": \"2020-03-25\"\n" +
            "        },\n" +
            "        \"arbeidsgiver\": {\n" +
            "          \"identType\": \"ORGNUMMER\",\n" +
            "          \"ident\": \"910909088\"\n" +
            "        },\n" +
            "        \"abakusReferanse\": null\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  \"ytelsespesifiktGrunnlag\": {\n" +
            "    \"ytelseType\": \"PSB\",\n" +
            "    \"dekningsgrad\": 100,\n" +
            "    \"kvalifisererTilBesteberegning\": false,\n" +
            "    \"utbetalingsgradPrAktivitet\": [\n" +
            "      {\n" +
            "        \"utbetalingsgradArbeidsforholdDto\": {\n" +
            "          \"arbeidsgiver\": {\n" +
            "            \"identType\": \"ORGNUMMER\",\n" +
            "            \"ident\": \"910909088\"\n" +
            "          },\n" +
            "          \"uttakArbeidType\": {\n" +
            "            \"kodeverk\": \"UTTAK_ARBEID_TYPE\",\n" +
            "            \"kode\": \"AT\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"periodeMedUtbetalingsgrad\": [\n" +
            "          {\n" +
            "            \"periode\": {\n" +
            "              \"fom\": \"2020-01-29\",\n" +
            "              \"tom\": \"2020-03-18\"\n" +
            "            },\n" +
            "            \"utbetalingsgrad\": 100.00\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      {\n" +
            "        \"utbetalingsgradArbeidsforholdDto\": {\n" +
            "          \"arbeidsgiver\": {\n" +
            "            \"identType\": \"ORGNUMMER\",\n" +
            "            \"ident\": \"910909088\"\n" +
            "          },\n" +
            "          \"uttakArbeidType\": {\n" +
            "            \"kode\": \"AT\",\n" +
            "            \"kodeverk\": \"UTTAK_ARBEID_TYPE\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"periodeMedUtbetalingsgrad\": [\n" +
            "          {\n" +
            "            \"periode\": {\n" +
            "              \"fom\": \"2020-03-19\",\n" +
            "              \"tom\": \"2020-03-25\"\n" +
            "            },\n" +
            "            \"utbetalingsgrad\": 100.00\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  \"skjæringstidspunkt\": \"2020-01-29\"\n" +
            "}";


    private String INPUT_UTEN_SKJÆRINGSTIDSPUNKT = "{\n" +
            "  \"aktivitetGradering\": null,\n" +
            "  \"refusjonskravDatoer\": null,\n" +
            "  \"iayGrunnlag\": {\n" +
            "    \"arbeidDto\": {\n" +
            "      \"yrkesaktiviteter\": [\n" +
            "        {\n" +
            "          \"arbeidsgiver\": {\n" +
            "            \"identType\": \"ORGNUMMER\",\n" +
            "            \"ident\": \"910909088\"\n" +
            "          },\n" +
            "          \"abakusReferanse\": {\n" +
            "            \"abakusReferanse\": \"3f15eb0a-7d0e-48e0-86b1-299663a7c324\"\n" +
            "          },\n" +
            "          \"arbeidType\": {\n" +
            "            \"kodeverk\": \"ARBEID_TYPE\",\n" +
            "            \"kode\": \"ORDINÆRT_ARBEIDSFORHOLD\"\n" +
            "          },\n" +
            "          \"aktivitetsAvtaler\": [\n" +
            "            {\n" +
            "              \"periode\": {\n" +
            "                \"fom\": \"2016-03-25\",\n" +
            "                \"tom\": \"9999-12-31\"\n" +
            "              }\n" +
            "            },\n" +
            "            {\n" +
            "              \"periode\": {\n" +
            "                \"fom\": \"2019-06-25\",\n" +
            "                \"tom\": \"9999-12-31\"\n" +
            "              },\n" +
            "              \"stillingsprosent\": 100.00\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    \"inntekterDto\": null,\n" +
            "    \"ytelserDto\": {\n" +
            "      \"ytelser\": [\n" +
            "        {\n" +
            "          \"vedtaksDagsats\": null,\n" +
            "          \"ytelseAnvist\": [\n" +
            "            {\n" +
            "              \"anvistPeriode\": {\n" +
            "                \"fom\": \"2019-06-25\",\n" +
            "                \"tom\": \"2020-03-25\"\n" +
            "              },\n" +
            "              \"beløp\": null,\n" +
            "              \"dagsats\": null,\n" +
            "              \"utbetalingsgradProsent\": 100.00\n" +
            "            }\n" +
            "          ],\n" +
            "          \"relatertYtelseType\": {\n" +
            "            \"kodeverk\": \"RELATERT_YTELSE_TYPE\",\n" +
            "            \"kode\": \"FP\"\n" +
            "          },\n" +
            "          \"periode\": {\n" +
            "            \"fom\": \"2019-06-25\",\n" +
            "            \"tom\": \"2020-03-25\"\n" +
            "          },\n" +
            "          \"kilde\": {\n" +
            "            \"kodeverk\": \"FAGSYSTEM\",\n" +
            "            \"kode\": \"INFOTRYGD\"\n" +
            "          },\n" +
            "          \"temaUnderkategori\": {\n" +
            "            \"kodeverk\": \"TEMA_UNDERKATEGORI\",\n" +
            "            \"kode\": \"FØ\"\n" +
            "          }\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    \"oppgittOpptjening\": null,\n" +
            "    \"inntektsmeldinger\": null,\n" +
            "    \"arbeidsforholdInformasjon\": {\n" +
            "      \"overstyringer\": [\n" +
            "        {\n" +
            "          \"arbeidsgiver\": {\n" +
            "            \"identType\": \"ORGNUMMER\",\n" +
            "            \"ident\": \"910909088\"\n" +
            "          },\n" +
            "          \"arbeidsforholdRefDto\": {\n" +
            "            \"abakusReferanse\": \"3f15eb0a-7d0e-48e0-86b1-299663a7c324\"\n" +
            "          },\n" +
            "          \"handling\": {\n" +
            "            \"kodeverk\": \"ARBEIDSFORHOLD_HANDLING_TYPE\",\n" +
            "            \"kode\": \"BRUK_UTEN_INNTEKTSMELDING\"\n" +
            "          }\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  },\n" +
            "  \"opptjeningAktiviteter\": {\n" +
            "    \"perioder\": [\n" +
            "      {\n" +
            "        \"opptjeningAktivitetType\": {\n" +
            "          \"kodeverk\": \"OPPTJENING_AKTIVITET_TYPE\",\n" +
            "          \"kode\": \"ARBEID\"\n" +
            "        },\n" +
            "        \"periode\": {\n" +
            "          \"fom\": \"2016-03-25\",\n" +
            "          \"tom\": \"9999-12-31\"\n" +
            "        },\n" +
            "        \"arbeidsgiver\": {\n" +
            "          \"identType\": \"ORGNUMMER\",\n" +
            "          \"ident\": \"910909088\"\n" +
            "        },\n" +
            "        \"abakusReferanse\": {\n" +
            "          \"abakusReferanse\": \"3f15eb0a-7d0e-48e0-86b1-299663a7c324\"\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"opptjeningAktivitetType\": {\n" +
            "          \"kodeverk\": \"OPPTJENING_AKTIVITET_TYPE\",\n" +
            "          \"kode\": \"FORELDREPENGER\"\n" +
            "        },\n" +
            "        \"periode\": {\n" +
            "          \"fom\": \"2019-06-25\",\n" +
            "          \"tom\": \"2020-03-25\"\n" +
            "        },\n" +
            "        \"arbeidsgiver\": {\n" +
            "          \"identType\": \"ORGNUMMER\",\n" +
            "          \"ident\": \"910909088\"\n" +
            "        },\n" +
            "        \"abakusReferanse\": null\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  \"ytelsespesifiktGrunnlag\": {\n" +
            "    \"ytelseType\": \"PSB\",\n" +
            "    \"dekningsgrad\": 100,\n" +
            "    \"kvalifisererTilBesteberegning\": false,\n" +
            "    \"utbetalingsgradPrAktivitet\": [\n" +
            "      {\n" +
            "        \"utbetalingsgradArbeidsforholdDto\": {\n" +
            "          \"arbeidsgiver\": {\n" +
            "            \"identType\": \"ORGNUMMER\",\n" +
            "            \"ident\": \"910909088\"\n" +
            "          },\n" +
            "          \"uttakArbeidType\": {\n" +
            "            \"kodeverk\": \"UTTAK_ARBEID_TYPE\",\n" +
            "            \"kode\": \"AT\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"periodeMedUtbetalingsgrad\": [\n" +
            "          {\n" +
            "            \"periode\": {\n" +
            "              \"fom\": \"2020-01-29\",\n" +
            "              \"tom\": \"2020-03-18\"\n" +
            "            },\n" +
            "            \"utbetalingsgrad\": 100.00\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      {\n" +
            "        \"utbetalingsgradArbeidsforholdDto\": {\n" +
            "          \"arbeidsgiver\": {\n" +
            "            \"identType\": \"ORGNUMMER\",\n" +
            "            \"ident\": \"910909088\"\n" +
            "          },\n" +
            "          \"uttakArbeidType\": {\n" +
            "            \"kode\": \"AT\",\n" +
            "            \"kodeverk\": \"UTTAK_ARBEID_TYPE\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"periodeMedUtbetalingsgrad\": [\n" +
            "          {\n" +
            "            \"periode\": {\n" +
            "              \"fom\": \"2020-03-19\",\n" +
            "              \"tom\": \"2020-03-25\"\n" +
            "            },\n" +
            "            \"utbetalingsgrad\": 100.00\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";



    private String GYLDIG_INPUT_MED_NYTT_FELT = "{\n" +
            "  \"aktivitetGradering\": null,\n" +
            "  \"refusjonskravDatoer\": null,\n" +
            "  \"iayGrunnlag\": {\n" +
            "    \"arbeidDto\": {\n" +
            "      \"yrkesaktiviteter\": [\n" +
            "        {\n" +
            "          \"arbeidsgiver\": {\n" +
            "            \"identType\": \"ORGNUMMER\",\n" +
            "            \"ident\": \"910909088\"\n" +
            "          },\n" +
            "          \"abakusReferanse\": {\n" +
            "            \"abakusReferanse\": \"3f15eb0a-7d0e-48e0-86b1-299663a7c324\"\n" +
            "          },\n" +
            "          \"arbeidType\": {\n" +
            "            \"kodeverk\": \"ARBEID_TYPE\",\n" +
            "            \"kode\": \"ORDINÆRT_ARBEIDSFORHOLD\"\n" +
            "          },\n" +
            "          \"aktivitetsAvtaler\": [\n" +
            "            {\n" +
            "              \"periode\": {\n" +
            "                \"fom\": \"2016-03-25\",\n" +
            "                \"tom\": \"9999-12-31\"\n" +
            "              }\n" +
            "            },\n" +
            "            {\n" +
            "              \"periode\": {\n" +
            "                \"fom\": \"2019-06-25\",\n" +
            "                \"tom\": \"9999-12-31\"\n" +
            "              },\n" +
            "              \"stillingsprosent\": 100.00\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    \"inntekterDto\": null,\n" +
            "    \"ytelserDto\": {\n" +
            "      \"ytelser\": [\n" +
            "        {\n" +
            "          \"vedtaksDagsats\": null,\n" +
            "          \"ytelseAnvist\": [\n" +
            "            {\n" +
            "              \"anvistPeriode\": {\n" +
            "                \"fom\": \"2019-06-25\",\n" +
            "                \"tom\": \"2020-03-25\"\n" +
            "              },\n" +
            "              \"beløp\": null,\n" +
            "              \"dagsats\": null,\n" +
            "              \"utbetalingsgradProsent\": 100.00\n" +
            "            }\n" +
            "          ],\n" +
            "          \"relatertYtelseType\": {\n" +
            "            \"kodeverk\": \"RELATERT_YTELSE_TYPE\",\n" +
            "            \"kode\": \"FP\"\n" +
            "          },\n" +
            "          \"periode\": {\n" +
            "            \"fom\": \"2019-06-25\",\n" +
            "            \"tom\": \"2020-03-25\"\n" +
            "          },\n" +
            "          \"kilde\": {\n" +
            "            \"kodeverk\": \"FAGSYSTEM\",\n" +
            "            \"kode\": \"INFOTRYGD\"\n" +
            "          },\n" +
            "          \"temaUnderkategori\": {\n" +
            "            \"kodeverk\": \"TEMA_UNDERKATEGORI\",\n" +
            "            \"kode\": \"FØ\"\n" +
            "          }\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    \"oppgittOpptjening\": null,\n" +
            "    \"inntektsmeldinger\": null,\n" +
            "    \"arbeidsforholdInformasjon\": {\n" +
            "      \"overstyringer\": [\n" +
            "        {\n" +
            "          \"arbeidsgiver\": {\n" +
            "            \"identType\": \"ORGNUMMER\",\n" +
            "            \"ident\": \"910909088\"\n" +
            "          },\n" +
            "          \"arbeidsforholdRefDto\": {\n" +
            "            \"abakusReferanse\": \"3f15eb0a-7d0e-48e0-86b1-299663a7c324\"\n" +
            "          },\n" +
            "          \"handling\": {\n" +
            "            \"kodeverk\": \"ARBEIDSFORHOLD_HANDLING_TYPE\",\n" +
            "            \"kode\": \"BRUK_UTEN_INNTEKTSMELDING\"\n" +
            "          }\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  },\n" +
            "  \"opptjeningAktiviteter\": {\n" +
            "    \"perioder\": [\n" +
            "      {\n" +
            "        \"opptjeningAktivitetType\": {\n" +
            "          \"kodeverk\": \"OPPTJENING_AKTIVITET_TYPE\",\n" +
            "          \"kode\": \"ARBEID\"\n" +
            "        },\n" +
            "        \"periode\": {\n" +
            "          \"fom\": \"2016-03-25\",\n" +
            "          \"tom\": \"9999-12-31\"\n" +
            "        },\n" +
            "        \"arbeidsgiver\": {\n" +
            "          \"identType\": \"ORGNUMMER\",\n" +
            "          \"ident\": \"910909088\"\n" +
            "        },\n" +
            "        \"abakusReferanse\": {\n" +
            "          \"abakusReferanse\": \"3f15eb0a-7d0e-48e0-86b1-299663a7c324\"\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"opptjeningAktivitetType\": {\n" +
            "          \"kodeverk\": \"OPPTJENING_AKTIVITET_TYPE\",\n" +
            "          \"kode\": \"FORELDREPENGER\"\n" +
            "        },\n" +
            "        \"periode\": {\n" +
            "          \"fom\": \"2019-06-25\",\n" +
            "          \"tom\": \"2020-03-25\"\n" +
            "        },\n" +
            "        \"arbeidsgiver\": {\n" +
            "          \"identType\": \"ORGNUMMER\",\n" +
            "          \"ident\": \"910909088\"\n" +
            "        },\n" +
            "        \"abakusReferanse\": null\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  \"ytelsespesifiktGrunnlag\": {\n" +
            "    \"ytelseType\": \"PSB\",\n" +
            "    \"dekningsgrad\": 100,\n" +
            "    \"kvalifisererTilBesteberegning\": false,\n" +
            "    \"utbetalingsgradPrAktivitet\": [\n" +
            "      {\n" +
            "        \"utbetalingsgradArbeidsforholdDto\": {\n" +
            "          \"arbeidsgiver\": {\n" +
            "            \"identType\": \"ORGNUMMER\",\n" +
            "            \"ident\": \"910909088\"\n" +
            "          },\n" +
            "          \"uttakArbeidType\": {\n" +
            "            \"kodeverk\": \"UTTAK_ARBEID_TYPE\",\n" +
            "            \"kode\": \"AT\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"periodeMedUtbetalingsgrad\": [\n" +
            "          {\n" +
            "            \"periode\": {\n" +
            "              \"fom\": \"2020-01-29\",\n" +
            "              \"tom\": \"2020-03-18\"\n" +
            "            },\n" +
            "            \"utbetalingsgrad\": 100.00\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      {\n" +
            "        \"utbetalingsgradArbeidsforholdDto\": {\n" +
            "          \"arbeidsgiver\": {\n" +
            "            \"identType\": \"ORGNUMMER\",\n" +
            "            \"ident\": \"910909088\"\n" +
            "          },\n" +
            "          \"uttakArbeidType\": {\n" +
            "            \"kode\": \"AT\",\n" +
            "            \"kodeverk\": \"UTTAK_ARBEID_TYPE\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"periodeMedUtbetalingsgrad\": [\n" +
            "          {\n" +
            "            \"periode\": {\n" +
            "              \"fom\": \"2020-03-19\",\n" +
            "              \"tom\": \"2020-03-25\"\n" +
            "            },\n" +
            "            \"utbetalingsgrad\": 100.00\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  \"skjæringstidspunkt\": \"2020-01-29\",\n" +
            "  \"nyttFelt\": \"100.00\"\n" +
            "}";


    @Test
    void skal_konvertere_input() {
        var dto = KalkulatorInputTjeneste.konverterTilInput(GYLDIG_INPUT, 1L);
        assertThat(dto).isPresent();
    }

    @Test
    void skal_gi_empty_ved_feil() {
        var dto = KalkulatorInputTjeneste.konverterTilInput(INPUT_UTEN_SKJÆRINGSTIDSPUNKT, 1L);
        assertThat(dto).isEmpty();

    }

    @Test
    void skal_konvertere_input_med_nytt_felt() {
        var dto = KalkulatorInputTjeneste.konverterTilInput(GYLDIG_INPUT_MED_NYTT_FELT, 1L);
        assertThat(dto).isPresent();
    }


}
