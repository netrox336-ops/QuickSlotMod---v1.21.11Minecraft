package dev.netrox.quickslot;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

public enum BlockType {
    WOOL("Шерсть"),
    PLANKS("Доски"),
    END_STONE("Эндерняк"),
    CLAY("Терракота"),
    GLASS("Стекло"),
    OBSIDIAN("Обсидиан"),
    OTHER("Другое");

    private final String displayName;

    BlockType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static BlockType fromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return OTHER;

        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (path.endsWith("_wool")) return WOOL;
        if (path.endsWith("_planks")) return PLANKS;
        if (path.equals("end_stone")) return END_STONE;
        if (path.equals("terracotta") || (path.endsWith("_terracotta") && !path.endsWith("_glazed_terracotta"))) return CLAY;
        if (path.equals("glass") || path.endsWith("_stained_glass")) return GLASS;
        if (path.equals("obsidian")) return OBSIDIAN;
        return OTHER;
    }
}
