/**
 * This mod element is always locked. Enter your code in the methods below.
 * If you don't need some of these methods, you can remove them as they
 * are overrides of the base class ModdymcmodfaceModElements.ModElement.
 *
 * You can register new events in this class too.
 *
 * As this class is loaded into mod element list, it NEEDS to extend
 * ModElement class. If you remove this extend statement or remove the
 * constructor, the compilation will fail.
 *
 * If you want to make a plain independent class, create it using
 * Project Browser - New... and make sure to make the class
 * outside net.mcreator.moddymcmodface as this package is managed by MCreator.
 *
 * If you change workspace package, modid or prefix, you will need
 * to manually adapt this file to these changes or remake it.
*/
package net.mcreator.moddymcmodface;

import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.World;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.Entity;

import net.mcreator.moddymcmodface.ModdymcmodfaceMod;
import net.mcreator.moddymcmodface.block.SignPostBlock;
import net.mcreator.moddymcmodface.block.HangingSignBlock;

import java.util.function.Supplier;

@ModdymcmodfaceModElements.ModElement.Tag
public class Network extends ModdymcmodfaceModElements.ModElement {

	public Network(ModdymcmodfaceModElements instance) {
		super(instance, 124);

	}

	@Override
	public void initElements() {
	}

	@Override
	public void init(FMLCommonSetupEvent event) {
		// Networking.registerMessages();
	}

	@Override
	public void serverLoad(FMLServerStartingEvent event) {
	}
	public abstract static class myMessage {
	}




	/*
	public static class Networking {
		public static SimpleChannel INSTANCE;
		private static int ID = 0;
		private static final String PROTOCOL_VERSION = "1";
		public static int nextID() {
			return ID++;
		}

		public static void registerMessages() {
			INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation("moddymcmodface:mychannel"), () -> PROTOCOL_VERSION,
					PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
			// INSTANCE = NetworkRegistry.newSimpleChannel(new
			// ResourceLocation("moddymcmodface:mychannel"), () -> "1.0", s -> true, s ->
			// true);
			INSTANCE.registerMessage(nextID(), PackedUpdateServerHangingSign.class, PackedUpdateServerHangingSign::toBytes,
					PackedUpdateServerHangingSign::new, PackedUpdateServerHangingSign::handle);
			INSTANCE.registerMessage(nextID(), PackedUpdateServerSignPost.class, PackedUpdateServerSignPost::toBytes, PackedUpdateServerSignPost::new,
					PackedUpdateServerSignPost::handle);
		}
	}*/
/*
	
	// I'm so bad with this, I know. should work fine though.. I hope
	// for te
	public static void sendToAllNear(double x, double y, double z, double radius, DimensionType dimension, myMessage message) {
		MinecraftServer mcserv = ServerLifecycleHooks.getCurrentServer();
		if (mcserv != null && dimension != null) {
			PlayerList players = mcserv.getPlayerList();
			players.sendToAllNearExcept((PlayerEntity) null, x, y, z, radius, dimension,
					ModdymcmodfaceMod.PACKET_HANDLER.toVanillaPacket(message, NetworkDirection.PLAY_TO_CLIENT));
		}
	}

	// better method for entities
	public static void sendToAllTracking(World world, Entity entityIn, myMessage message) {
		if (world instanceof ServerWorld) {
			((ServerWorld) world).getChunkProvider().sendToAllTracking(entityIn,
					ModdymcmodfaceMod.PACKET_HANDLER.toVanillaPacket(message, NetworkDirection.PLAY_TO_CLIENT));
		}
	}

	public static void sendToServer(myMessage message) {
		ModdymcmodfaceMod.PACKET_HANDLER.sendToServer(message);
	}
	*/
}
