package no.nav.folketrygdloven.kalkulator.guitjenester.inntektsgrunnlag;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørArbeidDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.inntektsgrunnlag.InntektsgrunnlagDto;

public class InntektsgrunnlagTjeneste {
    private static final List<ArbeidType> FRILANS_TYPER = Arrays.asList(ArbeidType.FRILANSER, ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER);

    public static Optional<InntektsgrunnlagDto> lagDto(BeregningsgrunnlagGUIInput input) {
        InntektFilterDto inntektFilter = new InntektFilterDto(input.getIayGrunnlag().getAktørInntektFraRegister()).før(input.getSkjæringstidspunktForBeregning());
        List<InntektDto> sammenligningInntekter = inntektFilter.getAlleInntektSammenligningsgrunnlag().stream()
                .filter(inntekt -> inntekt.getInntektsKilde().equals(InntektskildeType.INNTEKT_SAMMENLIGNING))
                .collect(Collectors.toList());
        List<Arbeidsgiver> frilansArbeidsgivere = finnFrilansArbeidsgivere(input);
        // Inntektsgrunnlaget skal brukes i tilfeller med avvik for at / fl, ser derfor etter dette spesifikt.
        Optional<SammenligningsgrunnlagPrStatusDto> sammenligningsgrunnlagATFL = input.getBeregningsgrunnlag() != null ? input.getBeregningsgrunnlag().getSammenligningsgrunnlagForStatus(SammenligningsgrunnlagType.SAMMENLIGNING_AT_FL) : Optional.empty();
        if (sammenligningsgrunnlagATFL.isEmpty()) {
            return Optional.empty();
        }
        Intervall sammenligningsperiode = Intervall.fraOgMedTilOgMed(sammenligningsgrunnlagATFL.get().getSammenligningsperiodeFom(), sammenligningsgrunnlagATFL.get().getSammenligningsperiodeTom());
        InntektsgrunnlagMapper mapper = new InntektsgrunnlagMapper(sammenligningsperiode, frilansArbeidsgivere);
        return mapper.map(sammenligningInntekter);
    }

    private static List<Arbeidsgiver> finnFrilansArbeidsgivere(BeregningsgrunnlagGUIInput input) {
        Collection<YrkesaktivitetDto> alleYrkesaktiviteter = input.getIayGrunnlag().getAktørArbeidFraRegister()
                .map(AktørArbeidDto::hentAlleYrkesaktiviteter)
                .orElse(Collections.emptyList());
        return alleYrkesaktiviteter.stream().filter(ya -> FRILANS_TYPER.contains(ya.getArbeidType()))
                .filter(ya -> ya.getArbeidsgiver() != null
                        && ya.getArbeidsgiver().getIdentifikator() != null)
                .map(YrkesaktivitetDto::getArbeidsgiver)
                .collect(Collectors.toList());
    }
}
