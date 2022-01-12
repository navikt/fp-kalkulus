package no.nav.folketrygdloven.kalkulus.app.jackson;

import jakarta.validation.constraints.Pattern;

class Patternklasse {

    @Pattern(regexp = "[Aa]")
    private String fritekst;
}
