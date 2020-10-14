
package net.mcreator.moddymcmodface.block;

import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.World;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.Rotation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Mirror;
import net.minecraft.util.Direction;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.StateContainer;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.BooleanProperty;
import net.minecraft.server.MinecraftServer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.BlockItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.block.material.PushReaction;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.Block;

import net.mcreator.moddymcmodface.ModdymcmodfaceModElements;
import net.mcreator.moddymcmodface.CommonUtil;

import java.util.Random;
import java.util.List;
import java.util.Collections;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.util.math.Vec3i;

@ModdymcmodfaceModElements.ModElement.Tag
public class PistonLauncherArmTileBlock extends ModdymcmodfaceModElements.ModElement {
	@ObjectHolder("moddymcmodface:piston_launcher_arm_tile")
	public static final Block block = null;
	@ObjectHolder("moddymcmodface:piston_launcher_arm_tile")
	public static final TileEntityType<CustomTileEntity> tileEntityType = null;
	public PistonLauncherArmTileBlock(ModdymcmodfaceModElements instance) {
		super(instance, 58);
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@Override
	public void initElements() {
		elements.blocks.add(() -> new CustomBlock());
		elements.items.add(() -> new BlockItem(block, new Item.Properties().group(null)).setRegistryName(block.getRegistryName()));
	}

	@SubscribeEvent
	public void registerTileEntity(RegistryEvent.Register<TileEntityType<?>> event) {
		event.getRegistry()
				.register(TileEntityType.Builder.create(CustomTileEntity::new, block).build(null).setRegistryName("piston_launcher_arm_tile"));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientLoad(FMLClientSetupEvent event) {
		//RenderTypeLookup.setRenderLayer(block, RenderType.getCutout());
		ClientRegistry.bindTileEntityRenderer(tileEntityType, CustomRender::new);
	}
	public static class CustomBlock extends Block {
		public static final DirectionProperty FACING = DirectionalBlock.FACING;
		public static final BooleanProperty EXTENDING = CommonUtil.EXTENDING;// is it extending?
		public CustomBlock() {
			super(Block.Properties.create(Material.IRON).sound(SoundType.METAL).hardnessAndResistance(100f, 10f).lightValue(0).doesNotBlockMovement()
					.notSolid());
			this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(EXTENDING, true));
			setRegistryName("piston_launcher_arm_tile");
		}

		@Override
		public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
			return false;
		}

		@Override
		public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
			return VoxelShapes.create(0D, 0D, 0D, 0D, 0D, 0D);
		}

		@Override
		public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
			return new ItemStack(PistonLauncherBlock.block, (int) (1));
		}

		@Override
		public PushReaction getPushReaction(BlockState state) {
			return PushReaction.BLOCK;
		}

		//for correct light rendering??
		@Override
		public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
			return VoxelShapes.empty();
		}


		@Override
		protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
			builder.add(FACING, EXTENDING);
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
		public MaterialColor getMaterialColor(BlockState state, IBlockReader blockAccess, BlockPos pos) {
			return MaterialColor.IRON;
		}

		@Override
		public BlockRenderType getRenderType(BlockState state) {
			return BlockRenderType.INVISIBLE;
		}

		@Override
		public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
			List<ItemStack> dropsOriginal = super.getDrops(state, builder);
			if (!dropsOriginal.isEmpty())
				return dropsOriginal;
			return Collections.singletonList(new ItemStack(Blocks.AIR, (int) (0)));
		}

		@Override
		public boolean hasTileEntity(BlockState state) {
			return true;
		}

		@Override
		public TileEntity createTileEntity(BlockState state, IBlockReader world) {
			return new CustomTileEntity(state.get(EXTENDING), state.get(FACING));
		}

		@Override
		public boolean eventReceived(BlockState state, World world, BlockPos pos, int eventID, int eventParam) {
			super.eventReceived(state, world, pos, eventID, eventParam);
			TileEntity tileentity = world.getTileEntity(pos);
			return tileentity == null ? false : tileentity.receiveClientEvent(eventID, eventParam);
		}
	}

	public static class CustomTileEntity extends TileEntity implements ITickableTileEntity {
		private int age = 0;
		private double increment = 0;
		private double offset = 0;
		private double prevOffset = 0;
		private int dx = 0;
		private int dy = 0;
		private int dz = 0;
		protected final Random rand = new Random();
		protected CustomTileEntity() {
			super(tileEntityType);
		}
		protected CustomTileEntity(boolean extending, Direction dir){
			this();
			this.setParameters(extending, dir);
		}

		public AxisAlignedBB getAdjustedBoundingBox() {
			return new AxisAlignedBB(pos).offset(this.dx * this.offset, this.dy * this.offset, this.dz * this.offset);
		}

		public void tick() {
			
			
		
			if(this.world.isRemote()){
				if (this.getExtending()) {
					double x = this.pos.getX() + 0.5 + this.dx * this.offset;
					double y = this.pos.getY() + this.dy * this.offset;
					double z = this.pos.getZ() + 0.5 + this.dz * this.offset;
					Random random = this.rand;
					for (int l = 0; l < 2; ++l) {
						double d0 = (x + random.nextFloat() - 0.5D);
						double d1 = (y + random.nextFloat() + 0.5D);
						double d2 = (z + random.nextFloat() - 0.5D);
						double d3 = (random.nextFloat() - 0.5D) * 0.05D;
						double d4 = (random.nextFloat() - 0.5D) * 0.05D;
						double d5 = (random.nextFloat() - 0.5D) * 0.05D;
						//world.addParticle(ParticleTypes.POOF, d0, d1, d2, d3, d4, d5);
						this.world.addParticle(ParticleTypes.CLOUD, d0, d1, d2, d3, d4, d5);
					}
				}
			}

			if (this.age > 1) {
				this.prevOffset = this.offset;
				if(!this.world.isRemote()){
					if (this.getExtending()) {
						BlockState _bs = PistonLauncherHeadBlock.block.getDefaultState();
						world.setBlockState(pos, _bs.with(CustomBlock.FACING, this.getDirection()), 3);
					} else {
						BlockState _bs = PistonLauncherBlock.block.getDefaultState();
						BlockPos _bp = pos.offset(this.getDirection().getOpposite());
						if (PistonLauncherBlock.block.getDefaultState().getBlock() == world.getBlockState(_bp).getBlock()) {
							world.setBlockState(_bp, _bs.with(CustomBlock.FACING, this.getDirection()), 3);
						}
						world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
					}
				}
			} else {
				this.age = this.age + 1;
				this.prevOffset = this.offset;
				this.offset += this.increment;
				if (this.getExtending()) {
					AxisAlignedBB p_bb = this.getAdjustedBoundingBox();
					
					List<Entity> list1 = this.world.getEntitiesWithinAABBExcludingEntity(null,p_bb);
					if (!list1.isEmpty()) {
						for (Entity entity : list1) {
							if (entity.getPushReaction() != PushReaction.IGNORE) {
								Vec3d vec3d = entity.getMotion();
								double d1 = vec3d.x;
								double d2 = vec3d.y;
								double d3 = vec3d.z;
								double speed = 1.5;
								if (dx != 0) {
									d1 = this.dx * speed;
								}
								if (dy != 0) {
									d2 = this.dy * speed;
								}
								if (dz != 0) {
									d3 = this.dz * speed;
								}
								
								entity.setMotion(d1, d2, d3);
								entity.velocityChanged=true;
								moveCollidedEntity(entity, p_bb);
							}
						}
					}
				}
				
			}
		}

		private void moveCollidedEntity(Entity entity, AxisAlignedBB p_bb) {
			AxisAlignedBB e_bb = entity.getBoundingBox();
			double dx = 0;
			double dy = 0;
			double dz = 0;
			switch ((Direction) this.getDirection()) {
				default :
					dy = 0;
					break;
				case UP :
					dy = p_bb.maxY - e_bb.minY;
					break;
				case DOWN :
					dy = p_bb.minY - e_bb.maxY;
					break;
				case NORTH :
					dz = p_bb.minZ - e_bb.maxZ;
					break;
				case SOUTH :
					dz = p_bb.maxZ - e_bb.minZ;
					break;
				case WEST :
					dx = p_bb.minX - e_bb.maxX;
					break;
				case EAST :
					dx = p_bb.maxX - e_bb.minX;
					break;
			}
			entity.move(MoverType.PISTON, new Vec3d(dx, dy, dz));
		}

		private void setParameters(boolean extending, Direction dir) {
			this.age = 0;
			if (extending) {
				this.increment = 0.5;
				this.offset = -1;
				this.prevOffset = -1;
			} else {
				this.increment = -0.5;
				this.offset = 0;
				this.prevOffset = 0;
			}
			
			Vec3i v = dir.getDirectionVec();
			this.dx=v.getX();
			this.dy=v.getY();
			this.dz=v.getZ();
		}

		public Direction getDirection(){
			return this.getBlockState().get(CustomBlock.FACING);
		}
		
		public boolean getExtending() {
			return  this.getBlockState().get(CustomBlock.EXTENDING);
		}

		public int getAge() {
			return this.age;
		}

		public double getOffset() {
			return this.offset;
		}

		public double getPrevOffset() {
			return this.prevOffset;
		}

		@Override
		public void read(CompoundNBT compound) {
			super.read(compound);
			this.age = compound.getInt("age");
			this.increment = compound.getDouble("increment");
			this.offset = compound.getDouble("offset");
			this.prevOffset = compound.getDouble("prevOffset");
			this.dx = compound.getInt("dx");
			this.dy = compound.getInt("dy");
			this.dz = compound.getInt("dz");
			
		}

		@Override
		public CompoundNBT write(CompoundNBT compound) {
			super.write(compound);
			compound.putInt("age", this.age);
			compound.putDouble("increment", this.increment);
			compound.putDouble("offset", this.offset);
			compound.putDouble("prevOffset", this.prevOffset);
			compound.putInt("dx", this.dx);
			compound.putInt("dy", this.dy);
			compound.putInt("dz", this.dz);	
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

	public static class CustomRender extends TileEntityRenderer<CustomTileEntity> {
		private static final ResourceLocation texture = new ResourceLocation("moddymcmodface:textures/piston_launcher_head.png");
		public CustomRender(TileEntityRendererDispatcher rendererDispatcherIn) {
			super(rendererDispatcherIn);
		}

		@Override
		public void render(CustomTileEntity entityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
				int combinedOverlayIn) {
			matrixStackIn.push();
			matrixStackIn.translate(0.5, 0.5, 0.5);
			matrixStackIn.rotate(entityIn.getDirection().getOpposite().getRotation());
			matrixStackIn.rotate(Vector3f.XP.rotationDegrees(180.0F));
			matrixStackIn.translate(-0.5, -0.5, -0.5);

			matrixStackIn.translate(0, MathHelper.lerp(partialTicks, entityIn.getPrevOffset(), entityIn.getOffset()), 0);
			BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();

			boolean flag1 = !(entityIn.getExtending() ^ entityIn.getAge() < 2);
			
			BlockState state = PistonLauncherHeadBlock.block.getDefaultState().with(CustomBlock.FACING, Direction.UP).with(BlockStateProperties.SHORT, flag1);
			blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
      
			matrixStackIn.pop();
		}


		public ResourceLocation getEntityTexture(CustomTileEntity entity) {
			return texture;
		}
	}
}
