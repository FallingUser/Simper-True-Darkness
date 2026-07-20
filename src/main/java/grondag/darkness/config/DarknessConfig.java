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

package grondag.darkness.config;

import grondag.darkness.Darkness;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = Darkness.MOD_ID)
public class DarknessConfig implements ConfigData {

    public Option options = new Option();

    public static class Option {
        @TomlComment("\n#  Option \"overworld_is_dark\" decides whether Overworld is dark.\n#  Allowed value: true/false\n#  Default value: true\n")
        public boolean overworld_is_dark = true;
        @TomlComment("#  Option \"fog_in_overworld_is_dark\" lets you control whether the fog in the overworld is affected by the option 'darkness_factor_of_fog_in_overworld'.\n#  Allowed value: true/false\n#  Default value: true\n")
        public boolean fog_in_overworld_is_dark = true;
        @TomlComment("#  Option \"darkness_factor_of_fog_in_overworld\" can control level darkness of fog in overworld when option \"fog_in_overworld_is_dark\" is true. The Lower the option \"darkness_factor_of_fog_in_overworld\" value, the darker the fog.\n#  Allowed value: a number in [0, 1]\n#  Default value: 0\n")
        public double darkness_factor_of_fog_in_overworld = 0.0;
        @TomlComment("#  Option \"dark_is_default\" decides whether other dimension added by mods with sky is dark.\n#  Allowed value: true/false\n#  Default value: true\n")
        public boolean dark_is_default = true;
        @TomlComment("#  Option \"nether_is_dark\" decides whether Nether is dark.\n#  Allowed value: true/false\n#  Default value: true\n")
        public boolean nether_is_dark = true;
        @TomlComment("#  Option \"darkness_factor_of_fog_in_nether\" decides the level of darkness of the fog in Nether.\n#  Allowed value: a number in [0, 1]\n#  Default value: 0\n")
        public double darkness_factor_of_fog_in_nether = 0.0;
        @TomlComment("#  Option \"end_is_dark\" decides whether The End is dark.\n#  Allowed value: true/false\n#  Default value: true\n")
        public boolean end_is_dark = true;
        @TomlComment("#  Option \"darkness_factor_of_fog_in_the_end\" decides the level of darkness of the fog in The End.\n#  Allowed value: a number in [0, 1]\n#  Default value: 0\n")
        public double darkness_factor_of_fog_in_the_end = 0.0;
        @TomlComment("#  Option \"outdoor_place_in_dimension_with_sky_is_dark\" decides whether dimension added by mods without sky is dark.\n#  Allowed value: true/false\n#  Default value: true\n")
        public boolean outdoor_place_in_dimension_with_sky_is_dark = true;
        @TomlComment("#  Option \"no_affecting_to_sky_light\" decides whether affect block light only.\n#  Allowed value: true/false\n#  Default value: true\n")
        public boolean no_affecting_to_sky_light = false;
        @TomlComment("#  Option \"moon_phase_is_no_influence_on_light\" decides whether influence of phase of the moon on light is ignored.\n#  Allowed value: true/false\n#  Default value: true\n")
        public boolean moon_phase_is_no_influence_on_light = true;
        @TomlComment("#  Option \"refuse_loading_mod_menu\" makes you cannot play in world with mod Mod Menu.\n#  Allowed value: true/false\n#  Default value: true\n")
        public boolean refuse_loading_mod_menu = false;
        @TomlComment("#  Option \"gamma_factor\" can control the importance of the gamma option.\n#  Allowed value: [-2^(2^10), 2^(2^10)]\n#  Default value: 0\n")
        public double gamma_factor = 0.0;
        @TomlComment("#  Option \"is_gamma_factor_as_multiple_or_exponent\" decide how option \"gamma_factor\" work.\n#  Allowed value: \"multiple\"/\"exponent\"\n#  Default value: \"exponent\"\n")
        public String is_gamma_factor_as_multiple_or_exponent = "exponent";
        @TomlComment("#  Option \"block_light_factor\" can control the brightness of block lights. The Lower the option \"block_light_factor\" value, the darker the block lights.\n#  Allowed value: [0, 2^(2^7)]\n#  Default value: 0.25\n")
        public float block_light_factor = 0.25F;
        @TomlComment("#  Option \"sky_light_factor\" can control the brightness of block lights. The Lower the option \"sky_light_factor\" value, the darker sky lights.\n#  Allowed value: [0, 2^(2^7)]\n#  Default value: 1\n")
        public float sky_light_factor = 1F;
    }
}
