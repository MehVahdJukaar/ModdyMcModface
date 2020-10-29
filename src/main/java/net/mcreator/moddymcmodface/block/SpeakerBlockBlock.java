
package net.mcreator.moddymcmodface.block;

import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.RegistryEvent;

import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Rotation;
import net.minecraft.util.Mirror;
import net.minecraft.util.Direction;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.StateContainer;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.BooleanProperty;
import net.minecraft.server.MinecraftServer;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.BlockItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.command.ICommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;

import net.mcreator.moddymcmodface.gui.EditSpeakerBlockGui;
import net.mcreator.moddymcmodface.ModdymcmodfaceModElements;
import net.mcreator.moddymcmodface.ModdymcmodfaceMod;

import java.util.List;
import java.util.Collections;

import com.mojang.text2speech.Narrator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLanguageProvider;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.network.NetworkEvent;
import java.util.function.Supplier;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraft.server.management.PlayerList;
import net.minecraft.client.Minecraft;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.particles.ParticleType;
import net.minecraft.particles.ParticleTypes;

@ModdymcmodfaceModElements.ModElement.Tag
public class SpeakerBlockBlock extends ModdymcmodfaceModElements.ModElement {
	@ObjectHolder("moddymcmodface:speaker_block")
	public static final Block block = null;
	@ObjectHolder("moddymcmodface:speaker_block")
	public static final TileEntityType<CustomTileEntity> tileEntityType = null;
	public SpeakerBlockBlock(ModdymcmodfaceModElements instance) {
		super(instance, 175);

		elements.addNetworkMessage(packetSendSpeakerBlockMessage.class, packetSendSpeakerBlockMessage::buffer, packetSendSpeakerBlockMessage::new,
				packetSendSpeakerBlockMessage::handler);

		
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new CustomBlock());
		elements.items.add(() -> new BlockItem(block, new Item.Properties().group(ItemGroup.REDSTONE)).setRegistryName(block.getRegistryName()));
	}

	@SubscribeEvent
	public void registerTileEntity(RegistryEvent.Register<TileEntityType<?>> event) {
		event.getRegistry().register(TileEntityType.Builder.create(CustomTileEntity::new, block).build(null).setRegistryName("speaker_block"));
	}
	
	public static class CustomBlock extends Block {
		public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
		public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
		public CustomBlock() {
			super(Block.Properties.create(Material.WOOD).sound(SoundType.WOOD).hardnessAndResistance(1f, 2f).lightValue(0));
			this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(POWERED, false));
			setRegistryName("speaker_block");
		}

		@Override
		protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
			builder.add(FACING, POWERED);
		}

		public BlockState rotate(BlockState state, Rotation rot) {
			return state.with(FACING, rot.rotate(state.get(FACING)));
		}

		public BlockState mirror(BlockState state, Mirror mirrorIn) {
			return state.rotate(mirrorIn.toRotation(state.get(FACING)));
		}

		@Override
		public BlockState getStateForPlacement(BlockItemUseContext context) {
			return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
		}


		@Override
		public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
			List<ItemStack> dropsOriginal = super.getDrops(state, builder);
			if (!dropsOriginal.isEmpty())
				return dropsOriginal;
			return Collections.singletonList(new ItemStack(this, 1));
		}

		@Override
		public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
			this.updatePower(state, worldIn, pos);
		}

		@Override
		public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
			super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
			this.updatePower(state, world, pos);
		}

		public void updatePower(BlockState state, World world, BlockPos pos) {
		
			if(!world.isRemote()){

				boolean pow = world.isBlockPowered(pos);
				//state changed
				if (pow != state.get(POWERED)) {
					world.setBlockState(pos, state.with(POWERED, Boolean.valueOf(pow)), 3);
					//can I emit sound?
					Direction facing  = state.get(FACING);
					if (pow && world.isAirBlock(pos.offset(facing))){
						
						TileEntity tileentity = world.getTileEntity(pos);
						if(tileentity instanceof CustomTileEntity){
							CustomTileEntity speaker = (CustomTileEntity) tileentity;
		
							MinecraftServer mcserv = ServerLifecycleHooks.getCurrentServer();
							DimensionType dimension = world.getDimension().getType();
							if (mcserv != null && dimension != null && speaker.message != ""){

								//particle
								world.addBlockEvent(pos, this, 0, 0);
								
								PlayerList players = mcserv.getPlayerList();
								players.sendToAllNearExcept((PlayerEntity) null, pos.getX(), pos.getY(), pos.getZ(),
									64, dimension, ModdymcmodfaceMod.PACKET_HANDLER.toVanillaPacket(
									new packetSendSpeakerBlockMessage(speaker.message, speaker.narrator), NetworkDirection.PLAY_TO_CLIENT));
		
							}
						}
					}
				}
			
			}
		}


		@Override
		public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity entity, Hand hand,
				BlockRayTraceResult hit) {
			TileEntity tileentity = world.getTileEntity(pos);
			if (tileentity instanceof CustomTileEntity  && entity instanceof PlayerEntity) {
				//client
				if(world.isRemote) EditSpeakerBlockGui.GuiWindow.open((CustomTileEntity)tileentity);
				return ActionResultType.SUCCESS;
			}
			return ActionResultType.PASS;
		}

		@Override
		public INamedContainerProvider getContainer(BlockState state, World worldIn, BlockPos pos) {
			TileEntity tileEntity = worldIn.getTileEntity(pos);
			return tileEntity instanceof INamedContainerProvider ? (INamedContainerProvider) tileEntity : null;
		}

		@Override
		public boolean hasTileEntity(BlockState state) {
			return true;
		}

		@Override
		public TileEntity createTileEntity(BlockState state, IBlockReader world) {
			return new CustomTileEntity();
		}

		@Override
		public boolean eventReceived(BlockState state, World world, BlockPos pos, int eventID, int eventParam) {
			super.eventReceived(state, world, pos, eventID, eventParam);

			Direction facing = state.get(FACING);

			world.addParticle(ParticleTypes.NOTE, pos.getX() + 0.5 + facing.getXOffset()*0.575,
				pos.getY() + 0.5, pos.getZ() + 0.5 + facing.getZOffset()*0.575,
				(double)world.rand.nextInt(24)/ 24.0D, 0.0D, 0.0D);
			return true;
		}
	}

	public static class CustomTileEntity extends TileEntity {
		public String message = "";
		public boolean narrator = false;
		protected CustomTileEntity() {
			super(tileEntityType);
		}

		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);
			this.message = compound.getString("Message");
			this.narrator = compound.getBoolean("Narrator");
		}

		@Override
		public CompoundNBT write(CompoundNBT compound) {
			super.write(compound);
			compound.putString("Message", this.message);
			compound.putBoolean("Narrator", this.narrator);
			return compound;
		}

		@Override
		public SUpdateTileEntityPacket getUpdatePacket() {
			return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
		}

		@Override
		public CompoundNBT getUpdateTag() {
			return this.write(new CompoundNBT());
		}

		@Override
		public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
			this.read(pkt.getNbtCompound());
		}
	}


	public static class packetSendSpeakerBlockMessage{
		private ITextComponent str;
		private boolean narrator;
		
		public packetSendSpeakerBlockMessage(PacketBuffer buf) {
			this.str = buf.readTextComponent();
			this.narrator = buf.readBoolean();
		}

		public packetSendSpeakerBlockMessage(String str, boolean narrator) {
			this.str = new StringTextComponent(str);
			this.narrator = narrator;
		}

		public static void buffer(packetSendSpeakerBlockMessage message, PacketBuffer buf) {

			buf.writeTextComponent(message.str);
			buf.writeBoolean(message.narrator);
		}
		
		public static void handler(packetSendSpeakerBlockMessage message, Supplier<NetworkEvent.Context> ctx) {
			// client world

			ctx.get().enqueueWork(() -> {

				//PlayerEntity player = ctx.get().getSender();
				if(message.narrator){
					Narrator.getNarrator().say(message.str.getString(), true);
				}
				else{
					Minecraft.getInstance().player.sendMessage(new StringTextComponent("[Speaker Block] "+message.str.getString()));
				}
				
			});
			ctx.get().setPacketHandled(true);
		}
	}
	
}
