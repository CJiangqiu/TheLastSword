<p style="text-align:center"><em>In ages past, countless adventurers sought a legendary weapon.</em></p>
<p style="text-align:center"><em>Yet as the ages passed, the sword was shattered, and the Queen who forged it vanished without a trace.</em></p>
<p style="text-align:center"><em>Now, scattered clues converge once more, pointing toward the End, as this forgotten tale awaits the adventurer who will continue it.</em></p>
<p style="text-align:center"><em>Though the road ahead be arduous, though the sword be shattered, embark on the journey and reforge it—The Last Sword You Never Forgot.</em></p>

***

***Note: This mod has been refactored in version 1.1.0. All content below applies only to version 1.1.0 and later. Downloading outdated versions is not recommended!***

This is a remastered version of [The Last Sword](https://www.curseforge.com/minecraft/mc-mods/last-sword-you-will-ever-need-mo) created with authorization from the original author [queenofsquiggles](https://www.curseforge.com/members/queenofsquiggles/projects). This version focuses on content expansion and gameplay enhancement for The Last Sword in modern Minecraft versions. Therefore, the gameplay experience may not feel as "nostalgic." If you prefer a more classic The Last Sword experience, please visit [another author's Fabric branch](https://www.curseforge.com/minecraft/mc-mods/the-last-sword-you-will-ever-need-remastered).

***

# What's different compared to the old 1.7.10 version?

## <img src="https://i.postimg.cc/XvPvJq3p/dragon_crystal_sword_4.png" alt="Dragon Crystal Sword" height="32"> Old Look, New Style

Completely revamped textures! For players who prefer nostalgia, you can activate the built-in "The Last Sword Classical Texture Pack" to use the original textures from version 1.7.10.

## <img src="https://i.postimg.cc/GhZ9Pz5q/the_last_sword_dragon_crystal_smithing_table.png" alt="Modpack Friendly" height="32"> Modpack Friendly!

This mod introduces a JSON-driven crafting system, allowing you complete freedom to customize recipes for each level of swords and armor. This system is fully compatible with JEI. Additionally, numerous configurable options have been included, enabling you to customize values and functionalities of The Last Sword.

**Built-in and config recipes:** Official recipes are stored inside the mod JAR and are loaded first. The directory `config/the_last_sword/dragon_crystal_smithing_recipes/` is reserved for overrides and custom recipes, so official recipes are no longer copied into it on startup. This allows official recipes to receive updates without replacing the user's config files.

**Overriding official recipes:** Place a recipe with the same file name as an official recipe anywhere under the config recipe directory. The config version will replace the official version and retain the same recipe ID. A current-format override must define `template.inputLevel`; old official recipe copies without this field are recognized as outdated and ignored, so they do not need to be deleted manually.

**Custom recipes and nested folders:** Recipe files may use arbitrary names and be organized into any number of subfolders, e.g. `dragon_crystal_smithing_recipes/sword/` or `dragon_crystal_smithing_recipes/armor/high_level/`. All enabled `.json` files are loaded recursively. Custom recipes with the same file name can coexist in different subfolders unless that name belongs to an official recipe, in which case the file acts as an override. Non-conflicting recipes using the old format remain compatible.

**Disabling recipes:** Rename a recipe to `.disabled.json`, `.json.disabled`, or `.json.disable` to disable it without deleting it. A disabled config file whose name matches an official recipe also disables that official recipe.

**File name convention:** Official sword and armor upgrade recipes are named after their output level. For example, `dragon_crystal_smithing_sword_level_6.json` produces a Level 6 sword. Custom recipe file names do not have to follow this convention.

**Example recipe format:**
```json
{
  "type": "the_last_sword:dragon_crystal_smithing",
  "template": {
    "item": "the_last_sword:dragon_crystal_upgrade_template",
    "inputLevel": 0
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

The `inputLevel` inside `template` applies to the first slot, while the `inputLevel` inside `input` applies to the second slot. The output receives `outputLevel`. For standalone custom recipes, omitting `template.inputLevel` disables the template-level check for compatibility with the old format. The second-slot `inputLevel` and `outputLevel` default to 0 when omitted. Same-name overrides of official recipes must include `template.inputLevel`.

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

Press the defence config key (default: Left Alt) to open the Defence Configuration screen, where you can toggle the shield overlay and move it anywhere on your HUD, as well as adjust equipment settings.

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

Dragon Sword and The Last Sword have a mode system. Press the mode switch key (default: Left Ctrl) to cycle through different modes. Each mode changes the sword's right-click ability while keeping left-click as melee attack.

#### Upgrade Path

**Netherite Sword → Dragon Crystal Sword (Level 0-5)**

First, you need to use the vanilla Smithing Table with a Dragon Crystal Upgrade Template, a Netherite Sword, and a Dragon Crystal to transform it into a Dragon Crystal Sword. Subsequent upgrades are done at the Dragon Crystal Smithing Table.

This sword deals physical damage plus extra magic damage based on its level. Right-click to shoot a diamond projectile.

![Dragon Crystal Sword Recipe](https://i.postimg.cc/ryrk1gGx/chapter_recipe_3_1.png)

To upgrade the Dragon Crystal Sword, use a Dragon Crystal Upgrade Template, a Dragon Crystal Sword, and a Dragon Crystal at the Dragon Crystal Smithing Table.

![Dragon Crystal Sword Upgrade Recipe](https://i.postimg.cc/ZYdS8jPc/chapter_recipe_3_2.png)

**Dragon Crystal Sword → Dragon Sword (Level 6-12)**

Continue upgrading to obtain the Dragon Sword. This sword deals physical damage plus extra dragon breath damage. It has 2 modes:
- Normal Mode: Right-click to shoot a dragon crystal projectile
- Summon Mode: Right-click to summon or recall your Sword Wraith

![Dragon Sword Recipe](https://i.postimg.cc/3rvTX9j1/chapter_recipe_3_3.png)

To upgrade the Dragon Sword, use a Dragon Crystal Upgrade Template, a Dragon Sword, and a Dragon Egg at the Dragon Crystal Smithing Table.

![Dragon Sword Upgrade Recipe](https://i.postimg.cc/BZK0TN5M/chapter_recipe_3_4.png)

**Dragon Sword → The Last Sword (Level 13)**

Congratulations, you have finally reforged this legendary weapon! This sword deals physical damage plus extra Absolute Destruction damage. It has 3 modes:
- Normal Mode: Left-click mines most blocks and deals melee damage, right-click shoots an End Crystal projectile
- Powerful Mining Mode: Right-click to select blocks within range, right-click again to destroy them
- Summon Mode: Left-click performs an area attack up to 6 blocks ahead, right-click summons or recalls your Sword Wraith

Additionally, having The Last Sword in your inventory grants flight, removes all item cooldowns, and provides defense protection.

### <img src="https://i.postimg.cc/Hx7k7ms9/dragon_crystal_helmet.png" alt="Armor" height="32"> Armor

#### Dragon Crystal Armor

Each piece provides potion effects when worn: the helmet grants night vision and water breathing, the chestplate grants damage resistance and strength, the leggings grant regeneration and jump boost, and the boots grant speed and fire resistance.

Wearing the full set activates Crystal Guard, which grants a shield equal to your max health and refreshes periodically.

#### Dragon Armor

A powerful living armor that requires FE energy to function at full capacity. Each piece provides similar effects to Dragon Crystal Armor but stronger. When powered, the effects are enhanced by one level.

The chestplate grants flight ability (consumes energy). When wearing the full set with energy, you gain 90% damage reduction from non-player attacks and explosions. Its other energy-powered features—saturation, immunity to fire and freezing, and phasing that lets you pass through blocks—come as modules you can toggle individually in the Equipment Settings screen.

### <img src="https://i.postimg.cc/RVjq1GDg/the_last_sword_dragon_crystal_enchanting_table.png" alt="Blocks" height="32"> Blocks

#### Dragon Crystal Smithing Table

A crafting station used to upgrade your swords and craft armor. It has 3 input slots (template, base item, and addition) similar to the vanilla smithing table. All recipes can be customized via JSON files in the config folder.

#### Dragon Crystal Enchanting Table

A perfect fusion of technology and magic. It can consume FE energy to apply enchantments, or function as a generator by using Dragon Crystals as fuel to produce power.

### <img src="https://i.postimg.cc/Y0Gy1pvy/dragon-crystal-ring.png" alt="Curios" height="32"> Curios

You can find some rare treasures in the chests of the new structures added to the End, the Nether and the Overworld: Dragon Crystal Necklace, Dragon Crystal Ring, Wings That Cover The World, Extreme Life Support Device, Dimension Explorer, and Ancient Energy Core. Each one has its own unique and powerful effect.

## <img src="https://i.postimg.cc/Jnm162NG/dragon-crystal-soul-stone-full-4.png" alt="Summon Your Sword Wraith" height="32"> Summon Your Sword Wraith!

You can craft a Dragon Crystal Soul Stone and a Dragon Soul Lantern to bind an entity's soul into the stone by slaying it, turning it into your Sword Wraith. Using the Dragon Soul Lantern or other items with Summon Mode, you can summon the corresponding Sword Wraith to fight for you! These entities will be enhanced to varying degrees based on your weapon's level. Sword Wraiths generally won't harm each other, though some mods with aggressive implementations may cause friendly fire to companions or the owner.

Press the summon GUI key (default: Z) to open the Summon Wraith screen and place a Dragon Crystal Soul Stone into its dedicated slot. The stone kept there is bound to you as your active soul stone, so Summon Mode will call that Sword Wraith without you having to hold the stone.

If you believe yourself strong enough, you may journey to the Sealed Spire in the End and free a knight who has been sealed away. After granting them peace, you will be able to summon a powerful and loyal knight—the Last End Sword Wraith—who will clear the obstacles on your path to reforging The Last Sword (though it is not invincible, of course).

## Stronger Ender Dragon

When you participate in slaying the Ender Dragon in the End, you will receive additional Dragon Eggs as a reward—even if you've already defeated it before. However, each time the Ender Dragon is reborn, it returns with greater strength. Its health, armor, and attack power will increase with each challenge, and its level will be displayed above its name.

***

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

<p style="text-align:center"><em>在过去的纪元里，无数冒险者都曾追寻一把传说中的武器。</em></p>
<p style="text-align:center"><em>然而岁月流转，剑已破碎，铸造它的女皇销声匿迹。</em></p>
<p style="text-align:center"><em>如今，散落的线索再次汇聚，指向末地，而这段被遗忘的故事，即将迎来它的续写者。</em></p>
<p style="text-align:center"><em>纵使前路艰难，纵使剑已破碎，也请踏上旅程，重铸那把——你从未忘记的最终之剑。</em></p>

***

***注意：本模组在 1.1.0 版本进行了重构。以下所有内容仅适用于 1.1.0 及之后的版本。不建议下载过时版本！***

这是经原作者 [queenofsquiggles](https://www.curseforge.com/members/queenofsquiggles/projects) 授权制作的 [The Last Sword](https://www.curseforge.com/minecraft/mc-mods/last-sword-you-will-ever-need-mo) 重制版。本Mod着重于在现代 Minecraft 版本中对最终之剑进行冒险向内容扩展和玩法增强。因此，游戏体验可能不会那么"怀旧"。如果你更喜欢经典的最终之剑体验，请访问[另一位作者的 Fabric 分支](https://www.curseforge.com/minecraft/mc-mods/the-last-sword-you-will-ever-need-remastered)。

***

# 与旧版 1.7.10 有什么不同？

## <img src="https://i.postimg.cc/XvPvJq3p/dragon_crystal_sword_4.png" alt="龙水晶剑" height="32"> 旧貌换新颜

全面翻新的材质！如果你喜欢怀旧风格，可以启用内置的"最终之剑经典材质包"（The Last Sword Classical Texture Pack）来使用 1.7.10 版本的原版材质。

## <img src="https://i.postimg.cc/GhZ9Pz5q/the_last_sword_dragon_crystal_smithing_table.png" alt="整合包友好" height="32"> 整合包友好！

本模组引入了 JSON 驱动的合成系统，允许你完全自由地自定义每个等级的剑和盔甲的配方。该系统完全兼容 JEI。此外，还提供了大量可配置选项，让你可以自定义最终之剑的各种数值和功能。

**内置配方与 config 配方：** 官方配方保存在模组 JAR 内并优先加载。`config/the_last_sword/dragon_crystal_smithing_recipes/` 目录只用于放置覆盖配方和自定义配方，启动时不再向其中复制官方配方。这样官方配方可以随模组更新，同时不会覆盖用户的 config 文件。

**覆盖官方配方：** 在 config 配方目录的任意位置放入与官方配方同名的文件，即可替换官方配方并沿用相同的配方 ID。当前格式的覆盖配方必须定义 `template.inputLevel`；缺少该字段的旧官方配方副本会被识别为过时配方并忽略，因此无需手动删除。

**自定义配方与嵌套文件夹：** 配方文件可以任意命名，并能放入任意层级的子文件夹中，例如 `dragon_crystal_smithing_recipes/sword/` 或 `dragon_crystal_smithing_recipes/armor/high_level/`。所有未禁用的 `.json` 文件都会被递归加载。不同子目录中的同名自定义配方可以共存；如果文件名与官方配方相同，则会作为官方配方的覆盖项。名称不冲突的旧格式自定义配方仍然兼容。

**禁用配方：** 将配方重命名为 `.disabled.json`、`.json.disabled` 或 `.json.disable` 即可禁用，而不必删除文件。与官方配方同名的禁用文件也会禁用对应的官方配方。

**文件命名规则：** 官方剑和盔甲升级配方以输出等级命名。例如，`dragon_crystal_smithing_sword_level_6.json` 的产物是 6 级剑。自定义配方文件名不强制遵循该规则。

**配方格式示例：**
```json
{
  "type": "the_last_sword:dragon_crystal_smithing",
  "template": {
    "item": "the_last_sword:dragon_crystal_upgrade_template",
    "inputLevel": 0
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

`template` 中的 `inputLevel` 对应第一个槽位，`input` 中的 `inputLevel` 对应第二个槽位，产物则会获得 `outputLevel`。对于独立的自定义配方，省略 `template.inputLevel` 会关闭模板等级检查，以兼容旧格式；第二槽位的 `inputLevel` 和 `outputLevel` 省略时默认为 0。覆盖官方配方的同名文件必须包含 `template.inputLevel`。

## 现代化的伤害数值

考虑到 1.20.1 中大多数模组的伤害数值相比 1.7.10 较小，本Mod也进行了合理调整。现在，基础伤害数值为：

- 龙水晶剑：12
- 龙之剑：200
- 最终之剑：1024

当然，我们也提供了配置选项让玩家控制每次升级的伤害加成。你可以在 `config/TheLastSword-common.toml` 中调整 `Increase Value` 来控制 0-5 级的伤害加成，以及 `Increase Value High Level` 来控制 6-13 级的伤害加成。

***

# 玩法介绍

## <img src="https://i.postimg.cc/VNGn9XQf/the-last-sword-the-last-end-scroll.png" alt="战斗机制" height="32"> 战斗机制

### 绝对毁灭伤害（Absolute Destruction Damage）

这是一种极其强大的真实伤害，能够绕过大多数模组的自定义生命值防御机制。它还会施加一段时间的禁疗效果。当伤害值超过实体的剩余生命值时，会直接斩杀该实体，并暂时阻止该实体类型的生成。

这种伤害不仅可以由玩家造成，某些敌对实体也能造成。因此，玩家需要尽可能避免被这种伤害命中——这就需要一种新的护盾属性。

### 肃正防御护盾（Justified Defence Shield）

该护盾属性显示为白色盾牌图标（灵感来自《Fate/Grand Order》）。每次受到伤害时，消耗 1 点即可抵消一次任意数值的伤害（包括绝毁伤害）。另外，当玩家死亡时，消耗 2 点可以复活玩家，这使其成为面对本模组某些实体时不可或缺的防御机制。

按下防御配置键（默认：左 Alt）可以打开防御配置界面，在这里可以开关护盾叠加层显示、将其调整到 HUD 上的任意位置，也可以调整装备设置。

### 虚化 Buff

处于虚化状态时，玩家会短暂地与世界隔离，能够像旁观者模式一样穿墙而过！此外，在此效果期间还会获得强大的防御。

## 物品

### <img src="https://i.postimg.cc/C1SVwn6V/the-last-sword-dragon-crystal.png" alt="龙水晶" height="32"> 龙水晶

一切开始于你将龙蛋分解为龙水晶。

![龙水晶配方](https://i.postimg.cc/8k6SRwdB/chapter_recipe_1_1.png)

接下来，你需要制作龙水晶升级模板和龙水晶锻造台——它们是你铸剑之路的基础。

![龙水晶升级模板配方](https://i.postimg.cc/DfXTr5Q1/chapter_recipe_2_1.png)
![龙水晶锻造台配方](https://i.postimg.cc/tRxj3Ddh/chapter_recipe_2_2.png)

### <img src="https://i.postimg.cc/6Tx2Q3r7/dragonsword.png" alt="剑" height="32"> 剑

#### 等级

本模组中所有的剑都拥有 0 到 13 的等级系统。剑的等级直接影响其额外伤害输出——等级越高，额外伤害越多。你可以在龙水晶锻造台上升级你的剑。

#### 模式

龙之剑和最终之剑拥有模式。按下模式切换键（默认：左 Ctrl）可以循环切换不同模式。每种模式会改变剑的右键技能，而左键始终为近战攻击。

#### 升级路线

**下界合金剑 → 龙水晶剑（0-5 级）**

首先，你需要在原版锻造台上使用龙水晶升级模板、下界合金剑和龙水晶将其转化为龙水晶剑。后续升级在龙水晶锻造台上完成。

该剑造成物理伤害的同时附加基于等级的额外魔法伤害，右键可发射钻石弹射物。

![龙水晶剑配方](https://i.postimg.cc/ryrk1gGx/chapter_recipe_3_1.png)

升级龙水晶剑需要在龙水晶锻造台上使用龙水晶升级模板、龙水晶剑和龙水晶。

![龙水晶剑升级配方](https://i.postimg.cc/ZYdS8jPc/chapter_recipe_3_2.png)

**龙水晶剑 → 龙之剑（6-12 级）**

继续升级即可获得龙之剑，该剑在造成物理伤害时额外附加龙息伤害。它有 2 种模式：
- 普通模式：右键发射龙水晶弹射物
- 唤灵模式：右键召唤或召回你的剑灵

![龙之剑配方](https://i.postimg.cc/3rvTX9j1/chapter_recipe_3_3.png)

升级龙之剑需要在龙水晶锻造台上使用龙水晶升级模板、龙之剑和龙蛋。

![龙之剑升级配方](https://i.postimg.cc/BZK0TN5M/chapter_recipe_3_4.png)

**龙之剑 → 最终之剑（13 级）**

恭喜，你终于重铸了这把传说中的武器！该剑在造成物理伤害时额外附加绝毁伤害。它有 3 种模式：
- 普通模式：左键可挖掘大部分方块并进行近战攻击，右键发射末影水晶弹射物
- 强力挖掘模式：右键选中范围内的方块，再次右键将其摧毁
- 唤灵模式：左键对前方 6 格范围内的目标造成范围攻击，右键召唤或召回你的剑灵

此外，背包中持有最终之剑可获得飞行能力、移除所有物品冷却时间，并提供防御保护。

### <img src="https://i.postimg.cc/Hx7k7ms9/dragon_crystal_helmet.png" alt="盔甲" height="32"> 盔甲

#### 龙水晶盔甲

每件装备穿戴后提供药水效果：头盔提供夜视和水下呼吸，胸甲提供伤害抗性和力量，护腿提供再生和跳跃提升，靴子提供速度和火焰抗性。

穿戴全套激活“水晶护佑”：获得等同于最大生命值的护盾，并定期刷新。

#### 龙之战甲

一套强大的活体盔甲，需要 FE 能量才能发挥全部功能。每件装备提供与龙水晶盔甲类似但更强的效果，通电时效果提升一个等级。

胸甲提供飞行能力（消耗能量）。穿戴全套并通电时，获得来自非玩家攻击和爆炸的 90% 伤害减免。其余耗能功能——饱和、免疫火焰与冰冻、以及允许你穿越方块的虚化状态——都以模块形式提供，可在装备设置界面中单独开关。

### <img src="https://i.postimg.cc/RVjq1GDg/the_last_sword_dragon_crystal_enchanting_table.png" alt="方块" height="32"> 方块

#### 龙水晶锻造台

用于升级剑和制作盔甲的工作站。它有 3 个输入槽（模板、基础物品和附加材料），类似原版锻造台。所有配方都可以通过配置文件夹中的 JSON 文件自定义。

#### 龙水晶附魔台

它是科技与魔法的完美融合，可以消耗 FE 能量来施加附魔，也可以使用龙水晶作为燃料发电。

### <img src="https://i.postimg.cc/Y0Gy1pvy/dragon-crystal-ring.png" alt="饰品" height="32"> 饰品

你可以在新增结构的箱子中找到一些稀有宝物，这些结构分布于末地、下界和主世界：龙水晶项链、龙水晶指环、覆世之翼、极限维生装置、维度探索者和远古能量核心，每一件都有独特且强大的效果。

## <img src="https://i.postimg.cc/Jnm162NG/dragon-crystal-soul-stone-full-4.png" alt="召唤你的剑灵" height="32"> 召唤你的剑灵！

你可以制作龙晶魂石和龙魂灯，通过击杀实体将其灵魂绑定到魂石中，使其成为你的剑灵。使用龙魂灯或其他拥有召唤模式的物品，你可以召唤对应的剑灵为你而战！这些实体会根据你武器的等级获得不同程度的强化。剑灵之间通常不会互相伤害，但某些模组的攻击实现方式可能导致友军误伤同伴或主人。

按下唤灵 GUI 键（默认：Z）可以打开唤灵界面，将龙晶魂石放入专属槽位。放在其中的魂石会绑定为你当前生效的魂石，因此使用唤灵模式时无需手持魂石也能召唤对应的剑灵。

如果你相信自己足够强大，可以前往末地的封印尖塔，解放一位被封印已久的骑士。在给予祂安息之后，你将能够召唤一位强大而忠诚的骑士——终焉剑灵，祂将为你扫清重铸最终之剑路上的障碍（当然，祂并非无敌）。

## 更强的末影龙

当你参与击杀末地的末影龙时，将会获得额外的龙蛋奖励，即使你已经战胜过它。然而，每次末影龙重生时都会带着更强的力量归来，它的生命值、护甲和攻击力都会随挑战次数提升，并在名称上显示等级。

***

## 依赖

本模组需要 3 个必要的前置模组：

- [GeckoLib](https://www.curseforge.com/minecraft/mc-mods/geckolib)
- [Curios API](https://www.curseforge.com/minecraft/mc-mods/curios)
- [Epic Core API](https://www.curseforge.com/minecraft/mc-mods/epic-core-api)

## 联动

已添加联动内容或进行了兼容的模组：

- [JEI](https://www.curseforge.com/minecraft/mc-mods/jei)
- [Jade](https://www.curseforge.com/minecraft/mc-mods/jade)
- [L_Ender's Cataclysm](https://www.curseforge.com/minecraft/mc-mods/lendercataclysm)
- [Lucky Block](https://www.curseforge.com/minecraft/mc-mods/lucky-block)
