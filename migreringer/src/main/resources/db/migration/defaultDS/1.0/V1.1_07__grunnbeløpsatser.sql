  CREATE TABLE BR_SATS (
    ID BIGINT NOT NULL,
    SATS_TYPE VARCHAR(100) NOT NULL,
	FOM TIMESTAMP(0) NOT NULL,
	TOM TIMESTAMP(0) NOT NULL,
	VERDI NUMERIC(10,0) NOT NULL,
    VERSJON  BIGINT DEFAULT 0 NOT NULL,
	OPPRETTET_AV VARCHAR(20) DEFAULT 'VL' NOT NULL,
	OPPRETTET_TID TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
	ENDRET_AV VARCHAR(20),
	ENDRET_TID TIMESTAMP (3)
   );

create UNIQUE index PK_SATS on BR_SATS (ID);
alter table BR_SATS add constraint PK_SATS primary key using index PK_SATS;

COMMENT ON COLUMN BR_SATS.ID IS 'Primary Key';
COMMENT ON COLUMN BR_SATS.SATS_TYPE IS 'Beskrivelse av satstype';
COMMENT ON COLUMN BR_SATS.FOM IS 'Gyldig Fra-Og-Med';
COMMENT ON COLUMN BR_SATS.TOM IS 'Gyldig Til-Og-Med';
COMMENT ON COLUMN BR_SATS.VERDI IS 'Sats verdi.';
COMMENT ON TABLE BR_SATS  IS 'Satser brukt ifm beregning av ytelser';

create sequence if not exists SEQ_BR_SATS increment by 50 minvalue 1000000;


-- VERDIER
-------------------------------------------------
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'ENGANG', to_timestamp('01/01/2017', 'dd/mm/rrrr'),to_timestamp('31/12/2017', 'dd/mm/rrrr'),61120,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'ENGANG',to_timestamp('01/01/2018', 'dd/mm/rrrr'),to_timestamp('31/12/2018', 'dd/mm/rrrr'),63140,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'ENGANG',to_timestamp('01/01/2016', 'dd/mm/rrrr'),to_timestamp('31/12/2016', 'dd/mm/rrrr'),46000,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/2017', 'dd/mm/rrrr'),to_timestamp('30/04/2018', 'dd/mm/rrrr'),93634,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/2017', 'dd/mm/rrrr'),to_timestamp('31/12/2017', 'dd/mm/rrrr'),93281,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/2016', 'dd/mm/rrrr'),to_timestamp('30/04/2017', 'dd/mm/rrrr'),92576,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/2016', 'dd/mm/rrrr'),to_timestamp('31/12/2016', 'dd/mm/rrrr'),91740,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/2015', 'dd/mm/rrrr'),to_timestamp('30/04/2016', 'dd/mm/rrrr'),90068,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/2015', 'dd/mm/rrrr'),to_timestamp('31/12/2015', 'dd/mm/rrrr'),89502,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/2014', 'dd/mm/rrrr'),to_timestamp('30/04/2015', 'dd/mm/rrrr'),88370,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/2014', 'dd/mm/rrrr'),to_timestamp('31/12/2014', 'dd/mm/rrrr'),87328,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/2013', 'dd/mm/rrrr'),to_timestamp('30/04/2014', 'dd/mm/rrrr'),85245,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/2013', 'dd/mm/rrrr'),to_timestamp('31/12/2013', 'dd/mm/rrrr'),84204,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/2012', 'dd/mm/rrrr'),to_timestamp('30/04/2013', 'dd/mm/rrrr'),82122,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/2012', 'dd/mm/rrrr'),to_timestamp('31/12/2012', 'dd/mm/rrrr'),81153,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/2011', 'dd/mm/rrrr'),to_timestamp('30/04/2012', 'dd/mm/rrrr'),79216,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/2011', 'dd/mm/rrrr'),to_timestamp('31/12/2011', 'dd/mm/rrrr'),78024,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/2010', 'dd/mm/rrrr'),to_timestamp('30/04/2011', 'dd/mm/rrrr'),75641,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/2010', 'dd/mm/rrrr'),to_timestamp('31/12/2010', 'dd/mm/rrrr'),74721,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/2009', 'dd/mm/rrrr'),to_timestamp('30/04/2010', 'dd/mm/rrrr'),72881,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/2009', 'dd/mm/rrrr'),to_timestamp('31/12/2009', 'dd/mm/rrrr'),72006,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/2008', 'dd/mm/rrrr'),to_timestamp('30/04/2009', 'dd/mm/rrrr'),70256,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/2008', 'dd/mm/rrrr'),to_timestamp('31/12/2008', 'dd/mm/rrrr'),69108,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/2007', 'dd/mm/rrrr'),to_timestamp('30/04/2008', 'dd/mm/rrrr'),66812,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/2007', 'dd/mm/rrrr'),to_timestamp('31/12/2007', 'dd/mm/rrrr'),65505,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/2006', 'dd/mm/rrrr'),to_timestamp('30/04/2007', 'dd/mm/rrrr'),62892,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/2006', 'dd/mm/rrrr'),to_timestamp('31/12/2006', 'dd/mm/rrrr'),62161,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/2005', 'dd/mm/rrrr'),to_timestamp('30/04/2006', 'dd/mm/rrrr'),60699,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/2005', 'dd/mm/rrrr'),to_timestamp('31/12/2005', 'dd/mm/rrrr'),60059,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/2004', 'dd/mm/rrrr'),to_timestamp('30/04/2005', 'dd/mm/rrrr'),58778,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/2004', 'dd/mm/rrrr'),to_timestamp('31/12/2004', 'dd/mm/rrrr'),58139,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/2003', 'dd/mm/rrrr'),to_timestamp('30/04/2004', 'dd/mm/rrrr'),56861,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/2003', 'dd/mm/rrrr'),to_timestamp('31/12/2003', 'dd/mm/rrrr'),55964,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/2002', 'dd/mm/rrrr'),to_timestamp('30/04/2003', 'dd/mm/rrrr'),54170,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/2002', 'dd/mm/rrrr'),to_timestamp('31/12/2002', 'dd/mm/rrrr'),53233,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/2001', 'dd/mm/rrrr'),to_timestamp('30/04/2002', 'dd/mm/rrrr'),51360,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/2001', 'dd/mm/rrrr'),to_timestamp('31/12/2001', 'dd/mm/rrrr'),50603,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/2000', 'dd/mm/rrrr'),to_timestamp('30/04/2001', 'dd/mm/rrrr'),49090,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/2000', 'dd/mm/rrrr'),to_timestamp('31/12/2000', 'dd/mm/rrrr'),48377,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/1999', 'dd/mm/rrrr'),to_timestamp('30/04/2000', 'dd/mm/rrrr'),46950,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1999', 'dd/mm/rrrr'),to_timestamp('31/12/1999', 'dd/mm/rrrr'),46423,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/1998', 'dd/mm/rrrr'),to_timestamp('30/04/1999', 'dd/mm/rrrr'),45370,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1998', 'dd/mm/rrrr'),to_timestamp('31/12/1998', 'dd/mm/rrrr'),44413,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/1997', 'dd/mm/rrrr'),to_timestamp('30/04/1998', 'dd/mm/rrrr'),42500,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1997', 'dd/mm/rrrr'),to_timestamp('31/12/1997', 'dd/mm/rrrr'),42000,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/1996', 'dd/mm/rrrr'),to_timestamp('30/04/1997', 'dd/mm/rrrr'),41000,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1996', 'dd/mm/rrrr'),to_timestamp('31/12/1996', 'dd/mm/rrrr'),40410,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/1995', 'dd/mm/rrrr'),to_timestamp('30/04/1996', 'dd/mm/rrrr'),39230,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1995', 'dd/mm/rrrr'),to_timestamp('31/12/1995', 'dd/mm/rrrr'),38847,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/1994', 'dd/mm/rrrr'),to_timestamp('30/04/1995', 'dd/mm/rrrr'),38080,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1994', 'dd/mm/rrrr'),to_timestamp('31/12/1994', 'dd/mm/rrrr'),37820,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/1993', 'dd/mm/rrrr'),to_timestamp('30/04/1994', 'dd/mm/rrrr'),37300,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1993', 'dd/mm/rrrr'),to_timestamp('31/12/1993', 'dd/mm/rrrr'),37033,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/1992', 'dd/mm/rrrr'),to_timestamp('30/04/1993', 'dd/mm/rrrr'),36500,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1992', 'dd/mm/rrrr'),to_timestamp('31/12/1992', 'dd/mm/rrrr'),36167,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/1991', 'dd/mm/rrrr'),to_timestamp('30/04/1992', 'dd/mm/rrrr'),35500,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1991', 'dd/mm/rrrr'),to_timestamp('31/12/1991', 'dd/mm/rrrr'),35033,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/1990', 'dd/mm/rrrr'),to_timestamp('30/11/1990', 'dd/mm/rrrr'),34000,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/12/1990', 'dd/mm/rrrr'),to_timestamp('30/04/1991', 'dd/mm/rrrr'),34100,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1990', 'dd/mm/rrrr'),to_timestamp('31/12/1990', 'dd/mm/rrrr'),33575,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/04/1989', 'dd/mm/rrrr'),to_timestamp('30/04/1990', 'dd/mm/rrrr'),32700,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1989', 'dd/mm/rrrr'),to_timestamp('31/12/1989', 'dd/mm/rrrr'),32275,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/01/1988', 'dd/mm/rrrr'),to_timestamp('31/03/1988', 'dd/mm/rrrr'),30400,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/04/1988', 'dd/mm/rrrr'),to_timestamp('31/03/1989', 'dd/mm/rrrr'),31000,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1988', 'dd/mm/rrrr'),to_timestamp('31/12/1988', 'dd/mm/rrrr'),30850,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/1987', 'dd/mm/rrrr'),to_timestamp('31/12/1987', 'dd/mm/rrrr'),29900,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1987', 'dd/mm/rrrr'),to_timestamp('31/12/1987', 'dd/mm/rrrr'),29267,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/01/1986', 'dd/mm/rrrr'),to_timestamp('30/04/1986', 'dd/mm/rrrr'),26300,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/1986', 'dd/mm/rrrr'),to_timestamp('30/04/1987', 'dd/mm/rrrr'),28000,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1986', 'dd/mm/rrrr'),to_timestamp('31/12/1986', 'dd/mm/rrrr'),27433,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/1985', 'dd/mm/rrrr'),to_timestamp('31/12/1985', 'dd/mm/rrrr'),25900,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1985', 'dd/mm/rrrr'),to_timestamp('31/12/1985', 'dd/mm/rrrr'),25333,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/1984', 'dd/mm/rrrr'),to_timestamp('30/04/1985', 'dd/mm/rrrr'),24200,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1984', 'dd/mm/rrrr'),to_timestamp('31/12/1984', 'dd/mm/rrrr'),23667,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/01/1983', 'dd/mm/rrrr'),to_timestamp('30/04/1983', 'dd/mm/rrrr'),21800,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/1983', 'dd/mm/rrrr'),to_timestamp('30/04/1984', 'dd/mm/rrrr'),22600,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1983', 'dd/mm/rrrr'),to_timestamp('31/12/1983', 'dd/mm/rrrr'),22333,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/1982', 'dd/mm/rrrr'),to_timestamp('31/12/1982', 'dd/mm/rrrr'),21200,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1982', 'dd/mm/rrrr'),to_timestamp('31/12/1982', 'dd/mm/rrrr'),20667,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/01/1981', 'dd/mm/rrrr'),to_timestamp('30/04/1981', 'dd/mm/rrrr'),17400,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/1981', 'dd/mm/rrrr'),to_timestamp('30/09/1981', 'dd/mm/rrrr'),19100,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/10/1981', 'dd/mm/rrrr'),to_timestamp('30/04/1982', 'dd/mm/rrrr'),19600,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1981', 'dd/mm/rrrr'),to_timestamp('31/12/1981', 'dd/mm/rrrr'),18658,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/01/1980', 'dd/mm/rrrr'),to_timestamp('30/04/1980', 'dd/mm/rrrr'),16100,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/1980', 'dd/mm/rrrr'),to_timestamp('31/12/1980', 'dd/mm/rrrr'),16900,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1980', 'dd/mm/rrrr'),to_timestamp('31/12/1980', 'dd/mm/rrrr'),16633,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/01/1979', 'dd/mm/rrrr'),to_timestamp('31/12/1979', 'dd/mm/rrrr'),15200,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1979', 'dd/mm/rrrr'),to_timestamp('31/12/1979', 'dd/mm/rrrr'),15200,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/07/1978', 'dd/mm/rrrr'),to_timestamp('31/12/1978', 'dd/mm/rrrr'),14700,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1978', 'dd/mm/rrrr'),to_timestamp('31/12/1978', 'dd/mm/rrrr'),14550,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/01/1977', 'dd/mm/rrrr'),to_timestamp('30/04/1977', 'dd/mm/rrrr'),13100,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/1977', 'dd/mm/rrrr'),to_timestamp('30/11/1977', 'dd/mm/rrrr'),13400,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/12/1977', 'dd/mm/rrrr'),to_timestamp('30/06/1978', 'dd/mm/rrrr'),14400,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1977', 'dd/mm/rrrr'),to_timestamp('31/12/1977', 'dd/mm/rrrr'),13383,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/01/1976', 'dd/mm/rrrr'),to_timestamp('30/04/1976', 'dd/mm/rrrr'),11800,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/1976', 'dd/mm/rrrr'),to_timestamp('31/12/1976', 'dd/mm/rrrr'),12100,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1976', 'dd/mm/rrrr'),to_timestamp('31/12/1976', 'dd/mm/rrrr'),12000,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/01/1975', 'dd/mm/rrrr'),to_timestamp('30/04/1975', 'dd/mm/rrrr'),10400,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/1975', 'dd/mm/rrrr'),to_timestamp('31/12/1975', 'dd/mm/rrrr'),11000,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1975', 'dd/mm/rrrr'),to_timestamp('31/12/1975', 'dd/mm/rrrr'),10800,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/01/1974', 'dd/mm/rrrr'),to_timestamp('30/04/1974', 'dd/mm/rrrr'),9200,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/1974', 'dd/mm/rrrr'),to_timestamp('31/12/1974', 'dd/mm/rrrr'),9700,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1974', 'dd/mm/rrrr'),to_timestamp('31/12/1974', 'dd/mm/rrrr'),9533,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/01/1973', 'dd/mm/rrrr'),to_timestamp('31/12/1973', 'dd/mm/rrrr'),8500,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1973', 'dd/mm/rrrr'),to_timestamp('31/12/1973', 'dd/mm/rrrr'),8500,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/01/1972', 'dd/mm/rrrr'),to_timestamp('31/12/1972', 'dd/mm/rrrr'),7900,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1972', 'dd/mm/rrrr'),to_timestamp('31/12/1972', 'dd/mm/rrrr'),7900,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/01/1971', 'dd/mm/rrrr'),to_timestamp('30/04/1971', 'dd/mm/rrrr'),7200,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/1971', 'dd/mm/rrrr'),to_timestamp('31/12/1971', 'dd/mm/rrrr'),7500,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1971', 'dd/mm/rrrr'),to_timestamp('31/12/1971', 'dd/mm/rrrr'),7400,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/01/1970', 'dd/mm/rrrr'),to_timestamp('31/12/1970', 'dd/mm/rrrr'),6800,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1970', 'dd/mm/rrrr'),to_timestamp('31/12/1970', 'dd/mm/rrrr'),6800,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/01/1969', 'dd/mm/rrrr'),to_timestamp('31/12/1969', 'dd/mm/rrrr'),6400,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1969', 'dd/mm/rrrr'),to_timestamp('31/12/1969', 'dd/mm/rrrr'),6400,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/01/1968', 'dd/mm/rrrr'),to_timestamp('31/12/1968', 'dd/mm/rrrr'),5900,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1968', 'dd/mm/rrrr'),to_timestamp('31/12/1968', 'dd/mm/rrrr'),5900,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/01/1967', 'dd/mm/rrrr'),to_timestamp('31/12/1967', 'dd/mm/rrrr'),5400,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/1967', 'dd/mm/rrrr'),to_timestamp('31/12/1967', 'dd/mm/rrrr'),5400,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/2018', 'dd/mm/rrrr'),to_timestamp('30/04/2019', 'dd/mm/rrrr'),96883,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/2018', 'dd/mm/rrrr'),to_timestamp('31/12/2018', 'dd/mm/rrrr'),95800,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'ENGANG',to_timestamp('01/01/2019', 'dd/mm/rrrr'),to_timestamp('31/12/2019', 'dd/mm/rrrr'),83140,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'ENGANG',to_timestamp('01/01/2010', 'dd/mm/rrrr'),to_timestamp('31/12/2013', 'dd/mm/rrrr'),35263,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GRUNNBELØP',to_timestamp('01/05/2019', 'dd/mm/rrrr'),to_timestamp('31/12/2099', 'dd/mm/rrrr'),99858,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'GSNITT',to_timestamp('01/01/2019', 'dd/mm/rrrr'),to_timestamp('31/12/2019', 'dd/mm/rrrr'),98866,0);
INSERT INTO BR_SATS (ID,SATS_TYPE,FOM,TOM,VERDI,VERSJON) VALUES (nextval('SEQ_BR_SATS'),'ENGANG',to_timestamp('01/01/2020', 'dd/mm/rrrr'),to_timestamp('31/12/9999', 'dd/mm/rrrr'),84720,0);
