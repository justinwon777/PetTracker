package com.github.justinwon777.pettracker.client;

import com.github.justinwon777.pettracker.PetTracker;
import com.github.justinwon777.pettracker.core.PacketHandler;
import com.github.justinwon777.pettracker.item.Tracker;
import com.github.justinwon777.pettracker.networking.RemovePacket;
import com.github.justinwon777.pettracker.networking.TeleportPacket;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.*;

@OnlyIn(Dist.CLIENT)
public class TrackerScreen extends Screen {

    private static final ResourceLocation TRACKER_BACKGROUND = new ResourceLocation(PetTracker.MOD_ID,
            "textures/trackerscreen.png");
    protected int imageWidth;
    protected int imageHeight;
    protected int leftPos;
    protected int topPos;
    private TrackerList trackerList;
    private Button teleportButton;
    private Button removeButton;
    private final ItemStack itemStack;
    private final String hand;

    public TrackerScreen(ItemStack tracker, String hand) {
        super(tracker.getItem().getName());
        this.imageHeight = 222;
        this.imageWidth = 176;
        this.itemStack = tracker;
        this.hand = hand;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        this.trackerList = new TrackerList(this.minecraft, this.itemStack, this);
        this.children.add(this.trackerList);
        this.teleportButton = addButton(new Button(leftPos + 5, topPos + imageHeight - 10 - 15
                , 83,
                20, new StringTextComponent("Teleport"),
                btn -> {
                    TrackerList.Entry entry = this.trackerList.getSelected();
                    if (entry != null) {
                        PacketHandler.INSTANCE.sendToServer(new TeleportPacket(entry.uuid));
                    }
                }));
        this.removeButton = addButton(new Button(leftPos + 89, topPos + imageHeight - 10 - 15
                , 83,
                20, new StringTextComponent("Remove"),
                btn -> {
                    TrackerList.Entry entry = this.trackerList.getSelected();
                    if (entry != null) {
                        PacketHandler.INSTANCE.sendToServer(new RemovePacket(entry.uuid, this.itemStack, this.hand));
                        this.trackerList.delete(entry);
                        updateRemoveButtonStatus(false);
                        updateTeleportButtonStatus(false);
                    }
                }));
        updateTeleportButtonStatus(false);
        updateRemoveButtonStatus(false);
    }

    @Override
    public void render(MatrixStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack);
        this.trackerList.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        String header = "Pet Tracker";
        this.font.drawString(pPoseStack, header, (float) (this.width / 2 - this.font.getStringWidth(header) / 2),
                (float) this.topPos + 5,
                4210752);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pPoseStack, pMouseX, pMouseY);
    }

    @Override
    public void renderBackground(MatrixStack pPoseStack, int pVOffset) {
        super.renderBackground(pPoseStack, pVOffset);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(TRACKER_BACKGROUND);

        this.blit(pPoseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    protected void renderTooltip(MatrixStack stack, int x, int y) {
        if (this.teleportButton.isHovered()) {
            List<ITextComponent> tooltips = new ArrayList<>();
            if (this.teleportButton.active || this.trackerList.getSelected() == null) {
                tooltips.add(new StringTextComponent("Teleports mob to you").mergeStyle(TextFormatting.GRAY).mergeStyle(TextFormatting.ITALIC));
            } else {
                tooltips.add(new StringTextComponent("Mob is either dead or in an unloaded chunk. " +
                        "Location is last known position.").mergeStyle(TextFormatting.GRAY).mergeStyle(TextFormatting.ITALIC));
            }
            this.func_243308_b(stack, tooltips, x, y);
        }


        if (this.removeButton.isHovered()) {
            List<ITextComponent> tooltips = new ArrayList<>();
            tooltips.add(new StringTextComponent("Removes mob from the list.").mergeStyle(TextFormatting.GRAY).mergeStyle(TextFormatting.ITALIC));

            this.func_243308_b(stack, tooltips, x, y);
        }
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        InputMappings.Input mouseKey = InputMappings.getInputByCode(pKeyCode, pScanCode);
        if (this.minecraft.gameSettings.keyBindInventory.isActiveAndMatches(mouseKey)) {
            this.closeScreen();
            return true;
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    public void updateTeleportButtonStatus(boolean pActive) {
        this.teleportButton.active = pActive;
    }

    public void updateRemoveButtonStatus(boolean pActive) {
        this.removeButton.active = pActive;
    }

    public boolean isPauseScreen() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    class TrackerList extends ExtendedList<TrackerList.Entry> {
        private final TrackerScreen screen;

        public TrackerList(Minecraft pMinecraft, ItemStack itemstack, TrackerScreen screen) {
            super(pMinecraft, TrackerScreen.this.width, TrackerScreen.this.height, 52,
                    TrackerScreen.this.height - 65, 26);
            this.func_244605_b(false);
            this.func_244606_c(false);
            this.screen = screen;
            CompoundNBT tag = itemstack.getTag();
            if (tag != null) {
                ListNBT listTag = tag.getList(Tracker.TRACKING, 10);
                for (int i = 0; i < listTag.size(); ++i) {
                    CompoundNBT entityTag = listTag.getCompound(i);
                    String name = entityTag.getString("name");
                    int x = entityTag.getInt("x");
                    int y = entityTag.getInt("y");
                    int z = entityTag.getInt("z");
                    boolean active = entityTag.getBoolean("active");
                    UUID uuid = entityTag.getUniqueId("uuid");
                    this.addEntry(new Entry(name, x, y, z, active, uuid));
                }
            }

            if (this.getSelected() != null) {
                this.centerScrollOn(this.getSelected());
            }

        }

        public void setSelected(@Nullable Entry pSelected) {
            super.setSelected(pSelected);
            if (pSelected != null) {
                NarratorChatListener.INSTANCE.say((new TranslationTextComponent("narrator.select", pSelected.name)).getString());
                this.screen.updateRemoveButtonStatus(true);
                this.screen.updateTeleportButtonStatus(pSelected.active);
            }

        }

        protected int getScrollbarPosition() {
            return super.getScrollbarPosition() - 45;
        }

        public int getRowWidth() {
            return super.getRowWidth() - 64;
        }

        protected boolean isFocused() {
            return TrackerScreen.this.getListener() == this;
        }

        public void delete(Entry entry) {
            this.removeEntry(entry);
        }

        protected void renderList(MatrixStack pPoseStack, int pX, int pY, int pMouseX, int pMouseY, float pPartialTick) {
            int i = this.getRowLeft();
            int j = this.getRowWidth();
            int k = this.itemHeight - 4;
            int l = this.getItemCount();

            for(int i1 = 0; i1 < l; ++i1) {
                int j1 = this.getRowTop(i1);
                int k1 = this.getRowTop(i1) + this.itemHeight;
                if (k1 >= this.y0 && j1 <= this.y1) {
                    this.renderItem(pPoseStack, pMouseX, pMouseY, pPartialTick, i1, i, j1, j, k);
                }
            }
        }

        protected void renderItem(MatrixStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick, int pIndex, int pLeft, int pTop, int pWidth, int pHeight) {
            Entry e = this.getEntry(pIndex);
            int i = this.x0 + (this.width - pWidth) / 2;
            int j = this.x0 + (this.width + pWidth) / 2;
            fill(pPoseStack, i, pTop - 2, j, pTop + pHeight + 2, 0xFFAAAAAA);
            fill(pPoseStack, i + 1, pTop - 1, j - 1, pTop + pHeight + 1, 0xFFFFFFFF);
            if (this.isSelectedItem(pIndex)) {
                this.renderSelection(pPoseStack, pTop, pWidth, pHeight, 0xFF000000, 0xFFE0E0E0);
            }

            e.render(pPoseStack, pIndex, pTop, pLeft, pWidth, pHeight, pMouseX, pMouseY, this.isMouseOver(pMouseX, pMouseY) && Objects.equals(this.getEntryAtPosition(pMouseX, pMouseY), e),
                    pPartialTick);
        }

        protected void renderSelection(MatrixStack pPoseStack, int pTop, int pWidth, int pHeight, int pOuterColor, int pInnerColor) {
            int i = this.x0 + (this.width - pWidth) / 2;
            int j = this.x0 + (this.width + pWidth) / 2;
            fill(pPoseStack, i, pTop - 2, j, pTop + pHeight + 2, pOuterColor);
            fill(pPoseStack, i + 1, pTop - 1, j - 1, pTop + pHeight + 1, pInnerColor);
        }

        @OnlyIn(Dist.CLIENT)
        public class Entry extends ExtendedList.AbstractListEntry<Entry> {
            public String name;
            public int x;
            public int y;
            public int z;
            public boolean active;
            public UUID uuid;

            public Entry(String name, int x, int y, int z, boolean active, UUID uuid) {
                this.name = name;
                this.x = x;
                this.y = y;
                this.z = z;
                this.active = active;
                this.uuid = uuid;
            }
            public void render(MatrixStack pPoseStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTick) {
                String location = "Location: " + this.x + ", " + this.y + ", " + this.z;
                TrackerScreen.this.font.drawString(pPoseStack, this.name,
                        (float)(TrackerList.this.width / 2 - TrackerScreen.this.font.getStringWidth(name) / 2),
                        (float)(pTop + 1), 4210752);
                TrackerScreen.this.font.drawString(pPoseStack, location,
                        (float)(TrackerList.this.width / 2 - TrackerScreen.this.font.getStringWidth(location) / 2),
                        (float)(pTop + 12), 4210752);
            }

            public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
                if (pButton == 0) {
                    this.select();
                    return true;
                } else {
                    return false;
                }
            }

            private void select() {
                TrackerList.this.setSelected(this);

            }
        }
    }
}
