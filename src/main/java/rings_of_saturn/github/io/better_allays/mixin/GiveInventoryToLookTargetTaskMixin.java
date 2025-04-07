package rings_of_saturn.github.io.better_allays.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.LookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.GiveInventoryToLookTargetTask;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.passive.AllayBrain;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Function;

import static net.minecraft.entity.ai.brain.task.GiveInventoryToLookTargetTask.playThrowSound;

@Mixin(GiveInventoryToLookTargetTask.class)
public abstract class GiveInventoryToLookTargetTaskMixin<E extends LivingEntity & InventoryOwner> {
    @Shadow @Final private Function<LivingEntity, Optional<LookTarget>> lookTargetFunction;

    @Shadow
    private static Vec3d offsetTarget(LookTarget target) {
        return target.getPos().add(0.0, 1.0, 0.0);
    }

    @Shadow protected abstract void triggerCriterion(LookTarget target, ItemStack stack, ServerPlayerEntity player);

    @Inject(at= @At("HEAD"), method = "keepRunning", cancellable = true)
    protected void keepRunning(ServerWorld world, E entity, long time, CallbackInfo ci) {
        Optional<LookTarget> optional = this.lookTargetFunction.apply(entity);
        if (optional.isPresent()) {
            LookTarget lookTarget = optional.get();
            double d = lookTarget.getPos().distanceTo(entity.getEyePos());
            if (d < 3.0) {
                for (int i = 0; i < entity.getInventory().size(); i++) {
                    if(!entity.getInventory().getStack(i).isEmpty()) {
                        ItemStack itemStack = entity.getInventory().removeStack(i, 1);
                        if (!itemStack.isEmpty()) {
                            if (offsetTarget(lookTarget) != null) {
                                playThrowSound(entity, itemStack, offsetTarget(lookTarget));
                            }
                            if (entity instanceof AllayEntity allayEntity) {
                                AllayBrain.getLikedPlayer(allayEntity).ifPresent((player) -> {
                                    this.triggerCriterion(lookTarget, itemStack, player);
                                });
                            }
                        }
                    }
                }
                entity.getBrain().remember(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, 60);
            }
        }
    }
}
