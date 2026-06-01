package com.linexstudios.foxtrot.mixins;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(C17PacketCustomPayload.class)
public interface IAccessorC17PacketCustomPayload {

    @Accessor("data")
    void setData(PacketBuffer data);

    @Accessor("data")
    PacketBuffer getData();
}