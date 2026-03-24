package com.gto.registrylibtest.client;

import com.gto.registrylibtest.entity.CrystalGuardian;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
/**
 * 晶体守护者的 MobRenderer — 完整骨骼动画渲染流水线。
 *
 * <p>渲染流水线架构：
 * <pre>
 *  MobRenderer&lt;CrystalGuardian, LivingEntityRenderState, CrystalGuardianModel&gt;
 *  │
 *  ├── createRenderState()   → 创建空的 LivingEntityRenderState
 *  ├── extractRenderState()  → 从实体提取 walkAnimPos/Speed、xRot、yRot 等
 *  ├── submit()              → 调用 model.setupAnim(state) 驱动骨骼动画
 *  │                           → submitModel() 提交骨骼网格到渲染队列
 *  └── getTextureLocation()  → 返回纹理路径
 * </pre>
 *
 * <p>关键依赖：
 * <ul>
 *   <li>{@link CrystalGuardianModel} — 提供骨骼定义 + setupAnim()</li>
 *   <li>{@link CrystalGuardianModel#LAYER_LOCATION} — 模型层注册键</li>
 *   <li>纹理 {@code registrylibtest:textures/entity/crystal_guardian.png}</li>
 * </ul>
 */
public class CrystalGuardianRenderer
        extends MobRenderer<CrystalGuardian, LivingEntityRenderState, CrystalGuardianModel> {

    private static final Identifier TEXTURE =
            Identifier.parse("registrylibtest:textures/entity/crystal_guardian.png");

    public CrystalGuardianRenderer(EntityRendererProvider.Context context) {
        // context.bakeLayer() 将 LayerDefinition → ModelPart，传入 Model 构造函数
        super(context, new CrystalGuardianModel(context.bakeLayer(CrystalGuardianModel.LAYER_LOCATION)), 0.4F);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }
}
