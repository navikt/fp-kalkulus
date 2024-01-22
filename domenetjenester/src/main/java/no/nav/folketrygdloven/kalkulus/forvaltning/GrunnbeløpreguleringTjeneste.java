package no.nav.folketrygdloven.kalkulus.forvaltning;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.folketrygdloven.kalkulus.beregning.input.KalkulatorInputTjeneste;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.BeregningSats;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.GrunnbeløpReguleringStatus;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.BeregningsgrunnlagRepository;

@ApplicationScoped
public class GrunnbeløpreguleringTjeneste {

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private KoblingTjeneste koblingTjeneste;
    private KalkulatorInputTjeneste kalkulatorInputTjeneste;


    public GrunnbeløpreguleringTjeneste() {
        // CDI
    }

    @Inject
    public GrunnbeløpreguleringTjeneste(BeregningsgrunnlagRepository beregningsgrunnlagRepository,
                                        KoblingTjeneste koblingTjeneste,
                                        KalkulatorInputTjeneste kalkulatorInputTjeneste) {
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
        this.koblingTjeneste = koblingTjeneste;
        this.kalkulatorInputTjeneste = kalkulatorInputTjeneste;
    }

    public GrunnbeløpReguleringStatus undersøkBehovForGregulering(KoblingReferanse referanse, String saksnummer) {
        Optional<KoblingEntitet> koblingEntitetOpt = koblingTjeneste.hentFor(referanse);
        if (!koblingErGyldig(koblingEntitetOpt, saksnummer)) {
            return GrunnbeløpReguleringStatus.IKKE_VURDERT;
        }
        Optional<BeregningsgrunnlagGrunnlagEntitet> gjeldendeBG = koblingEntitetOpt.flatMap(k -> beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(k.getId()));
        var inputresultat = kalkulatorInputTjeneste.hentForKoblinger(Collections.singletonList(koblingEntitetOpt.get().getId()));
        KalkulatorInputDto input = inputresultat.get(koblingEntitetOpt.get().getId());
        BeregningSats nySats = beregningsgrunnlagRepository.finnGrunnbeløp(input.getSkjæringstidspunkt());
        return Greguleringsstatusutleder.utledStatus(gjeldendeBG, BigDecimal.valueOf(nySats.getVerdi()), koblingEntitetOpt.get().getYtelseType());
    }

    private boolean koblingErGyldig(Optional<KoblingEntitet> koblingEntitetOpt, String saksnummer) {
        return koblingEntitetOpt.map(k -> k.getSaksnummer().equals(new Saksnummer(saksnummer)))
                .orElse(false);
    }
}
