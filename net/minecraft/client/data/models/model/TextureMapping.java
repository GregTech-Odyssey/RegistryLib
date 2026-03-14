package net.minecraft.client.data.models.model;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TextureMapping {
    private final Map<TextureSlot, Material> slots = Maps.newHashMap();
    private final Set<TextureSlot> forcedSlots = Sets.newHashSet();

    public TextureMapping put(TextureSlot slot, Material material) {
        this.slots.put(slot, material);
        return this;
    }

    public TextureMapping putForced(TextureSlot slot, Material material) {
        this.slots.put(slot, material);
        this.forcedSlots.add(slot);
        return this;
    }

    public Stream<TextureSlot> getForced() {
        return this.forcedSlots.stream();
    }

    public TextureMapping copySlot(TextureSlot from, TextureSlot to) {
        return this.put(to, this.slots.get(from));
    }

    public TextureMapping copyForced(TextureSlot from, TextureSlot to) {
        return this.putForced(to, this.slots.get(from));
    }

    public Material get(TextureSlot slot) {
        for (TextureSlot currentSlot = slot; currentSlot != null; currentSlot = currentSlot.getParent()) {
            Material result = this.slots.get(currentSlot);
            if (result != null) {
                return result;
            }
        }

        throw new IllegalStateException("Can't find texture for slot " + slot);
    }

    public TextureMapping copyAndUpdate(TextureSlot slot, Material material) {
        TextureMapping result = new TextureMapping();
        result.slots.putAll(this.slots);
        result.forcedSlots.addAll(this.forcedSlots);
        result.put(slot, material);
        return result;
    }

    public TextureMapping updateSlots(BiFunction<TextureSlot, Material, Material> mapper) {
        this.slots.replaceAll(mapper);
        return this;
    }

    public TextureMapping forceAllTranslucent() {
        return this.updateSlots((var0, material) -> material.withForceTranslucent(true));
    }

    public static TextureMapping cube(Block block) {
        Material texture = getBlockTexture(block);
        return cube(texture);
    }

    public static TextureMapping defaultTexture(Block block) {
        Material texture = getBlockTexture(block);
        return defaultTexture(texture);
    }

    public static TextureMapping defaultTexture(Material texture) {
        return new TextureMapping().put(TextureSlot.TEXTURE, texture);
    }

    public static TextureMapping cube(Material all) {
        return new TextureMapping().put(TextureSlot.ALL, all);
    }

    public static TextureMapping cross(Block block) {
        return singleSlot(TextureSlot.CROSS, getBlockTexture(block));
    }

    public static TextureMapping side(Block block) {
        return singleSlot(TextureSlot.SIDE, getBlockTexture(block));
    }

    public static TextureMapping crossEmissive(Block block) {
        return new TextureMapping().put(TextureSlot.CROSS, getBlockTexture(block)).put(TextureSlot.CROSS_EMISSIVE, getBlockTexture(block, "_emissive"));
    }

    public static TextureMapping cross(Material cross) {
        return singleSlot(TextureSlot.CROSS, cross);
    }

    public static TextureMapping plant(Block block) {
        return singleSlot(TextureSlot.PLANT, getBlockTexture(block));
    }

    public static TextureMapping plantEmissive(Block block) {
        return new TextureMapping().put(TextureSlot.PLANT, getBlockTexture(block)).put(TextureSlot.CROSS_EMISSIVE, getBlockTexture(block, "_emissive"));
    }

    public static TextureMapping plant(Material plant) {
        return singleSlot(TextureSlot.PLANT, plant);
    }

    public static TextureMapping rail(Block block) {
        return singleSlot(TextureSlot.RAIL, getBlockTexture(block));
    }

    public static TextureMapping rail(Material rail) {
        return singleSlot(TextureSlot.RAIL, rail);
    }

    public static TextureMapping wool(Block block) {
        return singleSlot(TextureSlot.WOOL, getBlockTexture(block));
    }

    public static TextureMapping flowerbed(Block block) {
        return new TextureMapping().put(TextureSlot.FLOWERBED, getBlockTexture(block)).put(TextureSlot.STEM, getBlockTexture(block, "_stem"));
    }

    public static TextureMapping wool(Material cross) {
        return singleSlot(TextureSlot.WOOL, cross);
    }

    public static TextureMapping stem(Block block) {
        return singleSlot(TextureSlot.STEM, getBlockTexture(block));
    }

    public static TextureMapping attachedStem(Block stem, Block upperStem) {
        return new TextureMapping().put(TextureSlot.STEM, getBlockTexture(stem)).put(TextureSlot.UPPER_STEM, getBlockTexture(upperStem));
    }

    public static TextureMapping pattern(Block block) {
        return singleSlot(TextureSlot.PATTERN, getBlockTexture(block));
    }

    public static TextureMapping fan(Block block) {
        return singleSlot(TextureSlot.FAN, getBlockTexture(block));
    }

    public static TextureMapping crop(Material id) {
        return singleSlot(TextureSlot.CROP, id);
    }

    public static TextureMapping pane(Block body, Block edge) {
        return new TextureMapping().put(TextureSlot.PANE, getBlockTexture(body)).put(TextureSlot.EDGE, getBlockTexture(edge, "_top"));
    }

    public static TextureMapping singleSlot(TextureSlot slot, Material id) {
        return new TextureMapping().put(slot, id);
    }

    public static TextureMapping column(Block block) {
        return new TextureMapping().put(TextureSlot.SIDE, getBlockTexture(block, "_side")).put(TextureSlot.END, getBlockTexture(block, "_top"));
    }

    public static TextureMapping cubeTop(Block block) {
        return new TextureMapping().put(TextureSlot.SIDE, getBlockTexture(block, "_side")).put(TextureSlot.TOP, getBlockTexture(block, "_top"));
    }

    public static TextureMapping pottedAzalea(Block block) {
        return new TextureMapping()
            .put(TextureSlot.PLANT, getBlockTexture(block, "_plant"))
            .put(TextureSlot.SIDE, getBlockTexture(block, "_side"))
            .put(TextureSlot.TOP, getBlockTexture(block, "_top"));
    }

    public static TextureMapping logColumn(Block block) {
        return new TextureMapping()
            .put(TextureSlot.SIDE, getBlockTexture(block))
            .put(TextureSlot.END, getBlockTexture(block, "_top"))
            .put(TextureSlot.PARTICLE, getBlockTexture(block));
    }

    public static TextureMapping column(Material side, Material end) {
        return new TextureMapping().put(TextureSlot.SIDE, side).put(TextureSlot.END, end);
    }

    public static TextureMapping fence(Block block) {
        return new TextureMapping()
            .put(TextureSlot.TEXTURE, getBlockTexture(block))
            .put(TextureSlot.SIDE, getBlockTexture(block, "_side"))
            .put(TextureSlot.TOP, getBlockTexture(block, "_top"));
    }

    public static TextureMapping customParticle(Block block) {
        return new TextureMapping().put(TextureSlot.TEXTURE, getBlockTexture(block)).put(TextureSlot.PARTICLE, getBlockTexture(block, "_particle"));
    }

    public static TextureMapping cubeBottomTop(Block block) {
        return new TextureMapping()
            .put(TextureSlot.SIDE, getBlockTexture(block, "_side"))
            .put(TextureSlot.TOP, getBlockTexture(block, "_top"))
            .put(TextureSlot.BOTTOM, getBlockTexture(block, "_bottom"));
    }

    public static TextureMapping cubeBottomTopWithWall(Block block) {
        Material side = getBlockTexture(block);
        return new TextureMapping()
            .put(TextureSlot.WALL, side)
            .put(TextureSlot.SIDE, side)
            .put(TextureSlot.TOP, getBlockTexture(block, "_top"))
            .put(TextureSlot.BOTTOM, getBlockTexture(block, "_bottom"));
    }

    public static TextureMapping columnWithWall(Block block) {
        Material side = getBlockTexture(block);
        return new TextureMapping()
            .put(TextureSlot.TEXTURE, side)
            .put(TextureSlot.WALL, side)
            .put(TextureSlot.SIDE, side)
            .put(TextureSlot.END, getBlockTexture(block, "_top"));
    }

    public static TextureMapping door(Material top, Material bottom) {
        return new TextureMapping().put(TextureSlot.TOP, top).put(TextureSlot.BOTTOM, bottom);
    }

    public static TextureMapping door(Block block) {
        return new TextureMapping().put(TextureSlot.TOP, getBlockTexture(block, "_top")).put(TextureSlot.BOTTOM, getBlockTexture(block, "_bottom"));
    }

    public static TextureMapping particle(Block block) {
        return new TextureMapping().put(TextureSlot.PARTICLE, getBlockTexture(block));
    }

    public static TextureMapping particle(Material id) {
        return new TextureMapping().put(TextureSlot.PARTICLE, id);
    }

    public static TextureMapping fire0(Block block) {
        return new TextureMapping().put(TextureSlot.FIRE, getBlockTexture(block, "_0"));
    }

    public static TextureMapping fire1(Block block) {
        return new TextureMapping().put(TextureSlot.FIRE, getBlockTexture(block, "_1"));
    }

    public static TextureMapping lantern(Block block) {
        return new TextureMapping().put(TextureSlot.LANTERN, getBlockTexture(block));
    }

    public static TextureMapping torch(Block block) {
        return new TextureMapping().put(TextureSlot.TORCH, getBlockTexture(block));
    }

    public static TextureMapping torch(Material id) {
        return new TextureMapping().put(TextureSlot.TORCH, id);
    }

    public static TextureMapping trialSpawner(Block block, String sideSuffix, String topSuffix) {
        return new TextureMapping()
            .put(TextureSlot.SIDE, getBlockTexture(block, sideSuffix))
            .put(TextureSlot.TOP, getBlockTexture(block, topSuffix))
            .put(TextureSlot.BOTTOM, getBlockTexture(block, "_bottom"));
    }

    public static TextureMapping vault(Block block, String frontSuffix, String sideSuffix, String topSuffix, String bottomSuffix) {
        return new TextureMapping()
            .put(TextureSlot.FRONT, getBlockTexture(block, frontSuffix))
            .put(TextureSlot.SIDE, getBlockTexture(block, sideSuffix))
            .put(TextureSlot.TOP, getBlockTexture(block, topSuffix))
            .put(TextureSlot.BOTTOM, getBlockTexture(block, bottomSuffix));
    }

    public static TextureMapping particleFromItem(Item item) {
        return new TextureMapping().put(TextureSlot.PARTICLE, getItemTexture(item));
    }

    public static TextureMapping commandBlock(Block block) {
        return new TextureMapping()
            .put(TextureSlot.SIDE, getBlockTexture(block, "_side"))
            .put(TextureSlot.FRONT, getBlockTexture(block, "_front"))
            .put(TextureSlot.BACK, getBlockTexture(block, "_back"));
    }

    public static TextureMapping orientableCube(Block block) {
        return new TextureMapping()
            .put(TextureSlot.SIDE, getBlockTexture(block, "_side"))
            .put(TextureSlot.FRONT, getBlockTexture(block, "_front"))
            .put(TextureSlot.TOP, getBlockTexture(block, "_top"))
            .put(TextureSlot.BOTTOM, getBlockTexture(block, "_bottom"));
    }

    public static TextureMapping orientableCubeOnlyTop(Block block) {
        return new TextureMapping()
            .put(TextureSlot.SIDE, getBlockTexture(block, "_side"))
            .put(TextureSlot.FRONT, getBlockTexture(block, "_front"))
            .put(TextureSlot.TOP, getBlockTexture(block, "_top"));
    }

    public static TextureMapping orientableCubeSameEnds(Block block) {
        return new TextureMapping()
            .put(TextureSlot.SIDE, getBlockTexture(block, "_side"))
            .put(TextureSlot.FRONT, getBlockTexture(block, "_front"))
            .put(TextureSlot.END, getBlockTexture(block, "_end"));
    }

    public static TextureMapping top(Block block) {
        return new TextureMapping().put(TextureSlot.TOP, getBlockTexture(block, "_top"));
    }

    public static TextureMapping craftingTable(Block table, Block bottomWood) {
        return new TextureMapping()
            .put(TextureSlot.PARTICLE, getBlockTexture(table, "_front"))
            .put(TextureSlot.DOWN, getBlockTexture(bottomWood))
            .put(TextureSlot.UP, getBlockTexture(table, "_top"))
            .put(TextureSlot.NORTH, getBlockTexture(table, "_front"))
            .put(TextureSlot.EAST, getBlockTexture(table, "_side"))
            .put(TextureSlot.SOUTH, getBlockTexture(table, "_side"))
            .put(TextureSlot.WEST, getBlockTexture(table, "_front"));
    }

    public static TextureMapping fletchingTable(Block table, Block bottomWood) {
        return new TextureMapping()
            .put(TextureSlot.PARTICLE, getBlockTexture(table, "_front"))
            .put(TextureSlot.DOWN, getBlockTexture(bottomWood))
            .put(TextureSlot.UP, getBlockTexture(table, "_top"))
            .put(TextureSlot.NORTH, getBlockTexture(table, "_front"))
            .put(TextureSlot.SOUTH, getBlockTexture(table, "_front"))
            .put(TextureSlot.EAST, getBlockTexture(table, "_side"))
            .put(TextureSlot.WEST, getBlockTexture(table, "_side"));
    }

    public static TextureMapping snifferEgg(String suffix) {
        return new TextureMapping()
            .put(TextureSlot.PARTICLE, getBlockTexture(Blocks.SNIFFER_EGG, suffix + "_north"))
            .put(TextureSlot.BOTTOM, getBlockTexture(Blocks.SNIFFER_EGG, suffix + "_bottom"))
            .put(TextureSlot.TOP, getBlockTexture(Blocks.SNIFFER_EGG, suffix + "_top"))
            .put(TextureSlot.NORTH, getBlockTexture(Blocks.SNIFFER_EGG, suffix + "_north"))
            .put(TextureSlot.SOUTH, getBlockTexture(Blocks.SNIFFER_EGG, suffix + "_south"))
            .put(TextureSlot.EAST, getBlockTexture(Blocks.SNIFFER_EGG, suffix + "_east"))
            .put(TextureSlot.WEST, getBlockTexture(Blocks.SNIFFER_EGG, suffix + "_west"));
    }

    public static TextureMapping driedGhast(String suffix) {
        return new TextureMapping()
            .put(TextureSlot.PARTICLE, getBlockTexture(Blocks.DRIED_GHAST, suffix + "_north"))
            .put(TextureSlot.BOTTOM, getBlockTexture(Blocks.DRIED_GHAST, suffix + "_bottom"))
            .put(TextureSlot.TOP, getBlockTexture(Blocks.DRIED_GHAST, suffix + "_top"))
            .put(TextureSlot.NORTH, getBlockTexture(Blocks.DRIED_GHAST, suffix + "_north"))
            .put(TextureSlot.SOUTH, getBlockTexture(Blocks.DRIED_GHAST, suffix + "_south"))
            .put(TextureSlot.EAST, getBlockTexture(Blocks.DRIED_GHAST, suffix + "_east"))
            .put(TextureSlot.WEST, getBlockTexture(Blocks.DRIED_GHAST, suffix + "_west"))
            .put(TextureSlot.TENTACLES, getBlockTexture(Blocks.DRIED_GHAST, suffix + "_tentacles"));
    }

    public static TextureMapping campfire(Block campfire) {
        return new TextureMapping().put(TextureSlot.LIT_LOG, getBlockTexture(campfire, "_log_lit")).put(TextureSlot.FIRE, getBlockTexture(campfire, "_fire"));
    }

    public static TextureMapping candleCake(Block block, boolean lit) {
        return new TextureMapping()
            .put(TextureSlot.PARTICLE, getBlockTexture(Blocks.CAKE, "_side"))
            .put(TextureSlot.BOTTOM, getBlockTexture(Blocks.CAKE, "_bottom"))
            .put(TextureSlot.TOP, getBlockTexture(Blocks.CAKE, "_top"))
            .put(TextureSlot.SIDE, getBlockTexture(Blocks.CAKE, "_side"))
            .put(TextureSlot.CANDLE, getBlockTexture(block, lit ? "_lit" : ""));
    }

    public static TextureMapping cauldron(Material contentTextureLoc) {
        return new TextureMapping()
            .put(TextureSlot.PARTICLE, getBlockTexture(Blocks.CAULDRON, "_side"))
            .put(TextureSlot.SIDE, getBlockTexture(Blocks.CAULDRON, "_side"))
            .put(TextureSlot.TOP, getBlockTexture(Blocks.CAULDRON, "_top"))
            .put(TextureSlot.BOTTOM, getBlockTexture(Blocks.CAULDRON, "_bottom"))
            .put(TextureSlot.INSIDE, getBlockTexture(Blocks.CAULDRON, "_inner"))
            .put(TextureSlot.CONTENT, contentTextureLoc);
    }

    public static TextureMapping sculkShrieker(boolean canSummon) {
        String innerTopString = canSummon ? "_can_summon" : "";
        return new TextureMapping()
            .put(TextureSlot.PARTICLE, getBlockTexture(Blocks.SCULK_SHRIEKER, "_bottom"))
            .put(TextureSlot.SIDE, getBlockTexture(Blocks.SCULK_SHRIEKER, "_side"))
            .put(TextureSlot.TOP, getBlockTexture(Blocks.SCULK_SHRIEKER, "_top"))
            .put(TextureSlot.INNER_TOP, getBlockTexture(Blocks.SCULK_SHRIEKER, innerTopString + "_inner_top"))
            .put(TextureSlot.BOTTOM, getBlockTexture(Blocks.SCULK_SHRIEKER, "_bottom"));
    }

    public static TextureMapping bars(Block block) {
        return new TextureMapping().put(TextureSlot.BARS, getBlockTexture(block)).put(TextureSlot.EDGE, getBlockTexture(block));
    }

    public static TextureMapping layer0(Item item) {
        return new TextureMapping().put(TextureSlot.LAYER0, getItemTexture(item));
    }

    public static TextureMapping layer0(Block block) {
        return new TextureMapping().put(TextureSlot.LAYER0, getBlockTexture(block));
    }

    public static TextureMapping layer0(Material id) {
        return new TextureMapping().put(TextureSlot.LAYER0, id);
    }

    public static TextureMapping layered(Material layer0, Material layer1) {
        return new TextureMapping().put(TextureSlot.LAYER0, layer0).put(TextureSlot.LAYER1, layer1);
    }

    public static TextureMapping layered(Material layer0, Material layer1, Material layer2) {
        return new TextureMapping().put(TextureSlot.LAYER0, layer0).put(TextureSlot.LAYER1, layer1).put(TextureSlot.LAYER2, layer2);
    }

    public static Material getBlockTexture(Block block) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        return new Material(id.withPrefix("block/"));
    }

    public static Material getBlockTexture(Block block, String suffix) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        return new Material(id.withPath(path -> "block/" + path + suffix));
    }

    public static Material getItemTexture(Item block) {
        Identifier id = BuiltInRegistries.ITEM.getKey(block);
        return new Material(id.withPrefix("item/"));
    }

    public static Material getItemTexture(Item item, String suffix) {
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        return new Material(id.withPath(path -> "item/" + path + suffix));
    }

    // Neo: Added to allow easier texture map copying
    public TextureMapping copy() {
        TextureMapping texturemapping = new TextureMapping();
        texturemapping.slots.putAll(this.slots);
        texturemapping.forcedSlots.addAll(this.forcedSlots);
        return texturemapping;
    }
}
