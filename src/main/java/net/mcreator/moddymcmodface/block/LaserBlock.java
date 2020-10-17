
package net.mcreator.moddymcmodface.block;

import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

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

import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.World;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Hand;
import net.minecraft.util.Direction;
import net.minecraft.util.ActionResultType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.state.StateContainer;
import net.minecraft.state.IntegerProperty;
import net.minecraft.potion.Potions;
import net.minecraft.potion.PotionUtils;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.item.PotionItem;
import net.minecraft.item.MilkBucketItem;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.HoneyBottleItem;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.BlockItem;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.block.material.PushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;

import net.mcreator.moddymcmodface.ModdymcmodfaceModElements;
import net.mcreator.moddymcmodface.CommonUtil;

import javax.annotation.Nullable;

import java.util.stream.IntStream;
import java.util.List;
import java.util.Collections;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraftforge.client.event.TextureStitchEvent;
import java.awt.TextComponent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.item.BucketItem;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import java.util.concurrent.Callable;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.block.Blocks;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraft.item.GlassBottleItem;
import net.minecraft.block.GlassBlock;
import net.minecraft.world.gen.feature.ShrubFeature;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.stats.Stats;
import sun.reflect.generics.tree.BottomSignature;
import sun.reflect.generics.tree.BottomSignature;
import net.minecraft.util.SoundEvent;

import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.World;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Rotation;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Mirror;
import net.minecraft.util.Direction;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.state.StateContainer;
import net.minecraft.state.DirectionProperty;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.BlockItem;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;
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

import net.mcreator.moddymcmodface.ModdymcmodfaceModElements;

import javax.annotation.Nullable;

import java.util.stream.IntStream;
import java.util.List;
import java.util.Collections;
import net.minecraft.entity.ai.brain.task.UpdateActivityTask;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.entity.LivingEntity;
import org.apache.logging.log4j.core.pattern.MaxLengthConverter;
import net.minecraftforge.common.util.Constants;

@ModdymcmodfaceModElements.ModElement.Tag
public class LaserBlock extends ModdymcmodfaceModElements.ModElement {
	public static final ResourceLocation texture = new ResourceLocation("moddymcmodface:blocks/laser_beam");
	public static final ResourceLocation texture1 = new ResourceLocation("moddymcmodface:blocks/laser_beam_end");
	public static final int MAXLENGHT = 15;
	@ObjectHolder("moddymcmodface:laser")
	public static final Block block = null;
	@ObjectHolder("moddymcmodface:laser")
	public static final TileEntityType<CustomTileEntity> tileEntityType = null;
	public LaserBlock(ModdymcmodfaceModElements instance) {
		super(instance, 113);
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new CustomBlock());
		//elements.items.add(() -> new BlockItem(block, new Item.Properties().group(ItemGroup.REDSTONE)).setRegistryName(block.getRegistryName()));
		elements.items.add(() -> new BlockItem(block, new Item.Properties().group(null)).setRegistryName(block.getRegistryName()));

	}

	@SubscribeEvent
	public void registerTileEntity(RegistryEvent.Register<TileEntityType<?>> event) {
		event.getRegistry().register(TileEntityType.Builder.create(CustomTileEntity::new, block).build(null).setRegistryName("laser"));
	}


	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientLoad(FMLClientSetupEvent event) {
		//RenderTypeLookup.setRenderLayer(block, RenderType.getCutout());
		ClientRegistry.bindTileEntityRenderer(tileEntityType, CustomRender::new);
	}




	
	public static class CustomBlock extends Block {
		public static final DirectionProperty FACING = DirectionalBlock.FACING;
		public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
		public static final IntegerProperty RECEIVING =  IntegerProperty.create("laser_receiving", 0, 15); //it's dececting incoming laser

		
		public CustomBlock() {
			super(Block.Properties.create(Material.ROCK).sound(SoundType.STONE).hardnessAndResistance(3.5f, 3.5f).lightValue(0));
			this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(RECEIVING, 0).with(POWERED,false));
			setRegistryName("laser");
		}

		@Override
		public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
			return state.get(POWERED)? 12 : 0;
		}


		@Override
		public boolean hasComparatorInputOverride(BlockState state) {
			return true;
		}

		@Override
		public int getComparatorInputOverride(BlockState blockState, World world, BlockPos pos) {
            return blockState.get(RECEIVING);
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

		public void updatePower(BlockState state, World world, BlockPos pos){
			if(!world.isRemote()){
				boolean powered = world.getRedstonePowerFromNeighbors(pos)>0;
				if(powered != state.get(POWERED))
					world.setBlockState(pos,state.with(POWERED, powered),2);
					TileEntity tileentity = world.getTileEntity(pos);
					if(tileentity instanceof CustomTileEntity)
						((CustomTileEntity) tileentity).updateReceivingLaser();
		    }
		}

		@Override
		public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
			return true;
		}


		@Override
		protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
			builder.add(FACING,POWERED,RECEIVING);
		}

		public BlockState rotate(BlockState state, Rotation rot) {
			return state.with(FACING, rot.rotate(state.get(FACING)));
		}

		public BlockState mirror(BlockState state, Mirror mirrorIn) {
			return state.rotate(mirrorIn.toRotation(state.get(FACING)));
		}

		@Override
		public BlockState getStateForPlacement(BlockItemUseContext context) {
			return this.getDefaultState().with(FACING, context.getNearestLookingDirection().getOpposite());
		}

		@Override
		public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
			List<ItemStack> dropsOriginal = super.getDrops(state, builder);
			if (!dropsOriginal.isEmpty())
				return dropsOriginal;
			return Collections.singletonList(new ItemStack(this, 1));
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
	   public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {

	      super.onBlockHarvested(worldIn, pos, state, player);
	   }
		@Override
	   public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
			super.onReplaced(state, worldIn, pos, newState, isMoving);
	   }



		
	}

	public static class CustomTileEntity extends TileEntity implements ITickableTileEntity {
		public BlockPos endpos = null; //block that the laser is touching
		public int lenght = 0;
		protected CustomTileEntity() {
			super(tileEntityType);
		}

		//TODO:cache the blockposition on a list for faster accsssing
		//this is already server only
		public void updateBeam(){
			if(this.canEmit()){
				BlockPos p =this.pos;
				Direction dir = this.getDirection();
				int i=0;
				boolean noblockfound = false;
				for(i=0; i<=MAXLENGHT; i++){
					p = this.pos.offset(dir,i+1);
					BlockState state = this.world.getBlockState(p);
					if(state.getOpacity(this.world, p) < 15) continue;
					if(state.isSolidSide(world, p, dir.getOpposite())){
						noblockfound = false;
						break;
					}
				}				if(this.lenght!=i){
					this.lenght = i;
					this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
				}
				if(noblockfound){
					this.endpos = null;
					i=MAXLENGHT+1;
				}
				else{
					this.endpos = p;
					this.updateReceivingLaser();
				}
				

			}
		}

		public void updateReceivingLaser(){
			if(endpos!=null){
			BlockState state = this.world.getBlockState(this.endpos); 
			if(state.getBlock() instanceof CustomBlock && state.get(CustomBlock.RECEIVING)!= MathHelper.clamp(MAXLENGHT+1-this.lenght, 0, 15) && state.get(CustomBlock.FACING)==this.getBlockState().get(CustomBlock.FACING).getOpposite()){
				this.world.setBlockState(this.endpos, state.with(CustomBlock.RECEIVING, MathHelper.clamp(MAXLENGHT+1-this.lenght, 0, 15)),3);
			}
			}
		}//TODO:o check if null

		public void turnOffReceivingLaser(){
			if(endpos!=null){
			BlockState state = this.world.getBlockState(this.endpos); 
			if(state.getBlock() instanceof CustomBlock && state.get(CustomBlock.RECEIVING)!= 0 && state.get(CustomBlock.FACING)==this.getBlockState().get(CustomBlock.FACING).getOpposite()){
				this.world.setBlockState(this.endpos, state.with(CustomBlock.RECEIVING, 0),3);
			}
			}
		}
		
		public boolean canEmit(){
			return this.isPowered() && !this.isReceiving();
		}
		public boolean isReceiving(){
			return this.getBlockState().get(CustomBlock.RECEIVING)>0;
		}		
		public boolean isPowered(){
			return this.getBlockState().get(CustomBlock.POWERED);
		}

		@Override
		public AxisAlignedBB getRenderBoundingBox(){
			return new AxisAlignedBB(getPos(), getPos().offset(this.getDirection(),this.lenght+2).add(1,1,1));
		}


		
		@Override
		public void tick(){
			if (this.world != null && !this.world.isRemote() && this.world.getGameTime() % 20L == 0L) {
				this.updateBeam();
			}
		}

		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);
			this.lenght=compound.getInt("lenght");
		}

		@Override
		public CompoundNBT write(CompoundNBT compound) {
			super.write(compound);
			compound.putInt("lenght", this.lenght);
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
		public void remove() {
			super.remove();

		}

		public Direction getDirection(){
			return this.getBlockState().get(CustomBlock.FACING);
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
			if(entityIn.canEmit()){
				
				TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(texture);
						//IVertexBuilder builder = bufferIn.getBuffer(RenderType.getTranslucent());
				IVertexBuilder builder = bufferIn.getBuffer(RenderType.getTranslucent());
				IVertexBuilder builder1 = bufferIn.getBuffer(RenderType.getLightning());
				// IVertexBuilder builder
				// =bufferIn.getBuffer(Customrender.CustomRenderTypes.TRANSLUCENT_CUSTOM);
				int color =0xff0000;
	
				int lenght = entityIn.lenght;
	
				Direction dir = entityIn.getDirection();
				float yaw = dir.getHorizontalAngle();
				float pitch =0;
				if (dir==Direction.UP) pitch=90f;
				else if (dir==Direction.DOWN) pitch=-90f;
			
				matrixStackIn.push();
				matrixStackIn.translate(0.5, 0.5, 0.5);
				matrixStackIn.rotate(dir.getRotation());
				matrixStackIn.translate(0, -0.5, 0);
			
	
				int j = 240;
				int k = combinedLightIn >> 16 & 255;
				combinedLightIn =  j | k << 16;
		      
				//matrixStackIn.translate(0, 1, 0);
				int l = Math.min(lenght, MAXLENGHT);
				for(int i=0; i<l; i++){
					matrixStackIn.translate(0, 1, 0);
					CommonUtil.addCube(builder, matrixStackIn, 0.125f, 1f, sprite, combinedLightIn, 0xFF0000, 0.7f, combinedOverlayIn, false,false,false, false);
					CommonUtil.addCube(builder1, matrixStackIn, 0.0625f, 1f, sprite, combinedLightIn, 0xFFFFFF, 0.6f, combinedOverlayIn,false,false,false, false);
				}
				if(lenght==MAXLENGHT+1){
					matrixStackIn.translate(0, 1, 0);
					TextureAtlasSprite sprite1 = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(texture1);
					CommonUtil.addCube(builder, matrixStackIn, 0.125f, 1f, sprite1, combinedLightIn, 0xFF0000, 0.7f, combinedOverlayIn, false,false,false, true);
					CommonUtil.addCube(builder1, matrixStackIn, 0.0625f, 1f, sprite1, combinedLightIn, 0xFFFFFF, 0.6f, combinedOverlayIn, false,false,false, true);
				}
				
				matrixStackIn.pop();
			}
		}
	}
			
	
}
