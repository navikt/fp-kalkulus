package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import static no.nav.folketrygdloven.kalkulus.felles.tid.AbstractIntervall.TIDENES_ENDE;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.folketrygdloven.kalkulator.AksjonspunktUtlederFaktaOmBeregning;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.kontrollerfakta.FaktaOmBeregningTilfelleTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningVenteårsak;
import no.nav.folketrygdloven.kalkulator.output.FaktaOmBeregningAksjonspunktResultat;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.BeregningAksjonspunktDefinisjon;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;

@ApplicationScoped
@FagsakYtelseTypeRef("FRISINN")
public class AksjonspunktUtlederFaktaOmBeregningFRISINN extends AksjonspunktUtlederFaktaOmBeregning {
    private static final Logger LOG = LoggerFactory.getLogger(AksjonspunktUtlederFaktaOmBeregningFRISINN.class);

    public AksjonspunktUtlederFaktaOmBeregningFRISINN() {
        // CDI
    }

    @Inject
    public AksjonspunktUtlederFaktaOmBeregningFRISINN(FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste) {
        super(faktaOmBeregningTilfelleTjeneste);
    }

    @Override
    public FaktaOmBeregningAksjonspunktResultat utledAksjonspunkterFor(BeregningsgrunnlagInput input,
                                                                       BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag,
                                                                       boolean erOverstyrt) {
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagGrunnlag.getBeregningsgrunnlag().orElse(null);
        Objects.requireNonNull(beregningsgrunnlag, "beregningsgrunnlag");
        List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller = faktaOmBeregningTilfelleTjeneste.finnTilfellerForFellesAksjonspunkt(input, beregningsgrunnlagGrunnlag);
        if (faktaOmBeregningTilfeller.contains(FaktaOmBeregningTilfelle.VURDER_AT_OG_FL_I_SAMME_ORGANISASJON)) {
            LOG.info("Kan ikke behandle FRISINN-ytelse for AT og FL i samme organisajon. Setter på vent. BehandlingID: " + input.getBehandlingReferanse().getBehandlingId());
            BeregningAksjonspunktResultat ventepunkt = BeregningAksjonspunktResultat.opprettMedFristFor(BeregningAksjonspunktDefinisjon.FRISINN_ATFL_SAMME_ORG,
                    BeregningVenteårsak.FRISINN_ATFL_SAMME_ORG,
                    LocalDateTime.of(TIDENES_ENDE, LocalTime.MIDNIGHT));
            return new FaktaOmBeregningAksjonspunktResultat(Collections.singletonList(ventepunkt), Collections.emptyList());
        }
        return FaktaOmBeregningAksjonspunktResultat.INGEN_AKSJONSPUNKTER;
    }
}
