package com.gto.registrylibtest;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylibtest.advancement.FullAdvancementExample;
import com.gto.registrylibtest.advancement.SimpleAdvancementExample;
import com.gto.registrylibtest.block.FullBlockExample;
import com.gto.registrylibtest.block.SimpleBlockExample;
import com.gto.registrylibtest.blockentity.FullBlockEntityExample;
import com.gto.registrylibtest.blockentity.SimpleBlockEntityExample;
import com.gto.registrylibtest.fluid.FullFluidExample;
import com.gto.registrylibtest.fluid.SimpleFluidExample;
import com.gto.registrylibtest.item.FullItemExample;
import com.gto.registrylibtest.item.SimpleItemExample;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import org.slf4j.Logger;

/**
 * Mod 入口类，仅负责：
 * <ol>
 *   <li>创建共享的 {@link RegistryCore} 实例 {@link #REGISTRYLIB}；
 *   <li>注册默认创造标签页；
 *   <li>按顺序触发各示例类的静态初始化，使其注册调用在模组加载期间执行。
 * </ol>
 *
 * <p><b>示例文件索引：</b>
 * <ul>
 *   <li>{@link SimpleItemExample} / {@link FullItemExample} — 物品注册
 *   <li>{@link SimpleBlockExample} / {@link FullBlockExample} — 方块注册
 *   <li>{@link SimpleBlockEntityExample} / {@link FullBlockEntityExample} — BlockEntity 注册
 *   <li>{@link SimpleFluidExample} / {@link FullFluidExample} — 流体注册
 *   <li>{@link SimpleAdvancementExample} / {@link FullAdvancementExample} — 成就进度
 * </ul>
 */
@Mod(RegistryLibTest.MOD_ID)
public class RegistryLibTest {

    public static final String MOD_ID = "registrylibtest";
    private static final Logger LOGGER = LogUtils.getLogger();

    /** 共享的注册核心，所有示例类均通过此实例发起注册调用。 */
    public static final RegistryCore REGISTRYLIB = RegistryCore.create(MOD_ID);

    // === Creative Tab ===
    static {
        REGISTRYLIB.defaultCreativeTab("test_tab").register();
    }

    // === 触发各示例类静态初始化 ===
    // Java 的类静态字段在首次访问时才初始化；通过在此引用各类的任意字段，
    // 强制其 static 块在 RegistryLibTest 加载时同步执行。
    static {
        // 物品示例
        var _item1 = SimpleItemExample.COPPER_COIN;
        var _item2 = FullItemExample.MAGIC_WAND;

        // 方块示例
        var _block1 = SimpleBlockExample.DECORATIVE_STONE;
        var _block2 = FullBlockExample.MAGIC_ORE;

        // BlockEntity 示例
        var _be1 = SimpleBlockEntityExample.SIMPLE_TIMER_BE;
        var _be2 = FullBlockEntityExample.TIMER_BLOCK_ENTITY;

        // 流体示例
        var _fluid1 = SimpleFluidExample.ACID;
        var _fluid2 = FullFluidExample.MOLTEN_IRON;

        // 成就进度
        SimpleAdvancementExample.register();
        FullAdvancementExample.register();
    }

    public RegistryLibTest(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("registrylib test mod initializing...");
        LOGGER.info(
                "Registered {} items and {} blocks",
                REGISTRYLIB.getAll(Registries.ITEM).size(),
                REGISTRYLIB.getAll(Registries.BLOCK).size());
    }
}
