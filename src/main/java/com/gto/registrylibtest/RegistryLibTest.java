package com.gto.registrylibtest;

import com.gto.registrylib.RegistryCore;

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
 *   <li>{@link ItemTest} — 物品注册（基础物品、CompositeItem、Tooltip 系统）
 *   <li>{@link BlockTest} — 方块注册（战利品表、Group 系统、自定义子类）
 *   <li>{@link BlockEntityTest} — BlockEntity 注册与客户端渲染器绑定
 *   <li>{@link FluidTest} — 流体注册（着色、多层配置、bucket/block 定制）
 *   <li>{@link AdvancementTest} — 所有成就进度树
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
        var _items = ItemTest.TEST_ITEM;

        // 方块示例
        var _blocks = BlockTest.TEST_BLOCK;

        // BlockEntity 示例
        var _be = BlockEntityTest.TIMER_BLOCK_ENTITY;

        // 流体示例
        var _fluids = FluidTest.MOLTEN_IRON;

        // 成就进度
        AdvancementTest.register();
    }

    public RegistryLibTest(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("registrylib test mod initializing...");
        LOGGER.info(
                "Registered {} items and {} blocks",
                REGISTRYLIB.getAll(Registries.ITEM).size(),
                REGISTRYLIB.getAll(Registries.BLOCK).size());
    }
}
