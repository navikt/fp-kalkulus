# fp-kalkulus

Beregningsgrunnlag service for foreldrepenger and svangerskapspenger.

## Shared context

- Source of truth for shared domain, architecture, and conventions: `navikt/fp-context`
- Copilot Space: `navikt/TeamForeldrepenger`

## Repo-specific context

| Topic      | Details                                                                                      |
|------------|----------------------------------------------------------------------------------------------|
| Role       | Calculates beregningsgrunnlag aggregate and distributes result based on reimbursement claims |
| Consumers  | `fp-sak`                                                                                     |
| Tech stack | Standard fp Java backend                                                                     |
| Data       | PostgreSQL; FSS deployment; long-term storage of beregning aggregates                        |

Does mainly mapping and persistence, relies heavily on `ft-beregning` for the rules and logic. No integrations.

## Entry points

- `OperereKalkulusRestTjeneste`: Running the beregning process and storing saksbehandler evaluations (avklaringsbehov) 
- `HentKalkulusRestTjeneste`: Retrieving beregning aggregates for use in `fp-sak` or frontends
- `GrunnbeløpRestTjeneste`: Assisting the process of adjusting Grunnbeløp in `fp-sak` (payout limits) 

## Verification

- For integration impact, verify via `navikt/fp-autotest`.
- Relevant suites: `fpkalkulus`, `fpsak` (`BeregningVerdikjede`) , `verdikjede`.
