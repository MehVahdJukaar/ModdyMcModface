
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
		public static final IntegerProperty LIQUID_LEVEL = IntegerProperty.create("liquid", 0, 12);
		public static final BooleanProperty HAS_LAVA = BooleanProperty.create("has_lava");
		public CustomBlock() {
			super(Block.Properties.create(Material.GLASS).sound(SoundType.GLASS).hardnessAndResistance(1f, 1f).lightValue(0).notSolid());
			setRegistryName("jar");
			this.setDefaultState(this.stateContainer.getBaseState().with(LIQUID_LEVEL, 0).with(HAS_LAVA, false));
		}



		@Override
		public float[] getBeaconColorMultiplier(BlockState state, IWorldReader world, BlockPos pos, BlockPos beaconPos) {
			TileEntity tileentity = world.getTileEntity(pos);
			if (tileentity instanceof CustomTileEntity) {
				CustomTileEntity te = (CustomTileEntity) tileentity;
				if(te.isEmpty())return null;
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
				//make te to the work
				CustomTileEntity te = (CustomTileEntity) tileentity;
				if(te.handleInteraction(player, handIn)){
					te.markDirty();
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
			builder.add(LIQUID_LEVEL, HAS_LAVA);
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
	    public int  getLightValue(BlockState state, IBlockReader world, BlockPos pos)
	    {
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
		public float opacity = 1;
		public float fluidLevel = 0;
		private boolean bottle = false;  //can liquid be extracted with bottle?
		private boolean bucket =false; //with buclet?
		private int liquidType = 0;
		
		private String texture = "minecraft:block/water_still";
		protected CustomTileEntity() {
			super(tileEntityType);
		}

		//called when inventory is updatet -> update fluid information
		//callen when item is placed chen item has blockentyty tag
		@Override
		public void markDirty() {

			this.updateFluidInformations();
			super.markDirty();
		}


		public void updateFluidInformations() {
			boolean haslava = false;
			this.color = 0xffffff;
			ItemStack stack = this.getStackInSlot(0);
			Item it = stack.getItem();
			this.bottle=false;
			this.bucket=false;
			if (it instanceof PotionItem) {
				if(PotionUtils.getPotionFromItem(stack) == Potions.WATER){
					this.color = PotionUtils.getColor(stack);
					this.liquidType = 2;
					this.opacity = 1f;//0.85f;
					this.texture = "minecraft:block/water_still";
					this.bottle=true;
					this.bucket=true;
				}
				else{
					this.color = PotionUtils.getColor(stack);
					this.liquidType = 0;
					this.opacity = 0.88f;//0.85f;
					this.texture = "moddymcmodface:blocks/potion_liquid";
					this.bottle=true;
				}
			} else if (it instanceof HoneyBottleItem) {
				this.color = 0xFFFFFF;
				this.liquidType = 1;
				this.opacity = 0.85f;
				this.texture = "moddymcmodface:blocks/honey_liquid";
				this.bottle=true;
			} else if (it instanceof MilkBucketItem) {
				this.color = 0xFFFFFF;
				this.liquidType = 4;
				this.opacity = 1f;
				this.texture = "moddymcmodface:blocks/milk_liquid";
				this.bucket=true;
			} else if (it == new ItemStack(Items.LAVA_BUCKET, 1).getItem()) {
				this.color = 0xFFFFFF;
				this.liquidType = 3;
				this.opacity = 1f;
				this.texture = "minecraft:block/lava_still";
				this.bucket=true;
				haslava=true;
			} else if (it == new ItemStack(Items.DRAGON_BREATH, 1).getItem()) {
				this.color = 0x9900FF;
				this.liquidType = 0;
				this.opacity = 0.9f;//0.35f;
				this.texture = "moddymcmodface:blocks/dragon_breath_liquid";
				this.bottle=true;
			} else if (it instanceof ExperienceBottleItem){
				this.color = 0xFFFFFF;
				this.liquidType = 5;
				this.opacity = 0.95f;
				this.texture = "moddymcmodface:blocks/xp_liquid";
				this.bottle=true;
			}

			// this.texture="minecraft:textures/block/water_still.png"
			this.fluidLevel = (float) this.getStackInSlot(0).getCount() / 16f;
			
			BlockState bs = this.world.getBlockState(this.pos);
			if(bs.get(CustomBlock.HAS_LAVA) != haslava){
				this.world.setBlockState(this.pos, bs.with(CustomBlock.HAS_LAVA, haslava));
			}


			
		}
		
		//does all the calculation for handling player interaction.
		public boolean handleInteraction(PlayerEntity player, Hand hand){
			ItemStack handstack = player.getHeldItem(hand);
			Item handitem = handstack.getItem();
			boolean isbucket = (handitem == new ItemStack(Items.BUCKET).getItem());
			boolean isbottle = (handitem == new ItemStack(Items.GLASS_BOTTLE).getItem());
			//is hand item bottle?
			if(isbottle){
				//can content be extracted with bottle
				if(this.bottle){
					//if extraction successfull
					if(this.extractItem(false, handstack, player, hand)){
						this.world.playSound(player, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0F, 1.0F);
						player.addStat(Stats.ITEM_USED.get(new ItemStack(Items.GLASS_BOTTLE).getItem()));
						return true;
					}
				}
				return false;	
			}
			//is hand item bucket?
			else if(isbucket){
				//can content be extracted with bucket
				if(this.bucket){
					//if extraction successfull
					if(this.extractItem(true, handstack, player, hand)){
						//TODO:add lava sound
						this.world.playSound(player, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ITEM_BUCKET_FILL, SoundCategory.NEUTRAL, 1.0F, 1.0F);
						player.addStat(Stats.ITEM_USED.get(new ItemStack(Items.BUCKET).getItem()));
						return true;
					}
				}
				return false;
			}
			//can I insert this item?
			else if(this.isItemValidForSlot(0, handstack)){
				this.addItem(handstack, player, hand);
				this.world.playSound(player, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.NEUTRAL, 1.0F, 1.0F);
				return true;
			}
			return false;
			
		}
		//removes item from te and gives it to player
		public boolean extractItem(boolean isbucket, ItemStack handstack, PlayerEntity player, Hand handIn){
			int amount = isbucket? 4 : 1;
			ItemStack mystack = this.getStackInSlot(0);
			int count = mystack.getCount();
			//do i have enough?
			if(count>=amount){
				ItemStack extracted = mystack.copy();
				extracted.setCount(1);
				//special case to convert water bottles into bucket
				if(this.bottle&&isbucket){
					extracted = new ItemStack(Items.WATER_BUCKET);
				}
				if(!player.isCreative()){
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
		public void addItem(ItemStack handstack, PlayerEntity player, Hand handIn) {
			ItemStack it = handstack.copy();
			Item i = it.getItem();
			boolean iswaterbucket = (i == new ItemStack(Items.WATER_BUCKET).getItem());
			boolean isbucket = iswaterbucket || i == new ItemStack(Items.LAVA_BUCKET).getItem() ||
					i == new ItemStack(Items.MILK_BUCKET).getItem();

			//shrink stack and replace bottle /bucket with empty ones
			if(!player.isCreative()){
				ItemStack emptybottle = isbucket? new ItemStack(Items.BUCKET) :  new ItemStack(Items.GLASS_BOTTLE);
				handstack.shrink(1);
				if (handstack.isEmpty()) {
	             	player.setHeldItem(handIn, emptybottle);
	            } else if (!player.inventory.addItemStackToInventory(emptybottle)) {
	             	player.dropItem(emptybottle, false);
	          	} 
			}		
			//empty
			if (this.isEmpty()) {
				it.setCount(1);
				if (iswaterbucket) {
					it = PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION, 4), Potions.WATER);
				}
				else if(isbucket){
					it.grow(3);
				}
				NonNullList<ItemStack> stacks = NonNullList.<ItemStack>withSize(1, it);
				this.setItems(stacks);
			} 
			//non empty->increment
			else {
				
				if (isbucket) {
					ItemStack st = this.getStackInSlot(0);
					st.grow(Math.min(4, 12-st.getCount()));
				} else {
					this.getStackInSlot(0).grow(1);
				}
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
			if (newitem instanceof PotionItem) {
				return (this.isEmpty() || ((PotionUtils.getPotionFromItem(stack) == PotionUtils.getPotionFromItem(currentstack)) && !this.isFull()));
			} else if (newitem == new ItemStack(Items.WATER_BUCKET, 1).getItem()) {
				return (this.isEmpty() || (PotionUtils.getPotionFromItem(currentstack) == Potions.WATER&& !this.isFull()));
			} else if (newitem instanceof ExperienceBottleItem || newitem instanceof HoneyBottleItem || newitem instanceof MilkBucketItem || newitem instanceof ExperienceBottleItem
					|| newitem == new ItemStack(Items.LAVA_BUCKET, 1).getItem() || newitem == new ItemStack(Items.DRAGON_BREATH, 1).getItem()) {
				return (this.isEmpty() || (currentitem == newitem && !this.isFull()));
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
				if(this.fluidLevel!=0){
					compound.putString("fluidTexture", this.texture);
					compound.putFloat("fluidLevel", this.fluidLevel);
					compound.putInt("fluidColor",this.color);
					compound.putFloat("fluidOpacity",this.opacity);
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
			this.color = compound.getInt("color");
			this.opacity = compound.getFloat("opacity");
			this.fluidLevel = compound.getFloat("fluid_level");
			this.texture = compound.getString("texture");
			this.bucket = compound.getBoolean("bucket");
			this.bottle = compound.getBoolean("bottle");


		}

		@Override
		public CompoundNBT write(CompoundNBT compound) {
			super.write(compound);
			if (!this.checkLootAndWrite(compound)) {
				ItemStackHelper.saveAllItems(compound, this.stacks);
			}
			compound.putInt("color", this.color);
			compound.putFloat("opacity", this.opacity);
			compound.putFloat("fluid_level", this.fluidLevel);
			compound.putInt("liquid_type", this.liquidType);
			compound.putString("texture", this.texture);
			compound.putBoolean("bucket", this.bucket);
			compound.putBoolean("bottle", this.bottle);
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
			return this.isItemValidForSlot(index, stack);
		}

		@Override
		public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
			return false;
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


			String t ="";
			float height = 0;
			int color = 0xffffff;
			float opacity = 1;
			
	        CompoundNBT compound = stack.getTag();
	        if(compound !=null && !compound.isEmpty() && compound.contains("BlockEntityTag")){
	        	compound = compound.getCompound("BlockEntityTag");
	        	if(compound.contains("fluidTexture"))
	        	t = compound.getString("fluidTexture");
	        	if(compound.contains("fluidLevel"))
	        	height = compound.getFloat("fluidLevel");
	        	if(compound.contains("fluidColor"))
	        	color = compound.getInt("fluidColor");
	        	if(compound.contains("fluidOpacity"))
	        	opacity = compound.getFloat("fluidOpacity");
	
		
				ResourceLocation texture = new ResourceLocation(t);
				TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(texture);
				IVertexBuilder builder = bufferIn.getBuffer(RenderType.getTranslucent());
		
				matrixStackIn.translate(0.25, 0.0625, 0.25);
				if (height != -0) {
					addCube(builder, matrixStackIn, 0.5f, height, sprite, combinedLightIn, color, opacity, combinedOverlayIn, false);
				}
	      	}

	        matrixStackIn.pop();
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
			ResourceLocation texture = new ResourceLocation(entityIn.texture);
			TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(texture);
			//TODO:remove breaking animation
			IVertexBuilder builder = bufferIn.getBuffer(RenderType.getTranslucent());
			 //IVertexBuilder builder
			 //=bufferIn.getBuffer(Customrender.CustomRenderTypes.TRANSLUCENT_CUSTOM);
			int color = entityIn.color;
			float opacity = entityIn.opacity;
			float height = entityIn.fluidLevel;
			matrixStackIn.push();
			matrixStackIn.translate(0.25, 0.0625, 0.25);
			if (height != 0) {
				addCube(builder, matrixStackIn, 0.5f, height, sprite, combinedLightIn, color, opacity, combinedOverlayIn, true);
			}
			matrixStackIn.pop();
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
		// down
		addVert(builder, matrixStackIn, 0, 0, 0, minu, minv, r5, g5, b5, a, lu, lv, 0, -1, 0);
		addVert(builder, matrixStackIn, w, 0, 0, maxu, minv, r5, g5, b5, a, lu, lv, 0, -1, 0);
		addVert(builder, matrixStackIn, w, 0, w, maxu, maxv2, r5, g5, b5, a, lu, lv, 0, -1, 0);
		addVert(builder, matrixStackIn, 0, 0, w, minu, maxv2, r5, g5, b5, a, lu, lv, 0, -1, 0);
		// up
		addVert(builder, matrixStackIn, 0, h, w, minu, minv, r, g, b, a, lu, lv, 0, 1, 0);
		addVert(builder, matrixStackIn, w, h, w, maxu, minv, r, g, b, a, lu, lv, 0, 1, 0);
		addVert(builder, matrixStackIn, w, h, 0, maxu, maxv2, r, g, b, a, lu, lv, 0, 1, 0);
		addVert(builder, matrixStackIn, 0, h, 0, minu, maxv2, r, g, b, a, lu, lv, 0, 1, 0);
	}

	private static void addVert(IVertexBuilder builder, MatrixStack matrixStackIn, float x, float y, float z, float u, float v, float r, float g,
			float b, float a, int lu, int lv, float dx, float dy, float dz) {
		builder.pos(matrixStackIn.getLast().getMatrix(), x, y, z).color(r, g, b, a).tex(u, v).overlay(OverlayTexture.NO_OVERLAY).lightmap(lu, lv)
				.normal(matrixStackIn.getLast().getNormal(), 0, 1, 0).endVertex();
	}




	
}
