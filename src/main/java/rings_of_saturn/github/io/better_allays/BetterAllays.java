package rings_of_saturn.github.io.better_allays;

import net.fabricmc.api.ModInitializer;

import static rings_of_saturn.github.io.better_allays.item.component.ModComponents.registerModComponents;

public class BetterAllays implements ModInitializer {
    public static String MOD_ID = "better_allays";
    @Override
    public void onInitialize() {
        registerModComponents();
    }
}
