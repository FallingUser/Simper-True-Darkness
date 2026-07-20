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

package grondag.darkness;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class Darkness {

	public static final String MOD_ID = "SimperTrueDarkness";
	public static Logger LOG = LogManager.getLogger("SimperTrueDarkness");

	static double darkOverworldFogEffective;
	static double darkNetherFogEffective;
	static double darkEndFogEffective;

	static {
		try {
			DarknessInit.Config.options.darkness_factor_of_fog_in_nether = Mth.clamp(DarknessInit.Config.options.darkness_factor_of_fog_in_nether, 0.0, 1.0);
		} catch (final Exception e) {
			DarknessInit.Config.options.darkness_factor_of_fog_in_nether = 0.0;
			LOG.warn("[Darkness] Invalid DarknessInit.Configuration value for 'darkness_factor_of_fog_in_nether'. Using default value.");
		}
		try {
			DarknessInit.Config.options.darkness_factor_of_fog_in_the_end = Mth.clamp(DarknessInit.Config.options.darkness_factor_of_fog_in_the_end, 0.0, 1.0);
		} catch (final Exception e) {
			DarknessInit.Config.options.darkness_factor_of_fog_in_the_end = 0.0;
			LOG.warn("[Darkness] Invalid DarknessInit.Configuration value for 'darkness_factor_of_fog_in_the_end'. Using default value.");
		}
		try {
			DarknessInit.Config.options.darkness_factor_of_fog_in_overworld = Mth.clamp(DarknessInit.Config.options.darkness_factor_of_fog_in_overworld, 0.0, 1.0);
		} catch (final Exception e) {
			DarknessInit.Config.options.darkness_factor_of_fog_in_overworld = 0.0;
			LOG.warn("[Darkness] Invalid DarknessInit.Configuration value for 'darkness_factor_of_fog_in_overworld'. Using default value.");
		}
		try {
			DarknessInit.Config.options.block_light_factor = Mth.clamp(DarknessInit.Config.options.block_light_factor, 0.0f, 2^128);
		} catch (final Exception e) {
			DarknessInit.Config.options.darkness_factor_of_fog_in_overworld = 0.0;
			LOG.warn("[Darkness] Invalid DarknessInit.Configuration value for 'block_light_factor'. Using default value.");
		}
		try {
			DarknessInit.Config.options.sky_light_factor = Mth.clamp(DarknessInit.Config.options.sky_light_factor, 0.0f, 2^128);
		} catch (final Exception e) {
			DarknessInit.Config.options.darkness_factor_of_fog_in_overworld = 0.0;
			LOG.warn("[Darkness] Invalid DarknessInit.Configuration value for 'sky_light_factor'. Using default value.");
		}
		if (!Objects.equals(DarknessInit.Config.options.is_gamma_factor_as_multiple_or_exponent, "multiple") && !Objects.equals(DarknessInit.Config.options.is_gamma_factor_as_multiple_or_exponent, "exponent")) {
			DarknessInit.Config.options.is_gamma_factor_as_multiple_or_exponent = "exponent";
			LOG.warn("[Darkness] Invalid DarknessInit.Configuration value for 'is_gamma_factor_as_multiple_or_exponent'. Using default value.");
		}
		computeConfigValues();
	}

	private static void computeConfigValues() {
		darkOverworldFogEffective = DarknessInit.Config.options.fog_in_overworld_is_dark ? DarknessInit.Config.options.darkness_factor_of_fog_in_overworld : 1.0;
		darkNetherFogEffective = DarknessInit.Config.options.nether_is_dark ? DarknessInit.Config.options.darkness_factor_of_fog_in_nether : 1.0;
		darkEndFogEffective = DarknessInit.Config.options.end_is_dark ? DarknessInit.Config.options.darkness_factor_of_fog_in_the_end : 1.0;
	}

	public static double darkOverworldFog() {
		return darkOverworldFogEffective;
	}

	public static double darkNetherFog() {
		return darkNetherFogEffective;
	}

	public static double darkEndFog() {
		return darkEndFogEffective;
	}


	private static boolean isDark(Level world) {
		final ResourceKey<Level> dimType = world.dimension();

		if (dimType == Level.OVERWORLD) {
			return DarknessInit.Config.options.overworld_is_dark;
		} else if (dimType == Level.NETHER) {
			return DarknessInit.Config.options.nether_is_dark;
		} else if (dimType == Level.END) {
			return DarknessInit.Config.options.end_is_dark;
		} else if (world.dimensionType().hasSkyLight()) {
			return DarknessInit.Config.options.dark_is_default;
		} else {
			return !DarknessInit.Config.options.outdoor_place_in_dimension_with_sky_is_dark;
		}
	}

	private static float skyFactor(Level world) {
		if (!DarknessInit.Config.options.no_affecting_to_sky_light && isDark(world)) {
			if (world.dimensionType().hasSkyLight()) {
				final float angle = world.getTimeOfDay(0);
				final float oldWeight = Math.max(0, (Math.abs(angle - 0.5f) - 0.2f)) * 20;
				final float moon = DarknessInit.Config.options.moon_phase_is_no_influence_on_light ? 0 : world.getMoonBrightness();

				if (angle > 0.25f && angle < 0.75f) {
					return Mth.lerp(oldWeight * oldWeight * oldWeight, moon * moon, 1f);
				} else {
					return 1;
				}
			} else {
				return 0;
			}
		} else {
			return 1;
		}
	}

	public static boolean enabled = true;
	private static final float[][] LUMINANCE = new float[16][16];

	public static int darken(int c, int blockIndex, int skyIndex) {
		final float lTarget = LUMINANCE[blockIndex][skyIndex];
		final float r = (c & 0xFF) / 255f;
		final float g = ((c >> 8) & 0xFF) / 255f;
		final float b = ((c >> 16) & 0xFF) / 255f;
		final float l = luminance(r, g, b);
		float f;
		f = l > 0 ? Math.min(1, lTarget / l) : 0;

		final var i = 0xFF << 24 | Math.round(f * r * 255) | (Math.round(f * g * 255) << 8) | (Math.round(f * b * 255) << 16);
		return f == 1f ? c : i;
	}

	public static float luminance(float r, float g, float b) {
		return r * 0.2126f + g * 0.7152f + b * 0.0722f;
	}

	public static void updateLuminance(float tickDelta, Minecraft client, GameRenderer worldRenderer, float prevFlicker) {
		final ClientLevel world = client.level;

		if (world != null && client.player != null) {
            if (!isDark(world) || client.player.hasEffect(MobEffects.NIGHT_VISION) || (client.player.hasEffect(MobEffects.CONDUIT_POWER) && client.player.getWaterVision() > 0) || world.getSkyFlashTime() > 0) {
                enabled = false;
                return;
            } else {
				enabled = true;
			}

            final float dimSkyFactor = Darkness.skyFactor(world);
			final float ambient = world.getSkyDarken(1.0F);
			final DimensionType dim = world.dimensionType();
			final boolean blockAmbient = !Darkness.isDark(world);

			for (int skyIndex = 0; skyIndex < 16; ++skyIndex) {
				float skyFactor = 1f - skyIndex / 15f;
				skyFactor = DarknessInit.Config.options.sky_light_factor - skyFactor * skyFactor * skyFactor * skyFactor;
				skyFactor *= dimSkyFactor;

				float min = skyFactor * 0.05f;
				final float rawAmbient = ambient * skyFactor;
				final float minAmbient = rawAmbient * (1 - min) + min;
				final float skyBase = LightTexture.getBrightness(dim, skyIndex) * minAmbient;

				min = 0.35f * skyFactor;
				float skyRed = skyBase * (rawAmbient * (1 - min) + min);
				float skyGreen = skyRed;
				float skyBlue = skyBase;

				if (worldRenderer.getDarkenWorldAmount(tickDelta) > 0.0F) {
					final float skyDarkness = worldRenderer.getDarkenWorldAmount(tickDelta);
					skyRed = skyRed * (1.0F - skyDarkness) + skyRed * 0.7F * skyDarkness;
					skyGreen = skyGreen * (1.0F - skyDarkness) + skyGreen * 0.6F * skyDarkness;
					skyBlue = skyBlue * (1.0F - skyDarkness) + skyBlue * 0.6F * skyDarkness;
				}

				for (int blockIndex = 0; blockIndex < 16; ++blockIndex) {
					float blockFactor = 1f;

					if (!blockAmbient) {
						blockFactor = 1f - blockIndex / 15f;
						blockFactor = DarknessInit.Config.options.block_light_factor - blockFactor * blockFactor * blockFactor * blockFactor;
					}

					final float blockBase = blockFactor * LightTexture.getBrightness(dim, blockIndex) * (prevFlicker * 0.1F + 1.5F);
					min = 0.4f * blockFactor;
					final float blockGreen = blockBase * ((blockBase * (1 - min) + min) * (1 - min) + min);
					final float blockBlue = blockBase * (blockBase * blockBase * (1 - min) + min);

					float red = skyRed + blockBase;
					float green = skyGreen + blockGreen;
					float blue = skyBlue + blockBlue;

					final float f = Math.max(skyFactor, blockFactor);
					min = 0.03f * f;
					red = red * (0.99F - min) + min;
					green = green * (0.99F - min) + min;
					blue = blue * (0.99F - min) + min;

					if (world.dimension() == Level.END) {
						red = skyFactor * 0.22F + blockBase * 0.75f;
						green = skyFactor * 0.28F + blockGreen * 0.75f;
						blue = skyFactor * 0.25F + blockBlue * 0.75f;
					}

					if (red > 1.0F) {
						red = 1.0F;
					}

					if (green > 1.0F) {
						green = 1.0F;
					}

					if (blue > 1.0F) {
						blue = 1.0F;
					}

					float gamma = 0;
					if (Objects.equals(DarknessInit.Config.options.is_gamma_factor_as_multiple_or_exponent, "multiple")) {
						gamma = (float) (client.options.gamma().get().floatValue() * f * DarknessInit.Config.options.gamma_factor);
					} else if (Objects.equals(DarknessInit.Config.options.is_gamma_factor_as_multiple_or_exponent, "exponent")) {
						gamma = (float) (Math.pow(client.options.gamma().get().floatValue(), DarknessInit.Config.options.gamma_factor) * f);
					}
					float invRed = 1.0F - red;
					float invGreen = 1.0F - green;
					float invBlue = 1.0F - blue;
					invRed = 1.0F - invRed * invRed * invRed * invRed;
					invGreen = 1.0F - invGreen * invGreen * invGreen * invGreen;
					invBlue = 1.0F - invBlue * invBlue * invBlue * invBlue;
					red = red * (1.0F - gamma) + invRed * gamma;
					green = green * (1.0F - gamma) + invGreen * gamma;
					blue = blue * (1.0F - gamma) + invBlue * gamma;

					min = 0.03f * f;
					red = red * (0.99F - min) + min;
					green = green * (0.99F - min) + min;
					blue = blue * (0.99F - min) + min;

					if (red > 1.0F) {
						red = 1.0F;
					}

					if (green > 1.0F) {
						green = 1.0F;
					}

					if (blue > 1.0F) {
						blue = 1.0F;
					}

					if (red < 0.0F) {
						red = 0.0F;
					}

					if (green < 0.0F) {
						green = 0.0F;
					}

					if (blue < 0.0F) {
						blue = 0.0F;
					}

					LUMINANCE[blockIndex][skyIndex] = Darkness.luminance(red, green, blue);
				}
			}
		}
	}
}
