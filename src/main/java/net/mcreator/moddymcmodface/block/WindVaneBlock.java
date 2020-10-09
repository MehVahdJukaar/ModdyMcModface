
package net.mcreator.moddymcmodface.block;

import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.World;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Direction;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.StateContainer;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.BooleanProperty;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.block.material.PushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;

import net.mcreator.moddymcmodface.ModdymcmodfaceModElements;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.color.IBlockColor;
import javax.annotation.Nullable;
import net.minecraft.world.ILightReader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

@ModdymcmodfaceModElements.ModElement.Tag
public class WindVaneBlock extends ModdymcmodfaceModElements.ModElement {
	@ObjectHolder("moddymcmodface:wind_vane")
	public static final Block block = null;
	@ObjectHolder("moddymcmodface:wind_vane")
	public static final TileEntityType<CustomTileEntity> tileEntityType = null;
	public WindVaneBlock(ModdymcmodfaceModElements instance) {
		super(instance, 79);
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new CustomBlock());
		elements.items.add(() -> new BlockItem(block, new Item.Properties().group(ItemGroup.REDSTONE)).setRegistryName(block.getRegistryName()));
	}

	@SubscribeEvent
	public void registerTileEntity(RegistryEvent.Register<TileEntityType<?>> event) {
		event.getRegistry().register(TileEntityType.Builder.create(CustomTileEntity::new, block).build(null).setRegistryName("wind_vane"));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientLoad(FMLClientSetupEvent event) {
		RenderTypeLookup.setRenderLayer(block, RenderType.getCutout());
		ClientRegistry.bindTileEntityRenderer(tileEntityType, CustomRender::new);
	}
	public static class CustomBlock extends Block {
		public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED; // is it rooster only?
		public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;
		public CustomBlock() {
			super(Block.Properties.create(Material.IRON).sound(SoundType.METAL).hardnessAndResistance(5f, 6f).lightValue(0).harvestLevel(2)
					.harvestTool(ToolType.PICKAXE).notSolid());
			setRegistryName("wind_vane");
			this.setDefaultState(this.stateContainer.getBaseState().with(INVERTED, false).with(POWER, Integer.valueOf(0)));
		}

		public static int getPower(BlockState blockstate) {
			return blockstate.get(POWER);
		}

		public static void updatePower(BlockState bs, World world, BlockPos pos) {
			int weather = 0;
			if (world.getWorld().isThundering()) {
				weather = 2;
			} else if (world.getWorld().isRaining()) {
				weather = 1;
			}
			if (weather != bs.get(POWER)) {
				world.setBlockState(pos, bs.with(POWER, weather), 3);
			}
		}

		@Override
		public boolean hasComparatorInputOverride(BlockState state) {
			return true;
		}

		public boolean canProvidePower(BlockState state) {
			return true;
		}

		@Override
		public int getComparatorInputOverride(BlockState blockState, World world, BlockPos pos) {
			return blockState.get(POWER);
		}

		@Override
		protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
			builder.add(POWER);
			builder.add(INVERTED);
		}

		@Override
		public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
			return false;
		}

		@Override
		public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
			return true;
		}

		@Override
		public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
			return VoxelShapes.create(0.125D, 0D, 0.125D, 0.875D, 1D, 0.875D);
		}

		/*
		 * @Override public PathNodeType getAiPathNodeType(BlockState state,
		 * IBlockReader world, BlockPos pos, MobEntity entity) { return
		 * PathNodeType.WALKABLE; }
		 */
		@Override
		public PushReaction getPushReaction(BlockState state) {
			return PushReaction.BLOCK;
		}

		@Override
		public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
			return blockState.get(POWER);
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
			TileEntity tileentity = world.getTileEntity(pos);
			return tileentity == null ? false : tileentity.receiveClientEvent(eventID, eventParam);
		}
	}

	public static class CustomTileEntity extends TileEntity implements ITickableTileEntity {
		private float yaw = 0;
		private float prevYaw = 0;
		private float power = 0;
		protected CustomTileEntity() {
			super(tileEntityType);
		}

		public float getYaw() {
			return this.yaw;
		}

		public float getPrevYaw() {
			return this.prevYaw;
		}

		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);
		}

		@Override
		public CompoundNBT write(CompoundNBT compound) {
			super.write(compound);
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

		@Override
		public void tick() {
			float currentyaw = this.yaw;
			this.prevYaw = currentyaw;
			if (this.world != null && this.world.getGameTime() % 20L == 0L) {
				BlockState blockstate = this.getBlockState();
				if (!this.world.isRemote) {
					Block block = blockstate.getBlock();
					if (block instanceof WindVaneBlock.CustomBlock) {
						CustomBlock.updatePower(blockstate, this.world, this.pos);
					}
				} else {
					float p = CustomBlock.getPower(blockstate);
					this.power = p;
				}
			}
			float i = this.getWorld().getGameTime();
			float b = this.power * 2;
			float newyaw = 30 * MathHelper.sin(i * (1 + b) / 60) + 10 * MathHelper.sin(i * (1 + b) / 20);
			this.yaw = MathHelper.clamp(newyaw, currentyaw - 8, currentyaw + 8);
		};
	}

	public static class CustomRender extends TileEntityRenderer<CustomTileEntity> {
		private static final ResourceLocation texture = new ResourceLocation("moddymcmodface:textures/pistonlauncherentity.png");
		public CustomRender(TileEntityRendererDispatcher rendererDispatcherIn) {
			super(rendererDispatcherIn);
		}

		@Override
		public void render(CustomTileEntity entityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
				int combinedOverlayIn) {

			matrixStackIn.push();
			matrixStackIn.translate(0.5, 0.5, 0.5);
			matrixStackIn.rotate(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, entityIn.getPrevYaw(), entityIn.getYaw())));
			matrixStackIn.translate(-0.5, -0.5, -0.5);
			BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();

			BlockState state = WindVaneBlock.block.getDefaultState().with(BlockStateProperties.INVERTED, true);
			blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
			matrixStackIn.pop();
		}

		public ResourceLocation getEntityTexture(CustomTileEntity entity) {
			return texture;
		}
	}







}
