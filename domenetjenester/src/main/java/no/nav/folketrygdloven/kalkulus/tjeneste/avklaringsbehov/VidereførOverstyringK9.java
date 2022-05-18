package no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov;

import java.util.Arrays;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;

@ApplicationScoped
@FagsakYtelseTypeRef("PSB")
@FagsakYtelseTypeRef("PPN")
@FagsakYtelseTypeRef("OMP")
@FagsakYtelseTypeRef("FRISINN")
public class VidereførOverstyringK9 implements VidereførOverstyring {

    private AvklaringsbehovTjeneste avklaringsbehovTjeneste;

    public VidereførOverstyringK9() {
    }

    @Inject
    public VidereførOverstyringK9(AvklaringsbehovTjeneste avklaringsbehovTjeneste) {
        this.avklaringsbehovTjeneste = avklaringsbehovTjeneste;
    }

    /** K9 har gått bort fra å videreføre overstyring av beregningsaktiviteter.
     *
     * @param koblingId Kobling Id
     * @param beregningSteg Steg
     * @return Overstyring avklaringsbehov
     */
    @Override
    public Optional<AvklaringsbehovEntitet> videreførOverstyringForSteg(Long koblingId, BeregningSteg beregningSteg) {
        return Arrays.stream(AvklaringsbehovDefinisjon.values()).filter(it -> it.erOverstyring() && it.getStegFunnet().equals(beregningSteg))
                .filter(it -> !it.equals(AvklaringsbehovDefinisjon.OVERSTYRING_AV_BEREGNINGSAKTIVITETER))
                .findFirst()
                .flatMap(it -> avklaringsbehovTjeneste.hentAvklaringsbehov(koblingId, it));
    }

}
