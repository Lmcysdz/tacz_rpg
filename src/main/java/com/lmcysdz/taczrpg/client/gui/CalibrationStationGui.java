package com.lmcysdz.taczrpg.client.gui;

import com.lmcysdz.taczrpg.api.affix.AffixOperation;
import com.lmcysdz.taczrpg.api.affix.AffixSystem;
import com.lmcysdz.taczrpg.api.affix.AffixType;
import com.lmcysdz.taczrpg.api.exotic.ExoticWeapon;
import com.lmcysdz.taczrpg.api.exotic.ExoticWeaponManager;
import com.lmcysdz.taczrpg.api.resource.MaterialQuality;
import com.lmcysdz.taczrpg.api.resource.ResourceCostSystem;
import com.lmcysdz.taczrpg.capability.AffixLibraryCapability;
import com.lmcysdz.taczrpg.network.CalibrationActionPacket;
import com.lmcysdz.taczrpg.network.ModNetwork;
import com.lmcysdz.taczrpg.registry.ModItems;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.IGun;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Map;

/**
 * 校准站 GUI —— 4 个选项卡：提取、校准、优化、分解。
 * 面板自适应，词条进度条 0→15%，材料图标+数量显示，校准消耗递增。
 */
public class CalibrationStationGui extends Screen {

    // 配色
    private static final int PANEL_BG    = 0xD0141C26;
    private static final int BORDER      = 0xFF4E9BE0;
    private static final int TAB_SEL     = 0xCC1B2A3A;
    private static final int TAB_NORM    = 0x66141C26;
    private static final int TEXT        = 0xFFE8EEF4;
    private static final int TEXT_DIM    = 0x77E8EEF4;
    private static final int BTN_BG      = 0x991B2A3A;
    private static final int BTN_DIS     = 0x33141C26;
    private static final int BAR_BG      = 0x33141C26;
    private static final int BAR_FILL    = 0xFF4E9BE0;
    private static final int BAR_FULL    = 0xFFF2C14E;
    private static final int SEL_BG      = 0x334E9BE0;
    private static final int EXOTIC_RGB  = 0xFF6B4A;     // 红橙色（Tooltip/样式用）
    private static final int EXOTIC_COLOR = 0xFFFF6B4A;  // 红橙色（GUI 绘制用 ARGB）

    // 布局
    private static final int TAB_H = 30, TAB_GAP = 8, TAB_W = 100;
    private static final int MARGIN_L = 14, MARGIN_R = 10, GAP = 10;
    private static final int BAR_W = 80, BAR_H = 6;
    private static final int BTN_W = 80, BTN_H = 16;

    private int tab = 0; // 0=提取, 1=校准, 2=优化, 3=分解
    private int selectedSlot = -1; // 校准页选中的枪械词条位
    private int calibScroll = 0;   // 校准页词条库滚动偏移
    private final java.util.ArrayList<Btn> btns = new java.util.ArrayList<>();

    public CalibrationStationGui() {
        super(Component.translatable("container.tacz_rpg.calibration_station"));
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new CalibrationStationGui());
    }

    // ===== 自适应尺寸 =====
    private int pw() { return Math.min(500, width - 20); }
    private int ph() { return Math.min(360, height - 20); }
    private int pxc() { return (width - pw()) / 2; }
    private int pyc() { return (height - ph()) / 2; }
    private int tabX() { return pw() - MARGIN_R - TAB_W; }
    private int contW() { return tabX() - MARGIN_L - GAP; }

    // ===== 渲染 =====
    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        btns.clear();
        int px = pxc(), py = pyc(), w = pw(), h = ph();

        // 面板
        g.fill(px, py, px + w, py + h, PANEL_BG);
        g.fill(px, py, px + w, py + 1, BORDER);
        g.fill(px, py + h - 1, px + w, py + h, BORDER);
        g.fill(px, py, px + 1, py + h, BORDER);
        g.fill(px + w - 1, py, px + w, py + h, BORDER);

        g.drawCenteredString(font, title, px + w / 2, py + 8, TEXT);

        drawTabs(g, px, py);
        drawContent(g, px, py);
    }

    private void drawTabs(GuiGraphics g, int px, int py) {
        String[] names = {"提取", "校准", "优化", "分解", "专精"};
        int[] accs = {0xFF4E9BE0, 0xFFF2C14E, 0xFF2ECC71, 0xFFE74C3C, 0xFF00D5C2};
        int tx = px + tabX();
        for (int i = 0; i < 5; i++) {
            int y = py + 44 + i * (TAB_H + TAB_GAP);
            g.fill(tx, y, tx + TAB_W, y + TAB_H, i == tab ? TAB_SEL : TAB_NORM);
            g.fill(tx, y, tx + 2, y + TAB_H, accs[i]);
            g.fill(tx, y, tx + TAB_W, y + 1, BORDER);
            g.fill(tx, y + TAB_H - 1, tx + TAB_W, y + TAB_H, BORDER);
            g.drawCenteredString(font, names[i], tx + TAB_W / 2, y + (TAB_H - 8) / 2, i == tab ? TEXT : TEXT_DIM);
        }
    }

    private void drawContent(GuiGraphics g, int px, int py) {
        Player p = Minecraft.getInstance().player;
        if (p == null) return;
        ItemStack gun = p.getMainHandItem();
        int cx = px + MARGIN_L, cw = contW();
        switch (tab) {
            case 0 -> drawExtract(g, cx, py, cw, p, gun);
            case 1 -> drawCalibrate(g, cx, py, cw, p, gun);
            case 2 -> drawOptimize(g, cx, py, cw, p, gun);
            case 3 -> drawDisassemble(g, cx, py, cw, p, gun);
            case 4 -> drawExpertise(g, cx, py, cw, p, gun);
        }
    }

    // ============ 提取页 ============
    private void drawExtract(GuiGraphics g, int cx, int py, int cw, Player p, ItemStack gun) {
        int cur = py + 44;
        if (!(gun.getItem() instanceof IGun)) { txt(g, cx, cur, "需要手持一把枪械", TEXT_DIM); return; }
        cur = gunInfo(g, cx, cw, cur, gun);
        if (ExoticWeaponManager.isExotic(gun)) { txt(g, cx, cur, "奇特武器无法提取词条", EXOTIC_COLOR); return; }
        txt(g, cx, cur, "—— 提取词条 ——", TEXT_DIM); cur += 18;

        List<String> ks = AffixSystem.getAttrKeys(gun.getOrCreateTag());
        int cs = gun.getOrCreateTag().getInt(AffixSystem.CALIBRATED_SLOT_TAG);
        if (ks.isEmpty()) { txt(g, cx, cur, "该枪尚未拥有词条", TEXT_DIM); return; }

        for (int i = 0; i < ks.size(); i++) {
            String k = ks.get(i);
            AffixType t = AffixType.byKey(k);
            if (t == null) continue;
            double v = gun.getOrCreateTag().getDouble(k);
            boolean lk = (cs == i);
            Component lb = Component.literal("◈ ").append(Component.translatable(t.translationKey())).append(fmt(v)).withStyle(t.category().chatColor());
            if (lk) lb = lb.copy().append(Component.literal("  [锁定]").withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
            txt(g, cx, cur, lb, t.category().color());
            bar(g, cx + cw - BAR_W - BTN_W - 4, cur + 4, BAR_W, BAR_H, (float)(Math.abs(v) / AffixSystem.MAX_AFFIX_VALUE), t.category().color());
            int fi = i;
            btn(g, cx + cw - BTN_W, cur - 2, BTN_W, BTN_H,
                    Component.literal("提取"),
                    () -> ModNetwork.sendToServer(new CalibrationActionPacket(CalibrationActionPacket.Action.EXTRACT, t.key())),
                    !lk, t.category().color());
            cur += 22;
        }
        cur += 4;
        txt(g, cx, cur, "§7提取无消耗，提取后枪械永久销毁", TEXT_DIM);
    }

    // ============ 校准页 ============
    private void drawCalibrate(GuiGraphics g, int cx, int py, int cw, Player p, ItemStack gun) {
        int cur = py + 44;
        if (!(gun.getItem() instanceof IGun)) { txt(g, cx, cur, "需要手持一把枪械", TEXT_DIM); return; }
        cur = gunInfo(g, cx, cw, cur, gun);
        if (ExoticWeaponManager.isExotic(gun)) { txt(g, cx, cur, "奇特武器无法校准（仅可优化）", EXOTIC_COLOR); return; }

        var tag = gun.getOrCreateTag();
        List<String> ks = AffixSystem.getAttrKeys(tag);
        int cs = tag.getInt(AffixSystem.CALIBRATED_SLOT_TAG);
        int ri = AffixSystem.getRankIndex(tag.getInt(AffixSystem.LEVEL_TAG));
        int tc = tag.getInt(AffixSystem.CALIBRATED_COUNT_TAG);
        int cost = ResourceCostSystem.getCalibrationTotalCost(ri, tc);
        int matCnt = ResourceCostSystem.countItem(p, ResourceCostSystem.getTierMaterial(gun));

        // 已锁定枪默认选中锁定槽；否则手动选择（越界则重置）
        if (cs != -1) selectedSlot = cs;
        else if (selectedSlot >= ks.size()) selectedSlot = -1;

        int panelBottom = py + ph();
        int bottom = panelBottom - 36;

        // ① 枪械当前词条（点击选择要替换的位）
        txt(g, cx, cur, "—— 枪械词条 ——", TEXT_DIM); cur += 16;
        if (ks.isEmpty()) {
            txt(g, cx, cur, "该枪尚未拥有词条", TEXT_DIM); cur += 16;
        } else {
            for (int i = 0; i < ks.size(); i++) {
                if (cur + 18 > bottom) break;
                String k = ks.get(i);
                AffixType t = AffixType.byKey(k);
                boolean lockedOut = (cs != -1 && cs != i); // 已锁定其他位则本槽不可选
                Component lb;
                if (t == null) {
                    lb = Component.literal("◈ " + k).withStyle(net.minecraft.ChatFormatting.GRAY);
                } else {
                    double v = tag.getDouble(k);
                    lb = Component.literal("◈ ").append(Component.translatable(t.translationKey())).append(fmt(v)).withStyle(t.category().chatColor());
                }
                boolean sel = (selectedSlot == i);
                int fi = i;
                selRow(g, cx, cur, cw, 16, lb, sel, !lockedOut, () -> selectedSlot = fi, t == null ? TEXT_DIM : t.category().color());
                cur += 18;
            }
        }
        cur += 2;

        // ② 材料信息（按枪械档位材料）
        txt(g, cx, cur, "—— 词条库 ——", TEXT_DIM); cur += 16;
        Item tierMat = ResourceCostSystem.getTierMaterial(gun);
        String tierMatName = Component.translatable(tierMat.getDescriptionId()).getString();
        g.renderItem(new ItemStack(tierMat, cost), cx, cur);
        g.renderItemDecorations(font, new ItemStack(tierMat, cost), cx, cur);
        txt(g, cx + 20, cur + 2, "消耗: " + cost + " " + tierMatName, TEXT_DIM);
        txt(g, cx + 20, cur + 12, "背包: " + matCnt + (selectedSlot >= 0 ? "  |  已选槽位 " + (selectedSlot + 1) : ""), TEXT_DIM);
        cur += 22;

        Map<String, Float> lib = AffixLibraryCapability.get(p)
                .map(l -> (Map<String, Float>) l.getAll())
                .orElse(java.util.Collections.emptyMap());
        if (lib.isEmpty()) { txt(g, cx, cur, "词条库为空，请先提取", TEXT_DIM); return; }

        // ③ 词条库（滚轮滚动，仅显示同分类/全部分类）
        int visible = Math.max(1, (bottom - cur) / 20);
        int maxScroll = Math.max(0, lib.size() - visible);
        if (calibScroll > maxScroll) calibScroll = maxScroll;
        if (calibScroll < 0) calibScroll = 0;

        int idx = 0;
        for (var e : lib.entrySet()) {
            if (idx++ < calibScroll) continue;
            if (cur + 20 > bottom) break;
            AffixType t = AffixType.byKey(e.getKey());
            if (t == null) continue;
            float v = e.getValue();
            Component lb = Component.literal("▸ ").append(Component.translatable(t.translationKey())).append(fmt(v)).withStyle(t.category().chatColor());
            txt(g, cx, cur, lb, t.category().color());
            String fk = e.getKey();
            btn(g, cx + cw - BTN_W, cur - 2, BTN_W, BTN_H,
                    Component.literal("替换"),
                    () -> ModNetwork.sendToServer(new CalibrationActionPacket(CalibrationActionPacket.Action.CALIBRATE, fk, selectedSlot)),
                    selectedSlot >= 0 && matCnt >= cost, t.category().color());
            cur += 20;
        }
        if (calibScroll > 0 || calibScroll + visible < lib.size()) {
            txt(g, cx, panelBottom - 16, "§7滚轮滚动查看更多词条", TEXT_DIM);
        }
    }

    // ============ 优化页 ============
    private void drawOptimize(GuiGraphics g, int cx, int py, int cw, Player p, ItemStack gun) {
        int cur = py + 44;
        if (!(gun.getItem() instanceof IGun)) { txt(g, cx, cur, "需要手持一把枪械", TEXT_DIM); return; }
        cur = gunInfo(g, cx, cw, cur, gun);

        ExoticWeapon exotic = ExoticWeaponManager.getExotic(gun);
        int ultMat = ResourceCostSystem.countItem(p, ModItems.MATERIALS.get(MaterialQuality.ULTIMATE).get());
        int modMat = ResourceCostSystem.countItem(p, ModItems.MODULE_ITEM.get());
        Item tierMat = ResourceCostSystem.getTierMaterial(gun);
        int tierMatCnt = ResourceCostSystem.countItem(p, tierMat);
        if (exotic != null) {
            txt(g, cx, cur, "优化消耗（奇特）：" + ResourceCostSystem.EXOTIC_OPTIMIZE_ULTIMATE + " 终极材料 + " + ResourceCostSystem.EXOTIC_OPTIMIZE_MODULE + " 模组数据块", TEXT_DIM); cur += 12;
            txt(g, cx, cur, "背包: 终极" + ultMat + " + 模组" + modMat, TEXT_DIM); cur += 12;
        } else {
            String tierMatName = Component.translatable(tierMat.getDescriptionId()).getString();
            txt(g, cx, cur, "优化消耗：" + ResourceCostSystem.OPTIMIZE_TIER_MATERIAL + " " + tierMatName + " + " + ResourceCostSystem.OPTIMIZE_MODULE + " 模组数据块", TEXT_DIM); cur += 12;
            txt(g, cx, cur, "背包: " + tierMatCnt + " + 模组" + modMat, TEXT_DIM); cur += 12;
        }

        var tag = gun.getOrCreateTag();
        List<String> ks = AffixSystem.getAttrKeys(tag);
        if (ks.isEmpty()) { txt(g, cx, cur, "该枪尚未拥有词条", TEXT_DIM); return; }
        int cs = tag.getInt(AffixSystem.CALIBRATED_SLOT_TAG);

        Map<String, Float> lib = AffixLibraryCapability.get(p)
                .map(l -> (Map<String, Float>) l.getAll())
                .orElse(java.util.Collections.emptyMap());

        for (int i = 0; i < ks.size(); i++) {
            String k = ks.get(i);
            AffixType t = AffixType.byKey(k);
            if (t == null) continue;
            double v = tag.getDouble(k);
            boolean lk = (cs == i);
            Float cap = lib.get(k);
            // 奇特词条：红橙 + "奇特词条：" 前缀；普通词条正常显示
            boolean isExoticAffix = exotic != null && k.equals(exotic.exoticAffix().key());
            Component lb;
            if (isExoticAffix) {
                lb = Component.literal("奇特词条：").append(Component.translatable(t.translationKey())).append(fmt(v))
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(EXOTIC_RGB)));
            } else {
                lb = Component.literal("◈ ").append(Component.translatable(t.translationKey())).append(fmt(v)).withStyle(t.category().chatColor());
                if (lk) lb = lb.copy().append(Component.literal("  [锁定]").withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
                if (cap != null) {
                    lb = lb.copy().append(Component.literal("  目标 " + Math.round(cap * 100) + "%").withStyle(net.minecraft.ChatFormatting.GRAY));
                } else {
                    lb = lb.copy().append(Component.literal("  库无记录").withStyle(net.minecraft.ChatFormatting.DARK_RED));
                }
            }
            txt(g, cx, cur, lb, isExoticAffix ? EXOTIC_COLOR : t.category().color());
            // 进度条满刻度 = 词条库记录值
            float frac = (cap != null && cap > 0) ? (float) Math.min(1.0, Math.abs(v) / cap) : 0f;
            bar(g, cx + cw - BAR_W - BTN_W - 4, cur + 4, BAR_W, BAR_H, frac, isExoticAffix ? EXOTIC_COLOR : t.category().color());
            boolean canOpt = !lk && cap != null && Math.abs(v) < cap - 1e-4f
                    && (exotic != null
                            ? (ultMat >= ResourceCostSystem.EXOTIC_OPTIMIZE_ULTIMATE && modMat >= ResourceCostSystem.EXOTIC_OPTIMIZE_MODULE)
                            : (tierMatCnt >= ResourceCostSystem.OPTIMIZE_TIER_MATERIAL && modMat >= ResourceCostSystem.OPTIMIZE_MODULE));
            btn(g, cx + cw - BTN_W, cur - 2, BTN_W, BTN_H,
                    Component.literal("优化"),
                    () -> ModNetwork.sendToServer(new CalibrationActionPacket(CalibrationActionPacket.Action.OPTIMIZE, t.key())),
                    canOpt, t.category().color());
            cur += 22;
        }
    }

    // ============ 分解页 ============
    private void drawDisassemble(GuiGraphics g, int cx, int py, int cw, Player p, ItemStack gun) {
        int cur = py + 44;
        boolean isGun = gun.getItem() instanceof IGun;
        boolean isAttachment = gun.getItem() instanceof IAttachment;
        if (!isGun && !isAttachment) { txt(g, cx, cur, "需要手持一把枪械或配件", TEXT_DIM); return; }
        cur = gunInfo(g, cx, cw, cur, gun);

        List<String> ks = AffixSystem.getAttrKeys(gun.getOrCreateTag());
        boolean can = !ks.isEmpty();

        // 配件：分解给 1 个档位材料，无专精/校准/提取
        if (isAttachment) {
            txt(g, cx, cur, "配件分解：获得 1 个档位材料", TEXT_DIM); cur += 16;
            txt(g, cx, cur, "§7配件无专精 / 校准 / 提取", TEXT_DIM); cur += 16;
            btn(g, cx + cw / 2 - 50, cur, 100, 20,
                    Component.literal("分解配件"),
                    () -> ModNetwork.sendToServer(new CalibrationActionPacket(CalibrationActionPacket.Action.DISASSEMBLE, "")),
                    can, BORDER);
            return;
        }

        int el = gun.getOrCreateTag().getInt(AffixSystem.EXPERTISE_LEVEL_TAG);
        int pc = ResourceCostSystem.getDisassembleCount(el);

        txt(g, cx, cur, "专精等级: " + el + " / " + AffixOperation.MAX_EXPERTISE_LEVEL, TEXT); cur += 16;
        txt(g, cx, cur, "分解获得: " + pc + " 个档位材料", TEXT_DIM); cur += 16;
        txt(g, cx, cur, "§7分解后词条不会入库", TEXT_DIM); cur += 16;

        btn(g, cx + cw / 2 - 50, cur, 100, 20,
                Component.literal("分解武器"),
                () -> ModNetwork.sendToServer(new CalibrationActionPacket(CalibrationActionPacket.Action.DISASSEMBLE, "")),
                can, BORDER);
    }

    // ============ 专精页 ============
    private void drawExpertise(GuiGraphics g, int cx, int py, int cw, Player p, ItemStack gun) {
        int cur = py + 44;
        if (!(gun.getItem() instanceof IGun)) { txt(g, cx, cur, "需要手持一把枪械", TEXT_DIM); return; }
        cur = gunInfo(g, cx, cw, cur, gun);

        int lv = AffixOperation.getExpertiseLevel(gun);
        int exp = gun.getOrCreateTag().getInt(AffixSystem.EXPERTISE_EXP_TAG);
        txt(g, cx, cur, "专精等级: " + lv + " / " + AffixOperation.MAX_EXPERTISE_LEVEL, TEXT); cur += 20;
        txt(g, cx, cur, "经验: " + exp + " / " + AffixOperation.EXP_TO_LEVEL, TEXT_DIM); cur += 14;
        bar(g, cx, cur, cw, BAR_H, (float) exp / AffixOperation.EXP_TO_LEVEL, BAR_FILL); cur += 16;

        txt(g, cx, cur, "§7击杀任意生物获得专精经验（含奇特武器）", TEXT_DIM); cur += 16;
        txt(g, cx, cur, "§7每级 +1 固定伤害，上限 +30", TEXT_DIM);
    }

    // ============ 辅助方法 ============

    private int gunInfo(GuiGraphics g, int cx, int cw, int cur, ItemStack gun) {
        if (ExoticWeaponManager.isExotic(gun)) {
            ExoticWeapon exotic = ExoticWeaponManager.getExotic(gun);
            txt(g, cx, cur, "奇特 " + exotic.displayName(), EXOTIC_COLOR);
        } else {
            // 枪名按品质档着色（基础绿 / 稀有蓝 / 精英紫 / 传说金）
            String rc = AffixSystem.getRankForLevel(gun.getOrCreateTag().getInt(AffixSystem.LEVEL_TAG)).color();
            net.minecraft.ChatFormatting cf = net.minecraft.ChatFormatting.getByCode(rc.charAt(1));
            txt(g, cx, cur, gun.getHoverName().copy().withStyle(cf), TEXT);
        }
        cur += 16;
        int lv = AffixOperation.getExpertiseLevel(gun);
        int at = AffixSystem.getAttrKeys(gun.getOrCreateTag()).size();
        txt(g, cx, cur, Component.translatable("gui.tacz_rpg.gun_info", String.valueOf(at), String.valueOf(lv)), TEXT_DIM);
        cur += 22;
        g.fill(cx, cur - 6, cx + cw, cur - 5, 0x334E9BE0);
        return cur;
    }

    private void bar(GuiGraphics g, int x, int y, int w, int h, float p, int c) {
        g.fill(x, y, x + w, y + h, BAR_BG);
        int fw = (int)(w * Math.min(1f, p));
        if (fw > 0) g.fill(x, y, x + fw, y + h, p >= 1f ? BAR_FULL : c);
    }

    private void txt(GuiGraphics g, int x, int y, String k, int c) {
        // 原始字符串：直接绘制，正确解析 § 格式码（不要当作翻译 key）
        g.drawString(font, k, x, y, c, false);
    }

    private void txt(GuiGraphics g, int x, int y, Component t, int c) {
        g.drawString(font, t, x, y, c, false);
    }

    /** 可点击的行（校准页词条位选择）：选中高亮，不可用置灰 */
    private void selRow(GuiGraphics g, int x, int y, int w, int h, Component label, boolean selected, boolean enabled, Runnable onClick, int color) {
        if (selected) {
            g.fill(x, y, x + w, y + h, SEL_BG);
        }
        if (!enabled) {
            label = label.copy().withStyle(net.minecraft.ChatFormatting.DARK_GRAY);
        }
        txt(g, x, y + (h - 8) / 2, label, enabled ? color : TEXT_DIM);
        if (selected) {
            txt(g, x + w - 40, y + (h - 8) / 2, "◀ 已选", TEXT);
        }
        btns.add(new Btn(x, y, w, h, onClick, enabled));
    }

    private void btn(GuiGraphics g, int x, int y, int w, int h, Component l, Runnable a, boolean e, int ac) {
        g.fill(x, y, x + w, y + h, e ? BTN_BG : BTN_DIS);
        g.fill(x, y, x + w, y + 1, e ? ac : 0x334E9BE0);
        g.fill(x, y + h - 1, x + w, y + h, e ? ac : 0x334E9BE0);
        g.fill(x, y, x + 1, y + h, e ? ac : 0x334E9BE0);
        g.fill(x + w - 1, y, x + w, y + h, e ? ac : 0x334E9BE0);
        int tc = e ? TEXT : TEXT_DIM;
        int tw = font.width(l);
        g.drawString(font, l, x + (w - tw) / 2, y + (h - 8) / 2, tc, false);
        btns.add(new Btn(x, y, w, h, a, e));
    }

    private record Btn(int x, int y, int w, int h, Runnable a, boolean e) {}

    // ============ 交互 ============
    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn != GLFW.GLFW_MOUSE_BUTTON_LEFT) return super.mouseClicked(mx, my, btn);
        int px = pxc(), py = pyc();
        for (int i = 0; i < 5; i++) {
            int y = py + 44 + i * (TAB_H + TAB_GAP);
            if (isIn(mx, my, px + tabX(), y, TAB_W, TAB_H)) { tab = i; return true; }
        }
        for (Btn b : btns) {
            if (b.e() && isIn(mx, my, b.x(), b.y(), b.w(), b.h())) { b.a().run(); return true; }
        }
        return super.mouseClicked(mx, my, btn);
    }

    private static boolean isIn(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        if (tab == 1) {
            calibScroll -= (int) Math.signum(delta);
            if (calibScroll < 0) calibScroll = 0;
            return true;
        }
        return super.mouseScrolled(mx, my, delta);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private static String fmt(double v) {
        int pct = (int) Math.round(Math.abs(v) * 100);
        return " " + (v < 0 ? "-" : "+") + pct + "%";
    }
}