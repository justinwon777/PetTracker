package com.github.justinwon777.pettracker.client;

import com.github.justinwon777.pettracker.PetTracker;
import com.github.justinwon777.pettracker.core.PacketHandler;
import com.github.justinwon777.pettracker.item.Tracker;
import com.github.justinwon777.pettracker.networking.RemovePacket;
import com.github.justinwon777.pettracker.networking.TeleportPacket;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.*;
import java.util.List;

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
        super(tracker.getItem().getDescription());
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
        this.trackerList = addWidget(new TrackerList(this.minecraft, this.itemStack, this));
        this.teleportButton = addRenderableWidget(new Button(leftPos + 5, topPos + imageHeight - 10 - 15
                , 83,
                20, Component.literal("Teleport"),
                btn -> {
                    TrackerList.Entry entry = this.trackerList.getSelected();
                    PacketHandler.INSTANCE.sendToServer(new TeleportPacket(entry.uuid));
                }));
        this.removeButton = addRenderableWidget(new Button(leftPos + 89, topPos + imageHeight - 10 - 15
                , 83,
                20, Component.literal("Remove"),
                btn -> {
                    TrackerList.Entry entry = this.trackerList.getSelected();
                    PacketHandler.INSTANCE.sendToServer(new RemovePacket(entry.uuid, this.itemStack, this.hand));
                    this.trackerList.delete(entry);
                    updateRemoveButtonStatus(false);
                }));
        updateTeleportButtonStatus(false);
        updateRemoveButtonStatus(false);
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack);
        this.trackerList.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        String header = "Pet Tracker";
        this.font.draw(pPoseStack, header, (float) (this.width / 2 - this.font.width(header) / 2),
                (float) this.topPos + 5,
                4210752);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pPoseStack, pMouseX, pMouseY);
    }

    @Override
    public void renderBackground(PoseStack pPoseStack, int pVOffset) {
        super.renderBackground(pPoseStack, pVOffset);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TRACKER_BACKGROUND);

        this.blit(pPoseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    protected void renderTooltip(PoseStack stack, int x, int y) {
        if (this.teleportButton.isHoveredOrFocused()) {
            List<Component> tooltips = new ArrayList<>();
            if (this.teleportButton.isActive() || this.trackerList.getSelected() == null) {
                tooltips.add(Component.literal("Teleports mob to you").withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
            } else {
                tooltips.add(Component.literal("Mob is either dead or in an unloaded chunk. " +
                        "Location is last known position.").withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
            }
            this.renderTooltip(stack, tooltips, Optional.empty(), x, y);
        }


        if (this.removeButton.isHoveredOrFocused()) {
            List<Component> tooltips = new ArrayList<>();
            tooltips.add(Component.literal("Removes mob from the list.").withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));

            this.renderTooltip(stack, tooltips, Optional.empty(), x, y);
        }
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        InputConstants.Key mouseKey = InputConstants.getKey(pKeyCode, pScanCode);
        if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
            this.onClose();
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
    class TrackerList extends ObjectSelectionList<TrackerScreen.TrackerList.Entry> {
        private final TrackerScreen screen;

        public TrackerList(Minecraft pMinecraft, ItemStack itemstack, TrackerScreen screen) {
            super(pMinecraft, TrackerScreen.this.width, TrackerScreen.this.height, 52,
                    TrackerScreen.this.height - 65, 26);
            this.setRenderBackground(false);
            this.setRenderTopAndBottom(false);
            this.screen = screen;
            CompoundTag tag = itemstack.getTag();
            ListTag listTag = tag.getList(Tracker.TRACKING, 10);
            for(int i = 0; i < listTag.size(); ++i) {
                CompoundTag entityTag = listTag.getCompound(i);
                String name = entityTag.getString("name");
                int x = entityTag.getInt("x");
                int y = entityTag.getInt("y");
                int z = entityTag.getInt("z");
                boolean active = entityTag.getBoolean("active");
                UUID uuid = entityTag.getUUID("uuid");
                this.addEntry(new Entry(name, x, y, z, active, uuid));
            }

            if (this.getSelected() != null) {
                this.centerScrollOn(this.getSelected());
            }

        }

        public void setSelected(@Nullable TrackerList.Entry pSelected) {
            super.setSelected(pSelected);
            if (pSelected != null) {
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

        protected void renderItem(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick, int pIndex, int pLeft, int pTop, int pWidth, int pHeight) {
            TrackerList.Entry e = this.getEntry(pIndex);
            int i = this.x0 + (this.width - pWidth) / 2;
            int j = this.x0 + (this.width + pWidth) / 2;
            fill(pPoseStack, i, pTop - 2, j, pTop + pHeight + 2, 0xFFAAAAAA);
            fill(pPoseStack, i + 1, pTop - 1, j - 1, pTop + pHeight + 1, 0xFFFFFFFF);
            if (this.isSelectedItem(pIndex)) {
                int k = this.isFocused() ? -1 : -8355712;
                this.renderSelection(pPoseStack, pTop, pWidth, pHeight, 0xFF000000, 0xFFE0E0E0);
            }

            e.render(pPoseStack, pIndex, pTop, pLeft, pWidth, pHeight, pMouseX, pMouseY, Objects.equals(this.getHovered(), e),
                    pPartialTick);
        }

        protected boolean isFocused() {
            return TrackerScreen.this.getFocused() == this;
        }

        public void delete(Entry entry) {
            this.removeEntry(entry);
        }

        @OnlyIn(Dist.CLIENT)
        public class Entry extends ObjectSelectionList.Entry<TrackerScreen.TrackerList.Entry> {
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
            public void render(PoseStack pPoseStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTick) {
                String location = "Location: " + this.x + ", " + this.y + ", " + this.z;
                TrackerScreen.this.font.draw(pPoseStack, this.name,
                        (float)(TrackerScreen.TrackerList.this.width / 2 - TrackerScreen.this.font.width(name) / 2),
                        (float)(pTop + 1), 4210752);
                TrackerScreen.this.font.draw(pPoseStack, location,
                        (float)(TrackerScreen.TrackerList.this.width / 2 - TrackerScreen.this.font.width(location) / 2),
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
                TrackerScreen.TrackerList.this.setSelected(this);

            }

            public Component getNarration() {
                return Component.translatable("narrator.select", this.name);
            }
        }
    }
}
