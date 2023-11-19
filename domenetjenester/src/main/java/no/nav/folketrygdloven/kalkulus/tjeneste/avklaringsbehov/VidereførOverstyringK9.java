package no.nav.folketrygdloven.kalkulus.tjeneste.avklaringsbehov;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.kalkulus.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.avklaringsbehov.AvklaringsbehovEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingRelasjon;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

@ApplicationScoped
@FagsakYtelseTypeRef(FagsakYtelseType.PLEIEPENGER_SYKT_BARN)
@FagsakYtelseTypeRef(FagsakYtelseType.OPPLÆRINGSPENGER)
@FagsakYtelseTypeRef(FagsakYtelseType.PLEIEPENGER_NÆRSTÅENDE)
@FagsakYtelseTypeRef(FagsakYtelseType.OMSORGSPENGER)
@FagsakYtelseTypeRef(FagsakYtelseType.FRISINN)
public class VidereførOverstyringK9 implements VidereførOverstyring {

    private AvklaringsbehovTjeneste avklaringsbehovTjeneste;
    private KoblingTjeneste koblingTjeneste;

    public VidereførOverstyringK9() {
    }

    @Inject
    public VidereførOverstyringK9(AvklaringsbehovTjeneste avklaringsbehovTjeneste, KoblingTjeneste koblingTjeneste) {
        this.avklaringsbehovTjeneste = avklaringsbehovTjeneste;
        this.koblingTjeneste = koblingTjeneste;
    }

    /**
     * K9 har gått bort fra å videreføre overstyring av beregningsaktiviteter.
     *
     * @param koblingId     Kobling Id
     * @param beregningSteg Steg
     * @return Overstyring avklaringsbehov
     */
    @Override
    public Optional<AvklaringsbehovEntitet> videreførOverstyringForSteg(Long koblingId, BeregningSteg beregningSteg) {
        Optional<AvklaringsbehovDefinisjon> aktuellDefinisjonSomSkalVidereføres = finnAktuellOverstyring(beregningSteg);

        if (aktuellDefinisjonSomSkalVidereføres.isPresent()) {
            var definisjon = aktuellDefinisjonSomSkalVidereføres.get();
            var overstyringerFraGjeldendeKobling = avklaringsbehovTjeneste.hentAvklaringsbehov(koblingId, definisjon).stream()
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
                .filter(it -> !it.equals(AvklaringsbehovDefinisjon.OVST_BEREGNINGSAKTIVITETER))
                .findFirst();
    }

    private Optional<AvklaringsbehovEntitet> finnOverstyringFraOriginalKobling(Long koblingId, AvklaringsbehovDefinisjon definisjon) {
        var originalKoblinger = koblingTjeneste.hentKoblingRelasjoner(List.of(koblingId))
                .stream().map(KoblingRelasjon::getOriginalKoblingId).collect(Collectors.toList());

        return originalKoblinger.stream().flatMap(it -> avklaringsbehovTjeneste.hentAvklaringsbehov(it, definisjon).stream())
                .filter(ab -> !ab.getErTrukket())
                        .findFirst();
    }

}
