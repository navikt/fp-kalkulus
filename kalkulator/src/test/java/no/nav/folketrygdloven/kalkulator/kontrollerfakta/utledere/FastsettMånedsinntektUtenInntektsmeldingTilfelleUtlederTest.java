package no.nav.folketrygdloven.kalkulator.kontrollerfakta.utledere;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.OrgNummer;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Inntektskategori;

public class FastsettMånedsinntektUtenInntektsmeldingTilfelleUtlederTest {

    public static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2018, 1, 1);
    public static final String ORGNR = "21348714121";
    private FastsettMånedsinntektUtenInntektsmeldingTilfelleUtleder utleder = new FastsettMånedsinntektUtenInntektsmeldingTilfelleUtleder();

    @Test
    public void skal_gi_tilfelle_om_beregningsgrunnlag_har_andel_med_kunstig_arbeid() {
        BeregningsgrunnlagGrunnlagDto grunnlag = lagGrunnlag(true);

        Optional<FaktaOmBeregningTilfelle> tilfelle = utleder.utled(null, grunnlag);
        assertThat(tilfelle.get()).isEqualTo(FaktaOmBeregningTilfelle.FASTSETT_MÅNEDSLØNN_ARBEIDSTAKER_UTEN_INNTEKTSMELDING);
    }

    @Test
    public void skal_ikkje_gi_tilfelle_om_beregningsgrunnlag_ikkje_har_andel_med_kunstig_arbeid() {
        BeregningsgrunnlagGrunnlagDto grunnlag = lagGrunnlag(false);

        Optional<FaktaOmBeregningTilfelle> tilfelle = utleder.utled(null, grunnlag);
        assertThat(tilfelle).isNotPresent();
    }

    @Test
    public void skal_gi_tilfelle_om_beregningsgrunnlag_har_andeler_for_samme_virksomhet_med_og_uten_referanse() {
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null).build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(Arbeidsgiver.virksomhet(ORGNR)))
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build(periode);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(Arbeidsgiver.virksomhet(ORGNR)).medArbeidsforholdRef(InternArbeidsforholdRefDto.nyRef()))
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build(periode);
        BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(bg).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);


        Optional<FaktaOmBeregningTilfelle> tilfelle = utleder.utled(grunnlag);
        assertThat(tilfelle).isPresent();
    }

    @Test
    public void skal_ikkje_gi_tilfelle_om_beregningsgrunnlag_kun_har_andeler_for_samme_virksomhet_med_referanse() {
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null).build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(Arbeidsgiver.virksomhet(ORGNR)).medArbeidsforholdRef(InternArbeidsforholdRefDto.nyRef()))
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build(periode);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(Arbeidsgiver.virksomhet(ORGNR)).medArbeidsforholdRef(InternArbeidsforholdRefDto.nyRef()))
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build(periode);
        BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(bg).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);


        Optional<FaktaOmBeregningTilfelle> tilfelle = utleder.utled(grunnlag);
        assertThat(tilfelle).isNotPresent();
    }



    private BeregningsgrunnlagGrunnlagDto lagGrunnlag(boolean medKunstigArbeid) {
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null).build(bg);
        String orgnr = medKunstigArbeid ? OrgNummer.KUNSTIG_ORG : ORGNR;
        BeregningsgrunnlagPrStatusOgAndelDto.kopier()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(Arbeidsgiver.virksomhet(orgnr)))
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode);
        return BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medBeregningsgrunnlag(bg).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
    }
}
