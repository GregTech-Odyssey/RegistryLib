package com.gto.registrylib.datagen.provider;

import com.gto.registrylib.RegistryCore;
import com.gto.registrylib.datagen.ProviderType;
import com.gto.registrylib.util.ImageUtil;
import com.gto.registrylib.util.map.NestedMultiMap;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.neoforged.fml.LogicalSide;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class RegistryLibGeneralResourceProvider implements RegistryLibProvider {

    private final RegistryCore parent;
    private final Path path;

    private final NestedMultiMap<String, String, BiFunction<Path, OutputStream, Path>> generates = NestedMultiMap.create(HashMap::new, ArrayList::new);

    public RegistryLibGeneralResourceProvider(RegistryCore parent, PackOutput output) {
        this.parent = parent;
        path = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK).resolve(parent.getModid());
    }

    public void addItemTexture(BiFunction<Path, OutputStream, Path> generate) {
        addTexture("item", generate);
    }

    public void addBlockTexture(BiFunction<Path, OutputStream, Path> generate) {
        addTexture("block", generate);
    }

    public void addTexture(String name, BiFunction<Path, OutputStream, Path> generate) {
        add("textures", name, generate);
    }

    public void add(String a, String b, BiFunction<Path, OutputStream, Path> generate) {
        generates.put(a, b, generate);
    }

    public BiFunction<Path, OutputStream, Path> simpleTexture(
                                                              String path, Supplier<BufferedImage> image) {
        return (p, stream) -> {
            try {
                return ImageUtil.writeToStream(path, image.get(), p, stream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.CLIENT;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        parent.genData(ProviderType.GENERAL_RESOURCE, this);
        return CompletableFuture.runAsync(
                () -> {
                    cleanGeneratedNamespaces();
                    generates
                            .getMap()
                            .forEach(
                                    (k, v) -> {
                                        var path = this.path.resolve(k);
                                        v.forEach(
                                                (n, g) -> {
                                                    var target = path.resolve(n);
                                                    if (!Files.exists(target)) {
                                                        try {
                                                            Files.createDirectories(target);
                                                        } catch (Exception e) {
                                                            throw new RuntimeException(e);
                                                        }
                                                    }
                                                    g.forEach(
                                                            f -> {
                                                                try {
                                                                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                                                                    HashingOutputStream hashedBytes = new HashingOutputStream(Hashing.sha256(), bytes);
                                                                    var p = f.apply(target, hashedBytes);
                                                                    cache.writeIfNeeded(p, bytes.toByteArray(), hashedBytes.hash());
                                                                } catch (IOException var10) {
                                                                    LOGGER.error("Failed to save file to {}", path, var10);
                                                                }
                                                            });
                                                });
                                    });
                });
    }

    private void cleanGeneratedNamespaces() {
        parent.getGeneratedNamespaceCleanups().forEach(this::cleanGeneratedNamespace);
    }

    private void cleanGeneratedNamespace(String relativePath) {
        String normalizedPath = relativePath.replace('\\', '/');
        String[] parts = normalizedPath.split("/");
        if (parts.length < 3 || !"textures".equals(parts[0])) {
            throw new IllegalArgumentException(
                    "Generated resource cleanup path must target a specific textures subdirectory, " + "for example textures/item/generated_materials: " + relativePath);
        }
        Path target = path.resolve(relativePath).normalize();
        if (!target.startsWith(path)) {
            throw new IllegalArgumentException(
                    "Generated resource cleanup path escapes namespace: " + relativePath);
        }
        if (!Files.exists(target)) return;
        try (var stream = Files.walk(target)) {
            stream
                    .sorted(Comparator.reverseOrder())
                    .forEach(
                            p -> {
                                try {
                                    Files.deleteIfExists(p);
                                } catch (IOException e) {
                                    throw new RuntimeException("Failed to clean generated resource path: " + p, e);
                                }
                            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to clean generated resource path: " + target, e);
        }
    }

    @Override
    public String getName() {
        return parent.getModid();
    }
}
