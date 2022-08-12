package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.fakta;

import static no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.fakta.FastsettFaktaLønnsendring.fastsettFaktaForLønnsendring;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.folketrygdloven.kalkulator.FagsakYtelseTypeRef;
import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

@ApplicationScoped
@FagsakYtelseTypeRef(FagsakYtelseType.SVANGERSKAPSPENGER)
@FagsakYtelseTypeRef(FagsakYtelseType.FORELDREPENGER)
public class FastsettFaktaTjenesteK14 implements FastsettFakta {

    public Optional<FaktaAggregatDto> fastsettFakta(BeregningsgrunnlagDto beregningsgrunnlag,
                                                    InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                    Collection<InntektsmeldingDto> inntektsmeldinger) {
        if (KonfigurasjonVerdi.get("AUTOMATISK_BEREGNE_LONNSENDRING", false)) {
            FaktaAggregatDto.Builder faktaBuilder = FaktaAggregatDto.builder();
            List<FaktaArbeidsforholdDto> faktaLønnsendring = fastsettFaktaForLønnsendring(beregningsgrunnlag, iayGrunnlag, inntektsmeldinger);
            faktaLønnsendring.forEach(faktaBuilder::kopierTilEksisterendeEllerLeggTil);
            if (!faktaBuilder.manglerFakta()) {
                return Optional.of(faktaBuilder.build());
            }
        }
        return Optional.empty();
    }

}
