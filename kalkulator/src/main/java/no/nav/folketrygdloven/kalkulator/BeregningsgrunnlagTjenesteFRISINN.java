package no.nav.folketrygdloven.kalkulator;

import static no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningsgrunnlagTilstand.OPPRETTET;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.FrisinnGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.output.BeregningResultatAggregat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.refusjon.BeregningRefusjonAksjonspunktutleder;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningAksjonspunktDefinisjon;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.Vilkårsavslagsårsak;

/**
 * Fasade tjeneste for å delegere alle kall fra steg
 */
@ApplicationScoped
@FagsakYtelseTypeRef("FRISINN")
public class BeregningsgrunnlagTjenesteFRISINN extends BeregningsgrunnlagTjeneste {

    public BeregningsgrunnlagTjenesteFRISINN() {
        // CDI Proxy
    }

    @Inject
    public BeregningsgrunnlagTjenesteFRISINN(Instance<FullføreBeregningsgrunnlag> fullføreBeregningsgrunnlag, Instance<AksjonspunktUtlederFaktaOmBeregning> aksjonspunktUtledereFaktaOmBeregning, Instance<AksjonspunktUtlederFastsettBeregningsaktiviteter> apUtlederFastsettAktiviteter, OpprettBeregningsgrunnlagTjeneste opprettBeregningsgrunnlagTjeneste, FordelBeregningsgrunnlagTjeneste fordelBeregningsgrunnlagTjeneste, BeregningRefusjonAksjonspunktutleder beregningRefusjonAksjonspunktutleder, Instance<ForeslåBeregningsgrunnlag> foreslåBeregningsgrunnlag, Instance<VurderBeregningsgrunnlagTjeneste> vurderBeregningsgrunnlagTjeneste, FastsettBeregningAktiviteter fastsettBeregningAktiviteter) {
        super(fullføreBeregningsgrunnlag, aksjonspunktUtledereFaktaOmBeregning, apUtlederFastsettAktiviteter, opprettBeregningsgrunnlagTjeneste, fordelBeregningsgrunnlagTjeneste, beregningRefusjonAksjonspunktutleder, foreslåBeregningsgrunnlag, vurderBeregningsgrunnlagTjeneste, fastsettBeregningAktiviteter);
    }

    @Override
    public BeregningResultatAggregat fastsettBeregningsaktiviteter(BeregningsgrunnlagInput input) {
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = fastsettBeregningAktiviteter.fastsettAktiviteter(input);
        BeregningsgrunnlagRegelResultat beregningsgrunnlagRegelResultat = opprettBeregningsgrunnlagTjeneste.fastsettSkjæringstidspunktOgStatuser(input, beregningAktivitetAggregat, input.getIayGrunnlag());
        Optional<BeregningAktivitetOverstyringerDto> overstyrt = hentTidligereOverstyringer(input);
        BeregningsgrunnlagGrunnlagDtoBuilder builder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(beregningAktivitetAggregat)
                .medOverstyring(overstyrt.orElse(null));
        builder.medBeregningsgrunnlag(beregningsgrunnlagRegelResultat.getBeregningsgrunnlag());

        var beregningsgrunnlagGrunnlag = builder.build(OPPRETTET);
        boolean erOverstyrt = overstyrt.isPresent();
        BeregningsgrunnlagInput inputOppdatertMedBg = input.medBeregningsgrunnlagGrunnlag(beregningsgrunnlagGrunnlag);
        var aksjonspunkter = new AksjonspunktUtlederFastsettBeregningsaktiviteterFRISINN().utledAksjonspunkter(
                beregningsgrunnlagRegelResultat,
                beregningAktivitetAggregat,
                inputOppdatertMedBg,
                erOverstyrt,
                input.getFagsakYtelseType());
        BeregningResultatAggregat.Builder resultatBuilder = BeregningResultatAggregat.Builder.fra(inputOppdatertMedBg)
                .medAksjonspunkter(aksjonspunkter);

        FrisinnGrunnlag frisinnGrunnlag = input.getYtelsespesifiktGrunnlag();
        if (beregningsgrunnlagRegelResultat.getAksjonspunkter().stream().anyMatch(bra -> bra.getBeregningAksjonspunktDefinisjon() == BeregningAksjonspunktDefinisjon.INGEN_AKTIVITETER)) {
            if (frisinnGrunnlag.getSøkerYtelseForFrilans() && !frisinnGrunnlag.getSøkerYtelseForNæring()) {
                resultatBuilder.medVilkårAvslått(Vilkårsavslagsårsak.SØKT_FL_INGEN_FL_INNTEKT);
            } else {
                resultatBuilder.medVilkårAvslått(Vilkårsavslagsårsak.FOR_LAVT_BG);
            }
        } else {
            if (frisinnGrunnlag.getSøkerYtelseForFrilans() && !frisinnGrunnlag.getSøkerYtelseForNæring()) {
                if (beregningsgrunnlagRegelResultat.getBeregningsgrunnlag() != null) {
                    if (beregningsgrunnlagRegelResultat.getBeregningsgrunnlag().getAktivitetStatuser().stream().noneMatch(as -> as.getAktivitetStatus().erFrilanser())) {
                        resultatBuilder.medVilkårAvslått(Vilkårsavslagsårsak.SØKT_FL_INGEN_FL_INNTEKT);
                    }
                }
            }
        }
        resultatBuilder.medBeregningsgrunnlag(beregningsgrunnlagRegelResultat.getBeregningsgrunnlag(), OPPRETTET);
        return resultatBuilder.build();
    }
}
