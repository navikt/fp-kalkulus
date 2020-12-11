package no.nav.folketrygdloven.kalkulator;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonskravDatoDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;

public class OpprettRefusjondatoerFraInntektsmeldinger {

    public static List<RefusjonskravDatoDto> opprett(KoblingReferanse ref, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        return iayGrunnlag.getInntektsmeldinger().map(ims -> ims.getAlleInntektsmeldinger().stream()
            .map(im -> new RefusjonskravDatoDto(im.getArbeidsgiver(), im.getStartDatoPermisjon().orElse(ref.getSkjæringstidspunktBeregning()), ref.getSkjæringstidspunktBeregning(), im.getStartDatoPermisjon().map(d -> d.equals(ref.getSkjæringstidspunktBeregning())).orElse(true)))
            .collect(Collectors.toList())
        ).orElse(List.of());
    }

    public static List<RefusjonskravDatoDto> opprett(KoblingReferanse ref, InntektArbeidYtelseGrunnlagDto iayGrunnlag, Map<Arbeidsgiver, LocalDate> førsteInnsendingsdatoMap) {
        return iayGrunnlag.getInntektsmeldinger().map(ims -> ims.getAlleInntektsmeldinger().stream()
            .map(im -> new RefusjonskravDatoDto(im.getArbeidsgiver(), im.getStartDatoPermisjon().orElse(ref.getSkjæringstidspunktBeregning()),
                førsteInnsendingsdatoMap.containsKey(im.getArbeidsgiver()) ? førsteInnsendingsdatoMap.get(im.getArbeidsgiver()) : ref.getSkjæringstidspunktBeregning(),  im.getStartDatoPermisjon().map(d -> d.equals(ref.getSkjæringstidspunktBeregning())).orElse(true)))
            .collect(Collectors.toList())
        ).orElse(List.of());
    }

}
