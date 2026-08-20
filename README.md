# TACZ-校准站 — 全境封锁枪械装备系统

为 Minecraft 1.20.1 / Forge 47.4.20 / TACZ 打造的《全境封锁》校准站的枪械词条·校准·专精系统。基于 TACZ（Timeless and Classics Zero）与 TAA（Tacz Attribute Add）。

## 🤖本项目全程由DeepSeek V4 Flash 开发

## ✨ 功能特色

- **校准站（五大功能页）**：提取 / 校准 / 优化 / 分解 / 专精
- **词条系统**：39 种词条（红=伤害 / 黄=控制·精度·射速 / 蓝=弹匣·换弹·移速等辅助）
  - 4 档品质（基础绿 / 稀有蓝 / 精英紫 / 传说金），按特工等级 0~60 划分；弹容/弹丸为固定值（+N 发/颗），其余属性 10%~30%
  - 枪械类型专属伤害：步枪只出步枪伤害，且以低概率（约 30%）出现
  - 词条可跨枪提取、存入「词条库」永久保存、反复校准复用
- **真实暴击**：暴击几率 + 暴击伤害（自定义属性），枪击时按概率触发、按爆伤倍率加成
- **专精（Expertise）**：每把枪独立 30 级，击杀任意生物升级，每级 +2 固定伤害（最高 +60）
- **材料体系**：基础 / 稀有 / 精英 / 传说 / 终极 五种材料
  - 各档枪分解给对应档位材料，校准 / 优化消耗对应档位材料（优化额外消耗模组数据块）
  - 终极材料仅由奇特武器分解获得，专用于奇特武器优化
- **配件系统**：TACZ 配件自动登记词条（最多 3 条），装上枪后其词条加成加载到枪上，分解给 1 个档位材料
- **奇特武器（Exotic）**：数据包定义，红橙色名字与「奇特词条」，行为独立（禁校准/提取、只能优化、分解给终极材料）
- **泛枪包支持**：经 TimelessAPI 读取枪械类型，兼容任意 TACZ 第三方枪包
- 独立创造标签页「TACZ-校准站」、击杀反馈 / 升级提示 / 专精经验条

## 📦 安装要求

- Minecraft 1.20.1 + Forge 47.4.20
- 前置模组：TACZ 1.1.8-hotfix、TAA 1.3.7
- 枪械包：`tacz_default_gun`（放到游戏目录的 `tacz/` 文件夹）

## 🚀 安装步骤

1. 安装 1.20.1 Forge，启动一次生成 `mods` 文件夹
2. 把前置模组（TACZ / TAA）和本模组 jar 一起放进 `mods/`
3. 把 `tacz_default_gun` 枪械包整个文件夹放进 `<游戏目录>/tacz/`
4. 启动游戏，创造模式用「TACZ-校准站」标签页获取物品

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

GPL-3.0

本模组的枪械材质素材来自 **TAC**（Timeless and Classics），遵循其 **GPL-3.0** 协议。
