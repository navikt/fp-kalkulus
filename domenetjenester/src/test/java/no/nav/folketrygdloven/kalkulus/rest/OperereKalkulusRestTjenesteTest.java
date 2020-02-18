package no.nav.folketrygdloven.kalkulus.rest;


import org.jboss.weld.junit5.EnableWeld;

@EnableWeld
public class OperereKalkulusRestTjenesteTest {

//    @WeldSetup
//    WeldInitiator weldInitiator = WeldInitiator.of(WeldInitiator.createWeld()
//            .addPackages(true, KoblingTjeneste.class)
//            .addPackages(true, BeregningStegTjeneste.class)
//            .addPackages(true, KalkulatorInputTjeneste.class)
//            .addPackage(true, HåndtererApplikasjonTjeneste.class));
//
//    @Inject
//    private KoblingTjeneste koblingTjeneste;
//
//    @Inject
//    private BeregningStegTjeneste beregningStegTjeneste;
//
//    @Inject
//    private KalkulatorInputTjeneste kalkulatorInputTjeneste;
//
//    @Inject
//    private HåndtererApplikasjonTjeneste håndtererApplikasjonTjeneste;
//
//    private OperereKalkulusRestTjeneste restTjeneste;
//
//    @BeforeEach
//    public void before() {
//        restTjeneste = new OperereKalkulusRestTjeneste(koblingTjeneste, beregningStegTjeneste, kalkulatorInputTjeneste, håndtererApplikasjonTjeneste);
//    }
//
//    @Test
//    void skal_starte_beregning() {
//        //arrange
//        String saksnummer = "1234";
//        UUID randomUUID = UUID.randomUUID();
//        AktørIdPersonident dummy = AktørIdPersonident.dummy();
//        Periode periode = new Periode(LocalDate.now(), LocalDate.now().plusMonths(2));
//        Organisasjon organisasjon = new Organisasjon("945748931");
//
//        List<GrunnbeløpDto> grunnbeløpDtos = List.of(new GrunnbeløpDto(periode, BigDecimal.valueOf(100000), BigDecimal.valueOf(100000)));
//        InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlagDto = new InntektArbeidYtelseGrunnlagDto();
//        OpptjeningAktiviteterDto opptjeningAktiviteterDto = new OpptjeningAktiviteterDto(List.of(new OpptjeningPeriodeDto(OpptjeningAktivitetType.ARBEID, periode, organisasjon, new InternArbeidsforholdRefDto("Dummy"))));
//        LocalDate skjæringstidspunkt = LocalDate.now();
//
//        KalkulatorInputDto kalkulatorInputDto = new KalkulatorInputDto(grunnbeløpDtos, inntektArbeidYtelseGrunnlagDto, opptjeningAktiviteterDto, skjæringstidspunkt);
//        StartBeregningRequest spesifikasjon = new StartBeregningRequest(randomUUID, saksnummer, dummy, YtelseTyperKalkulusStøtterKontrakt.PLEIEPENGER_SYKT_BARN, kalkulatorInputDto);
//
//        //act
//        Response beregn = restTjeneste.beregn(spesifikasjon);
//
//        //assert
//        TilstandResponse tilstandResponse = (TilstandResponse) beregn.getEntity();
//        assertThat(tilstandResponse.getAksjonspunktMedTilstandDto()).isEmpty();
//    }
}
