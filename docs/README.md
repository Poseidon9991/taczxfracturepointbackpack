# [TaCZ] Fracture Point's BackPack Compat — Developer Wiki

Local architecture wiki. Small addon: ~7 source files, so docs stay flat.

## Pages

| Page | Covers |
|---|---|
| [architecture.md](architecture.md) | Module map, the three mixin hooks, search order |
| [backpack-detection.md](backpack-detection.md) | WARBORN internals — `BackpackItem`, capabilities, wear positions |
| [config-debug.md](config-debug.md) | Config keys, `[TaCZ-WB]` diagnostics, known issues |

## Quick orientation

- **Mod id:** `tacz_warborn_compat`, entry `TaczWarbornCompat.java`
- **Platform:** Minecraft 1.20.1 / Forge 47.4.0 / Java 17
- **Goal:** let TaCZ guns see + consume ammo inside WARBORN (Fracture
  Point) backpacks — worn in chest slot, Curios `backpack` slot, or
  carried in the player inventory
- **Depends on:** TaCZ `[1.1.5,)` + `fracturepoint` (mandatory), Curios
  (optional — soft-guarded), GeckoLib (compile-only, needed to resolve
  `BackpackItem`'s `GeoItem` supertype)
- **Build:** `cd 1.20.1-forge && gradlew build` →
  `build/libs/tacz_warborn_compat-1.0.0.jar`
- **License note:** WARBORN code is view/reference only (Zarra Studios).
  This addon contains no WARBORN code — public API calls only.

## Sister project

Same three-mixin design as
[TaCZ-TravelersBackpack-Compat](https://github.com/Poseidon9991/taczxtravelerbackpack)
— only `collectStorage`/`CuriosCompat` differ (backpack discovery is
mod-specific; the TaCZ side is identical).
