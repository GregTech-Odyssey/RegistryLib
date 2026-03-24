package com.gto.registrylibtest.recipe;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * 注入器方块实体：带机器等级的配方处理器。
 *
 * <p>
 * Infuser block entity: a tiered recipe processor. Scans for item entities above, matches
 * infuser recipes (factoring in machine tier), processes them, awards experience on completion.
 */
public class InfuserBlockEntity extends BlockEntity {

    private int progress = 0;
    private @Nullable InfuserRecipe activeRecipe = null;

    public InfuserBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** 获取当前机器等级（从方块获取） / Get the machine tier from the block */
    public int getMachineTier() {
        if (getBlockState().getBlock() instanceof InfuserBlock infuser) {
            return infuser.getTier();
        }
        return 1;
    }

    public static void serverTick(
                                  Level level, BlockPos pos, BlockState state, InfuserBlockEntity be) {
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
        int machineTier = be.getMachineTier();
        InfuserRecipe.InfuserInput input = new InfuserRecipe.InfuserInput(stack, machineTier);

        // 尝试匹配配方（含机器等级检查）
        if (be.activeRecipe == null || !be.activeRecipe.matches(input, level)) {
            Optional<RecipeHolder<InfuserRecipe>> found = serverLevel
                    .recipeAccess()
                    .getRecipeFor(FullRecipeExample.INFUSER.getType(), input, serverLevel);
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
            float experience = be.activeRecipe.getExperience();

            stack.shrink(1);
            if (stack.isEmpty()) {
                itemEntity.discard();
            } else {
                itemEntity.setItem(stack.copy());
            }

            // 生成结果物品
            ItemEntity resultEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, result);
            resultEntity.setDeltaMovement(0, 0.15, 0);
            level.addFreshEntity(resultEntity);

            // 给予经验
            if (experience > 0) {
                ExperienceOrb.award(serverLevel, pos.above().getCenter(), (int) experience);
            }

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
