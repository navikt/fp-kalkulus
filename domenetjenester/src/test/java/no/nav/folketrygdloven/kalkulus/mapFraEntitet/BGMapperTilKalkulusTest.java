package no.nav.folketrygdloven.kalkulus.mapFraEntitet;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagArbeidstakerAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.InternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;

public class BGMapperTilKalkulusTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019,07,01);
    private static final String ORGANISASJON_1 = "123456789";
    private static final String ORGANISASJON_2 = "012345678";

    @Test
    public void skalIkkeHaInntektsmeldingNårInntektsmeldingHarReferanseOgAndelForSammeVirksomhetFinnesFraFørUtenReferanse(){
        BeregningsgrunnlagEntitet beregningsgrunnlagEntitet = BeregningsgrunnlagEntitet.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .build();
        BGAndelArbeidsforhold.Builder bgAndelArbeidsforhold1 = lagBGAndelArbeidsforhold(Arbeidsgiver.virksomhet(ORGANISASJON_1), InternArbeidsforholdRef.nullRef());
        BeregningsgrunnlagPrStatusOgAndel.Builder beregningsgrunnlagPrStatusOgAndel1 = BeregningsgrunnlagPrStatusOgAndel.builder()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(bgAndelArbeidsforhold1);
        BGAndelArbeidsforhold.Builder bgAndelArbeidsforhold2 = lagBGAndelArbeidsforhold(Arbeidsgiver.virksomhet(ORGANISASJON_2), InternArbeidsforholdRef.nullRef());
        BeregningsgrunnlagPrStatusOgAndel.Builder beregningsgrunnlagPrStatusOgAndel2 = BeregningsgrunnlagPrStatusOgAndel.builder()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(bgAndelArbeidsforhold2);
        BeregningsgrunnlagPeriode beregningsgrunnlagPeriode = BeregningsgrunnlagPeriode.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(11))
                .leggTilBeregningsgrunnlagPrStatusOgAndel(beregningsgrunnlagPrStatusOgAndel1)
                .leggTilBeregningsgrunnlagPrStatusOgAndel(beregningsgrunnlagPrStatusOgAndel2)
                .build(beregningsgrunnlagEntitet);

        InntektsmeldingDto inntektsmeldingDto = lagInntektsmelding(no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver.virksomhet(ORGANISASJON_1), InternArbeidsforholdRefDto.nyRef(), SKJÆRINGSTIDSPUNKT.plusDays(14));

        BeregningsgrunnlagPeriodeDto bgPeriode = BGMapperTilKalkulus.mapBeregningsgrunnlagPeriode(beregningsgrunnlagPeriode, Collections.singleton(inntektsmeldingDto)).build();

        assertThat(bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        Optional<BeregningsgrunnlagArbeidstakerAndelDto> bgArbeidstakerAndel1 = bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().get(0).getBeregningsgrunnlagArbeidstakerAndel();
        assertThat(bgArbeidstakerAndel1.isPresent());
        assertThat(bgArbeidstakerAndel1.get().getHarInntektsmelding()).isFalse();
        Optional<BeregningsgrunnlagArbeidstakerAndelDto> bgArbeidstakerAndel2 = bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().get(1).getBeregningsgrunnlagArbeidstakerAndel();
        assertThat(bgArbeidstakerAndel2.isPresent());
        assertThat(bgArbeidstakerAndel1.get().getHarInntektsmelding()).isFalse();
    }

    @Test
    public void skalHaInntektsmeldingNårInntektsmeldingErUtenReferanseOgAndelForSammeVirksomhetFinnesFraFørUtenReferanse(){
        BeregningsgrunnlagEntitet beregningsgrunnlagEntitet = BeregningsgrunnlagEntitet.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .build();
        BGAndelArbeidsforhold.Builder bgAndelArbeidsforhold1 = lagBGAndelArbeidsforhold(Arbeidsgiver.virksomhet(ORGANISASJON_1), InternArbeidsforholdRef.nullRef());
        BeregningsgrunnlagPrStatusOgAndel.Builder beregningsgrunnlagPrStatusOgAndel1 = BeregningsgrunnlagPrStatusOgAndel.builder()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(bgAndelArbeidsforhold1);
        BGAndelArbeidsforhold.Builder bgAndelArbeidsforhold2 = lagBGAndelArbeidsforhold(Arbeidsgiver.virksomhet(ORGANISASJON_2), InternArbeidsforholdRef.nullRef());
        BeregningsgrunnlagPrStatusOgAndel.Builder beregningsgrunnlagPrStatusOgAndel2 = BeregningsgrunnlagPrStatusOgAndel.builder()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(bgAndelArbeidsforhold2);
        BeregningsgrunnlagPeriode beregningsgrunnlagPeriode = BeregningsgrunnlagPeriode.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(11))
                .leggTilBeregningsgrunnlagPrStatusOgAndel(beregningsgrunnlagPrStatusOgAndel1)
                .leggTilBeregningsgrunnlagPrStatusOgAndel(beregningsgrunnlagPrStatusOgAndel2)
                .build(beregningsgrunnlagEntitet);

        InntektsmeldingDto inntektsmeldingDto = lagInntektsmelding(no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver.virksomhet(ORGANISASJON_1),
                InternArbeidsforholdRefDto.nullRef(), SKJÆRINGSTIDSPUNKT.plusDays(14));

        BeregningsgrunnlagPeriodeDto bgPeriode = BGMapperTilKalkulus.mapBeregningsgrunnlagPeriode(beregningsgrunnlagPeriode, Collections.singleton(inntektsmeldingDto)).build();

        assertThat(bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        Optional<BeregningsgrunnlagArbeidstakerAndelDto> bgArbeidstakerAndel1 =
                bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().get(0).getBeregningsgrunnlagArbeidstakerAndel();
        assertThat(bgArbeidstakerAndel1.isPresent());
        assertThat(bgArbeidstakerAndel1.get().getHarInntektsmelding()).isTrue();
        Optional<BeregningsgrunnlagArbeidstakerAndelDto> bgArbeidstakerAndel2 =
                bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().get(1).getBeregningsgrunnlagArbeidstakerAndel();
        assertThat(bgArbeidstakerAndel2.isPresent());
        assertThat(bgArbeidstakerAndel2.get().getHarInntektsmelding()).isFalse();
    }

    @Test
    public void skalHaInntektsmeldingNårInntektsmeldingHarReferanseOgAndelForSammeVirksomhetSomFinnesFraFørMedSammeReferanse(){
        UUID uuid = UUID.randomUUID();
        InternArbeidsforholdRef internArbeidsforholdRef = InternArbeidsforholdRef.ref(uuid);
        InternArbeidsforholdRefDto internArbeidsforholdRefDto = InternArbeidsforholdRefDto.ref(uuid);
        BeregningsgrunnlagEntitet beregningsgrunnlagEntitet = BeregningsgrunnlagEntitet.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .build();
        BGAndelArbeidsforhold.Builder bgAndelArbeidsforhold1 = lagBGAndelArbeidsforhold(Arbeidsgiver.virksomhet(ORGANISASJON_1), InternArbeidsforholdRef.nullRef());
        BeregningsgrunnlagPrStatusOgAndel.Builder beregningsgrunnlagPrStatusOgAndel1 = BeregningsgrunnlagPrStatusOgAndel.builder()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(bgAndelArbeidsforhold1);
        BGAndelArbeidsforhold.Builder bgAndelArbeidsforhold2 = lagBGAndelArbeidsforhold(Arbeidsgiver.virksomhet(ORGANISASJON_1), internArbeidsforholdRef);
        BeregningsgrunnlagPrStatusOgAndel.Builder beregningsgrunnlagPrStatusOgAndel2 = BeregningsgrunnlagPrStatusOgAndel.builder()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(bgAndelArbeidsforhold2);
        BeregningsgrunnlagPeriode beregningsgrunnlagPeriode = BeregningsgrunnlagPeriode.builder()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(11))
                .leggTilBeregningsgrunnlagPrStatusOgAndel(beregningsgrunnlagPrStatusOgAndel1)
                .leggTilBeregningsgrunnlagPrStatusOgAndel(beregningsgrunnlagPrStatusOgAndel2)
                .build(beregningsgrunnlagEntitet);

        InntektsmeldingDto inntektsmeldingDto = lagInntektsmelding(no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver.virksomhet(ORGANISASJON_1),
                internArbeidsforholdRefDto, SKJÆRINGSTIDSPUNKT.plusDays(14));

        BeregningsgrunnlagPeriodeDto bgPeriode = BGMapperTilKalkulus.mapBeregningsgrunnlagPeriode(beregningsgrunnlagPeriode, Collections.singleton(inntektsmeldingDto)).build();

        assertThat(bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        Optional<BeregningsgrunnlagArbeidstakerAndelDto> bgArbeidstakerAndel1 =
                bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().get(0).getBeregningsgrunnlagArbeidstakerAndel();
        assertThat(bgArbeidstakerAndel1.isPresent());
        assertThat(bgArbeidstakerAndel1.get().getHarInntektsmelding()).isFalse();
        Optional<BeregningsgrunnlagArbeidstakerAndelDto> bgArbeidstakerAndel2 =
                bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().get(1).getBeregningsgrunnlagArbeidstakerAndel();
        assertThat(bgArbeidstakerAndel2.isPresent());
        assertThat(bgArbeidstakerAndel2.get().getHarInntektsmelding()).isTrue();
    }

    private BGAndelArbeidsforhold.Builder lagBGAndelArbeidsforhold(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRef arbeidsforholdRef){
        return BGAndelArbeidsforhold.builder()
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsforholdRef(arbeidsforholdRef);
    }

    private InntektsmeldingDto lagInntektsmelding(no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdId, LocalDate startDatoPermisjon){
        return InntektsmeldingDtoBuilder.builder()
                .medArbeidsgiver(arbeidsgiver)
                .medStartDatoPermisjon(startDatoPermisjon)
                .medRefusjon(BigDecimal.valueOf(44733))
                .medBeløp(BigDecimal.valueOf(44733))
                .medArbeidsforholdId(arbeidsforholdId)
                .build();
    }

}
