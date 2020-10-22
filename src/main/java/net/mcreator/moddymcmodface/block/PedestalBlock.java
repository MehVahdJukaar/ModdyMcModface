
package net.mcreator.moddymcmodface.block;

import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.CapabilityItemHandler;
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
import net.minecraft.world.World;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Hand;
import net.minecraft.util.Direction;
import net.minecraft.util.ActionResultType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.block.material.PushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;

import net.mcreator.moddymcmodface.Network;
import net.mcreator.moddymcmodface.ModdymcmodfaceModElements;

import javax.annotation.Nullable;

import java.util.stream.IntStream;
import java.util.List;
import java.util.Collections;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.item.ToolItem;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.IntegerProperty;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.common.util.Constants;

@ModdymcmodfaceModElements.ModElement.Tag
public class PedestalBlock extends ModdymcmodfaceModElements.ModElement {
	@ObjectHolder("moddymcmodface:pedestal")
	public static final Block block = null;
	@ObjectHolder("moddymcmodface:pedestal")
	public static final TileEntityType<CustomTileEntity> tileEntityType = null;
	public PedestalBlock(ModdymcmodfaceModElements instance) {
		super(instance, 102);
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new CustomBlock());
		elements.items.add(() -> new BlockItem(block, new Item.Properties().group(ItemGroup.DECORATIONS)).setRegistryName(block.getRegistryName()));
	}

	@SubscribeEvent
	public void registerTileEntity(RegistryEvent.Register<TileEntityType<?>> event) {
		event.getRegistry().register(TileEntityType.Builder.create(CustomTileEntity::new, block).build(null).setRegistryName("pedestal"));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientLoad(FMLClientSetupEvent event) {
		RenderTypeLookup.setRenderLayer(block, RenderType.getCutoutMipped());
		ClientRegistry.bindTileEntityRenderer(tileEntityType, CustomRender::new);
	}
	public static class CustomBlock extends Block {
		public static final BooleanProperty UP = BlockStateProperties.UP;
		public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

		public CustomBlock() {
			super(Block.Properties.create(Material.ROCK).sound(SoundType.STONE).hardnessAndResistance(2f, 6f).lightValue(0).notSolid());
			setRegistryName("pedestal");
			this.setDefaultState(this.stateContainer.getBaseState().with(UP, false).with(DOWN, false));

		}

		@Override
		protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
			builder.add(UP,DOWN);
		}

		@Override
		public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
			super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof CustomTileEntity) {
				((CustomTileEntity)te).updateBlockShape(state, world, pos, fromPos);

			}
		}
		
		@Override
  		public BlockState getStateForPlacement(BlockItemUseContext context) {
  			BlockPos blockpos = context.getPos();
  			IBlockReader world = context.getWorld();
			boolean up = world.getBlockState(blockpos.up()).getBlock() instanceof CustomBlock;
			boolean down =false;
			if(world.getBlockState(blockpos.down()).getBlock() instanceof CustomBlock){
				TileEntity te2 = world.getTileEntity(blockpos.down());
				if ((te2 instanceof CustomTileEntity)){
					if(((CustomTileEntity)te2).isEmpty()){
						down =true;
					}
				}
			}
  			return super.getStateForPlacement(context).with(UP, up).with(DOWN, down);
  		}


		@Override
		public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof CustomTileEntity) {
				ItemStack i = ((CustomTileEntity)te).getStackInSlot(0);
				if (!i.isEmpty()) return i;
			}
			return new ItemStack(this, 1);
		
		}




		@Override
		public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
				BlockRayTraceResult hit) {
			TileEntity tileentity = worldIn.getTileEntity(pos);

			if (tileentity instanceof CustomTileEntity) {
				CustomTileEntity te = (CustomTileEntity) tileentity;
				ItemStack itemstack = player.getHeldItem(handIn);
				boolean flag1 = (te.isEmpty() && !itemstack.isEmpty() && (te.canInsertItem(0, itemstack, null)));
				boolean flag2 = (itemstack.isEmpty() && !te.isEmpty());
				if (flag1) {
					ItemStack it = (ItemStack) itemstack.copy();
					it.setCount((int) 1);
					NonNullList<ItemStack> stacks = NonNullList.<ItemStack>withSize(1, it);
					te.setItems(stacks);
					if (!player.isCreative()) {
						itemstack.shrink(1);
					}
					if(!worldIn.isRemote()){
					    worldIn.playSound((PlayerEntity) null, pos,SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM,SoundCategory.BLOCKS, 1.0F, worldIn.rand.nextFloat() * 0.10F + 0.95F);
						te.markDirty();
					}
					return ActionResultType.SUCCESS;
				} 
				else if (flag2) {
					ItemStack it = te.removeStackFromSlot(0);
					player.setHeldItem(handIn, it);
					if(!worldIn.isRemote()){
						te.markDirty();
					}
					return ActionResultType.SUCCESS;
				} 
			} 
			return ActionResultType.PASS;
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
			
			boolean up = state.get(UP);
			boolean down = state.get(DOWN);
			if(!up){
				if(!down){
					return VoxelShapes.or(VoxelShapes.create(0.1875D, 0.125D, 0.1875D, 0.815D, 0.885D, 0.815D),
					VoxelShapes.create(0.0625D, 0.8125D, 0.0625D, 0.9375D, 1D, 0.9375D),
					VoxelShapes.create(0.0625D, 0D, 0.0625D, 0.9375D, 0.1875D, 0.9375D)
					);
				}
				else{
					return VoxelShapes.or(VoxelShapes.create(0.1875D, 0, 0.1875D, 0.815D, 0.885D, 0.815D),
					VoxelShapes.create(0.0625D, 0.8125D, 0.0625D, 0.9375D, 1D, 0.9375D)
					);
				}
			}
			else{
				if(!down){
					return VoxelShapes.or(VoxelShapes.create(0.1875D, 0.125D, 0.1875D, 0.815D, 1, 0.815D),
					VoxelShapes.create(0.0625D, 0D, 0.0625D, 0.9375D, 0.1875D, 0.9375D)
				);	
				}	
				else{
					return VoxelShapes.create(0.1875D, 0, 0.1875D, 0.815D, 1, 0.815D);	
				}
			}
		}


		@Override
		public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
			List<ItemStack> dropsOriginal = super.getDrops(state, builder);
			if (!dropsOriginal.isEmpty())
				return dropsOriginal;
			return Collections.singletonList(new ItemStack(this, 1));
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
		private int type =0;
		protected CustomTileEntity() {
			super(tileEntityType);
		}

		//hijacking this method to work with hoppers
		@Override
		public void markDirty() {
			//this.updateServerAndClient();
			this.updateTile();
			this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);

			super.markDirty();
		}


		//receive new inv from server, then update tile
		public void updateInventoryFromServer(ItemStack stack){
			ItemStack newstack = stack.copy();
			NonNullList<ItemStack> stacks = NonNullList.<ItemStack>withSize(1, newstack);
			this.setItems(stacks);
			this.updateTile();
		}


		public void updateBlockShape(BlockState state, World world, BlockPos pos, BlockPos fromPos){
			boolean up = false;
			boolean down = false;
			if(fromPos.equals(pos.up())){

				
				if(world.getBlockState(fromPos).getBlock() instanceof CustomBlock){
					
					if (this.isEmpty()){
						up =true;
					}

				}
				if(up!=state.get(CustomBlock.UP)){
					world.setBlockState(pos, state.with(CustomBlock.UP,up), 2);
				}
			}
			else if(fromPos.equals(pos.down())){
				
				if(world.getBlockState(fromPos).getBlock() instanceof CustomBlock){
					TileEntity te2 = world.getTileEntity(fromPos);
					if (!(te2 instanceof CustomTileEntity)) return;
					if(((CustomTileEntity)te2).isEmpty()){
						down =true;
					}
				}
				if(down!=state.get(CustomBlock.DOWN)){
					world.setBlockState(pos, state.with(CustomBlock.DOWN,down), 2);
				}
			}
		}





		public void updateTile() {
		
			this.updateBlockShape(this.world.getBlockState(this.pos), this.world, this.pos, this.pos.down());
			this.updateBlockShape(this.world.getBlockState(this.pos), this.world, this.pos, this.pos.up());
	
			ItemStack itemstack = getStackInSlot(0);
			ItemStack its = (ItemStack) itemstack.copy();
			its.setCount((int) 1);
			NonNullList<ItemStack> stacks = NonNullList.<ItemStack>withSize(1, its);
			//this.setItems(stacks);
			
			Item it = its.getItem();
			if (it instanceof BlockItem){
				this.type=1;
			}
			else if(it instanceof ToolItem){
				this.type=2;
				
			}else{
				this.type=3;
			}
		}

		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);
			if (!this.checkLootAndRead(compound)) {
				this.stacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
			}
			ItemStackHelper.loadAllItems(compound, this.stacks);
			this.type=compound.getInt("type");
		}

		@Override
		public CompoundNBT write(CompoundNBT compound) {
			super.write(compound);
			if (!this.checkLootAndWrite(compound)) {
				ItemStackHelper.saveAllItems(compound, this.stacks);
			}
			compound.putInt("type",this.type);
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
			return new StringTextComponent("pedestal");
		}

		@Override
		public int getInventoryStackLimit() {
			return 1;
		}

		@Override
		public Container createMenu(int id, PlayerInventory player) {
			return ChestContainer.createGeneric9X3(id, player, this);
		}

		@Override
		public ITextComponent getDisplayName() {
			return new StringTextComponent("Pedestal");
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
			return true;
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

		public int getItemType(){
			return	this.type;
		}

		
	}

	public static class CustomRender extends TileEntityRenderer<CustomTileEntity> {
		public CustomRender(TileEntityRendererDispatcher rendererDispatcherIn) {
			super(rendererDispatcherIn);
		}

		@Override
		public void render(CustomTileEntity entityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
				int combinedOverlayIn) {

			/*
					long time = System.currentTimeMillis();
					float angle = (time / 40) % 360;
					Quaternion rotation = Vector3f.ZP.rotationDegrees(angle);
					matrixStackIn.scale(0.55f,0.55f,0.55f);
					matrixStackIn.translate(0.5/1.25, 1.125/1.25, 0.5/1.25);
					//matrixStackIn.translate(2*0.015625, 0, 0);
					matrixStackIn.rotate(rotation);
					matrixStackIn.translate(0.09375/1.25, 0, 0);

			switch((int)entityIn.getItemType()){
				case 1:
					long time = System.currentTimeMillis();
					float angle = (time / 40) % 360;
					Quaternion rotation = Vector3f.YP.rotationDegrees(angle);
	
					//matrixStackIn.translate(0.5, 1.1875, 0.5);
					matrixStackIn.translate(0.5, 1.125, 0.5);

					//matrixStackIn.scale(0.5f,0.5f,0.5f);
					//matrixStackIn.rotate(rotation);
					break;
				case 2:
					matrixStackIn.translate(0.25, 0.25, 0.5);
					Quaternion rotation1 = Vector3f.ZP.rotationDegrees(45);
					matrixStackIn.rotate(rotation1);matrixStackIn.translate(0D, 0.5, 0.D);
					//matrixStackIn.scale(1.25f,1.25f,1.25f);
					break;
				case 0:
					matrixStackIn.translate(0.5, 0.2, 0.5);
					break;
				case 3:
					matrixStackIn.translate(0.5, 1.125, 0.5);
					break;
					
				
				
			}*/

			if(!entityIn.isEmpty()){
				
				matrixStackIn.push();
				
				matrixStackIn.translate(0.5, 1.125, 0.5);
	
				if(!Minecraft.getInstance().isGamePaused()){
					BlockPos blockpos = entityIn.getPos(); 
					long blockoffset = (long)(blockpos.getX()*7 + blockpos.getY()*9 + blockpos.getZ()*13);
		
					long time = System.currentTimeMillis();
					long t = blockoffset + time;
					float angle = (t / 40) % 360;
					Quaternion rotation = Vector3f.YP.rotationDegrees(angle);
		
					matrixStackIn.rotate(rotation);
				}
				
				ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
				ItemStack stack = entityIn.getStackInSlot(0);
				IBakedModel ibakedmodel = itemRenderer.getItemModelWithOverrides(stack, entityIn.getWorld(), null);
				itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.GROUND, true, matrixStackIn, bufferIn, combinedLightIn,
						combinedOverlayIn, ibakedmodel);
						
				matrixStackIn.pop();
			}
		}
	}
}
