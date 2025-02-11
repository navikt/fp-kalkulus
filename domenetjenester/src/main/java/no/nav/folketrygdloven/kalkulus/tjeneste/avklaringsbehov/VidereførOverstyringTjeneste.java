package no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov;

import java.util.Arrays;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;

@ApplicationScoped
public class VidereførOverstyringTjeneste {

    private AvklaringsbehovTjeneste avklaringsbehovTjeneste;
    private KoblingTjeneste koblingTjeneste;

    VidereførOverstyringTjeneste() {
        // CDI
    }

    @Inject
    public VidereførOverstyringTjeneste(AvklaringsbehovTjeneste avklaringsbehovTjeneste, KoblingTjeneste koblingTjeneste) {
        this.avklaringsbehovTjeneste = avklaringsbehovTjeneste;
        this.koblingTjeneste = koblingTjeneste;
    }

    public Optional<AvklaringsbehovEntitet> videreførOverstyringForSteg(Long koblingId, BeregningSteg beregningSteg) {
        Optional<AvklaringsbehovDefinisjon> aktuellDefinisjonSomSkalVidereføres = finnAktuellOverstyring(beregningSteg);

        if (aktuellDefinisjonSomSkalVidereføres.isPresent()) {
            var definisjon = aktuellDefinisjonSomSkalVidereføres.get();
            var overstyringerFraGjeldendeKobling = avklaringsbehovTjeneste.hentAvklaringsbehov(koblingId, definisjon)
                .stream()
                .filter(ab -> !ab.getErTrukket())
                .findFirst();

            if (overstyringerFraGjeldendeKobling.isPresent()) {
                return overstyringerFraGjeldendeKobling;
            }

            return finnOverstyringFraOriginalKobling(koblingId, definisjon);
        }

        return Optional.empty();
    }

    private Optional<AvklaringsbehovDefinisjon> finnAktuellOverstyring(BeregningSteg beregningSteg) {
        return Arrays.stream(AvklaringsbehovDefinisjon.values())
            .filter(it -> it.erOverstyring() && it.getStegFunnet().equals(beregningSteg))
            .findFirst();
    }

    private Optional<AvklaringsbehovEntitet> finnOverstyringFraOriginalKobling(Long koblingId, AvklaringsbehovDefinisjon definisjon) {
        var originalKobling = koblingTjeneste.hentKobling(koblingId)
            .getOriginalKoblingReferanse()
            .flatMap(ref -> koblingTjeneste.hentKoblingOptional(ref));
        return originalKobling.flatMap(
            k -> avklaringsbehovTjeneste.hentAvklaringsbehov(k.getId(), definisjon).stream().filter(ab -> !ab.getErTrukket()).findFirst());
    }


}
