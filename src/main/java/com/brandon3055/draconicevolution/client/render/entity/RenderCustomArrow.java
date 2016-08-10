package com.brandon3055.draconicevolution.client.render.entity;

import codechicken.lib.render.TextureUtils;
import codechicken.lib.render.TransformUtils;
import com.brandon3055.brandonscore.lib.FullAtlasSprite;
import com.brandon3055.brandonscore.utils.ModelUtils;
import com.brandon3055.draconicevolution.entity.EntityCustomArrow;
import com.brandon3055.draconicevolution.helpers.ResourceHelperDE;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

/**
 * Created by brandon3055 on 3/3/2016.
 */
public class RenderCustomArrow extends Render<EntityCustomArrow> {
    private IBakedModel arrowModel = null;
    private TextureAtlasSprite fullSprite = new FullAtlasSprite();

    protected RenderCustomArrow(RenderManager renderManager) {
        super(renderManager);
        try {
            arrowModel = OBJLoader.INSTANCE.loadModel(ResourceHelperDE.getResource("models/item/tools/arrowCommon.obj")).bake(TransformUtils.DEFAULT_ITEM, DefaultVertexFormats.ITEM, TextureUtils.bakedTextureGetter);
                    //new Function<ResourceLocation, TextureAtlasSprite>() {
//                @Nullable
//                @Override
//                public TextureAtlasSprite apply(ResourceLocation input) {
//                    return input.equals(ResourceHelperDE.getResource("items/tools/obj/arrowCommon")) ? TextureUtils.bakedTextureGetter.apply(input) : fullSprite;
//                }
//            });
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void doRender(EntityCustomArrow entityArrow, double x, double y, double z, float f1, float f2) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.rotate(entityArrow.prevRotationYaw + (entityArrow.rotationYaw - entityArrow.prevRotationYaw) * f2 - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(entityArrow.prevRotationPitch + (entityArrow.rotationPitch - entityArrow.prevRotationPitch) * f2, 0.0F, 0.0F, 1.0F);

        float f10 = 0.3F;
        float f11 = (float) entityArrow.arrowShake - f2;

        if (f11 > 0.0F) {
            float f12 = -MathHelper.sin(f11 * 3.0F) * f11;
            GlStateManager.rotate(f12, 0.0F, 0.0F, 1.0F);
        }

        GlStateManager.rotate(90.0F, 0.0F, -1.0F, 0.0F);
        GlStateManager.scale(f10, f10, f10);
        GlStateManager.enableBlend();
        GlStateManager.enableRescaleNormal();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1F, 0F, 0F, 0.6F);

        if (entityArrow.bowProperties != null && entityArrow.bowProperties.energyBolt) {
            bindEntityTexture(entityArrow);
            ModelUtils.renderQuads(arrowModel.getQuads(null, null, 0));

            GlStateManager.translate(0, -0.025, 0);
            GlStateManager.color(1F, 1F, 1F, 0.6F);
            GlStateManager.scale(1.05, 1.05, 1.05);

            GlStateManager.color(1F, 1F, 1F, 0.4F);
            GlStateManager.scale(1.05, 1.05, 1.05);
            ModelUtils.renderQuads(arrowModel.getQuads(null, null, 0));
        } else {
            TextureUtils.bindBlockTexture();
            ModelUtils.renderQuads(arrowModel.getQuads(null, null, 0));
        }

        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

    }


    @Override
    protected ResourceLocation getEntityTexture(EntityCustomArrow arrow) {
        return arrow.bowProperties.energyBolt ? ResourceHelperDE.getResource("textures/models/reactorCore.png") : ResourceHelperDE.getResource("items/tools/obj/arrowCommon");
    }

    public static class Factory implements IRenderFactory<EntityCustomArrow> {
        @Override
        public Render<? super EntityCustomArrow> createRenderFor(RenderManager manager) {
            return new RenderCustomArrow(manager);
        }
    }
}
