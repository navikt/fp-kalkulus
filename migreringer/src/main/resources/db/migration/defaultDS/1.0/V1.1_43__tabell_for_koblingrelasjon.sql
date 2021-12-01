
create table if not exists KOBLING_RELASJON
(
    ID                          BIGINT                              not null,
    KOBLING_ID                  BIGINT                              not null,
    ORIGINAL_KOBLING_ID         BIGINT                              not null,
    VERSJON                     BIGINT       default 0              not null,
    OPPRETTET_AV                VARCHAR(20)  default 'VL'           not null,
    OPPRETTET_TID               TIMESTAMP(3) default localtimestamp not null,
    ENDRET_AV                   VARCHAR(20),
    ENDRET_TID                  TIMESTAMP(3),
    constraint PK_KOBLING_RELASJON primary key (ID)
);

alter table KOBLING_RELASJON
    add constraint FK_KOBLING_RELASJON_1 foreign key (KOBLING_ID) references KOBLING (ID);
alter table KOBLING_RELASJON
    add constraint FK_KOBLING_RELASJON_2 foreign key (ORIGINAL_KOBLING_ID) references KOBLING (ID);

create index IDX_KOBLING_RELASJON_1 on KOBLING_RELASJON (KOBLING_ID, ORIGINAL_KOBLING_ID);
create index IDX_KOBLING_RELASJON_2 on KOBLING_RELASJON (KOBLING_ID);

create sequence if not exists SEQ_KOBLING_RELASJON increment by 50 minvalue 1000000;


comment on table KOBLING_RELASJON is 'Definerer relasjon mellom to koblinger';
comment on column KOBLING_RELASJON.ID is 'Primærnøkkel';
comment on column KOBLING_RELASJON.KOBLING_ID is 'FK: Id for Kobling som revurderer perioden til original kobling';
comment on column KOBLING_RELASJON.ORIGINAL_KOBLING_ID is 'FK: Id for original kobling';
