package no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov;

import java.util.Arrays;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

@ApplicationScoped
@FagsakYtelseTypeRef(FagsakYtelseType.FORELDREPENGER)
@FagsakYtelseTypeRef(FagsakYtelseType.SVANGERSKAPSPENGER)
public class VidereførOverstyringK14 implements VidereførOverstyring {

    private AvklaringsbehovTjeneste avklaringsbehovTjeneste;

    public VidereførOverstyringK14() {
    }

    @Inject
    public VidereførOverstyringK14(AvklaringsbehovTjeneste avklaringsbehovTjeneste) {
        this.avklaringsbehovTjeneste = avklaringsbehovTjeneste;
    }

    @Override
    public Optional<AvklaringsbehovEntitet> videreførOverstyringForSteg(Long koblingId, BeregningSteg beregningSteg) {
        return Arrays.stream(AvklaringsbehovDefinisjon.values()).filter(it -> it.erOverstyring() && it.getStegFunnet().equals(beregningSteg))
                .findFirst()
                .flatMap(it -> avklaringsbehovTjeneste.hentAvklaringsbehov(koblingId, it));
    }

}
