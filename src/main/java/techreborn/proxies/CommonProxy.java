/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017 TechReborn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package techreborn.proxies;

import com.elytradev.concrete.network.NetworkContext;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import techreborn.Core;
import techreborn.compat.ICompatModule;
import techreborn.lib.ModInfo;
import techreborn.packets.CableUpdatePacket;
import techreborn.packets.CableUpdateRequestPacket;
import techreborn.tiles.cable.grid.CableTickHandler;

public class CommonProxy implements ICompatModule {
	public static boolean isChiselAround;

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		isChiselAround = Loader.isModLoaded("chisel");
		if (isChiselAround) {
			Core.logHelper.info("Hello chisel, shiny things will be enabled in techreborn");
		}
		Core.network = NetworkContext.forChannel(ModInfo.MOD_ID);
		Core.network.register(CableUpdateRequestPacket.class);
		Core.network.register(CableUpdatePacket.class);
	}

	@Override
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new CableTickHandler());
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {

	}

	@Override
	public void serverStarting(FMLServerStartingEvent event) {

	}

	public void registerFluidBlockRendering(Block block, String name) {

	}

	public void registerCustomBlockStateLocation(Block block, String name) {

	}

	public void registerCustomBlockStateLocation(Block block, String name, boolean item) {

	}

	public void registerSubItemInventoryLocation(Item item, int meta, String location, String name) {

	}

	public void registerSubBlockInventoryLocation(Block block, int meta, String location, String name) {
		registerSubItemInventoryLocation(Item.getItemFromBlock(block), meta, location, name);
	}

	public boolean isCTMAvailable() {
		return false;
	}

	public String getUpgradeConfigText() {
		return "";
	}

}
