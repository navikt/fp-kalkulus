package no.nav.folketrygdloven.kalkulator.felles.frist;

import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;
import no.nav.folketrygdloven.kalkulus.typer.Beløp;

public record KravOgUtfall(Beløp refusjonskrav, Utfall utfall) {}
