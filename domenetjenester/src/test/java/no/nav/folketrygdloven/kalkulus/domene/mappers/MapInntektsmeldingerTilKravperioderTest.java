package no.nav.folketrygdloven.kalkulus.domene.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.iay.KravperioderPrArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.PerioderForKravDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Organisasjon;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.iay.IayProsent;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.ArbeidDto;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.ArbeidsforholdInformasjonDto;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.ArbeidsforholdOverstyringDto;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.InntektsmeldingerDto;
import no.nav.folketrygdloven.kalkulus.iay.v1.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidsforholdHandlingType;
import no.nav.vedtak.konfig.Tid;

class MapInntektsmeldingerTilKravperioderTest {

    private static final LocalDate STP = LocalDate.of(2021,12,1);

    @Test
    void inntektsmelding_med_et_arbeidsforhold_mappes_korrekt() {
        var ag = new Organisasjon("99999999");
        var ref = new InternArbeidsforholdRefDto(UUID.randomUUID().toString());
        var ya = lagRegisterArbeid(ag, ref, førStp(500), etterSTP(500));
        var im = lagIM(ag, ref, 500_000, 500_000, STP);

        var gr = byggGr(List.of(im), List.of(im), List.of(ya));

        var resultat = MapInntektsmeldingerTilKravperioder.map(gr, STP);

        assertThat(resultat).hasSize(1);
        assertKrav(resultat, ag, ref, 500_000, STP, Tid.TIDENES_ENDE);
    }


    @Test
    void inntektsmelding_med_to_arbeidsforhold_ulike_bedrifter_mappes_korrekt() {
        var ag = new Organisasjon("99999999");
        var ag2 = new Organisasjon("99999998");
        var ref = new InternArbeidsforholdRefDto(UUID.randomUUID().toString());
        var ref2 = new InternArbeidsforholdRefDto(UUID.randomUUID().toString());
        var ya1 = lagRegisterArbeid(ag, ref, førStp(500), etterSTP(500));
        var ya2 = lagRegisterArbeid(ag2, ref2, førStp(200), etterSTP(100));
        var im1 = lagIM(ag, ref, 500_000, 500_000, STP);
        var im2 = lagIM(ag2, ref2, 300_000, 300_000, STP);

        var gr = byggGr(List.of(im1, im2), List.of(im1, im2), List.of(ya1, ya2));

        var resultat = MapInntektsmeldingerTilKravperioder.map(gr, STP);

        assertThat(resultat).hasSize(2);
        assertKrav(resultat, ag, ref, 500_000, STP, Tid.TIDENES_ENDE);
        assertKrav(resultat, ag2, ref2, 300_000, STP, Tid.TIDENES_ENDE);
    }

    @Test
    void inntektsmelding_med_to_arbeidsforhold_i_samme_bedrift_med_id() {
        var ag = new Organisasjon("99999999");
        var ref = new InternArbeidsforholdRefDto(UUID.randomUUID().toString());
        var ref2 = new InternArbeidsforholdRefDto(UUID.randomUUID().toString());

        var ya1 = lagRegisterArbeid(ag, ref, førStp(500), etterSTP(500));
        var ya2 = lagRegisterArbeid(ag, ref2, førStp(200), etterSTP(100));
        var im1 = lagIM(ag, null, 500_000, 500_000, STP);

        var gr = byggGr(List.of(im1), List.of(im1), List.of(ya1, ya2));

        var resultat = MapInntektsmeldingerTilKravperioder.map(gr, STP);

        assertThat(resultat).hasSize(1);
        assertKrav(resultat, ag, null, 500_000, STP, Tid.TIDENES_ENDE);
    }

    @Test
    void inntektsmelding_med_to_arbeidsforhold_i_samme_bedrift_en_uten_id() {
        var ag = new Organisasjon("99999999");
        var ref = new InternArbeidsforholdRefDto(UUID.randomUUID().toString());
        var ya1 = lagRegisterArbeid(ag, ref, førStp(500), etterSTP(500));
        var ya2 = lagRegisterArbeid(ag, null, etterSTP(50), Tid.TIDENES_ENDE);
        var im1 = lagIM(ag, null, 500_000, 500_000, STP);

        var gr = byggGr(List.of(im1), List.of(im1), List.of(ya1, ya2));

        var resultat = MapInntektsmeldingerTilKravperioder.map(gr, STP);

        assertThat(resultat).hasSize(1);
        assertKrav(resultat, ag, null, 500_000, STP, Tid.TIDENES_ENDE);
    }

    @Test
    void inntektsmelding_med_et_manuelt_opprettet_arbeidsforhold() {
        var ag = new Organisasjon("99999999");
        var ref = new InternArbeidsforholdRefDto(UUID.randomUUID().toString());
        var os1 = lagOverstyrtArbeid(ag, ref, førStp(500), etterSTP(500));
        var im1 = lagIM(ag, ref, 500_000, 500_000, STP);

        var gr = byggGr(List.of(im1), List.of(im1), List.of(), List.of(os1));

        var resultat = MapInntektsmeldingerTilKravperioder.map(gr, STP);

        assertThat(resultat).hasSize(1);
        assertKrav(resultat, ag, ref, 500_000, STP, Tid.TIDENES_ENDE);
    }

    @Test
    void inntektsmeldinger_med_et_manuelt_opprettet_arbeidsforhold_og_et_vanlig() {
        var ag1 = new Organisasjon("99999999");
        var ag2 = new Organisasjon("99999998");
        var ref1 = new InternArbeidsforholdRefDto(UUID.randomUUID().toString());
        var ref2 = new InternArbeidsforholdRefDto(UUID.randomUUID().toString());
        var os1 = lagOverstyrtArbeid(ag1, ref1, førStp(500), etterSTP(500));
        var ya1 = lagRegisterArbeid(ag2, ref2, førStp(500), etterSTP(500));
        var im1 = lagIM(ag1, ref1, 500_000, 500_000, STP);
        var im2 = lagIM(ag2, ref2, 150_000, 150_000, STP);

        var gr = byggGr(List.of(im1, im2), List.of(im1, im2), List.of(ya1), List.of(os1));

        var resultat = MapInntektsmeldingerTilKravperioder.map(gr, STP);

        assertThat(resultat).hasSize(2);
        assertKrav(resultat, ag1, ref1, 500_000, STP, Tid.TIDENES_ENDE);
        assertKrav(resultat, ag2, ref2, 150_000, STP, Tid.TIDENES_ENDE);

    }

    @Test
    void gammel_og_ny_im_for_samme_arbeidsforhold() {
        var ag1 = new Organisasjon("99999999");
        var ref1 = new InternArbeidsforholdRefDto(UUID.randomUUID().toString());
        var os1 = lagOverstyrtArbeid(ag1, ref1, førStp(500), etterSTP(500));
        var førsteInnsendingstidspunkt = STP.minusDays(200);
        var tidligereInnsendtIM = lagIM(ag1, ref1, 500_000, 500_000, STP, førsteInnsendingstidspunkt);
        var im1 = lagIM(ag1, ref1, 500_000, 500_000, STP);

        var gr = byggGr(List.of(im1), List.of(im1, tidligereInnsendtIM), List.of(), List.of(os1));

        var resultat = MapInntektsmeldingerTilKravperioder.map(gr, STP);

        assertThat(resultat).hasSize(1);
        var krav = resultat.getFirst();
        assertThat(krav.getArbeidsgiver().getIdentifikator()).isEqualTo(ag1.getIdent());
        assertThat(krav.getArbeidsforholdRef().getReferanse()).isEqualTo(ref1.getAbakusReferanse());
        assertThat(krav.getPerioder()).hasSize(2);
        var kravperioder = krav.getPerioder().stream()
            .sorted(Comparator.comparing(PerioderForKravDto::getInnsendingsdato))
            .toList();
        assertThat(kravperioder.getFirst().getPerioder()).hasSize(1);
        assertThat(kravperioder.getFirst().getPerioder().getFirst().periode().getFomDato()).isEqualTo(STP);
        assertThat(kravperioder.getFirst().getPerioder().getFirst().periode().getTomDato()).isEqualTo(Tid.TIDENES_ENDE);
        assertThat(kravperioder.getFirst().getInnsendingsdato()).isEqualTo(førsteInnsendingstidspunkt);
        assertThat(kravperioder.getFirst().getPerioder().getFirst().beløp().verdi()).isEqualByComparingTo(BigDecimal.valueOf(500_000));

        assertThat(kravperioder.get(1).getPerioder()).hasSize(1);
        assertThat(kravperioder.get(1).getPerioder().getFirst().periode().getFomDato()).isEqualTo(STP);
        assertThat(kravperioder.get(1).getPerioder().getFirst().periode().getTomDato()).isEqualTo(Tid.TIDENES_ENDE);
        assertThat(kravperioder.get(1).getPerioder().getFirst().beløp().verdi()).isEqualByComparingTo(BigDecimal.valueOf(500_000));
        assertThat(kravperioder.get(1).getInnsendingsdato()).isEqualTo(STP);

        assertThat(krav.getSisteSøktePerioder()).hasSize(1);
        assertThat(krav.getSisteSøktePerioder().getFirst().getFomDato()).isEqualTo(STP);
        assertThat(krav.getSisteSøktePerioder().getFirst().getTomDato()).isEqualTo(Tid.TIDENES_ENDE);
    }

    @Test
    void gammel_og_ny_im_for_samme_arbeidsforhold_ulike_perioder() {
        var ag1 = new Organisasjon("99999999");
        var ref1 = new InternArbeidsforholdRefDto(UUID.randomUUID().toString());
        var os1 = lagOverstyrtArbeid(ag1, ref1, førStp(500), etterSTP(500));
        var førsteInnsendingstidspunkt = STP.minusDays(200);
        var tidligereInnsendtIM = lagIM(ag1, ref1, 500_000, 500_000, STP, førsteInnsendingstidspunkt);
        var nyStartdato = STP.plusDays(10);
        var aktivIM = lagIM(ag1, ref1, 500_000, 350_000, nyStartdato);

        var gr = byggGr(List.of(aktivIM), List.of(aktivIM, tidligereInnsendtIM), List.of(), List.of(os1));

        var resultat = MapInntektsmeldingerTilKravperioder.map(gr, STP);

        assertThat(resultat).hasSize(1);
        var krav = resultat.getFirst();
        assertThat(krav.getArbeidsgiver().getIdentifikator()).isEqualTo(ag1.getIdent());
        assertThat(krav.getArbeidsforholdRef().getReferanse()).isEqualTo(ref1.getAbakusReferanse());
        assertThat(krav.getPerioder()).hasSize(2);
        var kravperioder = krav.getPerioder().stream()
            .sorted(Comparator.comparing(PerioderForKravDto::getInnsendingsdato))
            .toList();
        assertThat(kravperioder.getFirst().getPerioder()).hasSize(1);
        assertThat(kravperioder.getFirst().getPerioder().getFirst().periode().getFomDato()).isEqualTo(STP);
        assertThat(kravperioder.getFirst().getPerioder().getFirst().periode().getTomDato()).isEqualTo(Tid.TIDENES_ENDE);
        assertThat(kravperioder.getFirst().getInnsendingsdato()).isEqualTo(førsteInnsendingstidspunkt);
        assertThat(kravperioder.getFirst().getPerioder().getFirst().beløp().verdi()).isEqualByComparingTo(BigDecimal.valueOf(500_000));

        assertThat(kravperioder.get(1).getPerioder()).hasSize(1);

        // Siden startdato fra IM var etter stp men startdato for AF var før STP settes startdato for refusjon lik STP
        assertThat(kravperioder.get(1).getPerioder().getFirst().periode().getFomDato()).isEqualTo(STP);

        assertThat(kravperioder.get(1).getPerioder().getFirst().periode().getTomDato()).isEqualTo(Tid.TIDENES_ENDE);
        assertThat(kravperioder.get(1).getPerioder().getFirst().beløp().verdi()).isEqualByComparingTo(BigDecimal.valueOf(350_000));
        assertThat(kravperioder.get(1).getInnsendingsdato()).isEqualTo(nyStartdato);

        assertThat(krav.getSisteSøktePerioder().getFirst()).isNotNull();
        assertThat(krav.getSisteSøktePerioder().getFirst().getFomDato()).isEqualTo(STP);
        assertThat(krav.getSisteSøktePerioder().getFirst().getTomDato()).isEqualTo(Tid.TIDENES_ENDE);
    }

    private void assertKrav(List<KravperioderPrArbeidsforholdDto> resultat,
                            Aktør ag,
                            InternArbeidsforholdRefDto ref,
                            int beløp,
                            LocalDate fom,
                            LocalDate tom) {
        var mappetKrav = finnrettKrav(resultat, ag, ref);
        assertThat(mappetKrav.getPerioder()).hasSize(1);
        assertThat(mappetKrav.getPerioder()).hasSize(1);
        assertThat(mappetKrav.getPerioder().getFirst().getPerioder()).hasSize(1);
        assertThat(mappetKrav.getPerioder().getFirst().getPerioder().getFirst().beløp().verdi()).isEqualByComparingTo(BigDecimal.valueOf(beløp));
        assertThat(mappetKrav.getPerioder().getFirst().getPerioder().getFirst().periode().getFomDato()).isEqualTo(fom);
        assertThat(mappetKrav.getPerioder().getFirst().getPerioder().getFirst().periode().getTomDato()).isEqualTo(tom);
    }

    private KravperioderPrArbeidsforholdDto finnrettKrav(List<KravperioderPrArbeidsforholdDto> resultat,
                                                         Aktør ag,
                                                         InternArbeidsforholdRefDto ref) {
        var referanse = ref == null ? null : ref.getAbakusReferanse();
        return resultat.stream()
            .filter(krav -> krav.getArbeidsgiver().getIdentifikator().equals(ag.getIdent())
                && Objects.equals(krav.getArbeidsforholdRef() == null ? null : krav.getArbeidsforholdRef().getReferanse(), referanse))
            .findFirst()
            .orElseThrow();
    }

    private LocalDate førStp(int dagerFør) {
        return STP.minusDays(dagerFør);
    }

    private LocalDate etterSTP(int dagerEtter) {
        return STP.plusDays(dagerEtter);
    }

    private InntektsmeldingDto lagIM(Aktør ag, InternArbeidsforholdRefDto internRef, Integer inntekt, Integer refusjon, LocalDate startdatoPermisjon) {
        return lagIM(ag, internRef, inntekt, refusjon, startdatoPermisjon, startdatoPermisjon);
    }

    private InntektsmeldingDto lagIM(Aktør ag, InternArbeidsforholdRefDto internRef, Integer inntekt, Integer refusjon, LocalDate startdatoPermisjon, LocalDate innsendingstidspunkt) {
        return new InntektsmeldingDto(ag, Beløp.fra(inntekt), List.of(), List.of(), internRef, startdatoPermisjon, Tid.TIDENES_ENDE, Beløp.fra(refusjon), null, innsendingstidspunkt);
    }

    private ArbeidsforholdOverstyringDto lagOverstyrtArbeid(Aktør ag, InternArbeidsforholdRefDto internRef, LocalDate fom, LocalDate tom) {
        return new ArbeidsforholdOverstyringDto(ag, internRef, ArbeidsforholdHandlingType.BASERT_PÅ_INNTEKTSMELDING, IayProsent.fra(100), List.of(new Periode(fom, tom)));
    }

    private YrkesaktivitetDto lagRegisterArbeid(Aktør ag, InternArbeidsforholdRefDto internRef, LocalDate fom, LocalDate tom) {
        var aa = new AktivitetsAvtaleDto(new Periode(fom, tom), null, null);
        return new YrkesaktivitetDto(ag, internRef, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, List.of(aa));
    }

    private static InntektArbeidYtelseGrunnlagDto byggGr(List<InntektsmeldingDto> aktiveIm, List<InntektsmeldingDto> alleImPåSak, List<YrkesaktivitetDto> ya) {
        return byggGr(aktiveIm, alleImPåSak, ya, List.of());
    }

    private static InntektArbeidYtelseGrunnlagDto byggGr(List<InntektsmeldingDto> im, List<InntektsmeldingDto> alleImPåSak, List<YrkesaktivitetDto> ya, List<ArbeidsforholdOverstyringDto> overstyringer) {
        var gr = new InntektArbeidYtelseGrunnlagDto();
        gr.medAlleInntektsmeldingerPåSak(alleImPåSak)
            .medArbeidDto(new ArbeidDto(ya))
            .medInntektsmeldingerDto(new InntektsmeldingerDto(im));
        if (!overstyringer.isEmpty()) {
            gr.medArbeidsforholdInformasjonDto(new ArbeidsforholdInformasjonDto(overstyringer));
        }
        return gr;
    }
}
