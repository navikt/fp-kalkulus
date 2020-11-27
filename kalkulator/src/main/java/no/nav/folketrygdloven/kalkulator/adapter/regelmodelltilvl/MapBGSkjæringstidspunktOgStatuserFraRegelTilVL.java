package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl;

import static no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.kodeverk.MapAktivitetStatusVedSkjæringstidspunktFraRegelTilVL.mapAktivitetStatusfraRegelmodell;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.kalkulator.BeregningsperiodeTjeneste;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.kodeverk.MapOpptjeningAktivitetFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.OrgNummer;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.AktivitetStatus;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;

@ApplicationScoped
public class MapBGSkjæringstidspunktOgStatuserFraRegelTilVL {

    private Instance<BeregningsperiodeTjeneste> beregningperiodeTjenester;

    public MapBGSkjæringstidspunktOgStatuserFraRegelTilVL() {
        // CDI
    }

    @Inject
    public MapBGSkjæringstidspunktOgStatuserFraRegelTilVL(@Any Instance<BeregningsperiodeTjeneste> beregningperiodeTjenester) {
        this.beregningperiodeTjenester = beregningperiodeTjenester;
    }

    public BeregningsgrunnlagDto mapForSkjæringstidspunktOgStatuser(
        KoblingReferanse ref,
        AktivitetStatusModell regelModell,
        List<RegelResultat> regelResultater,
        InntektArbeidYtelseGrunnlagDto iayGrunnlag,
        List<Grunnbeløp> grunnbeløpSatser) {

        Objects.requireNonNull(regelModell, "regelmodell");
        // Regelresultat brukes kun til logging
        Objects.requireNonNull(regelResultater, "regelresultater");
        if (regelResultater.size() != 2) {
            throw new IllegalStateException("Antall regelresultater må være 2 for å spore regellogg");
        }

        if (regelModell.getAktivitetStatuser().containsAll(Arrays.asList(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus.DP,
            no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus.AAP))) {
            throw new IllegalStateException("Ugyldig kombinasjon av statuser: Kan ikke både ha status AAP og DP samtidig");
        }
        LocalDate skjæringstidspunktForBeregning = regelModell.getSkjæringstidspunktForBeregning();

        Grunnbeløp grunnbeløp = grunnbeløpSatser.stream()
            .filter(g -> Periode.of(g.getFom(), g.getTom()).inneholder(ref.getFørsteUttaksdato()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Fant ikke grunnbeløp for gitt dato " + ref.getFørsteUttaksdato()));

        var beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(skjæringstidspunktForBeregning)
            .medGrunnbeløp(BigDecimal.valueOf(grunnbeløp.getGVerdi()))
            .build();
        regelModell.getAktivitetStatuser()
            .forEach(as -> BeregningsgrunnlagAktivitetStatusDto.builder()
                .medAktivitetStatus(mapAktivitetStatusfraRegelmodell(regelModell, as))
                .build(beregningsgrunnlag));
        var beregningsgrunnlagPeriode = BeregningsgrunnlagPeriodeDto.builder()
            .medBeregningsgrunnlagPeriode(skjæringstidspunktForBeregning, null)
            .build(beregningsgrunnlag);

        YrkesaktivitetFilterDto filter = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), iayGrunnlag.getAktørArbeidFraRegister());

        opprettBeregningsgrunnlagPrStatusOgAndelForSkjæringstidspunkt(ref, filter, regelModell, beregningsgrunnlagPeriode);
        return beregningsgrunnlag;
    }

    private void opprettBeregningsgrunnlagPrStatusOgAndelForSkjæringstidspunkt(KoblingReferanse ref, YrkesaktivitetFilterDto filter, AktivitetStatusModell regelmodell,
                                                                               BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode) {
        var skjæringstidspunkt = regelmodell.getSkjæringstidspunktForBeregning();
        var beregningsperiode = lagBeregningsperiode(ref, skjæringstidspunkt);
        regelmodell.getBeregningsgrunnlagPrStatusListe().stream()
            .filter(bgps -> erATFL(bgps.getAktivitetStatus()))
            .forEach(bgps -> bgps.getArbeidsforholdList()
                .forEach(af -> {
                    var arbeidsgiver = MapArbeidsforholdFraRegelTilVL.map(af);
                    var iaRef = InternArbeidsforholdRefDto.ref(af.getArbeidsforholdId());
                    var ansettelsesPerioder = filter.getYrkesaktiviteterForBeregning().stream()
                        .filter(ya -> ya.gjelderFor(arbeidsgiver, iaRef))
                        .map(filter::getAnsettelsesPerioder)
                        .flatMap(Collection::stream)
                        .filter(a -> a.getPeriode().getFomDato().isBefore(skjæringstidspunkt))
                        .collect(Collectors.toList());
                    LocalDate arbeidsperiodeFom = ansettelsesPerioder.stream().map(a -> a.getPeriode().getFomDato()).min(LocalDate::compareTo).orElse(null);
                    LocalDate arbeidsperiodeTom = ansettelsesPerioder.stream().map(a -> a.getPeriode().getTomDato()).max(LocalDate::compareTo).orElse(null);

                    if (erKunstig(arbeidsgiver)) {
                        if (arbeidsperiodeFom == null) {
                            arbeidsperiodeFom = beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom();
                        }
                        if (arbeidsperiodeTom == null) {
                            arbeidsperiodeTom = beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeTom();
                        }
                    }

                    var andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                        .medArbforholdType(MapOpptjeningAktivitetFraRegelTilVL.map(af.getAktivitet()))
                        .medAktivitetStatus(af.erFrilanser() ? AktivitetStatus.FRILANSER : AktivitetStatus.ARBEIDSTAKER)
                        .medBeregningsperiode(beregningsperiode.getFomDato(), beregningsperiode.getTomDato());
                    if (arbeidsperiodeFom != null || af.getReferanseType() != null || af.getArbeidsforholdId() != null) {
                        BGAndelArbeidsforholdDto.Builder bgArbeidsforholdBuilder = BGAndelArbeidsforholdDto.builder()
                            .medArbeidsgiver(arbeidsgiver)
                            .medArbeidsforholdRef(af.getArbeidsforholdId())
                            .medArbeidsperiodeTom(arbeidsperiodeTom)
                            .medArbeidsperiodeFom(arbeidsperiodeFom);
                        andelBuilder.medBGAndelArbeidsforhold(bgArbeidsforholdBuilder);
                    }
                    andelBuilder
                        .build(beregningsgrunnlagPeriode);
                }));
        regelmodell.getBeregningsgrunnlagPrStatusListe().stream()
            .filter(bgps -> !(erATFL(bgps.getAktivitetStatus())))
            .forEach(bgps -> BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(mapAktivitetStatusfraRegelmodell(regelmodell, bgps.getAktivitetStatus()))
                .medArbforholdType(MapOpptjeningAktivitetFraRegelTilVL.map(bgps.getAktivitetStatus()))
                .build(beregningsgrunnlagPeriode));
    }

    private boolean erKunstig(Arbeidsgiver arbeidsgiver) {
        return arbeidsgiver != null && arbeidsgiver.getErVirksomhet() && OrgNummer.KUNSTIG_ORG.equals(arbeidsgiver.getIdentifikator());
    }

    protected Intervall lagBeregningsperiode(KoblingReferanse ref, LocalDate skjæringstidspunkt) {
        var periodeTjeneste = FagsakYtelseTypeRef.Lookup.find(beregningperiodeTjenester, ref.getFagsakYtelseType())
                .orElseThrow(() -> new IllegalStateException("Finner ikke implementasjon for håndtering av refusjon/gradering for BehandlingReferanse " + ref));
        return periodeTjeneste.fastsettBeregningsperiodeForATFLAndeler(skjæringstidspunkt);
    }

    private static boolean erATFL(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus aktivitetStatus) {
        return no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus.ATFL.equals(aktivitetStatus);
    }
}
