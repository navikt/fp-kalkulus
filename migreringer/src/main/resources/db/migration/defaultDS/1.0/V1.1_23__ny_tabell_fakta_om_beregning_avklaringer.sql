create table if not exists FAKTA_AGGREGAT
(
    ID               BIGINT                                 NOT NULL,
    VERSJON          BIGINT       DEFAULT 0                 NOT NULL,
    OPPRETTET_AV     VARCHAR(20)  DEFAULT 'VL'              NOT NULL,
    OPPRETTET_TID    TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ENDRET_AV        VARCHAR(20),
    ENDRET_TID       TIMESTAMP(3)
);
create UNIQUE index PK_FAKTA_AGGREGAT on FAKTA_AGGREGAT (ID);
alter table FAKTA_AGGREGAT add constraint PK_FAKTA_AGGREGAT primary key using index PK_FAKTA_AGGREGAT;
create sequence if not exists SEQ_FAKTA_AGGREGAT increment by 50 minvalue 1000000;

comment on table FAKTA_AGGREGAT is 'Tabell som lagrer faktaavklaringer for beregningsgrunnlag';
comment on column FAKTA_AGGREGAT.ID is 'Primary Key';

alter table GR_BEREGNINGSGRUNNLAG
add column fakta_aggregat_id BIGINT;
comment on column GR_BEREGNINGSGRUNNLAG.fakta_aggregat_id is 'Foreign Key til faktaavklaringer';

create index IDX_GR_BEREGNINGSGRUNNLAG_08 on GR_BEREGNINGSGRUNNLAG (fakta_aggregat_id);

alter table GR_BEREGNINGSGRUNNLAG
    add constraint FK_GR_BEREGNINGSGRUNNLAG_08 foreign key (fakta_aggregat_id) references FAKTA_AGGREGAT (ID);

create table if not exists FAKTA_ARBEIDSFORHOLD
(
    ID                          BIGINT                                  NOT NULL,
    FAKTA_AGGREGAT_ID           BIGINT                                  NOT NULL,
    ARBEIDSGIVER_ORGNR          BIGINT,
    ARBEIDSGIVER_AKTOR_ID       BIGINT,
    ARBEIDSFORHOLD_INTERN_ID    BIGINT,
    ER_TIDSBEGRENSET            BOOLEAN,
    HAR_MOTTATT_YTELSE          BOOLEAN,
    VERSJON                     BIGINT       DEFAULT 0                  NOT NULL,
    OPPRETTET_AV                VARCHAR(20)  DEFAULT 'VL'               NOT NULL,
    OPPRETTET_TID               TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP  NOT NULL,
    ENDRET_AV                   VARCHAR(20),
    ENDRET_TID                  TIMESTAMP(3)
);
create UNIQUE index PK_FAKTA_ARBEIDSFORHOLD on FAKTA_ARBEIDSFORHOLD (ID);
alter table FAKTA_ARBEIDSFORHOLD add constraint PK_FAKTA_ARBEIDSFORHOLD primary key using index PK_FAKTA_ARBEIDSFORHOLD;
alter table FAKTA_ARBEIDSFORHOLD add constraint FK_FAKTA_ARBEIDSFORHOLD_01 foreign key (fakta_aggregat_id) references FAKTA_AGGREGAT (ID);
create index IDX_FAKTA_ARBEIDSFORHOLD_01 on FAKTA_ARBEIDSFORHOLD (FAKTA_AGGREGAT_ID);
create index IDX_FAKTA_ARBEIDSFORHOLD_04 on FAKTA_ARBEIDSFORHOLD (ARBEIDSGIVER_ORGNR);
create index IDX_FAKTA_ARBEIDSFORHOLD_11 on FAKTA_ARBEIDSFORHOLD (ARBEIDSFORHOLD_INTERN_ID);
create sequence if not exists SEQ_FAKTA_ARBEIDSFORHOLD increment by 50 minvalue 1000000;

comment on table FAKTA_ARBEIDSFORHOLD is 'Tabell som lagrer faktaavklaringer for arbeidsforhold';
comment on column FAKTA_ARBEIDSFORHOLD.ID is 'Primary Key';
comment on column FAKTA_ARBEIDSFORHOLD.FAKTA_AGGREGAT_ID is 'Foreign Key til faktaavklaringer';
comment on column FAKTA_ARBEIDSFORHOLD.ARBEIDSGIVER_ORGNR is 'Arbeidsgiver orgnr';
comment on column FAKTA_ARBEIDSFORHOLD.ARBEIDSFORHOLD_INTERN_ID is 'Arbeidsforhold-id';
comment on column FAKTA_ARBEIDSFORHOLD.ER_TIDSBEGRENSET is 'Er arbeidsforhold tidsbegrenset';
comment on column FAKTA_ARBEIDSFORHOLD.HAR_MOTTATT_YTELSE is 'Er det tidligere mottatt ytelse for arbeidsforholdet';


create table if not exists FAKTA_AKTOER
(
    ID                              BIGINT                                  NOT NULL,
    FAKTA_AGGREGAT_ID               BIGINT                                  NOT NULL,
    ER_NY_I_ARBEIDSLIVET_SN         BOOLEAN,
    ER_NYOPPSTARTET_FL              BOOLEAN,
    HAR_FL_MOTTATT_YTELSE           BOOLEAN,
    SKAL_BESTEBEREGNES              BOOLEAN,
    MOTTAR_ETTERLONN_SLUTTPAKKE     BOOLEAN,
    VERSJON                         BIGINT       DEFAULT 0                  NOT NULL,
    OPPRETTET_AV                    VARCHAR(20)  DEFAULT 'VL'               NOT NULL,
    OPPRETTET_TID                   TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP  NOT NULL,
    ENDRET_AV                       VARCHAR(20),
    ENDRET_TID                      TIMESTAMP(3)
);
create UNIQUE index PK_FAKTA_AKTOER on FAKTA_AKTOER (ID);
alter table FAKTA_AKTOER add constraint PK_FAKTA_AKTOER primary key using index PK_FAKTA_AKTOER;
alter table FAKTA_AKTOER add constraint FK_FAKTA_AKTOER_01 foreign key (fakta_aggregat_id) references FAKTA_AGGREGAT (ID);
create sequence if not exists SEQ_FAKTA_AKTOER increment by 50 minvalue 1000000;

comment on table FAKTA_AKTOER is 'Tabell som lagrer faktaavklaringer for arbeidsforhold';
comment on column FAKTA_AKTOER.ID is 'Primary Key';
comment on column FAKTA_AKTOER.FAKTA_AGGREGAT_ID is 'Foreign Key til faktaavklaringer';
comment on column FAKTA_AKTOER.ER_NY_I_ARBEIDSLIVET_SN is 'Er SN og ny i arbeidslivet';
comment on column FAKTA_AKTOER.ER_NYOPPSTARTET_FL is 'Er FL og nyoppstartet';
comment on column FAKTA_AKTOER.HAR_FL_MOTTATT_YTELSE is 'Har mottatt ytelse for frilansaktivitet';
comment on column FAKTA_AKTOER.SKAL_BESTEBEREGNES is 'Skal bruker besteberegnes';
comment on column FAKTA_AKTOER.MOTTAR_ETTERLONN_SLUTTPAKKE is 'Mottar bruker etterlønn/sluttpakke';
