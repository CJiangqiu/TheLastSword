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

**Nested folder support:** You can organize recipe files into subfolders for better management, e.g. `dragon_crystal_smithing_recipes/sword/`, `dragon_crystal_smithing_recipes/armor/`. All `.json` files in nested directories will be loaded automatically.

**Disabling recipes:** To disable a recipe without deleting it, simply rename the file extension to `.disabled.json` (e.g. `recipe.json` → `recipe.disabled.json`). Files ending with `.disabled.json` will be skipped during loading. This is especially useful for modpack makers who want to disable default recipes via scripts.

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

## Stronger Ender Dragon

When you participate in slaying the Ender Dragon in the End, you will receive additional Dragon Eggs as a reward—even if you've already defeated it before. However, each time the Ender Dragon is reborn, it returns with greater strength. Its health, armor, and attack power will increase with each challenge, and its level will be displayed above its name. How far can you go?

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
- [Jade](https://www.curseforge.com/minecraft/mc-mods/jade)
- [L_Ender's Cataclysm](https://www.curseforge.com/minecraft/mc-mods/lendercataclysm)
- [Lucky Block](https://www.curseforge.com/minecraft/mc-mods/lucky-block)

---

<p style="text-align:center">你是否曾在 Minecraft 的新版本中，被那些从未见过的生物一次又一次地击倒？</p><p style="text-align:center">也许，是时候夺回曾经失去的一切了……</p><p style="text-align:center">如今，当你再次击败末影龙，将龙蛋化为龙晶之时，你发现了那把很久以前曾与你并肩作战的武器。</p><p style="text-align:center">举起它吧，冒险者——那把你从未忘记的最终之剑</p>

***

***注意：本模组在 1.1.0 版本进行了重构。以下所有内容仅适用于 1.1.0 及之后的版本。不建议下载过时版本！***

这是经原作者 [queenofsquiggles](https://www.curseforge.com/members/queenofsquiggles/projects) 授权制作的 [The Last Sword](https://www.curseforge.com/minecraft/mc-mods/last-sword-you-will-ever-need-mo) 重制版。本版本着重于在现代 Minecraft 版本中对最终之剑进行内容扩展和玩法增强。因此，游戏体验可能不会那么"怀旧"。如果你更喜欢经典的最终之剑体验，请访问[另一位作者的 Fabric 分支](https://www.curseforge.com/minecraft/mc-mods/the-last-sword-you-will-ever-need-remastered)。

***

# 与旧版 1.7.10 有什么不同？

## <img src="https://i.postimg.cc/XvPvJq3p/dragon_crystal_sword_4.png" alt="龙晶剑" height="32"> 旧貌换新颜

全面翻新的材质！如果你喜欢怀旧风格，可以启用内置的"最终之剑经典材质包"来使用 1.7.10 版本的原版材质。

## <img src="https://i.postimg.cc/GhZ9Pz5q/the_last_sword_dragon_crystal_smithing_table.png" alt="整合包友好" height="32"> 整合包友好！

本模组引入了 JSON 驱动的合成系统，允许你完全自由地自定义每个等级的剑和盔甲的配方。该系统完全兼容 JEI。此外，还提供了大量可配置选项，让你可以自定义最终之剑的各种数值和功能。

**关于自定义：** 你可以在 `config/the_last_sword/dragon_crystal_smithing_recipes/` 下找到 JSON 配方文件。这些文件允许你自由修改 `template` 和 `addition` 字段，分别对应龙晶锻造台的第 1 和第 3 个输入槽。

**嵌套文件夹支持：** 你可以将配方文件组织到子文件夹中以便管理，例如 `dragon_crystal_smithing_recipes/sword/`、`dragon_crystal_smithing_recipes/armor/`。所有嵌套目录中的 `.json` 文件都会被自动加载。

**禁用配方：** 要禁用某个配方而不删除文件，只需将文件扩展名改为 `.disabled.json`（例如 `recipe.json` → `recipe.disabled.json`）。以 `.disabled.json` 结尾的文件在加载时会被跳过。这对于整合包作者通过脚本禁用默认配方非常方便。

**配方格式示例：**
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

注意：`inputLevel` 和 `outputLevel` 为可选字段（默认值：0）。如果不需要基于等级的合成，可以省略它们。

## 现代化的伤害数值

考虑到 1.20.1 中大多数模组的伤害数值相比 1.7.10 较小，本Mod也进行了合理调整。现在，基础伤害数值为：

- 龙晶剑：12
- 龙之剑：200
- 最终之剑：1024

当然，我们也提供了配置选项让玩家控制每次升级的伤害加成。你可以在 `config/TheLastSword-common.toml` 中调整 `Increase Value` 来控制 0-5 级的伤害加成，以及 `Increase Value High Level` 来控制 6-13 级的伤害加成。

***

# 怎么玩？

## <img src="https://i.postimg.cc/VNGn9XQf/the-last-sword-the-last-end-scroll.png" alt="战斗机制" height="32"> 战斗机制

### 绝毁伤害

这是一种极其强大的真实伤害，能够绕过大多数模组的自定义生命值防御机制。它还会施加一段时间的治疗无效减益。当伤害值超过实体的剩余生命值时，会直接处决该实体，并暂时阻止该实体类型的生成。

这种伤害不仅可以由玩家造成，某些敌对实体也能造成。因此，玩家需要尽可能避免被这种伤害命中——这就需要一种新的护盾属性。

### 肃正防御护盾

该护盾属性显示为白色盾牌图标（灵感来自《Fate/Grand Order》）。每次受到伤害时，消耗 1 点即可抵消一次任意数值的伤害（包括绝毁伤害）。另外，当玩家死亡时，消耗 2 点可以复活玩家。这使其成为面对本模组某些实体时不可或缺的防御机制。

### 虚化状态

处于虚化状态时，玩家会短暂地与世界隔离，能够像旁观者模式一样穿墙而过！此外，在此效果期间还会获得强大的防御加成。

## 物品

### <img src="https://i.postimg.cc/C1SVwn6V/the-last-sword-dragon-crystal.png" alt="龙晶" height="32"> 龙晶

一切始于你将龙蛋分解为龙晶。

![龙晶配方](https://i.postimg.cc/8k6SRwdB/chapter_recipe_1_1.png)

接下来，你需要制作龙晶升级模板和龙晶锻造台——这是你铸剑之路的基础。

![龙晶升级模板配方](https://i.postimg.cc/DfXTr5Q1/chapter_recipe_2_1.png)
![龙晶锻造台配方](https://i.postimg.cc/tRxj3Ddh/chapter_recipe_2_2.png)

### <img src="https://i.postimg.cc/6Tx2Q3r7/dragonsword.png" alt="剑" height="32"> 剑

#### 等级

本模组中所有的剑都拥有 0 到 13 的等级系统。剑的等级直接影响其额外伤害输出——等级越高，额外伤害越多。你可以在龙晶锻造台上升级你的剑。

#### 模式

龙之剑和最终之剑拥有模式能力。按下模式切换键（默认：V）可以循环切换不同模式。每种模式会改变剑的右键技能，而左键始终为近战攻击。

#### 升级路线

**下界合金剑 → 龙晶剑（0-5 级）**

首先，你需要在原版锻造台上使用龙晶升级模板、下界合金剑和龙晶将其转化为龙晶剑。后续升级在龙晶锻造台上完成。

该剑造成物理伤害加上基于等级的额外魔法伤害。右键发射弹射物。

![龙晶剑配方](https://i.postimg.cc/ryrk1gGx/chapter_recipe_3_1.png)

升级龙晶剑需要在龙晶锻造台上使用龙晶升级模板、龙晶剑和龙晶。

![龙晶剑升级配方](https://i.postimg.cc/ZYdS8jPc/chapter_recipe_3_2.png)

**龙晶剑 → 龙之剑（6-12 级）**

继续升级即可获得龙之剑。该剑造成物理伤害加上额外的龙息伤害。它有 2 种模式：
- 普通模式：右键发射强力弹射物
- 唤灵模式：右键召唤或召回你的剑灵

![龙之剑配方](https://i.postimg.cc/3rvTX9j1/chapter_recipe_3_3.png)

升级龙之剑需要在龙晶锻造台上使用龙晶升级模板、龙之剑和龙蛋。

![龙之剑升级配方](https://i.postimg.cc/BZK0TN5M/chapter_recipe_3_4.png)

**龙之剑 → 最终之剑（13 级）**

恭喜，你重铸了这把被世界遗忘已久的武器。该剑造成物理伤害加上额外的绝毁伤害以及敌人最大生命值 13% 的伤害。它有 3 种模式：
- 普通模式：左键进行范围攻击，右键发射弹射物
- 强力挖掘模式：右键进行强力范围挖掘
- 唤灵模式：右键召唤或召回你的剑灵

此外，背包中持有最终之剑可获得飞行能力、移除所有物品冷却时间，并提供防御保护。

### <img src="https://i.postimg.cc/Hx7k7ms9/dragon_crystal_helmet.png" alt="盔甲" height="32"> 盔甲

#### 龙水晶盔甲

每件装备穿戴后提供药水效果：头盔提供夜视和水下呼吸，胸甲提供伤害抗性和力量，护腿提供再生和跳跃提升，靴子提供速度和火焰抗性。

穿戴全套激活水晶护佑，获得等同于最大生命值的护盾，并定期刷新。

#### 龙之战甲

一套强大的活体盔甲，需要 FE 能量才能发挥全部功能。每件装备提供与龙水晶盔甲类似但更强的效果。通电时效果提升一个等级。

胸甲提供飞行能力（消耗能量）。穿戴全套并通电时，获得饱和 V、免疫火焰和冰冻伤害，以及来自非玩家攻击和爆炸的 90% 伤害减免。穿戴全套飞行时还会获得虚化状态，允许你穿越方块。

### <img src="https://i.postimg.cc/RVjq1GDg/the_last_sword_dragon_crystal_enchanting_table.png" alt="方块" height="32"> 方块

#### 龙晶锻造台

用于升级剑和制作盔甲的工作站。它有 3 个输入槽（模板、基础物品和附加材料），类似原版锻造台。所有配方都可以通过配置文件夹中的 JSON 文件自定义。

#### 龙晶附魔台

科技与魔法的完美融合。它可以消耗 FE 能量来施加附魔，也可以使用龙晶作为燃料发电。

### <img src="https://i.postimg.cc/Y0Gy1pvy/dragon-crystal-ring.png" alt="饰品" height="32"> 饰品

你可以在末地新增的 3 个结构的箱子中找到一些稀有宝物：龙水晶项链、龙水晶指环、龙水晶王冠和远古能量核心。每一件都有独特的效果——有些乍看之下似乎有害，但与其他饰品搭配使用时，能够产生更强大的效果。

## <img src="https://i.postimg.cc/Jnm162NG/dragon-crystal-soul-stone-full-4.png" alt="召唤你的剑灵" height="32"> 召唤你的剑灵！

我们发现龙晶对灵魂有着独特的吸引力。现在，你可以制作龙晶魂石和龙魂灯，通过击杀实体将其灵魂绑定到魂石中，使其成为你的剑灵。使用龙魂灯或其他拥有召唤模式的物品，你可以召唤对应的剑灵为你而战！这些实体会根据你武器的等级获得不同程度的强化。剑灵之间通常不会互相伤害，但某些模组的攻击实现方式可能导致友军误伤同伴或主人。

如果你相信自己足够强大，可以前往末地的封印尖塔，解放一位被封印已久的骑士。在给予他们安息之后，你将能够召唤一位强大而忠诚的骑士——终焉剑灵——为你扫清重铸最终之剑路上的障碍（当然，它并非无敌）。

## 更强的末影龙

当你参与击杀末地的末影龙时，将会获得额外的龙蛋奖励——即使你已经战胜过它。然而，每次末影龙重生时都会带着更强的力量归来，它的生命值、护甲和攻击力都会随挑战次数提升，并在名称上显示等级。你能走多远？

***

# 依赖与兼容

## 依赖

本模组需要 3 个必要的前置模组：

- [GeckoLib](https://www.curseforge.com/minecraft/mc-mods/geckolib)
- [Curios API](https://www.curseforge.com/minecraft/mc-mods/curios)
- [Epic Core API](https://www.curseforge.com/minecraft/mc-mods/epic-core-api)

## 兼容

已添加兼容内容的模组：

- [JEI](https://www.curseforge.com/minecraft/mc-mods/jei)
- [Jade](https://www.curseforge.com/minecraft/mc-mods/jade)
- [L_Ender's Cataclysm](https://www.curseforge.com/minecraft/mc-mods/lendercataclysm)
- [Lucky Block](https://www.curseforge.com/minecraft/mc-mods/lucky-block)
