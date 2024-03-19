package no.nav.folketrygdloven.kalkulus.tjeneste.sporing;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingPeriode;

@ApplicationScoped
public class RegelSporingTjeneste {

    private RegelsporingRepository regelsporingRepository;

    public RegelSporingTjeneste() {
    }

    @Inject
    public RegelSporingTjeneste(RegelsporingRepository regelsporingRepository) {
        this.regelsporingRepository = regelsporingRepository;
    }

    /**
     * Lagrer regelsporing for periode
     *
     * @param koblingId            kobling Id
     * @param regelSporingPerioder reglsporingperioder som skal lagres
     */
    public void lagre(Long koblingId, List<RegelSporingPeriode> regelSporingPerioder) {
        var gruppertPrHash = HashGrupperingUtil.grupperRegelsporingerMD5(regelSporingPerioder); // TODO tfp-5742 trenger vi gjøre dette?
        var regelInputPrHash = gruppertPrHash.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0).regelInput()));
        regelsporingRepository.lagreRegelInputKomprimert(regelInputPrHash);
        regelsporingRepository.lagreSporinger(gruppertPrHash, koblingId);
    }

}
