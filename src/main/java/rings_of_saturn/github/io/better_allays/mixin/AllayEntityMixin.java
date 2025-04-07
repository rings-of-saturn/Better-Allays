package rings_of_saturn.github.io.better_allays.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AllayBrain;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rings_of_saturn.github.io.better_allays.item.component.ModComponents;

import java.util.Iterator;
import java.util.Objects;

@Mixin(AllayEntity.class)
public abstract class AllayEntityMixin {
    @Shadow @Final private SimpleInventory inventory = new SimpleInventory(65);

    @Shadow @Final private boolean areItemsEqual(ItemStack stack, ItemStack stack2) {
        return ItemStack.areItemsEqual(stack, stack2) && !this.areDifferentPotions(stack, stack2);
    }

    @Shadow
    private boolean areDifferentPotions(ItemStack stack, ItemStack stack2) {
        PotionContentsComponent potionContentsComponent = stack.get(DataComponentTypes.POTION_CONTENTS);
        PotionContentsComponent potionContentsComponent2 = stack2.get(DataComponentTypes.POTION_CONTENTS);
        return !Objects.equals(potionContentsComponent, potionContentsComponent2);
    }

    @Unique
    private final MobEntity thisAsEntity = (MobEntity)(Object)this;

    @Inject(at= @At("HEAD"), method = "canGather", cancellable = true)
    public void checkWithBundle(ServerWorld world, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        ItemStack handStack = thisAsEntity.getStackInHand(Hand.MAIN_HAND);
        if(handStack.getItem() == Items.BUNDLE){
            cir.cancel();
            BundleContentsComponent bundleContents = handStack.getOrDefault(DataComponentTypes.BUNDLE_CONTENTS, null);
            if(bundleContents != null){
                for (int i = 0; i < bundleContents.size(); i++) {
                    if(!stack.isEmpty() && world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING) && this.inventory.canInsert(stack) && this.areItemsEqual(bundleContents.get(i), stack)) {
                        cir.setReturnValue(true);
                        break;
                    }
                }
            }
        }
    }

    @Inject(at= @At("HEAD"), method = "interactMob", cancellable = true)
    private void setNoteBlockStack(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir){

        var playerStack = player.getStackInHand(hand);
        if(playerStack.getItem() == Items.NOTE_BLOCK){
            cir.cancel();
            playerStack.set(ModComponents.LINKED_ALLAY, thisAsEntity.getUuid());
            playerStack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
            cir.setReturnValue(ActionResult.PASS);
        }

    }

    @Inject(at= @At("HEAD"), method = "tick")
    private void removeAmnesia(CallbackInfo ci){
        if(thisAsEntity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.LIKED_NOTEBLOCK).isPresent())
            AllayBrain.rememberNoteBlock(thisAsEntity, thisAsEntity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.LIKED_NOTEBLOCK).get().pos());
    }

    @Mixin(AllayEntity.VibrationCallback.class)
    public static class VibrationMixin{
        @Inject(at= @At("HEAD"), method = "accept", cancellable = true)
        private void addAmnesia(ServerWorld world, BlockPos pos, RegistryEntry<GameEvent> event, Entity sourceEntity, Entity entity, float distance, CallbackInfo ci){
            ci.cancel();
            if (event.matches(GameEvent.NOTE_BLOCK_PLAY)) {
                if(entity != null) {
                    AllayEntity allay = (AllayEntity) world.getEntity(entity.getUuid());
                    if (allay.getBrain().getOptionalRegisteredMemory(MemoryModuleType.LIKED_NOTEBLOCK).get().pos() == pos) {
                        AllayBrain.rememberNoteBlock(allay, new BlockPos(pos));
                    }
                }
            }
        }
    }
}
