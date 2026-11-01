# Development

## Layout

- `1.20.1-forge/` — the ForgeGradle project (build from inside this folder)
- `src/main/java/com/poseidon/tacz_warborn_compat/`
  - `TaczWarbornCompat` — mod entry, registers the server config
  - `config/WarbornCompatConfig` — `enabled`, `wornBackpack`, `inventoryBackpacks`
  - `service/BackpackAmmoService` — scans WARBORN backpack stacks, exposes
    their `ITEM_HANDLER` storage to the TaCZ ammo flow
  - `service/CuriosCompat` — every `CuriosApi` reference, only loaded behind
    `ModList.isLoaded("curios")`
  - `mixin/` — three RETURN-injects into TaCZ:
    - `AbstractGunItemMixin` — `canReload`, `hasInventoryAmmo`
    - `ModernKineticGunScriptAPIMixin` — `hasAmmoToConsume`, `consumeAmmoFromPlayer`
    - `GunHudOverlayMixin` — `handleInventoryAmmo` (HUD reserve count)

## How it works

TaCZ checks ammo only in the player inventory. Three mixin hooks OR-in the
backpack scan:

- `canReload` / `hasInventoryAmmo` / `hasAmmoToConsume` return `true` when a
  backpack holds matching `IAmmo`/`IAmmoBox`
- `consumeAmmoFromPlayer`: after TaCZ drains the inventory, the remainder is
  extracted from backpack handlers via `findAndExtractInventoryAmmo`
- `handleInventoryAmmo`: backpack ammo is added to the HUD reserve count

WARBORN backpack detection: `BackpackItem.isBackpackItem(stack)` (the item is
an `ArmorItem`/`ICurioItem`); storage is `stack.getCapability(ITEM_HANDLER)`.
Worn sources: `EquipmentSlot.CHEST` + all Curios slots (soft-dep guarded).

Handler edits are live state and persist through Forge's normal item-capability
save cycle — no manual write-back needed after extraction.

## Build

```
cd 1.20.1-forge
gradlew build
```

Output: `build/libs/tacz_warborn_compat-<version>.jar`

`libs/` jars (not committed): `tacz-1.20.1-1.1.8-hotfix2.jar`,
`fracturepoint-1.20.1-3.0.0-PT1-fix-all.jar`,
`geckolib-forge-1.20.1-4.8.4.jar`, `curios-forge-5.14.1+1.20.1.jar`.
