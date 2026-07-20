/*
 * This file is part of True Darkness and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.darkness.mixin;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import grondag.darkness.Darkness;
import grondag.darkness.LightmapAccess;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightTexture.class)
public class MixinLightTexture implements LightmapAccess {
	@Final
    @Shadow
	private TextureTarget target;
	@Shadow
	private float blockLightRedFlicker;
	@Shadow
	private boolean updateLightTexture;

	@Inject(method = "updateLightTexture", at = @At("RETURN"))
	private void onUpdateLightTextureReturn(CallbackInfo ci) {
		if (!Darkness.enabled) return;

		// bind texture of the target
		RenderSystem.bindTexture(target.getColorTextureId());

		// Create 16×16 NativeImage and download texture data
		NativeImage image = new NativeImage(16, 16, false);
		image.downloadTexture(0, false);   // level=0, 不强制Alpha

		// Traverse all pixels and make them darker
		for (int b = 0; b < 16; b++) {
			for (int s = 0; s < 16; s++) {
				// Catch ARGB and turn to ABGR
				int colorARGB = image.getPixel(b, s);
				int colorABGR = ARGB.toABGR(colorARGB);
				colorABGR = Darkness.darken(colorABGR, b, s);
				colorARGB = ARGB.fromABGR(colorABGR);
				image.setPixel(b, s, colorARGB);
			}
		}

		// Forcefully change to pure white
		image.setPixel(15, 15, -1);

		// Update pixel after changing（level=0, x=0, y=0, blur=false）
		image.upload(0, 0, 0, false);
		image.close();
	}

	@Override
	public float darkness_prevFlicker() {
		return blockLightRedFlicker;
	}

	@Override
	public boolean darkness_isDirty() {
		return updateLightTexture;
	}
}
