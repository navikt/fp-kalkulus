create table if not exists REGEL_SPORING_GRUNNLAG
(
    ID               BIGINT                                 NOT NULL,
    KOBLING_ID       BIGINT                                 NOT NULL,
    REGEL_EVALUERING OID                                   NOT NULL,
    REGEL_INPUT      OID                                   NOT NULL,
    REGEL_TYPE       VARCHAR(100)                           NOT NULL,
    AKTIV            BOOLEAN      DEFAULT false             NOT NULL,
    VERSJON          BIGINT       DEFAULT 0                 NOT NULL,
    OPPRETTET_AV     VARCHAR(20)  DEFAULT 'VL'              NOT NULL,
    OPPRETTET_TID    TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ENDRET_AV        VARCHAR(20),
    ENDRET_TID       TIMESTAMP(3)
);
create index IDX_RS_GR_01 on REGEL_SPORING_GRUNNLAG (KOBLING_ID);
create index IDX_RS_GR_02 on REGEL_SPORING_GRUNNLAG (REGEL_TYPE);
create UNIQUE index PK_REGEL_SPORING_GRUNNLAG on REGEL_SPORING_GRUNNLAG (ID);
alter table REGEL_SPORING_GRUNNLAG add constraint PK_REGEL_SPORING_GRUNNLAG primary key using index PK_REGEL_SPORING_GRUNNLAG;
alter table REGEL_SPORING_GRUNNLAG add constraint FK_REGEL_SPORING_GRUNNLAG_01 foreign key (KOBLING_ID) references KOBLING (ID);
create sequence if not exists SEQ_REGEL_SPORING_GRUNNLAG increment by 50 minvalue 1000000;

comment on table REGEL_SPORING_GRUNNLAG is 'Tabell som lagrer regelsporinger for beregningsgrunnlag';
comment on column REGEL_SPORING_GRUNNLAG.REGEL_TYPE is 'Hvilken regel det gjelder';
comment on column REGEL_SPORING_GRUNNLAG.REGEL_INPUT is 'Input til regelen';
comment on column REGEL_SPORING_GRUNNLAG.REGEL_EVALUERING is 'Regelevaluering/logging';
comment on column REGEL_SPORING_GRUNNLAG.ID is 'Primary Key';
comment on column REGEL_SPORING_GRUNNLAG.KOBLING_ID is 'FK: Referanse til kobling';
comment on column REGEL_SPORING_GRUNNLAG.AKTIV is 'Sier om sporingen er aktiv';

create table if not exists REGEL_SPORING_PERIODE
(
    ID               BIGINT                                 NOT NULL,
    KOBLING_ID       BIGINT                                 NOT NULL,
    FOM              TIMESTAMP(0)                           NOT NULL,
    TOM              TIMESTAMP(0)                           NOT NULL,
    REGEL_EVALUERING OID,
    REGEL_INPUT      OID                                   NOT NULL,
    REGEL_TYPE       VARCHAR(100)                           NOT NULL,
    AKTIV            BOOLEAN      DEFAULT false             NOT NULL,
    VERSJON          BIGINT       DEFAULT 0                 NOT NULL,
    OPPRETTET_AV     VARCHAR(20)  DEFAULT 'VL'              NOT NULL,
    OPPRETTET_TID    TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ENDRET_AV        VARCHAR(20),
    ENDRET_TID       TIMESTAMP(3)
);
create index IDX_RS_PERIODE_01 on REGEL_SPORING_PERIODE (KOBLING_ID);
create index IDX_RS_PERIODE_02 on REGEL_SPORING_PERIODE (REGEL_TYPE);
create UNIQUE index PK_REGEL_SPORING_PERIODE on REGEL_SPORING_PERIODE (ID);
alter table REGEL_SPORING_PERIODE add constraint PK_REGEL_SPORING_PERIODE primary key using index PK_REGEL_SPORING_PERIODE;
alter table REGEL_SPORING_PERIODE add constraint FK_REGEL_SPORING_PERIODE_01 foreign key (KOBLING_ID) references KOBLING (ID);
create sequence if not exists SEQ_REGEL_SPORING_PERIODE increment by 50 minvalue 1000000;

comment on table REGEL_SPORING_PERIODE is 'Tabell som lagrer regelsporinger for beregningsgrunnlagperioder';
comment on column REGEL_SPORING_PERIODE.REGEL_TYPE is 'Hvilken regel det gjelder';
comment on column REGEL_SPORING_PERIODE.REGEL_INPUT is 'Input til regelen';
comment on column REGEL_SPORING_PERIODE.REGEL_EVALUERING is 'Regelevaluering/logging';
comment on column REGEL_SPORING_PERIODE.ID is 'Primary Key';
comment on column REGEL_SPORING_PERIODE.FOM is 'Fom-dato for periode som spores';
comment on column REGEL_SPORING_PERIODE.TOM is 'Tom-dato for periode som spores';
comment on column REGEL_SPORING_PERIODE.AKTIV is 'Sier om sporingen er aktiv';
