package com.brandon3055.draconicevolution.client.model;

import codechicken.lib.util.SneakyUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ArmorLayer;
import net.minecraft.client.renderer.entity.model.AbstractZombieModel;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;

/**
 * Created by brandon3055 on 30/6/20
 */
public class VBOArmorLayer<T extends LivingEntity, M extends BipedModel<T>, A extends BipedModel<T>> extends ArmorLayer<T, M, A> {

    public VBOArmorLayer(IEntityRenderer<T, M> renderer, @Nullable ArmorLayer<T, M, A> parent) {
        super(renderer, parent == null ? SneakyUtils.unsafeCast(new BipedModel<T>(0.5F)) : parent.modelLeggings, parent == null ? SneakyUtils.unsafeCast(new BipedModel<T>(1.0F)) : parent.modelArmor);
    }

    protected void setModelSlotVisible(A model, EquipmentSlotType slot) {
        this.setModelVisible(model);
        switch (slot) {
            case HEAD:
                model.bipedHead.showModel = true;
                model.bipedHeadwear.showModel = true;
                break;
            case CHEST:
                model.bipedBody.showModel = true;
                model.bipedRightArm.showModel = true;
                model.bipedLeftArm.showModel = true;
                break;
            case LEGS:
                model.bipedBody.showModel = true;
                model.bipedRightLeg.showModel = true;
                model.bipedLeftLeg.showModel = true;
                break;
            case FEET:
                model.bipedRightLeg.showModel = true;
                model.bipedLeftLeg.showModel = true;
        }

    }

    protected void setModelVisible(A model) {
        model.setVisible(false);
    }

    @Override
    protected A getArmorModelHook(T entity, net.minecraft.item.ItemStack itemStack, EquipmentSlotType slot, A model) {
        return net.minecraftforge.client.ForgeHooksClient.getArmorModel(entity, itemStack, slot, model);
    }


    @Override
    public void render(MatrixStack mStack, IRenderTypeBuffer getter, int packedLightIn, T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        this.renderArmorPart(mStack, getter, livingEntity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, EquipmentSlotType.CHEST, packedLightIn);
        this.renderArmorPart(mStack, getter, livingEntity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, EquipmentSlotType.LEGS, packedLightIn);
        this.renderArmorPart(mStack, getter, livingEntity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, EquipmentSlotType.FEET, packedLightIn);
        this.renderArmorPart(mStack, getter, livingEntity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, EquipmentSlotType.HEAD, packedLightIn);
    }

    private void renderArmorPart(MatrixStack mStack, IRenderTypeBuffer getter, T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, EquipmentSlotType slot, int packedLight) {
        ItemStack itemstack = livingEntity.getItemStackFromSlot(slot);
        if (itemstack.getItem() instanceof ArmorItem) {
            ArmorItem armoritem = (ArmorItem) itemstack.getItem();
            if (armoritem.getEquipmentSlot() == slot) {
                A baseModel = this.getModelFromSlot(slot);
                A model = baseModel;
                model = getArmorModelHook(livingEntity, itemstack, slot, model);
                if (model instanceof VBOBipedModel) {
                    if (baseModel instanceof AbstractZombieModel) {
                        baseModel.setRotationAngles(livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
                        ((VBOBipedModel) model).bipedRightArm.rotateAngleZ = baseModel.bipedRightArm.rotateAngleZ;
                        ((VBOBipedModel) model).bipedLeftArm.rotateAngleZ = baseModel.bipedLeftArm.rotateAngleZ;
                        ((VBOBipedModel) model).bipedRightArm.rotateAngleY = baseModel.bipedRightArm.rotateAngleY;
                        ((VBOBipedModel) model).bipedLeftArm.rotateAngleY = baseModel.bipedLeftArm.rotateAngleY;
                        ((VBOBipedModel) model).bipedRightArm.rotateAngleX = baseModel.bipedRightArm.rotateAngleX;
                        ((VBOBipedModel) model).bipedLeftArm.rotateAngleX = baseModel.bipedLeftArm.rotateAngleX;
                    }
                    //Dont need this because this model is still "rendered" by the vanilla armor layer which handles all of this.
//                    this.getEntityModel().setModelAttributes(model);
//                    model.setLivingAnimations(livingEntity, limbSwing, limbSwingAmount, partialTicks);
//                    this.setModelSlotVisible(model, slot);
//                    model.setRotationAngles(livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
                    ((VBOBipedModel<T>) model).render(mStack, getter, livingEntity, itemstack, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
                }
            }
        }
    }
}
