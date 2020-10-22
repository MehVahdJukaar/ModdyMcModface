/**
 * This mod element is always locked. Enter your code in the methods below.
 * If you don't need some of these methods, you can remove them as they
 * are overrides of the base class ModdymcmodfaceModElements.ModElement.
 *
 * You can register new events in this class too.
 *
 * As this class is loaded into mod element list, it NEEDS to extend
 * ModElement class. If you remove this extend statement or remove the
 * constructor, the compilation will fail.
 *
 * If you want to make a plain independent class, create it using
 * Project Browser - New... and make sure to make the class
 * outside net.mcreator.moddymcmodface as this package is managed by MCreator.
 *
 * If you change workspace package, modid or prefix, you will need
 * to manually adapt this file to these changes or remake it.
*/
package net.mcreator.moddymcmodface;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.RegistryObject;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.world.World;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.Particle;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.server.command.ModIdArgument;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraft.client.Minecraft;


import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.ActiveRenderInfo;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.util.math.Vec3d;

@ModdymcmodfaceModElements.ModElement.Tag
public class Particles extends ModdymcmodfaceModElements.ModElement {
	/**
	 * Do not remove this constructor
	 */
	public Particles(ModdymcmodfaceModElements instance) {
		super(instance, 136);
	}


	@Override
	public void initElements() {

	}

	@Override
	public void init(FMLCommonSetupEvent event) {
	
	}

	@Override
	public void serverLoad(FMLServerStartingEvent event) {

	}

	
/*

	public static class ParticleList{
		public static final DeferredRegister<ParticleType<?>> PARTICLES = new DeferredRegister<>(ForgeRegistries.PARTICLE_TYPES, "moddymcmodface");
		
		public static final RegistryObject<BasicParticleType> FIREFLY_GLOW = PARTICLES.register("firefly_glow", () -> new BasicParticleType(true)); 
		
	}
	@OnlyIn(Dist.CLIENT)
	public static class FireflyGlow extends SpriteTexturedParticle{
		protected FireflyGlow(World worldIn, double xCoordIn,double yCoordIn,double zCoordIn, double xSpeedIn,double ySpeedIn,double zSpeedIn){
			super(worldIn,  xCoordIn, yCoordIn, zCoordIn,  xSpeedIn, zSpeedIn, zSpeedIn);
			this.particleRed = 255;
			this.particleBlue =0;
			this.particleGreen= 0;
			//used for hitbox. not used
			//this.setSize(0.01F, 0.01F);
			this.particleScale=0.125f;
			//not used
			//this.motionX =0.2d;
			//this.motionY =0.2d;
			//this.motionZ =0.2d;
			this.maxAge =40;
		
		}
	   public float getScale(float partialTicks) {
	      float f = ((float)this.age + partialTicks) / (float)this.maxAge;
	      return this.particleScale * (1-f)*f*4;//(1.0F - f * f * 0.5F);
	   }

		@Override
		public IParticleRenderType getRenderType(){
			return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;	
		}
		
		@Override
		public void tick(){
			//this.prevPosX =this.posX;
			//this.prevPosY =this.posY;
			//this.prevPosZ =this.posZ;
			this.age++;
			if (this.age>this.maxAge){this.setExpired();}
			
		}
		//TODO: add this to the entity
	   public int getBrightnessForRender(float partialTick) {
	      float f = this.getScale(partialTick)/this.particleScale;
	      f = MathHelper.clamp(f, 0.0F, 1.0F);
	      int i = super.getBrightnessForRender(partialTick);
	      int j =(int)( f*240);
	      int k = i >> 16 & 255;
	      if (j > 240) {
	         j = 240;
	      }
	
	      return j | k << 16;
	   }




		@OnlyIn(Dist.CLIENT)
		public static class Factory implements IParticleFactory<BasicParticleType>{
			private final IAnimatedSprite spriteSet;
			
			public Factory(IAnimatedSprite sprite){
				this.spriteSet = sprite;
				
				
			}
			
			@Override
			public Particle makeParticle(BasicParticleType typeIn, World worldIn, double x,double y,double z, double xSpeed,double ySpeed,double zSpeed){
				FireflyGlow op = new FireflyGlow(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
				op.selectSpriteRandomly(this.spriteSet);
				op.setColor(1f,1f,1f);
				return op;
			
			
			}	
			
			

			
		}


		
	}


	@EventBusSubscriber(modid="moddymcmodface", bus = Bus.MOD)
	public static class ParticleUtil{

		@SubscribeEvent(priority=EventPriority.LOWEST)
		public static void registerParticles(ParticleFactoryRegisterEvent event){
			Minecraft.getInstance().particles.registerFactory(ParticleList.FIREFLY_GLOW.get(), FireflyGlow.Factory::new);
			
		}	
	}
*/

	




	
}
