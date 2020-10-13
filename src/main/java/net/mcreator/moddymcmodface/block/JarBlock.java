
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
import net.mcreator.moddymcmodface.Customrender;
import net.mcreator.moddymcmodface.Network;

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
import net.minecraft.world.IWorldReader;
import javax.swing.border.EmptyBorder;
import net.minecraft.item.FishBucketItem;
import net.minecraft.world.biome.BiomeColors;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.util.math.MathHelper;
import java.util.Random;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.ai.brain.task.UpdateActivityTask;
import net.minecraftforge.common.util.Constants;

@ModdymcmodfaceModElements.ModElement.Tag
public class JarBlock extends ModdymcmodfaceModElements.ModElement {
	@ObjectHolder("moddymcmodface:jar")
	public static final Block block = null;
	@ObjectHolder("moddymcmodface:jar")
	public static final TileEntityType<CustomTileEntity> tileEntityType = null;
	//private static final ResourceLocation MY_TEXTURE = new ResourceLocation("moddymcmodface:blocks/potion_liquid");
	public JarBlock(ModdymcmodfaceModElements instance) {
		super(instance, 106);
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new CustomBlock());
		elements.items.add(() -> new BlockItem(block, new Item.Properties().maxStackSize(16).setISTER(ISTERProvider::CustomISTER).group(ItemGroup.DECORATIONS)).setRegistryName(block.getRegistryName()));
	}

	@SubscribeEvent
	public void registerTileEntity(RegistryEvent.Register<TileEntityType<?>> event) {
		event.getRegistry().register(TileEntityType.Builder.create(CustomTileEntity::new, block).build(null).setRegistryName("jar"));
	}

	/*
	 @OnlyIn(Dist.CLIENT) 
	  
	 @SubscribeEvent public static void onTextureStitchEvent(TextureStitchEvent.Pre event){ 
	 ResourceLocation stitching = event.getMap().getTextureLocation();
	 if(!stitching.equals(AtlasTexture.LOCATION_BLOCKS_TEXTURE)) { return; }
		 event.addSprite(MY_TEXTURE); 
	  }*/

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientLoad(FMLClientSetupEvent event) {
		//RenderTypeLookup.setRenderLayer(block, RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(block, RenderType.getCutoutMipped());
		ClientRegistry.bindTileEntityRenderer(tileEntityType, CustomRender::new);
		
		// Minecraft.getInstance().getBlockColors().register(new myBlockColor(), block);
	}
	public static class CustomBlock extends Block {
		public static final BooleanProperty HAS_LAVA = BooleanProperty.create("has_lava");
		public CustomBlock() {
			super(Block.Properties.create(Material.GLASS).sound(SoundType.GLASS).hardnessAndResistance(1f, 1f).lightValue(0).notSolid());
			setRegistryName("jar");
			this.setDefaultState(this.stateContainer.getBaseState().with(HAS_LAVA, false));
		}



		@Override
		public float[] getBeaconColorMultiplier(BlockState state, IWorldReader world, BlockPos pos, BlockPos beaconPos) {
			TileEntity tileentity = world.getTileEntity(pos);
			if (tileentity instanceof CustomTileEntity) {
				CustomTileEntity te = (CustomTileEntity) tileentity;
				if(te.isEmpty())return null;
				//TODO:cookies . delegate this to te
				int color = te.color;
				float r = (float) ((color >> 16 & 255)) / 255.0F;
				float g = (float) ((color >> 8 & 255)) / 255.0F;
				float b = (float) ((color >> 0 & 255)) / 255.0F;
				return new float[]{r, g, b};
			}
			return null;
		}



		@Override
		public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
				BlockRayTraceResult hit) {
			TileEntity tileentity = worldIn.getTileEntity(pos);
			if (tileentity instanceof CustomTileEntity) {
				//make te do the work
				CustomTileEntity te = (CustomTileEntity) tileentity;
				if(te.handleInteraction(player, handIn)){
					if(!worldIn.isRemote()) te.markDirty();
					return ActionResultType.SUCCESS;
				}
			}
			return ActionResultType.PASS;
		}

		// shoulker box code
		@Override
		public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
			TileEntity tileentity = worldIn.getTileEntity(pos);
			if (tileentity instanceof CustomTileEntity) {
				CustomTileEntity shulkerboxtileentity = (CustomTileEntity) tileentity;
				if (!worldIn.isRemote && player.isCreative() && !shulkerboxtileentity.isEmpty()) {
					ItemStack itemstack = new ItemStack(this);
					CompoundNBT compoundnbt = shulkerboxtileentity.saveToNbt(new CompoundNBT());
					if (!compoundnbt.isEmpty()) {
						itemstack.setTagInfo("BlockEntityTag", compoundnbt);
					}
					ItemEntity itementity = new ItemEntity(worldIn, (double) pos.getX(), (double) pos.getY(), (double) pos.getZ(), itemstack);
					itementity.setDefaultPickupDelay();
					worldIn.addEntity(itementity);
				} else {
					shulkerboxtileentity.fillWithLoot(player);
				}
			}
			super.onBlockHarvested(worldIn, pos, state, player);
		}



		@Override
		public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {

			
			TileEntity tileentity = builder.get(LootParameters.BLOCK_ENTITY);
			if (tileentity instanceof CustomTileEntity) {
				CustomTileEntity shulkerboxtileentity = (CustomTileEntity) tileentity;
				/*
				 * builder = builder.withDynamicDrop(CONTENTS, (p_220168_1_, p_220168_2_) -> {
				 * for(int i = 0; i < shulkerboxtileentity.getSizeInventory(); ++i) {
				 * p_220168_2_.accept(shulkerboxtileentity.getStackInSlot(i)); } });
				 */
				ItemStack itemstack = new ItemStack(this);
				CompoundNBT compoundnbt = shulkerboxtileentity.saveToNbt(new CompoundNBT());
				if (!compoundnbt.isEmpty()) {
					itemstack.setTagInfo("BlockEntityTag", compoundnbt);
				}
												

				return Collections.singletonList(itemstack);
			}
			return super.getDrops(state, builder);
		}

		@OnlyIn(Dist.CLIENT)
		public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
			super.addInformation(stack, worldIn, tooltip, flagIn);
			CompoundNBT compoundnbt = stack.getChildTag("BlockEntityTag");
			if (compoundnbt != null) {
				if (compoundnbt.contains("LootTable", 8)) {
					tooltip.add(new StringTextComponent("???????"));
				}
				if (compoundnbt.contains("Items", 9)) {
					NonNullList<ItemStack> nonnulllist = NonNullList.withSize(27, ItemStack.EMPTY);
					ItemStackHelper.loadAllItems(compoundnbt, nonnulllist);
					int i = 0;
					int j = 0;
					for (ItemStack itemstack : nonnulllist) {
						if (!itemstack.isEmpty()) {
							++j;
							if (i <= 4) {
								++i; 
								ITextComponent itextcomponent = itemstack.getDisplayName().deepCopy();
								String s=itextcomponent.getFormattedText();
								s=s.replace(" Bucket","");
								s=s.replace(" Bottle","");
								s=s.replace("Bucket of ","");
								StringTextComponent str=new StringTextComponent(s);
								str.appendText(" x").appendText(String.valueOf(itemstack.getCount()));
								tooltip.add(str);
							}
						}
					}
					if (j - i > 0) {
						tooltip.add((new TranslationTextComponent("container.shulkerBox.more", j - i)).applyTextStyle(TextFormatting.ITALIC));
					}
				}
			}
		}

		public ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state) {
			ItemStack itemstack = super.getItem(worldIn, pos, state);
			CustomTileEntity shulkerboxtileentity = (CustomTileEntity) worldIn.getTileEntity(pos);
			CompoundNBT compoundnbt = shulkerboxtileentity.saveToNbt(new CompoundNBT());
			if (!compoundnbt.isEmpty()) {
				itemstack.setTagInfo("BlockEntityTag", compoundnbt);
			}
			return itemstack;
		}

		// end shoulker box code
		@Override
		protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
			builder.add(HAS_LAVA);
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
			return VoxelShapes.or(VoxelShapes.create(0.1875D, 0D, 0.1875D, 0.8125D, 0.875D, 0.8125D),
					VoxelShapes.create(0.3125, 0.875, 0.3125, 0.6875, 1, 0.6875));
		}

		@Override
		public PushReaction getPushReaction(BlockState state) {
			return PushReaction.DESTROY;
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
	    public int  getLightValue(BlockState state, IBlockReader world, BlockPos pos){
	        return state.get(HAS_LAVA) ? 15 : 0;
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
		public int color = 0xffffff;
		public float liquidLevel = 0;
		public int rand = (new Random()).nextInt(360);
		private JarContentType liquidType = JarContentType.EMPTY;
		protected CustomTileEntity() {
			super(tileEntityType);
		}
        
		//called when inventory is updated -> update tile
		//callen when item is placed chen item has blockentyty tag
		//hijacking this method to work with hoppers
		@Override
		public void markDirty() {
			//this.updateServerAndClient();
			this.updateTile();
			this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);

			super.markDirty();
		}

		
		private void updateServerAndClient() {
			if (this.world instanceof World && !this.world.isRemote()) {
				Network.sendToAllNear(this.pos.getX(), this.pos.getY(), this.pos.getZ(), 128, this.world.getDimension().getType(),
							new Network.PacketUpdateJar(this.pos, this.getStackInSlot(0)));
				this.updateTile();
			}
		}

		//receive new inv from server, then update tile
		public void updateInventoryFromServer(ItemStack stack){
			ItemStack newstack = stack.copy();
			NonNullList<ItemStack> stacks = NonNullList.<ItemStack>withSize(1, newstack);
			this.setItems(stacks);
			this.updateTile();
		}

		//I love hardcoding
		public void updateTile() {
			boolean haslava = false;
			ItemStack stack = this.getStackInSlot(0);
			Item it = stack.getItem();
			
			this.liquidLevel = (float) this.getStackInSlot(0).getCount() / 16f;

			
			if (it instanceof PotionItem) {
				if(PotionUtils.getPotionFromItem(stack) == Potions.WATER){
					this.color = BiomeColors.getWaterColor(this.world, this.pos); 
					this.liquidType = JarContentType.WATER;
				}
				else{
					this.color = PotionUtils.getColor(stack);
					this.liquidType = JarContentType.POTION;
				}
			}
			else if (it instanceof FishBucketItem){
				this.color = BiomeColors.getWaterColor(this.world, this.pos); 
				this.liquidLevel = 0.625f;
				if(it == new ItemStack(Items.COD_BUCKET).getItem()){
					this.liquidType = JarContentType.COD;
				}
				else if(it == new ItemStack(Items.PUFFERFISH_BUCKET).getItem()){
					this.liquidType = JarContentType.PUFFERFISH;
				}
				else if(it == new ItemStack(Items.SALMON_BUCKET).getItem()){
					this.liquidType = JarContentType.SALMON;
				}
				else{
					this.liquidType = JarContentType.TROPICAL_FISH;
				}
			
			} 
			else if (it == new ItemStack(Items.LAVA_BUCKET).getItem()) {
				this.liquidType = JarContentType.LAVA;
				this.color = this.liquidType.color;
				haslava=true;
			} 
			else if (it instanceof HoneyBottleItem) {
				this.liquidType = JarContentType.HONEY;
				this.color = this.liquidType.color;
			} 
			else if (it instanceof MilkBucketItem) {
				this.liquidType = JarContentType.MILK;
				this.color = this.liquidType.color;
			} 
			else if (it == new ItemStack(Items.DRAGON_BREATH).getItem()) {
				this.liquidType = JarContentType.DRAGON_BREATH;
				this.color = this.liquidType.color;
			} 
			else if (it instanceof ExperienceBottleItem){
				this.liquidType = JarContentType.XP;
				this.color = this.liquidType.color;
			}
			else if (it == new ItemStack(Items.COOKIE).getItem()){
				this.liquidType = JarContentType.COOKIES;
				this.color = this.liquidType.color;
			}
			else{
				this.liquidType = JarContentType.EMPTY;
			}
			
			
			BlockState bs = this.world.getBlockState(this.pos);	
			if(bs.get(CustomBlock.HAS_LAVA) != haslava){
				this.world.setBlockState(this.pos, bs.with(CustomBlock.HAS_LAVA, haslava), 2);
			}		
		}
		
		//does all the calculation for handling player interaction.
		public boolean handleInteraction(PlayerEntity player, Hand hand){
			ItemStack handstack = player.getHeldItem(hand);
			Item handitem = handstack.getItem();
			
			boolean isbucket = (handitem == new ItemStack(Items.BUCKET).getItem());
			boolean isbottle = (handitem == new ItemStack(Items.GLASS_BOTTLE).getItem());
			boolean isempty = handstack.isEmpty();
			//cookies!
			if(isempty && this.liquidType==JarContentType.COOKIES){
				boolean eat=false;
				if (player.canEat(false)) eat=true;
		    	if(this.extractItem(false, handstack, player, hand, !eat)){
		    		if(eat) player.getFoodStats().addStats(2, 0.1F);
					return true;
		    	}	
			}
			//is hand item bottle?
			else if(isbottle){
				//can content be extracted with bottle
				if(this.liquidType.bottle){
					//if extraction successfull
					if(this.extractItem(false, handstack, player, hand, true)){
						this.world.playSound(player, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
						player.addStat(Stats.ITEM_USED.get(new ItemStack(Items.GLASS_BOTTLE).getItem()));
						return true;
					}
				}
				return false;	
			}
			//is hand item bucket?
			else if(isbucket){
				//can content be extracted with bucket
				if(this.liquidType.bucket){
					//if extraction successfull
					if(this.extractItem(true, handstack, player, hand, true)){

						SoundEvent se;
						if (this.liquidType==JarContentType.LAVA){
							se = SoundEvents.ITEM_BUCKET_FILL_LAVA;
						}
						else if(this.liquidType.isFish()){
							se = SoundEvents.ITEM_BUCKET_FILL_FISH;
						}
						else{ 
							se = SoundEvents.ITEM_BUCKET_FILL;
						}
						this.world.playSound(player, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
						player.addStat(Stats.ITEM_USED.get(new ItemStack(Items.BUCKET).getItem()));
						return true;
					}
				}
				return false;
			}
			//can I insert this item?
			else if(this.isItemValidForSlot(0, handstack)){
				this.handleAddItem(handstack, player, hand);
				
				return true;
			}
			return false;
			
		}
		//removes item from te and gives it to player
		public boolean extractItem(boolean isbucket, ItemStack handstack, PlayerEntity player, Hand handIn, boolean givetoplayer){
			int amount = isbucket && !this.liquidType.isFish()? 4 : 1;
			ItemStack mystack = this.getStackInSlot(0);
			int count = mystack.getCount();
			//do i have enough?
			if(count>=amount){

				if(!player.isCreative() && givetoplayer){
					ItemStack extracted = mystack.copy();
					extracted.setCount(1);
					//special case to convert water bottles into bucket
					if(this.liquidType == JarContentType.WATER && isbucket){
						extracted = new ItemStack(Items.WATER_BUCKET);
					}

					
					handstack.shrink(1);	
					if (handstack.isEmpty()) {
	             		player.setHeldItem(handIn, extracted);
	            	} else if (!player.inventory.addItemStackToInventory(extracted)) {
	             		player.dropItem(extracted, false);
	          		} 
	          		/*
	          		else if (player instanceof ServerPlayerEntity) {
	             			((ServerPlayerEntity)player).sendContainerToPlayer(player.container);
	          		}*/
				}
				mystack.setCount(Math.max(0, count-amount));
				return true;
			}		
			return false;
		}

		//adds item to te, removes from player
		public void handleAddItem(ItemStack handstack, @Nullable PlayerEntity player, @Nullable Hand handIn) {
			ItemStack it = handstack.copy();
			Item i = it.getItem();
			boolean isfish = i instanceof FishBucketItem;
			boolean iswaterbucket = (i == new ItemStack(Items.WATER_BUCKET).getItem());
			boolean isbucket = iswaterbucket || i == new ItemStack(Items.LAVA_BUCKET).getItem() ||
					i == new ItemStack(Items.MILK_BUCKET).getItem() || isfish;
			boolean iscookie = i == Items.COOKIE;
			
			//shrink stack and replace bottle /bucket with empty ones
			if(player!=null && handIn!=null){			
				if(!player.isCreative()){
					handstack.shrink(1);
					if(!iscookie){
						ItemStack emptybottle = isbucket? new ItemStack(Items.BUCKET) :  new ItemStack(Items.GLASS_BOTTLE);
						
						if (handstack.isEmpty()) {
			             	player.setHeldItem(handIn, emptybottle);
			            } else if (!player.inventory.addItemStackToInventory(emptybottle)) {
			             	player.dropItem(emptybottle, false);
			          	}
					}
				}
			if(!iscookie)this.world.playSound(player, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);	
			}

			int count = 1;
			
			if (iswaterbucket) {
				it = PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), Potions.WATER);
			}
			if(isbucket && !isfish){
				count=4;
			}

			this.addItem(it, count);
		}

		public void addItem(ItemStack itemstack, int amount){
			if (this.isEmpty()) {
				itemstack.setCount(amount);
				NonNullList<ItemStack> stacks = NonNullList.<ItemStack>withSize(1, itemstack);
				this.setItems(stacks);
			}
			else{
				this.getStackInSlot(0).grow(Math.min(amount, this.getInventoryStackLimit()-itemstack.getCount()));
			}
		}


		public boolean isFull() {
			return this.getStackInSlot(0).getCount() >= this.getInventoryStackLimit();
		}


		@Override
		public boolean isItemValidForSlot(int index, ItemStack stack) {
			if (index != 0)
				return false;
			ItemStack currentstack = this.getStackInSlot(0);
			Item newitem = stack.getItem();
			Item currentitem = currentstack.getItem();
			//is it potion
			if (newitem instanceof PotionItem) {
				return (this.isEmpty() || ((PotionUtils.getPotionFromItem(stack) == PotionUtils.getPotionFromItem(currentstack)) && !this.isFull()));
			} 
			//is it waterbucket (check it it has water bottle)
			else if (newitem == new ItemStack(Items.WATER_BUCKET).getItem()) {
				return (this.isEmpty() || (PotionUtils.getPotionFromItem(currentstack) == Potions.WATER&& !this.isFull()));
			} 
			//other items (stack to 12)
			else if (newitem instanceof ExperienceBottleItem || newitem instanceof HoneyBottleItem || newitem instanceof MilkBucketItem || newitem instanceof ExperienceBottleItem
					|| newitem == new ItemStack(Items.LAVA_BUCKET).getItem() || newitem == new ItemStack(Items.DRAGON_BREATH).getItem() || newitem == new ItemStack(Items.COOKIE).getItem()) {
				return (this.isEmpty() || (currentitem == newitem && !this.isFull()));
			}
			//fish bucket (only 1 can stay in)
			else if(newitem instanceof FishBucketItem){
				return this.isEmpty();
			}
			return false;
		}
		/*
		public void loadFromNbt(CompoundNBT compound) {
			this.stacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
			if (!this.checkLootAndRead(compound) && compound.contains("Items", 9)) {
				ItemStackHelper.loadAllItems(compound, this.stacks);
							
				MinecraftServer mcserv = ServerLifecycleHooks.getCurrentServer();
			mcserv.getPlayerList().sendMessage(new StringTextComponent("no"));

				if(compound.contains("fluidLevel")){
					mcserv.getPlayerList().sendMessage(new StringTextComponent("nwewo"));
	        	this.fluidLevel = compound.getFloat("fluidLevel");
				}
			}
		}*/

		//save to itemstack
		public CompoundNBT saveToNbt(CompoundNBT compound) {
			if (!this.checkLootAndWrite(compound)) {
				ItemStackHelper.saveAllItems(compound, this.stacks, false);
				if(this.liquidLevel!=0){
					compound.putFloat("liquidLevel", this.liquidLevel);
					compound.putInt("liquidType", this.liquidType.ordinal());
					compound.putInt("liquidColor",this.liquidType.bucket?this.liquidType.color:this.color);
				}
			}
			return compound;
		}

		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);
			if (!this.checkLootAndRead(compound)) {
				this.stacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
			}
			ItemStackHelper.loadAllItems(compound, this.stacks);
			this.liquidLevel = compound.getFloat("liquid_level");
			this.color = compound.getInt("liquid_color");
			this.liquidType = JarContentType.values()[compound.getInt("liquid_type")];

		}

		@Override
		public CompoundNBT write(CompoundNBT compound) {
			super.write(compound);
			if (!this.checkLootAndWrite(compound)) {
				ItemStackHelper.saveAllItems(compound, this.stacks);
			}
			compound.putInt("liquid_color", this.color);
			compound.putFloat("liquid_level", this.liquidLevel);
			compound.putInt("liquid_type", this.liquidType.ordinal());
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
			return new StringTextComponent("jar");
		}

		@Override
		public int getInventoryStackLimit() {
			return 12;
		}

		@Override
		public Container createMenu(int id, PlayerInventory player) {
			return ChestContainer.createGeneric9X3(id, player, this);
		}

		@Override
		public ITextComponent getDisplayName() {
			return new StringTextComponent("Jar");
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
		public int[] getSlotsForFace(Direction side) {
			return IntStream.range(0, this.getSizeInventory()).toArray();
		}

		@Override
		public boolean canInsertItem(int index, ItemStack stack, @Nullable Direction direction) {
			return this.isItemValidForSlot(index, stack) && (this.liquidType==JarContentType.COOKIES||this.liquidType==JarContentType.EMPTY);
		}

		@Override
		public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
			return this.liquidType==JarContentType.COOKIES;
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
	}

	public static class ISTERProvider{
	    public static Callable<ItemStackTileEntityRenderer> CustomISTER() {
	        return CustomItemRender::new;
	    }
	}

	@OnlyIn(Dist.CLIENT)
	public static class CustomItemRender extends ItemStackTileEntityRenderer {
	
	
	    @Override
	    public void render( ItemStack stack,  MatrixStack matrixStackIn,  IRenderTypeBuffer bufferIn,
	          int combinedLightIn, int combinedOverlayIn) {

	        matrixStackIn.push();

	        BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
	        BlockState state = JarBlock.block.getDefaultState();
	        blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        	
			matrixStackIn.pop();

			String t ="";
			float height = 0;
			int color = 0xffffff;
			float opacity = 1;
			JarContentType lt = JarContentType.EMPTY;
			
	        CompoundNBT compound = stack.getTag();
	        if(compound !=null && !compound.isEmpty() && compound.contains("BlockEntityTag")){
	        	compound = compound.getCompound("BlockEntityTag");
	        	int fishtype=-1;
	        	if(compound.contains("liquidType")){
	        		lt = JarContentType.values()[compound.getInt("liquidType")];
	        		t=lt.texture;
	        		opacity=lt.opacity;
	        	}
	        	if(compound.contains("liquidLevel"))
	        	height = compound.getFloat("liquidLevel");
			    if(compound.contains("liquidColor"))
	        	color = compound.getInt("liquidColor");
	        	
				Random rand = new Random(420);
				if(lt==JarContentType.COOKIES){
					matrixStackIn.push();
					matrixStackIn.translate(0.5, 0.5, 0.5);
					matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-90));
					matrixStackIn.translate(0, 0, -0.5);
					float scale = 8f/14f;
					matrixStackIn.scale(scale, scale, scale);
					
					for(float i=0; i<height; i+=0.0625){
						matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(rand.nextInt(360)));
						matrixStackIn.translate(0, 0, 0.0625);
						ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
				        ItemStack itemstack = new ItemStack(Items.COOKIE);
				        IBakedModel ibakedmodel = itemRenderer.getItemModelWithOverrides(itemstack, null, null);
		       			itemRenderer.renderItem(itemstack, ItemCameraTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, ibakedmodel);	        	
						matrixStackIn.translate(0, 0, scale/16f);
        	
					}
					matrixStackIn.pop();
				}
				else{
					if(lt.isFish()){
						matrixStackIn.push();
			
						IVertexBuilder builder1 = bufferIn.getBuffer(RenderType.getCutout());
			
						matrixStackIn.translate(0.5,0.375,0.5);
						matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-45));
						
						//matrixStackIn.scale(0.6f, 0.6f, 0.6f);
			
						renderFish(builder1, matrixStackIn, 0, 0, fishtype, combinedLightIn, combinedOverlayIn);
						
						matrixStackIn.pop();
					}
					
					if (height != -0) {
						matrixStackIn.push();
						
						ResourceLocation texture = new ResourceLocation(t);
						TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(texture);
						IVertexBuilder builder = bufferIn.getBuffer(RenderType.getTranslucent());
				
						matrixStackIn.translate(0.25, 0.0625, 0.25);
						
						addCube(builder, matrixStackIn, 0.5f, height, sprite, combinedLightIn, color, opacity, combinedOverlayIn, false);
						
						matrixStackIn.pop();
					}
		      	}  
	        }
	    }
	}
	


	@OnlyIn(Dist.CLIENT)
	public static class CustomRender extends TileEntityRenderer<CustomTileEntity> {
		public CustomRender(TileEntityRendererDispatcher rendererDispatcherIn) {
			super(rendererDispatcherIn);
		}


		@Override
		public void render(CustomTileEntity entityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
				int combinedOverlayIn) {
			float height = entityIn.liquidLevel;
			Random rand = new Random(entityIn.rand);
			if(entityIn.liquidType==JarContentType.COOKIES){
				matrixStackIn.push();
				matrixStackIn.translate(0.5, 0.5, 0.5);
				matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-90));
				matrixStackIn.translate(0, 0, -0.5);
				float scale = 8f/14f;
				matrixStackIn.scale(scale, scale, scale);
				
				for(float i=0; i<height; i+=0.0625){
					matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(rand.nextInt(360)));
					//matrixStackIn.translate(0, 0, 0.0625);
					matrixStackIn.translate(0, 0, 1/(16f*scale));
					ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
			        ItemStack stack = new ItemStack(Items.COOKIE);
			        IBakedModel ibakedmodel = itemRenderer.getItemModelWithOverrides(stack, entityIn.getWorld(), null);
	       			itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, ibakedmodel);	        	
					
				}
				matrixStackIn.pop();
			}
			else{
	            if(entityIn.liquidType.isFish()){
					matrixStackIn.push();
					
					IVertexBuilder builder1 = bufferIn.getBuffer(RenderType.getCutout());
		
		            int r = entityIn.rand;
					long time = System.currentTimeMillis();
					float angle = ((time / 80)+r) % 360;
					float angle2 = ((time / 3)+r) % 360;
					float angle3 =((time / 350)+r) % 360;
					float wo =0.015f*(float)Math.sin(2*Math.PI*angle2/360);
					float ho =0.1f*(float)Math.sin(2*Math.PI*angle3/360);
					
					matrixStackIn.translate(0.5,0.5,0.5);
		
					Quaternion rotation = Vector3f.YP.rotationDegrees(-angle);
					matrixStackIn.rotate(rotation);
					
					matrixStackIn.scale(0.6f, 0.6f, 0.6f);
					matrixStackIn.translate(0,-0.2,-0.35);
		
					renderFish(builder1, matrixStackIn, wo, ho, entityIn.liquidType.fishtype, combinedLightIn, combinedOverlayIn);
					
					matrixStackIn.pop();
	            }	    
				if (height != 0) {
					matrixStackIn.push();
					
					int color = entityIn.color;
					float opacity = entityIn.liquidType.opacity;
				
					ResourceLocation texture = new ResourceLocation(entityIn.liquidType.texture);
					TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(texture);
		
					//TODO:remove breaking animation
					IVertexBuilder builder = bufferIn.getBuffer(RenderType.getTranslucent());
		
					matrixStackIn.translate(0.25, 0.0625, 0.25);
					
					addCube(builder, matrixStackIn, 0.5f, height, sprite, combinedLightIn, color, opacity, combinedOverlayIn, true);
					
					matrixStackIn.pop();
				}
			}
		}
	}


	private static void renderFish(IVertexBuilder builder, MatrixStack matrixStackIn, float wo, float ho, int fishType, int combinedLightIn, int combinedOverlayIn){

		ResourceLocation texture = new ResourceLocation("moddymcmodface:blocks/jar_fishes");
		TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(texture);
		
		float w = 5/16f;
		float h = 4/16f;
		float hw = w/2f;
		float hh = h/2f;
		
		int lu = combinedLightIn & '\uffff';
		int lv = combinedLightIn >> 16 & '\uffff';
		float atlasscaleU = sprite.getMaxU() - sprite.getMinU();
		float atlasscaleV = sprite.getMaxV() - sprite.getMinV();
		float minu = sprite.getMinU();
		float maxv = sprite.getMinV() + atlasscaleV*fishType*h;
		float maxu = atlasscaleU * w + minu;
		float minv = atlasscaleV * h + maxv;
		float temp=0;
		for(int j =0; j<2; j++){
			addVert(builder, matrixStackIn, hw-Math.abs(wo/2), -hh+ho, +wo, minu, minv, 1, 1, 1, 1, lu, lv);
			addVert(builder, matrixStackIn, -hw+Math.abs(wo/2), -hh+ho, -wo, maxu, minv, 1, 1, 1, 1, lu, lv);
			addVert(builder, matrixStackIn, -hw+Math.abs(wo/2), hh+ho, -wo, maxu, maxv, 1, 1, 1, 1, lu, lv);
			addVert(builder, matrixStackIn, hw-Math.abs(wo/2), hh+ho, +wo, minu, maxv, 1, 1, 1, 1, lu, lv);
			matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180));
			temp=maxu;
			maxu=minu;
			minu=temp;
		}							
	}

	// shaded rectangle with wx = wz with texture flipped vertically. starts from
	// block 0,0,0
	private static void addCube(IVertexBuilder builder, MatrixStack matrixStackIn, float w, float h, TextureAtlasSprite sprite, int combinedLightIn,
			int color, float a, int combinedOverlayIn, boolean fakeshading) {
		int lu = combinedLightIn & '\uffff';
		int lv = combinedLightIn >> 16 & '\uffff'; // ok
		float atlasscaleU = sprite.getMaxU() - sprite.getMinU();
		float atlasscaleV = sprite.getMaxV() - sprite.getMinV();
		float minu = sprite.getMinU();
		float minv = sprite.getMinV();
		float maxu = atlasscaleU * w + minu;
		float maxv = atlasscaleV * h + minv;
		float maxv2 = atlasscaleV * w + minv;
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
		addVert(builder, matrixStackIn, 0, 0, w, minu, minv, r8, g8, b8, a, lu, lv);
		addVert(builder, matrixStackIn, w, 0, w, maxu, minv, r8, g8, b8, a, lu, lv);
		addVert(builder, matrixStackIn, w, h, w, maxu, maxv, r8, g8, b8, a, lu, lv);
		addVert(builder, matrixStackIn, 0, h, w, minu, maxv, r8, g8, b8, a, lu, lv);
		// west
		addVert(builder, matrixStackIn, 0, 0, 0, minu, minv, r6, g6, b6, a, lu, lv);
		addVert(builder, matrixStackIn, 0, 0, w, maxu, minv, r6, g6, b6, a, lu, lv);
		addVert(builder, matrixStackIn, 0, h, w, maxu, maxv, r6, g6, b6, a, lu, lv);
		addVert(builder, matrixStackIn, 0, h, 0, minu, maxv, r6, g6, b6, a, lu, lv);
		// north
		addVert(builder, matrixStackIn, w, 0, 0, minu, minv, r8, g8, b8, a, lu, lv);
		addVert(builder, matrixStackIn, 0, 0, 0, maxu, minv, r8, g8, b8, a, lu, lv);
		addVert(builder, matrixStackIn, 0, h, 0, maxu, maxv, r8, g8, b8, a, lu, lv);
		addVert(builder, matrixStackIn, w, h, 0, minu, maxv, r8, g8, b8, a, lu, lv);
		// east
		addVert(builder, matrixStackIn, w, 0, w, minu, minv, r6, g6, b6, a, lu, lv);
		addVert(builder, matrixStackIn, w, 0, 0, maxu, minv, r6, g6, b6, a, lu, lv);
		addVert(builder, matrixStackIn, w, h, 0, maxu, maxv, r6, g6, b6, a, lu, lv);
		addVert(builder, matrixStackIn, w, h, w, minu, maxv, r6, g6, b6, a, lu, lv);
		// down
		addVert(builder, matrixStackIn, 0, 0, 0, minu, minv, r5, g5, b5, a, lu, lv);
		addVert(builder, matrixStackIn, w, 0, 0, maxu, minv, r5, g5, b5, a, lu, lv);
		addVert(builder, matrixStackIn, w, 0, w, maxu, maxv2, r5, g5, b5, a, lu, lv);
		addVert(builder, matrixStackIn, 0, 0, w, minu, maxv2, r5, g5, b5, a, lu, lv);
		// up
		addVert(builder, matrixStackIn, 0, h, w, minu, minv, r, g, b, a, lu, lv);
		addVert(builder, matrixStackIn, w, h, w, maxu, minv, r, g, b, a, lu, lv);
		addVert(builder, matrixStackIn, w, h, 0, maxu, maxv2, r, g, b, a, lu, lv);
		addVert(builder, matrixStackIn, 0, h, 0, minu, maxv2, r, g, b, a, lu, lv);
	}

	private static void addVert(IVertexBuilder builder, MatrixStack matrixStackIn, float x, float y, float z, float u, float v, float r, float g,
			float b, float a, int lu, int lv) {
		builder.pos(matrixStackIn.getLast().getMatrix(), x, y, z).color(r, g, b, a).tex(u, v).overlay(OverlayTexture.NO_OVERLAY).lightmap(lu, lv)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
	}

	public static enum JarContentType{
		//color is handles separatelly. here it's just for default case
		WATER("minecraft:block/water_still",0x3F76E4,1f,true,true,-1),
		LAVA("minecraft:block/lava_still",0xFFFFFF,1f,false,true,-1),
		MILK("moddymcmodface:blocks/milk_liquid",0xFFFFFF,1f,false,true,-1),
		POTION("moddymcmodface:blocks/potion_liquid",0x3F76E4,0.88f,true,false,-1),
		HONEY("moddymcmodface:blocks/honey_liquid",0xFFFFFF,0.85f,true,false,-1),
		DRAGON_BREATH("moddymcmodface:blocks/dragon_breath_liquid",0x9900FF,0.8f,true,false,-1),
		XP("moddymcmodface:blocks/xp_liquid",0xFFFFFF,0.95f,true,false,-1),
		TROPICAL_FISH("minecraft:block/water_still",0x3F76E4,1f,false,true,0),
		SALMON("minecraft:block/water_still",0x3F76E4,1f,false,true,1),
		COD("minecraft:block/water_still",0x3F76E4,1f,false,true,2),
		PUFFERFISH("minecraft:block/water_still",0x3F76E4,1f,false,true,3),
		COOKIES("",0x000000,1f,false,false,-1),
		EMPTY("",0x000000,1f,false,false,-1);
		public final String texture;
		public final float opacity;
		public final int color;
		public final boolean bucket; 
		public final boolean bottle;
		public final int fishtype;
		private JarContentType(String texture, int color, float opacity, boolean bottle, boolean bucket, int fishtype){
			this.texture=texture;
			this.color=color;
			this.opacity=opacity;
			this.bottle=bottle;
			this.bucket=bucket;
			this.fishtype=fishtype;
			//offet for fish textures. -1 is no fish
		}	
		public boolean isFish(){
			return this.fishtype!=-1;	
		}
	}	
}
