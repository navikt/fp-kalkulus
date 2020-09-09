package no.nav.folketrygdloven.kalkulator;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.BeregningsgrunnlagTestUtil;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.KontrollerFaktaBeregningFrilanserTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningIAYTestUtil;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.ArbeidType;

public class KontrollerFaktaBeregningFrilanserTjenesteImplTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, 9, 30);
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT_OPPTJENING);
    private InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder;

    @BeforeEach
    public void setup() {
        iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
    }

    @Test
    public void ikkeFrilansISammeArbeidsforholdHvisBareArbeidstaker() {
        //Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        String orgnr = "123456780";
        BeregningIAYTestUtil.byggArbeidForBehandling(koblingReferanse, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId, Arbeidsgiver.virksomhet(orgnr), iayGrunnlagBuilder);
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = iayGrunnlagBuilder.build();
        BeregningsgrunnlagDto beregningsgrunnlagDto = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(lagReferanseMedStp(koblingReferanse), SKJÆRINGSTIDSPUNKT_OPPTJENING, Optional.of(iayGrunnlag));

        //Act
        Set<Arbeidsgiver> brukerErArbeidstakerOgFrilanserISammeOrganisasjon = KontrollerFaktaBeregningFrilanserTjeneste.brukerErArbeidstakerOgFrilanserISammeOrganisasjon(
            koblingReferanse.getAktørId(), beregningsgrunnlagDto, iayGrunnlag);

        //Assert
        assertThat(brukerErArbeidstakerOgFrilanserISammeOrganisasjon).isEmpty();
    }

    @Test
    public void ikkeFrilansISammeArbeidsforholdHvisFrilansHosAnnenOppdragsgiver() {
        //Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        String orgnr = "123456780";
        BeregningIAYTestUtil.byggArbeidForBehandling(koblingReferanse, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId, Arbeidsgiver.virksomhet(orgnr), iayGrunnlagBuilder);
        String orgnrFrilans = "987654320";
        BeregningIAYTestUtil.byggArbeidForBehandling(koblingReferanse, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2),
            (InternArbeidsforholdRefDto) null, Arbeidsgiver.virksomhet(orgnrFrilans), ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER,
            singletonList(BigDecimal.TEN), false, Optional.empty(), iayGrunnlagBuilder);
        BeregningsgrunnlagDto beregningsgrunnlagDto = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(lagReferanseMedStp(koblingReferanse), SKJÆRINGSTIDSPUNKT_OPPTJENING, Optional.empty(), AktivitetStatus.KOMBINERT_AT_FL);

        //Act
        Set<Arbeidsgiver> brukerErArbeidstakerOgFrilanserISammeOrganisasjon = KontrollerFaktaBeregningFrilanserTjeneste.brukerErArbeidstakerOgFrilanserISammeOrganisasjon(
            koblingReferanse.getAktørId(), beregningsgrunnlagDto, iayGrunnlagBuilder.build());

        //Assert
        assertThat(brukerErArbeidstakerOgFrilanserISammeOrganisasjon).isEmpty();
    }

    @Test
    public void frilansISammeArbeidsforhold() {
        //Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        String orgnr = "123456780";
        BeregningIAYTestUtil.byggArbeidForBehandling(koblingReferanse, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId, Arbeidsgiver.virksomhet(orgnr), iayGrunnlagBuilder);
        BeregningIAYTestUtil.byggArbeidForBehandling(koblingReferanse, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), (InternArbeidsforholdRefDto) null, Arbeidsgiver.virksomhet(orgnr),
            ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER, singletonList(BigDecimal.TEN), false, Optional.empty(), iayGrunnlagBuilder);
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = iayGrunnlagBuilder.build();
        BeregningsgrunnlagDto beregningsgrunnlagDto = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(lagReferanseMedStp(koblingReferanse), SKJÆRINGSTIDSPUNKT_OPPTJENING, Optional.of(iayGrunnlag), AktivitetStatus.KOMBINERT_AT_FL);

        //Act
        Set<Arbeidsgiver> brukerErArbeidstakerOgFrilanserISammeOrganisasjon = KontrollerFaktaBeregningFrilanserTjeneste.brukerErArbeidstakerOgFrilanserISammeOrganisasjon(koblingReferanse.getAktørId(),
            beregningsgrunnlagDto,
            iayGrunnlag);

        //Assert
        assertThat(brukerErArbeidstakerOgFrilanserISammeOrganisasjon).hasSize(1);
    }

    private static KoblingReferanse lagReferanseMedStp(KoblingReferanse koblingReferanse) {
        return koblingReferanse.medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING);
    }
}
