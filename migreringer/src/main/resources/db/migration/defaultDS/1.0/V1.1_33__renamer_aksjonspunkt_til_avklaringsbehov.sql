create table if not exists AVKLARINGSBEHOV
(
    ID                              BIGINT                                  NOT NULL,
    KOBLING_ID                      BIGINT                                  NOT NULL,
    AVKLARINGSBEHOV_DEF             VARCHAR(20)                             NOT NULL,
    AVKLARINGSBEHOV_STATUS          VARCHAR(20)                             NOT NULL,
    BEGRUNNELSE                     VARCHAR(4000),
    VERSJON                         BIGINT       DEFAULT 0                  NOT NULL,
    OPPRETTET_AV                    VARCHAR(20)  DEFAULT 'VL'               NOT NULL,
    OPPRETTET_TID                   TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP  NOT NULL,
    ENDRET_AV                       VARCHAR(20),
    ENDRET_TID                      TIMESTAMP(3)
    );
create UNIQUE index PK_AVKLARINGSBEHOV on AVKLARINGSBEHOV (ID);
alter table AVKLARINGSBEHOV add constraint PK_AVKLARINGSBEHOV primary key using index PK_AVKLARINGSBEHOV;
alter table AVKLARINGSBEHOV add constraint FK_AVKLARINGSBEHOV_01 foreign key (KOBLING_ID) references KOBLING (ID);
create sequence if not exists SEQ_AVKLARINGSBEHOV increment by 50 minvalue 1000000;

comment on table AVKLARINGSBEHOV is 'Tabell som holder på avklaringsbehov knyttet til en kobling';
comment on column AVKLARINGSBEHOV.ID is 'Primary Key';
comment on column AVKLARINGSBEHOV.KOBLING_ID is 'Fremmednøkkel som kobler avklaringsbehov til koblingen';
comment on column AVKLARINGSBEHOV.AVKLARINGSBEHOV_DEF is 'Definisjonen av avklaringsbehovet';
comment on column AVKLARINGSBEHOV.AVKLARINGSBEHOV_STATUS is 'Status på avklaringsbehovet';
comment on column AVKLARINGSBEHOV.BEGRUNNELSE is 'Saksbehandlers begrunnelse for løsningen av avklaringsbehovet';

drop table AKSJONSPUNKT;
drop SEQUENCE SEQ_AKSJONSPUNKT;
