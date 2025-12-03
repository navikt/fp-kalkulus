package no.nav.folketrygdloven.kalkulus.web.app.jackson;

import jakarta.validation.constraints.Pattern;

class Patternklasse {

    @Pattern(regexp = "[Aa]")
    private String fritekst;
}
