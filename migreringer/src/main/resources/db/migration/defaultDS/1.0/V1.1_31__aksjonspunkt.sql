create table if not exists AKSJONSPUNKT
(
    ID                              BIGINT                                  NOT NULL,
    KOBLING_ID                      BIGINT                                  NOT NULL,
    AKSJONSPUNKT_DEF                VARCHAR(20)                             NOT NULL,
    AKSJONSPUNKT_STATUS             VARCHAR(20)                             NOT NULL,
    STEG_FUNNET                     VARCHAR(20)                             NOT NULL,
    BEGRUNNELSE                     VARCHAR(4000),
    VERSJON                         BIGINT       DEFAULT 0                  NOT NULL,
    OPPRETTET_AV                    VARCHAR(20)  DEFAULT 'VL'               NOT NULL,
    OPPRETTET_TID                   TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP  NOT NULL,
    ENDRET_AV                       VARCHAR(20),
    ENDRET_TID                      TIMESTAMP(3)
    );
create UNIQUE index PK_AKSJONSPUNKT on AKSJONSPUNKT (ID);
alter table AKSJONSPUNKT add constraint PK_AKSJONSPUNKT primary key using index PK_AKSJONSPUNKT;
alter table AKSJONSPUNKT add constraint FK_AKSJONSPUNKT_01 foreign key (KOBLING_ID) references KOBLING (ID);
create sequence if not exists SEQ_AKSJONSPUNKT increment by 50 minvalue 1000000;

comment on table AKSJONSPUNKT is 'Tabell som holder på aksjonspunkter knyttet til en kobling';
comment on column AKSJONSPUNKT.ID is 'Primary Key';
comment on column AKSJONSPUNKT.KOBLING_ID is 'Fremmednøkkel som kobler aksjonspunktet til koblingen';
comment on column AKSJONSPUNKT.AKSJONSPUNKT_DEF is 'Definisjonen av aksjonspunktet';
comment on column AKSJONSPUNKT.AKSJONSPUNKT_STATUS is 'Status på aksjonspunktet';
comment on column AKSJONSPUNKT.STEG_FUNNET is 'Steg der aksjonspunktet ble funnet';
comment on column AKSJONSPUNKT.BEGRUNNELSE is 'Saksbehandlers begrunnelse for løsningen av aksjonspunktet';
