package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.typer.EksternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.vedtak.util.Tuple;

public class ArbeidsforholdInformasjonDtoBuilder {

    private final ArbeidsforholdInformasjonDto kladd;
    private final List<Tuple<Arbeidsgiver, Tuple<InternArbeidsforholdRefDto, InternArbeidsforholdRefDto>>> erstattArbeidsforhold = new ArrayList<>();
    private final List<Tuple<Arbeidsgiver, Tuple<InternArbeidsforholdRefDto, InternArbeidsforholdRefDto>>> reverserteErstattninger = new ArrayList<>();

    private ArbeidsforholdInformasjonDtoBuilder(ArbeidsforholdInformasjonDto kladd) {
        this.kladd = kladd;
    }

    public static ArbeidsforholdInformasjonDtoBuilder oppdatere(ArbeidsforholdInformasjonDto oppdatere) {
        return new ArbeidsforholdInformasjonDtoBuilder(new ArbeidsforholdInformasjonDto(oppdatere));
    }

    public ArbeidsforholdOverstyringDtoBuilder getOverstyringBuilderFor(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto ref) {
        return kladd.getOverstyringBuilderFor(arbeidsgiver, ref);
    }

    /**
     * Benyttes for å vite hvilke inntektsmeldinger som skal tas ut av grunnlaget ved erstatting av ny id.
     *
     * @return Liste over Arbeidsgiver / ArbeidsforholdReferanser
     */
    public List<Tuple<Arbeidsgiver, Tuple<InternArbeidsforholdRefDto, InternArbeidsforholdRefDto>>> getErstattArbeidsforhold() {
        return Collections.unmodifiableList(erstattArbeidsforhold);
    }

    /**
     * Benyttes for å vite hvilke inntektsmeldinger som skal tas ut av grunnlaget ved erstatting av ny id.
     *
     * @return Liste over Arbeidsgiver / ArbeidsforholdReferanser
     */
    public List<Tuple<Arbeidsgiver, Tuple<InternArbeidsforholdRefDto, InternArbeidsforholdRefDto>>> getReverserteErstattArbeidsforhold() {
        return Collections.unmodifiableList(reverserteErstattninger);
    }

    public ArbeidsforholdInformasjonDtoBuilder erstattArbeidsforhold(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto gammelRef, InternArbeidsforholdRefDto ref) {
        // TODO: Sjekke om revertert allerede
        // Hvis eksisterer så reverter revertering og ikke legg inn erstattning og kall på erstatt
        erstattArbeidsforhold.add(new Tuple<>(arbeidsgiver, new Tuple<>(gammelRef, ref)));
        kladd.erstattArbeidsforhold(arbeidsgiver, gammelRef, ref);
        return this;
    }

    public ArbeidsforholdInformasjonDtoBuilder leggTil(ArbeidsforholdOverstyringDtoBuilder overstyringBuilder) {
        if (!overstyringBuilder.isOppdatering()) {
            kladd.leggTilOverstyring(overstyringBuilder.build());
        }
        return this;
    }

    public ArbeidsforholdInformasjonDto build() {
        return kladd;
    }

    public ArbeidsforholdInformasjonDtoBuilder fjernOverstyringVedrørende(Arbeidsgiver arbeidsgiver,
                                                                          InternArbeidsforholdRefDto arbeidsforholdRef) {
        kladd.fjernOverstyringVedrørende(arbeidsgiver, arbeidsforholdRef);
        return this;
    }

    public void fjernAlleOverstyringer() {
        kladd.tilbakestillOverstyringer();
    }

    public void fjernOverstyringerSomGjelder(Arbeidsgiver arbeidsgiver) {
        kladd.fjernOverstyringerSomGjelder(arbeidsgiver);
    }

    public void leggTil(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto internReferanse, EksternArbeidsforholdRef eksternReferanse) {
        kladd.leggTilNyReferanse(new ArbeidsforholdReferanseDto(arbeidsgiver, internReferanse, eksternReferanse));
    }

    public void leggTilNyReferanse(ArbeidsforholdReferanseDto arbeidsforholdReferanse) {
        kladd.leggTilNyReferanse(arbeidsforholdReferanse);
    }

    public static ArbeidsforholdInformasjonDtoBuilder oppdatere(Optional<InntektArbeidYtelseGrunnlagDto> grunnlag) {
        return oppdatere(InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(grunnlag).getInformasjon());
    }

    public static ArbeidsforholdInformasjonDtoBuilder builder(Optional<ArbeidsforholdInformasjonDto> arbeidsforholdInformasjon) {
        var arbeidInfo = arbeidsforholdInformasjon.map(ai -> new ArbeidsforholdInformasjonDto(ai)).orElseGet(() -> new ArbeidsforholdInformasjonDto());
        return new ArbeidsforholdInformasjonDtoBuilder(arbeidInfo);
    }
}
