package no.nav.folketrygdloven.kalkulator.guitjenester;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.EksternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Hjemmel;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon.RefusjonAndelTilVurderingDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon.RefusjonTilVurderingDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon.TidligereUtbetalingDto;

class VurderRefusjonDtoTjenesteTest {
    private Skjæringstidspunkt skjæringstidspunkt = Skjæringstidspunkt.builder().medSkjæringstidspunktBeregning(LocalDate.now())
            .medSkjæringstidspunktOpptjening(LocalDate.now()).build();
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock().medSkjæringstidspunkt(skjæringstidspunkt);
    private BeregningsgrunnlagDto beregningsgrunnlag;
    private BeregningsgrunnlagPeriodeDto bgPeriode;
    private BeregningsgrunnlagDto beregningsgrunnlagOrginal;
    private BeregningsgrunnlagPeriodeDto bgPeriodeOrginal;
    private InntektArbeidYtelseGrunnlagDto iay;
    private BeregningsgrunnlagRestInput input;

    @BeforeEach
    public void setup() {
        beregningsgrunnlag = no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(LocalDate.now())
                .build();
        BeregningsgrunnlagAktivitetStatusDto.builder()
                .medAktivitetStatus(no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus.ARBEIDSTAKER)
                .medHjemmel(Hjemmel.F_14_7_8_30)
                .build(beregningsgrunnlag);
        bgPeriode = buildBeregningsgrunnlagPeriode(beregningsgrunnlag);
        beregningsgrunnlagOrginal = no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(LocalDate.now())
                .build();
        BeregningsgrunnlagAktivitetStatusDto.builder()
                .medAktivitetStatus(no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus.ARBEIDSTAKER)
                .medHjemmel(Hjemmel.F_14_7_8_30)
                .build(beregningsgrunnlagOrginal);
        bgPeriodeOrginal = buildBeregningsgrunnlagPeriode(beregningsgrunnlagOrginal);
        }

    @Test
    public void skal_ikke_lage_dto_når_det_ikke_finnes_tilkommetIM() {
        String internRef = UUID.randomUUID().toString();
        lagIAYGrunnlag();
        byggBGAndel(Arbeidsgiver.virksomhet("999999999"), internRef);
        ferdigstillInput();

        Optional<RefusjonTilVurderingDto> resultat = VurderRefusjonDtoTjeneste.lagDto(input);

        assertThat(resultat).isEmpty();
    }

    @Test
    public void skal_ikke_lage_dto_når_tilkommetIM_ikke_har_refusjonskrav() {
        String internRef = UUID.randomUUID().toString();
        InntektsmeldingDto im = lagTilkommetIM("999999999", internRef, null, null);
        lagIAYGrunnlag(im);
        byggBGAndel(Arbeidsgiver.virksomhet("999999999"), internRef);
        ferdigstillInput();

        Optional<RefusjonTilVurderingDto> resultat = VurderRefusjonDtoTjeneste.lagDto(input);

        assertThat(resultat).isEmpty();
    }

    @Test
    public void skal_lage_dto_når_tilkommetIM_har_refusjonskrav_tidligere_utbetalt_bruker() {
        String internRef = UUID.randomUUID().toString();
        String orgnr = "999999999";
        LocalDate refusjonFom = LocalDate.now();
        InntektsmeldingDto im = lagTilkommetIM(orgnr, internRef, null, refusjonFom);
        lagIAYGrunnlag(im);
        byggBGAndel(Arbeidsgiver.virksomhet(orgnr), internRef);
        byggBGAndelOrginal(Arbeidsgiver.virksomhet(orgnr), internRef, 500000, 0);
        ferdigstillInput();

        Optional<RefusjonTilVurderingDto> resultat = VurderRefusjonDtoTjeneste.lagDto(input);
        TidligereUtbetalingDto tidligereUtb = new TidligereUtbetalingDto(bgPeriodeOrginal.getBeregningsgrunnlagPeriodeFom(), bgPeriodeOrginal.getBeregningsgrunnlagPeriodeTom(), false);
        assertThat(resultat).isPresent();
        assertThat(resultat.get().getAndeler()).hasSize(1);
        assertAndeler(resultat.get().getAndeler(), orgnr, internRef, refusjonFom, tidligereUtb);
    }

    @Test
    public void skal_lage_dto_når_tilkommetIM_har_refusjonskrav_tidligere_utbetalt_refusjon() {
        String internRef = UUID.randomUUID().toString();
        String orgnr = "999999999";
        LocalDate refusjonFom = LocalDate.now();
        InntektsmeldingDto im = lagTilkommetIM(orgnr, internRef, null, refusjonFom);
        lagIAYGrunnlag(im);
        byggBGAndel(Arbeidsgiver.virksomhet(orgnr), internRef);
        byggBGAndelOrginal(Arbeidsgiver.virksomhet(orgnr), internRef, 0, 500000);
        ferdigstillInput();

        Optional<RefusjonTilVurderingDto> resultat = VurderRefusjonDtoTjeneste.lagDto(input);
        TidligereUtbetalingDto tidligereUtb = new TidligereUtbetalingDto(bgPeriodeOrginal.getBeregningsgrunnlagPeriodeFom(), bgPeriodeOrginal.getBeregningsgrunnlagPeriodeTom(), true);
        assertThat(resultat).isPresent();
        assertThat(resultat.get().getAndeler()).hasSize(1);
        assertAndeler(resultat.get().getAndeler(), orgnr, internRef, refusjonFom, tidligereUtb);
    }


    private void assertAndeler(List<RefusjonAndelTilVurderingDto> andeler, String orgnr, String internRef, LocalDate refusjonFom, TidligereUtbetalingDto... tidligereUtbetaling) {
        RefusjonAndelTilVurderingDto matchetAndel = andeler.stream()
                .filter(a -> a.getArbeidsgiverId().getArbeidsgiverOrgnr().equals(orgnr)
                && Objects.equals(a.getInternArbeidsforholdRef(), internRef))
                .findFirst()
                .orElse(null);
        assertThat(matchetAndel).isNotNull();
        assertThat(matchetAndel.getNyttRefusjonskravFom()).isEqualTo(refusjonFom);

        List<TidligereUtbetalingDto> tidligereUtb = Arrays.asList(tidligereUtbetaling);
        assertThat(matchetAndel.getTidligereUtbetalinger()).containsAll(tidligereUtb);
    }

    private void ferdigstillInput() {
        BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(beregningsgrunnlag).build(BeregningsgrunnlagTilstand.VURDERT_REFUSJON);
        BeregningsgrunnlagGrunnlagDto grunnlagOrginal = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(beregningsgrunnlagOrginal).build(BeregningsgrunnlagTilstand.VURDERT_REFUSJON);

        input = new BeregningsgrunnlagRestInput(koblingReferanse, iay, AktivitetGradering.INGEN_GRADERING, List.of(), null)
                .medBeregningsgrunnlagGrunnlag(grunnlag)
                .medBeregningsgrunnlagGrunnlagFraForrigeBehandling(grunnlagOrginal);
    }

    private void lagIAYGrunnlag(InntektsmeldingDto... imer) {
        iay = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(List.of(), Arrays.asList(imer)).build();
    }

    private InntektsmeldingDto lagTilkommetIM(String orgnr, String internRef, String eksternRef, LocalDate refusjonFom) {
        return InntektsmeldingDtoBuilder.builder()
                .medArbeidsgiver(Arbeidsgiver.virksomhet(orgnr))
                .medArbeidsforholdId(InternArbeidsforholdRefDto.ref(internRef))
                .medArbeidsforholdId(EksternArbeidsforholdRef.ref(eksternRef))
                .medStartDatoPermisjon(refusjonFom).build();
    }

    private BeregningsgrunnlagPeriodeDto buildBeregningsgrunnlagPeriode(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto beregningsgrunnlag) {
        return BeregningsgrunnlagPeriodeDto.builder()
                .medBeregningsgrunnlagPeriode(LocalDate.now(), null)
                .build(beregningsgrunnlag);
    }

    private void byggBGAndel(Arbeidsgiver arbeidsgiver, String ref) {
        BGAndelArbeidsforholdDto.Builder bga = BGAndelArbeidsforholdDto
                .builder()
                .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
                .medArbeidsperiodeTom(LocalDate.now().plusYears(2))
                .medArbeidsforholdRef(ref)
                .medArbeidsgiver(arbeidsgiver);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(bga)
                .medInntektskategori(no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Inntektskategori.ARBEIDSTAKER)
                .medAndelsnr(1L)
                .medAktivitetStatus(no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus.ARBEIDSTAKER)
                .medBeregningsperiode(LocalDate.now(), LocalDate.now().plusMonths(1))
                .build(bgPeriode);
    }

    private void byggBGAndelOrginal(Arbeidsgiver arbeidsgiver, String internRef, int redusertBruker, int redusertAG) {
        BGAndelArbeidsforholdDto.Builder bga = BGAndelArbeidsforholdDto
                .builder()
                .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
                .medArbeidsperiodeTom(LocalDate.now().plusYears(2))
                .medArbeidsforholdRef(internRef)
                .medArbeidsgiver(arbeidsgiver);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(bga)
                .medInntektskategori(no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Inntektskategori.ARBEIDSTAKER)
                .medAndelsnr(1L)
                .medRedusertBrukersAndelPrÅr(BigDecimal.valueOf(redusertBruker))
                .medRedusertRefusjonPrÅr(BigDecimal.valueOf(redusertAG))
                .medAktivitetStatus(no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus.ARBEIDSTAKER)
                .medBeregningsperiode(LocalDate.now(), LocalDate.now().plusMonths(1))
                .build(bgPeriodeOrginal);
    }



}
