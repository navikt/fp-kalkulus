package no.nav.folketrygdloven.kalkulator.steg.refusjon.modell;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class RefusjonPeriodeEndring {
    private List<RefusjonAndel> originaleAndeler;
    private List<RefusjonAndel> revurderingAndeler;

    public RefusjonPeriodeEndring(List<RefusjonAndel> originaleAndeler, List<RefusjonAndel> revurderingAndeler) {
        Objects.requireNonNull(originaleAndeler, "originaleAndeler");
        Objects.requireNonNull(revurderingAndeler, "revurderingAndeler");
        this.originaleAndeler = originaleAndeler;
        this.revurderingAndeler = revurderingAndeler;
    }

    public List<RefusjonAndel> getOriginaleAndeler() {
        return originaleAndeler;
    }

    public List<RefusjonAndel> getRevurderingAndeler() {
        return revurderingAndeler;
    }

    public BigDecimal getOriginalBrutto() {
        return getBrutto(originaleAndeler);
    }

    public BigDecimal getRevurderingBrutto() {
        return getBrutto(revurderingAndeler);
    }

    public BigDecimal getOriginalRefusjon() {
        return getRefusjon(originaleAndeler);
    }

    public BigDecimal getRevurderingRefusjon() {
        return getRefusjon(revurderingAndeler);
    }

    private BigDecimal getRefusjon(List<RefusjonAndel> andeler) {
        return andeler.stream()
                .filter(andel -> andel.getRefusjon() != null)
                .map(RefusjonAndel::getRefusjon)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal getBrutto(List<RefusjonAndel> andeler) {
        return andeler.stream()
                .filter(andel -> andel.getBrutto() != null)
                .map(RefusjonAndel::getBrutto)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }
}
