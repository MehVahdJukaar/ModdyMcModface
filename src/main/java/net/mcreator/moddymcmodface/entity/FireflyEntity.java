
package net.mcreator.moddymcmodface.entity;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.DamageSource;
import net.minecraft.pathfinding.FlyingPathNavigator;
import net.minecraft.network.IPacket;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Item;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.passive.IFlyingAnimal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.controller.FlyingMovementController;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.Pose;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.Entity;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Matrix3f;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.block.BlockState;


import net.mcreator.moddymcmodface.ModdymcmodfaceModElements;

import javax.annotation.Nullable;

import java.util.Random;
import java.util.EnumSet;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

@ModdymcmodfaceModElements.ModElement.Tag
public class FireflyEntity extends ModdymcmodfaceModElements.ModElement {
	public static EntityType entity = null;
	public FireflyEntity(ModdymcmodfaceModElements instance) {
		super(instance, 111);
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}

	@Override
	public void initElements() {
		entity = (EntityType.Builder.<CustomEntity>create(CustomEntity::new, EntityClassification.AMBIENT).setShouldReceiveVelocityUpdates(true)
				.setTrackingRange(128).setUpdateInterval(3).setCustomClientFactory(CustomEntity::new).size(0.3125f, 1f)).build("firefly")
						.setRegistryName("firefly");
		elements.entities.add(() -> entity);
		elements.items
				.add(() -> new SpawnEggItem(entity, -4784384, -16777216, new Item.Properties().group(ItemGroup.MISC)).setRegistryName("firefly"));
	}

	@Override
	public void init(FMLCommonSetupEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(entity, renderManager -> new CustomRender(renderManager));
		for (Biome biome : ForgeRegistries.BIOMES.getValues()) {
			boolean biomeCriteria = false;
			if (ForgeRegistries.BIOMES.getKey(biome).equals(new ResourceLocation("plains")))
				biomeCriteria = true;
			if (ForgeRegistries.BIOMES.getKey(biome).equals(new ResourceLocation("swamp")))
				biomeCriteria = true;
			if (ForgeRegistries.BIOMES.getKey(biome).equals(new ResourceLocation("sunflower_plains")))
				biomeCriteria = true;
			if (ForgeRegistries.BIOMES.getKey(biome).equals(new ResourceLocation("dark_forest")))
				biomeCriteria = true;
			if (ForgeRegistries.BIOMES.getKey(biome).equals(new ResourceLocation("dark_forest_hills")))
				biomeCriteria = true;
			if (!biomeCriteria)
				continue;
			biome.getSpawns(EntityClassification.AMBIENT).add(new Biome.SpawnListEntry(entity, 8, 2, 5));
		}
		EntitySpawnPlacementRegistry.register(entity, EntitySpawnPlacementRegistry.PlacementType.NO_RESTRICTIONS,
				Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MobEntity::canSpawnOn);
	}

	public static class CustomEntity extends CreatureEntity implements IFlyingAnimal {
		private int particleCooldown = 20;
		private float alpha = 1;
		private float prevAlpha = 1;
		private int flickerPeriod = 40;
		private int flickerCounter =0;
		public CustomEntity(FMLPlayMessages.SpawnEntity packet, World world) {
			this(entity, world);
		}

		public CustomEntity(EntityType<CustomEntity> type, World world) {
			super(type, world);
			experienceValue = 1;
			setNoAI(false);
			this.moveController = new FlyingMovementController(this, 10, true);
			this.navigator = new FlyingPathNavigator(this, this.world);
			this.setRenderDistanceWeight(20);
			this.flickerCounter = (int)(this.rand.nextFloat()*this.flickerPeriod);
		}

		public float getAlpha() {
			return this.alpha;
		}

		public float getPrevAlpha() {
			return this.prevAlpha;
		}

		@OnlyIn(Dist.CLIENT)
		public double getMaxRenderDistanceSquared() {
			return 65536.0D;
		}

		@Override
		public void tick() {
			super.tick();
			this.flickerCounter++;
			this.prevAlpha = this.alpha;
			float p = 0.3f; 
			this.alpha = Math.max( (1-p)*MathHelper.sin(this.flickerCounter * ((float) Math.PI / this.flickerPeriod))+p, 0);
						MinecraftServer mcserv = ServerLifecycleHooks.getCurrentServer();
			//mcserv.getPlayerList().sendMessage(new StringTextComponent("no"+this.alpha));


			
			this.setMotion(this.getMotion().mul(1.0D, 0.6D, 1.0D));
			this.setMotion(this.getMotion().add(0.02 * (this.rand.nextDouble() - 0.5), 0.03 * (this.rand.nextDouble() - 0.5),
					0.02 * (this.rand.nextDouble() - 0.5)));
		}


		  @Override
		  public boolean isAlive() {
		    return true;
		  }


		@Override
		protected float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn) {
			return sizeIn.height / 2.0F;
		}

		@Override
		public boolean canBeLeashedTo(PlayerEntity player) {
			return false;
		}

		@Override
		public boolean doesEntityNotTriggerPressurePlate() {
			return true;
		}

		@Override
		public boolean canBePushed() {
			return false;
		}

		@Override
		protected void collideWithEntity(Entity entityIn) {
		}

		@Override
		protected void collideWithNearbyEntities() {
		}

		@Override
		protected boolean canTriggerWalking() {
			return false;
		}

		@Override
		public IPacket<?> createSpawnPacket() {
			return NetworkHooks.getEntitySpawningPacket(this);
		}

		@Override
		protected void registerGoals() {
			super.registerGoals();
			this.goalSelector.addGoal(0, new LookRandomlyGoal(this));
			// this.goalSelector.addGoal(1, new SwimGoal(this));
			this.goalSelector.addGoal(2, new CustomEntity.WanderGoal());
		}

		protected void updateAITasks() {
			super.updateAITasks();
		}

		@Override
		public CreatureAttribute getCreatureAttribute() {
			return CreatureAttribute.UNDEFINED;
		}

		protected void dropSpecialItems(DamageSource source, int looting, boolean recentlyHitIn) {
			super.dropSpecialItems(source, looting, recentlyHitIn);
		}

		@Override
		public net.minecraft.util.SoundEvent getHurtSound(DamageSource ds) {
			return (net.minecraft.util.SoundEvent) ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.bat.hurt"));
		}

		@Override
		public net.minecraft.util.SoundEvent getDeathSound() {
			return (net.minecraft.util.SoundEvent) ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.bat.death"));
		}

		@Override
		public boolean onLivingFall(float l, float d) {
			return false;
		}

		@Override
		public boolean attackEntityFrom(DamageSource source, float amount) {
			if (source.getImmediateSource() instanceof ArrowEntity)
				return false;
			if (source == DamageSource.FALL)
				return false;
			if (source == DamageSource.CACTUS)
				return false;
			return super.attackEntityFrom(source, amount);
		}

		@Override
		protected void registerAttributes() {
			super.registerAttributes();
			if (this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null)
				this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2);
			if (this.getAttribute(SharedMonsterAttributes.MAX_HEALTH) != null)
				this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(1);
			if (this.getAttribute(SharedMonsterAttributes.ARMOR) != null)
				this.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0);
			if (this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) == null)
				this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
			this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(0);
			if (this.getAttribute(SharedMonsterAttributes.FLYING_SPEED) == null)
				this.getAttributes().registerAttribute(SharedMonsterAttributes.FLYING_SPEED);
			this.getAttribute(SharedMonsterAttributes.FLYING_SPEED).setBaseValue(0.25);
			this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(48.0D);
		}

		@Override
		protected void updateFallState(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
		}

		@Override
		public void setNoGravity(boolean ignored) {
			super.setNoGravity(true);
		}

		public void livingTick() {
			super.livingTick();
			this.setNoGravity(true);

			this.particleCooldown--;
			/*
			double x = this.getPosX();
			double y = this.getPosY();
			double z = this.getPosZ();
			Random random = this.rand;
			Entity entity = this;
			if (this.particleCooldown < 0 && random.nextInt(10) == 0 && false) {
				this.particleCooldown = 35;
				for (int l = 0; l < 1; ++l) {
					double d0 = (x + (random.nextFloat() - 0.5D) * 0.5D);
					double d1 = (y + (random.nextFloat() - 0.5D) * 0.5D);
					double d2 = (z + (random.nextFloat() - 0.5D) * 0.5D);
					double d3 = (random.nextFloat() - 0.5D) * 0.5D;
					double d4 = (random.nextFloat() - 0.5D) * 0.5D;
					double d5 = (random.nextFloat() - 0.5D) * 0.5D;
					world.addParticle(ParticleList.ORANGE_PARTICLE.get(), x, y + 0.4, z, d3, d4, d5);
				}
			}*/
		}
		//bee code
		class WanderGoal extends Goal {
			WanderGoal() {
				this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
			}

			/**
			 * Returns whether execution should begin. You can also read and cache any state
			 * necessary for execution in this method as well.
			 */
			public boolean shouldExecute() {
				return CustomEntity.this.navigator.noPath() && CustomEntity.this.rand.nextInt(10) == 0;
			}

			/**
			 * Returns whether an in-progress EntityAIBase should continue executing
			 */
			public boolean shouldContinueExecuting() {
				return CustomEntity.this.navigator.func_226337_n_();
			}

			/**
			 * Execute a one shot task or start executing a continuous task
			 */
			public void startExecuting() {
				Vec3d vec3d = this.getRandomLocation();
				if (vec3d != null) {
					CustomEntity.this.navigator.setPath(CustomEntity.this.navigator.getPathToPos(new BlockPos(vec3d), 1), 1.0D);
				}
			}

			@Nullable
			private Vec3d getRandomLocation() {
				Vec3d vec3d;
				vec3d = CustomEntity.this.getLook(0.0F);
				int i = 8;
				Vec3d vec3d2 = RandomPositionGenerator.findAirTarget(CustomEntity.this, 8, 7, vec3d, ((float) Math.PI / 2F), 2, 1);
				return vec3d2 != null
						? vec3d2
						: RandomPositionGenerator.findGroundTarget(CustomEntity.this, 8, 4, -2, vec3d, (double) ((float) Math.PI / 2F));
			}
		}
	}


	public static class CustomRender extends EntityRenderer<CustomEntity> {
		private static final ResourceLocation texture = new ResourceLocation("moddymcmodface:textures/firefly.png");
		// private static final ResourceLocation texture = new
		// ResourceLocation("textures/particle/flame.png");
		public CustomRender(EntityRendererManager renderManager) {
			super(renderManager);
		}

		@Override
		public void render(CustomEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn,
				int packedLightIn) {
			matrixStackIn.push();
			// TextureAtlasSprite sprite =
			// Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_PARTICLES_TEXTURE);

			int j = 255;
			int k = 255;
			int l = 255;
			int a =(int) ((float)255f*MathHelper.lerp(partialTicks, entityIn.getAlpha(), entityIn.getPrevAlpha()));

			matrixStackIn.translate(0.0D, (double) 0.5F, 0.0D);
			matrixStackIn.rotate(this.renderManager.getCameraOrientation());
			matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180.0F));
			float f9 = 0.32F;
			matrixStackIn.scale(0.3F, 0.3F, 0.3F);
			IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getBeaconBeam(texture, true));
			MatrixStack.Entry matrixstack$entry = matrixStackIn.getLast();
			Matrix4f matrix4f = matrixstack$entry.getMatrix();
			Matrix3f matrix3f = matrixstack$entry.getNormal();
			vertex(ivertexbuilder, matrix4f, matrix3f, -0.5F, -0.5F, j, k, l, a, 0, 0, packedLightIn);
			vertex(ivertexbuilder, matrix4f, matrix3f, 0.5F, -0.5F, j, k, l, a, 1, 0, packedLightIn);
			vertex(ivertexbuilder, matrix4f, matrix3f, 0.5F, 0.5F, j, k, l, a, 1, 1, packedLightIn);
			vertex(ivertexbuilder, matrix4f, matrix3f, -0.5F, 0.5F, j, k, l, a, 0, 1, packedLightIn);
			matrixStackIn.pop();
			super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		}

		private static void vertex(IVertexBuilder bufferIn, Matrix4f matrixIn, Matrix3f matrixNormalIn, float x, float y, int red, int green,
				int blue, int alpha, float texU, float texV, int packedLight) {
			bufferIn.pos(matrixIn, x, y, 0.0F).color(red, green, blue, alpha).tex(texU, texV).overlay(OverlayTexture.NO_OVERLAY).lightmap(240, 0)
					.normal(matrixNormalIn, 0.0F, 1.0F, 0.0F).endVertex();
		}

		@Override
		public ResourceLocation getEntityTexture(CustomEntity entity) {
			return texture;
		}
	}
}
