package no.nav.folketrygdloven.kalkulus.domene.mapTilEntitet;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import no.nav.folketrygdloven.kalkulator.modell.refusjon.FordelingGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.RefusjonsandelDto;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.RefusjonsperiodeDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.JournalpostId;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfall;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfallÅrsak;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

class FordelingsgrunnlagMapperTest {

    @ParameterizedTest
    @EnumSource(RefusjonUtfall.class)
    void skal_mappe_alle_felter_fra_inntektsmelding(RefusjonUtfall utfall) {
        var ref = InternArbeidsforholdRefDto.nyRef();
        var fom = LocalDate.of(2026, 1, 1);
        var tom = LocalDate.of(2026, 1, 31);
        var beløp = new BigDecimal("12345.67");
        var periode = new RefusjonsperiodeDto(Intervall.fraOgMedTilOgMed(fom, tom), Beløp.fra(beløp), utfall,
            RefusjonUtfallÅrsak.IKKE_INNENFOR_FRIST, "Vurdering av refusjonskravet.");
        var andel = new RefusjonsandelDto(Arbeidsgiver.virksomhet("999999999"), ref, RefusjonKilde.INNTEKTSMELDING,
            new JournalpostId("123456789"), List.of(periode));

        var entitet = FordelingsgrunnlagMapper.map(new FordelingGrunnlagDto(List.of(andel))).build(42L);

        assertThat(entitet.getKoblingId()).isEqualTo(42L);
        assertThat(entitet.getRefusjonsandeler()).hasSize(1);
        var mappetAndel = entitet.getRefusjonsandeler().getFirst();
        assertThat(mappetAndel.getArbeidsgiver().getOrgnr()).isEqualTo("999999999");
        assertThat(mappetAndel.getArbeidsforholdRef().getReferanse()).isEqualTo(ref.getReferanse());
        assertThat(mappetAndel.getKilde()).isEqualTo(RefusjonKilde.INNTEKTSMELDING);
        assertThat(mappetAndel.getJournalpostId()).contains("123456789");
        assertThat(mappetAndel.getRefusjonsperioder()).hasSize(1);
        var mappetPeriode = mappetAndel.getRefusjonsperioder().getFirst();
        assertThat(mappetPeriode.getPeriode().getFomDato()).isEqualTo(fom);
        assertThat(mappetPeriode.getPeriode().getTomDato()).isEqualTo(tom);
        assertThat(mappetPeriode.getRefusjonPrMnd().getVerdi()).isEqualByComparingTo(beløp);
        assertThat(mappetPeriode.getUtfall()).isEqualTo(utfall);
        assertThat(mappetPeriode.getUtfallÅrsak()).isEqualTo(RefusjonUtfallÅrsak.IKKE_INNENFOR_FRIST);
        assertThat(mappetPeriode.getUtfallBegrunnelse()).isEqualTo("Vurdering av refusjonskravet.");
    }

    @Test
    void skal_mappe_personlig_arbeidsgiver_og_valgfrie_felter() {
        var periode = new RefusjonsperiodeDto(Intervall.fraOgMed(LocalDate.of(2026, 1, 1)), Beløp.ZERO,
            RefusjonUtfall.INNVILGET, null, null);
        var andel = new RefusjonsandelDto(Arbeidsgiver.person(new AktørId("9999999999999")), null,
            RefusjonKilde.SAKSBEHANDLER, null, List.of(periode));

        var entitet = FordelingsgrunnlagMapper.map(new FordelingGrunnlagDto(List.of(andel))).build(42L);

        var mappetAndel = entitet.getRefusjonsandeler().getFirst();
        assertThat(mappetAndel.getArbeidsgiver().getAktørId().getId()).isEqualTo("9999999999999");
        assertThat(mappetAndel.getArbeidsgiver().getOrgnr()).isNull();
        assertThat(mappetAndel.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold()).isFalse();
        assertThat(mappetAndel.getKilde()).isEqualTo(RefusjonKilde.SAKSBEHANDLER);
        assertThat(mappetAndel.getJournalpostId()).isEmpty();
        var mappetPeriode = mappetAndel.getRefusjonsperioder().getFirst();
        assertThat(mappetPeriode.getPeriode().getTomDato()).isEqualTo(periode.periode().getTomDato());
        assertThat(mappetPeriode.getRefusjonPrMnd().getVerdi()).isZero();
        assertThat(mappetPeriode.getUtfallÅrsak()).isNull();
        assertThat(mappetPeriode.getUtfallBegrunnelse()).isNull();
    }

    @Test
    void skal_mappe_tomt_fordelingsgrunnlag() {
        var entitet = FordelingsgrunnlagMapper.map(new FordelingGrunnlagDto(List.of())).build(42L);

        assertThat(entitet.getRefusjonsandeler()).isEmpty();
    }
}
