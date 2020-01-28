package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class RefusjonskravDatoerAggregatDto {

    private List<RefusjonskravDatoDto> refusjonskravDatoer = new ArrayList<>();


    public RefusjonskravDatoerAggregatDto() {
    }

    RefusjonskravDatoerAggregatDto(RefusjonskravDatoerAggregatDto refusjonskravDatoerAggregat) {
        this(refusjonskravDatoerAggregat.getRefusjonskravDatoer());
    }

    public RefusjonskravDatoerAggregatDto(Collection<RefusjonskravDatoDto> refusjonskravDatoer) {
        this.refusjonskravDatoer.addAll(refusjonskravDatoer.stream().map(rd -> {
            final RefusjonskravDatoDto refusjonskravDato = new RefusjonskravDatoDto(rd);
            return refusjonskravDato;
        }).collect(Collectors.toList()));
    }

    public List<RefusjonskravDatoDto> getRefusjonskravDatoer() {
        return refusjonskravDatoer;
    }
}
