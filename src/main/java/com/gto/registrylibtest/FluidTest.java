package com.gto.registrylibtest;

import com.gto.registrylib.util.entry.FluidEntry;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

/**
 * 示例：流体注册
 *
 * <p>演示 RegistryCore 流体 API 的三种典型用法：
 * <ol>
 *   <li><b>基础着色流体</b>（{@link #MOLTEN_IRON}、{@link #ACID}）——
 *       使用 registrylib 内置的可着色灰度纹理，通过 {@code .clientExtension()} 指定 ARGB 颜色；
 *       物理属性（密度、粘度、温度）通过 {@code .properties()} 配置。
 *   <li><b>自定义纹理流体</b>（{@link #LIQUID_MAGIC}）——
 *       直接传入 Minecraft 原版水流体纹理路径，展示如何复用已有纹理资源。
 *   <li><b>子对象作用域配置</b>（{@link #LIQUID_MAGIC}）——
 *       在同一个 consumer 内调用 {@code fluid.block()} 和 {@code fluid.bucket()} 分别
 *       定制流体方块属性与桶物品显示名，无需额外的外部调用。
 * </ol>
 *
 * <p><b>纹理约定：</b>registrylib 提供的默认纹理位于
 * {@code registrylib:block/fluid/liquid_still} 和 {@code registrylib:block/fluid/liquid_flow}，
 * 为单通道灰度图，适合通过 {@code .clientExtension()} 进行运行时着色。
 */
public class FluidTest {

    // registrylib 默认流体纹理（灰色可着色）
    private static final Identifier FLUID_STILL =
            Identifier.fromNamespaceAndPath("registrylib", "block/fluid/liquid_still");
    private static final Identifier FLUID_FLOW =
            Identifier.fromNamespaceAndPath("registrylib", "block/fluid/liquid_flow");

    // === 着色流体 ===

    /**
     * 熔融铁流体。
     *
     * <p>使用 registrylib 内置灰度纹理，通过 {@code clientExtension(ARGB)} 着为橙红色（{@code 0xFFFF4400}）。
     * 物理参数：密度 3000、粘度 6000、温度 1800 K。
     */
    public static final FluidEntry<BaseFlowingFluid.Flowing> MOLTEN_IRON =
            RegistryLibTest.REGISTRYLIB.fluid(
                    "molten_iron",
                    FLUID_STILL,
                    FLUID_FLOW,
                    fluid -> {
                        fluid
                                .properties(p -> p.density(3000).viscosity(6000).temperature(1800))
                                .lang("Molten Iron")
                                .clientExtension(FLUID_STILL, FLUID_FLOW, 0xFFFF4400);
                    });

    /**
     * 酸液流体。
     *
     * <p>着色为亮绿色（{@code 0xFF55DD00}），密度略高于水，粘度较低，流动较快。
     */
    public static final FluidEntry<BaseFlowingFluid.Flowing> ACID =
            RegistryLibTest.REGISTRYLIB.fluid(
                    "acid",
                    FLUID_STILL,
                    FLUID_FLOW,
                    fluid -> {
                        fluid
                                .properties(p -> p.density(1200).viscosity(800))
                                .lang("Acid")
                                .clientExtension(FLUID_STILL, FLUID_FLOW, 0xFF55DD00);
                    });

    // === 多层配置流体 ===

    /**
     * 魔法液体——演示在单个 consumer 内嵌套配置流体方块和桶物品。
     *
     * <p>复用原版水纹理；{@code fluid.block()} 作用域内可单独设置方块属性（此处配置自发光）；
     * {@code fluid.bucket()} 作用域内可覆盖桶物品的显示名称。
     * 自发光等级 15 表示该流体方块会为周围提供最亮的光照。
     */
    public static final FluidEntry<BaseFlowingFluid.Flowing> LIQUID_MAGIC =
            RegistryLibTest.REGISTRYLIB.fluid(
                    "liquid_magic",
                    Identifier.withDefaultNamespace("block/water_still"),
                    Identifier.withDefaultNamespace("block/water_flow"),
                    fluid -> {
                        fluid
                                .properties(p -> p.lightLevel(15).density(500).viscosity(200))
                                .lang("Liquid Magic");
                        fluid.block(block -> block.properties(p -> p.lightLevel(s -> 15)));
                        fluid.bucket(bucket -> bucket.lang("Liquid Magic Bucket"));
                    });
}
