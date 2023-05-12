drop index idx_kobling_grunnlagskopi_sporing_2;

create unique index idx_kobling_grunnlagskopi_sporing_2
    on kobling_grunnlagskopi_sporing (kopiert_til_kobling_id, kopiert_grunnlag_id)
    where aktiv = true;

