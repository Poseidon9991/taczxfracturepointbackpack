# [TaCZ] WARBORN Compatibility Addon

Use the ammo stored inside your **WARBORN backpacks** with **TaCZ** guns —
no more "empty magazine" when your backpack is full of bullets.

This is the WARBORN twin of the
[TaCZ × Traveler's Backpack compat](https://github.com/Poseidon9991/taczxtravelerbackpack).

## What it does

Vanilla TaCZ only searches your hotbar, armor, and offhand when checking for
ammo. With this mod installed:

- Reloading and firing checks can **see** ammo inside your WARBORN backpacks
- When a reload needs more ammo than your inventory has, the missing rounds
  are **pulled directly out of your backpacks**
- Works for regular ammo items **and** ammo boxes (their stored counts are
  drained correctly)

## Ammo search order

When a gun consumes ammo, it takes from your sources in this order:

1. Your normal player inventory (hotbar, armor, offhand) — TaCZ's native behavior
2. A WARBORN backpack worn in the **chest armor slot**
3. A WARBORN backpack equipped in a **Curios slot** (when Curios is installed)
4. WARBORN backpacks carried inside your inventory, in slot order

Your own inventory is always used first; backpacks only cover the rest.

## Requirements

| Dependency              | Version        |
|-------------------------|----------------|
| Minecraft               | 1.20.1         |
| Forge                   | 47+            |
| TaCZ                    | 1.1.5 – 1.1.8+ |
| WARBORN (Fracture Point)| 3.0.0+         |
| Curios                  | optional       |

Install: drop the jar into your `mods` folder together with TaCZ and
WARBORN. Required on the **server**; clients should also have it for
consistent behavior.

## Configuration

`config/tacz_warborn_compat-server.toml`:

- `enabled` — master switch for the ammo bridge
- `wornBackpack` — include backpacks worn in the chest slot or Curios
- `inventoryBackpacks` — include backpacks carried inside the inventory

## License note

WARBORN's code is *view/reference only* (Zarra Studios). This addon contains
no WARBORN code — it only calls its public API surface at runtime, the same
way any cross-mod integration does.
