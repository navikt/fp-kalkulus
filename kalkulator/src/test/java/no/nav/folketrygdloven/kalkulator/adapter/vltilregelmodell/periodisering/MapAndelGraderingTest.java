package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AndelGraderingImpl;
import no.nav.folketrygdloven.kalkulator.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.guitjenester.BeregningsgrunnlagDtoUtil;
import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.ArbeidType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FagsakYtelseType;

public class MapAndelGraderingTest {

    private static final Arbeidsgiver ARBEIDSGIVER = Arbeidsgiver.virksomhet("974761076");

    private BehandlingReferanse ref;

    @BeforeEach
    public void setUp() {
        ref = mock(BehandlingReferanse.class);
        when(ref.getSkjæringstidspunktBeregning()).thenReturn(LocalDate.now());
        when(ref.getFagsakYtelseType()).thenReturn(FagsakYtelseType.FORELDREPENGER);
    }

    @Test
    public void skalMappeAndelGraderingSN() {
        // Arrange
        LocalDate fom1 = LocalDate.now();
        LocalDate tom1 = fom1.plusWeeks(6);
        Intervall p1 = Intervall.fraOgMedTilOgMed(fom1, tom1);
        LocalDate fom2 = fom1.plusMonths(3);
        LocalDate tom2 = tom1.plusMonths(3);
        Intervall p2 = Intervall.fraOgMedTilOgMed(fom2, tom2);
        var vlAndelGradering = AndelGradering.builder()
            .medStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .leggTilGradering(new AndelGradering.Gradering(p1, BigDecimal.valueOf(50)))
            .leggTilGradering(new AndelGradering.Gradering(p2, BigDecimal.valueOf(25)))
                .medAndelsnr(1L)
            .build();
        YrkesaktivitetFilterDto filter = new YrkesaktivitetFilterDto(Optional.empty(), Optional.empty());

        // Act
        AndelGraderingImpl regelAndelGradering = MapAndelGradering.mapTilRegelAndelGradering(null, ref,
            vlAndelGradering, filter);

        // Assert
        assertThat(regelAndelGradering.getAktivitetStatus()).isEqualTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusV2.SN);
        assertThat(regelAndelGradering.getGraderinger()).hasSize(2);
        assertThat(regelAndelGradering.getGraderinger()).anySatisfy(periode -> {
            assertThat(periode.getFom()).isEqualTo(fom1);
            assertThat(periode.getTom()).isEqualTo(tom1);
        });
        assertThat(regelAndelGradering.getGraderinger()).anySatisfy(periode -> {
            assertThat(periode.getFom()).isEqualTo(fom2);
            assertThat(periode.getTom()).isEqualTo(tom2);
        });
        assertThat(regelAndelGradering.getGyldigeRefusjonskrav()).isEmpty();
    }

    @Test
    public void skalMappeAndelGraderingFL() {
// Arrange
        LocalDate fom1 = LocalDate.now();
        LocalDate tom1 = fom1.plusWeeks(6);
        Intervall p1 = Intervall.fraOgMedTilOgMed(fom1, tom1);
        LocalDate fom2 = fom1.plusMonths(3);
        LocalDate tom2 = tom1.plusMonths(3);
        Intervall p2 = Intervall.fraOgMedTilOgMed(fom2, tom2);
        var vlAndelGradering = AndelGradering.builder()
            .medStatus(AktivitetStatus.FRILANSER)
            .leggTilGradering(new AndelGradering.Gradering(p1, BigDecimal.valueOf(50)))
            .leggTilGradering(new AndelGradering.Gradering(p2, BigDecimal.valueOf(25)))
            .medAndelsnr(1L)
                .build();
        YrkesaktivitetFilterDto filter = new YrkesaktivitetFilterDto(Optional.empty(), Optional.empty());

        // Act
        AndelGraderingImpl regelAndelGradering = MapAndelGradering.mapTilRegelAndelGradering(null, ref, vlAndelGradering, filter);

        // Assert
        assertThat(regelAndelGradering.getAktivitetStatus()).isEqualTo(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusV2.FL);
        assertThat(regelAndelGradering.getGraderinger()).hasSize(2);
        assertThat(regelAndelGradering.getGraderinger()).anySatisfy(periode -> {
            assertThat(periode.getFom()).isEqualTo(fom1);
            assertThat(periode.getTom()).isEqualTo(tom1);
        });
        assertThat(regelAndelGradering.getGraderinger()).anySatisfy(periode -> {
            assertThat(periode.getFom()).isEqualTo(fom2);
            assertThat(periode.getTom()).isEqualTo(tom2);
        });
        assertThat(regelAndelGradering.getGyldigeRefusjonskrav()).isEmpty();
    }

    @Test
    public void skalMappeGraderingForArbeidstaker() {
        // Arrange
        LocalDate fom1 = LocalDate.now();
        LocalDate tom1 = fom1.plusWeeks(6);
        Intervall p1 = Intervall.fraOgMedTilOgMed(fom1, tom1);
        LocalDate fom2 = fom1.plusMonths(3);
        LocalDate tom2 = tom1.plusMonths(3);
        Intervall p2 = Intervall.fraOgMedTilOgMed(fom2, tom2);
        var vlAndelGradering = AndelGradering.builder()
            .medStatus(AktivitetStatus.ARBEIDSTAKER)
            .medArbeidsgiver(ARBEIDSGIVER)
            .leggTilGradering(new AndelGradering.Gradering(p1, BigDecimal.valueOf(50)))
            .leggTilGradering(new AndelGradering.Gradering(p2, BigDecimal.valueOf(25)))
            .medAndelsnr(1L)
            .build();
        InntektArbeidYtelseGrunnlagDtoBuilder oppdatere = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty());
        AktørId aktørId = AktørId.dummy();
        Intervall ansettelsesPeriode = Intervall.fraOgMed(fom1.minusYears(1));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = oppdatere
            .medInformasjon(ArbeidsforholdInformasjonDtoBuilder.oppdatere(Optional.empty()).build())
            .medData(InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER)
            .leggTilAktørArbeid(InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty())
                .medAktørId(aktørId)
                .leggTilYrkesaktivitet(YrkesaktivitetDtoBuilder.oppdatere(Optional.empty()).medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                    .medArbeidsgiver(ARBEIDSGIVER)
                    .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny().medPeriode(ansettelsesPeriode).medErAnsettelsesPeriode(false))
                .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny().medPeriode(ansettelsesPeriode))))).build();
        YrkesaktivitetFilterDto filter = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), iayGrunnlag.getAktørArbeidFraRegister(aktørId));

        // Act
        AndelGraderingImpl regelAndelGradering = MapAndelGradering.mapTilRegelAndelGradering(null, ref, vlAndelGradering, filter);

        // Assert
        assertThat(regelAndelGradering.getAktivitetStatus()).isEqualTo(AktivitetStatusV2.AT);
        assertThat(regelAndelGradering.getGraderinger()).hasSize(2);
        assertThat(regelAndelGradering.getGraderinger()).anySatisfy(periode -> {
            assertThat(periode.getFom()).isEqualTo(fom1);
            assertThat(periode.getTom()).isEqualTo(tom1);
        });
        assertThat(regelAndelGradering.getGraderinger()).anySatisfy(periode -> {
            assertThat(periode.getFom()).isEqualTo(fom2);
            assertThat(periode.getTom()).isEqualTo(tom2);
        });
        assertThat(regelAndelGradering.getArbeidsforhold().getOrgnr()).isEqualTo(ARBEIDSGIVER.getOrgnr());
        assertThat(regelAndelGradering.getArbeidsforhold().getAnsettelsesPeriode().getFom()).isEqualTo(ansettelsesPeriode.getFomDato());
        assertThat(regelAndelGradering.getArbeidsforhold().getAnsettelsesPeriode().getTom()).isEqualTo(ansettelsesPeriode.getTomDato());
        assertThat(regelAndelGradering.getGyldigeRefusjonskrav()).isEmpty();
    }
}
