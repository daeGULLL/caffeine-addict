package com.caffeineaddict.caffeineaddictmode.blocks.CoffeeMachine;

import static com.caffeineaddict.caffeineaddictmode.CaffeineAddictMode.MOD_ID;

import com.caffeineaddict.caffeineaddictmode.CaffeineAddictMode;
import com.caffeineaddict.caffeineaddictmode.blocks.CoffeeMachine.network.BrewRequestPacket;
import com.caffeineaddict.caffeineaddictmode.registry.ModNetwork;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class CoffeeMachineScreen extends AbstractContainerScreen<CoffeeMachineMenu> {

    private static final ResourceLocation GUI = new ResourceLocation(MOD_ID, "/textures/gui/shotmachine_v1.png");
    private static final ResourceLocation PLAYER_INVENTORY_TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");

    public CoffeeMachineScreen(CoffeeMachineMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 200;
        this.imageHeight = 172;
        this.inventoryLabelY = 140 + 6;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        RenderSystem.setShaderTexture(0, PLAYER_INVENTORY_TEXTURE);
        this.blit(poseStack, leftPos+20, topPos+20, 0, 0, 162, 86);


        RenderSystem.setShaderTexture(0, GUI);

        this.blit(poseStack, leftPos, topPos-30, 0, 0, imageWidth, imageHeight);

        // Draw animated gauge bar for each input slot
        for (int i = 0; i < 3; i++) {
            int fill = menu.getProgressForSlot(i); // progress: 0-24
            int barWidth = (int)(22 * fill / 25.0); // 13 px max bar height
            int barX = leftPos + (i == 0 ? 85 : 143);
            int barY = topPos + 43;
            int texU = (i == 0 ? 85 : 143); // Same as barX if drawn from background directly
            int texV = 43;
            this.blit(poseStack, barX, barY,
                    texU, texV, barWidth, 10); // x, y, u, v, width, height
        }
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        this.font.draw(poseStack, this.title, 8, 2, 4210752);
        this.font.draw(poseStack, this.playerInventoryTitle, 8, imageHeight - 94, 4210752);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    private void sendBrewRequest(int idx) {
        ModNetwork.sendToServer(new BrewRequestPacket(idx));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Log pressed key for debugging
        CaffeineAddictMode.LOGGER.info("[DEBUG] keyPressed: " + keyCode);

        if (keyCode == GLFW.GLFW_KEY_1) {
            CaffeineAddictMode.LOGGER.info("[DEBUG] Brew key 1 pressed!");
            sendBrewRequest(0);
            return true; // mark as handled
        }
        if (keyCode == GLFW.GLFW_KEY_2) {
            CaffeineAddictMode.LOGGER.info("[DEBUG] Brew key 2 pressed!");
            sendBrewRequest(1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_3) {
            CaffeineAddictMode.LOGGER.info("[DEBUG] Brew key 3 pressed!");
            sendBrewRequest(2);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}