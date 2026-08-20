package dev.netrox.quickslot;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public enum ItemRule {
    SWORD("Меч"),
    BLOCKS("Блоки"),
    PICKAXE("Кирка"),
    AXE("Топор"),
    BOW("Лук"),
    CONSUMABLE("Расходники"),
    UTILITY("Утилиты"),
    FREE("Свободно");

    private final String displayName;

    ItemRule(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public ItemRule next() {
        ItemRule[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public boolean matches(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return switch (this) {
            case SWORD -> stack.is(Items.WOODEN_SWORD) || stack.is(Items.STONE_SWORD) || stack.is(Items.IRON_SWORD)
                || stack.is(Items.GOLDEN_SWORD) || stack.is(Items.DIAMOND_SWORD) || stack.is(Items.NETHERITE_SWORD);
            case BLOCKS -> stack.getItem() instanceof BlockItem;
            case PICKAXE -> stack.is(Items.WOODEN_PICKAXE) || stack.is(Items.STONE_PICKAXE) || stack.is(Items.IRON_PICKAXE)
                || stack.is(Items.GOLDEN_PICKAXE) || stack.is(Items.DIAMOND_PICKAXE) || stack.is(Items.NETHERITE_PICKAXE);
            case AXE -> stack.is(Items.WOODEN_AXE) || stack.is(Items.STONE_AXE) || stack.is(Items.IRON_AXE)
                || stack.is(Items.GOLDEN_AXE) || stack.is(Items.DIAMOND_AXE) || stack.is(Items.NETHERITE_AXE);
            case BOW -> stack.is(Items.BOW) || stack.is(Items.CROSSBOW);
            case CONSUMABLE -> stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE)
                || stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION)
                || stack.is(Items.ENDER_PEARL) || stack.is(Items.SNOWBALL) || stack.is(Items.EGG)
                || stack.is(Items.FIRE_CHARGE) || stack.is(Items.WIND_CHARGE);
            case UTILITY -> stack.is(Items.SHEARS) || stack.is(Items.WATER_BUCKET) || stack.is(Items.LAVA_BUCKET)
                || stack.is(Items.MILK_BUCKET) || stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.TNT)
                || stack.is(Items.FISHING_ROD) || stack.is(Items.FIREWORK_ROCKET);
            case FREE -> false;
        };
    }
}
