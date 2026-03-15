package com.gto.registrylibtest.blockentity;

import com.gto.registrylib.util.entry.BlockEntityEntry;
import com.gto.registrylibtest.RegistryLibTest;
import com.gto.registrylibtest.block.FullBlockExample;
import com.gto.registrylibtest.client.TimerBlockEntityRenderer;

/**
 * 使用 BlockEntityBuilder 全部 API 的复杂示例。
 *
 * <p>涵盖：validBlocks（多方块绑定）/ renderer（延迟加载客户端渲染器）。
 */
public class FullBlockEntityExample {

    public static final BlockEntityEntry<TimerBlockEntity> TIMER_BLOCK_ENTITY =
            RegistryLibTest.REGISTRYLIB
                    .blockEntity("timer", TimerBlockEntity::new)
                    // --- validBlocks: 将一个 BlockEntity 类型绑定到多个方块 ---
                    .validBlocks(
                            FullBlockExample.TIMER_TIER_1,
                            FullBlockExample.TIMER_TIER_2,
                            FullBlockExample.TIMER_TIER_3)
                    // --- renderer: 通过 supplier-of-supplier 惰性绑定客户端渲染器 ---
                    // 外层 Supplier 确保只在客户端环境加载，避免服务端加载客户端类
                    .renderer(() -> TimerBlockEntityRenderer::new)
                    .register();
}
