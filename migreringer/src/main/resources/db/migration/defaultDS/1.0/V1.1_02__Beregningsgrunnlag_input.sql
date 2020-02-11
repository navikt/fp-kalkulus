-- Tabeller
create table if not exists KALKULATOR_INPUT
(
    ID            BIGINT                                 NOT NULL,
    KOBLING_ID    BIGINT                                 NOT NULL,
    INPUT         JSONB                                  NOT NULL,
    OPPRETTET_AV  VARCHAR(20)  DEFAULT 'VL'              NOT NULL,
    OPPRETTET_TID TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ENDRET_AV     VARCHAR(20),
    ENDRET_TID    TIMESTAMP(3)
);


-- Kommentar
comment on table KALKULATOR_INPUT is 'Holder referansen som kalles på fra av eksternt system';
comment on column KALKULATOR_INPUT.ID is 'Primærnøkkel';
comment on column KALKULATOR_INPUT.INPUT is 'JSON som inneholder inputdata for kalkulator (beregning)';
comment on column KALKULATOR_INPUT.KOBLING_ID is 'JSON som inneholder inputdata for kalkulator (beregning)';
comment on column KALKULATOR_INPUT.OPPRETTET_AV is 'Primærnøkkel';
comment on column KALKULATOR_INPUT.OPPRETTET_TID is 'Primærnøkkel';


-- Indekser
create index IDX_KALKULATOR_INPUT_01 on KALKULATOR_INPUT (KOBLING_ID);


-- Primærnøkler
create UNIQUE index PK_KALKULATOR_INPUT on KALKULATOR_INPUT (ID);
alter table KALKULATOR_INPUT
    add constraint PK_KALKULATOR_INPUT primary key using index PK_KALKULATOR_INPUT;


-- Fremmednøkler
alter table KALKULATOR_INPUT
    add constraint FK_KALKULATOR_INPUT_1 foreign key (KOBLING_ID) references KOBLING (ID);

-- Sekvenser
create sequence if not exists SEQ_KALKULATOR_INPUT increment by 50 minvalue 1000000;
