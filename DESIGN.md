# TACZ RPG — 全境封锁式枪械装备系统 设计文档

> 依赖：TACZ 1.1.8-hotfix（`yOVIzIJR`）、TAA 1.3.7、Cloth Config 11.1.106、Forge 47.4.20
>
> **修订记录（2026-08-19）**：校准允许跨分类替换；优化上限 = 词条库记录值（非品质档上限）；已校准词条禁优化、改为重新校准提升；词条库为平面库、按枪械类型分区为后续规划；词条池 39 个（TAA 全属性 + 自定义暴击属性，红/黄/蓝三色分类，移除「点燃目标」），新增真实暴击判定（§3.4）；类型专属伤害按枪型匹配、30% 概率；新增奇特武器识别接口（§6）；等级上限 60、4 档（基础/稀有/精英/传说）品质与档位材料体系、分解/校准/优化消耗多样化。
>
> **调试命令**：`/taczrpg setlevel <0-60>` / `getlevel`（特工等级）；`/taczrpg setexpertise <0-30>` / `getexpertise`（当前手持枪械的专精）；`/taczrpg getattr`（打印当前枪词条对应的 TAA 属性实际值，验证词条挂载，带词条时应为 1.0+词条值）。

## 0.目标阐述
【任务指令】
基于《我的世界》TaCZ（Timeless and Classics Zero）枪械模组，1:1还原《全境封锁2》的「枪械校准站」系统，严格复刻全部机制逻辑。必须先准确理解下文定义的「词条库（校准数值库）」核心概念，再进行设计，不得偏离原作规则。

【核心概念强制定义：词条库（校准数值库）】
词条库不是预设词条列表，也不是随机属性池，它是**玩家主动收集、永久存档、可跨枪械复用的词条存档数据库**，核心逻辑为「提取→存储→复用」：
1. 提取：玩家可拆解多余枪械，从中选择1条属性词条/天赋抽离出来存入词条库；提取后原枪械永久销毁。
2. 存储：词条库按「枪械类型+词条分类」分层归档；同一条词条自动保留历史最高数值，提取到更高数值的同词条时自动覆盖更新。
3. 复用：对任意一把同类型枪械校准时，可从词条库中选择已收录的同分类词条，替换枪械上的原有词条，该操作可无限次重复。
4. 限制：传奇/奇特枪械的专属词条、具名枪械的固定特殊效果，无法提取入库，也无法被校准替换。

【校准站三大完整功能明细】
#### 功能1：校准数值库（词条库管理）
校准系统的基础模块，用于管理玩家收集的所有词条：
- 分类规则：按枪械类型（手枪/步枪/冲锋枪/霰弹枪/狙击枪/轻机枪）分为独立库，不同类型枪械的词条不可互通；每类枪械下再分「核心属性」「普通属性」「特殊天赋」三个子分类。
- 提取操作：选择待拆解枪械 → 选择该枪上需提取的1条词条/天赋 → 确认提取，枪械销毁，对应词条永久入库。
- 数值更新：若提取的词条已在库中，仅当新数值高于库内现有数值时才会更新；低数值提取不覆盖原有高数值。
- 查看功能：可按分类查看所有已收录词条的名称、当前最高数值、品质等级。

#### 功能2：校准（词条替换）
消耗材料对枪械词条进行定向替换：
- 操作流程：选择目标枪械 → 选择该枪上待替换的1个词条位 → 从词条库中选择已收录词条 → 确认校准，消耗材料完成替换。
- 核心限制：
    1. 每把枪械**仅能锁定1个词条位**进行校准（选定后其他词条位不可再校准），但该锁定词条位可无限次更换不同词条。
    2. 只能替换为词条库中已收录的词条；已校准槽位**允许跨分类替换**（如从「伤害」类换成「后坐力」类）。词条库当前为平面库（未按枪械类型分区），按枪械类型分层为后续规划。
- 消耗规则：校准消耗「纳米核心」类材料；同一把枪校准次数越多，单次耗材量越高，次数不重置。

#### 功能3：优化（词条数值强化）
消耗材料直接提升枪械现有词条的数值，直至达到**词条库中该词条已记录的最高值**：
- 操作流程：选择目标枪械 → 选择该枪上待强化的词条 → 消耗材料优化，词条数值小幅提升（每次 +1.5%）。
- 强化规则：
    1. 每个非校准词条可多次优化，数值逐步上涨，**上限 = 词条库中该词条记录的最高值**；后续刷到更高记录可继续优化。
    2. 词条库中无该词条记录时，无法优化。
    3. **已校准的词条不可优化**（其数值通过重新校准提升，见功能2）——此点取代原「已校准同样可优化」的矛盾表述。
- 材料区分：低段优化消耗「实战侦察数据」类基础材料，高段优化消耗「国土校准数据」类高级材料（当前实现统一消耗「模组数据块」，按段分级为后续规划）。

【TaCZ模组适配规则】
将上述机制对应到TaCZ原生枪械体系，词条范围限定为：
- 核心属性：基础伤害、开火速率
- 普通属性：精准度、后坐力控制、水平/垂直后坐力、换弹速度、弹匣容量、爆头伤害倍率、护甲穿透、子弹初速、移动射击精度
- 特殊天赋：TaCZ可实现的枪械被动效果（如连续命中增伤、击杀换弹加速、霰弹枪弹丸数增加、狙击枪开镜提速等）
- 枪械类型划分严格遵循TaCZ原生分类，确保词条仅同类型互通。
【开发约束】
- 要结合开发实际需求来做，与用户协商，仅做目标参考
【输出要求】
输出完整的校准站系统设计，包含方块交互逻辑、UI界面结构、全机制规则明细、材料消耗设计、与TaCZ属性系统的对接方式，确保逻辑自洽、可落地实现。


## 1. 核心概念

| 概念 | 说明 |
|---|---|
| 特工等级（Agent Level） | 玩家级等级，击杀积累，决定词条注册的品质/词条数（**非 MC 经验等级**） |
| 词条（Affix） | 枪械/配件上的随机属性，映射到 TAA 属性 |
| 校准（Calibration） | 从词条库选词条替换目标枪**已选定槽位**的词条，替换后该槽锁定；每件枪仅可校准 1 个槽位 |
| 优化（Optimization） | 强化非校准词条数值到**词条库记录值**；校准过的词条禁优化 |
| 专精（Expertise） | 枪械级等级，击杀升级，每级 +枪械伤害 |

## 2. TACZ API（关键类，均已确认）

- `com.tacz.guns.api.item.IGun` — 枪械接口，`getGunId(ItemStack)` / `getAttachmentTag(gun, AttachmentType)` / `getAttachmentId(gun, type)` / `modifyProperty(...)`
- `com.tacz.guns.api.item.nbt.GunItemDataAccessor` — 枪 NBT 访问：`GunId`、`Attachment<Type>`（键 = `Attachment` + `type.name()`）、`GunLevelExp`（空实现）
- `com.tacz.guns.api.item.nbt.AttachmentItemDataAccessor` — 配件 NBT：`AttachmentId`
- `com.tacz.guns.api.item.attachment.AttachmentType` — `SCOPE/MUZZLE/STOCK/GRIP/LASER/EXTENDED_MAG/NONE`
- `com.tacz.guns.api.TimelessAPI` — `getCommonGunIndex(rl)` / `getCommonAttachmentIndex(rl)`（服务端可读）
- `com.tacz.guns.api.event.common.EntityKillByGunEvent` — 枪械击杀事件（`getAttacker()` / `getGunId()` / `isHeadShot()`）
- `com.tacz.guns.api.event.common.EntityHurtByGunEvent` — 枪械伤害事件
- `com.tacz.guns.item.ModernKineticGunItem` / `com.tacz.guns.item.AttachmentItem`

## 3. 词条系统

### 3.1 品质档（特工等级 → 词条数 / 档位材料）

等级上限 60。每档对应一种材料（基础/稀有/精英/传说），枪械名按档位着色（绿/蓝/紫/金）。

| 特工等级 | 档位 | 词条数 | 颜色 | 数值区间 | 材料 |
|---|---|---|---|---|---|
| 0~14 | 基础 | 1 | 绿 §a | +5%~10% | 基础材料 |
| 15~29 | 稀有 | 2 | 蓝 §9 | +10%~15% | 稀有材料 |
| 30~44 | 精英 | 3 | 紫 §5 | +15%~20% | 精英材料 |
| 45~60 | 传说 | 4 | 金 §6 | +20%~30% | 传说材料 |

> 终极材料：仅由奇特武器分解产生，用于奇特武器优化（见 §6）。

### 3.2 词条池（→ TAA 属性）

**前置：TAA（Tacz Attribute Add，模组 ID `taa`）**。词条全部映射到 TAA 属性。基础 6 词条见下表，完整 39 词条按红/黄/蓝分类见下文：

| 词条 key | 名称 | TAA 属性 | 方向 |
|---|---|---|---|
| bullet | 额外子弹伤害 | `taa:bullet_gundamage` | + |
| headshot | 爆头伤害加成 | `taa:headshot_multiplier` | + |
| rounds | 射击速度 | `taa:rounds_per_minute` | + |
| speed | 额外移速 | `taa:move_speed` | + |
| recoil | 后坐力 | `taa:recoil` | − |
| ignore | 护甲穿透 | `taa:armor_ignore` | + |

数值 = 区间内随机（如 +2%~4% 即 0.02~0.04），后坐力取负。TAA 属性基数为 1.0（倍率），通过 `ItemAttributeModifierEvent.addModifier(id, AttributeModifier)` 挂 `MULTIPLY_BASE`。

**完整词条库（39 个，颜色分类）**：

- **红 · 伤害（16）**：`bullet` `headshot` `ignore` `crit_chance` `crit_damage` `pierce` `bullet_count` `bullet_pistol` `bullet_rifle` `bullet_shotgun` `bullet_sniper` `bullet_smg` `bullet_lmg` `bullet_launcher` `melee_damage` `explode_damage`
- **黄 · 控制/精度/射速（16）**：`rounds` `recoil` `recoil_pitch` `recoil_yaw` `inaccuracy` `inaccuracy_stand` `inaccuracy_move` `inaccuracy_sneak` `inaccuracy_lie` `inaccuracy_aim` `ads_time` `effective_range` `weight` `knockback` `ammo_speed` `explode_knockback`
- **蓝 · 弹匣/换弹/移速等辅助（7）**：`speed` `magazine_capacity` `reload_time` `melee_distance` `explode_radius` `explode_delay` `explode_destroy`

**随机词条池**：爆炸/近战类 7 个（`melee_damage` `melee_distance` `explode_damage` `explode_knockback` `explode_radius` `explode_delay` `explode_destroy`）仅注册、不进随机池；通用池 26 个（不含类型专属伤害）。**类型专属伤害**（`bullet_pistol` 等）按枪械实际类型匹配，仅本枪对应类型可出，且以 30% 低概率加入（常量 `AffixSystem.TYPE_DAMAGE_CHANCE`）。

**数值**：统一为品质档区间（2%~12% 的 MULTIPLY_BASE 倍率）；`crit_chance`/`crit_damage` 为自定义属性（base=1.0），见 §3.4。逐词条数值调优为后续项。

> TAA 属性命名注意：爆炸击退 / 破坏方块为 `taa:explosion_knockbacknew` / `taa:explosion_destroy_blocknew`。

### 3.3 枪械 NBT 扩展（`tacz_rpg` 命名空间前缀）

- 词条本体：`bullet`/`headshot`/`rounds`/`speed`/`recoil`/`ignore`（double）+ `attrKeys`（List<String>，记录抽中的词条）
- 品质：`rankName` / `rankColor` / `level`（注册时的特工等级快照）
- 校准锁：`calibratedSlot`（int，-1 = 未校准，0~5 = 已校准词条位）
- 专精：`expertiseLevel`（int）/ `expertiseExp`（int）

### 3.4 暴击（自定义属性）
- 自定义属性：`tacz_rpg:crit_chance` / `tacz_rpg:crit_damage`（`RangedAttribute`，base = 1.0，通过 `EntityAttributeModificationEvent` 挂到 `EntityType.PLAYER`）。
- 词条：`crit_chance`（暴击几率）、`crit_damage`（暴击伤害），红（伤害）类，参与随机。
- 判定：`CritHandler` 监听 TACZ `EntityHurtByGunEvent.Pre`（仅服务端），按 `crit_chance − 1.0` 概率随机触发，命中后将 `baseAmount × crit_damage` 有效值。
- 数值语义：暴击率 = 词条加总（+5% 词条 → 5% 暴击率）；暴击伤害倍率 = 1.0 + 词条加总（+50% 词条 → 暴击 1.5× 伤害）。

## 4. 三大功能数据流

### 4.1 校准（词条库 → 槽位替换）
1. 校准站 GUI「提取」页：手持枪 A → 选中一个词条 → 该词条数值（绝对值）存入词条库（仅保留历史最高），枪 A 销毁，无消耗。
2. GUI「校准」页：手持枪 B → 点击选中 B 上一个词条位（已校准则自动选中锁定槽）→ 从词条库选一个词条点「替换」→ 服务端校验槽位 / 锁定 / 材料 → 替换该位词条并锁定（`calibratedSlot = slot`），消耗材料（费用随校准次数递增）。已锁定槽可反复替换为词条库中任意词条（允许跨分类）。
- 消耗：**枪械档位材料**（基础/稀有/精英/传说，按枪械档位），费用 = 基础价 + 校准次数 × 递增价（`getCalibrationTotalCost`）。

### 4.2 优化
- GUI「优化」页：手持枪 → 每个非校准词条显示「目标 X%」（X = 词条库记录值）→ 点「优化」→ 数值向词条库记录值递增一档（+1.5%）。
- 上限 = 词条库中该词条记录的最高值；库中无记录则不可优化。
- 约束：`calibratedSlot` 指向的已校准词条不可优化（通过重新校准提升，见 4.1）。
- 消耗：普通武器 = **档位材料 ×1 + 模组数据块 ×1**；奇特武器 = 终极材料 ×1 + 模组数据块 ×1（见 §6）。

### 4.3 专精
- `EntityKillByGunEvent`：attacker 持枪 → 该枪 `expertiseExp++` → 经验满则 `expertiseLevel++`，同时特工 `agentExp++`。**任意生物击杀都算（不限敌对）**，**任意枪械（含奇特）都有专精**。
- 每级 20 经验（20 杀/级）；击杀时 actionbar 显示「专精经验 +1（Lv X · Y/20）」，升级时显示「专精等级提升！」。
- 上限 30 级。**每级 +1 固定伤害（非百分比）**，最高 +30：`EntityHurtByGunEvent.Pre` 中 `baseAmount + expertiseLevel`。
- 校准站第五个标签页「专精」：显示当前手持枪的专精等级 / 经验条与说明。
- 调试命令：`/taczrpg setexpertise <0-30>`、`/taczrpg getexpertise`。
- 分解：**固定给 3 个档位材料**（`ResourceCostSystem.DISASSEMBLE_COUNT`），不再按专精等级浮动。

## 5. 校准站方块
- 方块「校准站」复用 `gun_smith_table` 材质（`assets/tacz/textures/block/gun_smith_table.png`，Bedrock 几何模型，`RenderShape.ENTITYBLOCK_ANIMATED`）。
- `BlockEntity` + `Menu`（`NetworkHooks.openScreen`）+ `Screen`（五标签页：提取/校准/优化/分解/专精，简约现代风格）。

## 6. 奇特武器（Exotic）

识别接口：数据包在 `data/tacz_rpg/exotic_weapons/<任意名>.json` 定义，`ExoticWeaponManager` 重载数据包时自动加载，按枪械 ID 识别。

```json
{
  "gun": "tacz:ak47",
  "display_name": "撕裂者",
  "exotic_affix": { "affix": "bullet_rifle", "value": 0.35 },
  "affixes": [
    { "affix": "crit_damage", "value": 0.10 },
    { "affix": "magazine_capacity", "value": 0.08 }
  ]
}
```

行为规则：
1. **分解**：给最高级材料（ULTIMATE）× 3 + 模组数据块 × 1（常量可调，`ResourceCostSystem.EXOTIC_DISASSEMBLE_*`）。
2. **校准**：禁止（返回错误码 8）。
3. **提取**：禁止（返回错误码 4）。
4. **优化**：允许，但消耗改为 终极材料 × 1 + 模组数据块 × 1（常量可调），上限仍为词条库记录值。
5. **显示**：名字红橙色「奇特 \<display_name\>」；奇特词条行红橙色「奇特词条：…」；普通词条正常颜色。

注册：被识别为奇特的枪在 `AutoRegisterHandler` 登记时按定义写入固定词条（奇特词条 + 普通词条），不走随机/品质档。

创造标签页：模组专属标签页「TACZ RPG」（`itemGroup.tacz_rpg`）包含全部模组物品（校准站 / 词条提取物 / 模组数据块 / 5 种品质材料）以及数据包定义的奇特武器（红橙名「奇特 \<display_name\>」）。

示例：正式版**默认不带**奇特武器定义（开发期示例 AK47「撕裂者」已随 0.2.0 移除）。制作方法见上方数据包格式。

## 7. 配件系统（全境封锁1式）

- **登记**：拾取 TACZ 配件自动登记词条（与枪共用 `AutoRegisterHandler`）。词条数**最多 3 条**（随特工等级档位升满：0-14 一条、15-29 两条、30+ 三条），从通用词条池随机（无枪械类型专属伤害）。
- **加载到枪**：配件装上枪后，其词条被读取并作为枪的加成生效（`AffixAttributeHandler` 读枪配件槽的嵌套 `tag`）；卸下即失效。
- **限制**：配件无专精 / 校准 / 提取（这些仅枪享有）。
- **分解**：校准站「分解」页可分解配件，给 **1 个档位材料**（按配件登记档位）。
- **泛枪包支持**：模组经 `TimelessAPI` 读取枪械类型与配件，不依赖具体枪包命名空间；任意 TACZ 枪包（含第三方包如 Create/Immersive Armorer）均可登记词条、枪型专属伤害、奇特定义。
