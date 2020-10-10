package net.mcreator.moddymcmodface.procedures;

import net.minecraftforge.fml.server.ServerLifecycleHooks;

import net.minecraft.util.text.StringTextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.item.ItemStack;

import net.mcreator.moddymcmodface.block.FireflyJarBlock;
import net.mcreator.moddymcmodface.ModdymcmodfaceModElements;

import java.util.Map;

@ModdymcmodfaceModElements.ModElement.Tag
public class WqsProcedure extends ModdymcmodfaceModElements.ModElement {
	public WqsProcedure(ModdymcmodfaceModElements instance) {
		super(instance, 153);
	}

	public static void executeProcedure(Map<String, Object> dependencies) {
		((new ItemStack(FireflyJarBlock.block.getDefaultState().getBlock()))).setDamage((int) 0);
		{
			MinecraftServer mcserv = ServerLifecycleHooks.getCurrentServer();
			if (mcserv != null)
				mcserv.getPlayerList().sendMessage(new StringTextComponent("Message"));
		}
	}
}
