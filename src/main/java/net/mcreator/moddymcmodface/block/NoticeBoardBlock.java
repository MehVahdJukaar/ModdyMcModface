
package net.mcreator.moddymcmodface.block;

import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.lighting.IWorldLightListener;
import net.minecraft.world.World;
import net.minecraft.world.LightType;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Rotation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Mirror;
import net.minecraft.util.Hand;
import net.minecraft.util.Direction;
import net.minecraft.util.ActionResultType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.StateContainer;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.BooleanProperty;
import net.minecraft.server.MinecraftServer;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.NetworkManager;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.DyeItem;
import net.minecraft.item.DyeColor;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.BlockItem;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.gui.RenderComponentsUtil;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.block.material.PushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.GlassBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;

import net.mcreator.moddymcmodface.gui.NoticeBoardGuiGui;
import net.mcreator.moddymcmodface.ModdymcmodfaceModElements;
import net.mcreator.moddymcmodface.Network;

import javax.annotation.Nullable;

import java.util.stream.IntStream;
import java.util.List;
import java.util.Collections;

import io.netty.buffer.Unpooled;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraftforge.common.util.Constants;

@ModdymcmodfaceModElements.ModElement.Tag
public class NoticeBoardBlock extends ModdymcmodfaceModElements.ModElement {
	@ObjectHolder("moddymcmodface:notice_board")
	public static final Block block = null;
	@ObjectHolder("moddymcmodface:notice_board")
	public static final TileEntityType<CustomTileEntity> tileEntityType = null;
	public NoticeBoardBlock(ModdymcmodfaceModElements instance) {
		super(instance, 85);
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new CustomBlock());
		elements.items.add(() -> new BlockItem(block, new Item.Properties().group(ItemGroup.DECORATIONS)).setRegistryName(block.getRegistryName()));
	}

	@SubscribeEvent
	public void registerTileEntity(RegistryEvent.Register<TileEntityType<?>> event) {
		event.getRegistry().register(TileEntityType.Builder.create(CustomTileEntity::new, block).build(null).setRegistryName("notice_board"));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientLoad(FMLClientSetupEvent event) {
		ClientRegistry.bindTileEntityRenderer(tileEntityType, CustomRender::new);
	}
	public static class CustomBlock extends Block {
		public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
		public static final BooleanProperty HAS_BOOK = BlockStateProperties.HAS_BOOK;
		public CustomBlock() {
			super(Block.Properties.create(Material.WOOD).sound(SoundType.WOOD).hardnessAndResistance(2.5f, 2.5f).lightValue(0));
			this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(HAS_BOOK, false));
			setRegistryName("notice_board");
		}

		@Override
		protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
			builder.add(FACING);
			builder.add(HAS_BOOK);
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
		public PushReaction getPushReaction(BlockState state) {
			return PushReaction.BLOCK;
		}

		@Override
		public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
			List<ItemStack> dropsOriginal = super.getDrops(state, builder);
			if (!dropsOriginal.isEmpty())
				return dropsOriginal;
			return Collections.singletonList(new ItemStack(this, 1));
		}

		@Override
		public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
				BlockRayTraceResult hit) {
			TileEntity tileentity = worldIn.getTileEntity(pos);
			// itemstack.getItem() instanceof WrittenBookItem && player.abilities.allowEdit;
			if (tileentity instanceof CustomTileEntity) {
				ItemStack itemstack = player.getHeldItem(handIn);
				CustomTileEntity te = (CustomTileEntity) tileentity;
				boolean flag = itemstack.getItem() instanceof DyeItem && player.abilities.allowEdit;
				boolean flag2 = (te.isEmpty() && (te.canInsertItem(0, itemstack, null)));
				boolean flag3 = (player.isSneaking() && !te.isEmpty());

				//insert Item
				if (flag2) {
					ItemStack it = (ItemStack) itemstack.copy();
					it.setCount((int) 1);
					NonNullList<ItemStack> stacks = NonNullList.<ItemStack>withSize(1, it);
					te.setItems(stacks);
					if (!player.isCreative()) {
						itemstack.shrink(1);
					}
					te.markDirty();
					return ActionResultType.SUCCESS;
				} 
				// change color
				else if (flag) {
					if(te.setTextColor(((DyeItem) itemstack.getItem()).getDyeColor())){
						if (!player.isCreative()) {
							itemstack.shrink(1);
						}
						te.markDirty();
						return ActionResultType.SUCCESS;
					}
				}
				//pop item
				else if (flag3) {
					ItemStack it = te.removeStackFromSlot(0);
					BlockPos newpos = pos.add(state.get(FACING).getDirectionVec());
					ItemEntity drop = new ItemEntity(worldIn, newpos.getX() + 0.5, newpos.getY() + 0.5, newpos.getZ() + 0.5, it);
					worldIn.addEntity(drop);
					te.markDirty();
					return ActionResultType.SUCCESS;
				} else if (player instanceof ServerPlayerEntity) {
					NetworkHooks.openGui((ServerPlayerEntity) player, new INamedContainerProvider() {
						@Override
						public ITextComponent getDisplayName() {
							return new StringTextComponent("Notice Board");
						}

						@Override
						public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
							return new NoticeBoardGuiGui.GuiContainerMod(id, inventory, new PacketBuffer(Unpooled.buffer()).writeBlockPos(pos));
						}
					}, pos);
					return ActionResultType.SUCCESS;
				}
			} else {
				return ActionResultType.PASS;
			}
			return ActionResultType.SUCCESS;
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
			TileEntity tileentity = world.getTileEntity(pos);
			return tileentity == null ? false : tileentity.receiveClientEvent(eventID, eventParam);
		}

		@Override
		public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
			if (state.getBlock() != newState.getBlock()) {
				TileEntity tileentity = world.getTileEntity(pos);
				if (tileentity instanceof CustomTileEntity) {
					InventoryHelper.dropInventoryItems(world, pos, (CustomTileEntity) tileentity);
					world.updateComparatorOutputLevel(pos, this);
				}
				super.onReplaced(state, world, pos, newState, isMoving);
			}
		}

		@Override
		public boolean hasComparatorInputOverride(BlockState state) {
			return true;
		}

		@Override
		public int getComparatorInputOverride(BlockState blockState, World world, BlockPos pos) {
			TileEntity tileentity = world.getTileEntity(pos);
			if (tileentity instanceof CustomTileEntity)
				return Container.calcRedstoneFromInventory((CustomTileEntity) tileentity);
			else
				return 0;
		}
	}

	public static class CustomTileEntity extends LockableLootTileEntity implements ISidedInventory {
		private NonNullList<ItemStack> stacks = NonNullList.<ItemStack>withSize(1, ItemStack.EMPTY);
		private String txt = null;
		private int fontScale = 1;
		private DyeColor textColor = DyeColor.BLACK;
		private List<ITextComponent> cachedPageLines = Collections.emptyList();
		private boolean inventoryChanged = true; //used to tell renderer when it has to slit new line(have to do it there cause i need fontrenderer function)
		// private int packedFrontLight =0;
		protected CustomTileEntity() {
			super(tileEntityType);
		}


		public DyeColor getTextColor() {
			return this.textColor;
		}

		public boolean setTextColor(DyeColor newColor) {
			if (newColor != this.getTextColor()) {
				this.textColor = newColor;
				//this.world.notifyBlockUpdate(this.getPos(), this.getBlockState(), this.getBlockState(), 3);
				return true;
			} else {
				return false;
			}
		}
  
		//update blockstate and plays sound
		public void updateBoardBlock(boolean b) {
			BlockState _bs = this.world.getBlockState(this.pos);
			if(_bs.get(BlockStateProperties.HAS_BOOK)!=b){
				this.world.setBlockState(this.pos, _bs.with(BlockStateProperties.HAS_BOOK,b), 2);
				if(b){
					this.world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.BLOCKS, 1F,
							this.world.rand.nextFloat() * 0.10F + 0.85F);
				}
				else{
					this.world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.BLOCKS, 1F,
							this.world.rand.nextFloat() * 0.10F + 0.50F);
				}
			}
		}


		//receive new inv from server, then update tile
		public void updateInventoryFromServer(ItemStack stack){
			ItemStack newstack = stack.copy();
			NonNullList<ItemStack> stacks = NonNullList.<ItemStack>withSize(1, newstack);
			this.setItems(stacks);
			this.updateTile();
		}

		//hijacking this method to work with hoppers
		@Override
		public void markDirty() {
			this.updateTile();
			this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
			//this.updateServerAndClient();
			super.markDirty();
		}

		
		private void updateServerAndClient() {
			if (this.world instanceof World && !this.world.isRemote()) {
				Network.sendToAllNear(this.pos.getX(), this.pos.getY(), this.pos.getZ(), 128, this.world.getDimension().getType(),
							new Network.PacketUpdateNoticeBoard(this.pos, this.getStackInSlot(0)));
				this.updateTile();
			}
		}

 
		public void updateTile() {
		    ItemStack itemstack = getStackInSlot(0);
			String s = null;
			this.inventoryChanged = true;
			this.cachedPageLines = Collections.emptyList();
			if (this.isItemValidForSlot(0, itemstack)) {
				
				CompoundNBT com = itemstack.getTag();

				if(com != null){
					ListNBT listnbt = com.getList("pages", 8).copy();
					s = listnbt.getString(0);
				}
				if (s != this.txt) {

					//this.inventoryChanged = true;

					this.txt = s;
				}
				this.updateBoardBlock(true);
			} else {
				if (this.txt != null) {
					//this.inventoryChanged = true;
					this.txt = null;
				}
				this.updateBoardBlock(false);
			}
		}

		public void setFontScale(int s) {
			this.fontScale = s;
		}

		public void setChachedPageLines(List<ITextComponent> l) {
			this.cachedPageLines = l;
		}

		public List<ITextComponent> getCachedPageLines() {
			return this.cachedPageLines;
		}

		public int getFontScale() {
			return this.fontScale;
		}

		public boolean getFlag() {
			if (this.inventoryChanged) {
				this.inventoryChanged = false;
				return true;
			}
			return false;
		}

		@Override
		public AxisAlignedBB getRenderBoundingBox() {
			return new AxisAlignedBB(this.pos);
		}

		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);
			if (!this.checkLootAndRead(compound)) {
				this.stacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
			}
			ItemStackHelper.loadAllItems(compound, this.stacks);
			this.txt = compound.getString("txt");
			this.fontScale = compound.getInt("fontscale");
			this.inventoryChanged = compound.getBoolean("invchanged");
			this.textColor = DyeColor.byTranslationKey(compound.getString("Color"), DyeColor.BLACK);
			// this.packedFrontLight = compound.getInt("light");

		}

		@Override
		public CompoundNBT write(CompoundNBT compound) {
			super.write(compound);
			if (!this.checkLootAndWrite(compound)) {
				ItemStackHelper.saveAllItems(compound, this.stacks);
			}
			if (this.txt != null) {
				compound.putString("txt", this.txt);
			}
			compound.putInt("fontscale", this.fontScale);
			compound.putBoolean("invchanged", this.inventoryChanged);
			compound.putString("Color", this.textColor.getTranslationKey());
			// compound.putInt("light", this.packedFrontLight);
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
		public int getSizeInventory() {
			return stacks.size();
		}

		@Override
		public boolean isEmpty() {
			for (ItemStack itemstack : this.stacks)
				if (!itemstack.isEmpty())
					return false;
			return true;
		}

		@Override
		public ITextComponent getDefaultName() {
			return new StringTextComponent("notice_board");
		}

		@Override
		public int getInventoryStackLimit() {
			return 1;
		}

		@Override
		public Container createMenu(int id, PlayerInventory player) {
			return new NoticeBoardGuiGui.GuiContainerMod(id, player, new PacketBuffer(Unpooled.buffer()).writeBlockPos(this.getPos()));
		}

		@Override
		public ITextComponent getDisplayName() {
			return new StringTextComponent("Notice Board");
		}

		@Override
		protected NonNullList<ItemStack> getItems() {
			return this.stacks;
		}

		@Override
		protected void setItems(NonNullList<ItemStack> stacks) {
			this.stacks = stacks;
		}

		@Override
		public boolean isItemValidForSlot(int index, ItemStack stack) {
			return (stack.getItem() == Items.WRITTEN_BOOK || stack.getItem() == Items.WRITABLE_BOOK);
		}

		@Override
		public int[] getSlotsForFace(Direction side) {
			return IntStream.range(0, this.getSizeInventory()).toArray();
		}

		@Override
		public boolean canInsertItem(int index, ItemStack stack, @Nullable Direction direction) {
			return this.isItemValidForSlot(index, stack);
		}

		@Override
		public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
			return true;
		}
		private final LazyOptional<? extends IItemHandler>[] handlers = SidedInvWrapper.create(this, Direction.values());
		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
			if (!this.removed && facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
				return handlers[facing.ordinal()].cast();
			return super.getCapability(capability, facing);
		}

		@Override
		public void remove() {
			super.remove();
			for (LazyOptional<? extends IItemHandler> handler : handlers)
				handler.invalidate();
		}

		public float getYaw() {
			return -this.getBlockState().get(CustomBlock.FACING).getHorizontalAngle();
		}

		public boolean getAxis() {
			Direction d = this.getBlockState().get(CustomBlock.FACING);
			if (d == Direction.NORTH || d == Direction.SOUTH) {
				return true;
			} else {
				return false;
			}
		}

		//TODO:make this work
		public boolean isTextVisible(){
			return true;
			/*
			BlockState state = this.getBlockState();
			Direction dir = state.get(CustomBlock.FACING);
			BlockPos frontpos = this.pos.offset(dir);
			return state.getBlock().shouldSideBeRendered(state, this.world, frontpos, dir);
			*/
		}

		public int getFrontLight() {
			World world = this.getWorld();
			IWorldLightListener block = world.getLightManager().getLightEngine(LightType.BLOCK);
			IWorldLightListener sky = world.getLightManager().getLightEngine(LightType.SKY);
			BlockPos newpos = this.getPos().add(this.getBlockState().get(CustomBlock.FACING).getDirectionVec());
			int u = block.getLightFor(newpos) * 16;
			int v = sky.getLightFor(newpos) * 16;
			return ((v << 16) | (int) (u));
			// return this.packedFrontLight;
		}

		public String getText() {
			if (this.txt != null) {
				return this.txt;
			} else {
				return (String) "";
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static class CustomRender extends TileEntityRenderer<CustomTileEntity> {
		private static final ResourceLocation texture = new ResourceLocation("moddymcmodface:textures/pistonlauncherentity.png");
		
		public CustomRender(TileEntityRendererDispatcher rendererDispatcherIn) {
			super(rendererDispatcherIn);
		}

		public ITextComponent iGetPageText(String s) {
			try {
				ITextComponent itextcomponent = ITextComponent.Serializer.fromJson(s);
				if (itextcomponent != null) {
					return itextcomponent;
				}
			} catch (Exception var4) {
				;
			}
			return new StringTextComponent(s);
		}

		@Override
		public void render(CustomTileEntity entityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
				int combinedOverlayIn) {
			if(entityIn.isTextVisible()){
			    String page = entityIn.getText();
	        	if (page == null || page == "") {
					return;
				}
				FontRenderer fontrenderer = this.renderDispatcher.getFontRenderer();
		
				matrixStackIn.push();		
	
				int newl = entityIn.getFrontLight();
	
				float d0 = 0f;
				if (entityIn.getAxis()) {
					d0 = 0.8f*0.7f; //0.54
				} else {
					d0 = 0.6f*0.7f;
				}
	
				matrixStackIn.translate(0.5, 0.5, 0.5);
				matrixStackIn.rotate(Vector3f.YP.rotationDegrees(entityIn.getYaw()));
				matrixStackIn.translate(0, 0.5, 0.5005);
				int i = entityIn.getTextColor().getTextColor();
				int r = (int) ((double) NativeImage.getRed(i) * d0);
				int g = (int) ((double) NativeImage.getGreen(i) * d0);
				int b = (int) ((double) NativeImage.getBlue(i) * d0);
				int i1 = NativeImage.getCombined(0, b, g, r);
				float bordery = 0.125f;
				float borderx = 0.1875f;
				int scalingfactor = 1;
				List<ITextComponent> tempPageLines;
				ITextComponent txt = iGetPageText(page);
				int width = fontrenderer.getStringWidth(txt.getFormattedText());
				if (entityIn.getFlag()) {
					float lx = 1 - (2 * borderx);
					float ly = 1 - (2 * bordery);
					float maxlines = 1;
					do {
						scalingfactor = MathHelper.floor(MathHelper.sqrt((width * 8f) / (lx * ly)));
						tempPageLines = RenderComponentsUtil.splitText(txt, MathHelper.floor(lx * scalingfactor), fontrenderer, true, true);
						maxlines = ly * scalingfactor / 8f;
						width += 1;
						// when lines fully filled @scaling factor > actual lines -> no overflow lines
						// rendered
					} while (maxlines < tempPageLines.size());
					entityIn.setFontScale(scalingfactor);
					entityIn.setChachedPageLines(tempPageLines);
				} else {
					tempPageLines = entityIn.getCachedPageLines();
					scalingfactor = entityIn.getFontScale();
				}
				float scale = 1 / (float) scalingfactor;
				matrixStackIn.scale(scale, -scale, scale);
				int numberoflin = tempPageLines.size();
				for (int lin = 0; lin < numberoflin; ++lin) {
					String str = tempPageLines.get(lin).getFormattedText();
					//border offsets. always add 0.5 to center properly
					float dx = (float) (-fontrenderer.getStringWidth(str) / 2f) + 0.5f;
					// float dy = (float) scalingfactor * bordery;
					float dy = (float) ((scalingfactor - (8 * numberoflin)) / 2f) + 0.5f;
					fontrenderer.renderString(str, dx, dy + 8 * lin, i1, false, matrixStackIn.getLast().getMatrix(), bufferIn, false, 0, newl);
				}
				matrixStackIn.pop();
			}
		}
	}
}
