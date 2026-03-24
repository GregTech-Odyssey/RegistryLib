package com.gto.registrylib.datagen.provider;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.builders.EnchantmentBuilder;
import com.gto.registrylib.datagen.ProviderType;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.fml.LogicalSide;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Datagen provider that writes enchantment JSON files and enchantment tag JSON files.
 */
public class RegistryLibEnchantmentProvider
                                           implements RegistryLibProvider, EnchantmentBuilder.RegistryLibEnchantmentDataCollector {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final RegistryCore owner;
    private final Path dataPath;

    private final Map<String, JsonObject> enchantments = new LinkedHashMap<>();
    private final Map<TagKey<Enchantment>, List<Identifier>> tagEntries = new LinkedHashMap<>();

    public RegistryLibEnchantmentProvider(
                                          RegistryCore owner,
                                          PackOutput output,
                                          java.util.concurrent.CompletableFuture<net.minecraft.core.HolderLookup.Provider> provider) {
        this.owner = owner;
        this.dataPath = output.getOutputFolder(PackOutput.Target.DATA_PACK);
    }

    @Override
    public void add(String name, JsonObject enchantmentJson) {
        enchantments.put(name, enchantmentJson);
    }

    @Override
    public void addTag(TagKey<Enchantment> tag, Identifier enchantmentId) {
        tagEntries.computeIfAbsent(tag, k -> new ArrayList<>()).add(enchantmentId);
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.SERVER;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        owner.genData(ProviderType.ENCHANTMENT_DATA, this);
        List<CompletableFuture<?>> futures = new ArrayList<>();

        // Write enchantment JSONs
        for (var entry : enchantments.entrySet()) {
            Path jsonPath = dataPath
                    .resolve(owner.getModid())
                    .resolve("enchantment")
                    .resolve(entry.getKey() + ".json");

            byte[] bytes = GSON.toJson(entry.getValue()).getBytes(StandardCharsets.UTF_8);
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    HashingOutputStream hos = new HashingOutputStream(Hashing.sha1(), baos);
                    hos.write(bytes);
                    cache.writeIfNeeded(jsonPath, baos.toByteArray(), hos.hash());
                } catch (IOException e) {
                    throw new RuntimeException("Failed to write enchantment JSON: " + jsonPath, e);
                }
            }));
        }

        // Write tag JSONs
        for (var entry : tagEntries.entrySet()) {
            TagKey<Enchantment> tag = entry.getKey();
            List<Identifier> ids = entry.getValue();

            // Tag files go under the tag's namespace directory
            Path tagPath = dataPath
                    .resolve(tag.location().getNamespace())
                    .resolve("tags")
                    .resolve("enchantment")
                    .resolve(tag.location().getPath() + ".json");

            JsonObject tagJson = new JsonObject();
            tagJson.addProperty("replace", false);
            JsonArray values = new JsonArray();
            for (Identifier id : ids) {
                values.add(id.toString());
            }
            tagJson.add("values", values);

            byte[] bytes = GSON.toJson(tagJson).getBytes(StandardCharsets.UTF_8);
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    HashingOutputStream hos = new HashingOutputStream(Hashing.sha1(), baos);
                    hos.write(bytes);
                    cache.writeIfNeeded(tagPath, baos.toByteArray(), hos.hash());
                } catch (IOException e) {
                    throw new RuntimeException("Failed to write enchantment tag JSON: " + tagPath, e);
                }
            }));
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Enchantments";
    }
}
