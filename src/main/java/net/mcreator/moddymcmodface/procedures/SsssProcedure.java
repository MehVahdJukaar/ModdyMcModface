package net.mcreator.moddymcmodface.procedures;

import net.minecraftforge.fml.server.ServerLifecycleHooks;

import net.minecraft.util.text.StringTextComponent;
import net.minecraft.server.MinecraftServer;

import net.mcreator.moddymcmodface.ModdymcmodfaceModElements;

import java.util.Map;

@ModdymcmodfaceModElements.ModElement.Tag
public class SsssProcedure extends ModdymcmodfaceModElements.ModElement {
	public SsssProcedure(ModdymcmodfaceModElements instance) {
		super(instance, 166);
	}

	public static void executeProcedure(Map<String, Object> dependencies) {
		{
			MinecraftServer mcserv = ServerLifecycleHooks.getCurrentServer();
			if (mcserv != null)
				mcserv.getPlayerList().sendMessage(new StringTextComponent("Message"));
		}
	}
}
