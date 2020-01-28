package no.nav.folketrygdloven.kalkulator.rest;

import no.nav.folketrygdloven.kalkulator.modell.behandling.BehandlingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelRestDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.ArbeidsgiverMedNavn;


public class VisningsnavnForAktivitetTjeneste {

    private static final int ANTALL_SIFFER_SOM_SKAL_VISES_AV_REFERANSE = 4;

    private VisningsnavnForAktivitetTjeneste() {
        // For CDI
    }

    public static String lagVisningsnavn(BehandlingReferanse ref, InntektArbeidYtelseGrunnlagDto iayGrunnlag, BeregningsgrunnlagPrStatusOgAndelRestDto andel) {
        if (andel.getAktivitetStatus().erArbeidstaker()) {
            return finnVisningsnavnForArbeidstaker(ref, iayGrunnlag, andel);
        }
        return andel.getArbeidsforholdType() == null || OpptjeningAktivitetType.UDEFINERT.equals(andel.getArbeidsforholdType()) ? andel.getAktivitetStatus().getNavn() : andel.getArbeidsforholdType().getNavn();
    }

    private static String finnVisningsnavnForArbeidstaker(BehandlingReferanse ref, InntektArbeidYtelseGrunnlagDto iayGrunnlag, BeregningsgrunnlagPrStatusOgAndelRestDto andel) {
        return andel.getBgAndelArbeidsforhold()
            .map(bgAndelArbeidsforhold -> {
                ArbeidsgiverMedNavn arbeidsgiver = bgAndelArbeidsforhold.getArbeidsgiver();
                String visningsnavnUtenReferanse = finnVisningsnavnUtenReferanse(arbeidsgiver);
                return finnVisningsnavnMedReferanseHvisFinnes(ref, arbeidsgiver, bgAndelArbeidsforhold, visningsnavnUtenReferanse, iayGrunnlag);
            }).orElse(andel.getArbeidsforholdType().getNavn());
    }

    private static String finnVisningsnavnMedReferanseHvisFinnes(BehandlingReferanse ref, ArbeidsgiverMedNavn arbeidsgiver, BGAndelArbeidsforholdRestDto bgAndelArbeidsforhold, String visningsnavnUtenReferanse, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        String referanse = bgAndelArbeidsforhold.getArbeidsforholdRef().getReferanse();
        if (referanse != null) {
            if (inntektArbeidYtelseGrunnlag.getArbeidsforholdInformasjon().isEmpty()) {
                throw new IllegalStateException("Mangler arbeidsforholdinformasjon for behandlingId=" + ref.getBehandlingId());
            }
            var eksternArbeidsforholdRef = inntektArbeidYtelseGrunnlag.getArbeidsforholdInformasjon().get().finnEkstern(MapBeregningsgrunnlagFraRestTilDomene.mapArbeidsgiver(arbeidsgiver), bgAndelArbeidsforhold.getArbeidsforholdRef());
            var eksternArbeidsforholdId = eksternArbeidsforholdRef.getReferanse();
            return visningsnavnUtenReferanse + " ..." + finnSubstringAvReferanse(eksternArbeidsforholdId);
        }
        return visningsnavnUtenReferanse;
    }

    private static String finnSubstringAvReferanse(String eksternArbeidsforholdId) {
        if (eksternArbeidsforholdId == null) {
            return "";
        }
        if (eksternArbeidsforholdId.length() <= ANTALL_SIFFER_SOM_SKAL_VISES_AV_REFERANSE) {
            return eksternArbeidsforholdId;
        }
        return eksternArbeidsforholdId.substring(eksternArbeidsforholdId.length() - ANTALL_SIFFER_SOM_SKAL_VISES_AV_REFERANSE);
    }

    private static String finnVisningsnavnUtenReferanse(ArbeidsgiverMedNavn arbeidsgiver) {
        return arbeidsgiver.getNavn() + " (" + arbeidsgiver.getIdentifikator() + ")";
    }
}
