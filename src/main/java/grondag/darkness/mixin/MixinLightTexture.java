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

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import grondag.darkness.Darkness;
import grondag.darkness.LightmapAccess;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

@Mixin(LightTexture.class)
public class MixinLightTexture implements LightmapAccess {

	@Final
	@Shadow
	private GpuTexture texture;          // LightTexture use GpuTexture in 1.21.5

	@Shadow
	private float blockLightRedFlicker;

	@Shadow
	private boolean updateLightTexture;

	/**
	 * Put the RGBA format int（R | G<<8 | B<<16 | A<<24） to ABGR（A | B<<8 | G<<16 | R<<24）
	 */
	@Unique
    private static int rgbaToABGR(int rgba) {
		return (rgba & 0xFF000000)         // reserve Alpha
				| ((rgba & 0x00FF0000) >>> 8)   // B move to 8th bit - 15th bit
				| ((rgba & 0x0000FF00) << 8)    // G move to 16th bit - 23rd bit
				| (rgba & 0x000000FF);          // R reserve in lowest position
	}

	@Inject(method = "updateLightTexture", at = @At("RETURN"))
	private void onUpdateLightTextureReturn(float f, CallbackInfo ci) {
		if (!Darkness.enabled) return;

		GpuTexture tex = this.texture;
		GpuDevice device = RenderSystem.getDevice();
		CommandEncoder encoder = device.createCommandEncoder();

		int pixelCount = 16 * 16;
		int bufferSize = pixelCount * 4;  // RGBA8，4 bits / pixel

		// 1. Create a readable buffer of type PIXEL_PACK for reading from textures
		try (GpuBuffer buffer = device.createBuffer(
				() -> "LightTextureReadBuffer",  // test tag
				BufferType.PIXEL_PACK,
				BufferUsage.STATIC_READ,
				bufferSize
		)) {
			// Replicate the entire texture(mipmap 0，offest 0)
			encoder.copyTextureToBuffer(tex, buffer, 0, () -> {}, 0);

			// 2. Reads buffer data to ByteBuffer
			ByteBuffer byteBuf = encoder.readBuffer(buffer).data();
			IntBuffer intBuf = byteBuf.asIntBuffer();

			// 3. Create a NativeImage and fill it in ABGR format
			NativeImage image = new NativeImage(16, 16, false);
			for (int y = 0; y < 16; y++) {
				for (int x = 0; x < 16; x++) {
					int rgba = intBuf.get(y * 16 + x);
					int abgr = rgbaToABGR(rgba);
					image.setPixelABGR(x, y, abgr);
				}
			}

			// 4. Darkening (based on ABGR)
			for (int b = 0; b < 16; b++) {
				for (int s = 0; s < 16; s++) {
					int abgr = image.getPixel(b, s);
					abgr = Darkness.darken(abgr, b, s);
					image.setPixelABGR(b, s, abgr);
				}
			}
			// set to pure white(15, 15) forcefully
			image.setPixelABGR(15, 15, -1);

			// 5. Upload the modified texture
			encoder.writeToTexture(tex, image);
			image.close();
		}
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
