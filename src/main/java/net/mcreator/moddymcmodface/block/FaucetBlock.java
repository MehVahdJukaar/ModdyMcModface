
package net.mcreator.moddymcmodface.block;

import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.World;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Rotation;
import net.minecraft.util.Mirror;
import net.minecraft.util.Hand;
import net.minecraft.util.Direction;
import net.minecraft.util.ActionResultType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.StateContainer;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.BooleanProperty;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.BlockItem;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;
import net.minecraft.block.CauldronBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;

import net.mcreator.moddymcmodface.ModdymcmodfaceModElements;

import java.util.stream.IntStream;
import java.util.Random;
import java.util.List;
import java.util.Collections;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.tags.FluidTags;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.world.biome.BiomeColors;

@ModdymcmodfaceModElements.ModElement.Tag
public class FaucetBlock extends ModdymcmodfaceModElements.ModElement {
	public static final ResourceLocation texture = new ResourceLocation("moddymcmodface:blocks/faucet_water");
	
	@ObjectHolder("moddymcmodface:faucet")
	public static final Block block = null;
	@ObjectHolder("moddymcmodface:faucet")
	public static final TileEntityType<CustomTileEntity> tileEntityType = null;
	public FaucetBlock(ModdymcmodfaceModElements instance) {
		super(instance, 104);
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new CustomBlock());
		elements.items.add(() -> new BlockItem(block, new Item.Properties().group(ItemGroup.REDSTONE)).setRegistryName(block.getRegistryName()));
	}

	@SubscribeEvent
	public void registerTileEntity(RegistryEvent.Register<TileEntityType<?>> event) {
		event.getRegistry().register(TileEntityType.Builder.create(CustomTileEntity::new, block).build(null).setRegistryName("faucet"));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientLoad(FMLClientSetupEvent event) {
		RenderTypeLookup.setRenderLayer(block, RenderType.getCutoutMipped());
		ClientRegistry.bindTileEntityRenderer(tileEntityType, CustomRender::new);

	}
	public static class CustomBlock extends Block {
		public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
		public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;
		public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
		public static final BooleanProperty HAS_WATER = BooleanProperty.create("has_water");
		public CustomBlock() {
			super(Block.Properties.create(Material.IRON).sound(SoundType.METAL).hardnessAndResistance(3f, 4.8f).lightValue(0).notSolid());
			this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(ENABLED, false).with(POWERED, false)
					.with(HAS_WATER, false));
			setRegistryName("faucet");
		}

		@Override
		public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
			switch ((Direction) state.get(FACING)) {
				case UP :
				case DOWN :
				case NORTH :
				default :
					return VoxelShapes.create(0.6875D, 0.3125D, 1D, 0.3125D, 0.9375D, 0.312D);
				case SOUTH :
					return VoxelShapes.create(0.3125D, 0.3125D, 0D, 0.6875D, 0.9375D, 0.688D);
				case EAST :
					return VoxelShapes.create(0D, 0.3125D, 0.6875D, 0.688D, 0.9375D, 0.3125D);
				case WEST :
					return VoxelShapes.create(1D, 0.3125D, 0.3125D, 0.312D, 0.9375D, 0.6875D);
			}
		}

		@Override
		public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
				BlockRayTraceResult hit) { 
				
			float f = state.get(ENABLED) ? 0.6F : 0.5F;
			worldIn.playSound((PlayerEntity) null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.3F, f);
			this.updateBlock(state, worldIn, pos, true);
			return ActionResultType.SUCCESS;
		}

		@Override
		public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
			this.updateBlock(state, worldIn, pos, false);
		}

		@Override
		public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
			super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
			this.updateBlock(state, world, pos, false);
		}

		public void updateBlock(BlockState state, World world, BlockPos pos, boolean toggle) {
			boolean flag = world.getRedstonePowerFromNeighbors(pos) > 0;

			BlockPos backpos = pos.offset(state.get(FACING), -1);
			BlockState backblock = world.getBlockState(backpos);
						
			boolean flag2 = (world.getFluidState(backpos).isTagged(FluidTags.WATER)
					|| ((backblock.getBlock() instanceof CauldronBlock) && backblock.getComparatorInputOverride(world, backpos) > 0));

			//if update blockstate with powered, haswater and enabled
			if (flag != state.get(POWERED) || flag2 != state.get(HAS_WATER) || toggle) {
				world.setBlockState(pos, state.with(POWERED, flag).with(HAS_WATER, flag2).with(ENABLED, toggle ^ state.get(ENABLED)));
			}
		}

		public boolean isOpen(BlockState state) {
			return (state.get(BlockStateProperties.POWERED) ^ state.get(BlockStateProperties.ENABLED));
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
		protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
			builder.add(FACING, ENABLED, POWERED, HAS_WATER);
		}

		public BlockState rotate(BlockState state, Rotation rot) {
			return state.with(FACING, rot.rotate(state.get(FACING)));
		}

		public BlockState mirror(BlockState state, Mirror mirrorIn) {
			return state.rotate(mirrorIn.toRotation(state.get(FACING)));
		}

		@Override
		public BlockState getStateForPlacement(BlockItemUseContext context) {
			if (context.getFace() == Direction.UP || context.getFace() == Direction.DOWN)
				return this.getDefaultState().with(FACING, Direction.NORTH);
			return this.getDefaultState().with(FACING, context.getFace());
		}

		@Override
		public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
			List<ItemStack> dropsOriginal = super.getDrops(state, builder);
			if (!dropsOriginal.isEmpty())
				return dropsOriginal;
			return Collections.singletonList(new ItemStack(this, 1));
		}

		@OnlyIn(Dist.CLIENT)
		@Override
		public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
			super.animateTick(state, world, pos, random);
			if (this.isOpen(state) && state.get(HAS_WATER)) {
				int x = pos.getX();
				int y = pos.getY();
				int z = pos.getZ();
				for (int l = 0; l < 4; ++l) {
					double d0 = (x + 0.375 + 0.25 * random.nextFloat());
					double d1 = (y + 0.25 + 0 * random.nextFloat());//0.3125
					double d2 = (z + 0.375  + 0.25 * random.nextFloat());

					world.addParticle(ParticleTypes.FALLING_WATER, d0, d1, d2, 0, 0, 0);
					world.addParticle(ParticleTypes.DRIPPING_WATER, x + 0.5, y + 0.25, z + 0.5, 0, 0, 0);
				}
			}
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
		private int transferCooldown = 0;
		protected final Random rand = new Random();
		public int watercolor = 0x423cf7;

		protected CustomTileEntity() {
			super(tileEntityType);
			
		}

		private boolean isOnTransferCooldown() {
			return this.transferCooldown > 0;
		}

		@Override
		public void tick() {
			if (this.world != null && !this.world.isRemote) {
				if (this.isOnTransferCooldown()) {
					this.transferCooldown--;
				} else if (this.isOpen()) {
					boolean flag = pullItems();
					if (flag) {
						this.transferCooldown = 20;
					}
				}
			}
		}


		public boolean isOpen() {
			return (this.getBlockState().get(BlockStateProperties.POWERED) ^ this.getBlockState().get(BlockStateProperties.ENABLED));
		}
		
		public boolean hasWater(){
			return this.getBlockState().get(CustomBlock.HAS_WATER);
		}

		// hopper code
		private static boolean canExtractItemFromSlot(IInventory inventoryIn, ItemStack stack, int index, Direction side) {
			return !(inventoryIn instanceof ISidedInventory) || ((ISidedInventory) inventoryIn).canExtractItem(index, stack, side);
		}

		private boolean pullItemFromSlot(IInventory inventoryIn, int index, Direction direction) {
			ItemStack itemstack = inventoryIn.getStackInSlot(index);
			if (!itemstack.isEmpty() && canExtractItemFromSlot(inventoryIn, itemstack, index, direction)) {

				ItemStack it = (ItemStack) itemstack.copy();
				itemstack.shrink(1);
				if (itemstack.isEmpty()) {
		            inventoryIn.markDirty();
		         }
				inventoryIn.markDirty();
				it.setCount((int) 1);
				ItemEntity drop = new ItemEntity(this.world, this.pos.getX() + 0.5, this.pos.getY(), this.pos.getZ() + 0.5, it);
				drop.setMotion(new Vec3d(0, 0, 0));
				this.world.addEntity(drop);
				float f= (this.rand.nextFloat()-0.5f)/4f;
				this.world.playSound((PlayerEntity) null, this.pos, SoundEvents.ENTITY_CHICKEN_EGG, SoundCategory.BLOCKS, 0.3F, 0.5f+f);
	
				return true;
			}
			return false;
		}

		public boolean pullItems() {
			IInventory iinventory = getSourceInventory();
			if (iinventory != null) {
				Direction direction = this.getBlockState().get(HorizontalBlock.HORIZONTAL_FACING);
				return func_213972_a(iinventory, direction).anyMatch((p_213971_3_) -> {
					return pullItemFromSlot(iinventory, p_213971_3_, direction);
				});
			}
			return false;
		}

		public IInventory getSourceInventory() {
			BlockPos behind = this.pos.offset(this.getBlockState().get(HorizontalBlock.HORIZONTAL_FACING), -1);
			IInventory firstinv = HopperTileEntity.getInventoryAtPosition(this.getWorld(), behind);
			if (firstinv != null) {
				return firstinv;
			} else if (this.world.getBlockState(behind).getBlock().isNormalCube(this.world.getBlockState(behind), this.world, this.pos)) {
				return HopperTileEntity.getInventoryAtPosition(this.getWorld(),
						this.pos.offset(this.getBlockState().get(HorizontalBlock.HORIZONTAL_FACING), -2));
			} else
				return null;
		}

		private static IntStream func_213972_a(IInventory inv, Direction dir) {
			return inv instanceof ISidedInventory
					? IntStream.of(((ISidedInventory) inv).getSlotsForFace(dir))
					: IntStream.range(0, inv.getSizeInventory());
		}

		// end hopper code
		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);
			this.watercolor=compound.getInt("watercolor");
		}

		@Override
		public CompoundNBT write(CompoundNBT compound) {
			super.write(compound);this.watercolor = BiomeColors.getWaterColor(this.world, this.pos);
			compound.putInt("watercolor",this.watercolor);
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






	@OnlyIn(Dist.CLIENT)
	public static class CustomRender extends TileEntityRenderer<CustomTileEntity> {
		public CustomRender(TileEntityRendererDispatcher rendererDispatcherIn) {
			super(rendererDispatcherIn);
		}

		// shaded rectangle with wx = wz with texture flipped vertically. starts from
		// block 0,0,0

		@Override
		public void render(CustomTileEntity entityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
				int combinedOverlayIn) {
			if(entityIn.hasWater() && entityIn.isOpen()){
				 TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(texture);
				//TODO:remove breaking animation
				IVertexBuilder builder = bufferIn.getBuffer(RenderType.getTranslucent());
				int color =entityIn.watercolor;
				float opacity =0.75f;
				float height = 1;
				matrixStackIn.push();
				matrixStackIn.translate(0.5, 0.5, 0.5);
				matrixStackIn.rotate(Vector3f.XP.rotationDegrees(180));
				matrixStackIn.translate(-0.125, 0.1875, -0.125);
				if (height != 0) {
					addCube(builder, matrixStackIn, 0.25f, height, sprite, combinedLightIn, color, opacity, combinedOverlayIn, true);
				}
				matrixStackIn.pop();
			}
		}
	}







	private static void addCube(IVertexBuilder builder, MatrixStack matrixStackIn, float w, float h, TextureAtlasSprite sprite, int combinedLightIn,
			int color, float a, int combinedOverlayIn, boolean fakeshading) {
		int lu = combinedLightIn & '\uffff';
		int lv = combinedLightIn >> 16 & '\uffff'; // ok
		float minu = sprite.getMinU();
		float minv = sprite.getMinV();
		float maxu = (sprite.getMaxU() - minu) * w + minu;
		float maxv = (sprite.getMaxV() - minv) * h + minv;
		float maxv2 = (sprite.getMaxV() - minv) * w + minv;
		float r = (float) ((color >> 16 & 255)) / 255.0F;
		float g = (float) ((color >> 8 & 255)) / 255.0F;
		float b = (float) ((color >> 0 & 255)) / 255.0F;
		// float a = 1f;// ((color >> 24) & 0xFF) / 255f;
		// shading:
		float r8 = r;
		float g8 = g;
		float b8 = b;
		float r6 = r;
		float g6 = g;
		float b6 = b;
		float r5 = r;
		float g5 = g;
		float b5 = b;

		if(fakeshading){
			// 80%: s,n
			r8 *= 0.8f;
			g8 *= 0.8f;
			b8 *= 0.8f;
			// 60%: e,w
			r6 *= 0.6f;
			g6 *= 0.6f;
			b6 *= 0.6f;
			// 50%: d
			r5 *= 0.5f;
			g5 *= 0.5f;
			b5 *= 0.5f;
		}

		
		// r6=r;r8=r;r5=r;g6=g;g8=g;g5=g;b8=b;b6=b;b5=b;
		// south z+
		// x y z u v r g b a lu lv
		addVert(builder, matrixStackIn, 0, 0, w, minu, minv, r8, g8, b8, a, lu, lv, 0, 0, 1);
		addVert(builder, matrixStackIn, w, 0, w, maxu, minv, r8, g8, b8, a, lu, lv, 0, 0, 1);
		addVert(builder, matrixStackIn, w, h, w, maxu, maxv, r8, g8, b8, a, lu, lv, 0, 0, 1);
		addVert(builder, matrixStackIn, 0, h, w, minu, maxv, r8, g8, b8, a, lu, lv, 0, 0, 1);
		// west
		addVert(builder, matrixStackIn, 0, 0, 0, minu, minv, r6, g6, b6, a, lu, lv, -1, 0, 0);
		addVert(builder, matrixStackIn, 0, 0, w, maxu, minv, r6, g6, b6, a, lu, lv, -1, 0, 0);
		addVert(builder, matrixStackIn, 0, h, w, maxu, maxv, r6, g6, b6, a, lu, lv, -1, 0, 0);
		addVert(builder, matrixStackIn, 0, h, 0, minu, maxv, r6, g6, b6, a, lu, lv, -1, 0, 0);
		// north
		addVert(builder, matrixStackIn, w, 0, 0, minu, minv, r8, g8, b8, a, lu, lv, 0, 0, -1);
		addVert(builder, matrixStackIn, 0, 0, 0, maxu, minv, r8, g8, b8, a, lu, lv, 0, 0, -1);
		addVert(builder, matrixStackIn, 0, h, 0, maxu, maxv, r8, g8, b8, a, lu, lv, 0, 0, -1);
		addVert(builder, matrixStackIn, w, h, 0, minu, maxv, r8, g8, b8, a, lu, lv, 0, 0, -1);
		// east
		addVert(builder, matrixStackIn, w, 0, w, minu, minv, r6, g6, b6, a, lu, lv, 1, 0, 0);
		addVert(builder, matrixStackIn, w, 0, 0, maxu, minv, r6, g6, b6, a, lu, lv, 1, 0, 0);
		addVert(builder, matrixStackIn, w, h, 0, maxu, maxv, r6, g6, b6, a, lu, lv, 1, 0, 0);
		addVert(builder, matrixStackIn, w, h, w, minu, maxv, r6, g6, b6, a, lu, lv, 1, 0, 0);
	}

	private static void addVert(IVertexBuilder builder, MatrixStack matrixStackIn, float x, float y, float z, float u, float v, float r, float g,
			float b, float a, int lu, int lv, float dx, float dy, float dz) {
		builder.pos(matrixStackIn.getLast().getMatrix(), x, y, z).color(r, g, b, a).tex(u, v).overlay(OverlayTexture.NO_OVERLAY).lightmap(lu, lv)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
	}


	
}
