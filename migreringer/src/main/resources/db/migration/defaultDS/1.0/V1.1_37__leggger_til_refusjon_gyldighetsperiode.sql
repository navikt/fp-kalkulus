create table if not exists REFUSJON_GYLDIGHETSPERIODE
(
    ID                              BIGINT                                  NOT NULL,
    BG_REFUSJON_OVERSTYRING_ID      BIGINT                                  NOT NULL,
    FOM                             TIMESTAMP(0)                            NOT NULL,
    TOM                             TIMESTAMP(0)                            NOT NULL,
    VERSJON                         BIGINT       DEFAULT 0                  NOT NULL,
    OPPRETTET_AV                    VARCHAR(20)  DEFAULT 'VL'               NOT NULL,
    OPPRETTET_TID                   TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP  NOT NULL,
    ENDRET_AV                       VARCHAR(20),
    ENDRET_TID                      TIMESTAMP(3)
    );
create UNIQUE index PK_REFUSJON_GYLDIGHETSPERIODE on REFUSJON_GYLDIGHETSPERIODE (ID);
alter table REFUSJON_GYLDIGHETSPERIODE add constraint PK_REFUSJON_GYLDIGHETSPERIODE primary key using index PK_REFUSJON_GYLDIGHETSPERIODE;
alter table REFUSJON_GYLDIGHETSPERIODE add constraint FK_REFUSJON_GYLDIGHETSPERIODE_1 foreign key (BG_REFUSJON_OVERSTYRING_ID) references BG_REFUSJON_OVERSTYRING (ID);

create sequence if not exists SEQ_REFUSJON_GYLDIGHETSPERIODE increment by 50 minvalue 1000000;

comment on table REFUSJON_GYLDIGHETSPERIODE is 'Tabell som lagrer gyldige periode for refusjon som er bekreftet av saksbehandler';
comment on column REFUSJON_GYLDIGHETSPERIODE.ID is 'Primary Key';
comment on column REFUSJON_GYLDIGHETSPERIODE.BG_REFUSJON_OVERSTYRING_ID is 'Foreign key til refusjonoverstyringer';
comment on column REFUSJON_GYLDIGHETSPERIODE.FOM is 'Fom-dato for periode';
comment on column REFUSJON_GYLDIGHETSPERIODE.TOM is 'Tom-dato for periode';
