package no.nav.folketrygdloven.kalkulus;

import java.time.LocalDate;

import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.typer.AktørId;

public class BehandlingReferanseMock extends BehandlingReferanse {

    public static final AktørId AKTØR_ID = AktørId.dummy();
    private Skjæringstidspunkt skjæringstidspunkt;


    public BehandlingReferanseMock() {
        super();
    }

    public BehandlingReferanseMock(LocalDate skjæringstidspunktBeregning, LocalDate skjæringstidspunktOpptjening) {
        super();
        this.skjæringstidspunkt = Skjæringstidspunkt.builder()
                .medSkjæringstidspunktBeregning(skjæringstidspunktBeregning)
                .medSkjæringstidspunktOpptjening(skjæringstidspunktOpptjening)
                .medFørsteUttaksdato(skjæringstidspunktOpptjening.plusDays(1))
                .build();
    }

    public BehandlingReferanseMock(LocalDate skjæringstidspunkt) {
        super();
        this.skjæringstidspunkt = Skjæringstidspunkt.builder()
            .medSkjæringstidspunktBeregning(skjæringstidspunkt)
            .medSkjæringstidspunktOpptjening(skjæringstidspunkt)
            .medFørsteUttaksdato(skjæringstidspunkt.plusDays(1))
            .build();
    }

    @Override
    public FagsakYtelseType getFagsakYtelseType() {
        return FagsakYtelseType.FORELDREPENGER;
    }

    @Override
    public Skjæringstidspunkt getSkjæringstidspunkt() {
        return skjæringstidspunkt;
    }

    @Override
    public LocalDate getSkjæringstidspunktBeregning() {
        return skjæringstidspunkt.getSkjæringstidspunktBeregning();
    }

    @Override
    public LocalDate getSkjæringstidspunktOpptjening() {
        return skjæringstidspunkt.getSkjæringstidspunktOpptjening();
    }

    @Override
    public LocalDate getFørsteUttaksdato() {
        return skjæringstidspunkt.getFørsteUttaksdato();
    }

    @Override
    public AktørId getAktørId() {
        return AKTØR_ID;
    }

    @Override
    public Long getBehandlingId() {
        return 1L;
    }
}
