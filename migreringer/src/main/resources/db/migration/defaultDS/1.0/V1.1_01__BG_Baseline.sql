-- Tabeller
create table if not exists BEREGNINGSGRUNNLAG
(
    ID                            BIGINT                                 NOT NULL,
    SKJARINGSTIDSPUNKT            TIMESTAMP(0)                           NOT NULL,
    VERSJON                       BIGINT       DEFAULT 0                 NOT NULL,
    OPPRETTET_AV                  VARCHAR(20)  DEFAULT 'VL'              NOT NULL,
    OPPRETTET_TID                 TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ENDRET_AV                     VARCHAR(20),
    ENDRET_TID                    TIMESTAMP(3),
    REGELLOGG_SKJARINGSTIDSPUNKT  TEXT,
    REGELLOGG_BRUKERS_STATUS      TEXT,
    REGELINPUT_SKJARINGSTIDSPUNKT TEXT,
    REGELINPUT_BRUKERS_STATUS     TEXT,
    GRUNNBELOEP                   NUMERIC(12, 2),
    REGELINPUT_PERIODISERING      TEXT,
    OVERSTYRT                     BOOLEAN      DEFAULT false             NOT NULL
);

create table if not exists BEREGNINGSGRUNNLAG_PERIODE
(
    ID                            BIGINT                                 NOT NULL,
    BEREGNINGSGRUNNLAG_ID         BIGINT                                 NOT NULL,
    BG_PERIODE_FOM                TIMESTAMP(0)                           NOT NULL,
    BG_PERIODE_TOM                TIMESTAMP(0),
    BRUTTO_PR_AAR                 NUMERIC(19, 2),
    AVKORTET_PR_AAR               NUMERIC(19, 2),
    REDUSERT_PR_AAR               NUMERIC(19, 2),
    VERSJON                       BIGINT       DEFAULT 0                 NOT NULL,
    OPPRETTET_AV                  VARCHAR(20)  DEFAULT 'VL'              NOT NULL,
    OPPRETTET_TID                 TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ENDRET_AV                     VARCHAR(20),
    ENDRET_TID                    TIMESTAMP(3),
    REGEL_EVALUERING              TEXT,
    REGEL_EVALUERING_FASTSETT     TEXT,
    REGEL_INPUT                   TEXT,
    REGEL_INPUT_FASTSETT          TEXT,
    DAGSATS                       BIGINT,
    REGEL_INPUT_FASTSETT_2        TEXT,
    REGEL_EVALUERING_FASTSETT_2   TEXT,
    REGEL_INPUT_VILKAR            TEXT,
    REGEL_EVALUERING_VILKAR       TEXT,
    REGEL_INPUT_OPPDATER_SVP      TEXT,
    REGEL_EVALUERING_OPPDATER_SVP TEXT
);

create table if not exists BG_AKTIVITET
(
    ID                        BIGINT                                 NOT NULL,
    FOM                       TIMESTAMP(0)                           NOT NULL,
    TOM                       TIMESTAMP(0)                           NOT NULL,
    BG_AKTIVITETER_ID         BIGINT                                 NOT NULL,
    OPPTJENING_AKTIVITET_TYPE VARCHAR(100)                           NOT NULL,
    ARBEIDSGIVER_AKTOR_ID     VARCHAR(100),
    VERSJON                   BIGINT       DEFAULT 0                 NOT NULL,
    OPPRETTET_AV              VARCHAR(20)  DEFAULT 'VL'              NOT NULL,
    OPPRETTET_TID             TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ENDRET_AV                 VARCHAR(20),
    ENDRET_TID                TIMESTAMP(3),
    ARBEIDSGIVER_ORGNR        VARCHAR(100),
    ARBEIDSFORHOLD_INTERN_ID  UUID
);

create table if not exists BG_AKTIVITET_OVERSTYRING
(
    ID                        BIGINT                                 NOT NULL,
    BA_OVERSTYRINGER_ID       BIGINT                                 NOT NULL,
    HANDLING_TYPE             VARCHAR(100)                           NOT NULL,
    OPPTJENING_AKTIVITET_TYPE VARCHAR(100)                           NOT NULL,
    FOM                       TIMESTAMP(0)                           NOT NULL,
    TOM                       TIMESTAMP(0)                           NOT NULL,
    ARBEIDSGIVER_ORGNR        VARCHAR(100),
    ARBEIDSGIVER_AKTOR_ID     VARCHAR(100),
    VERSJON                   BIGINT       DEFAULT 0                 NOT NULL,
    OPPRETTET_AV              VARCHAR(20)  DEFAULT 'VL'              NOT NULL,
    OPPRETTET_TID             TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ENDRET_AV                 VARCHAR(20),
    ENDRET_TID                TIMESTAMP(3),
    ARBEIDSFORHOLD_INTERN_ID  UUID
);

create table if not exists BG_AKTIVITET_OVERSTYRINGER
(
    ID            BIGINT                                 NOT NULL,
    VERSJON       BIGINT       DEFAULT 0                 NOT NULL,
    OPPRETTET_AV  VARCHAR(20)  DEFAULT 'VL'              NOT NULL,
    OPPRETTET_TID TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ENDRET_AV     VARCHAR(20),
    ENDRET_TID    TIMESTAMP(3)
);

create table if not exists BG_AKTIVITET_STATUS
(
    ID                    BIGINT                                 NOT NULL,
    BEREGNINGSGRUNNLAG_ID BIGINT                                 NOT NULL,
    AKTIVITET_STATUS      VARCHAR(100)                           NOT NULL,
    VERSJON               BIGINT       DEFAULT 0                 NOT NULL,
    OPPRETTET_AV          VARCHAR(20)  DEFAULT 'VL'              NOT NULL,
    OPPRETTET_TID         TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ENDRET_AV             VARCHAR(20),
    ENDRET_TID            TIMESTAMP(3),
    HJEMMEL               VARCHAR(100) DEFAULT '-'               NOT NULL
);

create table if not exists BG_AKTIVITETER
(
    ID                            BIGINT                                 NOT NULL,
    VERSJON                       BIGINT       DEFAULT 0                 NOT NULL,
    OPPRETTET_AV                  VARCHAR(20)  DEFAULT 'VL'              NOT NULL,
    OPPRETTET_TID                 TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ENDRET_AV                     VARCHAR(20),
    ENDRET_TID                    TIMESTAMP(3),
    SKJARINGSTIDSPUNKT_OPPTJENING TIMESTAMP(0)                           NOT NULL
);

create table if not exists BG_ANDEL_ARBEIDSFORHOLD
(
    ID                             BIGINT                                 NOT NULL,
    BG_ANDEL_ID                    BIGINT                                 NOT NULL,
    REFUSJONSKRAV_PR_AAR           NUMERIC(19, 2),
    NATURALYTELSE_BORTFALT_PR_AAR  NUMERIC(19, 2),
    NATURALYTELSE_TILKOMMET_PR_AAR NUMERIC(19, 2),
    TIDSBEGRENSET_ARBEIDSFORHOLD   VARCHAR(1),
    ARBEIDSPERIODE_FOM             TIMESTAMP(0),
    ARBEIDSPERIODE_TOM             TIMESTAMP(0),
    VERSJON                        BIGINT       DEFAULT 0                 NOT NULL,
    OPPRETTET_AV                   VARCHAR(20)  DEFAULT 'VL'              NOT NULL,
    OPPRETTET_TID                  TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ENDRET_AV                      VARCHAR(20),
    ENDRET_TID                     TIMESTAMP(3),
    ARBEIDSGIVER_AKTOR_ID          VARCHAR(100),
    LOENNSENDRING_I_PERIODEN       VARCHAR(1),
    ARBEIDSGIVER_ORGNR             VARCHAR(100),
    ARBEIDSFORHOLD_INTERN_ID       UUID
);

create table if not exists BG_ARBEIDSTAKER_ANDEL
(
    ID                    BIGINT                                 NOT NULL,
    BG_PR_STATUS_ANDEL_ID BIGINT                                 NOT NULL,
    MOTTAR_YTELSE         VARCHAR(1),
    VERSJON               BIGINT       DEFAULT 0                 NOT NULL,
    OPPRETTET_AV          VARCHAR(20)  DEFAULT 'VL'              NOT NULL,
    OPPRETTET_TID         TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ENDRET_AV             VARCHAR(20),
    ENDRET_TID            TIMESTAMP(3)
);

create table if not exists BG_FAKTA_BER_TILFELLE
(
    ID                       BIGINT                                 NOT NULL,
    FAKTA_BEREGNING_TILFELLE VARCHAR(100)                           NOT NULL,
    BEREGNINGSGRUNNLAG_ID    BIGINT                                 NOT NULL,
    VERSJON                  BIGINT       DEFAULT 0                 NOT NULL,
    OPPRETTET_AV             VARCHAR(20)  DEFAULT 'VL'              NOT NULL,
    OPPRETTET_TID            TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ENDRET_AV                VARCHAR(20),
    ENDRET_TID               TIMESTAMP(3)
);

create table if not exists BG_FRILANS_ANDEL
(
    ID                    BIGINT                                 NOT NULL,
    BG_PR_STATUS_ANDEL_ID BIGINT                                 NOT NULL,
    MOTTAR_YTELSE         VARCHAR(1),
    VERSJON               BIGINT       DEFAULT 0                 NOT NULL,
    OPPRETTET_AV          VARCHAR(20)  DEFAULT 'VL'              NOT NULL,
    OPPRETTET_TID         TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ENDRET_AV             VARCHAR(20),
    ENDRET_TID            TIMESTAMP(3),
    NYOPPSTARTET          VARCHAR(1)
);

create table if not exists BG_PERIODE_AARSAK
(
    ID             BIGINT                                 NOT NULL,
    PERIODE_AARSAK VARCHAR(100)                           NOT NULL,
    BG_PERIODE_ID  BIGINT                                 NOT NULL,
    VERSJON        BIGINT       DEFAULT 0                 NOT NULL,
    OPPRETTET_AV   VARCHAR(20)  DEFAULT 'VL'              NOT NULL,
    OPPRETTET_TID  TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ENDRET_AV      VARCHAR(20),
    ENDRET_TID     TIMESTAMP(3)
);

create table if not exists BG_PERIODE_REGEL_SPORING
(
    ID               BIGINT                                 NOT NULL,
    BG_PERIODE_ID    BIGINT                                 NOT NULL,
    REGEL_EVALUERING TEXT,
    REGEL_INPUT      TEXT,
    REGEL_TYPE       VARCHAR(100)                           NOT NULL,
    VERSJON          BIGINT       DEFAULT 0                 NOT NULL,
    OPPRETTET_AV     VARCHAR(20)  DEFAULT 'VL'              NOT NULL,
    OPPRETTET_TID    TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ENDRET_AV        VARCHAR(20),
    ENDRET_TID       TIMESTAMP(3)
);

create table if not exists BG_PR_STATUS_OG_ANDEL
(
    ID                             BIGINT                                 NOT NULL,
    BG_PERIODE_ID                  BIGINT                                 NOT NULL,
    AKTIVITET_STATUS               VARCHAR(100)                           NOT NULL,
    BEREGNINGSPERIODE_FOM          TIMESTAMP(0),
    BEREGNINGSPERIODE_TOM          TIMESTAMP(0),
    BRUTTO_PR_AAR                  NUMERIC(19, 2),
    OVERSTYRT_PR_AAR               NUMERIC(19, 2),
    AVKORTET_PR_AAR                NUMERIC(19, 2),
    REDUSERT_PR_AAR                NUMERIC(19, 2),
    BEREGNET_PR_AAR                NUMERIC(19, 2),
    VERSJON                        BIGINT       DEFAULT 0                 NOT NULL,
    OPPRETTET_AV                   VARCHAR(20)  DEFAULT 'VL'              NOT NULL,
    OPPRETTET_TID                  TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ENDRET_AV                      VARCHAR(20),
    ENDRET_TID                     TIMESTAMP(3),
    MAKSIMAL_REFUSJON_PR_AAR       NUMERIC(19, 2),
    AVKORTET_REFUSJON_PR_AAR       NUMERIC(19, 2),
    REDUSERT_REFUSJON_PR_AAR       NUMERIC(19, 2),
    AVKORTET_BRUKERS_ANDEL_PR_AAR  NUMERIC(19, 2),
    REDUSERT_BRUKERS_ANDEL_PR_AAR  NUMERIC(19, 2),
    DAGSATS_BRUKER                 BIGINT,
    DAGSATS_ARBEIDSGIVER           BIGINT,
    PGI_SNITT                      BIGINT,
    PGI1                           BIGINT,
    PGI2                           BIGINT,
    PGI3                           BIGINT,
    AARSBELOEP_TILSTOETENDE_YTELSE BIGINT,
    INNTEKTSKATEGORI               VARCHAR(100)                           NOT NULL,
    NY_I_ARBEIDSLIVET              VARCHAR(1),
    ANDELSNR                       BIGINT                                 NOT NULL,
    ARBEIDSFORHOLD_TYPE            VARCHAR(100) DEFAULT '-'               NOT NULL,
    BESTEBEREGNING_PR_AAR          NUMERIC(19, 2),
    LAGT_TIL_AV_SAKSBEHANDLER      BOOLEAN      DEFAULT false             NOT NULL,
    DAGSATS_TILSTOETENDE_YTELSE    BIGINT,
    FORDELT_PR_AAR                 NUMERIC(19, 2),
    FASTSATT_AV_SAKSBEHANDLER      BOOLEAN   DEFAULT false
);

create table if not exists BG_REFUSJON_OVERSTYRING
(
    ID                    BIGINT                                 NOT NULL,
    BR_OVERSTYRINGER_ID   BIGINT                                 NOT NULL,
    ARBEIDSGIVER_ORGNR    VARCHAR(100),
    ARBEIDSGIVER_AKTOR_ID VARCHAR(100),
    FOM                   TIMESTAMP(0)                           NOT NULL,
    VERSJON               BIGINT       DEFAULT 0                 NOT NULL,
    OPPRETTET_AV          VARCHAR(20)  DEFAULT 'VL'              NOT NULL,
    OPPRETTET_TID         TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ENDRET_AV             VARCHAR(20),
    ENDRET_TID            TIMESTAMP(3)
);

create table if not exists BG_REFUSJON_OVERSTYRINGER
(
    ID            BIGINT                                 NOT NULL,
    VERSJON       BIGINT       DEFAULT 0                 NOT NULL,
    OPPRETTET_AV  VARCHAR(20)  DEFAULT 'VL'              NOT NULL,
    OPPRETTET_TID TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ENDRET_AV     VARCHAR(20),
    ENDRET_TID    TIMESTAMP(3)
);

create table if not exists BG_REGEL_SPORING
(
    ID               BIGINT                                 NOT NULL,
    BG_ID            BIGINT                                 NOT NULL,
    REGEL_EVALUERING TEXT,
    REGEL_INPUT      TEXT,
    REGEL_TYPE       VARCHAR(100)                           NOT NULL,
    VERSJON          BIGINT       DEFAULT 0                 NOT NULL,
    OPPRETTET_AV     VARCHAR(20)  DEFAULT 'VL'              NOT NULL,
    OPPRETTET_TID    TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ENDRET_AV        VARCHAR(20),
    ENDRET_TID       TIMESTAMP(3)
);

create table if not exists BG_SG_PR_STATUS
(
    ID                          BIGINT                                 NOT NULL,
    BEREGNINGSGRUNNLAG_ID       BIGINT                                 NOT NULL,
    SAMMENLIGNINGSGRUNNLAG_TYPE VARCHAR(100)                           NOT NULL,
    SAMMENLIGNINGSPERIODE_FOM   TIMESTAMP(0)                           NOT NULL,
    SAMMENLIGNINGSPERIODE_TOM   TIMESTAMP(0)                           NOT NULL,
    RAPPORTERT_PR_AAR           NUMERIC(19, 2)                         NOT NULL,
    AVVIK_PROMILLE              BIGINT,
    VERSJON                     BIGINT       DEFAULT 0                 NOT NULL,
    OPPRETTET_AV                VARCHAR(20)  DEFAULT 'VL'              NOT NULL,
    OPPRETTET_TID               TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ENDRET_AV                   VARCHAR(20),
    ENDRET_TID                  TIMESTAMP(3),
    AVVIK_PROMILLE_NY           NUMERIC(27, 10)                        NOT NULL
);

create table if not exists GR_BEREGNINGSGRUNNLAG
(
    ID                           BIGINT                                 NOT NULL,
    KOBLING_ID                   BIGINT                                 NOT NULL,
    BEREGNINGSGRUNNLAG_ID        BIGINT,
    VERSJON                      BIGINT       DEFAULT 0                 NOT NULL,
    OPPRETTET_AV                 VARCHAR(20)  DEFAULT 'VL'              NOT NULL,
    OPPRETTET_TID                TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ENDRET_AV                    VARCHAR(20),
    ENDRET_TID                   TIMESTAMP(3),
    STEG_OPPRETTET               VARCHAR(100) DEFAULT ('-')             NOT NULL,
    AKTIV                        BOOLEAN      DEFAULT false             NOT NULL,
    REGISTER_AKTIVITETER_ID      BIGINT,
    SAKSBEHANDLET_AKTIVITETER_ID BIGINT,
    BA_OVERSTYRINGER_ID          BIGINT,
    BR_OVERSTYRINGER_ID          BIGINT
);

create table if not exists SAMMENLIGNINGSGRUNNLAG
(
    ID                        BIGINT                                 NOT NULL,
    BEREGNINGSGRUNNLAG_ID     BIGINT                                 NOT NULL,
    SAMMENLIGNINGSPERIODE_FOM TIMESTAMP(0)                           NOT NULL,
    SAMMENLIGNINGSPERIODE_TOM TIMESTAMP(0)                           NOT NULL,
    RAPPORTERT_PR_AAR         NUMERIC(19, 2)                         NOT NULL,
    VERSJON                   BIGINT       DEFAULT 0                 NOT NULL,
    OPPRETTET_AV              VARCHAR(20)  DEFAULT 'VL'              NOT NULL,
    OPPRETTET_TID             TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ENDRET_AV                 VARCHAR(20),
    ENDRET_TID                TIMESTAMP(3),
    AVVIK_PROMILLE            BIGINT,
    AVVIK_PROMILLE_NY         NUMERIC(27, 10)                        NOT NULL
);

create table KOBLING
(
    ID                BIGINT                              not null,
    KOBLING_REFERANSE UUID                                not null,
    YTELSE_TYPE       VARCHAR(100)                        not null,
    KL_YTELSE_TYPE    VARCHAR(100) default 'FAGSAK_YTELSE_TYPE',
    BRUKER_AKTOER_ID  varchar(50)                         not null,
    SAKSNUMMER        varchar(19)                         not null,
    VERSJON           BIGINT       default 0              not null,
    OPPRETTET_AV      VARCHAR(20)  default 'VL'           not null,
    OPPRETTET_TID     TIMESTAMP(3) default localtimestamp not null,
    ENDRET_AV         VARCHAR(20),
    ENDRET_TID        TIMESTAMP(3),
    constraint PK_KOBLING
        primary key (ID),
    constraint UIDX_KOBLING_1
        unique (KOBLING_REFERANSE)
);

comment on table KOBLING is 'Holder referansen som kalles på fra av eksternt system';
comment on column KOBLING.ID is 'Primærnøkkel';
comment on column KOBLING.KOBLING_REFERANSE is 'Referansenøkkel som eksponeres lokalt';
comment on column KOBLING.YTELSE_TYPE is 'Hvilken ytelse komplekset henger under';
comment on column KOBLING.BRUKER_AKTOER_ID is 'Aktøren koblingen gjelder for';
COMMENT ON COLUMN KOBLING.SAKSNUMMER IS 'Saksnummer til saken koblingen gjelder for';

-- Indekser
create index IDX_BEREGNINGSGRUNNLAG_02 on BEREGNINGSGRUNNLAG (SKJARINGSTIDSPUNKT);
create index IDX_BG_AKTIV_OVERSTYRING_11 on BG_AKTIVITET_OVERSTYRING (ARBEIDSFORHOLD_INTERN_ID);
create index IDX_BG_AKTIVITET_01 on BG_AKTIVITET (BG_AKTIVITETER_ID);
create index IDX_BG_AKTIVITET_04 on BG_AKTIVITET (ARBEIDSGIVER_ORGNR);
create index IDX_BG_AKTIVITET_11 on BG_AKTIVITET (ARBEIDSFORHOLD_INTERN_ID);
create index IDX_BG_AKTIVITET_STATUS_01 on BG_AKTIVITET_STATUS (BEREGNINGSGRUNNLAG_ID);
create index IDX_BG_AKTIVITET_STATUS_02 on BG_AKTIVITET_STATUS (AKTIVITET_STATUS);
create index IDX_BG_AKTIVITET_STATUS_03 on BG_AKTIVITET_STATUS (HJEMMEL);
create index IDX_BG_ANDEL_ARBEIDSFORHOLD_01 on BG_ANDEL_ARBEIDSFORHOLD (BG_ANDEL_ID);
create index IDX_BG_ANDEL_ARBEIDSFORHOLD_03 on BG_ANDEL_ARBEIDSFORHOLD (ARBEIDSGIVER_ORGNR);
create index IDX_BG_ANDEL_ARBEIDSFORHOLD_11 on BG_ANDEL_ARBEIDSFORHOLD (ARBEIDSFORHOLD_INTERN_ID);
create index IDX_BG_ARBEIDSTAKER_ANDEL on BG_ARBEIDSTAKER_ANDEL (BG_PR_STATUS_ANDEL_ID);
create index IDX_BG_FAKTA_BER_TILFELLE_1 on BG_FAKTA_BER_TILFELLE (BEREGNINGSGRUNNLAG_ID);
create index IDX_BG_FRILANS_ANDEL on BG_FRILANS_ANDEL (BG_PR_STATUS_ANDEL_ID);
create index IDX_BG_PERIODE_01 on BEREGNINGSGRUNNLAG_PERIODE (BEREGNINGSGRUNNLAG_ID);
create index IDX_BG_PERIODE_02 on BEREGNINGSGRUNNLAG_PERIODE (BG_PERIODE_FOM);
create index IDX_BG_PERIODE_03 on BEREGNINGSGRUNNLAG_PERIODE (BG_PERIODE_TOM);
create index IDX_BG_PERIODE_AARSAK_1 on BG_PERIODE_AARSAK (BG_PERIODE_ID);
create index IDX_BG_PERIODE_RS_01 on BG_PERIODE_REGEL_SPORING (BG_PERIODE_ID);
create index IDX_BG_PERIODE_RS_02 on BG_PERIODE_REGEL_SPORING (REGEL_TYPE);
create index IDX_BG_PR_STATUS_OG_ANDEL_01 on BG_PR_STATUS_OG_ANDEL (BG_PERIODE_ID);
create index IDX_BG_PR_STATUS_OG_ANDEL_02 on BG_PR_STATUS_OG_ANDEL (AKTIVITET_STATUS);
create index IDX_BG_RS_01 on BG_REGEL_SPORING (BG_ID);
create index IDX_BG_RS_02 on BG_REGEL_SPORING (REGEL_TYPE);
create index IDX_BG_SG_PR_STATUS_01 on BG_SG_PR_STATUS (BEREGNINGSGRUNNLAG_ID);
create index IDX_SAMMENLIGNINGSGRUNNLAG_01 on SAMMENLIGNINGSGRUNNLAG (BEREGNINGSGRUNNLAG_ID);
create index IDX_SAMMENLIGNINGSGRUNNLAG_02 on SAMMENLIGNINGSGRUNNLAG (SAMMENLIGNINGSPERIODE_FOM);
create index IDX_SAMMENLIGNINGSGRUNNLAG_03 on SAMMENLIGNINGSGRUNNLAG (SAMMENLIGNINGSPERIODE_TOM);
create index IDX_SAMMENLIGNINGSGRUNNLAG_04 on SAMMENLIGNINGSGRUNNLAG (RAPPORTERT_PR_AAR);
create index IDX_GR_BEREGNINGSGRUNNLAG_02 on GR_BEREGNINGSGRUNNLAG (REGISTER_AKTIVITETER_ID);
create index IDX_GR_BEREGNINGSGRUNNLAG_03 on GR_BEREGNINGSGRUNNLAG (SAKSBEHANDLET_AKTIVITETER_ID);
create index IDX_GR_BEREGNINGSGRUNNLAG_04 on GR_BEREGNINGSGRUNNLAG (BA_OVERSTYRINGER_ID);
create index IDX_GR_BEREGNINGSGRUNNLAG_05 on GR_BEREGNINGSGRUNNLAG (BR_OVERSTYRINGER_ID);
create index IDX_GR_BEREGNINGSGRUNNLAG_6 on GR_BEREGNINGSGRUNNLAG (KOBLING_ID);
create index IDX_GR_BEREGNINGSGRUNNLAG_7 on GR_BEREGNINGSGRUNNLAG (BEREGNINGSGRUNNLAG_ID);
create index IDX_KOBLING_1 on KOBLING (KOBLING_REFERANSE);
create index IDX_KOBLING_2 on KOBLING (SAKSNUMMER);
create index IDX_KOBLING_3 on KOBLING (YTELSE_TYPE, KL_YTELSE_TYPE);

-- Primærnøkler
create UNIQUE index PK_BEREGNINGSGRUNNLAG on BEREGNINGSGRUNNLAG (ID);
alter table BEREGNINGSGRUNNLAG
    add constraint PK_BEREGNINGSGRUNNLAG primary key using index PK_BEREGNINGSGRUNNLAG;
create UNIQUE index PK_BG_AKTIVITET on BG_AKTIVITET (ID);
alter table BG_AKTIVITET
    add constraint PK_BG_AKTIVITET primary key using index PK_BG_AKTIVITET;
create UNIQUE index PK_BG_AKTIVITET_OVERSTYRINGER on BG_AKTIVITET_OVERSTYRINGER (ID);
alter table BG_AKTIVITET_OVERSTYRINGER
    add constraint PK_BG_AKTIVITET_OVERSTYRINGER primary key using index PK_BG_AKTIVITET_OVERSTYRINGER;
create UNIQUE index PK_BG_AKTIVITET_STATUS on BG_AKTIVITET_STATUS (ID);
alter table BG_AKTIVITET_STATUS
    add constraint PK_BG_AKTIVITET_STATUS primary key using index PK_BG_AKTIVITET_STATUS;
create UNIQUE index PK_BG_AKTIVITETER on BG_AKTIVITETER (ID);
alter table BG_AKTIVITETER
    add constraint PK_BG_AKTIVITETER primary key using index PK_BG_AKTIVITETER;
create UNIQUE index PK_BG_ANDEL_ARBEIDSFORHOLD on BG_ANDEL_ARBEIDSFORHOLD (ID);
alter table BG_ANDEL_ARBEIDSFORHOLD
    add constraint PK_BG_ANDEL_ARBEIDSFORHOLD primary key using index PK_BG_ANDEL_ARBEIDSFORHOLD;
create UNIQUE index PK_BG_ARBEIDSTAKER_ANDEL on BG_ARBEIDSTAKER_ANDEL (ID);
alter table BG_ARBEIDSTAKER_ANDEL
    add constraint PK_BG_ARBEIDSTAKER_ANDEL primary key using index PK_BG_ARBEIDSTAKER_ANDEL;
create UNIQUE index PK_BG_FAKTA_BER_TILFELLE on BG_FAKTA_BER_TILFELLE (ID);
alter table BG_FAKTA_BER_TILFELLE
    add constraint PK_BG_FAKTA_BER_TILFELLE primary key using index PK_BG_FAKTA_BER_TILFELLE;
create UNIQUE index PK_BG_FRILANS_ANDEL on BG_FRILANS_ANDEL (ID);
alter table BG_FRILANS_ANDEL
    add constraint PK_BG_FRILANS_ANDEL primary key using index PK_BG_FRILANS_ANDEL;
create UNIQUE index PK_BG_PERIODE on BEREGNINGSGRUNNLAG_PERIODE (ID);
alter table BEREGNINGSGRUNNLAG_PERIODE
    add constraint PK_BEREGNINGSGRUNNLAG_PERIODE primary key using index PK_BG_PERIODE;
create UNIQUE index PK_BG_PERIODE_AARSAK on BG_PERIODE_AARSAK (ID);
alter table BG_PERIODE_AARSAK
    add constraint PK_BG_PERIODE_AARSAK primary key using index PK_BG_PERIODE_AARSAK;
create UNIQUE index PK_BG_PERIODE_REGEL_SPORING on BG_PERIODE_REGEL_SPORING (ID);
alter table BG_PERIODE_REGEL_SPORING
    add constraint PK_BG_PERIODE_REGEL_SPORING primary key using index PK_BG_PERIODE_REGEL_SPORING;
create UNIQUE index PK_BG_PR_STATUS_OG_ANDEL on BG_PR_STATUS_OG_ANDEL (ID);
alter table BG_PR_STATUS_OG_ANDEL
    add constraint PK_BG_PR_STATUS_OG_ANDEL primary key using index PK_BG_PR_STATUS_OG_ANDEL;
create UNIQUE index PK_BG_REFUSJON_OVERSTYRINGER on BG_REFUSJON_OVERSTYRINGER (ID);
alter table BG_REFUSJON_OVERSTYRINGER
    add constraint PK_BG_REFUSJON_OVERSTYRINGER primary key using index PK_BG_REFUSJON_OVERSTYRINGER;
create UNIQUE index PK_BG_REGEL_SPORING on BG_REGEL_SPORING (ID);
alter table BG_REGEL_SPORING
    add constraint PK_BG_REGEL_SPORING primary key using index PK_BG_REGEL_SPORING;
create UNIQUE index PK_BG_SG_PR_STATUS on BG_SG_PR_STATUS (ID);
alter table BG_SG_PR_STATUS
    add constraint PK_BG_SG_PR_STATUS primary key using index PK_BG_SG_PR_STATUS;
create UNIQUE index PK_SAMMENLIGNINGSGRUNNLAG on SAMMENLIGNINGSGRUNNLAG (ID);
alter table SAMMENLIGNINGSGRUNNLAG
    add constraint PK_SAMMENLIGNINGSGRUNNLAG primary key using index PK_SAMMENLIGNINGSGRUNNLAG;
create UNIQUE index PK_GR_BEREGNINGSGRUNNLAG on GR_BEREGNINGSGRUNNLAG (ID);
alter table GR_BEREGNINGSGRUNNLAG
    add constraint PK_GR_BEREGNINGSGRUNNLAG primary key using index PK_GR_BEREGNINGSGRUNNLAG;

-- Fremmednøkler
alter table BEREGNINGSGRUNNLAG_PERIODE
    add constraint FK_BG_PERIODE_1 foreign key (BEREGNINGSGRUNNLAG_ID) references BEREGNINGSGRUNNLAG (ID);
alter table BG_AKTIVITET
    add constraint FK_BG_AKTIVITET_01 foreign key (BG_AKTIVITETER_ID) references BG_AKTIVITETER (ID);
alter table BG_AKTIVITET_STATUS
    add constraint FK_BG_AKTIVITET_STATUS_1 foreign key (BEREGNINGSGRUNNLAG_ID) references BEREGNINGSGRUNNLAG (ID);
alter table BG_ANDEL_ARBEIDSFORHOLD
    add constraint FK_BG_ANDEL_ARBEIDSFORHOLD_1 foreign key (BG_ANDEL_ID) references BG_PR_STATUS_OG_ANDEL (ID);
alter table BG_ARBEIDSTAKER_ANDEL
    add constraint FK_BG_ARBEIDSTAKER_ANDEL foreign key (BG_PR_STATUS_ANDEL_ID) references BG_PR_STATUS_OG_ANDEL (ID);
alter table BG_FAKTA_BER_TILFELLE
    add constraint FK_BG_FAKTA_BER_TILFELLE_1 foreign key (BEREGNINGSGRUNNLAG_ID) references BEREGNINGSGRUNNLAG (ID);
alter table BG_FRILANS_ANDEL
    add constraint FK_BG_FRILANS_ANDEL foreign key (BG_PR_STATUS_ANDEL_ID) references BG_PR_STATUS_OG_ANDEL (ID);
alter table BG_PERIODE_AARSAK
    add constraint FK_BG_PERIODE_AARSAK_1 foreign key (BG_PERIODE_ID) references BEREGNINGSGRUNNLAG_PERIODE (ID);
alter table BG_PERIODE_REGEL_SPORING
    add constraint FK_BG_PERIODE_REGEL_SPORING_01 foreign key (BG_PERIODE_ID) references BEREGNINGSGRUNNLAG_PERIODE (ID);
alter table BG_PR_STATUS_OG_ANDEL
    add constraint FK_BG_PR_STATUS_OG_ANDEL_1 foreign key (BG_PERIODE_ID) references BEREGNINGSGRUNNLAG_PERIODE (ID);
alter table BG_REGEL_SPORING
    add constraint FK_BG_REGEL_SPORING_01 foreign key (BG_ID) references BEREGNINGSGRUNNLAG (ID);
alter table BG_SG_PR_STATUS
    add constraint FK_BG_SG_PR_STATUS_01 foreign key (BEREGNINGSGRUNNLAG_ID) references BEREGNINGSGRUNNLAG (ID);
alter table SAMMENLIGNINGSGRUNNLAG
    add constraint FK_SAMMENLIGNINGSGRUNNLAG_1 foreign key (BEREGNINGSGRUNNLAG_ID) references BEREGNINGSGRUNNLAG (ID);
alter table GR_BEREGNINGSGRUNNLAG
    add constraint FK_GR_BEREGNINGSGRUNNLAG_2 foreign key (BEREGNINGSGRUNNLAG_ID) references BEREGNINGSGRUNNLAG (ID);
alter table GR_BEREGNINGSGRUNNLAG
    add constraint FK_GR_BEREGNINGSGRUNNLAG_3 foreign key (REGISTER_AKTIVITETER_ID) references BG_AKTIVITETER (ID);
alter table GR_BEREGNINGSGRUNNLAG
    add constraint FK_GR_BEREGNINGSGRUNNLAG_4 foreign key (SAKSBEHANDLET_AKTIVITETER_ID) references BG_AKTIVITETER (ID);
alter table GR_BEREGNINGSGRUNNLAG
    add constraint FK_GR_BEREGNINGSGRUNNLAG_5 foreign key (BA_OVERSTYRINGER_ID) references BG_AKTIVITET_OVERSTYRINGER (ID);
alter table GR_BEREGNINGSGRUNNLAG
    add constraint FK_GR_BEREGNINGSGRUNNLAG_6 foreign key (BR_OVERSTYRINGER_ID) references BG_REFUSJON_OVERSTYRINGER (ID);
alter table GR_BEREGNINGSGRUNNLAG
    add constraint FK_GR_BEREGNINGSGRUNNLAG_7 foreign key (KOBLING_ID) references KOBLING (ID);

-- Kommentar på tabeller
comment on table SAMMENLIGNINGSGRUNNLAG is 'Sammenligningsgrunnlag';
comment on table BEREGNINGSGRUNNLAG_PERIODE is 'Beregningsgrunnlagsperiode';
comment on table BEREGNINGSGRUNNLAG is 'Aggregat for beregningsgrunnlag';
comment on table BG_SG_PR_STATUS is 'Sammenligningsgrunnlag pr status';
comment on table BG_REGEL_SPORING is 'Tabell som lagrer regelsporinger for beregningsgrunnlag';
comment on table GR_BEREGNINGSGRUNNLAG is 'Tabell som kobler et beregningsgrunnlag til koblingen';
comment on table BG_REFUSJON_OVERSTYRINGER is 'Tabell som knytter BG_REFUSJON_OVERSTYRING til GR_BEREGNINGSGRUNNLAG';
comment on table BG_REFUSJON_OVERSTYRING is 'Overstyringer av aktiviteter som er relevant for beregning';
comment on table BG_PR_STATUS_OG_ANDEL is 'Beregningsgrunnlag pr status og andel';
comment on table BG_PERIODE_REGEL_SPORING is 'Tabell som lagrer regelsporinger for beregningsgrunnlagperioder';
comment on table BG_PERIODE_AARSAK is 'Periodeårsaker for splitting av perioder i beregningsgrunnlag';
comment on table BG_FRILANS_ANDEL is 'Tabell for felter på andel spesifikt for frilans';
comment on table BG_FAKTA_BER_TILFELLE is 'Eit fakta om beregning tilfelle for eit beregningsgrunnlag';
comment on table BG_ARBEIDSTAKER_ANDEL is 'Tabell for felter på andel spesifikt for arbeidstaker';
comment on table BG_ANDEL_ARBEIDSFORHOLD is 'Informasjon om arbeidsforholdet knyttet til beregningsgrunnlagandelen';
comment on table BG_AKTIVITETER is 'Tabell som knytter BG_AKTIVITET til GR_BEREGNINGSGRUNNLAG';
comment on table BG_AKTIVITET_STATUS is 'Aktivitetsstatus i beregningsgrunnlag';
comment on table BG_AKTIVITET_OVERSTYRINGER is 'Tabell som knytter BG_AKTIVITET_OVERSTYRING til GR_BEREGNINGSGRUNNLAG';
comment on table BG_AKTIVITET_OVERSTYRING is 'Overstyringer av aktiviteter som er relevant for beregning';
comment on table BG_AKTIVITET is 'Aktivitet som er relevant for beregning';

-- Kommentar på kolonner
comment on column GR_BEREGNINGSGRUNNLAG.STEG_OPPRETTET is 'Hvilket steg eller vurderingspunkt grunnlaget ble opprettet i';
comment on column GR_BEREGNINGSGRUNNLAG.REGISTER_AKTIVITETER_ID is 'Aktiviteter relevant for beregning før saksbehandlers vurdering';
comment on column GR_BEREGNINGSGRUNNLAG.ID is 'Primary Key';
comment on column GR_BEREGNINGSGRUNNLAG.BR_OVERSTYRINGER_ID is 'Overstyringer av refusjon';
comment on column GR_BEREGNINGSGRUNNLAG.BEREGNINGSGRUNNLAG_ID is 'FK:BEREGNINGSGRUNNLAG Fremmednøkkel til tabell som knytter beregningsgrunnlagforekomsten til koblingen';
comment on column GR_BEREGNINGSGRUNNLAG.KOBLING_ID is 'FK: KOBLING Fremmednøkkel til koblingen som forbindes med beregningsgrunnlaget';
comment on column GR_BEREGNINGSGRUNNLAG.BA_OVERSTYRINGER_ID is 'Overstyringer av beregningaktiviteter';
comment on column BG_SG_PR_STATUS.SAMMENLIGNINGSPERIODE_TOM is 'Tom-dato for sammenligningsperiode';
comment on column BG_SG_PR_STATUS.SAMMENLIGNINGSPERIODE_FOM is 'Fom-dato for sammenligningsperiode';
comment on column BG_SG_PR_STATUS.SAMMENLIGNINGSGRUNNLAG_TYPE is 'Type av sammenligningsgrunnlag';
comment on column BG_SG_PR_STATUS.RAPPORTERT_PR_AAR is 'Rapportert inntekt pr aar i for gitt status';
comment on column BG_SG_PR_STATUS.BEREGNINGSGRUNNLAG_ID is 'FK: BEREGNINGSGRUNNLAG';
comment on column BG_SG_PR_STATUS.AVVIK_PROMILLE_NY is 'Midlertidig kolonne som skal erstatte AVVIK_PROMILLE da denne støtter høyere nøyaktighet';
comment on column BG_SG_PR_STATUS.AVVIK_PROMILLE is 'Avvik promille';
comment on column BG_REGEL_SPORING.REGEL_TYPE is 'Hvilken regel det gjelder';
comment on column BG_REGEL_SPORING.REGEL_INPUT is 'Input til regelen';
comment on column BG_REGEL_SPORING.REGEL_EVALUERING is 'Regelevaluering/logging';
comment on column BG_REGEL_SPORING.ID is 'Primary Key';
comment on column BG_REGEL_SPORING.BG_ID is 'FK: Referanse til beregningsgrunnlag';
comment on column BG_REFUSJON_OVERSTYRING.ID is 'Primary Key';
comment on column BG_REFUSJON_OVERSTYRING.BR_OVERSTYRINGER_ID is 'Arbeidsgivers orgnr';
comment on column BG_REFUSJON_OVERSTYRING.ARBEIDSGIVER_ORGNR is 'Arbeidsgivers orgnr';
comment on column BG_REFUSJON_OVERSTYRING.ARBEIDSGIVER_AKTOR_ID is 'Arbeidsgivers aktør_id';
comment on column BG_PR_STATUS_OG_ANDEL.REDUSERT_REFUSJON_PR_AAR is 'Refusjon til arbeidsgiver, redusert';
comment on column BG_PR_STATUS_OG_ANDEL.REDUSERT_PR_AAR is 'Beregningsgrunnlag, redusert';
comment on column BG_PR_STATUS_OG_ANDEL.REDUSERT_BRUKERS_ANDEL_PR_AAR is 'Brukers andel, redusert';
comment on column BG_PR_STATUS_OG_ANDEL.PGI_SNITT is 'Gjennomsnittlig pensjonsgivende inntekt';
comment on column BG_PR_STATUS_OG_ANDEL.PGI3 is 'Pensjonsgivende inntekt i år 3';
comment on column BG_PR_STATUS_OG_ANDEL.PGI2 is 'Pensjonsgivende inntekt i år 2';
comment on column BG_PR_STATUS_OG_ANDEL.PGI1 is 'Pensjonsgivende inntekt i år 1';
comment on column BG_PR_STATUS_OG_ANDEL.OVERSTYRT_PR_AAR is 'Beregningsgrunnlagsandel, overstyrt';
comment on column BG_PR_STATUS_OG_ANDEL.NY_I_ARBEIDSLIVET is 'Oppgir om bruker er ny i arbeidslivet';
comment on column BG_PR_STATUS_OG_ANDEL.MAKSIMAL_REFUSJON_PR_AAR is 'Maksimalverdi for refusjon til arbeidsgiver';
comment on column BG_PR_STATUS_OG_ANDEL.LAGT_TIL_AV_SAKSBEHANDLER is 'Angir om andel er lagt til av saksbehandler manuelt';
comment on column BG_PR_STATUS_OG_ANDEL.INNTEKTSKATEGORI is 'FK:INNTEKTSKATEGORI Fremmednøkkel til tabell med oversikt over inntektskategorier';
comment on column BG_PR_STATUS_OG_ANDEL.ID is 'Primærnøkkel';
comment on column BG_PR_STATUS_OG_ANDEL.FORDELT_PR_AAR is 'Beregningsgrunnlagsandel etter fordeling';
comment on column BG_PR_STATUS_OG_ANDEL.FASTSATT_AV_SAKSBEHANDLER is 'Angir om inntekt er fastsatt manuelt av saksbehandler';
comment on column BG_PR_STATUS_OG_ANDEL.DAGSATS_TILSTOETENDE_YTELSE is 'Original dagsats fra tilstøtende ytelse AAP/Dagpenger';
comment on column BG_PR_STATUS_OG_ANDEL.DAGSATS_BRUKER is 'Dagsats til bruker';
comment on column BG_PR_STATUS_OG_ANDEL.DAGSATS_ARBEIDSGIVER is 'Dagsats til arbeidsgiver';
comment on column BG_PR_STATUS_OG_ANDEL.BRUTTO_PR_AAR is 'Beregningsgrunnlagsandel, brutto';
comment on column BG_PR_STATUS_OG_ANDEL.BG_PERIODE_ID is 'FK: Fremmednøkkel til tabell som knytter beregningsgrunnlagsandelen til en beregningsgrunnlagperiode';
comment on column BG_PR_STATUS_OG_ANDEL.BESTEBEREGNING_PR_AAR is 'Inntekt fastsatt av saksbehandler ved besteberegning for fødende kvinne';
comment on column BG_PR_STATUS_OG_ANDEL.BEREGNINGSPERIODE_TOM is 'Siste dag i beregningsperiode';
comment on column BG_PR_STATUS_OG_ANDEL.BEREGNINGSPERIODE_FOM is 'Første dag i beregningsperiode';
comment on column BG_PR_STATUS_OG_ANDEL.BEREGNET_PR_AAR is 'Beregningsgrunnlagsandel, beregnet';
comment on column BG_PR_STATUS_OG_ANDEL.AVKORTET_REFUSJON_PR_AAR is 'Refusjon til arbeidsgiver, avkortet';
comment on column BG_PR_STATUS_OG_ANDEL.AVKORTET_PR_AAR is 'Beregningsgrunnlagsandel, avkortet';
comment on column BG_PR_STATUS_OG_ANDEL.AVKORTET_BRUKERS_ANDEL_PR_AAR is 'Brukers andel, avkortet';
comment on column BG_PR_STATUS_OG_ANDEL.ARBEIDSFORHOLD_TYPE is 'Typekode for arbeidstakeraktivitet som ikke er tilknyttet noen virksomhet';
comment on column BG_PR_STATUS_OG_ANDEL.ANDELSNR is 'Nummer for å identifisere andel innanfor ein periode';
comment on column BG_PR_STATUS_OG_ANDEL.AKTIVITET_STATUS is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
comment on column BG_PR_STATUS_OG_ANDEL.AARSBELOEP_TILSTOETENDE_YTELSE is 'Årsbeløp for tilstøtende ytelse';
comment on column BG_PERIODE_REGEL_SPORING.REGEL_TYPE is 'Hvilken regel det gjelder';
comment on column BG_PERIODE_REGEL_SPORING.REGEL_INPUT is 'Input til regelen';
comment on column BG_PERIODE_REGEL_SPORING.REGEL_EVALUERING is 'Regelevaluering/logging';
comment on column BG_PERIODE_REGEL_SPORING.ID is 'Primary Key';
comment on column BG_PERIODE_REGEL_SPORING.BG_PERIODE_ID is 'FK: Referanse til beregningsgrunnlagperiode';
comment on column BG_PERIODE_AARSAK.PERIODE_AARSAK is 'Årsak til splitting av periode';
comment on column BG_PERIODE_AARSAK.ID is 'Primary Key';
comment on column BG_FRILANS_ANDEL.NYOPPSTARTET is 'Oppgir om bruker er nyoppstartet frilans';
comment on column BG_FRILANS_ANDEL.MOTTAR_YTELSE is 'Oppgir om bruker mottar ytelse for andel';
comment on column BG_FRILANS_ANDEL.BG_PR_STATUS_ANDEL_ID is 'FK: BG_PR_STATUS_OG_ANDEL';
comment on column BG_FAKTA_BER_TILFELLE.FAKTA_BEREGNING_TILFELLE is 'FK: FAKTA_OM_BEREGNING_TILFELLE';
comment on column BG_FAKTA_BER_TILFELLE.BEREGNINGSGRUNNLAG_ID is 'FK: BEREGNINGSGRUNNLAG';
comment on column BG_ARBEIDSTAKER_ANDEL.MOTTAR_YTELSE is 'Oppgir om bruker mottar ytelse for andel';
comment on column BG_ANDEL_ARBEIDSFORHOLD.TIDSBEGRENSET_ARBEIDSFORHOLD is 'Er arbeidsforholdet tidsbegrenset';
comment on column BG_ANDEL_ARBEIDSFORHOLD.REFUSJONSKRAV_PR_AAR is 'Arbeidsgivers refusjonskrav';
comment on column BG_ANDEL_ARBEIDSFORHOLD.NATURALYTELSE_TILKOMMET_PR_AAR is 'Verdi av tilkommet naturalytelse';
comment on column BG_ANDEL_ARBEIDSFORHOLD.NATURALYTELSE_BORTFALT_PR_AAR is 'Verdi av bortfalt naturalytelse';
comment on column BG_ANDEL_ARBEIDSFORHOLD.LOENNSENDRING_I_PERIODEN is 'Er det lønnsendring i beregningsperioden';
comment on column BG_ANDEL_ARBEIDSFORHOLD.BG_ANDEL_ID is 'Beregningsgrunnlagandelen arbeidsforholdet er knyttet til';
comment on column BG_ANDEL_ARBEIDSFORHOLD.ARBEIDSPERIODE_TOM is 'Til og med dato arbeidsperiode';
comment on column BG_ANDEL_ARBEIDSFORHOLD.ARBEIDSPERIODE_FOM is 'Fra og med dato arbeidsperiode';
comment on column BG_ANDEL_ARBEIDSFORHOLD.ARBEIDSGIVER_ORGNR is 'Organisasjonsnummer for arbeidsgivere som er virksomheter';
comment on column BG_ANDEL_ARBEIDSFORHOLD.ARBEIDSGIVER_AKTOR_ID is 'Arbeidsgivers aktør id.';
comment on column BG_ANDEL_ARBEIDSFORHOLD.ARBEIDSFORHOLD_INTERN_ID is 'Globalt unikt arbeidsforhold id generert for arbeidsgiver/arbeidsforhold. I motsetning til arbeidsforhold_ekstern_id som holder arbeidsgivers referanse';
comment on column BG_AKTIVITETER.SKJARINGSTIDSPUNKT_OPPTJENING is 'Skjæringstidspunkt for opptjening';
comment on column BG_AKTIVITET_STATUS.HJEMMEL is 'Hjemmel for beregningsgrunnlag';
comment on column BG_AKTIVITET_STATUS.BEREGNINGSGRUNNLAG_ID is 'Fremmednøkkel til tabell som knytter beregningsgrunnlagsaktivitetstatusen til et beregningsgrunnlag';
comment on column BG_AKTIVITET_STATUS.AKTIVITET_STATUS is 'Referanse til KODEVERK-kolonnen i KODELISTE-tabellen';
comment on column BG_AKTIVITET_OVERSTYRING.OPPTJENING_AKTIVITET_TYPE is 'Type aktivitet som har inngått i vurdering av opptjening';
comment on column BG_AKTIVITET_OVERSTYRING.ID is 'Primary Key';
comment on column BG_AKTIVITET_OVERSTYRING.HANDLING_TYPE is 'FK: Kodeliste BEREGNING_AKTIVITET_HANDLING_TYPE';
comment on column BG_AKTIVITET_OVERSTYRING.ARBEIDSGIVER_ORGNR is 'Arbeidsgivers orgnr';
comment on column BG_AKTIVITET_OVERSTYRING.ARBEIDSGIVER_AKTOR_ID is 'Arbeidsgivers aktør_id';
comment on column BG_AKTIVITET_OVERSTYRING.ARBEIDSFORHOLD_INTERN_ID is 'Globalt unikt arbeidsforhold id generert for arbeidsgiver/arbeidsforhold. I motsetning til arbeidsforhold_ekstern_id som holder arbeidsgivers referanse';
comment on column BG_AKTIVITET.OPPTJENING_AKTIVITET_TYPE is 'Type aktivitet som har inngått i vurdering av opptjening';
comment on column BG_AKTIVITET.ID is 'Primary Key';
comment on column BG_AKTIVITET.FOM is 'Aktiviteter relevant for beregning etter saksbehandlers vurdering';
comment on column BG_AKTIVITET.ARBEIDSGIVER_ORGNR is 'Organisasjonsnummer for arbeidsgivere som er virksomheter';
comment on column BG_AKTIVITET.ARBEIDSGIVER_AKTOR_ID is 'Arbeidsgivers aktør_id';
comment on column BG_AKTIVITET.ARBEIDSFORHOLD_INTERN_ID is 'Globalt unikt arbeidsforhold id generert for arbeidsgiver/arbeidsforhold. I motsetning til arbeidsforhold_ekstern_id som holder arbeidsgivers referanse';
comment on column BEREGNINGSGRUNNLAG_PERIODE.REGEL_INPUT_VILKAR is 'Input til beregningsregel for å vurdere beregningsvilkåret, JSON';
comment on column BEREGNINGSGRUNNLAG_PERIODE.REGEL_INPUT_OPPDATER_SVP is 'Input til beregningsregel som oppdaterer beregningsgrunnlag for søkt delvis SVP, JSON';
comment on column BEREGNINGSGRUNNLAG_PERIODE.REGEL_INPUT_FASTSETT_2 is 'Input til beregningsregel fastsett beregningsgrunnlag ved andre kjøring for SVP, JSON';
comment on column BEREGNINGSGRUNNLAG_PERIODE.REGEL_INPUT_FASTSETT is 'Input til beregningsregel fastsette beregningsgrunnlag, JSON';
comment on column BEREGNINGSGRUNNLAG_PERIODE.REGEL_INPUT is 'Input til beregningsregel foreslå beregningsgrunnlag, JSON';
comment on column BEREGNINGSGRUNNLAG_PERIODE.REGEL_EVALUERING_VILKAR is 'Logg fra beregningsregel som vurderer beregningsvilkåret, JSON';
comment on column BEREGNINGSGRUNNLAG_PERIODE.REGEL_EVALUERING_OPPDATER_SVP is 'Logg fra beregningsregel som oppdaterer beregningsgrunnlag for søkt delvis SVP, JSON';
comment on column BEREGNINGSGRUNNLAG_PERIODE.REGEL_EVALUERING_FASTSETT_2 is 'Logg fra beregningsregel fastsette beregningsgrunnlag etter andre kjøring for SVP, JSON';
comment on column BEREGNINGSGRUNNLAG_PERIODE.REGEL_EVALUERING_FASTSETT is 'Logg fra beregningsregel fastsette beregningsgrunnlag, JSON';
comment on column BEREGNINGSGRUNNLAG_PERIODE.REGEL_EVALUERING is 'Logg fra beregningsregel foreslå beregningsgrunnlag, JSON';
comment on column BEREGNINGSGRUNNLAG_PERIODE.REDUSERT_PR_AAR is 'Beregningsgrunnlag, redusert';
comment on column BEREGNINGSGRUNNLAG_PERIODE.ID is 'Primærnøkkel';
comment on column BEREGNINGSGRUNNLAG_PERIODE.DAGSATS is 'Dagsats, avrundet';
comment on column BEREGNINGSGRUNNLAG_PERIODE.BRUTTO_PR_AAR is 'Beregningsgrunnlag, brutto';
comment on column BEREGNINGSGRUNNLAG_PERIODE.BG_PERIODE_TOM is 'Siste gyldighetsdag for beregningsgrunnlag';
comment on column BEREGNINGSGRUNNLAG_PERIODE.BG_PERIODE_FOM is 'Første gyldighetsdag for beregningsgrunnlag';
comment on column BEREGNINGSGRUNNLAG_PERIODE.BEREGNINGSGRUNNLAG_ID is 'Fremmednøkkel til tabell som knytter beregningsgrunnlagsperioden til et beregningsgrunnlag';
comment on column BEREGNINGSGRUNNLAG_PERIODE.AVKORTET_PR_AAR is 'Avkortet beregningsgrunnlag';
comment on column BEREGNINGSGRUNNLAG.SKJARINGSTIDSPUNKT is 'Skjæringstidspunkt for beregning';
comment on column BEREGNINGSGRUNNLAG.REGELLOGG_SKJARINGSTIDSPUNKT is 'Logg fra beregningsregel for skjæringstidspunkt, JSON';
comment on column BEREGNINGSGRUNNLAG.REGELLOGG_BRUKERS_STATUS is 'Logg fra beregningsregel for brukers status, JSON';
comment on column BEREGNINGSGRUNNLAG.REGELINPUT_SKJARINGSTIDSPUNKT is 'Input til beregningsregel for skjæringstidspunkt, JSON';
comment on column BEREGNINGSGRUNNLAG.REGELINPUT_PERIODISERING is 'Input til regelen som periodiserer beregningsgrunnlag.';
comment on column BEREGNINGSGRUNNLAG.REGELINPUT_BRUKERS_STATUS is 'Input til beregningsregel for brukers status, JSON';
comment on column BEREGNINGSGRUNNLAG.OVERSTYRT is 'Oppgir om beregningsgrunnlaget er overstyrt ved faktaavklaring';
comment on column BEREGNINGSGRUNNLAG.ID is 'Primærnøkkel';
comment on column BEREGNINGSGRUNNLAG.GRUNNBELOEP is 'Grunnbeløp (G) ved opprinnelig_skjæringstidspunkt';
comment on column SAMMENLIGNINGSGRUNNLAG.SAMMENLIGNINGSPERIODE_TOM is 'Siste dag i sammenligningsperiode';
comment on column SAMMENLIGNINGSGRUNNLAG.SAMMENLIGNINGSPERIODE_FOM is 'Første dag i sammenligningsperiode';
comment on column SAMMENLIGNINGSGRUNNLAG.RAPPORTERT_PR_AAR is 'Rapportert inntekt per år';
comment on column SAMMENLIGNINGSGRUNNLAG.ID is 'Primærnøkkel';
comment on column SAMMENLIGNINGSGRUNNLAG.BEREGNINGSGRUNNLAG_ID is 'FK:';
comment on column SAMMENLIGNINGSGRUNNLAG.AVVIK_PROMILLE_NY is 'Midlertidig kolonne som skal erstatte AVVIK_PROMILLE da denne støtter høyere nøyaktighet';
comment on column SAMMENLIGNINGSGRUNNLAG.AVVIK_PROMILLE is 'Avvik, promille';

-- Sekvenser
create sequence if not exists SEQ_GR_BEREGNINGSGRUNNLAG increment by 50 minvalue 1000000;
create sequence if not exists SEQ_BEREGNINGSGRUNNLAG increment by 50 minvalue 1000000;
create sequence if not exists SEQ_BG_AKTIVITET increment by 50 minvalue 1000000;
create sequence if not exists SEQ_BG_AKTIVITETER increment by 50 minvalue 1000000;
create sequence if not exists SEQ_BG_AKTIVITET_OVERSTYRING increment by 50 minvalue 1000000;
create sequence if not exists SEQ_BG_AKTIVITET_OVERSTYRINGER increment by 50 minvalue 1000000;
create sequence if not exists SEQ_BG_AKTIVITET_STATUS increment by 50 minvalue 1000000;
create sequence if not exists SEQ_BG_ANDEL_ARBEIDSFORHOLD increment by 50 minvalue 1000000;
create sequence if not exists SEQ_BG_ARBEIDSTAKER_ANDEL increment by 50 minvalue 1000000;
create sequence if not exists SEQ_BG_FAKTA_BER_TILFELLE increment by 50 minvalue 1000000;
create sequence if not exists SEQ_BG_FRILANS_ANDEL increment by 50 minvalue 1000000;
create sequence if not exists SEQ_BG_PERIODE increment by 50 minvalue 1000000;
create sequence if not exists SEQ_BG_PERIODE_AARSAK increment by 50 minvalue 1000000;
create sequence if not exists SEQ_BG_PERIODE_REGEL_SPORING increment by 50 minvalue 1000000;
create sequence if not exists SEQ_BG_PR_STATUS_OG_ANDEL increment by 50 minvalue 1000000;
create sequence if not exists SEQ_BG_REFUSJON_OVERSTYRING increment by 50 minvalue 1000000;
create sequence if not exists SEQ_BG_REFUSJON_OVERSTYRINGER increment by 50 minvalue 1000000;
create sequence if not exists SEQ_BG_REGEL_SPORING increment by 50 minvalue 1000000;
create sequence if not exists SEQ_BG_SG_PR_STATUS increment by 50 minvalue 1000000;
create sequence if not exists SEQ_SAMMENLIGNINGSGRUNNLAG increment by 50 minvalue 1000000;
create sequence if not exists SEQ_KOBLING MINVALUE 1 MAXVALUE 999999999999999999 INCREMENT BY 50 START WITH 1368400 NO CYCLE;
