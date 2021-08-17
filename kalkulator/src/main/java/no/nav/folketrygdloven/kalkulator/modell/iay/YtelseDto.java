package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.TemaUnderkategori;

public class YtelseDto {

    private Beløp vedtaksDagsats;
    private FagsakYtelseType relatertYtelseType = FagsakYtelseType.UDEFINERT;
    private Intervall periode;
    // Brukes til å skille ulike ytelser med samme ytelsetype
    private TemaUnderkategori temaUnderkategori = TemaUnderkategori.UDEFINERT;
    private Set<YtelseAnvistDto> ytelseAnvist = new LinkedHashSet<>();

    public YtelseDto() {
        // hibernate
    }

    public YtelseDto(YtelseDto ytelse) {
        this.relatertYtelseType = ytelse.getRelatertYtelseType();
        this.periode = ytelse.getPeriode();
        this.temaUnderkategori = ytelse.getBehandlingsTema();
        this.ytelseAnvist = ytelse.getYtelseAnvist().stream().map(YtelseAnvistDto::new).collect(Collectors.toCollection(LinkedHashSet::new));
        ytelse.getVedtaksDagsats().ifPresent(dagsats -> this.vedtaksDagsats = new Beløp(dagsats.getVerdi()));
    }

    public Optional<Beløp> getVedtaksDagsats() {
        return Optional.ofNullable(vedtaksDagsats);
    }

    public void setVedtaksDagsats(Beløp vedtaksDagsats) {
        this.vedtaksDagsats = vedtaksDagsats;
    }

    public FagsakYtelseType getRelatertYtelseType() {
        return relatertYtelseType;
    }

    void setRelatertYtelseType(FagsakYtelseType relatertYtelseType) {
        this.relatertYtelseType = relatertYtelseType;
    }

    public TemaUnderkategori getBehandlingsTema() {
        return temaUnderkategori;
    }

    void setBehandlingsTema(TemaUnderkategori behandlingsTema) {
        this.temaUnderkategori = behandlingsTema;
    }

    public Intervall getPeriode() {
        return periode;
    }

    void setPeriode(Intervall periode) {
        this.periode = periode;
    }

    public Collection<YtelseAnvistDto> getYtelseAnvist() {
        return Collections.unmodifiableCollection(ytelseAnvist);
    }

    void leggTilYtelseAnvist(YtelseAnvistDto ytelseAnvist) {
        this.ytelseAnvist.add(ytelseAnvist);

    }

    void tilbakestillAnvisteYtelser() {
        ytelseAnvist.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof YtelseDto))
            return false;
        YtelseDto that = (YtelseDto) o;
        return Objects.equals(relatertYtelseType, that.relatertYtelseType) &&
                Objects.equals(temaUnderkategori, that.temaUnderkategori) &&
                Objects.equals(periode, that.periode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(relatertYtelseType, periode);
    }

    @Override
    public String toString() {
        return "YtelseEntitet{" + //$NON-NLS-1$
                "relatertYtelseType=" + relatertYtelseType + //$NON-NLS-1$
                ", typeUnderkategori=" + temaUnderkategori + //$NON-NLS-1$
                ", periode=" + periode + //$NON-NLS-1$
                '}';
    }

}
