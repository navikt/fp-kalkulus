package no.nav.folketrygdloven.kalkulus.tjeneste.sporing;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingPeriode;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.sporing.RegelsporingRepository;

@ApplicationScoped
public class RegelSporingTjeneste {

    private RegelsporingRepository regelsporingRepository;

    RegelSporingTjeneste() {
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
        regelsporingRepository.lagreSporinger(regelSporingPerioder, koblingId);
    }

}
