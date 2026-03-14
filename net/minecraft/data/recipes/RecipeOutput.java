package net.minecraft.data.recipes;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import org.jspecify.annotations.Nullable;

public interface RecipeOutput extends net.neoforged.neoforge.common.extensions.IRecipeOutputExtension {
    default void accept(ResourceKey<Recipe<?>> id, Recipe<?> recipe, @Nullable AdvancementHolder advancement) {
        accept(id, recipe, advancement, new net.neoforged.neoforge.common.conditions.ICondition[0]);
    }

    Advancement.Builder advancement();

    void includeRootAdvancement();
}
