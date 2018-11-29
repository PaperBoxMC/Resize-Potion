package com.camellias.resizer.potions.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.camellias.resizer.Main;
import com.camellias.resizer.capability.SimpleCapability;

import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PotionHandler extends SimpleCapability
{
	@CapabilityInject(value = PotionHandler.class)
	public static final Capability<PotionHandler> CAPABILITY = null;
	
	public static final PotionHandler INSTANCE = new PotionHandler();
	
	public int scale;
	
	@Override
	public boolean isRelevantFor(Entity entity)
	{
		return entity instanceof EntityPlayer;
	}
	
	@Override
	public SimpleCapability getNewInstance()
	{
		return INSTANCE;
	}
	
	public static Method setSize = ReflectionHelper.findMethod(Entity.class, "setSize", "func_70105_a", float.class, float.class);
	
	@SubscribeEvent
	public static void onPlayerUpdate(TickEvent.PlayerTickEvent event)
	{
		//----Thank you to XzeroAir from the MMD Discord for helping out with the hitbox changes. Life saver, that guy.----//
		
		EntityPlayer player = event.player;
		PotionEffect growth = player.getActivePotionEffect(Main.GROWTH);
		
		if(player.isPotionActive(Main.GROWTH))
		{
			player.height = 1.8F + (growth.getAmplifier() + 1F);
			player.width = player.height * (1F / 3F);
			AxisAlignedBB aabb = player.getEntityBoundingBox();
			double d0 = (double)player.width / 2.0D;
			
			player.eyeHeight = player.height * 0.9F;
			player.stepHeight = player.height / 3F;
			
			if(player.isPotionActive(Main.SHRINKING))
			{
				player.removeActivePotionEffect(Main.SHRINKING);
			}
			
			try
			{
				setSize.invoke(player, player.width, player.height);
			}
			catch (IllegalAccessException e)
			{
				e.printStackTrace();
			}
			catch (IllegalArgumentException e)
			{
				e.printStackTrace();
			}
			catch (InvocationTargetException e)
			{
				e.printStackTrace();
			}
            
			player.setEntityBoundingBox(new AxisAlignedBB(player.posX - d0, aabb.minY, player.posZ - d0, 
            		player.posX + d0, aabb.minY + (double)player.height, player.posZ + d0));
		}
		
		PotionEffect shrinking = player.getActivePotionEffect(Main.SHRINKING);
		
		if(player.isPotionActive(Main.SHRINKING))
		{
			player.height = 0.9F / (shrinking.getAmplifier() + 1);
			player.width = player.height * (1F / 3F);
			AxisAlignedBB aabb = player.getEntityBoundingBox();
			double d0 = (double)player.width / 2.0D;
			
			player.eyeHeight = player.height * 0.85F;
			player.stepHeight = player.height / 3F;
			player.jumpMovementFactor *= 1.75F;
			player.fallDistance = 0.0F;
			
			try
			{
				setSize.invoke(player, player.width, player.height);
			}
			catch (IllegalAccessException e)
			{
				e.printStackTrace();
			}
			catch (IllegalArgumentException e)
			{
				e.printStackTrace();
			}
			catch (InvocationTargetException e)
			{
				e.printStackTrace();
			}
			
			player.setEntityBoundingBox(new AxisAlignedBB(player.posX - d0, aabb.minY, player.posZ - d0, 
            		player.posX + d0, aabb.minY + (double)player.height, player.posZ + d0));
		}
		
		if(player.isPotionActive(Main.GROWTH) == false && player.isPotionActive(Main.SHRINKING) == false)
		{
			player.eyeHeight = player.getDefaultEyeHeight();
			player.stepHeight = 0.6F;
		}
	}
	
	@SubscribeEvent
	public static void onPlayerJump(LivingJumpEvent event)
	{
		EntityLivingBase entity = event.getEntityLiving();
		PotionEffect potion = entity.getActivePotionEffect(Main.SHRINKING);
		
		if(entity.isPotionActive(Main.SHRINKING))
		{
			if(potion.getAmplifier() == 0)
			{
				entity.motionY += potion.getAmplifier() + 0.25D;
			}
			else
			{
				entity.motionY += potion.getAmplifier() / 2.0D;
			}
		}
	}
	
	
	
	
	
//-------------------------------------------------------------------------------------------------------------------------//
	
	
	
	
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void onPlayerRenderPre(RenderPlayerEvent.Pre event)
	{
		//----Thank you Melonslise from the MMD Discord for helping getting the rendering working properly.----//
		
		EntityPlayer player = event.getEntityPlayer();
		ModelPlayer model = event.getRenderer().getMainModel();
		PotionEffect growth = player.getActivePotionEffect(Main.GROWTH);
		PotionEffect shrinking = player.getActivePotionEffect(Main.SHRINKING);
		
		if(player.isPotionActive(Main.GROWTH))
		{
			if(growth.getAmplifier() == 0)
			{
				float scale = 1.5F;
				
				GlStateManager.pushMatrix();
				GlStateManager.scale(scale, scale, scale);
				GlStateManager.translate((event.getX() / scale) - event.getX(), 
						(event.getY() / scale) - event.getY(), (event.getZ() / scale) - event.getZ());
			}
			else
			{
				float scale = growth.getAmplifier() + 1.0F;
				
				GlStateManager.pushMatrix();
				GlStateManager.scale(scale, scale, scale);
				GlStateManager.translate((event.getX() / scale) - event.getX(), 
						(event.getY() / scale) - event.getY(), (event.getZ() / scale) - event.getZ());
			}
		}
		
		if(player.isPotionActive(Main.SHRINKING))
		{
			if(shrinking.getAmplifier() == 0)
			{
				float scale = 0.5F;
				
				GlStateManager.pushMatrix();
				GlStateManager.scale(scale, scale, scale);
				GlStateManager.translate((event.getX() / scale) - event.getX(), 
						(event.getY() / scale) - event.getY(), (event.getZ() / scale) - event.getZ());
			}
			else
			{
				float scale = (float) Math.pow(0.25D, shrinking.getAmplifier());
				
				GlStateManager.pushMatrix();
				GlStateManager.scale(scale, scale, scale);
				GlStateManager.translate((event.getX() / scale) - event.getX(), 
						(event.getY() / scale) - event.getY(), (event.getZ() / scale) - event.getZ());
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void onPlayerRenderPost(RenderPlayerEvent.Post event)
	{
		if(event.getEntityPlayer().isPotionActive(Main.GROWTH) || 
				event.getEntityPlayer().isPotionActive(Main.SHRINKING))
		{
			GlStateManager.popMatrix();
		}
	}
}