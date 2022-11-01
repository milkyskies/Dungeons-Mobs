package com.infamous.dungeons_mobs.client.renderer.armor;

import com.infamous.dungeons_libraries.client.renderer.ArmorGearRenderer;
import com.infamous.dungeons_libraries.entities.SpawnArmoredMob;
import com.infamous.dungeons_libraries.items.materials.armor.ArmorMaterialBaseType;
import com.infamous.dungeons_libraries.items.materials.armor.DungeonsArmorMaterial;
import com.infamous.dungeons_mobs.entities.illagers.WindcallerEntity;
import com.infamous.dungeons_mobs.items.armor.WindcallerClothesArmorGear;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.IArmorMaterial;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.util.RenderUtils;

public class WindcallerClothesArmorGearRenderer extends ArmorGearRenderer<WindcallerClothesArmorGear> {

    @Override
    public void preparePositionRotationScale(GeoBone bone, MatrixStack stack) {
        RenderUtils.translate(bone, stack);
        RenderUtils.moveToPivot(bone, stack);
        EntityRenderer<? super LivingEntity> entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entityLiving);
        if(!(entityRenderer instanceof GeoEntityRenderer) || !bone.getName().contains("armor")) {
            RenderUtils.rotate(bone, stack);
        }
        RenderUtils.scale(bone, stack);
        IArmorMaterial material = this.currentArmorItem.getMaterial();
        if(bone.getName().contains("Body") && material instanceof DungeonsArmorMaterial && ((DungeonsArmorMaterial) material).getBaseType() == ArmorMaterialBaseType.CLOTH){
            stack.scale(1.0F, 1.0F, 0.85F);
        }
        if(entityLiving instanceof WindcallerEntity && bone.getName().contains("Head")){
            stack.scale(0.93F, 0.93F, 0.93F);
            stack.translate(0.0D, 0.116D, 0.0D);
        }
        RenderUtils.moveBackFromPivot(bone, stack);
    }
}
