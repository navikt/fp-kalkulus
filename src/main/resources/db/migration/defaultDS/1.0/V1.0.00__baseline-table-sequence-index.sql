create sequence seq_global_pk
    minvalue 1000000
    increment by 50;

create table beregningsgrunnlag
(
    id                  bigint                                       not null
        constraint pk_beregningsgrunnlag
            primary key,
    skjaeringstidspunkt timestamp(0)                                 not null,
    grunnbeloep         numeric(12, 2),
    overstyrt           boolean      default false                   not null,
    opprettet_av        varchar(20)  default 'VL'::character varying not null,
    opprettet_tid       timestamp(3) default CURRENT_TIMESTAMP       not null,
    versjon             int          default 0                       not null,
    endret_av           varchar(20),
    endret_tid          timestamp(3)
);

comment on table beregningsgrunnlag is 'Aggregat for beregningsgrunnlag';

comment on column beregningsgrunnlag.id is 'Primærnøkkel';

comment on column beregningsgrunnlag.skjaeringstidspunkt is 'Skjæringstidspunkt for beregning';

comment on column beregningsgrunnlag.grunnbeloep is 'Grunnbeløp (G) ved opprinnelig_skjæringstidspunkt';

comment on column beregningsgrunnlag.overstyrt is 'Oppgir om beregningsgrunnlaget er overstyrt ved faktaavklaring';

create index idx_beregningsgrunnlag_02
    on beregningsgrunnlag (skjaeringstidspunkt);

create table beregningsgrunnlag_periode
(
    id                               bigint                                       not null
        constraint pk_beregningsgrunnlag_periode
            primary key,
    beregningsgrunnlag_id            bigint                                       not null
        constraint fk_beregningsgrunnlag_periode_1
            references beregningsgrunnlag,
    periode_fom                      DATE                                         not null,
    periode_tom                      DATE,
    brutto_pr_aar                    numeric(19, 2),
    avkortet_pr_aar                  numeric(19, 2),
    redusert_pr_aar                  numeric(19, 2),
    dagsats                          bigint,
    inntekt_graderingsprosent_brutto numeric(19, 2),
    total_utbetalingsgrad_fra_uttak  numeric(19, 4),
    opprettet_av                     varchar(20)  default 'VL'::character varying not null,
    opprettet_tid                    timestamp(3) default CURRENT_TIMESTAMP       not null,
    versjon                          int          default 0                       not null,
    endret_av                        varchar(20),
    endret_tid                       timestamp(3)
);

comment on table beregningsgrunnlag_periode is 'Beregningsgrunnlagsperiode';

comment on column beregningsgrunnlag_periode.id is 'Primærnøkkel';

comment on column beregningsgrunnlag_periode.beregningsgrunnlag_id is 'Fremmednøkkel til tabell som knytter beregningsgrunnlagsperioden til et beregningsgrunnlag';

comment on column beregningsgrunnlag_periode.periode_fom is 'Første gyldighetsdag for beregningsgrunnlag';

comment on column beregningsgrunnlag_periode.periode_tom is 'Siste gyldighetsdag for beregningsgrunnlag';

comment on column beregningsgrunnlag_periode.brutto_pr_aar is 'Beregningsgrunnlag, brutto';

comment on column beregningsgrunnlag_periode.avkortet_pr_aar is 'Avkortet beregningsgrunnlag';

comment on column beregningsgrunnlag_periode.redusert_pr_aar is 'Beregningsgrunnlag, redusert';

comment on column beregningsgrunnlag_periode.dagsats is 'Dagsats, avrundet';

comment on column beregningsgrunnlag_periode.inntekt_graderingsprosent_brutto is 'Graderingsprosent ved gradering mot inntekt. Angir totalt bortfalt inntekt av totalt brutto beregningsgrunnlag.';

comment on column beregningsgrunnlag_periode.total_utbetalingsgrad_fra_uttak is 'Total utbetalingsgrad fra uttak. Utregnet separat fra reduksjon ved tilkommet inntekt.';

create index idx_beregningsgrunnlag_periode_01
    on beregningsgrunnlag_periode (beregningsgrunnlag_id);

create index idx_beregningsgrunnlag_periode_02
    on beregningsgrunnlag_periode (periode_fom);

create index idx_beregningsgrunnlag_periode_03
    on beregningsgrunnlag_periode (periode_tom);

create table beregningsgrunnlag_aktivitet_status
(
    id                    bigint                                       not null
        constraint pk_beregningsgrunnlag_aktivitet_status
            primary key,
    beregningsgrunnlag_id bigint                                       not null
        constraint fk_beregningsgrunnlag_aktivitet_status_1
            references beregningsgrunnlag,
    aktivitet_status      varchar(50)                                  not null,
    hjemmel               varchar(20)  default '-'::character varying  not null,
    opprettet_av          varchar(20)  default 'VL'::character varying not null,
    opprettet_tid         timestamp(3) default CURRENT_TIMESTAMP       not null,
    versjon               int          default 0                       not null,
    endret_av             varchar(20),
    endret_tid            timestamp(3)
);

comment on table beregningsgrunnlag_aktivitet_status is 'Aktivitetsstatus i beregningsgrunnlag';

comment on column beregningsgrunnlag_aktivitet_status.beregningsgrunnlag_id is 'Fremmednøkkel til tabell som knytter beregningsgrunnlagsaktivitetstatusen til et beregningsgrunnlag';

comment on column beregningsgrunnlag_aktivitet_status.aktivitet_status is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';

comment on column beregningsgrunnlag_aktivitet_status.hjemmel is 'Hjemmel for beregningsgrunnlag';

create index idx_beregningsgrunnlag_aktivitet_status_01
    on beregningsgrunnlag_aktivitet_status (beregningsgrunnlag_id);

create index idx_beregningsgrunnlag_aktivitet_status_02
    on beregningsgrunnlag_aktivitet_status (aktivitet_status);

create index idx_beregningsgrunnlag_aktivitet_status_03
    on beregningsgrunnlag_aktivitet_status (hjemmel);

create table aktiviteter
(
    id                             bigint                                       not null
        constraint pk_aktiviteter
            primary key,
    skjaeringstidspunkt_opptjening DATE                                         not null,
    opprettet_av                   varchar(20)  default 'VL'::character varying not null,
    opprettet_tid                  timestamp(3) default CURRENT_TIMESTAMP       not null,
    versjon                        int          default 0                       not null,
    endret_av                      varchar(20),
    endret_tid                     timestamp(3)
);

comment on table aktiviteter is 'Tabell som knytter AKTIVITET til GR_BEREGNINGSGRUNNLAG';

comment on column aktiviteter.skjaeringstidspunkt_opptjening is 'Skjæringstidspunkt for opptjening';

create table aktivitet
(
    id                        bigint                                       not null
        constraint pk_aktivitet
            primary key,
    fom                       timestamp(0)                                 not null,
    tom                       timestamp(0)                                 not null,
    aktiviteter_id            bigint                                       not null
        constraint fk_aktivitet_01
            references aktiviteter,
    opptjening_aktivitet_type varchar(50)                                  not null,
    arbeidsgiver_aktoer_id    varchar(13),
    arbeidsgiver_orgnr        varchar(9),
    overstyr_handling_type    varchar(20),
    arbeidsforhold_intern_id  uuid,
    opprettet_av              varchar(20)  default 'VL'::character varying not null,
    opprettet_tid             timestamp(3) default CURRENT_TIMESTAMP       not null,
    versjon                   int          default 0                       not null,
    endret_av                 varchar(20),
    endret_tid                timestamp(3)
);

comment on table aktivitet is 'Aktivitet som er relevant for beregning';

comment on column aktivitet.id is 'Primary Key';

comment on column aktivitet.fom is 'Aktiviteter relevant for beregning etter saksbehandlers vurdering';

comment on column aktivitet.opptjening_aktivitet_type is 'Type aktivitet som har inngått i vurdering av opptjening';

comment on column aktivitet.arbeidsgiver_aktoer_id is 'Arbeidsgivers aktør_id';

comment on column aktivitet.overstyr_handling_type is 'Hvis denne er satt beskriver den hva slags overstyringshandling som er gjort for denne aktiviteten';

comment on column aktivitet.arbeidsgiver_orgnr is 'Organisasjonsnummer for arbeidsgivere som er virksomheter';

comment on column aktivitet.arbeidsforhold_intern_id is 'Globalt unikt arbeidsforhold id generert for arbeidsgiver/arbeidsforhold. I motsetning til arbeidsforhold_ekstern_id som holder arbeidsgivers referanse';

create index idx_aktivitet_01
    on aktivitet (aktiviteter_id);

create index idx_aktivitet_04
    on aktivitet (arbeidsgiver_orgnr);

create index idx_aktivitet_11
    on aktivitet (arbeidsforhold_intern_id);

create table fakta_tilfelle
(
    id                       bigint                                       not null
        constraint pk_fakta_tilfelle
            primary key,
    fakta_beregning_tilfelle varchar(100)                                 not null,
    beregningsgrunnlag_id    bigint                                       not null
        constraint fk_fakta_tilfelle_1
            references beregningsgrunnlag,
    versjon                  int          default 0                       not null,
    opprettet_av             varchar(20)  default 'VL'::character varying not null,
    opprettet_tid            timestamp(3) default CURRENT_TIMESTAMP       not null,
    endret_av                varchar(20),
    endret_tid               timestamp(3)
);

comment on table fakta_tilfelle is 'Eit fakta om beregning tilfelle for eit beregningsgrunnlag';

comment on column fakta_tilfelle.fakta_beregning_tilfelle is 'FK: FAKTA_OM_BEREGNING_TILFELLE';

comment on column fakta_tilfelle.beregningsgrunnlag_id is 'FK: BEREGNINGSGRUNNLAG';

create index idx_fakta_ber_tilfelle_1
    on fakta_tilfelle (beregningsgrunnlag_id);

create table periode_aarsak
(
    id                            bigint                                       not null
        constraint pk_periode_aarsak
            primary key,
    periode_aarsak                varchar(50)                                  not null,
    beregningsgrunnlag_periode_id bigint                                       not null
        constraint fk_beregningsgrunnlag_periode_aarsak_1
            references beregningsgrunnlag_periode,
    versjon                       int          default 0                       not null,
    opprettet_av                  varchar(20)  default 'VL'::character varying not null,
    opprettet_tid                 timestamp(3) default CURRENT_TIMESTAMP       not null,
    endret_av                     varchar(20),
    endret_tid                    timestamp(3)
);

comment on table periode_aarsak is 'Periodeårsaker for splitting av perioder i beregningsgrunnlag';

comment on column periode_aarsak.id is 'Primary Key';

comment on column periode_aarsak.periode_aarsak is 'Årsak til splitting av periode';

create index idx_periode_aarsak_1
    on periode_aarsak (beregningsgrunnlag_periode_id);

create table beregningsgrunnlag_andel
(
    id                                 bigint                                                  not null
        constraint pk_beregningsgrunnlag_andel
            primary key,
    beregningsgrunnlag_periode_id      bigint                                                  not null
        constraint fk_beregningsgrunnlag_andel_1
            references beregningsgrunnlag_periode,
    aktivitet_status                   varchar(50)                                             not null,
    beregningsperiode_fom              DATE,
    beregningsperiode_tom              DATE,
    brutto_pr_aar                      numeric(19, 2),
    overstyrt_pr_aar                   numeric(19, 2),
    avkortet_pr_aar                    numeric(19, 2),
    redusert_pr_aar                    numeric(19, 2),
    beregnet_pr_aar                    numeric(19, 2),
    maksimal_refusjon_pr_aar           numeric(19, 2),
    avkortet_refusjon_pr_aar           numeric(19, 2),
    redusert_refusjon_pr_aar           numeric(19, 2),
    avkortet_brukers_andel_pr_aar      numeric(19, 2),
    redusert_brukers_andel_pr_aar      numeric(19, 2),
    dagsats_bruker                     bigint,
    dagsats_arbeidsgiver               bigint,
    pgi_snitt                          bigint,
    pgi1                               bigint,
    pgi2                               bigint,
    pgi3                               bigint,
    aarsbeloep_tilstoetende_ytelse     bigint,
    inntektskategori                   varchar(50)                                             not null,
    andelsnr                           bigint                                                  not null,
    arbeidsforhold_type                varchar(50)  default '-'::character varying             not null,
    besteberegning_pr_aar              numeric(19, 2),
    lagt_til_av_saksbehandler          boolean      default false                              not null,
    dagsats_tilstoetende_ytelse        bigint,
    fordelt_pr_aar                     numeric(19, 2),
    fastsatt_av_saksbehandler          boolean,
    kilde                              varchar(50)  default 'PROSESS_START'::character varying not null,
    avkortet_foer_gradering_pr_aar     numeric(19, 2),
    manuelt_fordelt_pr_aar             numeric(19, 2),
    inntektskategori_manuell_fordeling varchar(50),
    inntektskategori_fordeling         varchar(50),
    opprettet_av                       varchar(20)  default 'VL'::character varying            not null,
    opprettet_tid                      timestamp(3) default CURRENT_TIMESTAMP                  not null,
    versjon                            int          default 0                                  not null,
    endret_av                          varchar(20),
    endret_tid                         timestamp(3)
);

comment on table beregningsgrunnlag_andel is 'Beregningsgrunnlag pr status og andel';

comment on column beregningsgrunnlag_andel.id is 'Primærnøkkel';

comment on column beregningsgrunnlag_andel.beregningsgrunnlag_periode_id is 'FK: Fremmednøkkel til tabell som knytter beregningsgrunnlagsandelen til en beregningsgrunnlagperiode';

comment on column beregningsgrunnlag_andel.aktivitet_status is 'Andelens aktivitetstatus';

comment on column beregningsgrunnlag_andel.beregningsperiode_fom is 'Første dag i beregningsperiode';

comment on column beregningsgrunnlag_andel.beregningsperiode_tom is 'Siste dag i beregningsperiode';

comment on column beregningsgrunnlag_andel.brutto_pr_aar is 'Beregningsgrunnlagsandel, brutto';

comment on column beregningsgrunnlag_andel.overstyrt_pr_aar is 'Beregningsgrunnlagsandel, overstyrt';

comment on column beregningsgrunnlag_andel.avkortet_pr_aar is 'Beregningsgrunnlagsandel, avkortet';

comment on column beregningsgrunnlag_andel.redusert_pr_aar is 'Beregningsgrunnlag, redusert';

comment on column beregningsgrunnlag_andel.beregnet_pr_aar is 'Beregningsgrunnlagsandel, beregnet';

comment on column beregningsgrunnlag_andel.maksimal_refusjon_pr_aar is 'Maksimalverdi for refusjon til arbeidsgiver';

comment on column beregningsgrunnlag_andel.avkortet_refusjon_pr_aar is 'Refusjon til arbeidsgiver, avkortet';

comment on column beregningsgrunnlag_andel.redusert_refusjon_pr_aar is 'Refusjon til arbeidsgiver, redusert';

comment on column beregningsgrunnlag_andel.avkortet_brukers_andel_pr_aar is 'Brukers andel, avkortet';

comment on column beregningsgrunnlag_andel.redusert_brukers_andel_pr_aar is 'Brukers andel, redusert';

comment on column beregningsgrunnlag_andel.dagsats_bruker is 'Dagsats til bruker';

comment on column beregningsgrunnlag_andel.dagsats_arbeidsgiver is 'Dagsats til arbeidsgiver';

comment on column beregningsgrunnlag_andel.pgi_snitt is 'Gjennomsnittlig pensjonsgivende inntekt';

comment on column beregningsgrunnlag_andel.pgi1 is 'Pensjonsgivende inntekt i år 1';

comment on column beregningsgrunnlag_andel.pgi2 is 'Pensjonsgivende inntekt i år 2';

comment on column beregningsgrunnlag_andel.pgi3 is 'Pensjonsgivende inntekt i år 3';

comment on column beregningsgrunnlag_andel.aarsbeloep_tilstoetende_ytelse is 'Årsbeløp for tilstøtende ytelse';

comment on column beregningsgrunnlag_andel.inntektskategori is 'Andelens inntektskategori';

comment on column beregningsgrunnlag_andel.andelsnr is 'Nummer for å identifisere andel innanfor ein periode';

comment on column beregningsgrunnlag_andel.arbeidsforhold_type is 'Typekode for arbeidstakeraktivitet som ikke er tilknyttet noen virksomhet';

comment on column beregningsgrunnlag_andel.besteberegning_pr_aar is 'Inntekt fastsatt av saksbehandler ved besteberegning for fødende kvinne';

comment on column beregningsgrunnlag_andel.lagt_til_av_saksbehandler is 'Angir om andel er lagt til av saksbehandler manuelt';

comment on column beregningsgrunnlag_andel.dagsats_tilstoetende_ytelse is 'Original dagsats fra tilstøtende ytelse AAP/Dagpenger';

comment on column beregningsgrunnlag_andel.fordelt_pr_aar is 'Beregningsgrunnlagsandel etter fordeling';

comment on column beregningsgrunnlag_andel.kilde is 'Angir kilde/opphav for andel';

comment on column beregningsgrunnlag_andel.avkortet_foer_gradering_pr_aar is 'Beløp etter avkorting før gradering mot utbetalingsgrad';

comment on column beregningsgrunnlag_andel.manuelt_fordelt_pr_aar is 'Manuelt fordelt beregningsgrunnlag.';

comment on column beregningsgrunnlag_andel.inntektskategori_manuell_fordeling is 'Inntektskategori satt ved manuell fordeling.';

comment on column beregningsgrunnlag_andel.inntektskategori_fordeling is 'Inntektskategori satt ved automatisk fordeling.';

comment on column beregningsgrunnlag_andel.fastsatt_av_saksbehandler is 'Er andel fastsatt av saksbehandler.';

create table andel_arbeidsforhold
(
    id                              bigint                                       not null
        constraint pk_andel_arbeidsforhold
            primary key,
    beregningsgrunnlag_andel_id     bigint                                       not null
        constraint fk_andel_arbeidsforhold_1
            references beregningsgrunnlag_andel,
    refusjonskrav_pr_aar            numeric(19, 2),
    naturalytelse_bortfalt_pr_aar   numeric(19, 2),
    naturalytelse_tilkommet_pr_aar  numeric(19, 2),
    arbeidsperiode_fom              DATE,
    arbeidsperiode_tom              DATE,
    arbeidsgiver_aktoer_id          varchar(13),
    arbeidsgiver_orgnr              varchar(9),
    arbeidsforhold_intern_id        uuid,
    hjemmel_for_refusjonskravfrist  varchar(20),
    saksbehandlet_refusjon_pr_aar   numeric(19, 2),
    fordelt_refusjon_pr_aar         numeric(19, 2),
    refusjonskrav_frist_utfall      varchar(20),
    manuelt_fordelt_refusjon_pr_aar numeric(19, 2),
    opprettet_av                    varchar(20)  default 'VL'::character varying not null,
    opprettet_tid                   timestamp(3) default CURRENT_TIMESTAMP       not null,
    versjon                         int          default 0                       not null,
    endret_av                       varchar(20),
    endret_tid                      timestamp(3)
);

comment on table andel_arbeidsforhold is 'Informasjon om arbeidsforholdet knyttet til beregningsgrunnlagandelen';

comment on column andel_arbeidsforhold.beregningsgrunnlag_andel_id is 'Andelen arbeidsforholdet er knyttet til';

comment on column andel_arbeidsforhold.refusjonskrav_pr_aar is 'Arbeidsgivers refusjonskrav';

comment on column andel_arbeidsforhold.naturalytelse_bortfalt_pr_aar is 'Verdi av bortfalt naturalytelse';

comment on column andel_arbeidsforhold.naturalytelse_tilkommet_pr_aar is 'Verdi av tilkommet naturalytelse';

comment on column andel_arbeidsforhold.arbeidsperiode_fom is 'Fra og med dato arbeidsperiode';

comment on column andel_arbeidsforhold.arbeidsperiode_tom is 'Til og med dato arbeidsperiode';

comment on column andel_arbeidsforhold.arbeidsgiver_aktoer_id is 'Arbeidsgivers aktør id.';

comment on column andel_arbeidsforhold.arbeidsgiver_orgnr is 'Organisasjonsnummer for arbeidsgivere som er virksomheter';

comment on column andel_arbeidsforhold.arbeidsforhold_intern_id is 'Globalt unikt arbeidsforhold id generert for arbeidsgiver/arbeidsforhold. I motsetning til arbeidsforhold_ekstern_id som holder arbeidsgivers referanse';

comment on column andel_arbeidsforhold.hjemmel_for_refusjonskravfrist is 'Hjemmel for refusjonskrav frist.';

comment on column andel_arbeidsforhold.saksbehandlet_refusjon_pr_aar is 'Refusjonsbeløp satt som følge av å ha vurdert refusjonskravet og refusjonsbeløpet';

comment on column andel_arbeidsforhold.fordelt_refusjon_pr_aar is 'Refusjonsbeløp satt i henhold til fordelingsregler';

comment on column andel_arbeidsforhold.refusjonskrav_frist_utfall is 'Utfall for vurdering av frist for refusjonskrav';

comment on column andel_arbeidsforhold.manuelt_fordelt_refusjon_pr_aar is 'Refusjonsbeløp satt av saksbehandler i fordeling.';

create index idx_andel_arbeidsforhold_01
    on andel_arbeidsforhold (beregningsgrunnlag_andel_id);

create index idx_andel_arbeidsforhold_03
    on andel_arbeidsforhold (arbeidsgiver_orgnr);

create index idx_andel_arbeidsforhold_11
    on andel_arbeidsforhold (arbeidsforhold_intern_id);

create index idx_andel_01
    on beregningsgrunnlag_andel (beregningsgrunnlag_periode_id);

create index idx_andel_02
    on beregningsgrunnlag_andel (aktivitet_status);

create table refusjon_overstyringer
(
    id            bigint                                       not null
        constraint pk_refusjon_overstyringer
            primary key,
    versjon       int          default 0                       not null,
    opprettet_av  varchar(20)  default 'VL'::character varying not null,
    opprettet_tid timestamp(3) default CURRENT_TIMESTAMP       not null,
    endret_av     varchar(20),
    endret_tid    timestamp(3)
);

comment on table refusjon_overstyringer is 'Tabell som knytter REFUSJON_OVERSTYRING til GR_BEREGNINGSGRUNNLAG';

create table refusjon_overstyring
(
    id                        bigint                                       not null
        constraint pk_refusjon_overstyring
            primary key,
    refusjon_overstyringer_id bigint                                       not null
        constraint fk_refusjon_overstyringer_01
            references refusjon_overstyringer,
    arbeidsgiver_orgnr        varchar(9),
    arbeidsgiver_aktoer_id    varchar(13),
    fom                       DATE,
    er_frist_utvidet          boolean,
    opprettet_av              varchar(20)  default 'VL'::character varying not null,
    opprettet_tid             timestamp(3) default CURRENT_TIMESTAMP       not null,
    versjon                   int          default 0                       not null,
    endret_av                 varchar(20),
    endret_tid                timestamp(3)
);

comment on table refusjon_overstyring is 'Overstyringer av aktiviteter som er relevant for beregning';

comment on column refusjon_overstyring.id is 'Primary Key';

comment on column refusjon_overstyring.refusjon_overstyringer_id is 'Arbeidsgivers orgnr';

comment on column refusjon_overstyring.arbeidsgiver_orgnr is 'Arbeidsgivers orgnr';

comment on column refusjon_overstyring.arbeidsgiver_aktoer_id is 'Arbeidsgivers aktør_id';

comment on column refusjon_overstyring.er_frist_utvidet is 'Er frist for refusjonskrav utvidet.';

create index idx_refusjon_overstyring_01
    on refusjon_overstyring (refusjon_overstyringer_id);

create table sammenligningsgrunnlag_pr_status
(
    id                          bigint                                       not null
        constraint pk_sammenligningsgrunnlag_pr_status
            primary key,
    beregningsgrunnlag_id       bigint                                       not null
        constraint fk_sammenligningsgrunnlag_pr_status_01
            references beregningsgrunnlag,
    sammenligningsgrunnlag_type varchar(50)                                  not null,
    sammenligningsperiode_fom   DATE                                         not null,
    sammenligningsperiode_tom   DATE                                         not null,
    rapportert_pr_aar           numeric(19, 2)                               not null,
    avvik_promille              numeric(27, 10)                              not null,
    opprettet_av                varchar(20)  default 'VL'::character varying not null,
    opprettet_tid               timestamp(3) default CURRENT_TIMESTAMP       not null,
    versjon                     int          default 0                       not null,
    endret_av                   varchar(20),
    endret_tid                  timestamp(3)
);

comment on table sammenligningsgrunnlag_pr_status is 'Sammenligningsgrunnlag pr status';

comment on column sammenligningsgrunnlag_pr_status.beregningsgrunnlag_id is 'FK: BEREGNINGSGRUNNLAG';

comment on column sammenligningsgrunnlag_pr_status.sammenligningsgrunnlag_type is 'Type av sammenligningsgrunnlag';

comment on column sammenligningsgrunnlag_pr_status.sammenligningsperiode_fom is 'Fom-dato for sammenligningsperiode';

comment on column sammenligningsgrunnlag_pr_status.sammenligningsperiode_tom is 'Tom-dato for sammenligningsperiode';

comment on column sammenligningsgrunnlag_pr_status.rapportert_pr_aar is 'Rapportert inntekt pr aar i for gitt status';

comment on column sammenligningsgrunnlag_pr_status.avvik_promille is 'Avvik promille';

create index idx_sammenligningsgrunnlag_pr_status_01
    on sammenligningsgrunnlag_pr_status (beregningsgrunnlag_id);

create table kobling
(
    id                         bigint                                       not null
        constraint pk_kobling
            primary key,
    kobling_referanse          uuid                                         not null
        constraint uidx_kobling_1
            unique,
    original_kobling_referanse uuid,
    ytelse_type                varchar(20)                                  not null,
    bruker_aktoer_id           varchar(13)                                  not null,
    saksnummer                 varchar(19)                                  not null,
    er_avsluttet               boolean      default false                   not null,
    versjon                    int          default 0                       not null,
    opprettet_av               varchar(20)  default 'VL'::character varying not null,
    opprettet_tid              timestamp(3) default LOCALTIMESTAMP          not null,
    endret_av                  varchar(20),
    endret_tid                 timestamp(3)
);

comment on table kobling is 'Holder referansen som kalles på fra av eksternt system';

comment on column kobling.id is 'Primærnøkkel';

comment on column kobling.kobling_referanse is 'Referansenøkkel som eksponeres lokalt';

comment on column kobling.ytelse_type is 'Hvilken ytelse komplekset henger under';

comment on column kobling.bruker_aktoer_id is 'Aktøren koblingen gjelder for';

comment on column kobling.saksnummer is 'Saksnummer til saken koblingen gjelder for';

comment on column kobling.original_kobling_referanse is 'Referanse til original kobling hvis koblingen er basert på en annen';

comment on column kobling.er_avsluttet is 'Markerer om koblingen tilhører en behandling der vedtak er fattet og derfor ikke skal kunne endres';

create index idx_kobling_1
    on kobling (kobling_referanse);

create index idx_kobling_2
    on kobling (saksnummer);

create index idx_kobling_3
    on kobling (ytelse_type);

create table br_sats
(
    id            bigint                                       not null
        constraint pk_sats
            primary key,
    sats_type     varchar(20)                                  not null,
    fom           date                                         not null,
    tom           date                                         not null,
    verdi         numeric(10)                                  not null,
    versjon       int          default 0                       not null,
    opprettet_av  varchar(20)  default 'VL'::character varying not null,
    opprettet_tid timestamp(3) default CURRENT_TIMESTAMP       not null,
    endret_av     varchar(20),
    endret_tid    timestamp(3)
);

comment on table br_sats is 'Satser brukt ifm beregning av ytelser';

comment on column br_sats.id is 'Primary Key';

comment on column br_sats.sats_type is 'Beskrivelse av satstype';

comment on column br_sats.fom is 'Gyldig Fra-Og-Med';

comment on column br_sats.tom is 'Gyldig Til-Og-Med';

comment on column br_sats.verdi is 'Sats verdi.';

create table refusjon_periode
(
    id                       bigint                                       not null
        constraint pk_refusjon_periode
            primary key,
    refusjon_overstyring_id  bigint                                       not null
        constraint fk_refusjon_periode_refusjon_overstyring_id
            references refusjon_overstyring,
    arbeidsforhold_intern_id uuid,
    fom                      date                                         not null,
    versjon                  int          default 0                       not null,
    opprettet_av             varchar(20)  default 'VL'::character varying not null,
    opprettet_tid            timestamp(3) default CURRENT_TIMESTAMP       not null,
    endret_av                varchar(20),
    endret_tid               timestamp(3)
);

comment on table refusjon_periode is 'Tabell som holder på hvilke refusjonskrav som skal gjelde fra hvilken dato gitt arbeidsgiver og arbeidsforhold';

comment on column refusjon_periode.refusjon_overstyring_id is 'Foreign key til tabell REFUSJON_OVERSTYRING';

comment on column refusjon_periode.arbeidsforhold_intern_id is 'Globalt unikt arbeidsforhold id generert for arbeidsgiver/arbeidsforhold. I motsetning til arbeidsforhold_ekstern_id som holder arbeidsgivers referanse';

comment on column refusjon_periode.fom is 'Fra og med datoen refusjon skal tas med i beregningen';

create index idx_refusjon_periode_1
    on refusjon_periode (refusjon_overstyring_id);

create table regel_sporing_grunnlag
(
    id                    bigint                                       not null
        constraint pk_regel_sporing_grunnlag
            primary key,
    kobling_id            bigint                                       not null
        constraint fk_regel_sporing_grunnlag_01
            references kobling,
    regel_type            varchar(30)                                  not null,
    aktiv                 boolean      default false                   not null,
    regel_evaluering_json text,
    regel_input_json      text,
    regel_versjon         varchar(20),
    opprettet_av          varchar(20)  default 'VL'::character varying not null,
    opprettet_tid         timestamp(3) default CURRENT_TIMESTAMP       not null,
    versjon               int          default 0                       not null,
    endret_av             varchar(20),
    endret_tid            timestamp(3)
);

comment on table regel_sporing_grunnlag is 'Tabell som lagrer regelsporinger for beregningsgrunnlag';

comment on column regel_sporing_grunnlag.id is 'Primary Key';

comment on column regel_sporing_grunnlag.kobling_id is 'FK: Referanse til kobling';

comment on column regel_sporing_grunnlag.regel_type is 'Hvilken regel det gjelder';

comment on column regel_sporing_grunnlag.aktiv is 'Sier om sporingen er aktiv';

comment on column regel_sporing_grunnlag.regel_evaluering_json is 'Regelevaluering/logging i json-format';

comment on column regel_sporing_grunnlag.regel_input_json is 'Input til regelen i json-format';

comment on column regel_sporing_grunnlag.regel_versjon is 'Versjon av beregningsreglene som er brukt';

create index idx_rs_gr_01
    on regel_sporing_grunnlag (kobling_id);

create index idx_rs_gr_02
    on regel_sporing_grunnlag (regel_type);

create table fakta_aggregat
(
    id            bigint                                       not null
        constraint pk_fakta_aggregat
            primary key,
    versjon       int          default 0                       not null,
    opprettet_av  varchar(20)  default 'VL'::character varying not null,
    opprettet_tid timestamp(3) default CURRENT_TIMESTAMP       not null,
    endret_av     varchar(20),
    endret_tid    timestamp(3)
);

comment on table fakta_aggregat is 'Tabell som lagrer faktaavklaringer for beregningsgrunnlag';

comment on column fakta_aggregat.id is 'Primary Key';

create table gr_beregningsgrunnlag
(
    id                           bigint                                       not null
        constraint pk_gr_beregningsgrunnlag
            primary key,
    kobling_id                   bigint                                       not null
        constraint fk_gr_beregningsgrunnlag_7
            references kobling,
    beregningsgrunnlag_id        bigint
        constraint fk_gr_beregningsgrunnlag_2
            references beregningsgrunnlag,
    steg_opprettet               varchar(50)  default '-'::character varying  not null,
    aktiv                        boolean      default false                   not null,
    register_aktiviteter_id      bigint                                       not null
        constraint fk_gr_beregningsgrunnlag_3
            references aktiviteter,
    saksbehandlet_aktiviteter_id bigint
        constraint fk_gr_beregningsgrunnlag_4
            references aktiviteter,
    overstyrte_aktiviteter_id   bigint
        constraint fk_gr_beregningsgrunnlag_5
            references aktiviteter,
    refusjon_overstyringer_id    bigint
        constraint fk_gr_beregningsgrunnlag_6
            references refusjon_overstyringer,
    grunnlag_referanse           uuid                                         not null,
    fakta_aggregat_id            bigint
        constraint fk_gr_beregningsgrunnlag_08
            references fakta_aggregat,
    opprettet_av                 varchar(20)  default 'VL'::character varying not null,
    opprettet_tid                timestamp(3) default CURRENT_TIMESTAMP       not null,
    versjon                      int          default 0                       not null,
    endret_av                    varchar(20),
    endret_tid                   timestamp(3)
);

comment on table gr_beregningsgrunnlag is 'Tabell som kobler et beregningsgrunnlag til koblingen';

comment on column gr_beregningsgrunnlag.id is 'Primary Key';

comment on column gr_beregningsgrunnlag.kobling_id is 'FK: KOBLING Fremmednøkkel til koblingen som forbindes med beregningsgrunnlaget';

comment on column gr_beregningsgrunnlag.beregningsgrunnlag_id is 'FK:BEREGNINGSGRUNNLAG Fremmednøkkel til tabell som knytter beregningsgrunnlagforekomsten til koblingen';

comment on column gr_beregningsgrunnlag.steg_opprettet is 'Hvilket steg eller vurderingspunkt grunnlaget ble opprettet i';

comment on column gr_beregningsgrunnlag.register_aktiviteter_id is 'Aktiviteter relevant for beregning før saksbehandlers vurdering';

comment on column gr_beregningsgrunnlag.overstyrte_aktiviteter_id is 'Overstyringer av beregningaktiviteter';

comment on column gr_beregningsgrunnlag.refusjon_overstyringer_id is 'Overstyringer av refusjon';

comment on column gr_beregningsgrunnlag.fakta_aggregat_id is 'Foreign Key til faktaavklaringer';

comment on column gr_beregningsgrunnlag.grunnlag_referanse is 'UUID: referansen til grunnlaget.';

create index idx_gr_beregningsgrunnlag_02
    on gr_beregningsgrunnlag (register_aktiviteter_id);

create index idx_gr_beregningsgrunnlag_03
    on gr_beregningsgrunnlag (saksbehandlet_aktiviteter_id);

create index idx_gr_beregningsgrunnlag_04
    on gr_beregningsgrunnlag (overstyrte_aktiviteter_id);

create index idx_gr_beregningsgrunnlag_05
    on gr_beregningsgrunnlag (refusjon_overstyringer_id);

create index idx_gr_beregningsgrunnlag_6
    on gr_beregningsgrunnlag (kobling_id);

create index idx_gr_beregningsgrunnlag_7
    on gr_beregningsgrunnlag (beregningsgrunnlag_id);

create index idx_gr_beregningsgrunnlag_08
    on gr_beregningsgrunnlag (fakta_aggregat_id);

create index idx_gr_beregningsgrunnlag_09
    on gr_beregningsgrunnlag (steg_opprettet);

create index idx_gr_beregningsgrunnlag_10
    on gr_beregningsgrunnlag (opprettet_tid);

create index idx_gr_beregningsgrunnlag_11
    on gr_beregningsgrunnlag (kobling_id, steg_opprettet, opprettet_tid);

create index idx_gr_beregningsgrunnlag_12
    on gr_beregningsgrunnlag (kobling_id, steg_opprettet);

create table fakta_arbeidsforhold
(
    id                                           bigint                                       not null
        constraint pk_fakta_arbeidsforhold
            primary key,
    fakta_aggregat_id                            bigint                                       not null
        constraint fk_fakta_arbeidsforhold_01
            references fakta_aggregat,
    er_tidsbegrenset                             boolean,
    har_mottatt_ytelse                           boolean,
    arbeidsforhold_intern_id                     uuid,
    arbeidsgiver_orgnr                           varchar(9),
    arbeidsgiver_aktoer_id                       varchar(13),
    har_loennsendring_i_beregningsperioden       boolean,
    er_tidsbegrenset_kilde                       varchar(20),
    har_mottatt_ytelse_kilde                     varchar(20),
    har_loennsendring_i_beregningsperioden_kilde varchar(20),
    opprettet_av                                 varchar(20)  default 'VL'::character varying not null,
    opprettet_tid                                timestamp(3) default CURRENT_TIMESTAMP       not null,
    versjon                                      int          default 0                       not null,
    endret_av                                    varchar(20),
    endret_tid                                   timestamp(3)
);

comment on table fakta_arbeidsforhold is 'Tabell som lagrer faktaavklaringer for arbeidsforhold';

comment on column fakta_arbeidsforhold.id is 'Primary Key';

comment on column fakta_arbeidsforhold.fakta_aggregat_id is 'Foreign Key til faktaavklaringer';

comment on column fakta_arbeidsforhold.er_tidsbegrenset is 'Er arbeidsforhold tidsbegrenset';

comment on column fakta_arbeidsforhold.har_mottatt_ytelse is 'Er det tidligere mottatt ytelse for arbeidsforholdet';

comment on column fakta_arbeidsforhold.arbeidsforhold_intern_id is 'Intern id til arbeidsforholdet';

comment on column fakta_arbeidsforhold.arbeidsgiver_aktoer_id is 'Aktør id til arbeidsgiveren.';

comment on column fakta_arbeidsforhold.arbeidsgiver_orgnr is 'Organisasjonsnummer til arbeidsgiveren.';

comment on column fakta_arbeidsforhold.har_loennsendring_i_beregningsperioden is 'Sier om arbeidsforholdet har hatt lønnsendring i beregningsperioden';

comment on column fakta_arbeidsforhold.er_tidsbegrenset_kilde is 'Kilde til vurdering av om arbeidsforhold er tidsbegrenset';

comment on column fakta_arbeidsforhold.har_mottatt_ytelse_kilde is 'Kilde til vurdering av om det er mottatt ytelse for arbeidsforhold';

comment on column fakta_arbeidsforhold.har_loennsendring_i_beregningsperioden_kilde is 'Kilde til vurdering av om arbeidsforhold har lønnsendring i beregningsperioden';

create index idx_fakta_arbeidsforhold_01
    on fakta_arbeidsforhold (fakta_aggregat_id);

create table fakta_aktoer
(
    id                                 bigint                                       not null
        constraint pk_fakta_aktoer
            primary key,
    fakta_aggregat_id                  bigint                                       not null
        constraint fk_fakta_aktoer_01
            references fakta_aggregat,
    er_ny_i_arbeidslivet_sn            boolean,
    er_nyoppstartet_fl                 boolean,
    har_fl_mottatt_ytelse              boolean,
    skal_besteberegnes                 boolean,
    mottar_etterloenn_sluttpakke       boolean,
    skal_beregnes_som_militaer         boolean,
    er_ny_i_arbeidslivet_sn_kilde      varchar(20),
    er_nyoppstartet_fl_kilde           varchar(20),
    har_fl_mottatt_ytelse_kilde        varchar(20),
    mottar_etterloenn_sluttpakke_kilde varchar(20),
    skal_beregnes_som_militaer_kilde   varchar(20),
    skal_besteberegnes_kilde           varchar(20),
    opprettet_av                       varchar(20)  default 'VL'::character varying not null,
    opprettet_tid                      timestamp(3) default CURRENT_TIMESTAMP       not null,
    versjon                            int          default 0                       not null,
    endret_av                          varchar(20),
    endret_tid                         timestamp(3)
);

create index idx_fakta_aktoer_fakta_aggregat_id
    on fakta_aktoer (fakta_aggregat_id);


comment on table fakta_aktoer is 'Tabell som lagrer faktaavklaringer for arbeidsforhold';

comment on column fakta_aktoer.id is 'Primary Key';

comment on column fakta_aktoer.fakta_aggregat_id is 'Foreign Key til faktaavklaringer';

comment on column fakta_aktoer.er_ny_i_arbeidslivet_sn is 'Er SN og ny i arbeidslivet';

comment on column fakta_aktoer.er_nyoppstartet_fl is 'Er FL og nyoppstartet';

comment on column fakta_aktoer.har_fl_mottatt_ytelse is 'Har mottatt ytelse for frilansaktivitet';

comment on column fakta_aktoer.skal_besteberegnes is 'Skal bruker besteberegnes';

comment on column fakta_aktoer.mottar_etterloenn_sluttpakke is 'Mottar bruker etterlønn/sluttpakke';

comment on column fakta_aktoer.skal_beregnes_som_militaer is 'Sier om bruker har hatt og skal beregnes som militær/siviltjeneste';

comment on column fakta_aktoer.er_ny_i_arbeidslivet_sn_kilde is 'Kilde til vurdering av om bruker er ny i arbeidslivet.';

comment on column fakta_aktoer.er_nyoppstartet_fl_kilde is 'Kilde til vurdering av om bruker er nyoppstartet frilans.';

comment on column fakta_aktoer.har_fl_mottatt_ytelse_kilde is 'Kilde til vurdering av om det er mottatt ytelse for frilans.';

comment on column fakta_aktoer.mottar_etterloenn_sluttpakke_kilde is 'Kilde til vurdering av om bruker mottar etterlønn/sluttpakke.';

comment on column fakta_aktoer.skal_beregnes_som_militaer_kilde is 'Kilde til vurdering av om bruker skal beregnes som militær.';

comment on column fakta_aktoer.skal_besteberegnes_kilde is 'Kilde til vurdering av om bruker skal besteberegnes.';

create table avklaringsbehov
(
    id                     bigint                                       not null
        constraint pk_avklaringsbehov
            primary key,
    kobling_id             bigint                                       not null
        constraint fk_avklaringsbehov_01
            references kobling,
    avklaringsbehov_def    varchar(50)                                  not null,
    avklaringsbehov_status varchar(20)                                  not null,
    begrunnelse            varchar(4000),
    er_trukket             boolean,
    vurdert_av             varchar(20),
    vurdert_tid            timestamp(3),
    opprettet_av           varchar(20)  default 'VL'::character varying not null,
    opprettet_tid          timestamp(3) default CURRENT_TIMESTAMP       not null,
    versjon                int          default 0                       not null,
    endret_av              varchar(20),
    endret_tid             timestamp(3)
);

comment on table avklaringsbehov is 'Tabell som holder på avklaringsbehov knyttet til en kobling';

comment on column avklaringsbehov.id is 'Primary Key';

comment on column avklaringsbehov.kobling_id is 'Fremmednøkkel som kobler avklaringsbehov til koblingen';

comment on column avklaringsbehov.avklaringsbehov_def is 'Definisjonen av avklaringsbehovet';

comment on column avklaringsbehov.avklaringsbehov_status is 'Status på avklaringsbehovet';

comment on column avklaringsbehov.begrunnelse is 'Saksbehandlers begrunnelse for løsningen av avklaringsbehovet';

comment on column avklaringsbehov.er_trukket is 'Kun relevant for overstyring. Spesifiserer om handling for overstyring er trukket/avbrutt.';

comment on column avklaringsbehov.vurdert_av is 'Hvem har vurdert';

comment on column avklaringsbehov.vurdert_tid is 'Når ble det vurdert';

create index idx_avklaringsbehov_10
    on avklaringsbehov (kobling_id);

create index idx_avklaringsbehov_12
    on avklaringsbehov (avklaringsbehov_status);

create index idx_avklaringsbehov_11
    on avklaringsbehov (avklaringsbehov_def);

create table regel_sporing_periode
(
    id                    bigint                                       not null
        constraint pk_regel_sporing_periode
            primary key,
    kobling_id            bigint                                       not null
        constraint fk_regel_sporing_periode_01
            references kobling,
    fom                   DATE                                         not null,
    tom                   DATE                                         not null,
    regel_type            varchar(50)                                  not null,
    aktiv                 boolean      default false                   not null,
    regel_evaluering_json text,
    regel_input_json      text,
    regel_versjon         varchar(20),
    opprettet_av          varchar(20)  default 'VL'::character varying not null,
    opprettet_tid         timestamp(3) default CURRENT_TIMESTAMP       not null,
    versjon               int          default 0                       not null,
    endret_av             varchar(20),
    endret_tid            timestamp(3)
);

comment on table regel_sporing_periode is 'Tabell som lagrer regelsporinger for beregningsgrunnlagperioder';

comment on column regel_sporing_periode.id is 'Primary Key';

comment on column regel_sporing_periode.fom is 'Fom-dato for periode som spores';

comment on column regel_sporing_periode.tom is 'Tom-dato for periode som spores';

comment on column regel_sporing_periode.regel_type is 'Hvilken regel det gjelder';

comment on column regel_sporing_periode.aktiv is 'Sier om sporingen er aktiv';

comment on column regel_sporing_periode.regel_evaluering_json is 'Regelevaluering/logging i json-format';

comment on column regel_sporing_periode.regel_input_json is 'Input til regelen i json-format';

comment on column regel_sporing_periode.regel_versjon is 'Versjon av beregningsreglene som er brukt';

create index idx_rs_periode_01
    on regel_sporing_periode (kobling_id);

create index idx_rs_periode_02
    on regel_sporing_periode (regel_type);

create table besteberegning_grunnlag
(
    ID                    bigint                                       not null
        constraint pk_besteberegning_grunnlag
            primary key,
    beregningsgrunnlag_id bigint                                       not null
        constraint fk_besteberegning_grunnlag
            references beregningsgrunnlag,
    avvik_beloep          numeric(19, 2),
    opprettet_av          varchar(20)  default 'VL'::character varying not null,
    opprettet_tid         timestamp(3) default CURRENT_TIMESTAMP       not null,
    versjon               int          default 0                       not null,
    endret_av             varchar(20),
    endret_tid            timestamp(3)
);

comment on table besteberegning_grunnlag is 'Grunnlag for vurdering av besteberegning';

comment on column besteberegning_grunnlag.id is 'PK';

comment on column besteberegning_grunnlag.beregningsgrunnlag_id is 'FK til beregningsgrunnlag';

comment on column besteberegning_grunnlag.avvik_beloep is 'Hvor mye avviker beregningen mellom første og tredje ledd';

create index idx_besteberegning_1
    on besteberegning_grunnlag (beregningsgrunnlag_id)
;

create table besteberegning_maaned
(
    id                        bigint                                       not null
        constraint PK_BESTEBEREGNING_MAANED
            primary key,
    besteberegninggrunnlag_id bigint                                       not null
        constraint fk_besteberegning_maaned
            references besteberegning_grunnlag,
    fom                       DATE                                         not null,
    tom                       DATE                                         not null,
    opprettet_av              varchar(20)  default 'VL'::character varying not null,
    opprettet_tid             timestamp(3) default CURRENT_TIMESTAMP       not null,
    versjon                   int          default 0                       not null,
    endret_av                 varchar(20),
    endret_tid                timestamp(3)
)
;

comment on table besteberegning_maaned is 'Aggregat for inntekter pr måned for månedene brukt til beregning av besteberegning';

comment on column besteberegning_maaned.id is 'PK';

comment on column besteberegning_maaned.besteberegninggrunnlag_id is 'FK til besteberegninggrunnlag';

comment on column besteberegning_maaned.fom is 'Første dato i måned';

comment on column besteberegning_maaned.tom is 'Siste dato i måned';

create index idx_besteberegning_maaned_1
    on besteberegning_maaned (besteberegninggrunnlag_id)
;

create table besteberegning_inntekt
(
    id                        bigint                                       not null
        constraint pk_besteberegning_inntekt
            primary key,
    besteberegning_maaned_id  bigint                                       not null
        constraint fk_besteberegning_inntekt
            references besteberegning_maaned,
    arbeidsgiver_aktoer_id    varchar(13),
    arbeidsgiver_orgnr        varchar(9),
    arbeidsforhold_intern_id  uuid,
    opptjening_aktivitet_type varchar(50)  default '-'                     not null,
    inntekt                   numeric(19, 2)                               not null,
    opprettet_av              varchar(20)  default 'VL'::character varying not null,
    opprettet_tid             timestamp(3) default CURRENT_TIMESTAMP       not null,
    versjon                   int          default 0                       not null,
    endret_av                 varchar(20),
    endret_tid                timestamp(3)
)
;

comment on table besteberegning_inntekt is 'Inntekt for en aktivitet i en måned.';

comment on column besteberegning_inntekt.id is 'PK';

comment on column besteberegning_inntekt.besteberegning_maaned_id is 'FK til månedsaggregat';

comment on column besteberegning_inntekt.arbeidsgiver_aktoer_id is 'Arbeidsgiver aktør id';

comment on column besteberegning_inntekt.arbeidsgiver_orgnr is 'Arbeidsgiver organisasjonsnummer';

comment on column besteberegning_inntekt.arbeidsforhold_intern_id is 'Arbeidsforhold intern-id';

comment on column besteberegning_inntekt.opptjening_aktivitet_type is 'Opptjeningaktivitettype';

comment on column besteberegning_inntekt.inntekt is 'Inntekt i måned';

create index idx_besteberegning_inntekt_1
    on besteberegning_inntekt (besteberegning_maaned_id)
;
