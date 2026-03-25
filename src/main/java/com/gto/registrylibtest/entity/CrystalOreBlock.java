package com.gto.registrylibtest.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 晶体矿方块。当玩家左键攻击（挖掘）此方块时，记录该玩家的 UUID 和时间戳。 附近的 {@link CrystalGuardian} 通过 {@link CrystalMinerSensor}
 * 查询此记录， 从而发现正在挖矿的玩家并追踪攻击。
 */
public class CrystalOreBlock extends Block {

    /** 最近挖掘过此方块的玩家 — UUID → gameTime 时间戳 */
    private static final Map<UUID, Long> RECENT_MINERS = new ConcurrentHashMap<>();

    /** 挖掘记忆保持时间：200 ticks = 10 秒 */
    private static final long MINING_MEMORY_TICKS = 200L;

    public CrystalOreBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide()) {
            RECENT_MINERS.put(player.getUUID(), level.getGameTime());
        }
        super.attack(state, level, pos, player);
    }

    /** 检查玩家是否在最近 {@value MINING_MEMORY_TICKS} ticks 内挖掘过晶体矿。 */
    public static boolean isRecentMiner(Player player, long currentGameTime) {
        Long lastMined = RECENT_MINERS.get(player.getUUID());
        if (lastMined == null) return false;
        if (currentGameTime - lastMined > MINING_MEMORY_TICKS) {
            RECENT_MINERS.remove(player.getUUID());
            return false;
        }
        return true;
    }

    /** 清理过期的挖掘记录。 */
    public static void cleanStaleEntries(long currentGameTime) {
        RECENT_MINERS.entrySet().removeIf(e -> currentGameTime - e.getValue() > MINING_MEMORY_TICKS);
    }
}
