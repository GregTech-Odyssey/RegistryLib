package com.gto.registrylibtest.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.stream.Stream;

/**
 * 自定义 Ingredient：匹配指定标签中且剩余耐久 ≥ 给定阈值的物品。
 *
 * <p>
 * Custom ingredient that matches items within a given tag, whose remaining durability is at
 * or above a specified minimum threshold.
 *
 * <h3>JSON Format</h3>
 *
 * <pre>{@code
 * {
 *   "neoforge:ingredient_type": "registrylibtest:min_durability",
 *   "tag": "minecraft:swords",
 *   "min_durability": 100
 * }
 * }</pre>
 */
public class MinDurabilityIngredient implements ICustomIngredient {

    private final TagKey<Item> tag;
    private final int minDurability;

    // ── Codecs ───────────────────────────────────────────────────────────────

    public static final MapCodec<MinDurabilityIngredient> CODEC = RecordCodecBuilder.mapCodec(
            inst -> inst.group(
                    TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(e -> e.tag),
                    Codec.INT.fieldOf("min_durability").forGetter(e -> e.minDurability))
                    .apply(inst, MinDurabilityIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MinDurabilityIngredient> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

    // ── Constructor ──────────────────────────────────────────────────────────

    public MinDurabilityIngredient(TagKey<Item> tag, int minDurability) {
        this.tag = tag;
        this.minDurability = minDurability;
    }

    // ── ICustomIngredient ────────────────────────────────────────────────────

    @Override
    public boolean test(ItemStack stack) {
        if (!stack.is(tag)) return false;
        int maxDamage = stack.getMaxDamage();
        if (maxDamage <= 0) return false;
        int remaining = maxDamage - stack.getDamageValue();
        return remaining >= minDurability;
    }

    @Override
    public Stream<Holder<Item>> items() {
        return BuiltInRegistries.ITEM.getOrThrow(tag).stream();
    }

    @Override
    public boolean isSimple() {
        // We inspect stack components (durability), so this is not simple.
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return SimpleIngredientTypeExample.MIN_DURABILITY.get();
    }

    // ── Convenience factory ──────────────────────────────────────────────────

    /**
     * 创建一个匹配给定标签且剩余耐久 ≥ minDurability 的 Ingredient。
     *
     * <p>
     * Creates an ingredient matching items in the given tag with remaining durability ≥
     * minDurability.
     */
    public static Ingredient of(TagKey<Item> tag, int minDurability) {
        return new MinDurabilityIngredient(tag, minDurability).toVanilla();
    }

    // ── equals / hashCode ────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MinDurabilityIngredient that)) return false;
        return minDurability == that.minDurability && tag.equals(that.tag);
    }

    @Override
    public int hashCode() {
        return 31 * tag.hashCode() + minDurability;
    }
}
