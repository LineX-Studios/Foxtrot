package com.linexstudios.foxtrot.mixins;

import com.linexstudios.foxtrot.Misc.RingHelper;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {

    @Inject(method = "onPlayerRightClick", at = @At("HEAD"), cancellable = true)
    public void onBlockPlace(EntityPlayerSP player, WorldClient worldIn, ItemStack itemStackIn, BlockPos pos, EnumFacing direction, Vec3 vec, CallbackInfoReturnable<Boolean> cir) {
        if (RingHelper.enabled && RingHelper.preventMisplace) {
            if (RingHelper.instance.shouldBlockPlacement(pos, direction)) {
                cir.setReturnValue(false); 
            }
        }
    }
}