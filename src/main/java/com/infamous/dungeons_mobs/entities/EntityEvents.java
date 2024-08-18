package com.infamous.dungeons_mobs.entities;

import com.infamous.dungeons_mobs.config.DungeonsMobsConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.*;
import net.minecraftforge.event.level.BlockEvent.BlockToolModificationEvent;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;

import static com.infamous.dungeons_mobs.DungeonsMobs.MODID;
import static com.infamous.dungeons_mobs.mod.ModEffects.ENSNARED;
import static net.minecraft.world.entity.EntityType.HUSK;

@Mod.EventBusSubscriber(modid = MODID)
public class EntityEvents {

	@SubscribeEvent
	public static void changeAttributes(EntityJoinLevelEvent event) {
		// Tougher Husks
		if (event.getEntity().getType().equals(HUSK) && event.getEntity() instanceof LivingEntity livingEntity && DungeonsMobsConfig.COMMON.ENABLE_STRONGER_HUSKS.get()) {
			AttributeInstance attribute = livingEntity.getAttribute(Attributes.ARMOR);
			if (attribute != null) {
				attribute.setBaseValue(10.0D);
			}
			attribute = livingEntity.getAttribute(Attributes.MOVEMENT_SPEED);
			if (attribute != null) {
				attribute.setBaseValue(0.17D);
			}
			attribute = livingEntity.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
			if (attribute != null) {
				attribute.setBaseValue(0.6D);
			}
		}
	}

	//TODO Pack and organize -- Meme Man
	@SubscribeEvent
	public static void preventKnockback(LivingKnockBackEvent event) {
		LivingEntity owner = event.getEntity();

		if (owner.hasEffect(ENSNARED.get())) event.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void preventBlockBreaking(BreakEvent event) {
		Player owner = event.getPlayer();
		
		if (owner != null && owner.hasEffect(ENSNARED.get())) {
			// Prevent crashes, cause sometimes it isn't cancellable
			if (event.isCancelable()) event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public static void preventBlockPlacement(EntityPlaceEvent event) {
		Entity owner = event.getEntity();
		
		if (owner instanceof LivingEntity) {
			if (((LivingEntity) owner).hasEffect(ENSNARED.get())) {
				if (event.isCancelable()) event.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent
	public static void preventBlockInteraction(BlockToolModificationEvent event) {
		Player owner = event.getPlayer();
		
		if (owner != null && owner.hasEffect(ENSNARED.get())) {
			if (event.isCancelable()) event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public static void preventItemUseCustom(LivingEntityUseItemEvent event) {
		LivingEntity owner = event.getEntity();
		
		if (owner.hasEffect(ENSNARED.get())) {
			if (event.isCancelable()) event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public static void preventEntityAttack(AttackEntityEvent event) {
		Player owner = event.getEntity();
		
		if (owner.hasEffect(ENSNARED.get())) {
			if (event.isCancelable()) event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public static void preventBucketFill(FillBucketEvent event) {
		Player owner = event.getEntity();
		
		if (owner.hasEffect(ENSNARED.get())) {
			if (event.isCancelable()) event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public static void preventEmptyInteraction(RightClickEmpty event) {
		Player owner = event.getEntity();
		
		if (owner.hasEffect(ENSNARED.get())) {
			if (event.isCancelable()) event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public static void preventEmptyInteraction(LeftClickEmpty event) {
		Player owner = event.getEntity();
		
		if (owner.hasEffect(ENSNARED.get())) {
			if (event.isCancelable()) event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public static void preventItemUse(RightClickItem event) {
		Player owner = event.getEntity();
		
		if (owner.hasEffect(ENSNARED.get())) {
			if (event.isCancelable()) event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public static void preventBlockInteraction(RightClickBlock event) {
		Player owner = event.getEntity();
		
		if (owner.hasEffect(ENSNARED.get())) {
			if (event.isCancelable()) event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public static void preventBlockInteraction(LeftClickBlock event) {
		Player owner = event.getEntity();
		
		if (owner.hasEffect(ENSNARED.get())) {
			if (event.isCancelable()) event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void addEndermanSpawns(LevelEvent.PotentialSpawns event) {
		if (event.getLevel() instanceof Level level && level.dimensionTypeRegistration().is(BuiltinDimensionTypes.END)) {
			int reduce = event.getSpawnerDataList().stream().filter(spawnerData -> spawnerData.type.equals(EntityType.ENDERMAN)).map(spawnerData -> spawnerData.getWeight().asInt()).reduce(Integer::sum).orElse(100);
			if (reduce < 100) {
					event.addSpawnerData(new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 100-reduce, 4, 4));
			}
		}
	}
}
