package com.gto.registrylibtest;

import com.gto.registrylib.util.entry.BlockEntityEntry;
import com.gto.registrylibtest.blockentity.TimerBlockEntity;
import com.gto.registrylibtest.client.TimerBlockEntityRenderer;

/**
 * 示例：BlockEntity 注册与客户端渲染器绑定
 *
 * <p>演示如何将 BlockEntity 类型与一组有效方块关联，并指定客户端渲染器：
 * <ul>
 *   <li>{@code .validBlocks()} — 声明哪些方块实例会附带此 BlockEntity；
 *       所有在 {@link BlockTest} 中注册的计时器方块均作为有效宿主。
 *   <li>{@code .renderer()} — 通过 supplier-of-supplier 惰性绑定客户端渲染器工厂，
 *       避免服务端加载时触发客户端类。
 * </ul>
 *
 * <p><b>架构说明：</b>渲染器代码位于 {@link com.gto.registrylibtest.client} 子包，
 * 严格隔离客户端与服务端逻辑。注册入口仍在此类，通过 lambda 延迟引用，
 * 确保只在客户端环境下才会实际加载渲染器类。
 */
public class BlockEntityTest {

    /**
     * 计时器 BlockEntity 类型。
     *
     * <p>绑定 {@link BlockTest#TIMER_TIER_1}、{@link BlockTest#TIMER_TIER_2}、
     * 和 {@link BlockTest#TIMER_TIER_3} 三个方块，共享同一套 BlockEntity 实现
     * ({@link TimerBlockEntity}) 与渲染逻辑 ({@link TimerBlockEntityRenderer})。
     */
    public static final BlockEntityEntry<TimerBlockEntity> TIMER_BLOCK_ENTITY =
            RegistryLibTest.REGISTRYLIB.blockEntity(
                    "timer",
                    TimerBlockEntity::new,
                    be -> {
                        be.validBlocks(
                                        BlockTest.TIMER_TIER_1,
                                        BlockTest.TIMER_TIER_2,
                                        BlockTest.TIMER_TIER_3)
                                .renderer(() -> TimerBlockEntityRenderer::new);
                    });
}
