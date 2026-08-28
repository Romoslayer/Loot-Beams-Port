package me.clefal.lootbeams.modules.tooltip.nametag;

import me.clefal.lootbeams.LootBeamsConstants;
import me.clefal.lootbeams.config.configs.LootInfomationConfig;
import me.clefal.lootbeams.data.lbitementity.LBItemEntity;
import me.clefal.lootbeams.data.lbitementity.rarity.LBColor;
import me.clefal.lootbeams.data.new_render.LootBeamRenderState;
import me.clefal.lootbeams.duck.PoseCopy;
import me.clefal.lootbeams.events.TooltipsGatherNameAndRarityEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
//? <26.2 {
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
//?}
//? >=26.2 {
/*import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.FormattedCharSequence;
*///?}
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NameTagRenderer {
    //? <26.2 {
    public static void renderNameTag(PoseStack stack, MultiBufferSource buffer, LBItemEntity LBItemEntity){
        PoseCopy last = (PoseCopy) ((Object) stack.last());
        renderNameTag(buffer, LootBeamRenderState.NameTagRenderState.fromLBEntity(LBItemEntity, last.loot_Beams_Refork$copy()));
    }

    public static void renderNameTag(MultiBufferSource buffer, LootBeamRenderState.NameTagRenderState renderState) {
        LootInfomationConfig.nameTagSection nameTagSection = LootInfomationConfig.lootInfomationConfig.nameTag;
        PoseStack.Pose pose = renderState.poseStack;
        PoseStack stack = new PoseStack();
        stack.last().pose().set(pose.pose());
        stack.last().normal().set(pose.normal());
        //If player is crouching or looking at the item
        if (Minecraft.getInstance().player.isCrouching() || ((nameTagSection.render_name_tag_on_look && renderState.isLookingAtThis))) {
            LBColor color = renderState.rarity.color();
            float foregroundAlpha = nameTagSection.name_tag_text_alpha.get();
            float backgroundAlpha = nameTagSection.name_tag_background_alpha.get();
            double yOffset = nameTagSection.name_tag_y_offset.get();
            int foregroundColor = color.changeA(((int) (foregroundAlpha * 255))).argb();
            int backgroundColor = color.changeA(((int) (foregroundAlpha * 255))).argb();
            stack.pushPose();
            //Render nametags at heights based on player distance
            stack.translate(0.0D, Math.min(1D, Minecraft.getInstance().player.distanceToSqr(renderState.location) * 0.025D) + yOffset, 0.0D);
            //? < 1.21.10 {
            stack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
            //? } else {
            /*stack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().camera.rotation());
            *///? }
            //? if > 1.20.1
            stack.mulPose(Axis.YP.rotationDegrees(180));

            float nametagScale = nameTagSection.name_tag_scale.get();
            stack.scale(-0.02F * nametagScale, -0.02F * nametagScale, 0.02F * nametagScale);

            //Render stack counts on nametag
            List<Component> nameAndRarity = renderState.nameAndRarity;
            Font fontrenderer = Minecraft.getInstance().font;
            stack.translate(0, 2, -10);

            for (Component c : nameAndRarity) {
                String s = c.getString();
                if (s.isBlank()) continue;
                renderText(fontrenderer, stack, buffer, s, foregroundColor, backgroundColor, backgroundAlpha);
                stack.translate(0, Minecraft.getInstance().font.lineHeight, 0.0f);

            }


            stack.popPose();


        }


    }

    private static void renderText(Font fontRenderer, PoseStack stack, MultiBufferSource buffer, String text, int foregroundColor, int backgroundColor, float backgroundAlpha) {

        if (LootInfomationConfig.lootInfomationConfig.nameTag.add_text_border) {
            float w = -fontRenderer.width(text) / 2f;
            int bg = new Color(0, 0, 0, (int) (255 * backgroundAlpha)).getRGB();
            Component comp = Component.literal(text);
            fontRenderer.drawInBatch8xOutline(comp.getVisualOrderText(), w, 0f, foregroundColor, bg, stack.last().pose(), buffer, LightTexture.FULL_BRIGHT);
        } else {
            fontRenderer.drawInBatch(text, (float) (-fontRenderer.width(text) / 2), 30f, foregroundColor, false, stack.last().pose(), buffer, Font.DisplayMode.NORMAL, backgroundColor, 15728864);
        }
    }
    //?}

    //? >=26.2 {
    /*public static void renderNameTag(PoseStack stack, SubmitNodeCollector collector, LBItemEntity LBItemEntity){
        PoseCopy last = (PoseCopy) ((Object) stack.last());
        renderNameTag(collector, LootBeamRenderState.NameTagRenderState.fromLBEntity(LBItemEntity, last.loot_Beams_Refork$copy()));
    }

    public static void renderNameTag(SubmitNodeCollector collector, LootBeamRenderState.NameTagRenderState renderState) {
        LootInfomationConfig.nameTagSection nameTagSection = LootInfomationConfig.lootInfomationConfig.nameTag;
        PoseStack.Pose pose = renderState.poseStack;
        PoseStack stack = new PoseStack();
        stack.last().pose().set(pose.pose());
        stack.last().normal().set(pose.normal());
        if (Minecraft.getInstance().player.isCrouching() || ((nameTagSection.render_name_tag_on_look && renderState.isLookingAtThis))) {
            LBColor color = renderState.rarity.color();
            float foregroundAlpha = nameTagSection.name_tag_text_alpha.get();
            float backgroundAlpha = nameTagSection.name_tag_background_alpha.get();
            double yOffset = nameTagSection.name_tag_y_offset.get();
            int foregroundColor = color.changeA(((int) (foregroundAlpha * 255))).argb();
            int backgroundColor = color.changeA(((int) (foregroundAlpha * 255))).argb();
            stack.pushPose();
            stack.translate(0.0D, Math.min(1D, Minecraft.getInstance().player.distanceToSqr(renderState.location) * 0.025D) + yOffset, 0.0D);
            stack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().camera.rotation());
            stack.mulPose(Axis.YP.rotationDegrees(180));

            float nametagScale = nameTagSection.name_tag_scale.get();
            stack.scale(-0.02F * nametagScale, -0.02F * nametagScale, 0.02F * nametagScale);

            List<Component> nameAndRarity = renderState.nameAndRarity;
            Font fontrenderer = Minecraft.getInstance().font;
            stack.translate(0, 2, -10);

            for (Component c : nameAndRarity) {
                String s = c.getString();
                if (s.isBlank()) continue;
                renderText(fontrenderer, stack, collector, s, foregroundColor, backgroundColor, backgroundAlpha);
                stack.translate(0, Minecraft.getInstance().font.lineHeight, 0.0f);
            }

            stack.popPose();
        }
    }

    private static void renderText(Font fontRenderer, PoseStack stack, SubmitNodeCollector collector, String text, int foregroundColor, int backgroundColor, float backgroundAlpha) {
        FormattedCharSequence sequence = Component.literal(text).getVisualOrderText();
        if (LootInfomationConfig.lootInfomationConfig.nameTag.add_text_border) {
            float w = -fontRenderer.width(text) / 2f;
            int bg = new Color(0, 0, 0, (int) (255 * backgroundAlpha)).getRGB();
            collector.submitText(stack, w, 0f, sequence, false, Font.DisplayMode.NORMAL, 0xF000F0, foregroundColor, 0, bg);
        } else {
            collector.submitText(stack, (float) (-fontRenderer.width(text) / 2), 30f, sequence, false, Font.DisplayMode.NORMAL, 15728864, foregroundColor, backgroundColor, 0);
        }
    }
    *///?}



}
