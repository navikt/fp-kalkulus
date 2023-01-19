package no.nav.folketrygdloven.kalkulus.tjeneste.sporing;

import static ch.qos.logback.core.encoder.ByteArrayUtil.toHexString;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingPeriode;

@ApplicationScoped
public class RegelSporingTjeneste {

    private RegelsporingRepository regelsporingRepository;
    private MessageDigest hashInstance;

    public RegelSporingTjeneste() {
    }

    @Inject
    public RegelSporingTjeneste(RegelsporingRepository regelsporingRepository) {
        this.regelsporingRepository = regelsporingRepository;
        try {
            this.hashInstance = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }


    /**
     * Lagrer regelsporing for periode
     *
     * @param koblingId            kobling Id
     * @param regelSporingPerioder reglsporingperioder som skal lagres
     */
    public void lagre(Long koblingId, List<RegelSporingPeriode> regelSporingPerioder) {
        var gruppertPrHash = regelSporingPerioder.stream().collect(Collectors.groupingByConcurrent(p -> lagMD5Hash(p.getRegelInput())));
        var regelInputPrHash = gruppertPrHash.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0).getRegelInput()));
        regelsporingRepository.lagreRegelInputKomprimert(regelInputPrHash);
        regelsporingRepository.lagreSporinger(gruppertPrHash, koblingId);
    }

    private String lagMD5Hash(String regelInput) {
        byte[] md5Hash = hashInstance.digest(regelInput.getBytes());
        return toHexString(md5Hash);
    }


}
