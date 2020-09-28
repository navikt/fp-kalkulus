package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.FaktaOmBeregningTilfelleRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere.TilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;


/**
 * Utleder hvilke tilfeller (FaktaOmBeregningTilfelle) som inntreffer i fakta om beregning.
 */
@ApplicationScoped
public class FaktaOmBeregningTilfelleTjeneste {
    private Instance<TilfelleUtleder> tilfelleUtledere;

    public FaktaOmBeregningTilfelleTjeneste() {
        // For CDI
    }

    @Inject
    public FaktaOmBeregningTilfelleTjeneste(@Any Instance<TilfelleUtleder> tilfelleUtledere) {
        this.tilfelleUtledere = tilfelleUtledere;
    }

    /**
     * Finner tilfeller i fakta om beregning som gir grunnlag for manuell behandling.
     *
     * @param input Beregningsgrunnlaginput
     * @param beregningsgrunnlagGrunnlag Beregningsgrunnlag
     * @return Liste med tilfeller
     */
    public List<FaktaOmBeregningTilfelle> finnTilfellerForFellesAksjonspunkt(BeregningsgrunnlagInput input,
                                                                             BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        List<FaktaOmBeregningTilfelle> tilfeller = new ArrayList<>();
        List<TilfelleUtleder> utledere = finnUtledere(input.getFagsakYtelseType());
        for (TilfelleUtleder utleder : utledere) {
            utleder.utled(input, beregningsgrunnlagGrunnlag).ifPresent(tilfeller::add);
        }
        return tilfeller;
    }


    /**
     * Finner utledere av fakta om beregning tilfeller for ytelse.
     *
     * @param ytelseType Ytelsetype for behandling
     * @return Liste med utledere
     */
    private List<TilfelleUtleder> finnUtledere(FagsakYtelseType ytelseType) {
        var spesifikkeTilfelleUtledereForYtelseType = finnYtelsesspesifikkeUtledere(ytelseType);
        List<TilfelleUtleder> utledere = spesifikkeTilfelleUtledereForYtelseType.stream().collect(Collectors.toList());
        leggTilDefaultOmDetIkkeFinnesSpesifikkPerTilfelle(spesifikkeTilfelleUtledereForYtelseType, utledere);
        return utledere;
    }

    private Instance<TilfelleUtleder> finnYtelsesspesifikkeUtledere(FagsakYtelseType ytelseType) {
        return this.tilfelleUtledere.select(new FagsakYtelseTypeRef.FagsakYtelseTypeRefLiteral(ytelseType.getKode()));
    }

    private void leggTilDefaultOmDetIkkeFinnesSpesifikkPerTilfelle(Instance<TilfelleUtleder> spesifikkeYtelseInstanser, List<TilfelleUtleder> utledere) {
        var defaultInstanser = finnDefaultInstanserAvUtledere();
        defaultInstanser.stream().forEach(i -> {
            if (harIkkeYtelsesspesifikkImplementasjon(spesifikkeYtelseInstanser, i)) {
                utledere.add(i);
            }
        });
    }

    private Instance<TilfelleUtleder> finnDefaultInstanserAvUtledere() {
        return this.tilfelleUtledere.select(new FagsakYtelseTypeRef.FagsakYtelseTypeRefLiteral("*"));
    }

    private boolean harIkkeYtelsesspesifikkImplementasjon(Instance<TilfelleUtleder> spesifikkeYtelseInstanser, TilfelleUtleder defaultInstans) {
        String faktaOmBeregningTilfelleKode = getFaktaOmBeregningAnnotation(defaultInstans).map(FaktaOmBeregningTilfelleRef::value).orElse(null);
        return faktaOmBeregningTilfelleKode == null || finnesIngenSpesifikkImplementasjonForTilfelle(spesifikkeYtelseInstanser, faktaOmBeregningTilfelleKode);
    }

    private boolean finnesIngenSpesifikkImplementasjonForTilfelle(Instance<TilfelleUtleder> spesifikkeYtelseInstanser, String faktaOmBeregningTilfelleKode) {
        return spesifikkeYtelseInstanser.stream().noneMatch(utlederInstans -> getFaktaOmBeregningAnnotation(utlederInstans).map(FaktaOmBeregningTilfelleRef::value).orElse("").equals(faktaOmBeregningTilfelleKode));
    }

    private Optional<FaktaOmBeregningTilfelleRef> getFaktaOmBeregningAnnotation(TilfelleUtleder utleder) {
        return Optional.ofNullable(utleder.getClass().getAnnotation(FaktaOmBeregningTilfelleRef.class));
    }


}
