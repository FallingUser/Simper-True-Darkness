package grondag.darkness.mixin.accessor;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LightTexture.class)
public interface AccessLightTexture {
    @Accessor("lightPixels")
    NativeImage getLightPixels();
}
