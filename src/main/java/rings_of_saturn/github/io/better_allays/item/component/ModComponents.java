package rings_of_saturn.github.io.better_allays.item.component;

import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

import static rings_of_saturn.github.io.better_allays.BetterAllays.MOD_ID;

public class ModComponents {
    public static final ComponentType<UUID> LINKED_ALLAY = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(MOD_ID, "linked_allay"),
            ComponentType.<UUID>builder().codec(Uuids.CODEC).build()
    );
    public static void registerModComponents(){

    }
}
