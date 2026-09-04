---
name: searching-literature-evidence
description: Search the peer-reviewed literature with the Consensus MCP server to source a norovirus parameter for the Cruise Ship X agent-based model or the compartmental models — dose-response coefficients, shedding curves, incubation and infectious duration, susceptibility, fomite transfer, touch frequency — including how a hit becomes a Javadoc or R comment citation with an evidence grade. Use whenever an epidemiological constant needs a citation, or when asked what the literature says about a mechanism. Pairs with the org-level consensus-literature-retrieval skill, which owns retrieval mechanics.
---

# Searching the Literature (Consensus MCP)

## Retrieval mechanics are in the org-level skill

Load `consensus-literature-retrieval` (`~/.agents/skills/`) before searching. It
owns the tool surface, `include_full_text_chunks: true` — which is mandatory and
returns Results, Methods and tables, including for paywalled articles — query
construction, filter behaviour, result handling, and recording which section of
the paper a number was read from.

This skill is the other half: what needs sourcing in Cruise Ship X and the
compartmental models, and what a hit is allowed to become here.

## This repo already does provenance well — match it

Existing constants cite their source at the definition, and new ones must too.
In Java (`NorwalkVirus/Source/CruiseShipModel/Person.java`):

```java
/**
 * The below coefficient values are from the paper "Norwalk virus: How infectious is it".
 * Table III. Maximum likelihood estimates.
 */
```

and in R (`compartmental-models/SEIQR-SCM-shipX.Rmd`) parameters carry inline
comments with PMC/PubMed URLs. The convention worth preserving is that the
citation names the **table or figure** the number came from, not just the paper.
Extend it with the setting and an evidence grade:

```java
/**
 * Fraction of the population that is non-secretor (FUT2-null) and therefore
 * resistant to GII.4 infection: <value> (95% CI <lo>-<hi>), <population>.
 * <Author> et al. <year>, <journal>, Table <n> (DOI: <doi>).
 * Grade B: general population genotyping standing in for the passenger mix.
 */
```

State **what was measured**, **in what population or assay**, the value with its
interval, author + year + journal + table/figure + DOI. Then grade it:

- **A** — direct measurement of this quantity in this setting (e.g. a human
  challenge study for a dose-response coefficient).
- **B** — an analogous setting or population: a surrogate virus, a different
  outbreak setting, a general-population cohort.
- **C** — inferred, estimated, or a declared assumption.

`infectInterval` (face-touch frequency) and `baseMoveRate` (3 ft/s from a scale
model) are the current Grade C examples: both have real literature behind them
(hand-to-face contact observation studies; pedestrian walking-speed
distributions) and are the obvious first candidates to upgrade.

## Query construction

- Good: `Norwalk virus shedding RT-PCR genome copies per gram stool duration challenge`
- Weak: `how long do people shed norovirus`

Quantities this repo needs sourced, and the words that find them:

- Dose-response — `human challenge`, `infectious dose`, `ID50`,
  `hypergeometric dose-response`, `aggregation`, `maximum likelihood estimate`.
- Shedding — `RT-PCR genome copies per gram`, `viral load kinetics`,
  `asymptomatic shedding`, `duration of shedding`.
- Natural history — `incubation period`, `latent period`,
  `duration of symptoms`, `post-symptomatic infectiousness`.
- Susceptibility — `secretor status`, `FUT2`, `histo-blood group antigen`,
  `homotypic immunity`, `duration of immunity`.
- Environment — `fomite transfer efficiency`, `surface persistence`,
  `inactivation kinetics`, `hand-to-face contact rate`, `vomiting aerosol`.
- Setting — `cruise ship outbreak`, `attack rate`, `case ascertainment`,
  `isolation compliance`, `secondary attack rate household`.

## Filter discipline

Filters that are specifically wrong for this repo's literature:

- `human=true` will discard the surrogate-virus and in-vitro literature — murine
  norovirus, feline calicivirus, MS2 — which is where the persistence and
  fomite-transfer measurements live. Do not set it when sourcing an
  environmental constant.
- `medical_mode=true` restricts to a curated medical subset. Reasonable for
  clinical natural history; wrong for environmental microbiology, surface
  transfer, or movement/behaviour parameters.
- Do **not** set `year_min`. The Norwalk challenge studies and the canonical
  dose-response fits this model depends on are decades old and unsuperseded.

## Check the assay before reusing a number

RT-PCR genome copies, infectious units, and ELISA-detectable antigen are three
different quantities, and a shedding curve in one is not interchangeable with
another. Likewise a dose-response coefficient is only valid for the inoculum
preparation and aggregation assumption it was fitted under — `alpha`/`beta` from
one fit cannot be mixed with `eta`/`gamma` from a different one.

Two models in this repo consume the same literature at different levels, so be
explicit about which you are sourcing:

- The **agent-based** model wants per-contact and per-dose mechanism
  parameters — transfer fractions, shedding magnitudes, touch intervals.
- The **compartmental** models want population-level rates — `param_R0`,
  `param_e_dur`, `param_i_dur`, `param_vspt`. An R0 is an *outcome* of the
  agent-based mechanism, so a literature R0 is a comparison target for the ABM,
  not an input to it. Do not source ABM mechanism parameters to reproduce it.

## What this search must never be used for

Do not screen candidate papers by which value reproduces an observed attack
rate, a VSP-reported outbreak size, or a compartmental fit. That converts a
validation into a restatement of the search.

If a sourced constant moves the simulated outbreak away from the observed
one, that is a result: report it, and do not modify tests to make it go away.
