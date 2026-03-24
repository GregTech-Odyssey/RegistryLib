package com.gto.registrylibtest.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 晶体守护者的骨骼模型 — 展示完整的 ModelPart / LayerDefinition / 骨骼动画流水线。
 *
 * <p>骨骼结构：
 * <pre>
 *  root
 *  ├── head       (6×6×6 立方体，带发光晶体角)
 *  ├── body       (6×8×4 躯干)
 *  ├── left_arm   (3×8×3 左臂)
 *  ├── right_arm  (3×8×3 右臂)
 *  ├── left_leg   (3×6×3 左腿)
 *  └── right_leg  (3×6×3 右腿)
 * </pre>
 *
 * <p>动画在 {@link #setupAnim(LivingEntityRenderState)} 中通过直接操作 {@link ModelPart} 的
 * 旋转 / 位移字段实现。这是 Minecraft 传统的「代码驱动」动画方式。
 */
@OnlyIn(Dist.CLIENT)
public class CrystalGuardianModel extends EntityModel<LivingEntityRenderState> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.parse("registrylibtest:crystal_guardian"), "main");

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public CrystalGuardianModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    /**
     * 创建模型层定义 — 在 {@code EntityRenderersEvent.RegisterLayerDefinitions} 时调用。
     *
     * <p>流程：
     * <ol>
     *   <li>{@link MeshDefinition} — 根网格容器</li>
     *   <li>{@link PartDefinition#addOrReplaceChild} — 添加骨骼节点</li>
     *   <li>{@link CubeListBuilder} — 定义该骨骼的立方体列表</li>
     *   <li>{@link PartPose} — 骨骼的初始位姿（偏移 + 旋转）</li>
     *   <li>{@link LayerDefinition#create} — 封装为最终定义（含纹理尺寸）</li>
     * </ol>
     *
     * @return 可被 {@code context.bakeLayer()} 烘焙为 {@link ModelPart} 的层定义
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // 头部：6×6×6，居中于身体上方
        // PartPose.offset(x, y, z) 设置骨骼原点相对于父骨骼的偏移
        // y=0 表示模型顶部（MC 模型 Y 轴向下为正）
        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F, CubeDeformation.NONE)
                        // 晶体角装饰（左上 + 右上小凸起）
                        .texOffs(24, 0)
                        .addBox(-1.0F, -8.0F, -1.0F, 2.0F, 2.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 4.0F, 0.0F));

        // 躯干：6×8×4
        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 12)
                        .addBox(-3.0F, 0.0F, -2.0F, 6.0F, 8.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 4.0F, 0.0F));

        // 右臂：3×8×3，挂在躯干右侧
        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(20, 12)
                        .addBox(-2.0F, -1.0F, -1.5F, 3.0F, 8.0F, 3.0F, CubeDeformation.NONE),
                PartPose.offset(-4.5F, 5.0F, 0.0F));

        // 左臂：3×8×3（镜像）
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(32, 12)
                        .addBox(-1.0F, -1.0F, -1.5F, 3.0F, 8.0F, 3.0F, CubeDeformation.NONE),
                PartPose.offset(4.5F, 5.0F, 0.0F));

        // 右腿：3×6×3
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 24)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, CubeDeformation.NONE),
                PartPose.offset(-1.5F, 12.0F, 0.0F));

        // 左腿：3×6×3
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(12, 24)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, CubeDeformation.NONE),
                PartPose.offset(1.5F, 12.0F, 0.0F));

        // 纹理尺寸 48×36（覆盖所有 UV 区域）
        return LayerDefinition.create(mesh, 48, 36);
    }

    /**
     * 每帧调用，根据实体状态设置骨骼旋转/位移 — 即「代码驱动动画」。
     *
     * <p>{@link LivingEntityRenderState} 由 {@code LivingEntityRenderer.extractRenderState()} 提供，
     * 包含 walkAnimationPos/Speed、xRot、yRot 等插值后的状态量。
     *
     * <p>关键公式：
     * <ul>
     *   <li>行走摆臂/摆腿：{@code cos(walkPos * 0.6662) * factor * walkSpeed}</li>
     *   <li>头部跟随：{@code xRot / (180/PI)}, {@code yRot / (180/PI)}</li>
     * </ul>
     */
    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);

        // 头部跟随视角
        head.yRot = state.yRot * Mth.DEG_TO_RAD;
        head.xRot = state.xRot * Mth.DEG_TO_RAD;

        // 行走摆臂（左右反相）
        float walkPos = state.walkAnimationPos;
        float walkSpeed = state.walkAnimationSpeed;
        rightArm.xRot = Mth.cos(walkPos * 0.6662F + Mth.PI) * 1.4F * walkSpeed;
        leftArm.xRot = Mth.cos(walkPos * 0.6662F) * 1.4F * walkSpeed;

        // 行走摆腿（左右反相）
        rightLeg.xRot = Mth.cos(walkPos * 0.6662F) * 1.0F * walkSpeed;
        leftLeg.xRot = Mth.cos(walkPos * 0.6662F + Mth.PI) * 1.0F * walkSpeed;
    }
}
