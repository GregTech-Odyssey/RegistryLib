package com.gto.registrylibtest.fluid;

import com.gto.registrylib.util.entry.FluidEntry;
import com.gto.registrylibtest.ModRegistryCore;
import com.gto.registrylibtest.RegistryLibTest;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

/**
 * 使用 FluidBuilder 全部 API 的复杂流体示例。
 *
 * <p>
 * 涵盖：properties / fluidProperties / lang / clientExtension（3 参数着色） / block（自定义配置）/
 * bucket（自定义配置）/ tag / removeTag / source / noBlock / noBucket。
 */
public class FullFluidExample {

    private static final Identifier FLUID_STILL = Identifier.fromNamespaceAndPath("registrylib", "block/fluid/liquid_still");
    private static final Identifier FLUID_FLOW = Identifier.fromNamespaceAndPath("registrylib", "block/fluid/liquid_flow");

    // ── 着色流体（全 API） ─────────────────────────────────────────────────

    /** 熔融铁：使用灰度纹理 + ARGB 着色，自定义 FluidType 物理属性、 流体方块（自发光）、桶物品（自定义显示名），并打上流体标签。 */
    public static final FluidEntry<BaseFlowingFluid.Flowing> MOLTEN_IRON = RegistryLibTest.REGISTRYLIB
            .fluid("molten_iron", FLUID_STILL, FLUID_FLOW)
            // --- properties: 配置 FluidType 的物理参数 ---
            .properties(p -> p.density(3000).viscosity(6000).temperature(1800))
            // --- lang: 自定义流体显示名称 ---
            .lang("Molten Iron")
            // --- lang (zh_cn): 简体中文显示名称 ---
            .lang(ModRegistryCore.LANG_ZH_CN, "熔融铁")
            // --- clientExtension（3 参数）: 灰度纹理 + ARGB 颜色着色 ---
            .clientExtension(FLUID_STILL, FLUID_FLOW, 0xFFFF4400)
            // --- tag: 给流体添加标签 ---
            .tag(FluidTags.LAVA)
            // --- block: 自定义流体方块属性（自发光） ---
            .block(block -> block.properties(p -> p.lightLevel(s -> 12)))
            // --- bucket: 自定义桶物品显示名 ---
            .bucket(
                    bucket -> bucket.lang("Molten Iron Bucket").lang(ModRegistryCore.LANG_ZH_CN, "熔融铁桶"))
            .register();

    // ── 使用原版纹理 + 多层配置 ────────────────────────────────────────────

    /** 魔法液体：复用原版水纹理、嵌套配置流体方块和桶物品。 */
    public static final FluidEntry<BaseFlowingFluid.Flowing> LIQUID_MAGIC = RegistryLibTest.REGISTRYLIB
            .fluid(
                    "liquid_magic",
                    Identifier.withDefaultNamespace("block/water_still"),
                    Identifier.withDefaultNamespace("block/water_flow"))
            // --- properties: 物理参数（含光照等级） ---
            .properties(p -> p.lightLevel(15).density(500).viscosity(200))
            // --- lang ---
            .lang("Liquid Magic")
            // --- lang (zh_cn): 简体中文显示名称 ---
            .lang(ModRegistryCore.LANG_ZH_CN, "魔法液体")
            // --- block: 方块子对象配置 ---
            .block(block -> block.properties(p -> p.lightLevel(s -> 15)))
            // --- bucket: 桶物品子对象配置 ---
            .bucket(
                    bucket -> bucket.lang("Liquid Magic Bucket").lang(ModRegistryCore.LANG_ZH_CN, "魔法液体桶"))
            .register();
}
