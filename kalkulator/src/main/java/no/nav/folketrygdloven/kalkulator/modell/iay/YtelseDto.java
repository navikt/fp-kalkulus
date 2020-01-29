package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.behandling.Fagsystem;
import no.nav.folketrygdloven.kalkulator.modell.diff.IndexKey;
import no.nav.folketrygdloven.kalkulator.modell.ytelse.RelatertYtelseType;
import no.nav.folketrygdloven.kalkulator.modell.ytelse.TemaUnderkategori;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.RelatertYtelseTilstand;

public class YtelseDto {

    private YtelseGrunnlagDto ytelseGrunnlag;
    private RelatertYtelseType relatertYtelseType = RelatertYtelseType.UDEFINERT;
    private Intervall periode;
    private RelatertYtelseTilstand status;
    private Fagsystem kilde;
    private TemaUnderkategori temaUnderkategori = TemaUnderkategori.UDEFINERT;
    private Set<YtelseAnvistDto> ytelseAnvist = new LinkedHashSet<>();

    public YtelseDto() {
        // hibernate
    }

    public YtelseDto(YtelseDto ytelse) {
        this.relatertYtelseType = ytelse.getRelatertYtelseType();
        this.status = ytelse.getStatus();
        this.periode = ytelse.getPeriode();
        this.temaUnderkategori = ytelse.getBehandlingsTema();
        this.kilde = ytelse.getKilde();
        ytelse.getYtelseGrunnlag().ifPresent(yg -> {
            YtelseGrunnlagDto ygn = new YtelseGrunnlagDto(yg);
            this.ytelseGrunnlag = ygn;
        });
        this.ytelseAnvist = ytelse.getYtelseAnvist().stream().map(ya -> {
            YtelseAnvistDto ytelseAnvist = new YtelseAnvistDto(ya);
            return ytelseAnvist;
        }).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public String getIndexKey() {
        return IndexKey.createKey(periode, relatertYtelseType);
    }

    public RelatertYtelseType getRelatertYtelseType() {
        return relatertYtelseType;
    }

    void setRelatertYtelseType(RelatertYtelseType relatertYtelseType) {
        this.relatertYtelseType = relatertYtelseType;
    }

    public TemaUnderkategori getBehandlingsTema() {
        return temaUnderkategori;
    }

    void setBehandlingsTema(TemaUnderkategori behandlingsTema) {
        this.temaUnderkategori = behandlingsTema;
    }

    public RelatertYtelseTilstand getStatus() {
        return status;
    }

    void setStatus(RelatertYtelseTilstand status) {
        this.status = status;
    }

    public Intervall getPeriode() {
        return periode;
    }

    void setPeriode(Intervall periode) {
        this.periode = periode;
    }

    public Fagsystem getKilde() {
        return kilde;
    }

    void setKilde(Fagsystem kilde) {
        this.kilde = kilde;
    }

    public Optional<YtelseGrunnlagDto> getYtelseGrunnlag() {
        return Optional.ofNullable(ytelseGrunnlag);
    }

    void setYtelseGrunnlag(YtelseGrunnlagDto ytelseGrunnlag) {
        if (ytelseGrunnlag != null) {
            this.ytelseGrunnlag = ytelseGrunnlag;
        }
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
        if (o == null || !(o instanceof YtelseDto))
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
            ", relatertYtelseStatus=" + status + //$NON-NLS-1$
            '}';
    }

}
