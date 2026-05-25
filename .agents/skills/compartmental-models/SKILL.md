---
name: compartmental-models
description: Run the R-based compartmental epidemic models (SIR, SEIR, SEIQR-SCM) for cruise ship outbreak analysis. Use for parameter exploration and model comparison.
---

# Compartmental Models

## Prerequisites

- R >= 4.2
- R packages: `pomp`, `EpiModel`, `deSolve`, `ggplot2`, `tidyverse`
- Working directory: `compartmental-models/`

## Devin Secrets Needed

None — models run on local parameters.

## Install R Dependencies
```r
install.packages(c("pomp", "EpiModel", "deSolve", "ggplot2", "tidyverse", "knitr", "rmarkdown"))
```

## Available Models

| File | Model | Description |
|------|-------|-------------|
| `SIR_shipX_diamond_FINAL.Rmd` | SIR | Basic Susceptible-Infected-Recovered for Ship X and Diamond Princess |
| `SEIR_shipX_diamond_FINAL.Rmd` | SEIR | Adds Exposed compartment for latent period |
| `SEIQR_SEIR-DCM-shipX.Rmd` | SEIQR/SEIR DCM | Deterministic compartmental with quarantine |
| `SEIQR_SEIR-DCM-diamond.Rmd` | SEIQR/SEIR DCM | Diamond Princess variant |
| `SEIQR-SCM-shipX.Rmd` | SEIQR-SCM | Stochastic compartmental (passenger/crew stratified) for Ship X |
| `SEIQR-SCM-diamond.Rmd` | SEIQR-SCM | Stochastic compartmental for Diamond Princess |

## Running Models

### Render a single model report
```bash
cd compartmental-models
Rscript -e "rmarkdown::render('SIR_shipX_diamond_FINAL.Rmd')"
```

### Render all models
```bash
cd compartmental-models
for f in *.Rmd; do
  echo "=== Rendering $f ==="
  Rscript -e "rmarkdown::render('$f')"
done
```

Output: HTML reports in the same directory.

## Key Parameters

### SEIQR-SCM Model Parameters
| Parameter | Description |
|-----------|-------------|
| `mixpc` | Contact intensity between passengers and crew |
| `qfrac` | Fraction of infectious agents moved to quarantine |
| `beta` | Transmission rate |
| `sigma` | Rate of progression from Exposed to Infectious (1/incubation period) |
| `gamma` | Recovery rate (1/infectious period) |
| `N_p`, `N_c` | Population sizes for passengers and crew |

### Accumulator Variables
- `Cp` — cumulative passenger cases
- `Cc` — cumulative crew cases

## Modeling Framework

- **pomp**: Partially Observed Markov Processes — used for stochastic models with Euler-multinomial transitions
- **EpiModel**: Network-based epidemic modeling
- **deSolve**: ODE solver for deterministic compartmental models

## Relationship to Agent-Based Model

These compartmental models provide analytical baselines for comparison with the MASON-based agent-based simulation in `NorwalkVirus/`. The SEIQR-SCM model is the closest mathematical analog to the ABM's passenger/crew stratification with quarantine intervention.
