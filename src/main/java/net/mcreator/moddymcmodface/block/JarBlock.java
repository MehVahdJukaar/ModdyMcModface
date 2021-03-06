
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
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.biome.BiomeColors;
import net.minecraft.world.World;
import net.minecraft.world.IWorldReader;
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
import net.minecraft.util.SoundEvents;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Hand;
import net.minecraft.util.Direction;
import net.minecraft.util.ActionResultType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.stats.Stats;
import net.minecraft.state.StateContainer;
import net.minecraft.state.BooleanProperty;
import net.minecraft.potion.Potions;
import net.minecraft.potion.PotionUtils;
import net.minecraft.pathfinding.PathNodeType;
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
import net.minecraft.item.FishBucketItem;
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
import net.minecraft.entity.MobEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
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
import java.util.concurrent.Callable;
import java.util.Random;
import java.util.List;
import java.util.Collections;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.matrix.MatrixStack;

@ModdymcmodfaceModElements.ModElement.Tag
public class JarBlock extends ModdymcmodfaceModElements.ModElement {
	@ObjectHolder("moddymcmodface:jar")
	public static final Block block = null;
	@ObjectHolder("moddymcmodface:jar")
	public static final TileEntityType<CustomTileEntity> tileEntityType = null;
	// private static final ResourceLocation MY_TEXTURE = new
	// ResourceLocation("moddymcmodface:blocks/potion_liquid");
	public JarBlock(ModdymcmodfaceModElements instance) {
		super(instance, 106);
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new CustomBlock());
		elements.items.add(
			() -> new BlockItem(block, new Item.Properties().maxStackSize(16).setISTER(ISTERProvider::CustomISTER).group(ItemGroup.DECORATIONS))
						.setRegistryName(block.getRegistryName()));
	}

	@SubscribeEvent
	public void registerTileEntity(RegistryEvent.Register<TileEntityType<?>> event) {
		event.getRegistry().register(TileEntityType.Builder.create(CustomTileEntity::new, block).build(null).setRegistryName("jar"));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientLoad(FMLClientSetupEvent event) {
		// Minecraft.getInstance().getBlockColors().register(new myBlockColor(), block);
		RenderTypeLookup.setRenderLayer(block, RenderType.getCutoutMipped());
		ClientRegistry.bindTileEntityRenderer(tileEntityType, CustomRender::new);
		
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
				int color = te.color;
				if (te.isEmpty() || color == 0x000000)
					return null;
				float r = (float) ((color >> 16 & 255)) / 255.0F;
				float g = (float) ((color >> 8 & 255)) / 255.0F;
				float b = (float) ((color >> 0 & 255)) / 255.0F;
				return new float[]{r, g, b};
			}
			return null;
		}

		@Override
		public PathNodeType getAiPathNodeType(BlockState state, IBlockReader world, BlockPos pos, MobEntity entity) {
			return PathNodeType.BLOCKED;
		}

		@Override
		public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
				BlockRayTraceResult hit) {
			TileEntity tileentity = worldIn.getTileEntity(pos);
			if (tileentity instanceof CustomTileEntity) {
				// make te do the work
				CustomTileEntity te = (CustomTileEntity) tileentity;
				if (te.handleInteraction(player, handIn)) {
					if (!worldIn.isRemote())
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
								String s = itextcomponent.getFormattedText();
								s = s.replace(" Bucket", "");
								s = s.replace(" Bottle", "");
								s = s.replace("Bucket of ", "");
								StringTextComponent str = new StringTextComponent(s);
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
		public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
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
		private JarContentType liquidType = JarContentType.EMPTY;
		protected CustomTileEntity() {
			super(tileEntityType);
		}

		// called when inventory is updated -> update tile
		// callen when item is placed chen item has blockentyty tag
		// hijacking this method to work with hoppers
		@Override
		public void markDirty() {
			// this.updateServerAndClient();
			this.updateTile();
			this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
			super.markDirty();
		}

		// I love hardcoding
		public void updateTile() {
			boolean haslava = false;
			ItemStack stack = this.getStackInSlot(0);
			Item it = stack.getItem();
			this.liquidLevel = (float) this.getStackInSlot(0).getCount() / 16f;
			boolean getdefaultcolor = true;
			this.color = 0xFFFFFF;
			if (it instanceof PotionItem) {
				getdefaultcolor = false;
				if (PotionUtils.getPotionFromItem(stack) == Potions.WATER) {
					this.color = -1; //let client get biome color on next rendering. ugly i know but that class is client side
					this.liquidType = JarContentType.WATER;
				} else {
					this.color = PotionUtils.getColor(stack);
					this.liquidType = JarContentType.POTION;
				}
			} else if (it instanceof FishBucketItem) {
				getdefaultcolor = false;
				this.color = -1;
				this.liquidLevel = 0.625f;
				if (it == new ItemStack(Items.COD_BUCKET).getItem()) {
					this.liquidType = JarContentType.COD;
				} else if (it == new ItemStack(Items.PUFFERFISH_BUCKET).getItem()) {
					this.liquidType = JarContentType.PUFFERFISH;
				} else if (it == new ItemStack(Items.SALMON_BUCKET).getItem()) {
					this.liquidType = JarContentType.SALMON;
				} else {
					this.liquidType = JarContentType.TROPICAL_FISH;
				}
			} else if (it == new ItemStack(Items.LAVA_BUCKET).getItem()) {
				this.liquidType = JarContentType.LAVA;
				haslava = true;
			} else if (it instanceof HoneyBottleItem) {
				this.liquidType = JarContentType.HONEY;
			} else if (it instanceof MilkBucketItem) {
				this.liquidType = JarContentType.MILK;
			} else if (it == new ItemStack(Items.DRAGON_BREATH).getItem()) {
				this.liquidType = JarContentType.DRAGON_BREATH;
			} else if (it instanceof ExperienceBottleItem) {
				this.liquidType = JarContentType.XP;
			} else if (it == new ItemStack(Items.COOKIE).getItem()) {
				this.liquidType = JarContentType.COOKIES;
			} else {
				this.liquidType = JarContentType.EMPTY;
			}
			if (getdefaultcolor) {
				this.color = this.liquidType.color;
			}
			BlockState bs = this.world.getBlockState(this.pos);
			if (bs.get(CustomBlock.HAS_LAVA) != haslava) {
				this.world.setBlockState(this.pos, bs.with(CustomBlock.HAS_LAVA, haslava), 2);
			}
		}

		// does all the calculation for handling player interaction.
		public boolean handleInteraction(PlayerEntity player, Hand hand) {
			ItemStack handstack = player.getHeldItem(hand);
			Item handitem = handstack.getItem();
			boolean isbucket = (handitem == new ItemStack(Items.BUCKET).getItem());
			boolean isbottle = (handitem == new ItemStack(Items.GLASS_BOTTLE).getItem());
			boolean isempty = handstack.isEmpty();
			// cookies!
			if (isempty && this.liquidType == JarContentType.COOKIES) {
				boolean eat = false;
				if (player.canEat(false))
					eat = true;
				if (this.extractItem(false, handstack, player, hand, !eat)) {
					if (eat)
						player.getFoodStats().addStats(2, 0.1F);
					return true;
				}
			}
			// is hand item bottle?
			else if (isbottle) {
				// can content be extracted with bottle
				if (this.liquidType.bottle) {
					// if extraction successfull
					if (this.extractItem(false, handstack, player, hand, true)) {
						this.world.playSound(player, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ITEM_BOTTLE_FILL,
								SoundCategory.BLOCKS, 1.0F, 1.0F);
						player.addStat(Stats.ITEM_USED.get(new ItemStack(Items.GLASS_BOTTLE).getItem()));
						return true;
					}
				}
				return false;
			}
			// is hand item bucket?
			else if (isbucket) {
				// can content be extracted with bucket
				if (this.liquidType.bucket) {
					// if extraction successfull
					if (this.extractItem(true, handstack, player, hand, true)) {
						SoundEvent se;
						if (this.liquidType == JarContentType.LAVA) {
							se = SoundEvents.ITEM_BUCKET_FILL_LAVA;
						} else if (this.liquidType.isFish()) {
							se = SoundEvents.ITEM_BUCKET_FILL_FISH;
						} else {
							se = SoundEvents.ITEM_BUCKET_FILL;
						}
						this.world.playSound(player, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ITEM_BUCKET_FILL,
								SoundCategory.BLOCKS, 1.0F, 1.0F);
						player.addStat(Stats.ITEM_USED.get(new ItemStack(Items.BUCKET).getItem()));
						return true;
					}
				}
				return false;
			}
			// can I insert this item?
			else if (this.isItemValidForSlot(0, handstack)) {
				this.handleAddItem(handstack, player, hand);
				return true;
			}
			return false;
		}

		// removes item from te and gives it to player
		public boolean extractItem(boolean isbucket, ItemStack handstack, PlayerEntity player, Hand handIn, boolean givetoplayer) {
			int amount = isbucket && !this.liquidType.isFish() ? 4 : 1;
			ItemStack mystack = this.getStackInSlot(0);
			int count = mystack.getCount();
			// do i have enough?
			if (count >= amount) {
				if (!player.isCreative() && givetoplayer) {
					ItemStack extracted = mystack.copy();
					extracted.setCount(1);
					// special case to convert water bottles into bucket
					if (this.liquidType == JarContentType.WATER && isbucket) {
						extracted = new ItemStack(Items.WATER_BUCKET);
					}
					handstack.shrink(1);
					if (handstack.isEmpty()) {
						player.setHeldItem(handIn, extracted);
					} else if (!player.inventory.addItemStackToInventory(extracted)) {
						player.dropItem(extracted, false);
					}
					/*
					 * else if (player instanceof ServerPlayerEntity) {
					 * ((ServerPlayerEntity)player).sendContainerToPlayer(player.container); }
					 */
				}
				mystack.setCount(Math.max(0, count - amount));
				return true;
			}
			return false;
		}

		// adds item to te, removes from player
		public void handleAddItem(ItemStack handstack, @Nullable PlayerEntity player, @Nullable Hand handIn) {
			ItemStack it = handstack.copy();
			Item i = it.getItem();
			boolean isfish = i instanceof FishBucketItem;
			boolean iswaterbucket = (i == new ItemStack(Items.WATER_BUCKET).getItem());
			boolean isbucket = iswaterbucket || i == new ItemStack(Items.LAVA_BUCKET).getItem() || i == new ItemStack(Items.MILK_BUCKET).getItem()
					|| isfish;
			boolean iscookie = i == Items.COOKIE;
			// shrink stack and replace bottle /bucket with empty ones
			if (player != null && handIn != null) {
				if (!player.isCreative()) {
					handstack.shrink(1);
					if (!iscookie) {
						ItemStack emptybottle = isbucket ? new ItemStack(Items.BUCKET) : new ItemStack(Items.GLASS_BOTTLE);
						if (handstack.isEmpty()) {
							player.setHeldItem(handIn, emptybottle);
						} else if (!player.inventory.addItemStackToInventory(emptybottle)) {
							player.dropItem(emptybottle, false);
						}
					}
				}
				if (!iscookie)
					this.world.playSound(player, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ITEM_BOTTLE_EMPTY,
							SoundCategory.BLOCKS, 1.0F, 1.0F);
			}
			int count = 1;
			if (iswaterbucket) {
				it = PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), Potions.WATER);
			}
			if (isbucket && !isfish) {
				count = 4;
			}
			this.addItem(it, count);
		}

		public void addItem(ItemStack itemstack, int amount) {
			if (this.isEmpty()) {
				itemstack.setCount(amount);
				NonNullList<ItemStack> stacks = NonNullList.<ItemStack>withSize(1, itemstack);
				this.setItems(stacks);
			} else {
				this.getStackInSlot(0).grow(Math.min(amount, this.getInventoryStackLimit() - itemstack.getCount()));
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
			// is it potion
			if (newitem instanceof PotionItem) {
				return (this.isEmpty() || ((PotionUtils.getPotionFromItem(stack) == PotionUtils.getPotionFromItem(currentstack)) && !this.isFull()));
			}
			// is it waterbucket (check it it has water bottle)
			else if (newitem == new ItemStack(Items.WATER_BUCKET).getItem()) {
				return (this.isEmpty() || (PotionUtils.getPotionFromItem(currentstack) == Potions.WATER && !this.isFull()));
			}
			// other items (stack to 12)
			else if (newitem instanceof ExperienceBottleItem || newitem instanceof HoneyBottleItem || newitem instanceof MilkBucketItem
					|| newitem instanceof ExperienceBottleItem || newitem == new ItemStack(Items.LAVA_BUCKET).getItem()
					|| newitem == new ItemStack(Items.DRAGON_BREATH).getItem() || newitem == new ItemStack(Items.COOKIE).getItem()) {
				return (this.isEmpty() || (currentitem == newitem && !this.isFull()));
			}
			// fish bucket (only 1 can stay in)
			else if (newitem instanceof FishBucketItem) {
				return this.isEmpty();
			}
			return false;
		}

		/*
		 * public void loadFromNbt(CompoundNBT compound) { this.stacks =
		 * NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY); if
		 * (!this.checkLootAndRead(compound) && compound.contains("Items", 9)) {
		 * ItemStackHelper.loadAllItems(compound, this.stacks);
		 * 
		 * MinecraftServer mcserv = ServerLifecycleHooks.getCurrentServer();
		 * mcserv.getPlayerList().sendMessage(new StringTextComponent("no"));
		 * 
		 * if(compound.contains("fluidLevel")){ mcserv.getPlayerList().sendMessage(new
		 * StringTextComponent("nwewo")); this.fluidLevel =
		 * compound.getFloat("fluidLevel"); } } }
		 */
		// save to itemstack
		public CompoundNBT saveToNbt(CompoundNBT compound) {
			if (!this.checkLootAndWrite(compound)) {
				ItemStackHelper.saveAllItems(compound, this.stacks, false);
				if (this.liquidLevel != 0) {
					compound.putFloat("liquidLevel", this.liquidLevel);
					compound.putInt("liquidType", this.liquidType.ordinal());
					compound.putInt("liquidColor", this.liquidType.bucket ? this.liquidType.color : this.color);
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
			//can only insert cookies
			return stack.getItem() == Items.COOKIE;
			//return this.isItemValidForSlot(index, stack) && (this.liquidType == JarContentType.COOKIES || this.liquidType == JarContentType.EMPTY);
		}

		@Override
		public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
			return this.liquidType == JarContentType.COOKIES;
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

		@OnlyIn(Dist.CLIENT)
		public int updateClientWaterColor(){
			this.color = BiomeColors.getWaterColor(this.world, this.pos);
			return this.color;			
		}
		
	}
	

	public static class ISTERProvider {
		public static Callable<ItemStackTileEntityRenderer> CustomISTER() {
			return CustomItemRender::new;
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static class CustomItemRender extends ItemStackTileEntityRenderer {
		
		@Override
		public void render(ItemStack stack, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
			matrixStackIn.push();
			BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
			BlockState state = JarBlock.block.getDefaultState();
			blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
			matrixStackIn.pop();
			String t = "";
			float height = 0;
			int color = 0xffffff;
			float opacity = 1;
			JarContentType lt = JarContentType.EMPTY;
			CompoundNBT compound = stack.getTag();
			if (compound != null && !compound.isEmpty() && compound.contains("BlockEntityTag")) {
				compound = compound.getCompound("BlockEntityTag");
				if (compound.contains("liquidType")) {
					lt = JarContentType.values()[compound.getInt("liquidType")];
					t = lt.texture;
					opacity = lt.opacity;
				}
				if (compound.contains("liquidLevel"))
					height = compound.getFloat("liquidLevel");
				if (compound.contains("liquidColor") && lt.applycolor) {
					color = compound.getInt("liquidColor");
				}
				Random rand = new Random(420);
				//cookies
				if (lt == JarContentType.COOKIES) {
					matrixStackIn.push();
					matrixStackIn.translate(0.5, 0.5, 0.5);
					matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-90));
					matrixStackIn.translate(0, 0, -0.5);
					float scale = 8f / 14f;
					matrixStackIn.scale(scale, scale, scale);
					for (float i = 0; i < height; i += 0.0625) {
						matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(rand.nextInt(360)));
						matrixStackIn.translate(0, 0, 0.0625);
						ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
						ItemStack itemstack = new ItemStack(Items.COOKIE);
						IBakedModel ibakedmodel = itemRenderer.getItemModelWithOverrides(itemstack, null, null);
						itemRenderer.renderItem(itemstack, ItemCameraTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, combinedLightIn,
								combinedOverlayIn, ibakedmodel);
						matrixStackIn.translate(0, 0, scale / 16f);
					}
					matrixStackIn.pop();
				} 
				//liquid
				else {
					//fish
					if (lt.isFish()) {
						matrixStackIn.push();
						IVertexBuilder builder1 = bufferIn.getBuffer(RenderType.getCutout());
						matrixStackIn.translate(0.5, 0.375, 0.5);
						matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-45));
						// matrixStackIn.scale(0.6f, 0.6f, 0.6f);
						CommonUtil.renderFish(builder1, matrixStackIn, 0, 0, lt.fishtype, combinedLightIn, combinedOverlayIn);
						matrixStackIn.pop();
					}
					if (height != -0) {
						matrixStackIn.push();
						ResourceLocation texture = new ResourceLocation(t);
						TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(texture);
						IVertexBuilder builder = bufferIn.getBuffer(RenderType.getTranslucent());
						matrixStackIn.translate(0.5, 0.0625, 0.5);
						CommonUtil.addCube(builder, matrixStackIn, 0.5f, height, sprite, combinedLightIn, color, opacity, combinedOverlayIn, true,
								true, false, true);
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
			long r = entityIn.getPos().toLong();
			Random rand = new Random(r);
			//render cookies
			if (entityIn.liquidType == JarContentType.COOKIES) {
				matrixStackIn.push();
				matrixStackIn.translate(0.5, 0.5, 0.5);
				matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-90));
				matrixStackIn.translate(0, 0, -0.5);
				float scale = 8f / 14f;
				matrixStackIn.scale(scale, scale, scale);
				for (float i = 0; i < height; i += 0.0625) {
					matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(rand.nextInt(360)));
					// matrixStackIn.translate(0, 0, 0.0625);
					matrixStackIn.translate(0, 0, 1 / (16f * scale));
					ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
					ItemStack stack = new ItemStack(Items.COOKIE);
					IBakedModel ibakedmodel = itemRenderer.getItemModelWithOverrides(stack, entityIn.getWorld(), null);
					itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, combinedLightIn,
							combinedOverlayIn, ibakedmodel);
				}
				matrixStackIn.pop();
			}
			//render liquid 
			else {
				//render fish
				if (entityIn.liquidType.isFish()) {
					matrixStackIn.push();
					IVertexBuilder builder1 = bufferIn.getBuffer(RenderType.getCutout());
					long time = System.currentTimeMillis();
					float angle = ((time / 80) + r) % 360;
					float angle2 = ((time / 3) + r) % 360;
					float angle3 = ((time / 350) + r) % 360;
					float wo = 0.015f * (float) Math.sin(2 * Math.PI * angle2 / 360);
					float ho = 0.1f * (float) Math.sin(2 * Math.PI * angle3 / 360);
					matrixStackIn.translate(0.5, 0.5, 0.5);
					Quaternion rotation = Vector3f.YP.rotationDegrees(-angle);
					matrixStackIn.rotate(rotation);
					matrixStackIn.scale(0.6f, 0.6f, 0.6f);
					matrixStackIn.translate(0, -0.2, -0.35);
					CommonUtil.renderFish(builder1, matrixStackIn, wo, ho, entityIn.liquidType.fishtype, combinedLightIn, combinedOverlayIn);
					matrixStackIn.pop();
				}
				if (height != 0) {
					matrixStackIn.push();
					int color = entityIn.liquidType.applycolor ? entityIn.color : 0xFFFFFF;
					if (color == -1) color = entityIn.updateClientWaterColor(); //TODO: rewrite this
					float opacity = entityIn.liquidType.opacity;
					ResourceLocation texture = new ResourceLocation(entityIn.liquidType.texture);
					TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(texture);
					// TODO:remove breaking animation
					IVertexBuilder builder = bufferIn.getBuffer(RenderType.getTranslucent());
					matrixStackIn.translate(0.5, 0.0625, 0.5);
					CommonUtil.addCube(builder, matrixStackIn, 0.5f, height, sprite, combinedLightIn, color, opacity, combinedOverlayIn, true, true,
							true, true);
					matrixStackIn.pop();
				}
			}
		}
	}

	public static enum JarContentType {
		// color is handles separatelly. here it's just for default case
		WATER("minecraft:block/water_still", 0x3F76E4, true, 1f, true, true, -1), LAVA("minecraft:block/lava_still", 0xFF6600, false, 1f, false, true,-1),
		MILK("moddymcmodface:blocks/milk_liquid", 0xFFFFFF, false, 1f, false, true, -1), 
		POTION("moddymcmodface:blocks/potion_liquid", 0x3F76E4, true, 0.88f, true, false, -1), 
		HONEY("moddymcmodface:blocks/honey_liquid", 0xFAAC1C, false, 0.85f, true, false, -1), 
		DRAGON_BREATH("moddymcmodface:blocks/dragon_breath_liquid", 0xFF33FF, true, 0.8f, true, false, -1),
		XP("moddymcmodface:blocks/xp_liquid", 0x33FF33, false, 0.95f, true, false, -1), 
		TROPICAL_FISH("minecraft:block/water_still", 0x3F76E4, true, 1f, false, true, 0), 
		SALMON("minecraft:block/water_still", 0x3F76E4, true, 1f, false, true, 1), 
		COD("minecraft:block/water_still", 0x3F76E4, true, 1f, false, true, 2), 
		PUFFERFISH("minecraft:block/water_still", 0x3F76E4, true, 1f, false, true, 3), 
		COOKIES("", 0x000000, false, 1f, false, false, -1), 
		EMPTY("", 0x000000, false, 1f, false, false, -1);
		public final String texture;
		public final float opacity;
		public final int color;
		public final boolean applycolor;
		public final boolean bucket;
		public final boolean bottle;
		public final int fishtype;
		private JarContentType(String texture, int color, boolean applycolor, float opacity, boolean bottle, boolean bucket, int fishtype) {
			this.texture = texture;
			this.color = color; // beacon color. this will also be texture color if applycolor is true
			this.applycolor = applycolor; // is texture grayscale and needs to be colored?
			this.opacity = opacity;
			this.bottle = bottle;
			this.bucket = bucket;
			this.fishtype = fishtype;
			// offet for fish textures. -1 is no fish
		}

		public boolean isFish() {
			return this.fishtype != -1;
		}
	}
}
