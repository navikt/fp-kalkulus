package no.nav.folketrygdloven.kalkulus.håndtering.faktaberegning;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.virksomhet.OrganisasjonsNummerValidator;
import no.nav.folketrygdloven.kalkulus.felles.v1.AktørIdPersonident;
import no.nav.folketrygdloven.kalkulus.felles.v1.Organisasjon;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FaktaOmBeregningHåndteringDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.RefusjonskravPrArbeidsgiverVurderingDto;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.FaktaOmBeregningVurderinger;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.RefusjonskravGyldighetEndring;
import no.nav.folketrygdloven.kalkulus.response.v1.håndtering.ToggleEndring;

class UtledFaktaOmBeregningVurderinger {

    private UtledFaktaOmBeregningVurderinger() {
        // Skjul
    }

    public static FaktaOmBeregningVurderinger utled(FaktaOmBeregningHåndteringDto dto, BeregningsgrunnlagDto beregningsgrunnlagDto, Optional<BeregningsgrunnlagDto> forrigeGrunnlag) {
        FaktaOmBeregningVurderinger faktaOmBeregningVurderinger = new FaktaOmBeregningVurderinger();
        faktaOmBeregningVurderinger.setHarEtterlønnSluttpakkeEndring(utledVurderEtterlønnSluttpakkeEndring(dto));
        faktaOmBeregningVurderinger.setHarLønnsendringIBeregningsperiodenEndring(utledVurderLønnsendringEndring(dto));
        faktaOmBeregningVurderinger.setHarMilitærSiviltjenesteEndring(utledVurderMilitærEllerSiviltjenesteEndring(dto));
        faktaOmBeregningVurderinger.setErSelvstendingNyIArbeidslivetEndring(utledErSelvstendigNyIArbeidslivetEndring(dto));
        faktaOmBeregningVurderinger.setErNyoppstartetFLEndring(utledErNyoppstartetFLEndring(dto));
        faktaOmBeregningVurderinger.setVurderRefusjonskravGyldighetEndringer(utledUtvidRefusjonskravGyldighetEndringer(dto));
        faktaOmBeregningVurderinger.setErMottattYtelseEndringer(UtledErMottattYtelseEndringer.utled(beregningsgrunnlagDto, forrigeGrunnlag));
        faktaOmBeregningVurderinger.setErTidsbegrensetArbeidsforholdEndringer(UtledErTidsbegrensetArbeidsforholdEndringer.utled(beregningsgrunnlagDto, forrigeGrunnlag));
        if (harEndringer(faktaOmBeregningVurderinger)) {
            return faktaOmBeregningVurderinger;
        }
        return null;
    }

    private static List<RefusjonskravGyldighetEndring> utledUtvidRefusjonskravGyldighetEndringer(FaktaOmBeregningHåndteringDto dto) {
        if (dto.getFakta().getRefusjonskravGyldighet() == null) {
            return null;
        }
        return dto.getFakta().getRefusjonskravGyldighet().stream().map(UtledFaktaOmBeregningVurderinger::utledRefusjonskravGyldighetEndring)
                .collect(Collectors.toList());
    }

    private static RefusjonskravGyldighetEndring utledRefusjonskravGyldighetEndring(RefusjonskravPrArbeidsgiverVurderingDto dto) {
        return new RefusjonskravGyldighetEndring(new ToggleEndring(null, dto.isSkalUtvideGyldighet()),
                OrganisasjonsNummerValidator.erGyldig(dto.getArbeidsgiverId()) ? new Organisasjon(dto.getArbeidsgiverId()) : new AktørIdPersonident(dto.getArbeidsgiverId()));
    }

    private static boolean harEndringer(FaktaOmBeregningVurderinger faktaOmBeregningVurderinger) {
        return faktaOmBeregningVurderinger.getErNyoppstartetFLEndring() != null ||
                faktaOmBeregningVurderinger.getErSelvstendingNyIArbeidslivetEndring() != null ||
                faktaOmBeregningVurderinger.getHarEtterlønnSluttpakkeEndring() != null ||
                faktaOmBeregningVurderinger.getHarLønnsendringIBeregningsperiodenEndring() != null ||
                faktaOmBeregningVurderinger.getHarMilitærSiviltjenesteEndring() != null ||
                !faktaOmBeregningVurderinger.getErMottattYtelseEndringer().isEmpty() ||
                !faktaOmBeregningVurderinger.getErTidsbegrensetArbeidsforholdEndringer().isEmpty();
    }

    private static ToggleEndring utledErNyoppstartetFLEndring(FaktaOmBeregningHåndteringDto dto) {
        if (dto.getFakta().getVurderNyoppstartetFL() != null) {
            return new ToggleEndring(null, dto.getFakta().getVurderNyoppstartetFL().erErNyoppstartetFL());
        }
        return null;
    }

    private static ToggleEndring utledVurderEtterlønnSluttpakkeEndring(FaktaOmBeregningHåndteringDto dto) {
        if (dto.getFakta().getVurderEtterlønnSluttpakke() != null) {
            return new ToggleEndring(null, dto.getFakta().getVurderEtterlønnSluttpakke().getErEtterlønnSluttpakke());
        }
        return null;
    }

    private static ToggleEndring utledVurderLønnsendringEndring(FaktaOmBeregningHåndteringDto dto) {
        if (dto.getFakta().getVurdertLonnsendring() != null) {
            return new ToggleEndring(null, dto.getFakta().getVurdertLonnsendring().erLønnsendringIBeregningsperioden());
        }
        return null;
    }

    private static ToggleEndring utledVurderMilitærEllerSiviltjenesteEndring(FaktaOmBeregningHåndteringDto dto) {
        if (dto.getFakta().getVurderMilitaer() != null) {
            return new ToggleEndring(null, dto.getFakta().getVurderMilitaer().getHarMilitaer());
        }
        return null;
    }

    private static ToggleEndring utledErSelvstendigNyIArbeidslivetEndring(FaktaOmBeregningHåndteringDto dto) {
        if (dto.getFakta().getVurderNyIArbeidslivet() != null) {
            return new ToggleEndring(null, dto.getFakta().getVurderNyIArbeidslivet().erNyIArbeidslivet());
        }
        return null;
    }


}
