# Backpack Detection (WARBORN internals)

## What a WARBORN backpack is

`com.raiiiden.warborn.common.item.BackpackItem extends ArmorItem implements
GeoItem, ICurioItem` — a chestplate-type armor item that is *also* a valid
curio. `BackpackItem.isBackpackItem(stack)` = `instanceof` check (their own
API, stable name).

## Storage

`initCapabilities` → `BackpackCapabilityProvider` —
`stack.getCapability(ForgeCapabilities.ITEM_HANDLER)` →
`BackpackItemStackHandler` (plain `ItemStackHandler`, **54 slots** = 9×6
max; tier controls how many the GUI shows, `isItemValid` rejects nested
backpacks).

Persistence: Forge serializes `provider.serializeNBT()` →
`handler.serializeNBT()` under the stack's `ForgeCaps` on save/sync;
`deserializeNBT` restores on load. The constructor's `BackpackCap` tag read
is a legacy/migration path — live handler edits persist, so ammo
extraction needs no write-back.

## Wear positions & scan order (`collectStorage`)

1. `EquipmentSlot.CHEST` — it's an `ArmorItem`
2. All Curios slot **types** — WARBORN's `equipToCurios` (Shift+RClick)
   inserts into the slot whose identifier equals `"backpack"`
   (`data/fracturepoint/curios/slots/backpack.json` registers it); we scan
   every slot type via `CuriosCompat.collectStacks` so mask/shoulderpads/
   uniform slots are covered for free
3. Player inventory — carried backpacks

Each stack → `getCapability(ITEM_HANDLER)` → storage `IItemHandler`.

## Why CuriosCompat is separate

WARBORN declares Curios as optional. `CuriosCompat` is the only class
importing `top.theillusivec4.curios` — it only gets class-loaded inside
the `ModList.isLoaded("curios")` branch, so the mod runs fine without
Curios (chest + inventory paths still work).
