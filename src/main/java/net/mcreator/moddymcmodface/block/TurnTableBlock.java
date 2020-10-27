
package net.mcreator.moddymcmodface.block;

import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.RegistryEvent;

import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.World;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Rotation;
import net.minecraft.util.Mirror;
import net.minecraft.util.Hand;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction;
import net.minecraft.util.ActionResultType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.state.properties.ChestType;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.StateContainer;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.BooleanProperty;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.BlockItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.block.material.Material;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.block.WallBannerBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;
import net.minecraft.block.BedBlock;

import net.mcreator.moddymcmodface.ModdymcmodfaceModElements;

import java.util.List;
import java.util.Collections;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

@ModdymcmodfaceModElements.ModElement.Tag
public class TurnTableBlock extends ModdymcmodfaceModElements.ModElement {
	@ObjectHolder("moddymcmodface:turn_table")
	public static final Block block = null;
	@ObjectHolder("moddymcmodface:turn_table")
	public static final TileEntityType<CustomTileEntity> tileEntityType = null;
	public TurnTableBlock(ModdymcmodfaceModElements instance) {
		super(instance, 135);
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new CustomBlock());
		elements.items.add(() -> new BlockItem(block, new Item.Properties().group(ItemGroup.REDSTONE)).setRegistryName(block.getRegistryName()));
	}

	@SubscribeEvent
	public void registerTileEntity(RegistryEvent.Register<TileEntityType<?>> event) {
		event.getRegistry().register(TileEntityType.Builder.create(CustomTileEntity::new, block).build(null).setRegistryName("turn_table"));
	}
	public static final int PERIOD = 20; //TODO:figure out why these two don't match up
	public static final float ANGLE_INCREMENT = 90 / (PERIOD-1);
	public static class CustomBlock extends Block {
		public static final DirectionProperty FACING = DirectionalBlock.FACING;
		public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
		public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;
		public CustomBlock() {
			super(Block.Properties.create(Material.ROCK).sound(SoundType.STONE).hardnessAndResistance(15f, 0.5f).lightValue(0).slipperiness(0.6f));
			this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.UP).with(POWERED, false).with(INVERTED, false));
			setRegistryName("turn_table");
		}

		@Override
		protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
			builder.add(FACING, POWERED, INVERTED);
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
		public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
			boolean powerchanged = this.updatePower(state, world, pos);
			// if power changed and is powered or facing block changed
			if (world.getBlockState(pos).get(POWERED) && powerchanged)
				this.tryRotate(world, pos);
		}

		@Override
		public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
				BlockRayTraceResult hit) {
			Direction face = hit.getFace();
			Direction mydir = state.get(FACING);
			if (face != mydir && face != mydir.getOpposite()) {
				if (!player.abilities.allowEdit) {
					return ActionResultType.PASS;
				} else {
					state = state.cycle(INVERTED);
					float f = state.get(INVERTED) ? 0.55F : 0.5F;
					worldIn.playSound(player, pos, SoundEvents.BLOCK_COMPARATOR_CLICK, SoundCategory.BLOCKS, 0.3F, f);
					worldIn.setBlockState(pos, state, 2 | 4);
					return ActionResultType.SUCCESS;
				}
			}
			return ActionResultType.PASS;
		}

		public boolean updatePower(BlockState state, World world, BlockPos pos) {
			boolean ispowered = world.getRedstonePowerFromNeighbors(pos) > 0;
			boolean haspower = state.get(POWERED);
			// on-off
			if (ispowered != haspower) {
				world.setBlockState(pos, state.with(POWERED, ispowered), 2 | 4);
				return true;
				//returns if state changed
			}
			return false;
			/*
			 * 
			 * //do rotate if(nextrot == 0){ world.setBlockState(pos,
			 * state.with(NEXT_ROTATION, PERIOD), 2|4|16); this.doRotateBlock(pos, state,
			 * world); //mcserv.getPlayerList().sendMessage(new
			 * StringTextComponent("2--"+nextrot));
			 * 
			 * } //keep rotating else if(haspower&&nextrot==PERIOD || nextrot!=PERIOD){
			 * world.setBlockState(pos, state.with(NEXT_ROTATION, nextrot-1), 2|4|16);
			 * world.getPendingBlockTicks().scheduleTick(pos, state.getBlock(), (int) 1);
			 * //mcserv.getPlayerList().sendMessage(new StringTextComponent("1--"+nextrot));
			 * }
			 */
		}

		private void tryRotate(World world, BlockPos pos) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof CustomTileEntity) {
				((CustomTileEntity) te).tryRotate();
			}
		}

		@Override
		public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
			super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
			boolean powerchanged = this.updatePower(state, world, pos);
			// if power changed and is powered or facing block changed
			if (world.getBlockState(pos).get(POWERED) && (powerchanged || fromPos.equals(pos.offset(state.get(FACING)))))
				this.tryRotate(world, pos);
			// TODO:optimze this
		}

		private static Vec3d rotateY(Vec3d vec, double deg) {
			if (deg == 0)
				return vec;
			if (vec == Vec3d.ZERO)
				return vec;
			double x = vec.x;
			double y = vec.y;
			double z = vec.z;
			float angle = (float) ((deg / 180f) * Math.PI);
			double s = Math.sin(angle);
			double c = Math.cos(angle);
			return new Vec3d(x * c + z * s, y, z * c - x * s);
		}

		// rotate entities
		@Override
		public void onEntityWalk(World world, BlockPos pos, Entity e) {
			super.onEntityWalk(world, pos, e);
			BlockState state = world.getBlockState(pos);
			if (state.get(POWERED) && state.get(FACING) == Direction.UP) {
				float increment = state.get(INVERTED) ? ANGLE_INCREMENT : -1 * ANGLE_INCREMENT;
				Vec3d origin = new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
				Vec3d oldpos = e.getPositionVec();
				Vec3d oldoffset = oldpos.subtract(origin);
				Vec3d newoffset = this.rotateY(oldoffset, increment);
				Vec3d posdiff = origin.add(newoffset).subtract(oldpos);
				// mcserv.getPlayerList().sendMessage(new
				// StringTextComponent("diff"+posdiff.toString()));
				e.move(MoverType.SELF, posdiff);
				// e.setMotion(e.getMotion().add(adjustedposdiff));
				e.velocityChanged = true;
				if ((e instanceof LivingEntity)) {
					float diff = e.getRotationYawHead() - increment;
					((LivingEntity) e).setIdleTime(20);
					e.setRenderYawOffset(diff);
					e.setRotationYawHead(diff);
					e.onGround = false;
					e.velocityChanged = true;
				}
				// e.prevRotationYaw = e.rotationYaw;
				e.rotationYaw -= increment;
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
		private int cooldown = PERIOD;
		private boolean canRotate = false;
		// private long tickedGameTime;
		protected CustomTileEntity() {
			super(tileEntityType);
		}

		public void tryRotate() {
			this.canRotate = true;
			// allows for a rotation try nedxt period
		}

		public void tick() {
			if (this.world != null && !this.world.isRemote) {

				// cd > 0
				if (this.cooldown == 0) {
					boolean success = this.doRotateBlock();
					this.cooldown = PERIOD;
					// if it didn't rotate last block that means that block is immovable
					this.canRotate = (success && this.getBlockState().get(CustomBlock.POWERED));
				} else if (this.canRotate) {
					this.cooldown--;
				}
			}
		}

		private boolean isInBlacklist(BlockState state) {
			// double blocks
			if (state.getBlock() instanceof BedBlock)
				return true;
			if (state.has(BlockStateProperties.CHEST_TYPE)) {
				if (!(state.get(BlockStateProperties.CHEST_TYPE) == ChestType.SINGLE))
					return true;
			}
			// no piston bases
			if (state.has(BlockStateProperties.EXTENDED)) {
				if (state.get(BlockStateProperties.EXTENDED) == true)
					return true;
			}
			// neither piston arms
			if (state.has(BlockStateProperties.SHORT))
				return true;
			// can't rotate blocks that it can't toutch
			if (state.getBlock() instanceof WallTorchBlock || state.getBlock() instanceof WallBlock || state.getBlock() instanceof WallBannerBlock)
				return true;
			return false;
		}

		// spaghetti code incoming
		public boolean doRotateBlock() {
			BlockState state = this.getBlockState();

			World world = this.world;
			BlockPos mypos = this.pos;
			Direction mydir = state.get(BlockStateProperties.FACING);
			BlockPos targetpos = mypos.offset(mydir);
			BlockState _bs = world.getBlockState(targetpos);
			// is block blacklisted?
			if (this.isInBlacklist(_bs))
				return false;
			boolean ccw = (state.get(BlockStateProperties.INVERTED) ^ (state.get(BlockStateProperties.FACING) == Direction.DOWN));
			Rotation rot = ccw ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90;
			try {
				// horizontal blocks. only if facing up or down. using each block rotation
				// method
				if (_bs.has(BlockStateProperties.HORIZONTAL_FACING) || _bs.has(BlockStateProperties.FACING_EXCEPT_UP)
						|| _bs.has(BlockStateProperties.RAIL_SHAPE)) {
					if (mydir.getAxis() == Direction.Axis.Y) {
						world.setBlockState(targetpos, _bs.rotate(rot), 3);
						return true;
					} else {
						return false;
					}
				}
				// rotateable blocks
				else if (_bs.has(BlockStateProperties.FACING)) {
					if (mydir.getAxis() == Axis.Y) {
						world.setBlockState(targetpos, _bs.rotate(rot), 3);
						return true;
					} else {
						Vector3f targetvec = _bs.get(BlockStateProperties.FACING).toVector3f();
						Vector3f myvec = mydir.toVector3f();
						if (!ccw)
							targetvec.mul(-1);
						// hacky I know..
						myvec.cross(targetvec);
						if (myvec.equals(new Vector3f(0, 0, 0))) {
							// same axis, can't rotate
							return false;
						}
						Direction newdir = Direction.getFacingFromVector(myvec.getX(), myvec.getY(), myvec.getZ());
						world.setBlockState(targetpos, _bs.with(BlockStateProperties.FACING, newdir), 3);
						return true;
					}
				}
				// axis blocks
				else if (_bs.has(BlockStateProperties.AXIS)) {
					if (mydir.getAxis() == Axis.Y) {
						world.setBlockState(targetpos, _bs.rotate(rot), 3);
						return true;
					} else {
						Axis targetaxis = _bs.get(BlockStateProperties.AXIS);
						Axis myaxis = mydir.getAxis();
						if (myaxis == targetaxis) {
							// same axis, can't rotate
							return false;
						} else if (myaxis == Axis.X) {
							world.setBlockState(targetpos, _bs.with(BlockStateProperties.AXIS, targetaxis == Axis.Y ? Axis.Z : Axis.Y), 3);
							return true;
						} else if (myaxis == Axis.Z) {
							world.setBlockState(targetpos, _bs.with(BlockStateProperties.AXIS, targetaxis == Axis.Y ? Axis.X : Axis.Y), 3);
							return true;
						}
					}
					// TODO:add sign post support
				}
				else if (_bs.has(BlockStateProperties.ROTATION_0_15)){
					if (mydir.getAxis() == Axis.Y) {
						world.setBlockState(targetpos, _bs.rotate(rot), 3);
						return true;
					}
				}	
			} catch (Exception e) {
				// mcserv.getPlayerList().sendMessage(new StringTextComponent("error rotating
				// block: "+e.toString()));
			}
			return false;
		}

		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);
			this.cooldown = compound.getInt("Cooldown");
			this.canRotate = compound.getBoolean("Can_rotate");
		}

		@Override
		public CompoundNBT write(CompoundNBT compound) {
			super.write(compound);
			compound.putInt("Cooldown", this.cooldown);
			compound.putBoolean("Can_rotate", this.canRotate);
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
}
