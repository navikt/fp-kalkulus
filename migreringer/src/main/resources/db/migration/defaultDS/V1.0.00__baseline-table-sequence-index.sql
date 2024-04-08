create sequence seq_gr_beregningsgrunnlag
    minvalue 1000000
    increment by 50;

create sequence seq_beregningsgrunnlag
    minvalue 1000000
    increment by 50;

create sequence seq_bg_aktivitet
    minvalue 1000000
    increment by 50;

create sequence seq_bg_aktiviteter
    minvalue 1000000
    increment by 50;

create sequence seq_bg_aktivitet_overstyring
    minvalue 1000000
    increment by 50;

create sequence seq_bg_aktivitet_overstyringer
    minvalue 1000000
    increment by 50;

create sequence seq_bg_aktivitet_status
    minvalue 1000000
    increment by 50;

create sequence seq_bg_andel_arbeidsforhold
    minvalue 1000000
    increment by 50;

create sequence seq_bg_fakta_ber_tilfelle
    minvalue 1000000
    increment by 50;

create sequence seq_bg_periode
    minvalue 1000000
    increment by 50;

create sequence seq_bg_periode_aarsak
    minvalue 1000000
    increment by 50;

create sequence seq_bg_pr_status_og_andel
    minvalue 1000000
    increment by 50;

create sequence seq_bg_refusjon_overstyring
    minvalue 1000000
    increment by 50;

create sequence seq_bg_refusjon_overstyringer
    minvalue 1000000
    increment by 50;

create sequence seq_bg_sg_pr_status
    minvalue 1000000
    increment by 50;

create sequence seq_kobling
    minvalue 1000000
    increment by 50;

create sequence seq_br_sats
    minvalue 1000000
    increment by 50;

create sequence seq_bg_refusjon_periode
    minvalue 1000000
    increment by 50;

create sequence seq_regel_sporing_grunnlag
    minvalue 1000000
    increment by 50;

create sequence seq_regel_sporing_periode
    minvalue 1000000
    increment by 50;

create sequence seq_fakta_aggregat
    minvalue 1000000
    increment by 50;

create sequence seq_fakta_arbeidsforhold
    minvalue 1000000
    increment by 50;

create sequence seq_fakta_aktoer
    minvalue 1000000
    increment by 50;

create sequence seq_avklaringsbehov
    minvalue 1000000
    increment by 50;

create sequence seq_kobling_relasjon
    minvalue 1000000
    increment by 50;

create table beregningsgrunnlag
(
    id                 bigint                                       not null
        constraint pk_beregningsgrunnlag
            primary key,
    skjaringstidspunkt timestamp(0)                                 not null,
    grunnbeloep        numeric(12, 2),
    overstyrt          boolean      default false                   not null,
    opprettet_av       varchar(20)  default 'VL'::character varying not null,
    opprettet_tid      timestamp(3) default CURRENT_TIMESTAMP       not null,
    versjon            int          default 0                       not null,
    endret_av          varchar(20),
    endret_tid         timestamp(3)
);

comment on table beregningsgrunnlag is 'Aggregat for beregningsgrunnlag';

comment on column beregningsgrunnlag.id is 'Primærnøkkel';

comment on column beregningsgrunnlag.skjaringstidspunkt is 'Skjæringstidspunkt for beregning';

comment on column beregningsgrunnlag.grunnbeloep is 'Grunnbeløp (G) ved opprinnelig_skjæringstidspunkt';

comment on column beregningsgrunnlag.overstyrt is 'Oppgir om beregningsgrunnlaget er overstyrt ved faktaavklaring';

create index idx_beregningsgrunnlag_02
    on beregningsgrunnlag (skjaringstidspunkt);

create table beregningsgrunnlag_periode
(
    id                                                          bigint                                       not null
        constraint pk_beregningsgrunnlag_periode
            primary key,
    beregningsgrunnlag_id                                       bigint                                       not null
        constraint fk_bg_periode_1
            references beregningsgrunnlag,
    bg_periode_fom                                              timestamp(0)                                 not null,
    bg_periode_tom                                              timestamp(0),
    brutto_pr_aar                                               numeric(19, 2),
    avkortet_pr_aar                                             numeric(19, 2),
    redusert_pr_aar                                             numeric(19, 2),
    dagsats                                                     bigint,
    inntekt_graderingsprosent_brutto                            numeric(19, 2),
    total_utbetalingsgrad_fra_uttak                             numeric(19, 4),
    total_utbetalingsgrad_etter_reduksjon_ved_tilkommet_inntekt numeric(19, 4),
    reduksjonsfaktor_inaktiv_type_a                             numeric(19, 4),
    opprettet_av                                                varchar(20)  default 'VL'::character varying not null,
    opprettet_tid                                               timestamp(3) default CURRENT_TIMESTAMP       not null,
    versjon                                                     int          default 0                       not null,
    endret_av                                                   varchar(20),
    endret_tid                                                  timestamp(3)
);

comment on table beregningsgrunnlag_periode is 'Beregningsgrunnlagsperiode';

comment on column beregningsgrunnlag_periode.id is 'Primærnøkkel';

comment on column beregningsgrunnlag_periode.beregningsgrunnlag_id is 'Fremmednøkkel til tabell som knytter beregningsgrunnlagsperioden til et beregningsgrunnlag';

comment on column beregningsgrunnlag_periode.bg_periode_fom is 'Første gyldighetsdag for beregningsgrunnlag';

comment on column beregningsgrunnlag_periode.bg_periode_tom is 'Siste gyldighetsdag for beregningsgrunnlag';

comment on column beregningsgrunnlag_periode.brutto_pr_aar is 'Beregningsgrunnlag, brutto';

comment on column beregningsgrunnlag_periode.avkortet_pr_aar is 'Avkortet beregningsgrunnlag';

comment on column beregningsgrunnlag_periode.redusert_pr_aar is 'Beregningsgrunnlag, redusert';

comment on column beregningsgrunnlag_periode.dagsats is 'Dagsats, avrundet';

comment on column beregningsgrunnlag_periode.inntekt_graderingsprosent_brutto is 'Graderingsprosent ved gradering mot inntekt. Angir totalt bortfalt inntekt av totalt brutto beregningsgrunnlag.';

comment on column beregningsgrunnlag_periode.total_utbetalingsgrad_fra_uttak is 'Total utbetalingsgrad fra uttak. Utregnet separat fra reduksjon ved tilkommet inntekt.';

comment on column beregningsgrunnlag_periode.total_utbetalingsgrad_etter_reduksjon_ved_tilkommet_inntekt is 'Total utbetalingsgrad etter reduksjon ved tilkommet inntekt. Utregnet separat fra utbetalingsgrad fra uttak.';

comment on column beregningsgrunnlag_periode.reduksjonsfaktor_inaktiv_type_a is 'Reduksjonsfaktor benyttet ved midlertidig inaktiv type A (§8-47a)';

create index idx_bg_periode_01
    on beregningsgrunnlag_periode (beregningsgrunnlag_id);

create index idx_bg_periode_02
    on beregningsgrunnlag_periode (bg_periode_fom);

create index idx_bg_periode_03
    on beregningsgrunnlag_periode (bg_periode_tom);

create table bg_aktivitet_overstyringer
(
    id            bigint                                       not null
        constraint pk_bg_aktivitet_overstyringer
            primary key,
    opprettet_av  varchar(20)  default 'VL'::character varying not null,
    opprettet_tid timestamp(3) default CURRENT_TIMESTAMP       not null,
    versjon       int          default 0                       not null,
    endret_av     varchar(20),
    endret_tid    timestamp(3)
);

comment on table bg_aktivitet_overstyringer is 'Tabell som knytter BG_AKTIVITET_OVERSTYRING til GR_BEREGNINGSGRUNNLAG';

create table bg_aktivitet_overstyring
(
    id                        bigint                                       not null
        constraint pk_ba_overstyring
            primary key,
    ba_overstyringer_id       bigint                                       not null
        constraint fk_ba_overstyringer_01
            references bg_aktivitet_overstyringer,
    handling_type             varchar(100)                                 not null,
    opptjening_aktivitet_type varchar(100)                                 not null,
    fom                       timestamp(0)                                 not null,
    tom                       timestamp(0)                                 not null,
    arbeidsgiver_orgnr        varchar(100),
    arbeidsgiver_aktor_id     varchar(100),
    arbeidsforhold_intern_id  uuid,
    opprettet_av              varchar(20)  default 'VL'::character varying not null,
    opprettet_tid             timestamp(3) default CURRENT_TIMESTAMP       not null,
    versjon                   int          default 0                       not null,
    endret_av                 varchar(20),
    endret_tid                timestamp(3)
);

comment on table bg_aktivitet_overstyring is 'Overstyringer av aktiviteter som er relevant for beregning';

comment on column bg_aktivitet_overstyring.id is 'Primary Key';

comment on column bg_aktivitet_overstyring.handling_type is 'FK: Kodeliste BEREGNING_AKTIVITET_HANDLING_TYPE';

comment on column bg_aktivitet_overstyring.opptjening_aktivitet_type is 'Type aktivitet som har inngått i vurdering av opptjening';

comment on column bg_aktivitet_overstyring.arbeidsgiver_orgnr is 'Arbeidsgivers orgnr';

comment on column bg_aktivitet_overstyring.arbeidsgiver_aktor_id is 'Arbeidsgivers aktør_id';

comment on column bg_aktivitet_overstyring.arbeidsforhold_intern_id is 'Globalt unikt arbeidsforhold id generert for arbeidsgiver/arbeidsforhold. I motsetning til arbeidsforhold_ekstern_id som holder arbeidsgivers referanse';

create index idx_bg_aktiv_overstyring_11
    on bg_aktivitet_overstyring (arbeidsforhold_intern_id);

create index idx_ba_overstyring_01
    on bg_aktivitet_overstyring (ba_overstyringer_id);

create table bg_aktivitet_status
(
    id                    bigint                                       not null
        constraint pk_bg_aktivitet_status
            primary key,
    beregningsgrunnlag_id bigint                                       not null
        constraint fk_bg_aktivitet_status_1
            references beregningsgrunnlag,
    aktivitet_status      varchar(100)                                 not null,
    hjemmel               varchar(100) default '-'::character varying  not null,
    opprettet_av          varchar(20)  default 'VL'::character varying not null,
    opprettet_tid         timestamp(3) default CURRENT_TIMESTAMP       not null,
    versjon               int          default 0                       not null,
    endret_av             varchar(20),
    endret_tid            timestamp(3)
);

comment on table bg_aktivitet_status is 'Aktivitetsstatus i beregningsgrunnlag';

comment on column bg_aktivitet_status.beregningsgrunnlag_id is 'Fremmednøkkel til tabell som knytter beregningsgrunnlagsaktivitetstatusen til et beregningsgrunnlag';

comment on column bg_aktivitet_status.aktivitet_status is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';

comment on column bg_aktivitet_status.hjemmel is 'Hjemmel for beregningsgrunnlag';

create index idx_bg_aktivitet_status_01
    on bg_aktivitet_status (beregningsgrunnlag_id);

create index idx_bg_aktivitet_status_02
    on bg_aktivitet_status (aktivitet_status);

create index idx_bg_aktivitet_status_03
    on bg_aktivitet_status (hjemmel);

create table bg_aktiviteter
(
    id                            bigint                                       not null
        constraint pk_bg_aktiviteter
            primary key,
    skjaringstidspunkt_opptjening timestamp(0)                                 not null,
    opprettet_av                  varchar(20)  default 'VL'::character varying not null,
    opprettet_tid                 timestamp(3) default CURRENT_TIMESTAMP       not null,
    versjon                       int          default 0                       not null,
    endret_av                     varchar(20),
    endret_tid                    timestamp(3)
);

comment on table bg_aktiviteter is 'Tabell som knytter BG_AKTIVITET til GR_BEREGNINGSGRUNNLAG';

comment on column bg_aktiviteter.skjaringstidspunkt_opptjening is 'Skjæringstidspunkt for opptjening';

create table bg_aktivitet
(
    id                        bigint                                       not null
        constraint pk_bg_aktivitet
            primary key,
    fom                       timestamp(0)                                 not null,
    tom                       timestamp(0)                                 not null,
    bg_aktiviteter_id         bigint                                       not null
        constraint fk_bg_aktivitet_01
            references bg_aktiviteter,
    opptjening_aktivitet_type varchar(100)                                 not null,
    arbeidsgiver_aktor_id     varchar(100),
    arbeidsgiver_orgnr        varchar(100),
    arbeidsforhold_intern_id  uuid,
    opprettet_av              varchar(20)  default 'VL'::character varying not null,
    opprettet_tid             timestamp(3) default CURRENT_TIMESTAMP       not null,
    versjon                   int          default 0                       not null,
    endret_av                 varchar(20),
    endret_tid                timestamp(3)
);

comment on table bg_aktivitet is 'Aktivitet som er relevant for beregning';

comment on column bg_aktivitet.id is 'Primary Key';

comment on column bg_aktivitet.fom is 'Aktiviteter relevant for beregning etter saksbehandlers vurdering';

comment on column bg_aktivitet.opptjening_aktivitet_type is 'Type aktivitet som har inngått i vurdering av opptjening';

comment on column bg_aktivitet.arbeidsgiver_aktor_id is 'Arbeidsgivers aktør_id';

comment on column bg_aktivitet.arbeidsgiver_orgnr is 'Organisasjonsnummer for arbeidsgivere som er virksomheter';

comment on column bg_aktivitet.arbeidsforhold_intern_id is 'Globalt unikt arbeidsforhold id generert for arbeidsgiver/arbeidsforhold. I motsetning til arbeidsforhold_ekstern_id som holder arbeidsgivers referanse';

create index idx_bg_aktivitet_01
    on bg_aktivitet (bg_aktiviteter_id);

create index idx_bg_aktivitet_04
    on bg_aktivitet (arbeidsgiver_orgnr);

create index idx_bg_aktivitet_11
    on bg_aktivitet (arbeidsforhold_intern_id);

create table bg_fakta_ber_tilfelle
(
    id                       bigint                                       not null
        constraint pk_bg_fakta_ber_tilfelle
            primary key,
    fakta_beregning_tilfelle varchar(100)                                 not null,
    beregningsgrunnlag_id    bigint                                       not null
        constraint fk_bg_fakta_ber_tilfelle_1
            references beregningsgrunnlag,
    versjon                  int          default 0                       not null,
    opprettet_av             varchar(20)  default 'VL'::character varying not null,
    opprettet_tid            timestamp(3) default CURRENT_TIMESTAMP       not null,
    endret_av                varchar(20),
    endret_tid               timestamp(3)
);

comment on table bg_fakta_ber_tilfelle is 'Eit fakta om beregning tilfelle for eit beregningsgrunnlag';

comment on column bg_fakta_ber_tilfelle.fakta_beregning_tilfelle is 'FK: FAKTA_OM_BEREGNING_TILFELLE';

comment on column bg_fakta_ber_tilfelle.beregningsgrunnlag_id is 'FK: BEREGNINGSGRUNNLAG';

create index idx_bg_fakta_ber_tilfelle_1
    on bg_fakta_ber_tilfelle (beregningsgrunnlag_id);

create table bg_periode_aarsak
(
    id             bigint                                       not null
        constraint pk_bg_periode_aarsak
            primary key,
    periode_aarsak varchar(100)                                 not null,
    bg_periode_id  bigint                                       not null
        constraint fk_bg_periode_aarsak_1
            references beregningsgrunnlag_periode,
    versjon        int          default 0                       not null,
    opprettet_av   varchar(20)  default 'VL'::character varying not null,
    opprettet_tid  timestamp(3) default CURRENT_TIMESTAMP       not null,
    endret_av      varchar(20),
    endret_tid     timestamp(3)
);

comment on table bg_periode_aarsak is 'Periodeårsaker for splitting av perioder i beregningsgrunnlag';

comment on column bg_periode_aarsak.id is 'Primary Key';

comment on column bg_periode_aarsak.periode_aarsak is 'Årsak til splitting av periode';

create index idx_bg_periode_aarsak_1
    on bg_periode_aarsak (bg_periode_id);

create table bg_pr_status_og_andel
(
    id                                 bigint                                                  not null
        constraint pk_bg_pr_status_og_andel
            primary key,
    bg_periode_id                      bigint                                                  not null
        constraint fk_bg_pr_status_og_andel_1
            references beregningsgrunnlag_periode,
    aktivitet_status                   varchar(100)                                            not null,
    beregningsperiode_fom              timestamp(0),
    beregningsperiode_tom              timestamp(0),
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
    inntektskategori                   varchar(100)                                            not null,
    andelsnr                           bigint                                                  not null,
    arbeidsforhold_type                varchar(100) default '-'::character varying             not null,
    besteberegning_pr_aar              numeric(19, 2),
    lagt_til_av_saksbehandler          boolean      default false                              not null,
    dagsats_tilstoetende_ytelse        bigint,
    fordelt_pr_aar                     numeric(19, 2),
    ny_i_arbeidslivet                  boolean,
    fastsatt_av_saksbehandler          boolean,
    kilde                              varchar(100) default 'PROSESS_START'::character varying not null,
    avkortet_foer_gradering_pr_aar     numeric(19, 2),
    manuelt_fordelt_pr_aar             numeric(19, 2),
    inntektskategori_manuell_fordeling varchar(100),
    inntektskategori_fordeling         varchar(100),
    opprettet_av                       varchar(20)  default 'VL'::character varying            not null,
    opprettet_tid                      timestamp(3) default CURRENT_TIMESTAMP                  not null,
    versjon                            int          default 0                                  not null,
    endret_av                          varchar(20),
    endret_tid                         timestamp(3)
);

comment on table bg_pr_status_og_andel is 'Beregningsgrunnlag pr status og andel';

comment on column bg_pr_status_og_andel.id is 'Primærnøkkel';

comment on column bg_pr_status_og_andel.bg_periode_id is 'FK: Fremmednøkkel til tabell som knytter beregningsgrunnlagsandelen til en beregningsgrunnlagperiode';

comment on column bg_pr_status_og_andel.aktivitet_status is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';

comment on column bg_pr_status_og_andel.beregningsperiode_fom is 'Første dag i beregningsperiode';

comment on column bg_pr_status_og_andel.beregningsperiode_tom is 'Siste dag i beregningsperiode';

comment on column bg_pr_status_og_andel.brutto_pr_aar is 'Beregningsgrunnlagsandel, brutto';

comment on column bg_pr_status_og_andel.overstyrt_pr_aar is 'Beregningsgrunnlagsandel, overstyrt';

comment on column bg_pr_status_og_andel.avkortet_pr_aar is 'Beregningsgrunnlagsandel, avkortet';

comment on column bg_pr_status_og_andel.redusert_pr_aar is 'Beregningsgrunnlag, redusert';

comment on column bg_pr_status_og_andel.beregnet_pr_aar is 'Beregningsgrunnlagsandel, beregnet';

comment on column bg_pr_status_og_andel.maksimal_refusjon_pr_aar is 'Maksimalverdi for refusjon til arbeidsgiver';

comment on column bg_pr_status_og_andel.avkortet_refusjon_pr_aar is 'Refusjon til arbeidsgiver, avkortet';

comment on column bg_pr_status_og_andel.redusert_refusjon_pr_aar is 'Refusjon til arbeidsgiver, redusert';

comment on column bg_pr_status_og_andel.avkortet_brukers_andel_pr_aar is 'Brukers andel, avkortet';

comment on column bg_pr_status_og_andel.redusert_brukers_andel_pr_aar is 'Brukers andel, redusert';

comment on column bg_pr_status_og_andel.dagsats_bruker is 'Dagsats til bruker';

comment on column bg_pr_status_og_andel.dagsats_arbeidsgiver is 'Dagsats til arbeidsgiver';

comment on column bg_pr_status_og_andel.pgi_snitt is 'Gjennomsnittlig pensjonsgivende inntekt';

comment on column bg_pr_status_og_andel.pgi1 is 'Pensjonsgivende inntekt i år 1';

comment on column bg_pr_status_og_andel.pgi2 is 'Pensjonsgivende inntekt i år 2';

comment on column bg_pr_status_og_andel.pgi3 is 'Pensjonsgivende inntekt i år 3';

comment on column bg_pr_status_og_andel.aarsbeloep_tilstoetende_ytelse is 'Årsbeløp for tilstøtende ytelse';

comment on column bg_pr_status_og_andel.inntektskategori is 'FK:INNTEKTSKATEGORI Fremmednøkkel til tabell med oversikt over inntektskategorier';

comment on column bg_pr_status_og_andel.andelsnr is 'Nummer for å identifisere andel innanfor ein periode';

comment on column bg_pr_status_og_andel.arbeidsforhold_type is 'Typekode for arbeidstakeraktivitet som ikke er tilknyttet noen virksomhet';

comment on column bg_pr_status_og_andel.besteberegning_pr_aar is 'Inntekt fastsatt av saksbehandler ved besteberegning for fødende kvinne';

comment on column bg_pr_status_og_andel.lagt_til_av_saksbehandler is 'Angir om andel er lagt til av saksbehandler manuelt';

comment on column bg_pr_status_og_andel.dagsats_tilstoetende_ytelse is 'Original dagsats fra tilstøtende ytelse AAP/Dagpenger';

comment on column bg_pr_status_og_andel.fordelt_pr_aar is 'Beregningsgrunnlagsandel etter fordeling';

comment on column bg_pr_status_og_andel.kilde is 'Angir kilde/opphav for andel';

comment on column bg_pr_status_og_andel.avkortet_foer_gradering_pr_aar is 'Beløp etter avkorting før gradering mot utbetalingsgrad';

comment on column bg_pr_status_og_andel.manuelt_fordelt_pr_aar is 'Manuelt fordelt beregningsgrunnlag.';

comment on column bg_pr_status_og_andel.inntektskategori_manuell_fordeling is 'Inntektskategori satt ved manuell fordeling.';

comment on column bg_pr_status_og_andel.inntektskategori_fordeling is 'Inntektskategori satt ved automatisk fordeling.';

create table bg_andel_arbeidsforhold
(
    id                              bigint                                       not null
        constraint pk_bg_andel_arbeidsforhold
            primary key,
    bg_andel_id                     bigint                                       not null
        constraint fk_bg_andel_arbeidsforhold_1
            references bg_pr_status_og_andel,
    refusjonskrav_pr_aar            numeric(19, 2),
    naturalytelse_bortfalt_pr_aar   numeric(19, 2),
    naturalytelse_tilkommet_pr_aar  numeric(19, 2),
    arbeidsperiode_fom              timestamp(0),
    arbeidsperiode_tom              timestamp(0),
    arbeidsgiver_aktor_id           varchar(100),
    arbeidsgiver_orgnr              varchar(100),
    arbeidsforhold_intern_id        uuid,
    tidsbegrenset_arbeidsforhold    boolean,
    loennsendring_i_perioden        boolean,
    hjemmel_for_refusjonskravfrist  varchar(100),
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

comment on table bg_andel_arbeidsforhold is 'Informasjon om arbeidsforholdet knyttet til beregningsgrunnlagandelen';

comment on column bg_andel_arbeidsforhold.bg_andel_id is 'Beregningsgrunnlagandelen arbeidsforholdet er knyttet til';

comment on column bg_andel_arbeidsforhold.refusjonskrav_pr_aar is 'Arbeidsgivers refusjonskrav';

comment on column bg_andel_arbeidsforhold.naturalytelse_bortfalt_pr_aar is 'Verdi av bortfalt naturalytelse';

comment on column bg_andel_arbeidsforhold.naturalytelse_tilkommet_pr_aar is 'Verdi av tilkommet naturalytelse';

comment on column bg_andel_arbeidsforhold.arbeidsperiode_fom is 'Fra og med dato arbeidsperiode';

comment on column bg_andel_arbeidsforhold.arbeidsperiode_tom is 'Til og med dato arbeidsperiode';

comment on column bg_andel_arbeidsforhold.arbeidsgiver_aktor_id is 'Arbeidsgivers aktør id.';

comment on column bg_andel_arbeidsforhold.arbeidsgiver_orgnr is 'Organisasjonsnummer for arbeidsgivere som er virksomheter';

comment on column bg_andel_arbeidsforhold.arbeidsforhold_intern_id is 'Globalt unikt arbeidsforhold id generert for arbeidsgiver/arbeidsforhold. I motsetning til arbeidsforhold_ekstern_id som holder arbeidsgivers referanse';

comment on column bg_andel_arbeidsforhold.saksbehandlet_refusjon_pr_aar is 'Refusjonsbeløp satt som følge av å ha vurdert refusjonskravet og refusjonsbeløpet';

comment on column bg_andel_arbeidsforhold.fordelt_refusjon_pr_aar is 'Refusjonsbeløp satt i henhold til fordelingsregler';

comment on column bg_andel_arbeidsforhold.refusjonskrav_frist_utfall is 'Utfall for vurdering av frist for refusjonskrav';

comment on column bg_andel_arbeidsforhold.manuelt_fordelt_refusjon_pr_aar is 'Refusjonsbeløp satt av saksbehandler i fordeling.';

create index idx_bg_andel_arbeidsforhold_01
    on bg_andel_arbeidsforhold (bg_andel_id);

create index idx_bg_andel_arbeidsforhold_03
    on bg_andel_arbeidsforhold (arbeidsgiver_orgnr);

create index idx_bg_andel_arbeidsforhold_11
    on bg_andel_arbeidsforhold (arbeidsforhold_intern_id);

create index idx_bg_pr_status_og_andel_01
    on bg_pr_status_og_andel (bg_periode_id);

create index idx_bg_pr_status_og_andel_02
    on bg_pr_status_og_andel (aktivitet_status);

create table bg_refusjon_overstyringer
(
    id            bigint                                       not null
        constraint pk_bg_refusjon_overstyringer
            primary key,
    versjon       int          default 0                       not null,
    opprettet_av  varchar(20)  default 'VL'::character varying not null,
    opprettet_tid timestamp(3) default CURRENT_TIMESTAMP       not null,
    endret_av     varchar(20),
    endret_tid    timestamp(3)
);

comment on table bg_refusjon_overstyringer is 'Tabell som knytter BG_REFUSJON_OVERSTYRING til GR_BEREGNINGSGRUNNLAG';

create table bg_refusjon_overstyring
(
    id                    bigint                                       not null
        constraint pk_bg_refusjon_overstyring
            primary key,
    br_overstyringer_id   bigint                                       not null
        constraint fk_br_overstyringer_01
            references bg_refusjon_overstyringer,
    arbeidsgiver_orgnr    varchar(100),
    arbeidsgiver_aktor_id varchar(100),
    fom                   timestamp(0),
    er_frist_utvidet      boolean,
    opprettet_av          varchar(20)  default 'VL'::character varying not null,
    opprettet_tid         timestamp(3) default CURRENT_TIMESTAMP       not null,
    versjon               int          default 0                       not null,
    endret_av             varchar(20),
    endret_tid            timestamp(3)
);

comment on table bg_refusjon_overstyring is 'Overstyringer av aktiviteter som er relevant for beregning';

comment on column bg_refusjon_overstyring.id is 'Primary Key';

comment on column bg_refusjon_overstyring.br_overstyringer_id is 'Arbeidsgivers orgnr';

comment on column bg_refusjon_overstyring.arbeidsgiver_orgnr is 'Arbeidsgivers orgnr';

comment on column bg_refusjon_overstyring.arbeidsgiver_aktor_id is 'Arbeidsgivers aktør_id';

comment on column bg_refusjon_overstyring.er_frist_utvidet is 'Er frist for refusjonskrav utvidet.';

create index idx_br_overstyring_01
    on bg_refusjon_overstyring (br_overstyringer_id);

create table bg_sg_pr_status
(
    id                          bigint                                       not null
        constraint pk_bg_sg_pr_status
            primary key,
    beregningsgrunnlag_id       bigint                                       not null
        constraint fk_bg_sg_pr_status_01
            references beregningsgrunnlag,
    sammenligningsgrunnlag_type varchar(100)                                 not null,
    sammenligningsperiode_fom   timestamp(0)                                 not null,
    sammenligningsperiode_tom   timestamp(0)                                 not null,
    rapportert_pr_aar           numeric(19, 2)                               not null,
    avvik_promille              numeric(27, 10),
    avvik_promille_ny           numeric(27, 10)                              not null,
    opprettet_av                varchar(20)  default 'VL'::character varying not null,
    opprettet_tid               timestamp(3) default CURRENT_TIMESTAMP       not null,
    versjon                     int          default 0                       not null,
    endret_av                   varchar(20),
    endret_tid                  timestamp(3)
);

comment on table bg_sg_pr_status is 'Sammenligningsgrunnlag pr status';

comment on column bg_sg_pr_status.beregningsgrunnlag_id is 'FK: BEREGNINGSGRUNNLAG';

comment on column bg_sg_pr_status.sammenligningsgrunnlag_type is 'Type av sammenligningsgrunnlag';

comment on column bg_sg_pr_status.sammenligningsperiode_fom is 'Fom-dato for sammenligningsperiode';

comment on column bg_sg_pr_status.sammenligningsperiode_tom is 'Tom-dato for sammenligningsperiode';

comment on column bg_sg_pr_status.rapportert_pr_aar is 'Rapportert inntekt pr aar i for gitt status';

comment on column bg_sg_pr_status.avvik_promille is 'Avvik promille';

comment on column bg_sg_pr_status.avvik_promille_ny is 'Midlertidig kolonne som skal erstatte AVVIK_PROMILLE da denne støtter høyere nøyaktighet';

create index idx_bg_sg_pr_status_01
    on bg_sg_pr_status (beregningsgrunnlag_id);

create table kobling
(
    id                bigint                                       not null
        constraint pk_kobling
            primary key,
    kobling_referanse uuid                                         not null
        constraint uidx_kobling_1
            unique,
    ytelse_type       varchar(100)                                 not null,
    kl_ytelse_type    varchar(100) default 'FAGSAK_YTELSE_TYPE'::character varying,
    bruker_aktoer_id  varchar(50)                                  not null,
    saksnummer        varchar(19)                                  not null,
    versjon           int          default 0                       not null,
    opprettet_av      varchar(20)  default 'VL'::character varying not null,
    opprettet_tid     timestamp(3) default LOCALTIMESTAMP          not null,
    endret_av         varchar(20),
    endret_tid        timestamp(3)
);

comment on table kobling is 'Holder referansen som kalles på fra av eksternt system';

comment on column kobling.id is 'Primærnøkkel';

comment on column kobling.kobling_referanse is 'Referansenøkkel som eksponeres lokalt';

comment on column kobling.ytelse_type is 'Hvilken ytelse komplekset henger under';

comment on column kobling.bruker_aktoer_id is 'Aktøren koblingen gjelder for';

comment on column kobling.saksnummer is 'Saksnummer til saken koblingen gjelder for';

create index idx_kobling_1
    on kobling (kobling_referanse);

create index idx_kobling_2
    on kobling (saksnummer);

create index idx_kobling_3
    on kobling (ytelse_type, kl_ytelse_type);

create table br_sats
(
    id            bigint                                       not null
        constraint pk_sats
            primary key,
    sats_type     varchar(100)                                 not null,
    fom           timestamp(0)                                 not null,
    tom           timestamp(0)                                 not null,
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

create table bg_refusjon_periode
(
    id                         bigint                                       not null
        primary key,
    bg_refusjon_overstyring_id bigint                                       not null
        references bg_refusjon_overstyring,
    arbeidsforhold_intern_id   uuid,
    fom                        timestamp(0)                                 not null,
    versjon                    int          default 0                       not null,
    opprettet_av               varchar(20)  default 'VL'::character varying not null,
    opprettet_tid              timestamp(3) default CURRENT_TIMESTAMP       not null,
    endret_av                  varchar(20),
    endret_tid                 timestamp(3)
);

comment on table bg_refusjon_periode is 'Tabell som holder på hvilke refusjonskrav som skal gjelde fra hvilken dato gitt arbeidsgiver og arbeidsforhold';

comment on column bg_refusjon_periode.bg_refusjon_overstyring_id is 'Foreign key til tabell BG_REFUSJON_OVERSTYRING';

comment on column bg_refusjon_periode.arbeidsforhold_intern_id is 'Globalt unikt arbeidsforhold id generert for arbeidsgiver/arbeidsforhold. I motsetning til arbeidsforhold_ekstern_id som holder arbeidsgivers referanse';

comment on column bg_refusjon_periode.fom is 'Fra og med datoen refusjon skal tas med i beregningen';

create index idx_bg_refusjon_periode_1
    on bg_refusjon_periode (bg_refusjon_overstyring_id);

create table regel_sporing_grunnlag
(
    id                    bigint                                       not null
        constraint pk_regel_sporing_grunnlag
            primary key,
    kobling_id            bigint                                       not null
        constraint fk_regel_sporing_grunnlag_01
            references kobling,
    regel_type            varchar(100)                                 not null,
    aktiv                 boolean      default false                   not null,
    regel_evaluering_json text,
    regel_input_json      text,
    regel_versjon         varchar(100),
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
    steg_opprettet               varchar(100) default '-'::character varying  not null,
    aktiv                        boolean      default false                   not null,
    register_aktiviteter_id      bigint                                       not null
        constraint fk_gr_beregningsgrunnlag_3
            references bg_aktiviteter,
    saksbehandlet_aktiviteter_id bigint
        constraint fk_gr_beregningsgrunnlag_4
            references bg_aktiviteter,
    ba_overstyringer_id          bigint
        constraint fk_gr_beregningsgrunnlag_5
            references bg_aktivitet_overstyringer,
    br_overstyringer_id          bigint
        constraint fk_gr_beregningsgrunnlag_6
            references bg_refusjon_overstyringer,
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

comment on column gr_beregningsgrunnlag.ba_overstyringer_id is 'Overstyringer av beregningaktiviteter';

comment on column gr_beregningsgrunnlag.br_overstyringer_id is 'Overstyringer av refusjon';

comment on column gr_beregningsgrunnlag.fakta_aggregat_id is 'Foreign Key til faktaavklaringer';

create index idx_gr_beregningsgrunnlag_02
    on gr_beregningsgrunnlag (register_aktiviteter_id);

create index idx_gr_beregningsgrunnlag_03
    on gr_beregningsgrunnlag (saksbehandlet_aktiviteter_id);

create index idx_gr_beregningsgrunnlag_04
    on gr_beregningsgrunnlag (ba_overstyringer_id);

create index idx_gr_beregningsgrunnlag_05
    on gr_beregningsgrunnlag (br_overstyringer_id);

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
    id                                          bigint                                       not null
        constraint pk_fakta_arbeidsforhold
            primary key,
    fakta_aggregat_id                           bigint                                       not null
        constraint fk_fakta_arbeidsforhold_01
            references fakta_aggregat,
    er_tidsbegrenset                            boolean,
    har_mottatt_ytelse                          boolean,
    arbeidsforhold_intern_id                    uuid,
    arbeidsgiver_orgnr                          varchar(100),
    arbeidsgiver_aktor_id                       varchar(100),
    har_lonnsendring_i_beregningsperioden       boolean,
    er_tidsbegrenset_kilde                      varchar(100),
    har_mottatt_ytelse_kilde                    varchar(100),
    har_lonnsendring_i_beregningsperioden_kilde varchar(100),
    opprettet_av                                varchar(20)  default 'VL'::character varying not null,
    opprettet_tid                               timestamp(3) default CURRENT_TIMESTAMP       not null,
    versjon                                     int          default 0                       not null,
    endret_av                                   varchar(20),
    endret_tid                                  timestamp(3)
);

comment on table fakta_arbeidsforhold is 'Tabell som lagrer faktaavklaringer for arbeidsforhold';

comment on column fakta_arbeidsforhold.id is 'Primary Key';

comment on column fakta_arbeidsforhold.fakta_aggregat_id is 'Foreign Key til faktaavklaringer';

comment on column fakta_arbeidsforhold.er_tidsbegrenset is 'Er arbeidsforhold tidsbegrenset';

comment on column fakta_arbeidsforhold.har_mottatt_ytelse is 'Er det tidligere mottatt ytelse for arbeidsforholdet';

comment on column fakta_arbeidsforhold.har_lonnsendring_i_beregningsperioden is 'Sier om arbeidsforholdet har hatt lønnsendring i beregningsperioden';

comment on column fakta_arbeidsforhold.er_tidsbegrenset_kilde is 'Kilde til vurdering av om arbeidsforhold er tidsbegrenset';

comment on column fakta_arbeidsforhold.har_mottatt_ytelse_kilde is 'Kilde til vurdering av om det er mottatt ytelse for arbeidsforhold';

comment on column fakta_arbeidsforhold.har_lonnsendring_i_beregningsperioden_kilde is 'Kilde til vurdering av om arbeidsforhold har lønnsendring i beregningsperioden';

create index idx_fakta_arbeidsforhold_01
    on fakta_arbeidsforhold (fakta_aggregat_id);

create table fakta_aktoer
(
    id                                bigint                                       not null
        constraint pk_fakta_aktoer
            primary key,
    fakta_aggregat_id                 bigint                                       not null
        constraint fk_fakta_aktoer_01
            references fakta_aggregat,
    er_ny_i_arbeidslivet_sn           boolean,
    er_nyoppstartet_fl                boolean,
    har_fl_mottatt_ytelse             boolean,
    skal_besteberegnes                boolean,
    mottar_etterlonn_sluttpakke       boolean,
    skal_beregnes_som_militaer        boolean,
    er_ny_i_arbeidslivet_sn_kilde     varchar(100),
    er_nyoppstartet_fl_kilde          varchar(100),
    har_fl_mottatt_ytelse_kilde       varchar(100),
    mottar_etterlonn_sluttpakke_kilde varchar(100),
    skal_beregnes_som_militaer_kilde  varchar(100),
    skal_besteberegnes_kilde          varchar(100),
    opprettet_av                      varchar(20)  default 'VL'::character varying not null,
    opprettet_tid                     timestamp(3) default CURRENT_TIMESTAMP       not null,
    versjon                           int          default 0                       not null,
    endret_av                         varchar(20),
    endret_tid                        timestamp(3)
);

comment on table fakta_aktoer is 'Tabell som lagrer faktaavklaringer for arbeidsforhold';

comment on column fakta_aktoer.id is 'Primary Key';

comment on column fakta_aktoer.fakta_aggregat_id is 'Foreign Key til faktaavklaringer';

comment on column fakta_aktoer.er_ny_i_arbeidslivet_sn is 'Er SN og ny i arbeidslivet';

comment on column fakta_aktoer.er_nyoppstartet_fl is 'Er FL og nyoppstartet';

comment on column fakta_aktoer.har_fl_mottatt_ytelse is 'Har mottatt ytelse for frilansaktivitet';

comment on column fakta_aktoer.skal_besteberegnes is 'Skal bruker besteberegnes';

comment on column fakta_aktoer.mottar_etterlonn_sluttpakke is 'Mottar bruker etterlønn/sluttpakke';

comment on column fakta_aktoer.skal_beregnes_som_militaer is 'Sier om bruker har hatt og skal beregnes som militær/siviltjeneste';

comment on column fakta_aktoer.er_ny_i_arbeidslivet_sn_kilde is 'Kilde til vurdering av om bruker er ny i arbeidslivet.';

comment on column fakta_aktoer.er_nyoppstartet_fl_kilde is 'Kilde til vurdering av om bruker er nyoppstartet frilans.';

comment on column fakta_aktoer.har_fl_mottatt_ytelse_kilde is 'Kilde til vurdering av om det er mottatt ytelse for frilans.';

comment on column fakta_aktoer.mottar_etterlonn_sluttpakke_kilde is 'Kilde til vurdering av om bruker mottar etterlønn/sluttpakke.';

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

create index idx_avklaringsbehov_10
    on avklaringsbehov (kobling_id);

create index idx_avklaringsbehov_12
    on avklaringsbehov (avklaringsbehov_status);

create index idx_avklaringsbehov_11
    on avklaringsbehov (avklaringsbehov_def);

create table kobling_relasjon
(
    id                  bigint                                       not null
        constraint pk_kobling_relasjon
            primary key,
    kobling_id          bigint                                       not null
        constraint fk_kobling_relasjon_1
            references kobling,
    original_kobling_id bigint                                       not null
        constraint fk_kobling_relasjon_2
            references kobling,
    versjon             int          default 0                       not null,
    opprettet_av        varchar(20)  default 'VL'::character varying not null,
    opprettet_tid       timestamp(3) default LOCALTIMESTAMP          not null,
    endret_av           varchar(20),
    endret_tid          timestamp(3)
);

comment on table kobling_relasjon is 'Definerer relasjon mellom to koblinger';

comment on column kobling_relasjon.id is 'Primærnøkkel';

comment on column kobling_relasjon.kobling_id is 'FK: Id for Kobling som revurderer perioden til original kobling';

comment on column kobling_relasjon.original_kobling_id is 'FK: Id for original kobling';

create index idx_kobling_relasjon_1
    on kobling_relasjon (kobling_id, original_kobling_id);

create index idx_kobling_relasjon_2
    on kobling_relasjon (kobling_id);

create table regel_sporing_periode
(
    id                    bigint                                       not null
        constraint pk_regel_sporing_periode
            primary key,
    kobling_id            bigint                                       not null
        constraint fk_regel_sporing_periode_01
            references kobling,
    fom                   timestamp(0)                                 not null,
    tom                   timestamp(0)                                 not null,
    regel_type            varchar(100)                                 not null,
    aktiv                 boolean      default false                   not null,
    regel_evaluering_json text,
    regel_input_json      text,
    regel_versjon         varchar(100),
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
