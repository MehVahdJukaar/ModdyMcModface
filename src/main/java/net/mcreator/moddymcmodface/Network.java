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

import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.Minecraft;

import net.mcreator.moddymcmodface.block.NoticeBoardBlock;
import net.mcreator.moddymcmodface.block.PedestalBlock;
import net.mcreator.moddymcmodface.block.JarBlock;
import net.mcreator.moddymcmodface.block.HangingSignBlock;

import java.util.function.Supplier;
import net.minecraft.entity.Entity;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.block.Blocks;

@ModdymcmodfaceModElements.ModElement.Tag
public class Network extends ModdymcmodfaceModElements.ModElement {
	/**
	 * Do not remove this constructor
	 */
	public Network(ModdymcmodfaceModElements instance) {
		super(instance, 124);
	}

	@Override
	public void initElements() {
	}

	@Override
	public void init(FMLCommonSetupEvent event) {
		Networking.registerMessages();
	}

	@Override
	public void serverLoad(FMLServerStartingEvent event) {
	}




	public abstract static class myMessage {}


	public static class PackedUpdateServerHangingSign extends myMessage {
		private BlockPos pos;
		private ITextComponent t0;
		private ITextComponent t1;
		private ITextComponent t2;
		private ITextComponent t3;
		private ITextComponent t4;
		public PackedUpdateServerHangingSign(PacketBuffer buf) {
			this.pos = buf.readBlockPos();

			String s = buf.readString();
			ITextComponent itextcomponent = ITextComponent.Serializer.fromJson(s.isEmpty() ? "\"\"" : s);
			this.t0 = itextcomponent;
			s = buf.readString();
			itextcomponent = ITextComponent.Serializer.fromJson(s.isEmpty() ? "\"\"" : s);
			this.t1 = itextcomponent;
			s = buf.readString();
			itextcomponent = ITextComponent.Serializer.fromJson(s.isEmpty() ? "\"\"" : s);
			this.t2 = itextcomponent;
			s = buf.readString();
			itextcomponent = ITextComponent.Serializer.fromJson(s.isEmpty() ? "\"\"" : s);
			this.t3 = itextcomponent;
			s = buf.readString();
			itextcomponent = ITextComponent.Serializer.fromJson(s.isEmpty() ? "\"\"" : s);
			this.t4 = itextcomponent;			
		}
		public PackedUpdateServerHangingSign(BlockPos pos, ITextComponent t0, ITextComponent t1, ITextComponent t2, ITextComponent t3, ITextComponent t4) {
			this.pos = pos;
			this.t0 = t0;
			this.t1 = t1;
			this.t2 = t2;
			this.t3 = t3;
			this.t4 = t4;

		}
		public void toBytes(PacketBuffer buf) {
			buf.writeBlockPos(this.pos);

			String s = ITextComponent.Serializer.toJson(this.t0);
			buf.writeString(s);
			s = ITextComponent.Serializer.toJson(this.t1);
			buf.writeString(s);
			s = ITextComponent.Serializer.toJson(this.t2);
			buf.writeString(s);
			s = ITextComponent.Serializer.toJson(this.t3);
			buf.writeString(s);
			s = ITextComponent.Serializer.toJson(this.t4);
			buf.writeString(s);
			
		}
		public void handle(Supplier<NetworkEvent.Context> ctx) {
			//server world
			World world = ctx.get().getSender().world;

			ctx.get().enqueueWork(() -> {


				if (world != null) {
					TileEntity tileentity = world.getTileEntity(pos);
					if (tileentity instanceof HangingSignBlock.CustomTileEntity) {
						HangingSignBlock.CustomTileEntity sign = (HangingSignBlock.CustomTileEntity) tileentity;
						sign.setText(0, this.t0);
						sign.setText(1, this.t1);
						sign.setText(2, this.t2);
						sign.setText(3, this.t3);
						sign.setText(3, this.t4);
					}
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}


	public static class PacketUpdateNoticeBoard extends myMessage {
		private BlockPos pos;
		private ItemStack stack;
		public PacketUpdateNoticeBoard(PacketBuffer buf) {
			this.pos = buf.readBlockPos();
			this.stack = buf.readItemStack();
		}
		public PacketUpdateNoticeBoard(BlockPos pos, ItemStack stack) {
			this.pos = pos;
			this.stack = stack;
		}
		public void toBytes(PacketBuffer buf) {
			buf.writeBlockPos(this.pos);
			buf.writeItemStack(this.stack);
		}
		public void handle(Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				World world = Minecraft.getInstance().world;
				if (world != null) {
					TileEntity tileentity = world.getTileEntity(pos);
					if (tileentity instanceof NoticeBoardBlock.CustomTileEntity) {
						((NoticeBoardBlock.CustomTileEntity) tileentity).updateInventoryFromServer(this.stack);
					}
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}



	public static class PacketUpdatePedestal extends myMessage {
		private BlockPos pos;
		private ItemStack stack;
		public PacketUpdatePedestal(PacketBuffer buf) {
			this.pos = buf.readBlockPos();
			this.stack = buf.readItemStack();
		}
		public PacketUpdatePedestal(BlockPos pos, ItemStack stack) {
			this.pos = pos;
			this.stack = stack;
		}
		public void toBytes(PacketBuffer buf) {
			buf.writeBlockPos(this.pos);
			buf.writeItemStack(this.stack);
		}
		public void handle(Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				World world = Minecraft.getInstance().world;
				if (world != null) {
					TileEntity tileentity = world.getTileEntity(pos);
					if (tileentity instanceof PedestalBlock.CustomTileEntity) {
						((PedestalBlock.CustomTileEntity) tileentity).updateInventoryFromServer(this.stack);
					}
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}

	public static class PacketUpdateJar extends myMessage {
		private BlockPos pos;
		private ItemStack stack;
		public PacketUpdateJar(PacketBuffer buf) {
			this.pos = buf.readBlockPos();
			this.stack = buf.readItemStack();
		}
		public PacketUpdateJar(BlockPos pos, ItemStack stack) {
			this.pos = pos;
			this.stack = stack;
		}
		public void toBytes(PacketBuffer buf) {
			buf.writeBlockPos(this.pos);
			buf.writeItemStack(this.stack);
		}
		public void handle(Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				World world = Minecraft.getInstance().world;
				if (world != null) {
					TileEntity tileentity = world.getTileEntity(pos);
					if (tileentity instanceof JarBlock.CustomTileEntity) {
						((JarBlock.CustomTileEntity) tileentity).updateInventoryFromServer(this.stack);
					}
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}	





	

	public static class Networking {
		public static SimpleChannel INSTANCE;
		private static int ID = 0;
		private static final String PROTOCOL_VERSION = "1";
		public static int nextID() {
			return ID++;
		}

		public static void registerMessages() {
			

			INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation("moddymcmodface:mychannel"),
			() -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

			//INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation("moddymcmodface:mychannel"), () -> "1.0", s -> true, s -> true);
			INSTANCE.registerMessage(nextID(), 
					PacketUpdateNoticeBoard.class, 
					PacketUpdateNoticeBoard::toBytes, 
					PacketUpdateNoticeBoard::new,
					PacketUpdateNoticeBoard::handle);
			INSTANCE.registerMessage(nextID(), 
					PacketUpdatePedestal.class, 
					PacketUpdatePedestal::toBytes, 
					PacketUpdatePedestal::new,
					PacketUpdatePedestal::handle);
			INSTANCE.registerMessage(nextID(), 
					PacketUpdateJar.class, 
					PacketUpdateJar::toBytes, 
					PacketUpdateJar::new,
					PacketUpdateJar::handle);
			INSTANCE.registerMessage(nextID(), 
					PackedUpdateServerHangingSign.class, 
					PackedUpdateServerHangingSign::toBytes, 
					PackedUpdateServerHangingSign::new,
					PackedUpdateServerHangingSign::handle);

		}
	}
	// I'm so bad with this, I know. should work fine though.. I hope
	//for te
	public static void sendToAllNear(double x, double y, double z, double radius, DimensionType dimension, myMessage message) {
		MinecraftServer mcserv = ServerLifecycleHooks.getCurrentServer();
		if (mcserv != null && dimension != null) {
			PlayerList players = mcserv.getPlayerList();
			players.sendToAllNearExcept((PlayerEntity) null, x, y, z, radius, dimension,
					Networking.INSTANCE.toVanillaPacket(message, NetworkDirection.PLAY_TO_CLIENT));
		}
	}
	
	//better method for entities
	public static void sendToAllTracking(World world, Entity entityIn, myMessage message){
		if (world instanceof ServerWorld){
		((ServerWorld)world).getChunkProvider().sendToAllTracking(entityIn, Networking.INSTANCE.toVanillaPacket(message, NetworkDirection.PLAY_TO_CLIENT));
		}

	}

	public static void sendToServer(myMessage message){
		Networking.INSTANCE.sendToServer(message);
	}
	
	
}
