# Config & Debugging

## Config — `WarbornCompatConfig` (SERVER)

`tacz_warborn_compat-server.toml`:

| Key | Default | Effect |
|---|---|---|
| `enabled` | true | Master switch for the whole bridge |
| `wornBackpack` | true | Include chest-slot + Curios backpacks |
| `inventoryBackpacks` | true | Include inventory-carried backpacks |

All getters return `true` while the spec isn't loaded yet (early-calls safe).

## Diagnostics — `[TaCZ-WB]`

`collectStorage` logs a rate-limited (3s) summary during ammo checks:

```
[TaCZ-WB] server -> 1 storages | chest=air curios=5stacks/1bp/0nocap invBp=0
```

Fields: side · `storages` = handlers collected · `chest=` chest-slot item ·
`curios=` stacks scanned / backpacks found / backpacks missing the
capability (`nocap`) · `invBp=` inventory backpacks.

A `nocap > 0` means the curio slot's stack lost its `ITEM_HANDLER`
capability — the strongest lead for the "Curios-worn ammo doesn't work"
report.

## Known issues / investigation notes

- **Curios-worn backpack ammo not detected** — under investigation.
  `collectStacks` covers all curio slot types incl. `backpack`; suspects:
  (a) the worn stack's capability resolves empty after `ItemStack.copy()`
  on equip — `BackpackCapabilityProvider` snapshots `BackpackCap` NBT at
  construction, so caps must come through `deserializeNBT` (ForgeCaps),
  (b) curio contents not synced on the client `canReload` path,
  (c) extraction writes the handler but the curio slot never dirties →
  stale ForgeCaps. Read `[TaCZ-WB]` lines to narrow it down.
- **Version-range gotcha (fixed):** WARBORN's jar reports version
  `3.0.0-PT1-fix` — a Maven qualifier below `3.0.0`. mods.toml requires
  `fracturepoint` but Forge accepted the qualifier anyway; if deps ever
  harden, use `[3.0.0-PT1,)`.

## Build

- `cd 1.20.1-forge && gradlew build` (Java 17, ForgeGradle 6)
- libs (not committed): `tacz-1.20.1-1.1.8-hotfix2.jar`,
  `fracturepoint-1.20.1-3.0.0-PT1-fix-all.jar`,
  `geckolib-forge-1.20.1-4.8.4.jar`,
  `curios-forge-5.14.1+1.20.1.jar` (compileOnly)
