# Config & Debugging

## Config — `WarbornCompatConfig` (SERVER)

`tacz_warborn_compat-server.toml`:

| Key | Default | Effect |
|---|---|---|
| `enabled` | true | Master switch for the whole bridge |
| `wornBackpack` | true | Include chest-slot + Curios backpacks |
| `inventoryBackpacks` | true | Include inventory-carried backpacks |
| `debugLogging` | false | Enables the `[TaCZ-WB]` diagnostics |

Getters return `true` while the spec isn't loaded yet (early-calls safe);
`debugEnabled` returns `false` until the spec loads.

## Diagnostics — `[TaCZ-WB]`

Opt-in: set `debugLogging = true`. During ammo checks a rate-limited (3s)
summary is logged:

```
[TaCZ-WB] server -> 1 storages [12/54 filled/slots]
```

Fields: side (`client` or `server`) · `storages` = handlers collected · the
bracketed list is `filled/total` slots per storage, comma-separated.

## Known issues / investigation notes

- **Curios-worn backpack ammo not detected — fixed after v1.0.1
  (unreleased, ships in the next version).** Symptom: backpack ammo was
  intermittently not seen for Curios-worn backpacks while
  inventory-carried ones worked. `BackpackItemMixin` overrides
  `getShareTag`/`readShareTag` to carry the serialized handler under
  `BackpackCap`, and `BackpackCapabilityProviderMixin` mirrors the
  serialized handler into the stack's `BackpackCap` tag on
  serialize/deserialize. User-verified in game; the exact root cause was
  not isolated.
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
