package jp.sohta.biomeselector.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

/** Draws a full-height outline, so selected borders remain visible above terrain. */
public class SelectionRenderer {
    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (!ClientSelectionState.present || minecraft.theWorld == null
                || minecraft.theWorld.provider.dimensionId != ClientSelectionState.dimension) return;

        double cameraX = minecraft.renderViewEntity.lastTickPosX
                + (minecraft.renderViewEntity.posX - minecraft.renderViewEntity.lastTickPosX) * event.partialTicks;
        double cameraY = minecraft.renderViewEntity.lastTickPosY
                + (minecraft.renderViewEntity.posY - minecraft.renderViewEntity.lastTickPosY) * event.partialTicks;
        double cameraZ = minecraft.renderViewEntity.lastTickPosZ
                + (minecraft.renderViewEntity.posZ - minecraft.renderViewEntity.lastTickPosZ) * event.partialTicks;

        GL11.glPushMatrix();
        GL11.glTranslated(-cameraX, -cameraY, -cameraZ);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false);
        GL11.glLineWidth(2.0F);

        if (ClientSelectionState.complete) {
            GL11.glColor4f(0.2F, 1.0F, 0.25F, 0.9F);
            drawBox(Math.min(ClientSelectionState.x1, ClientSelectionState.x2),
                    Math.max(ClientSelectionState.x1, ClientSelectionState.x2) + 1,
                    Math.min(ClientSelectionState.z1, ClientSelectionState.z2),
                    Math.max(ClientSelectionState.z1, ClientSelectionState.z2) + 1);
        } else {
            GL11.glColor4f(1.0F, 0.85F, 0.1F, 0.9F);
            drawMarker(ClientSelectionState.x1, ClientSelectionState.z1);
        }

        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    private void drawMarker(int x, int z) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawing(GL11.GL_LINES);
        tessellator.addVertex(x + 0.5D, 0.0D, z + 0.5D);
        tessellator.addVertex(x + 0.5D, 256.0D, z + 0.5D);
        tessellator.draw();
    }

    private void drawBox(int minX, int maxX, int minZ, int maxZ) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawing(GL11.GL_LINES);
        line(tessellator, minX, 0, minZ, maxX, 0, minZ);
        line(tessellator, maxX, 0, minZ, maxX, 0, maxZ);
        line(tessellator, maxX, 0, maxZ, minX, 0, maxZ);
        line(tessellator, minX, 0, maxZ, minX, 0, minZ);
        line(tessellator, minX, 256, minZ, maxX, 256, minZ);
        line(tessellator, maxX, 256, minZ, maxX, 256, maxZ);
        line(tessellator, maxX, 256, maxZ, minX, 256, maxZ);
        line(tessellator, minX, 256, maxZ, minX, 256, minZ);
        line(tessellator, minX, 0, minZ, minX, 256, minZ);
        line(tessellator, maxX, 0, minZ, maxX, 256, minZ);
        line(tessellator, maxX, 0, maxZ, maxX, 256, maxZ);
        line(tessellator, minX, 0, maxZ, minX, 256, maxZ);
        tessellator.draw();
    }

    private void line(Tessellator tessellator, double x1, double y1, double z1,
            double x2, double y2, double z2) {
        tessellator.addVertex(x1, y1, z1);
        tessellator.addVertex(x2, y2, z2);
    }
}
