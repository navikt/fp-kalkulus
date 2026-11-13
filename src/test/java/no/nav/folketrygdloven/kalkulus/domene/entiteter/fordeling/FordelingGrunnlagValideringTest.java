package no.nav.folketrygdloven.kalkulus.domene.entiteter.fordeling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.AktørId;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.InternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonKilde;

class FordelingGrunnlagValideringTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void skal_avvise_duplikater_uavhengig_av_journalpost_og_type_arbeidsgiver(boolean personligArbeidsgiver) {
        var arbeidsgiver = personligArbeidsgiver
            ? Arbeidsgiver.person(new AktørId("9999999999999")) : Arbeidsgiver.virksomhet("999999999");
        var grunnlag = new FordelingGrunnlagEntitet(42L);
        grunnlag.leggTilRefusjonsandel(new RefusjonsandelEntitet(arbeidsgiver, null, RefusjonKilde.INNTEKTSMELDING, "123"));

        assertThatThrownBy(() -> grunnlag.leggTilRefusjonsandel(new RefusjonsandelEntitet(
            Arbeidsgiver.fra(arbeidsgiver), InternArbeidsforholdRef.nullRef(), RefusjonKilde.INNTEKTSMELDING, "456")))
            .isInstanceOf(IllegalArgumentException.class);
        assertThat(grunnlag.getRefusjonsandeler()).hasSize(1);
    }

    @Test
    void skal_avvise_duplikater_gjennom_builder_med_spesifikk_referanse() {
        var arbeidsgiver = Arbeidsgiver.virksomhet("999999999");
        var ref = UUID.randomUUID();
        var builder = FordelingGrunnlagEntitet.builder().leggTilRefusjonsandel(
            new RefusjonsandelEntitet(arbeidsgiver, InternArbeidsforholdRef.ref(ref), RefusjonKilde.INNTEKTSMELDING, "123"));

        assertThatThrownBy(() -> builder.leggTilRefusjonsandel(new RefusjonsandelEntitet(
            arbeidsgiver, InternArbeidsforholdRef.ref(ref), RefusjonKilde.INNTEKTSMELDING, "456")))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void skal_tillate_ulike_arbeidsforhold_kilder_og_arbeidsgivere() {
        var arbeidsgiver = Arbeidsgiver.virksomhet("999999999");
        var ref = InternArbeidsforholdRef.nyRef();
        var grunnlag = FordelingGrunnlagEntitet.builder()
            .leggTilRefusjonsandel(new RefusjonsandelEntitet(arbeidsgiver, ref, RefusjonKilde.INNTEKTSMELDING, "123"))
            .leggTilRefusjonsandel(new RefusjonsandelEntitet(arbeidsgiver, InternArbeidsforholdRef.nyRef(), RefusjonKilde.INNTEKTSMELDING, "456"))
            .leggTilRefusjonsandel(new RefusjonsandelEntitet(arbeidsgiver, ref, RefusjonKilde.SAKSBEHANDLER, null))
            .leggTilRefusjonsandel(new RefusjonsandelEntitet(Arbeidsgiver.virksomhet("999999998"), ref, RefusjonKilde.INNTEKTSMELDING, "789"))
            .build(42L);

        assertThat(grunnlag.getRefusjonsandeler()).hasSize(4);
    }
}
