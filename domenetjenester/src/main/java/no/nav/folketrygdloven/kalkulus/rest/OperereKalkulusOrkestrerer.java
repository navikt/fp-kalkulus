package no.nav.folketrygdloven.kalkulus.rest;

import static no.nav.folketrygdloven.kalkulus.beregning.MapStegTilTilstand.mapTilStegTilstand;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulus.beregning.BeregningStegTjeneste;
import no.nav.folketrygdloven.kalkulus.beregning.MapHåndteringskodeTilTilstand;
import no.nav.folketrygdloven.kalkulus.beregning.input.HåndteringInputTjeneste;
import no.nav.folketrygdloven.kalkulus.beregning.input.KalkulatorInputTjeneste;
import no.nav.folketrygdloven.kalkulus.beregning.input.StegProsessInputTjeneste;
import no.nav.folketrygdloven.kalkulus.beregning.v1.YtelsespesifiktGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.KoblingReferanse;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.del_entiteter.Saksnummer;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingEntitet;
import no.nav.folketrygdloven.kalkulus.domene.entiteter.kobling.KoblingRelasjon;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.håndtering.HåndtererApplikasjonTjeneste;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.kobling.KoblingTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.StegType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseTyperKalkulusStøtterKontrakt;
import no.nav.folketrygdloven.kalkulus.request.v1.HåndterBeregningRequest;
import no.nav.folketrygdloven.kalkulus.response.v1.KalkulusRespons;
import no.nav.folketrygdloven.kalkulus.tjeneste.beregningsgrunnlag.RullTilbakeTjeneste;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

@ApplicationScoped
public class OperereKalkulusOrkestrerer {

    private KoblingTjeneste koblingTjeneste;
    private BeregningStegTjeneste beregningStegTjeneste;
    private KalkulatorInputTjeneste kalkulatorInputTjeneste;
    private StegProsessInputTjeneste stegInputTjeneste;
    private HåndteringInputTjeneste håndteringInputTjeneste;
    private HåndtererApplikasjonTjeneste håndtererApplikasjonTjeneste;
    private RullTilbakeTjeneste rullTilbakeTjeneste;

    public OperereKalkulusOrkestrerer() {
        // for CDI
    }

    @Inject
    public OperereKalkulusOrkestrerer(KoblingTjeneste koblingTjeneste,
                                      BeregningStegTjeneste beregningStegTjeneste,
                                      KalkulatorInputTjeneste kalkulatorInputTjeneste,
                                      StegProsessInputTjeneste stegInputTjeneste,
                                      HåndteringInputTjeneste håndteringInputTjeneste,
                                      HåndtererApplikasjonTjeneste håndtererApplikasjonTjeneste,
                                      RullTilbakeTjeneste rullTilbakeTjeneste) {
        this.koblingTjeneste = koblingTjeneste;
        this.beregningStegTjeneste = beregningStegTjeneste;
        this.kalkulatorInputTjeneste = kalkulatorInputTjeneste;
        this.stegInputTjeneste = stegInputTjeneste;
        this.håndteringInputTjeneste = håndteringInputTjeneste;
        this.håndtererApplikasjonTjeneste = håndtererApplikasjonTjeneste;
        this.rullTilbakeTjeneste = rullTilbakeTjeneste;
    }

    public Map<Long, KalkulusRespons> start(Map<UUID, KalkulatorInputDto> inputPrReferanse,
                                            YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes,
                                            Map<UUID, List<UUID>> koblingRelasjon,
                                            Saksnummer saksnummer, AktørId aktørId) {
        // Finn koblinger
        var koblinger = finnKoblinger(ytelseSomSkalBeregnes,
                saksnummer,
                inputPrReferanse.keySet().stream().toList(),
                aktørId);
        koblingTjeneste.opprettKoblingRelasjoner(koblingRelasjon);
        Set<Long> koblingIder = koblinger.stream().map(KoblingEntitet::getId).collect(Collectors.toSet());
        // Lag input
        var hentInputResultat = kalkulatorInputTjeneste.hentOgLagre(inputPrReferanse, koblingIder);
        var stegInputPrKobling = lagInputOgRullTilbakeVedBehov(koblingIder, hentInputResultat, new InputForStart(koblinger));
        // Operer
        return opererAlle(stegInputPrKobling, new Starter());
    }

    public Map<Long, KalkulusRespons> fortsett(Map<UUID, KalkulatorInputDto> inputPrReferanse,
                                               Map<UUID, YtelsespesifiktGrunnlagDto> ytelsespesifiktGrunnlagPrKoblingReferanse,
                                               List<UUID> eksternReferanser,
                                               YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes,
                                               Saksnummer saksnummer,
                                               StegType stegType) {
        // Finn koblinger
        var koblinger = finnKoblinger(ytelseSomSkalBeregnes, saksnummer, eksternReferanser, null);
        Set<Long> koblingIder = koblinger.stream().map(KoblingEntitet::getId).collect(Collectors.toSet());
        var koblingRelasjoner = koblingTjeneste.hentKoblingRelasjoner(koblingIder);
        // Lag input
        BeregningSteg beregningSteg = BeregningSteg.fraKode(stegType.getKode());
        var hentInputResultat = kalkulatorInputTjeneste.hentOgLagre(inputPrReferanse, ytelsespesifiktGrunnlagPrKoblingReferanse, koblingIder);
        var stegInputPrKobling = lagInputOgRullTilbakeVedBehov(koblingIder,
                hentInputResultat,
                new InputForSteg(beregningSteg, koblingRelasjoner));
        // Operer
        return opererAlle(stegInputPrKobling, new Fortsetter(beregningSteg));
    }

    public Map<Long, KalkulusRespons> håndter(Map<UUID, KalkulatorInputDto> inputPrReferanse,
                                              YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes,
                                              Saksnummer saksnummer,
                                              List<HåndterBeregningRequest> håndterBeregningListe) {
        List<UUID> referanseListe = håndterBeregningListe.stream().map(HåndterBeregningRequest::getEksternReferanse).collect(Collectors.toList());
        // Finn koblinger
        var koblinger = finnKoblinger(ytelseSomSkalBeregnes, saksnummer, referanseListe, null);
        var koblingTilDto = lagDtoMap(håndterBeregningListe, koblinger);
        Set<Long> koblingIder = koblingTilDto.keySet();
        // Lag input
        var hentInputResultat = kalkulatorInputTjeneste.hentOgLagre(inputPrReferanse, koblingIder);
        var håndterInputPrKobling = lagInputOgRullTilbakeVedBehov(koblingIder, hentInputResultat, new InputForHåndtering(koblingTilDto));
        // Operer
        return opererAlle(håndterInputPrKobling, new Håndterer(koblingTilDto));
    }

    private Map<Long, KalkulusRespons> opererAlle(Map<Long, BeregningsgrunnlagInput> inputResultat,
                                                  Opererer opererer) {
        return inputResultat.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> opererer.utfør(e.getValue())));
    }

    private Map<Long, HåndterBeregningDto> lagDtoMap(List<HåndterBeregningRequest> håndterBeregningListe, List<KoblingEntitet> koblinger) {
        return håndterBeregningListe.stream()
                .collect(Collectors.toMap(s -> finnKoblingId(koblinger, s), HåndterBeregningRequest::getHåndterBeregning));
    }

    private Map<Long, BeregningsgrunnlagInput> lagInputOgRullTilbakeVedBehov(Set<Long> koblingIder,
                                                                            Map<Long, KalkulatorInputDto> kalkulatorInputPrKobling,
                                                                            LagInputTjeneste lagInputTjeneste) {
        rullTilbakeTjeneste.rullTilbakeTilObligatoriskTilstandFørVedBehov(koblingIder, lagInputTjeneste.getTilstand());
        return lagInput(koblingIder, lagInputTjeneste, kalkulatorInputPrKobling);
    }

    private Map<Long, BeregningsgrunnlagInput> lagInput(Set<Long> koblingIder, LagInputTjeneste lagInputTjeneste, Map<Long, KalkulatorInputDto> kalkulatorInputPrKobling) {
        return lagInputTjeneste.utfør(koblingIder, kalkulatorInputPrKobling);
    }

    private List<KoblingEntitet> finnKoblinger(YtelseTyperKalkulusStøtterKontrakt ytelseSomSkalBeregnes,
                                               Saksnummer saksnummer,
                                               List<UUID> refranser,
                                               AktørId aktørId) {
        List<KoblingReferanse> referanser = refranser.stream().map(KoblingReferanse::new).collect(Collectors.toList());
        if (ytelseSomSkalBeregnes == null) {
            var koblinger = koblingTjeneste.hentKoblinger(referanser);
            validerKoblingOgSak(koblinger, saksnummer.getVerdi());
            return koblinger;
        } else {
            var ytelseTyperKalkulusStøtter = YtelseTyperKalkulusStøtterKontrakt.fraKode(ytelseSomSkalBeregnes.getKode());
            var koblinger = koblingTjeneste.finnEllerOpprett(referanser, ytelseTyperKalkulusStøtter, aktørId, saksnummer);
            validerKoblingOgSak(koblinger, saksnummer.getVerdi());
            return koblinger;
        }
    }

    private void validerKoblingOgSak(List<KoblingEntitet> koblinger, String saksnummer) {
        List<KoblingEntitet> koblingUtenSaksnummer = koblinger.stream()
                .filter(k -> !Objects.equals(k.getSaksnummer().getVerdi(), saksnummer)).collect(Collectors.toList());
        if (!koblingUtenSaksnummer.isEmpty()) {
            throw new IllegalArgumentException("Koblinger tilhører ikke saksnummer [" + saksnummer + "]: " + koblingUtenSaksnummer);
        }
    }

    private Long finnKoblingId(List<KoblingEntitet> koblinger, HåndterBeregningRequest s) {
        return koblinger.stream().filter(kobling -> kobling.getKoblingReferanse().getReferanse().equals(s.getEksternReferanse()))
                .findFirst().map(KoblingEntitet::getId).orElse(null);
    }


    private interface LagInputTjeneste {
        Map<Long, BeregningsgrunnlagInput> utfør(Set<Long> koblingIder, Map<Long, KalkulatorInputDto> inputPrKobling);

        BeregningsgrunnlagTilstand getTilstand();
    }

    private class InputForStart implements LagInputTjeneste {

        private final List<KoblingEntitet> koblinger;

        private InputForStart(List<KoblingEntitet> koblinger) {
            this.koblinger = koblinger;
        }

        @Override
        public Map<Long, BeregningsgrunnlagInput> utfør(Set<Long> koblingIder, Map<Long, KalkulatorInputDto> inputPrKobling) {
            return inputPrKobling.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> stegInputTjeneste.lagStartInput(finnKobling(e.getKey()), e.getValue())
                    ));
        }

        private KoblingEntitet finnKobling(Long id) {
            return koblinger.stream().filter(k -> k.getId().equals(id)).findFirst().orElseThrow(() -> new IllegalStateException("Fant ikke kobling"));
        }

        @Override
        public BeregningsgrunnlagTilstand getTilstand() {
            return BeregningsgrunnlagTilstand.OPPRETTET;
        }
    }

    private class InputForSteg implements LagInputTjeneste {

        private final BeregningSteg steg;
        private final List<KoblingRelasjon> koblingRelasjoner;


        private InputForSteg(BeregningSteg steg, List<KoblingRelasjon> koblingRelasjoner) {
            this.steg = steg;
            this.koblingRelasjoner = koblingRelasjoner;
        }

        @Override
        public Map<Long, BeregningsgrunnlagInput> utfør(Set<Long> koblingIder, Map<Long, KalkulatorInputDto> inputPrKobling) {
            return stegInputTjeneste.lagBeregningsgrunnlagInput(koblingIder, inputPrKobling, steg, koblingRelasjoner)
                    .entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue));
        }

        @Override
        public BeregningsgrunnlagTilstand getTilstand() {
            return mapTilStegTilstand(steg);
        }
    }

    private class InputForHåndtering implements LagInputTjeneste {

        private final BeregningsgrunnlagTilstand tilstand;

        private InputForHåndtering(Map<Long, HåndterBeregningDto> koblingTilDto) {
            this.tilstand = finnTilstandFraDto(koblingTilDto);
        }

        @Override
        public Map<Long, BeregningsgrunnlagInput> utfør(Set<Long> koblingIder, Map<Long, KalkulatorInputDto> inputPrKobling) {
            return håndteringInputTjeneste.lagBeregningsgrunnlagInput(koblingIder, inputPrKobling, tilstand)
                    .entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue));
        }

        @Override
        public BeregningsgrunnlagTilstand getTilstand() {
            return tilstand;
        }


        private BeregningsgrunnlagTilstand finnTilstandFraDto(Map<Long, HåndterBeregningDto> håndterBeregningDtoPrKobling) {
            List<BeregningsgrunnlagTilstand> tilstander = håndterBeregningDtoPrKobling.values().stream().map(HåndterBeregningDto::getKode)
                    .map(MapHåndteringskodeTilTilstand::map)
                    .distinct()
                    .collect(Collectors.toList());
            if (tilstander.size() > 1) {
                throw new IllegalStateException("Kan ikke løse avklaringsbehov for flere tilstander samtidig");
            }
            return tilstander.get(0);
        }

    }


    private interface Opererer {
        KalkulusRespons utfør(BeregningsgrunnlagInput beregningsgrunnlagInput);
    }

    private class Håndterer implements Opererer {

        private final Map<Long, HåndterBeregningDto> håndteringDtoMap;

        public Håndterer(Map<Long, HåndterBeregningDto> håndteringDtoMap) {
            this.håndteringDtoMap = håndteringDtoMap;
        }

        @Override
        public KalkulusRespons utfør(BeregningsgrunnlagInput beregningsgrunnlagInput) {
            return håndtererApplikasjonTjeneste.håndter((HåndterBeregningsgrunnlagInput) beregningsgrunnlagInput, håndteringDtoMap.get(beregningsgrunnlagInput.getKoblingId()));
        }
    }

    private class Starter implements Opererer {

        @Override
        public KalkulusRespons utfør(BeregningsgrunnlagInput beregningsgrunnlagInput) {
            return beregningStegTjeneste.fastsettBeregningsaktiviteter((FastsettBeregningsaktiviteterInput) beregningsgrunnlagInput);
        }
    }

    private class Fortsetter implements Opererer {

        private final BeregningSteg beregningSteg;

        public Fortsetter(BeregningSteg beregningSteg) {
            this.beregningSteg = beregningSteg;
        }

        @Override
        public KalkulusRespons utfør(BeregningsgrunnlagInput beregningsgrunnlagInput) {
            return beregningStegTjeneste.beregnFor(beregningSteg, (StegProsesseringInput) beregningsgrunnlagInput);
        }
    }


}
