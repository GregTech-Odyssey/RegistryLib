package com.gto.registrylibtest.recipe;

import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

/**
 * 祭坛方块实体：每 tick 检测上方的物品实体，匹配配方后转化。
 *
 * <p>Altar block entity: scans for item entities above, matches altar recipes, and processes them.
 */
public class AltarBlockEntity extends BlockEntity {

    private int progress = 0;
    private @Nullable AltarRecipe activeRecipe = null;

    public AltarBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AltarBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        AABB scanArea = new AABB(pos.above()).inflate(0.25, 0.5, 0.25);
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, scanArea);
        if (items.isEmpty()) {
            be.progress = 0;
            be.activeRecipe = null;
            return;
        }

        ItemEntity itemEntity = items.getFirst();
        ItemStack stack = itemEntity.getItem();
        SingleRecipeInput input = new SingleRecipeInput(stack);

        // 尝试匹配配方
        if (be.activeRecipe == null || !be.activeRecipe.matches(input, level)) {
            Optional<RecipeHolder<AltarRecipe>> found = serverLevel
                    .recipeAccess()
                    .getRecipeFor(SimpleRecipeExample.ALTAR.getType(), input, serverLevel);
            if (found.isEmpty()) {
                be.progress = 0;
                be.activeRecipe = null;
                return;
            }
            be.activeRecipe = found.get().value();
            be.progress = 0;
        }

        // 处理进度
        be.progress++;
        if (be.progress >= be.activeRecipe.getProcessingTime()) {
            ItemStack result = be.activeRecipe.assemble(input);
            stack.shrink(1);
            if (stack.isEmpty()) {
                itemEntity.discard();
            } else {
                itemEntity.setItem(stack.copy());
            }
            // 生成结果物品
            ItemEntity resultEntity = new ItemEntity(
                    level,
                    pos.getX() + 0.5,
                    pos.getY() + 1.2,
                    pos.getZ() + 0.5,
                    result);
            resultEntity.setDeltaMovement(0, 0.15, 0);
            level.addFreshEntity(resultEntity);
            be.progress = 0;
            be.activeRecipe = null;
        }
        be.setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("progress", Codec.INT, progress);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        progress = input.read("progress", Codec.INT).orElse(0);
    }
}
