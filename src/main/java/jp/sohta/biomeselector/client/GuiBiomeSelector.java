package jp.sohta.biomeselector.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import jp.sohta.biomeselector.BiomeSelectorMod;
import jp.sohta.biomeselector.network.BiomeChangeMessage;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.biome.BiomeGenBase;

@SideOnly(Side.CLIENT)
public class GuiBiomeSelector extends GuiScreen {
    private GuiBiomeList biomeList;
    private GuiButton applyButton;

    @Override
    public void initGui() {
        biomeList = new GuiBiomeList(this);
        buttonList.add(applyButton = new GuiButton(0, width / 2 - 102, height - 42, 100, 20, "変更する"));
        buttonList.add(new GuiButton(1, width / 2 + 2, height - 42, 100, 20, "キャンセル"));
        applyButton.enabled = biomeList.getSelectedBiome() != null;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0 && biomeList.getSelectedBiome() != null) {
            BiomeSelectorMod.NETWORK.sendToServer(new BiomeChangeMessage(biomeList.getSelectedBiome().biomeID));
            mc.displayGuiScreen(null);
        } else if (button.id == 1) {
            mc.displayGuiScreen(null);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        biomeList.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(fontRendererObj, "変更するバイオームを選択", width / 2, 15, 0xFFFFFF);
        drawCenteredString(fontRendererObj, "Shift＋空中右クリックで実行中の変更を中止できます", width / 2, 29, 0xBBBBBB);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void selectionChanged() {
        applyButton.enabled = biomeList.getSelectedBiome() != null;
    }

    @SideOnly(Side.CLIENT)
    private static class GuiBiomeList extends GuiSlot {
        private final GuiBiomeSelector parent;
        private final List<BiomeGenBase> biomes = new ArrayList<BiomeGenBase>();
        private int selected = -1;

        public GuiBiomeList(GuiBiomeSelector parent) {
            super(parent.mc, parent.width, parent.height, 45, parent.height - 58, 18);
            this.parent = parent;
            BiomeGenBase[] registered = BiomeGenBase.getBiomeGenArray();
            for (BiomeGenBase biome : registered) {
                if (biome != null) {
                    biomes.add(biome);
                }
            }
            Collections.sort(biomes, new Comparator<BiomeGenBase>() {
                @Override
                public int compare(BiomeGenBase a, BiomeGenBase b) {
                    return a.biomeName.compareToIgnoreCase(b.biomeName);
                }
            });
        }

        public BiomeGenBase getSelectedBiome() {
            return selected >= 0 && selected < biomes.size() ? biomes.get(selected) : null;
        }

        @Override
        protected int getSize() {
            return biomes.size();
        }

        @Override
        protected void elementClicked(int index, boolean doubleClick, int mouseX, int mouseY) {
            selected = index;
            parent.selectionChanged();
        }

        @Override
        protected boolean isSelected(int index) {
            return index == selected;
        }

        @Override
        protected void drawBackground() {
        }

        @Override
        protected void drawSlot(int index, int x, int y, int height, Tessellator tessellator,
                int mouseX, int mouseY) {
            BiomeGenBase biome = biomes.get(index);
            parent.fontRendererObj.drawString("[" + biome.biomeID + "] " + biome.biomeName, x + 8, y + 3, 0xFFFFFF);
        }
    }
}
