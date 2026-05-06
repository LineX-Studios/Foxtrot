package com.linexstudios.foxtrot.mixins;

import net.minecraft.client.gui.GuiUtilRenderComponents;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GuiUtilRenderComponents.class)
public class MixinGuiUtilRenderComponents {
    // Left intentionally blank. Modifying local variables inside GuiUtilRenderComponents
    // causes NoClassDefFoundError due to aggressive SRG obfuscation on Forge 1.8.9.
    // Ranks.java natively intercepts Chat and Tab perfectly without this.
}