package dev.netrox.quickslot;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public enum ItemRule {
    SWORD("Меч"),
    BLOCKS("Блоки"),
    GOLDEN_APPLE("Золотое яблоко"),
    SHEARS("Ножницы"),
    PICKAXE("Кирка"),
    AXE("Топор"),
    BOW("Лук"),
    ARROWS("Стрелы"),
    TNT("TNT"),
    FIREBALL("Fireball"),
    ENDER_PEARL("Эндер-жемчуг"),
    LADDER("Лестницы"),
    WATER("Вода"),
    CONSUMABLE("Расходники"),
    UTILITY("Утилиты"),
    EMPTY("Держать пустым"),
    FREE("Не трогать");

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
            case BLOCKS -> stack.getItem() instanceof BlockItem && !stack.is(Items.TNT) && !stack.is(Items.LADDER);
            case GOLDEN_APPLE -> stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE);
            case SHEARS -> stack.is(Items.SHEARS);
            case PICKAXE -> stack.is(Items.WOODEN_PICKAXE) || stack.is(Items.STONE_PICKAXE) || stack.is(Items.IRON_PICKAXE)
                || stack.is(Items.GOLDEN_PICKAXE) || stack.is(Items.DIAMOND_PICKAXE) || stack.is(Items.NETHERITE_PICKAXE);
            case AXE -> stack.is(Items.WOODEN_AXE) || stack.is(Items.STONE_AXE) || stack.is(Items.IRON_AXE)
                || stack.is(Items.GOLDEN_AXE) || stack.is(Items.DIAMOND_AXE) || stack.is(Items.NETHERITE_AXE);
            case BOW -> stack.is(Items.BOW) || stack.is(Items.CROSSBOW);
            case ARROWS -> stack.is(Items.ARROW) || stack.is(Items.SPECTRAL_ARROW) || stack.is(Items.TIPPED_ARROW);
            case TNT -> stack.is(Items.TNT);
            case FIREBALL -> stack.is(Items.FIRE_CHARGE);
            case ENDER_PEARL -> stack.is(Items.ENDER_PEARL);
            case LADDER -> stack.is(Items.LADDER);
            case WATER -> stack.is(Items.WATER_BUCKET);
            case CONSUMABLE -> stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION)
                || stack.is(Items.SNOWBALL) || stack.is(Items.EGG) || stack.is(Items.WIND_CHARGE);
            case UTILITY -> stack.is(Items.LAVA_BUCKET) || stack.is(Items.MILK_BUCKET) || stack.is(Items.FLINT_AND_STEEL)
                || stack.is(Items.FISHING_ROD) || stack.is(Items.FIREWORK_ROCKET);
            case EMPTY, FREE -> false;
        };
    }

    public int priority(ItemStack stack) {
        if (!matches(stack)) return -1;
        return switch (this) {
            case SWORD -> tier(stack, Items.WOODEN_SWORD, Items.GOLDEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD);
            case PICKAXE -> tier(stack, Items.WOODEN_PICKAXE, Items.GOLDEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE);
            case AXE -> tier(stack, Items.WOODEN_AXE, Items.GOLDEN_AXE, Items.STONE_AXE, Items.IRON_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE);
            default -> stack.getCount();
        };
    }

    private static int tier(ItemStack stack, net.minecraft.world.item.Item... items) {
        for (int i = items.length - 1; i >= 0; i--) {
            if (stack.is(items[i])) return i + 1;
        }
        return 0;
    }
}
