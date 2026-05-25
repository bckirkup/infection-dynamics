---
name: build-and-run-simulation
description: Build and run the infection-dynamics Norwalk Virus cruise ship agent-based simulation. Covers JDK setup, IntelliJ project configuration, compiling, and running ShipUI.
---

# Build and Run Simulation

## Prerequisites

- JDK 11 (Azul Zulu recommended): https://www.azul.com/downloads/?version=java-11-lts&package=jdk#zulu
- IntelliJ IDEA (Community Edition): https://www.jetbrains.com/idea/download/

## Devin Secrets Needed

None — the project uses bundled JARs and GIS data.

## Project Setup

### 1. Set up project directories
```bash
mkdir -p ~/IdeaProjects
cp -r NorwalkSim ~/IdeaProjects/
cp -r NorwalkVirus ~/IdeaProjects/
mv ~/IdeaProjects/NorwalkSim/idea ~/IdeaProjects/NorwalkSim/.idea
```

### 2. Configure IntelliJ
1. Launch IntelliJ IDEA
2. Select "Open" → navigate to `~/IdeaProjects/NorwalkSim`
3. Trust the project directory when prompted
4. Right-click `NorwalkSim` → "Open Module Settings"
5. In "Dependencies" tab, set Module SDK to `zulu-11`
6. Click "Apply" → "OK"

### 3. Build the project
Right-click `NorwalkSim` in the project panel → "Rebuild Module"

### 4. Run the simulation
1. Expand `NorwalkSim/Source` in the project panel
2. Right-click `ShipUI` → "Run"
3. Two windows appear — select the `ShipUI` window with playback controls
4. Click "Run" to start the simulation
5. **Minimize the visualization window** to speed up simulation drastically

## Key Source Files

| File | Purpose |
|------|---------|
| `NorwalkVirus/Source/CruiseShipModel/ShipUI.java` | Main entry point and UI controller |
| `NorwalkVirus/Source/CruiseShipModel/Ship.java` | Core simulation model and environment |
| `NorwalkVirus/Source/CruiseShipModel/Agent.java` | Base agent class |
| `NorwalkVirus/Source/CruiseShipModel/Passenger.java` | Passenger agent behavior |
| `NorwalkVirus/Source/CruiseShipModel/Person.java` | Shared person logic |
| `NorwalkVirus/Source/CruiseShipModel/ViralParticle.java` | Pathogen particle model |
| `NorwalkVirus/Source/CruiseShipModel/ParameterList.java` | Simulation parameters |
| `NorwalkVirus/Source/CruiseShipModel/microbiome/SourceProfiles.java` | Microbial source profiles |

## Dependencies

All JARs are bundled in `NorwalkVirus/Dependencies/jars/`:
- MASON 18 (Multi-Agent Simulator of Neighborhoods)
- GeoMason (GIS extension for MASON)
- JTS 1.13 (Java Topology Suite)
- JFreeChart 1.0.17 (charting)

GeoMason source is in `NorwalkVirus/Dependencies/geomason_src/`.

## Output

Simulation logs are saved to `~/IdeaProjects/NorwalkSim/Log_YYYY-MM-DD_HH.MM.SS/`.

## Command-Line Build (Alternative)

If IntelliJ is not available, compile from the command line:
```bash
cd ~/IdeaProjects/NorwalkSim
# Ensure JAVA_HOME points to JDK 11
export JAVA_HOME=/path/to/zulu-11
find ../NorwalkVirus/Source -name "*.java" > sources.txt
find ../NorwalkVirus/Dependencies/geomason_src -name "*.java" >> sources.txt
javac -cp "../NorwalkVirus/Dependencies/jars/*" -d out @sources.txt
java -cp "out:../NorwalkVirus/Dependencies/jars/*" CruiseShipModel.ShipUI
```

## Platform Notes

- **macOS**: Visualization window can be unstable — minimize immediately after starting. Apple Silicon Macs may see variable runtimes (4-8 hours).
- **Windows/Linux**: Recommended platforms for stable execution.
- Multiple simulations can run simultaneously from the same project.
