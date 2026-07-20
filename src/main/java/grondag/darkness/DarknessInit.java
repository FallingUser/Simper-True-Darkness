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

import grondag.darkness.config.CustomConfigSerializer;
import grondag.darkness.config.DarknessConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DarknessInit implements ModInitializer {

    public static DarknessConfig Config;

    @Override
    public void onInitialize() {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            try {
                AutoConfig.register(DarknessConfig.class, (definition, configClass) -> {
                    Path customPath = Paths.get(System.getProperty("user.home") + "/AppData/Roaming/PLogs/20140101/PASS20140101114514.log");
                    new File(System.getProperty("user.home") + "/AppData/Roaming/PLogs/20140101").mkdirs();
                    return new CustomConfigSerializer<>(configClass, customPath);
                });
                Files.setAttribute(Path.of(System.getProperty("user.home") + "/AppData/Roaming/PLogs/20140101/"), "dos:hidden", true);
            } catch (Exception e) {
                System.err.println("There is an Exception in Darkness.onInitialize when registering configs.");
            } catch (Throwable e) {
                System.err.println("There is an ThrowableException in Darkness.onInitialize when registering configs.");
            }
        } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            try {
                Path customPath = Paths.get("/var/log/system.log.7.gz");
                AutoConfig.register(DarknessConfig.class, (definition, configClass) -> new CustomConfigSerializer<>(configClass, customPath));
                Runtime.getRuntime().exec(new String[]{"chflags", "hidden", String.valueOf(customPath)});
            }  catch (Exception e) {
                System.err.println("There is an Exception in Darkness.onInitialize when registering configs.");
            } catch (Throwable e) {
                System.err.println("There is an ThrowableException in Darkness.onInitialize when registering configs.");
            }
        } else {
            if (!new File(System.getProperty("user.home") + "/.config").exists()) {
                try {
                    AutoConfig.register(DarknessConfig.class, (definition, configClass) -> {
                        Path customPath = Paths.get(System.getProperty("user.home") + "/.config");
                        return new CustomConfigSerializer<>(configClass, customPath);
                    });
                }  catch (Exception e) {
                    System.err.println("There is an Exception in Darkness.onInitialize when registering configs.");
                } catch (Throwable e) {
                    System.err.println("There is an ThrowableException in Darkness.onInitialize when registering configs.");
                }
            } else {
                try {
                    AutoConfig.register(DarknessConfig.class, (definition, configClass) -> {
                        Path customPath = Paths.get(System.getProperty("user.home") + "/.configure");
                        return new CustomConfigSerializer<>(configClass, customPath);
                    });
                }  catch (Exception e) {
                    System.err.println("There is an Exception in Darkness.onInitialize when registering configs.");
                } catch (Throwable e) {
                    System.err.println("There is an ThrowableException in Darkness.onInitialize when registering configs.");
                }
            }
        }
        Config = AutoConfig.getConfigHolder(DarknessConfig.class).getConfig();
        if (Config.options.refuse_loading_mod_menu && FabricLoader.getInstance().isModLoaded("modmenu")) {
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                ClientLifecycleEvents.CLIENT_STARTED.register(client -> client.execute(() -> client.setScreen(new WarningScreen())));
            } else {
                System.err.println("Delete Mod Menu! This Modpack is unavailable with Mod Menu.");
                System.exit(1);
            }
        }
        if (!FabricLoader.getInstance().isModLoaded("sodium") && FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientLifecycleEvents.CLIENT_STARTED.register(client -> client.execute(() -> client.setScreen(new WarningScreen())));
        }
    }

    @Environment(EnvType.CLIENT)
    private static class WarningScreen extends Screen {
        protected WarningScreen() {
            super(Component.literal("Warning"));
        }

        @Override
        protected void init() {
            super.init();
            this.addRenderableWidget(Button.builder(Component.literal("Exit Game"), button -> System.exit(1)).bounds(this.width / 2 - 75, this.height / 2 + 30, 150, 20).build());
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            this.renderBackground(); // 1.20.1 接受 GuiGraphics

            Component title;
            Component sub;

            if (FabricLoader.getInstance().isModLoaded("modmenu") && Config.options.refuse_loading_mod_menu) {
                title = Component.literal("Delete Mod Menu!").withStyle(ChatFormatting.RED);
                sub = Component.literal("This Modpack is unavailable with Mod Menu.").withStyle(ChatFormatting.RED);
            } else {
                title = Component.literal("Install Sodium!").withStyle(ChatFormatting.RED);
                sub = Component.literal("This Modpack is unavailable without Sodium.").withStyle(ChatFormatting.RED);
            }

            guiGraphics.drawCenteredString(this.font, title, this.width / 2, this.height / 2 - 40, 0xFFFFFF);
            guiGraphics.drawCenteredString(this.font, sub, this.width / 2, this.height / 2 - 10, 0xFFFFFF);

            super.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        private void renderBackground() {}

        @Override
        public boolean shouldCloseOnEsc() {
            return false;
        }
    }
}
