package dev.shadowsoffire.apotheosis.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.shadowsoffire.apotheosis.Apotheosis;
import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;

/**
 * Validates that all client model definitions, custom item model types,
 * atlas sprite sources, and referenced texture resources exist and resolve cleanly.
 */
public class ClientResourceValidationTest {

    private static final Path ASSETS_DIR = Path.of("src/main/resources/assets");
    private static final Set<String> MOD_NAMESPACES = Set.of(
        "apotheosis",
        "apothic_enchanting",
        "apothic_spawners",
        "apothic_attributes"
    );

    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        GemModel.init();
    }

    @Test
    void testGemDispatchModelRegistration() {
        // Verify custom model type is registered in ItemModels
        assertNotNull(GemModel.Unbaked.MAP_CODEC);
        var idMapper = ItemModels.ID_MAPPER;
        assertNotNull(idMapper, "ItemModels ID_MAPPER must be accessible");
        assertTrue(idMapper.values().contains(GemModel.Unbaked.MAP_CODEC), "ID_MAPPER must contain gem_dispatch codec");

        // Verify gem item definition references apotheosis:gem_dispatch
        Path gemItemDef = ASSETS_DIR.resolve("apotheosis/items/gem.json");
        assertTrue(Files.exists(gemItemDef), "gem.json item definition must exist");

        try (FileReader reader = new FileReader(gemItemDef.toFile())) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject modelObj = json.getAsJsonObject("model");
            assertNotNull(modelObj, "Model element in gem.json must not be null");
            String type = modelObj.get("type").getAsString();
            assertTrue(type.equals("apotheosis:gem_dispatch"), "Gem model type must be apotheosis:gem_dispatch");
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testBuiltinGemModelsAndTextures() {
        // Verify that every built-in gem variant has a model file and corresponding texture on disk
        assertFalse(GemModel.BUILTIN_GEMS.isEmpty(), "Built-in gems list must not be empty");

        for (String gemPath : GemModel.BUILTIN_GEMS) {
            Path modelPath = ASSETS_DIR.resolve("apotheosis/models/item/gems/" + gemPath + ".json");
            assertTrue(Files.exists(modelPath), "Gem model must exist on disk: " + modelPath);

            try (FileReader reader = new FileReader(modelPath.toFile())) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                JsonObject textures = json.getAsJsonObject("textures");
                assertNotNull(textures, "Textures object must exist in " + modelPath);

                String layer0 = textures.get("layer0").getAsString();
                assertTrue(layer0.startsWith("apotheosis:"), "Texture must be under apotheosis namespace: " + layer0);

                String textureRelPath = layer0.substring("apotheosis:".length());
                Path texturePath = ASSETS_DIR.resolve("apotheosis/textures/" + textureRelPath + ".png");
                assertTrue(Files.exists(texturePath), "Gem texture must exist on disk: " + texturePath);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    void testAtlasSourceConfigurations() throws IOException {
        // Verify blocks.json atlas includes blocks/ directory source without overlapping into items/
        Path blocksAtlas = ASSETS_DIR.resolve("minecraft/atlases/blocks.json");
        assertTrue(Files.exists(blocksAtlas), "blocks.json atlas definition must exist");

        try (FileReader reader = new FileReader(blocksAtlas.toFile())) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray sources = json.getAsJsonArray("sources");
            assertNotNull(sources);

            boolean hasBlocksDir = false;
            boolean hasItemsDir = false;
            for (JsonElement el : sources) {
                JsonObject obj = el.getAsJsonObject();
                if ("minecraft:directory".equals(obj.get("type").getAsString())) {
                    String src = obj.get("source").getAsString();
                    if ("blocks".equals(src)) hasBlocksDir = true;
                    if ("items".equals(src)) hasItemsDir = true;
                }
            }
            assertTrue(hasBlocksDir, "blocks.json must declare directory source for 'blocks'");
            assertFalse(hasItemsDir, "blocks.json must not declare directory source for 'items' to prevent multiple atlas collision");
        }

        // Verify items.json atlas exists and includes directory sources without overlapping into blocks/
        Path itemsAtlas = ASSETS_DIR.resolve("minecraft/atlases/items.json");
        assertTrue(Files.exists(itemsAtlas), "items.json atlas definition must exist");

        try (FileReader reader = new FileReader(itemsAtlas.toFile())) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray sources = json.getAsJsonArray("sources");
            assertNotNull(sources);

            boolean hasItemsDir = false;
            boolean hasBlocksDir = false;
            for (JsonElement el : sources) {
                JsonObject obj = el.getAsJsonObject();
                if ("minecraft:directory".equals(obj.get("type").getAsString())) {
                    String src = obj.get("source").getAsString();
                    if ("items".equals(src)) hasItemsDir = true;
                    if ("blocks".equals(src)) hasBlocksDir = true;
                }
            }
            assertTrue(hasItemsDir, "items.json must declare directory source for 'items'");
            assertFalse(hasBlocksDir, "items.json must not declare directory source for 'blocks' to prevent multiple atlas collision");
        }
    }

    @Test
    void testAllModelTextureReferencesExistOnDisk() throws IOException {
        List<String> missing = new ArrayList<>();
        int checkedCount = 0;

        try (Stream<Path> stream = Files.walk(ASSETS_DIR)) {
            List<Path> modelFiles = stream
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().contains("/models/") && p.toString().endsWith(".json"))
                .toList();

            for (Path modelFile : modelFiles) {
                try (FileReader reader = new FileReader(modelFile.toFile())) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    if (!json.has("textures")) continue;

                    JsonObject textures = json.getAsJsonObject("textures");
                    for (String key : textures.keySet()) {
                        JsonElement val = textures.get(key);
                        String texStr = null;
                        if (val.isJsonPrimitive()) {
                            texStr = val.getAsString();
                        }
                        else if (val.isJsonObject() && val.getAsJsonObject().has("sprite")) {
                            texStr = val.getAsJsonObject().get("sprite").getAsString();
                        }

                        if (texStr == null) continue;

                        // Resolve inherited variable references
                        String resolved = resolveTextureVariable(texStr, textures, new HashSet<>());
                        if (resolved == null || resolved.startsWith("#")) continue;

                        if (resolved.contains(":")) {
                            String[] parts = resolved.split(":", 2);
                            String ns = parts[0];
                            String path = parts[1];

                            if (MOD_NAMESPACES.contains(ns)) {
                                checkedCount++;
                                Path expectedPng = ASSETS_DIR.resolve(ns + "/textures/" + path + ".png");
                                if (!Files.exists(expectedPng)) {
                                    missing.add(modelFile + " -> " + resolved + " (missing " + expectedPng + ")");
                                }
                            }
                        }
                    }
                }
            }
        }

        assertTrue(checkedCount > 100, "Should have verified over 100 texture references (checked " + checkedCount + ")");
        assertTrue(missing.isEmpty(), "Missing texture files detected on disk:\n" + String.join("\n", missing));
    }

    private static String resolveTextureVariable(String val, JsonObject textures, Set<String> seen) {
        String curr = val;
        while (curr.startsWith("#")) {
            String varName = curr.substring(1);
            if (!seen.add(varName)) return null;
            if (!textures.has(varName)) return null;
            JsonElement el = textures.get(varName);
            if (!el.isJsonPrimitive()) return null;
            curr = el.getAsString();
        }
        return curr;
    }
}
