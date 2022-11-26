create table if not exists TILKOMMET_INNTEKT
(
    ID                          BIGINT                                 NOT NULL,
    BG_PERIODE_ID               BIGINT                                 NOT NULL,
    AKTIVITET_STATUS            VARCHAR(100)                           NOT NULL,
    ARBEIDSGIVER_ORGNR          VARCHAR(100),
    ARBEIDSGIVER_AKTOR_ID       VARCHAR(100),
    BRUTTO_INNTEKT_PR_AAR       NUMERIC(19, 2)                         NOT NULL,
    TILKOMMET_INNTEKT_PR_AAR    NUMERIC(19, 2)                         NOT NULL,
    VERSJON                     BIGINT       DEFAULT 0                 NOT NULL,
    OPPRETTET_AV                VARCHAR(20)  DEFAULT 'VL'              NOT NULL,
    OPPRETTET_TID               TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ENDRET_AV                   VARCHAR(20),
    ENDRET_TID                  TIMESTAMP(3)
);

create UNIQUE index if not exists PK_TILKOMMET_INNTEKT on TILKOMMET_INNTEKT (ID);
create index if not exists IDX_TILKOMMET_INNTEKT on TILKOMMET_INNTEKT (BG_PERIODE_ID);

alter table if exists TILKOMMET_INNTEKT add constraint PK_TILKOMMET_INNTEKT primary key using index PK_TILKOMMET_INNTEKT;
alter table if exists TILKOMMET_INNTEKT add constraint FK_TILKOMMET_INNTEKT foreign key (BG_PERIODE_ID) references BEREGNINGSGRUNNLAG_PERIODE (ID);

create sequence if not exists SEQ_TILKOMMET_INNTEKT increment by 50 minvalue 1000000;


comment on table TILKOMMET_INNTEKT is 'Tabell som lagrer tilkomne aktiviteter som skal benyttes i gradering mot inntekt';
comment on column TILKOMMET_INNTEKT.ID is 'Primary Key';
comment on column TILKOMMET_INNTEKT.BG_PERIODE_ID is 'FK Fremmednøkkel til periode';
comment on column TILKOMMET_INNTEKT.AKTIVITET_STATUS is 'Aktivitetstatus til aktivitet';
comment on column TILKOMMET_INNTEKT.ARBEIDSGIVER_ORGNR is 'Orgnr dersom arbeidsgiver er virksomhet';
comment on column TILKOMMET_INNTEKT.ARBEIDSGIVER_AKTOR_ID is 'Aktørid dersom arbeidsgiver er privatperson';
comment on column TILKOMMET_INNTEKT.BRUTTO_INNTEKT_PR_AAR is 'Full årsinntekt for aktiviteten';
comment on column TILKOMMET_INNTEKT.TILKOMMET_INNTEKT_PR_AAR is 'Tilkommet årsinntekt for aktiviteten';
