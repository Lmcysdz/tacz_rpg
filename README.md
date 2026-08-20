# TACZ RPG — 全境封锁枪械装备系统

为 Minecraft 1.20.1 / Forge 47.4.20 / TACZ 打造的《全境封锁》校准站的枪械词条·校准·专精系统。基于 TACZ（Timeless and Classics Zero）与 TAA（Tacz Attribute Add）。

## 🤖本项目全程由DeepSeek V4 Flash 开发

## ✨ 功能特色

- **校准站（五大功能页）**：提取 / 校准 / 优化 / 分解 / 专精
- **词条系统**：39 种词条
  - 4 档品质（基础 / 稀有 / 精英 / 传说），按特工等级 0~60 划分，词条数值 +5%~30%
  - 词条可跨枪提取、存入「词条库」永久保存、反复校准复用
- **专精（Expertise）**：每把枪独立 30 级，击杀任意生物升级，每级 +1 固定伤害（最高 +30）
- **材料体系**：基础 / 稀有 / 精英 / 传说 / 终极 五种材料
  - 各档枪分解给对应档位材料，校准 / 优化消耗对应档位材料（优化额外消耗模组数据块）
  - 终极材料仅由奇特武器分解获得，专用于奇特武器优化
- **配件系统**：TACZ 配件自动登记词条（最多 3 条），装上枪后其词条加成加载到枪上，分解给 1 个档位材料
- **奇特武器（Exotic）**：数据包定义，红橙色名字与「奇特词条」，行为独立（禁校准/提取、只能优化、分解给终极材料）
- **泛枪包支持**：经 TimelessAPI 读取枪械类型，兼容任意 TACZ 第三方枪包
- 独立创造标签页「TACZ RPG」、击杀反馈 / 升级提示 / 专精经验条

## 📦 安装要求

- Minecraft 1.20.1 + Forge 47.4.20
- 前置模组：TACZ 1.1.8-hotfix、TAA 1.3.7

## 🎮 调试命令

```
/taczrpg setlevel <0-60>      设置特工等级（决定词条品质档与词条数）
/taczrpg getlevel             查询特工等级
/taczrpg setexpertise <0-30>  设置当前手持枪的专精等级
/taczrpg getexpertise         查询当前手持枪的专精等级与经验
/taczrpg getattr              打印当前枪词条对应的 TAA 属性实际值（验证词条挂载）
```

## 🌟 制作奇特武器（数据包）

在 `data/tacz_rpg/exotic_weapons/` 放一个 JSON：

```json
{
  "gun": "tacz:ak47",
  "display_name": "[自定义名称]",
  "exotic_affix": { "affix": "bullet_rifle", "value": 0.35 },
  "affixes": [
    { "affix": "crit_damage", "value": 0.10 },
    { "affix": "magazine_capacity", "value": 0.08 }
  ]
}
```

## 📝 许可

MIT
