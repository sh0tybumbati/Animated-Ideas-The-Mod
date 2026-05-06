package com.garrett.mod.mixin;

import com.garrett.mod.GarrettMod;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
    @Inject(method = "apply", at = @At("HEAD"))
    protected void onApply(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler, CallbackInfo info) {
        if (!GarrettMod.CONFIG.enableLogicalStairs) return;

        List<Identifier> toRemove = new ArrayList<>();
        Map<Identifier, JsonElement> toAdd = new java.util.HashMap<>();

        map.forEach((id, element) -> {
            if (element.isJsonObject()) {
                JsonObject json = element.getAsJsonObject();
                
                // Check if it's a shaped crafting recipe
                if (json.has("type") && json.get("type").getAsString().equals("minecraft:crafting_shaped")) {
                    String resultId = "";
                    if (json.has("result")) {
                        JsonElement result = json.get("result");
                        if (result.isJsonObject()) {
                            resultId = result.getAsJsonObject().get("id").getAsString();
                        } else if (result.isJsonPrimitive()) {
                            resultId = result.getAsString();
                        }
                    }

                    // Identify if the result is a stair block
                    if (resultId.endsWith("_stairs")) {
                        // Create Right-facing Orientation
                        JsonObject rightStair = json.deepCopy();
                        JsonArray rightPattern = new JsonArray();
                        rightPattern.add(" #");
                        rightPattern.add("##");
                        rightStair.add("pattern", rightPattern);
                        updateResultCount(rightStair, 4);
                        toAdd.put(id, rightStair);

                        // Create Left-facing Orientation
                        JsonObject leftStair = json.deepCopy();
                        JsonArray leftPattern = new JsonArray();
                        leftPattern.add("# ");
                        leftPattern.add("##");
                        leftStair.add("pattern", leftPattern);
                        updateResultCount(leftStair, 4);
                        
                        // Use a unique ID for the mirrored recipe
                        Identifier mirroredId = Identifier.of(id.getNamespace(), id.getPath() + "_mirrored");
                        toAdd.put(mirroredId, leftStair);
                    }
                }
            }
        });

        map.putAll(toAdd);
    }

    private void updateResultCount(JsonObject json, int count) {
        if (json.has("result")) {
            JsonElement result = json.get("result");
            if (result.isJsonObject()) {
                result.getAsJsonObject().addProperty("count", count);
            } else if (result.isJsonPrimitive()) {
                // If it was just a string, convert to object to support count
                String id = result.getAsString();
                JsonObject newResult = new JsonObject();
                newResult.addProperty("id", id);
                newResult.addProperty("count", count);
                json.add("result", newResult);
            }
        }
    }
}
