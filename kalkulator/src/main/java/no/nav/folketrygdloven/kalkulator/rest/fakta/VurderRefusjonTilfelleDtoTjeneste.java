package no.nav.folketrygdloven.kalkulator.rest.fakta;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.folketrygdloven.kalkulator.KLASSER_MED_AVHENGIGHETER.InntektsmeldingMedRefusjonTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagRestInput;
import no.nav.folketrygdloven.kalkulator.kontrakt.v1.ArbeidsgiverOpplysningerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelRestDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagRestDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.ArbeidsgiverMedNavn;
import no.nav.folketrygdloven.kalkulator.rest.MapBeregningsgrunnlagFraRestTilDomene;
import no.nav.folketrygdloven.kalkulator.rest.dto.FaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulator.rest.dto.RefusjonskravSomKommerForSentDto;
import no.nav.folketrygdloven.kalkulus.felles.kodeverk.domene.FaktaOmBeregningTilfelle;

@ApplicationScoped
class VurderRefusjonTilfelleDtoTjeneste implements FaktaOmBeregningTilfelleDtoTjeneste {

    @Override
    public void lagDto(BeregningsgrunnlagRestInput input, FaktaOmBeregningDto faktaOmBeregningDto) {
        BeregningsgrunnlagRestDto beregningsgrunnlag = input.getBeregningsgrunnlag();
        List<FaktaOmBeregningTilfelle> tilfeller = beregningsgrunnlag.getFaktaOmBeregningTilfeller();
        if (!tilfeller.contains(FaktaOmBeregningTilfelle.VURDER_REFUSJONSKRAV_SOM_HAR_KOMMET_FOR_SENT)) {
            return;
        }
        List<RefusjonskravSomKommerForSentDto> refusjonskravSomKommerForSentList = lagListeMedKravSomKommerForSent(input);
        faktaOmBeregningDto.setRefusjonskravSomKommerForSentListe(refusjonskravSomKommerForSentList);
    }

    private List<RefusjonskravSomKommerForSentDto> lagListeMedKravSomKommerForSent(BeregningsgrunnlagRestInput input) {
        List<BeregningRefusjonOverstyringDto> refusjonOverstyringer = input.getBeregningsgrunnlagGrunnlag().getRefusjonOverstyringer()
            .map(BeregningRefusjonOverstyringerDto::getRefusjonOverstyringer)
            .orElse(Collections.emptyList());

        var beregnGrunnlag = input.getBeregningsgrunnlagGrunnlag();
        Set<Arbeidsgiver> arbeidsgivere = InntektsmeldingMedRefusjonTjeneste.finnArbeidsgiverSomHarSøktRefusjonForSent(
                input.getBehandlingReferanse(),
                input.getIayGrunnlag(),
                MapBeregningsgrunnlagFraRestTilDomene.mapBeregningsgrunnlagGrunnlag(input.getBeregningsgrunnlagGrunnlag()),
                input.getRefusjonskravDatoer()
        );
        Map<Arbeidsgiver, ArbeidsgiverOpplysningerDto> arbeidsgiverOpplysninger = input.getIayGrunnlag().getArbeidsgiverOpplysninger();
        return arbeidsgivere
            .stream()
            .map(arbeidsgiver -> {
                RefusjonskravSomKommerForSentDto dto = new RefusjonskravSomKommerForSentDto();
                dto.setArbeidsgiverId(arbeidsgiver.getIdentifikator());
                ArbeidsgiverOpplysningerDto arbeidsgiverOpplysningerDto = arbeidsgiverOpplysninger.get((arbeidsgiver));
                dto.setArbeidsgiverVisningsnavn(arbeidsgiverOpplysningerDto == null ? "Orgnummer " + arbeidsgiver.getIdentifikator() : arbeidsgiverOpplysningerDto.getNavn());
                LocalDate skjæringstidspunkt = beregnGrunnlag.getBeregningsgrunnlag().map(BeregningsgrunnlagRestDto::getSkjæringstidspunkt).orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag med skjæringstidspunkt"));
                dto.setErRefusjonskravGyldig(sjekkStatusPåRefusjon(arbeidsgiver.getIdentifikator(), refusjonOverstyringer, skjæringstidspunkt));

                return dto;
            }).collect(Collectors.toList());
    }

    private Boolean sjekkStatusPåRefusjon(String identifikator,
                                          List<BeregningRefusjonOverstyringDto> refusjonOverstyringer,
                                          LocalDate skjæringstidspunkt) {
        Optional<BeregningRefusjonOverstyringDto> statusOpt = refusjonOverstyringer
            .stream()
            .filter(refusjonOverstyring -> refusjonOverstyring.getArbeidsgiver().getIdentifikator().equals(identifikator))
            .findFirst();
        if (statusOpt.isEmpty() && refusjonOverstyringer.isEmpty()) {
            return null;
        }
        return statusOpt.isPresent() && statusOpt.get().getFørsteMuligeRefusjonFom().isEqual(skjæringstidspunkt);
    }
}
