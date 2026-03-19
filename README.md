<p style="text-align:center">Have you ever been repeatedly defeated by creatures you've never encountered before in the newer versions of Minecraft?</p><p style="text-align:center">Perhaps it's time to reclaim what was lost...  </p><p style="text-align:center">Now, as you defeat the Ender Dragon once more and transform the Dragon Egg into Dragon Crystals, you discover the weapon that once accompanied you long ago. </p><p style="text-align:center">Raise it, adventurer—The Last Sword You Never Forgot</p>

***

***Note: This mod has been refactored in version 1.1.0. All content below applies only to version 1.1.0 and later. Downloading outdated versions is not recommended!***

This is a remastered version of [The Last Sword](https://www.curseforge.com/minecraft/mc-mods/last-sword-you-will-ever-need-mo) created with authorization from the original author [queenofsquiggles](https://www.curseforge.com/members/queenofsquiggles/projects). This version focuses on content expansion and gameplay enhancement for The Last Sword in modern Minecraft versions. Therefore, the gameplay experience may not feel as "nostalgic." If you prefer a more classic The Last Sword experience, please visit [another author's Fabric branch](https://www.curseforge.com/minecraft/mc-mods/the-last-sword-you-will-ever-need-remastered).

***

# What's different compared to the old 1.7.10 version?

## <img src="https://i.postimg.cc/XvPvJq3p/dragon_crystal_sword_4.png" alt="Dragon Crystal Sword" height="32"> Old Look, New Style

Completely revamped textures! For players who prefer nostalgia, you can activate the built-in "The Last Sword Classic Texture Pack" to use the original textures from version 1.7.10.

## <img src="https://i.postimg.cc/GhZ9Pz5q/the_last_sword_dragon_crystal_smithing_table.png" alt="Modpack Friendly" height="32"> Modpack Friendly!

This mod introduces a JSON-driven crafting system, allowing you complete freedom to customize recipes for each level of swords and armor. This system is fully compatible with JEI. Additionally, numerous configurable options have been included, enabling you to customize values and functionalities of The Last Sword.

**About customization:** You can find JSON recipe files under `config/the_last_sword/dragon_crystal_smithing_recipes/`. These files allow you to modify the `template` and `addition` fields freely, corresponding to the 1st and 3rd input slots of the Dragon Crystal Smithing Table.

**Example recipe format:**
```json
{
  "type": "the_last_sword:dragon_crystal_smithing",
  "template": {
    "item": "the_last_sword:dragon_crystal_upgrade_template"
  },
  "input": {
    "item": "the_last_sword:dragon_crystal_sword",
    "inputLevel": 0
  },
  "addition": {
    "item": "the_last_sword:dragon_crystal"
  },
  "output": {
    "item": "the_last_sword:dragon_crystal_sword",
    "outputLevel": 1
  }
}
```

Note: `inputLevel` and `outputLevel` are optional fields (default: 0). If you don't need level-based crafting, you can omit them.

## Modernized Damage Values

Considering that most mods in 1.20.1 have smaller damage values compared to 1.7.10 mods, this version has also been reasonably adjusted. Now, the base damage values are:

- Dragon Crystal Sword: 12
- Dragon Sword: 200
- The Last Sword: 1024

Of course, we also provide configuration options to let players control the damage bonus per upgrade. You can adjust `Increase Value` in `config/TheLastSword-common.toml` to control the damage bonus for levels 0-5, and `Increase Value High Level` to control the damage bonus for levels 6-13.

***

# How to play?

## <img src="https://i.postimg.cc/VNGn9XQf/the-last-sword-the-last-end-scroll.png" alt="Combat Lore" height="32"> Combat Lore

### Absolute Destruction Damage

This is an extremely powerful true damage that bypasses most mods' custom health defenses. It also applies a healing negation debuff for a period of time. When the damage value exceeds the entity's remaining health, it will instantly execute that entity and temporarily prevent that entity type from spawning.

This damage can be dealt not only by players, but also by certain hostile entities. Therefore, players need to avoid being hit by this damage as much as possible—and that requires a new shield attribute.

### Justified Defence Shield

This shield attribute appears as a white shield icon (inspired by *Fate/Grand Order*). Each time you take damage, it consumes 1 point and negates one instance of damage of any amount (including Absolute Destruction damage). Alternatively, when the player dies, it consumes 2 points to revive the player. This makes it an essential defense mechanism when facing certain entities from this mod.

### Phasing Buff

While in the Phasing state, the player is briefly isolated from the world, allowing them to pass through walls like in Spectator Mode! Additionally, they gain powerful defense during this effect.

## Items

### <img src="https://i.postimg.cc/C1SVwn6V/the-last-sword-dragon-crystal.png" alt="Dragon Crystal" height="32"> Dragon Crystal

It all begins when you break down the Dragon Egg into Dragon Crystals.

![Dragon Crystal Recipe](https://i.postimg.cc/8k6SRwdB/chapter_recipe_1_1.png)

Next, you need to craft the Dragon Crystal Upgrade Template and the Dragon Crystal Smithing Table—these are the foundation of your path to forging the sword.

![Dragon Crystal Upgrade Template Recipe](https://i.postimg.cc/DfXTr5Q1/chapter_recipe_2_1.png)
![Dragon Crystal Smithing Table Recipe](https://i.postimg.cc/tRxj3Ddh/chapter_recipe_2_2.png)

### <img src="https://i.postimg.cc/6Tx2Q3r7/dragonsword.png" alt="Swords" height="32"> Swords

#### Level

All swords in this mod have a level system ranging from 0 to 13. A sword's level directly affects its extra damage output—the higher the level, the more extra damage it deals. You can upgrade your sword at the Dragon Crystal Smithing Table.

#### Mode

Dragon Sword and The Last Sword have a mode system. Press the mode switch key (default: V) to cycle through different modes. Each mode changes the sword's right-click ability while keeping left-click as melee attack.

#### Upgrade Path

**Netherite Sword → Dragon Crystal Sword (Level 0-5)**

First, you need to use the vanilla Smithing Table with a Dragon Crystal Upgrade Template, a Netherite Sword, and a Dragon Crystal to transform it into a Dragon Crystal Sword. Subsequent upgrades are done at the Dragon Crystal Smithing Table.

This sword deals physical damage plus extra magic damage based on its level. Right-click to shoot a projectile.

![Dragon Crystal Sword Recipe](https://i.postimg.cc/ryrk1gGx/chapter_recipe_3_1.png)

To upgrade the Dragon Crystal Sword, use a Dragon Crystal Upgrade Template, a Dragon Crystal Sword, and a Dragon Crystal at the Dragon Crystal Smithing Table.

![Dragon Crystal Sword Upgrade Recipe](https://i.postimg.cc/ZYdS8jPc/chapter_recipe_3_2.png)

**Dragon Crystal Sword → Dragon Sword (Level 6-12)**

Continue upgrading to obtain the Dragon Sword. This sword deals physical damage plus extra dragon breath damage. It has 2 modes:
- Normal Mode: Right-click to shoot a powerful projectile
- Summon Mode: Right-click to summon or recall your Sword Wraith

![Dragon Sword Recipe](https://i.postimg.cc/3rvTX9j1/chapter_recipe_3_3.png)

To upgrade the Dragon Sword, use a Dragon Crystal Upgrade Template, a Dragon Sword, and a Dragon Egg at the Dragon Crystal Smithing Table.

![Dragon Sword Upgrade Recipe](https://i.postimg.cc/BZK0TN5M/chapter_recipe_3_4.png)

**Dragon Sword → The Last Sword (Level 13)**

Congratulations, you have reforged this weapon that the world had long forgotten. This sword deals physical damage plus extra Absolute Destruction damage and 13% of the enemy's max health. It has 3 modes:
- Normal Mode: Left-click performs area attack, right-click shoots projectile
- Mining Mode: Right-click to perform powerful area mining
- Summon Mode: Right-click to summon or recall your Sword Wraith

Additionally, having The Last Sword in your inventory grants flight, removes all item cooldowns, and provides defense protection.

### <img src="https://i.postimg.cc/Hx7k7ms9/dragon_crystal_helmet.png" alt="Armor" height="32"> Armor

#### Dragon Crystal Armor

Each piece provides potion effects when worn: the helmet grants night vision and water breathing, the chestplate grants damage resistance and strength, the leggings grant regeneration and jump boost, and the boots grant speed and fire resistance.

Wearing the full set activates Crystal Guard, which grants absorption hearts equal to your max health and refreshes periodically.

#### Dragon Armor

A powerful living armor that requires FE energy to function at full capacity. Each piece provides similar effects to Dragon Crystal Armor but stronger. When powered, the effects are enhanced by one level.

The chestplate grants flight ability (consumes energy). When wearing the full set with energy, you gain Saturation V, immunity to fire and freezing, and 90% damage reduction from non-player attacks and explosions. Flying with the full set also grants phasing, allowing you to pass through blocks.

### <img src="https://i.postimg.cc/RVjq1GDg/the_last_sword_dragon_crystal_enchanting_table.png" alt="Blocks" height="32"> Blocks

#### Dragon Crystal Smithing Table

A crafting station used to upgrade your swords and craft armor. It has 3 input slots (template, base item, and addition) similar to the vanilla smithing table. All recipes can be customized via JSON files in the config folder.

#### Dragon Crystal Enchanting Table

A perfect fusion of technology and magic. It can consume FE energy to apply enchantments, or function as a generator by using Dragon Crystals as fuel to produce power.

### <img src="https://i.postimg.cc/Y0Gy1pvy/dragon-crystal-ring.png" alt="Curios" height="32"> Curios

You can find some rare treasures in the chests of the 3 new structures added to the End: Dragon Crystal Necklace, Dragon Crystal Ring, Dragon Crystal Crown, and Ancient Energy Core. Each one has its own unique effect—some may seem harmful at first glance, but when combined with other accessories, they can produce even more powerful effects.

## <img src="https://i.postimg.cc/Jnm162NG/dragon-crystal-soul-stone-full-4.png" alt="Summon Your Sword Wraith" height="32"> Summon Your Sword Wraith!

We have discovered that Dragon Crystals possess a unique attraction to souls. Now, you can craft a Dragon Crystal Soul Stone and a Dragon Soul Lantern to bind an entity's soul into the stone by slaying it, turning it into your Sword Wraith. Using the Dragon Soul Lantern or other items with Summon Mode, you can summon the corresponding Sword Wraith to fight for you! These entities will be enhanced to varying degrees based on your weapon's level. Sword Wraiths generally won't harm each other, though some mods with aggressive implementations may cause friendly fire to companions or the owner.

If you believe yourself strong enough, you may journey to the Sealed Spire in the End and free a knight who has been sealed away. After granting them peace, you will be able to summon a powerful and loyal knight—the Last End Sword Wraith—who will clear the obstacles on your path to reforging The Last Sword (though it is not invincible, of course).

***

# Dependencies and Compatibility

## Dependencies

This mod requires 3 essential dependency mods:

- [GeckoLib](https://www.curseforge.com/minecraft/mc-mods/geckolib)
- [Curios API](https://www.curseforge.com/minecraft/mc-mods/curios)
- [Epic Core API](https://www.curseforge.com/minecraft/mc-mods/epic-core-api)

## Compatibility

Mods with added compatibility content:

- [JEI](https://www.curseforge.com/minecraft/mc-mods/jei)
- [L_Ender's Cataclysm](https://www.curseforge.com/minecraft/mc-mods/lendercataclysm)
