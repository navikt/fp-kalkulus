create table if not exists KOBLING_GRUNNLAGSKOPI_SPORING
(
    ID                          BIGINT                              not null,
    KOPIERT_TIL_KOBLING_ID      BIGINT                              not null,
    KOPIERT_FRA_KOBLING_ID      BIGINT                              not null,
    KOPIERT_GRUNNLAG_ID         BIGINT                              not null,
    AKTIV                       BOOLEAN      default true           not null,
    VERSJON                     BIGINT       default 0              not null,
    OPPRETTET_AV                VARCHAR(20)  default 'VL'           not null,
    OPPRETTET_TID               TIMESTAMP(3) default localtimestamp not null,
    ENDRET_AV                   VARCHAR(20),
    ENDRET_TID                  TIMESTAMP(3),
    constraint PK_KOBLING_GRUNNLAGSKOPI_SPORING primary key (ID)
    );

alter table KOBLING_GRUNNLAGSKOPI_SPORING
    add constraint FK_KOBLING_GRUNNLAGSKOPI_SPORING_1 foreign key (KOPIERT_TIL_KOBLING_ID) references KOBLING (ID);
alter table KOBLING_GRUNNLAGSKOPI_SPORING
    add constraint FK_KOBLING_GRUNNLAGSKOPI_SPORING_2 foreign key (KOPIERT_FRA_KOBLING_ID) references KOBLING (ID);

alter table KOBLING_GRUNNLAGSKOPI_SPORING
    add constraint FK_KOBLING_GRUNNLAGSKOPI_SPORING_3 foreign key (KOPIERT_GRUNNLAG_ID) references GR_BEREGNINGSGRUNNLAG (ID);

create index IDX_KOBLING_GRUNNLAGSKOPI_SPORING_1 on KOBLING_GRUNNLAGSKOPI_SPORING (KOPIERT_TIL_KOBLING_ID);
create unique index IDX_KOBLING_GRUNNLAGSKOPI_SPORING_2 on KOBLING_GRUNNLAGSKOPI_SPORING (KOPIERT_TIL_KOBLING_ID, KOPIERT_GRUNNLAG_ID);

create sequence if not exists SEQ_KOBLING_GRUNNLAGSKOPI_SPORING increment by 50 minvalue 1000000;


comment on table KOBLING_GRUNNLAGSKOPI_SPORING is 'Definerer kopi av grunnlag fra en kobling til en annen';
comment on column KOBLING_GRUNNLAGSKOPI_SPORING.ID is 'Primærnøkkel';
comment on column KOBLING_GRUNNLAGSKOPI_SPORING.KOPIERT_TIL_KOBLING_ID is 'FK: Id for Kobling som det kopieres til.';
comment on column KOBLING_GRUNNLAGSKOPI_SPORING.KOPIERT_FRA_KOBLING_ID is 'FK: Id for Kobling som det kopieres fra';
comment on column KOBLING_GRUNNLAGSKOPI_SPORING.KOPIERT_GRUNNLAG_ID is 'FK: Id for grunnlag som kopieres';
comment on column KOBLING_GRUNNLAGSKOPI_SPORING.AKTIV is 'Definerer om kopien er aktiv eller om den har blitt erstattet av en ny kopi av et annet grunnlag';


