package com.brandon3055.draconicevolution.client.render.item;

import codechicken.lib.math.MathHelper;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.buffer.VBORenderType;
import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.render.shader.ShaderProgram;
import codechicken.lib.render.shader.ShaderRenderType;
import codechicken.lib.render.shader.UniformCache;
import codechicken.lib.render.shader.*;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Vector3;
import com.brandon3055.brandonscore.api.TechLevel;
import com.brandon3055.brandonscore.client.BCClientEventHandler;
import com.brandon3055.draconicevolution.DEConfig;
import com.brandon3055.draconicevolution.DraconicEvolution;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;


import static codechicken.lib.render.shader.ShaderObject.StandardShaderType.FRAGMENT;
import static codechicken.lib.render.shader.ShaderObject.StandardShaderType.VERTEX;
import static codechicken.lib.util.TransformUtils.*;
import static com.brandon3055.draconicevolution.DraconicEvolution.MODID;
import static net.minecraft.client.renderer.RenderState.*;
import static net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType.*;

/**
 * Created by brandon3055 on 22/5/20.
 */
public abstract class ToolRenderBase implements IItemRenderer {

    public static ShaderProgram chaosShader = ShaderProgramBuilder.builder()
            .addShader("vert", shader -> shader
                    .type(VERTEX)
                    .source(new ResourceLocation(MODID, "shaders/chaos.vert"))
            )
            .addShader("frag", shader -> shader
                    .type(FRAGMENT)
                    .source(new ResourceLocation(MODID, "shaders/chaos.frag"))
                    .uniform("alpha", UniformType.FLOAT)
                    .uniform("yaw", UniformType.FLOAT)
                    .uniform("pitch", UniformType.FLOAT)
                    .uniform("time", UniformType.FLOAT)
            )
            .whenUsed(cache -> {
                cache.glUniform1f("alpha", 0.7F);
                Minecraft mc = Minecraft.getInstance();
                cache.glUniform1f("yaw", (float) ((mc.player.rotationYaw * 2 * Math.PI) / 360.0));
                cache.glUniform1f("pitch", -(float) ((mc.player.rotationPitch * 2 * Math.PI) / 360.0));
                cache.glUniform1f("time", (BCClientEventHandler.elapsedTicks + Minecraft.getInstance().getRenderPartialTicks()) / 1);
            })
            .build();

    public static ShaderProgram gemShader = ShaderProgramBuilder.builder()
            .addShader("vert", shader -> shader
                    .type(VERTEX)
                    .source(new ResourceLocation(MODID, "shaders/common.vert"))
            )
            .addShader("frag", shader -> shader
                    .type(FRAGMENT)
                    .source(new ResourceLocation(MODID, "shaders/tool_gem.frag"))
                    .uniform("time", UniformType.FLOAT)
                    .uniform("tier", UniformType.INT)
            )
            .whenUsed(cache -> cache.glUniform1f("time", (BCClientEventHandler.elapsedTicks + Minecraft.getInstance().getRenderPartialTicks()) / 20))
            .build();

    public static ShaderProgram bladeShader = ShaderProgramBuilder.builder()
            .addShader("vert", shader -> shader
                    .type(VERTEX)
                    .source(new ResourceLocation(MODID, "shaders/common.vert"))
            )
            .addShader("frag", shader -> shader
                    .type(FRAGMENT)
                    .source(new ResourceLocation(MODID, "shaders/tool_blade.frag"))
                    .uniform("time", UniformType.FLOAT)
                    .uniform("tier", UniformType.INT)
            )
            .whenUsed(cache -> cache.glUniform1f("time", (BCClientEventHandler.elapsedTicks + Minecraft.getInstance().getRenderPartialTicks()) / 20))
            .build();

    public static ShaderProgram traceShader = ShaderProgramBuilder.builder()
            .addShader("vert", shader -> shader
                    .type(VERTEX)
                    .source(new ResourceLocation(MODID, "shaders/common.vert"))
            )
            .addShader("frag", shader -> shader
                    .type(FRAGMENT)
                    .source(new ResourceLocation(MODID, "shaders/tool_trace.frag"))
                    .uniform("time", UniformType.FLOAT)
                    .uniform("tier", UniformType.INT)
            )
            .whenUsed(cache -> cache.glUniform1f("time", (BCClientEventHandler.elapsedTicks + Minecraft.getInstance().getRenderPartialTicks()) / 20))
            .build();


    public RenderType modelType;
    public RenderType modelGuiType;
    public RenderType chaosType;
    public RenderType shaderParentType;

    public CCModel baseModel;            //These parts will always be rendered solid using the model texture.
    public CCModel materialModel;        //These are parts like the head that are made out of the base material and will have the chaos shader applied if tech level is chaos.
    public CCModel traceModel;           //These are the shaded model "inlays" on the handles of most tools
    public CCModel bladeModel;
    public CCModel gemModel;

    public VBORenderType baseVBOType;
    public VBORenderType guiBaseVBOType;
    public VBORenderType materialVBOType;
    public VBORenderType materialChaosVBOType;
    public VBORenderType guiMaterialVBOType;
    public VBORenderType traceVBOType;
    public VBORenderType bladeVBOType;
    public VBORenderType gemVBOType;

    public TechLevel techLevel;

    public ToolRenderBase(TechLevel techLevel, String tool) {
        this.techLevel = techLevel;
        String levelName = techLevel.name().toLowerCase();
        modelType = RenderType.makeType("modelType", DefaultVertexFormats.BLOCK, GL11.GL_TRIANGLES, 256, true, false, RenderType.State.getBuilder()
                .texture(new RenderState.TextureState(new ResourceLocation(DraconicEvolution.MODID, "textures/models/item/equipment/" + levelName + "_" + tool + ".png"), false, false))
                .diffuseLighting(DIFFUSE_LIGHTING_ENABLED)
                .lightmap(LIGHTMAP_ENABLED)
                .build(true));

        modelGuiType = RenderType.makeType("modelGuiType", DefaultVertexFormats.BLOCK, GL11.GL_TRIANGLES, 256, RenderType.State.getBuilder()
                .texture(new RenderState.TextureState(new ResourceLocation(DraconicEvolution.MODID, "textures/models/item/equipment/" + levelName + "_" + tool + ".png"), false, false))
                .lightmap(LIGHTMAP_ENABLED)
                .overlay(OVERLAY_DISABLED)
                .build(false)
        );

        chaosType = RenderType.makeType("chaosShaderType", DefaultVertexFormats.BLOCK, GL11.GL_TRIANGLES, 256, RenderType.State.getBuilder()
                .texture(new RenderState.TextureState(new ResourceLocation(DraconicEvolution.MODID, "textures/models/item/equipment/chaos_shader.png"), true, false))
                .lightmap(LIGHTMAP_ENABLED)
                .overlay(OVERLAY_ENABLED)
                .build(false)
        );

        shaderParentType = RenderType.makeType("shaderGemType", DefaultVertexFormats.BLOCK, GL11.GL_TRIANGLES, 256, RenderType.State.getBuilder()
                .texture(new RenderState.TextureState(new ResourceLocation(DraconicEvolution.MODID, "textures/models/item/equipment/shader_fallback_" + levelName + ".png"), false, false))
                .lightmap(LIGHTMAP_ENABLED)
                .overlay(OVERLAY_ENABLED)
                .build(false)
        );
    }

    @Override
    public void renderItem(ItemStack stack, TransformType transformType, MatrixStack mStack, IRenderTypeBuffer getter, int packedLight, int packedOverlay) {
        Matrix4 mat = new Matrix4(mStack);
        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        ccrs.brightness = 240;//packedLight;
        ccrs.overlay = packedOverlay;
        renderTool(ccrs, stack, transformType, mat, mStack, getter, transformType == GUI, packedLight);
    }

    public abstract void renderTool(CCRenderState ccrs, ItemStack stack, TransformType transform, Matrix4 mat, MatrixStack mStack, IRenderTypeBuffer getter, boolean gui, int packedLight);

    public void transform(Matrix4 mat, double x, double y, double z, double scale) {
        mat.translate(x, y, z);
        mat.rotate(MathHelper.torad * 90, Vector3.Y_NEG);
        mat.rotate(MathHelper.torad * 45, Vector3.X_POS);
        mat.scale(scale);
    }

    @Override
    public ImmutableMap<TransformType, TransformationMatrix> getTransforms() {
        return DEFAULT_TOOL;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean func_230044_c_() {
        return false;
    }

    public static ShaderRenderType getShaderType(RenderType parent, TechLevel techLevel, ShaderProgram shader) {
        UniformCache uniforms = shader.pushCache();
        uniforms.glUniform1i("tier", techLevel.index);
        return new ShaderRenderType(parent, shader, uniforms);
    }

    public static ShaderRenderType getShaderType(RenderType parent, ShaderProgram shader) {
        return new ShaderRenderType(parent, shader, shader.pushCache());
    }

//    @Deprecated
//    public void bindShaderType(CCRenderState ccrs, IRenderTypeBuffer getter, TechLevel techLevel, ShaderProgram shader, RenderType renderType) {
//        if (DEConfig.toolShaders) {
//            UniformCache uniforms = shader.pushCache();
//            uniforms.glUniform1i("tier", techLevel.index);
//            renderType = new ShaderRenderType(renderType, shader, uniforms);
//        }
//        ccrs.bind(renderType, getter);
//    }
//
//    @Deprecated
//    public void bindShader(CCRenderState ccrs, IRenderTypeBuffer getter, ShaderProgram shader, RenderType renderType) {
//        if (DEConfig.toolShaders) {
//            renderType = new ShaderRenderType(renderType, shader, shader.pushCache());
//        }
//        ccrs.bind(renderType, getter);
//    }

    public void initBaseVBO() {
        baseVBOType = new VBORenderType(modelType, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL, (format, builder) -> {
            CCRenderState ccrs = CCRenderState.instance();
            ccrs.reset();
            ccrs.bind(builder, format);
            baseModel.render(ccrs);
        });

        guiBaseVBOType = new VBORenderType(modelGuiType, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL, (format, builder) -> {
            CCRenderState ccrs = CCRenderState.instance();
            ccrs.reset();
            ccrs.bind(builder, format);
            baseModel.render(ccrs);
        });
    }

    public void initMaterialVBO() {
        materialVBOType = new VBORenderType(modelType, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL, (format, builder) -> {
            CCRenderState ccrs = CCRenderState.instance();
            ccrs.reset();
            ccrs.bind(builder, format);
            materialModel.render(ccrs);
        });

        guiMaterialVBOType = new VBORenderType(modelGuiType, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL, (format, builder) -> {
            CCRenderState ccrs = CCRenderState.instance();
            ccrs.reset();
            ccrs.bind(builder, format);
            materialModel.render(ccrs);
        });

        materialChaosVBOType = new VBORenderType(chaosType, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL, (format, builder) -> {
            CCRenderState ccrs = CCRenderState.instance();
            ccrs.reset();
            ccrs.bind(builder, format);
            materialModel.render(ccrs);
        });

    }

    public void initTraceVBO() {
        traceVBOType = new VBORenderType(shaderParentType, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL, (format, builder) -> {
            CCRenderState ccrs = CCRenderState.instance();
            ccrs.reset();
            ccrs.bind(builder, format);
            traceModel.render(ccrs);
        });
    }

    public void initBladeVBO() {
        bladeVBOType = new VBORenderType(shaderParentType, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL, (format, builder) -> {
            CCRenderState ccrs = CCRenderState.instance();
            ccrs.reset();
            ccrs.bind(builder, format);
            bladeModel.render(ccrs);
        });
    }

    public void initGemVBO() {
        gemVBOType = new VBORenderType(shaderParentType, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL, (format, builder) -> {
            CCRenderState ccrs = CCRenderState.instance();
            ccrs.reset();
            ccrs.bind(builder, format);
            gemModel.render(ccrs);
        });
    }
}
