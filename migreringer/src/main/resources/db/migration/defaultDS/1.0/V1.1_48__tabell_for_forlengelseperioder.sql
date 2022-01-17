
create table if not exists FORLENGELSE_PERIODER
(
    ID                          BIGINT                              not null,
    KOBLING_ID                  BIGINT                              not null,
    AKTIV                       BOOLEAN                             not null,
    VERSJON                     BIGINT       default 0              not null,
    OPPRETTET_AV                VARCHAR(20)  default 'VL'           not null,
    OPPRETTET_TID               TIMESTAMP(3) default localtimestamp not null,
    ENDRET_AV                   VARCHAR(20),
    ENDRET_TID                  TIMESTAMP(3),
    constraint PK_FORLENGELSE_PERIODER primary key (ID)
);

alter table FORLENGELSE_PERIODER
    add constraint FK_FORLENGELSE_PERIODER_1 foreign key (KOBLING_ID) references KOBLING (ID);

create index IDX_FORLENGELSE_PERIODER_1 on FORLENGELSE_PERIODER (KOBLING_ID);

create sequence if not exists SEQ_FORLENGELSE_PERIODER increment by 50 minvalue 1000000;


comment on table FORLENGELSE_PERIODER is 'Holder på alle forlengelseperioder for en kobling';
comment on column FORLENGELSE_PERIODER.ID is 'Primærnøkkel';
comment on column FORLENGELSE_PERIODER.KOBLING_ID is 'FK: Id for Kobling';
comment on column FORLENGELSE_PERIODER.AKTIV is 'Sier om aggregatet er aktivt';




create table if not exists FORLENGELSE_PERIODE
(
    ID                                              BIGINT                                      not null,
    FOM                                             TIMESTAMP(0)                                not null,
    TOM                                             TIMESTAMP(0)                                not null,
    FORLENGELSE_PERIODER_ID                         BIGINT                                      not null,
    VERSJON                                         BIGINT       default 0                      not null,
    OPPRETTET_AV                                    VARCHAR(20)  default 'VL'                   not null,
    OPPRETTET_TID                                   TIMESTAMP(3) default localtimestamp         not null,
    ENDRET_AV                                       VARCHAR(20),
    ENDRET_TID                                      TIMESTAMP(3),
    constraint PK_FORLENGELSE_PERIODE primary key (ID)
);

alter table FORLENGELSE_PERIODE
    add constraint FK_FORLENGELSE_PERIODE_1 foreign key (FORLENGELSE_PERIODER_ID) references FORLENGELSE_PERIODER (ID);

create index IDX_FORLENGELSE_PERIODE_1 on FORLENGELSE_PERIODE (FORLENGELSE_PERIODER_ID);

create sequence if not exists SEQ_FORLENGELSE_PERIODE increment by 50 minvalue 1000000;


comment on table FORLENGELSE_PERIODE is 'Holder på periode for forlengelse.';
comment on column FORLENGELSE_PERIODE.ID is 'Primærnøkkel';
comment on column FORLENGELSE_PERIODE.FORLENGELSE_PERIODER_ID is 'FK: Id for aggregattabell';
comment on column FORLENGELSE_PERIODE.FOM is 'FOM-dato for forlengelseperiode';
comment on column FORLENGELSE_PERIODE.TOM is 'TOM-dato for forlengelseperiode';
