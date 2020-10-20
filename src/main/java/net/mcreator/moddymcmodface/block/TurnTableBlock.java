
package net.mcreator.moddymcmodface.block;

import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
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
import net.minecraft.state.properties.ComparatorMode;
import net.minecraft.state.properties.ChestType;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.StateContainer;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.BooleanProperty;
import net.minecraft.server.MinecraftServer;
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

import java.util.Random;
import java.util.List;
import java.util.Collections;

@ModdymcmodfaceModElements.ModElement.Tag
public class TurnTableBlock extends ModdymcmodfaceModElements.ModElement {
	@ObjectHolder("moddymcmodface:turn_table")
	public static final Block block = null;
	public TurnTableBlock(ModdymcmodfaceModElements instance) {
		super(instance, 135);
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new CustomBlock());
		elements.items.add(() -> new BlockItem(block, new Item.Properties().group(ItemGroup.REDSTONE)).setRegistryName(block.getRegistryName()));
	}
	public static final int PERIOD = 19;
	public static final float ANGLE_INCREMENT = 90/(PERIOD+1);
	public static class CustomBlock extends Block {
		public static final DirectionProperty FACING = DirectionalBlock.FACING;
		public static final IntegerProperty NEXT_ROTATION = IntegerProperty.create("cooldown", 0, 19);
		public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
		public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;
		public CustomBlock() {
			super(Block.Properties.create(Material.ROCK).sound(SoundType.STONE).hardnessAndResistance(15f, 0.5f).lightValue(0).slipperiness(0.6f));
			this.setDefaultState(
					this.stateContainer.getBaseState().with(FACING, Direction.UP).with(NEXT_ROTATION, PERIOD).with(POWERED, false).with(INVERTED, false));
			setRegistryName("turn_table");
		}

		@Override
		protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
			builder.add(FACING, NEXT_ROTATION, POWERED, INVERTED);
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
		public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
			this.updateBlock(state, worldIn, pos);
		}

		@Override
		public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
				BlockRayTraceResult hit) {
			Direction face = hit.getFace();
			Direction mydir = state.get(FACING);
			if (face != mydir && face != mydir.getOpposite()){
				if (!player.abilities.allowEdit) {
					return ActionResultType.PASS;
				} else {
					state = state.cycle(INVERTED);
					float f = state.get(INVERTED)? 0.55F : 0.5F;
					worldIn.playSound(player, pos, SoundEvents.BLOCK_COMPARATOR_CLICK, SoundCategory.BLOCKS, 0.3F, f);
					worldIn.setBlockState(pos, state, 2|4);
	
					return ActionResultType.SUCCESS;
				}
			}
			return ActionResultType.PASS;
		}

		public boolean isInBlacklist(BlockState state) {
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

		public boolean isOnCooldown(BlockState state) {
			return state.get(NEXT_ROTATION) != 0;
		}

		// spaghetti code incoming
		public boolean doRotateBlock(BlockPos mypos, BlockState state, World world) {
			MinecraftServer mcserv = ServerLifecycleHooks.getCurrentServer();


			BlockPos targetpos = mypos.offset(state.get(FACING));
			BlockState _bs = world.getBlockState(targetpos);
			
			// is block blacklisted?
			if (this.isInBlacklist(_bs))return false;
			
			
			boolean ccw = (state.get(INVERTED) ^ (state.get(FACING)==Direction.DOWN));
			Rotation rot =  ccw? Rotation.COUNTERCLOCKWISE_90:Rotation.CLOCKWISE_90;
				
			try {
				Direction mydir = state.get(FACING);
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
				else if (_bs.has(FACING)) {
					if (mydir.getAxis() == Axis.Y) {
						world.setBlockState(targetpos, _bs.rotate(rot), 3);
						return true;
					} else {
						Vector3f targetvec = _bs.get(FACING).toVector3f();
						Vector3f myvec = mydir.toVector3f();
						
						if(!ccw) targetvec.mul(-1);
						// hacky I know..
						myvec.cross(targetvec);
						if (myvec.equals(new Vector3f(0, 0, 0))) {
							// same axis, can't rotate
							return false;
						}
						Direction newdir = Direction.getFacingFromVector(myvec.getX(), myvec.getY(), myvec.getZ());
						world.setBlockState(targetpos, _bs.with(FACING, newdir), 3);
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
						Axis myaxis = mydir.getAxis();;
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
					} //TODO:add sign post support
				}
			} catch (Exception e) {
				mcserv.getPlayerList().sendMessage(new StringTextComponent("error rotating block: "+e.toString()));
			}
			return false;
		}


		public void updateBlock(BlockState state, World world, BlockPos pos) {
			boolean flag = world.getRedstonePowerFromNeighbors(pos) > 0;
			boolean haspower = state.get(POWERED);

			int nextrot = state.get(NEXT_ROTATION);
			MinecraftServer mcserv = ServerLifecycleHooks.getCurrentServer();
			//mcserv.getPlayerList().sendMessage(new StringTextComponent("tick"+nextrot));

			//on-off
			if (flag != haspower) {
				haspower = flag;
				state = state.with(POWERED, haspower);
				world.setBlockState(pos, state, 2|4);
				//mcserv.getPlayerList().sendMessage(new StringTextComponent("fl"+nextrot));

			}


			//do rotate
			if(nextrot == 0){
				world.setBlockState(pos, state.with(NEXT_ROTATION, PERIOD), 2|4|16);
				this.doRotateBlock(pos, state, world);
				//mcserv.getPlayerList().sendMessage(new StringTextComponent("2--"+nextrot));
				
			}
			//keep rotating
			else if(haspower&&nextrot==PERIOD || nextrot!=PERIOD){
				world.setBlockState(pos, state.with(NEXT_ROTATION, nextrot-1), 2|4|16);	
				world.getPendingBlockTicks().scheduleTick(pos, state.getBlock(), (int) 1);
				//mcserv.getPlayerList().sendMessage(new StringTextComponent("1--"+nextrot));	
			}


			
	
		}

		@Override
		public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
			super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
			this.updateBlock(state, world, pos);
		}

		@Override
		public void tick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
			super.tick(state, world, pos, random);
			this.updateBlock(state, world, pos);
		}

		public static Vec3d rotateY(Vec3d vec, double deg) {
			if (deg == 0) return vec;
			if (vec == Vec3d.ZERO) return vec;
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
			MinecraftServer mcserv = ServerLifecycleHooks.getCurrentServer();
			BlockState state = world.getBlockState(pos);
			float increment = state.get(INVERTED)? ANGLE_INCREMENT : -1*ANGLE_INCREMENT;
			if (state.get(POWERED)&& state.get(FACING)==Direction.UP) {
				
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
				//e.prevRotationYaw = e.rotationYaw;
				e.rotationYaw -= increment;
			}
		}
	}
}
