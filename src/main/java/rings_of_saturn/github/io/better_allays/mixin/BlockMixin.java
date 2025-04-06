package rings_of_saturn.github.io.better_allays.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AllayBrain;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rings_of_saturn.github.io.better_allays.item.component.ModComponents;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(at= @At("HEAD"), method = "onPlaced")
    private void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack, CallbackInfo ci){
        if(itemStack.getItem() == Items.NOTE_BLOCK && itemStack.getOrDefault(ModComponents.LINKED_ALLAY, null) != null) {
            AllayEntity allay = (AllayEntity) world.getEntity(itemStack.get(ModComponents.LINKED_ALLAY));
            if (allay != null) {
                AllayBrain.rememberNoteBlock(allay, pos);
            }
        }
    }

}
