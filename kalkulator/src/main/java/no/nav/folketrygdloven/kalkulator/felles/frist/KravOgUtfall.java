package no.nav.folketrygdloven.kalkulator.felles.frist;

import java.math.BigDecimal;

import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;

public record KravOgUtfall(BigDecimal refusjonskrav, Utfall utfall) {}
