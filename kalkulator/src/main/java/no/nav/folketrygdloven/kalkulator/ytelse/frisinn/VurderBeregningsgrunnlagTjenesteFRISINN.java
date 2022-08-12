package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.vurder.frisinn.RegelVurderBeregningsgrunnlagFRISINN;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningVilkårResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.vilkår.VurderBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Vilkårsavslagsårsak;
import no.nav.fpsak.nare.evaluation.Evaluation;

@ApplicationScoped
@FagsakYtelseTypeRef(FagsakYtelseType.FRISINN)
public class VurderBeregningsgrunnlagTjenesteFRISINN extends VurderBeregningsgrunnlagTjeneste {

    public VurderBeregningsgrunnlagTjenesteFRISINN() {
        // CDI
    }

    @Inject
    public VurderBeregningsgrunnlagTjenesteFRISINN(MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel) {
        super(mapBeregningsgrunnlagFraVLTilRegel);
    }

    @Override
    public BeregningsgrunnlagRegelResultat vurderBeregningsgrunnlag(BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDto oppdatertGrunnlag) {
        // Oversetter foreslått Beregningsgrunnlag -> regelmodell
        var beregningsgrunnlagRegel = mapBeregningsgrunnlagFraVLTilRegel.map(input, oppdatertGrunnlag);
        if (!(input.getYtelsespesifiktGrunnlag() instanceof FrisinnGrunnlag)) {
            throw new IllegalStateException("Har ikke FRISINN grunnlag når frisinnvilkår skal vurderes");
        }
        List<RegelResultat> regelResultater = kjørRegel(input, beregningsgrunnlagRegel);
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = Collections.emptyList();
        return mapTilRegelresultat(input, regelResultater, oppdatertGrunnlag.getBeregningsgrunnlag().orElseThrow(), avklaringsbehov);
    }

    @Override
    protected List<RegelResultat> kjørRegel(BeregningsgrunnlagInput input, Beregningsgrunnlag beregningsgrunnlagRegel) {
        String jsonInput = toJson(beregningsgrunnlagRegel);
        // Evaluerer hver BeregningsgrunnlagPeriode fra foreslått Beregningsgrunnlag
        List<RegelResultat> regelResultater = new ArrayList<>();
        FrisinnGrunnlag frisinnGrunnlag = input.getYtelsespesifiktGrunnlag();
        for (BeregningsgrunnlagPeriode periode : beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder()) {
            LocalDate fom = periode.getBeregningsgrunnlagPeriode().getFom();
            settSøktYtelseForStatus(beregningsgrunnlagRegel, AktivitetStatus.FL, frisinnGrunnlag.getSøkerYtelseForFrilans(fom));
            settSøktYtelseForStatus(beregningsgrunnlagRegel, AktivitetStatus.SN, frisinnGrunnlag.getSøkerYtelseForNæring(fom));
            RegelVurderBeregningsgrunnlagFRISINN regel = new RegelVurderBeregningsgrunnlagFRISINN(periode);
            Evaluation evaluation = regel.evaluer(periode);
            regelResultater.add(RegelmodellOversetter.getRegelResultat(evaluation, jsonInput));
        }
        return regelResultater;
    }

    @Override
    protected List<BeregningVilkårResultat> mapTilVilkårResultatListe(List<RegelResultat> regelResultater,
                                                                      BeregningsgrunnlagDto beregningsgrunnlag,
                                                                      YtelsespesifiktGrunnlag ytelsesSpesifiktGrunnlag) {
        FrisinnGrunnlag frisinnGrunnlag = (FrisinnGrunnlag) ytelsesSpesifiktGrunnlag;
        List<BeregningVilkårResultat> vilkårsResultatListe = new ArrayList<>();
        Iterator<RegelResultat> regelResultatIterator = regelResultater.iterator();
        for (var periode : beregningsgrunnlag.getBeregningsgrunnlagPerioder()) {
            BeregningVilkårResultat vilkårResultat = lagVilkårResultatForPeriode(regelResultatIterator.next(), periode.getPeriode());
            LocalDate fom = periode.getBeregningsgrunnlagPeriodeFom();
            if (frisinnGrunnlag.getSøkerYtelseForFrilans(fom) || frisinnGrunnlag.getSøkerYtelseForNæring(fom)) {
                vilkårsResultatListe.add(vilkårResultat);
            }
        }
        return vilkårsResultatListe;
    }

    private BeregningVilkårResultat lagVilkårResultatForPeriode(RegelResultat regelResultat, Intervall periode) {
        boolean erVilkårOppfylt = regelResultat.getMerknader().stream().map(RegelMerknad::utfallÅrsak)
                .noneMatch(AVSLAGSÅRSAKER::contains);
        return new BeregningVilkårResultat(erVilkårOppfylt, finnAvslagsårsak(regelResultat), periode);
    }

    private Vilkårsavslagsårsak finnAvslagsårsak(RegelResultat regelResultat) {
        boolean frilansUtenInntekt = regelResultat.getMerknader().stream().map(RegelMerknad::utfallÅrsak)
                .anyMatch(BeregningUtfallÅrsak.FRISINN_FRILANS_UTEN_INNTEKT::equals);
        if (frilansUtenInntekt) {
            return Vilkårsavslagsårsak.SØKT_FL_INGEN_FL_INNTEKT;
        }
        boolean harForLavtBG = regelResultat.getMerknader().stream().map(RegelMerknad::utfallÅrsak)
                .anyMatch(AVSLAGSÅRSAKER::contains);
        if (harForLavtBG) {
            return Vilkårsavslagsårsak.FOR_LAVT_BG;
        }
        return null;
    }


    public static void settSøktYtelseForStatus(Beregningsgrunnlag beregningsgrunnlagRegel, AktivitetStatus status, boolean erSøktYtelseFor) {
        beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder().forEach(bgPeriode -> {
            if (AktivitetStatus.FL.equals(status)) {
                getBGArbeidsforhold(bgPeriode).stream()
                        .filter(BeregningsgrunnlagPrArbeidsforhold::erFrilanser)
                        .findFirst()
                        .ifPresent(bgFrilans -> bgFrilans.setErSøktYtelseFor(erSøktYtelseFor));
            }
            if (AktivitetStatus.SN.equals(status)) {
                BeregningsgrunnlagPrStatus snAndel = bgPeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
                if (snAndel != null) {
                    snAndel.setErSøktYtelseFor(erSøktYtelseFor);
                }
            }
        });
    }

    private static List<BeregningsgrunnlagPrArbeidsforhold> getBGArbeidsforhold(BeregningsgrunnlagPeriode bgPeriode) {
        return Optional.ofNullable(bgPeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL))
                .map(BeregningsgrunnlagPrStatus::getArbeidsforhold)
                .orElse(Collections.emptyList());
    }


}
