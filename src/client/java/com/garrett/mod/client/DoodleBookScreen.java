package com.garrett.mod.client;

import com.garrett.mod.DoodleBookItem;
import com.garrett.mod.DoodleBookSavePayload;
import com.garrett.mod.GarrettMod;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public class DoodleBookScreen extends Screen {
    private static final int GRID = 16;
    private static final int CELL = 12;          // pixels per cell on screen
    private static final int PAL_CELL = 14;      // palette swatch size
    private static final int PAL_COLS = 8;
    private static final int CANVAS_W = GRID * CELL;
    private static final int CANVAS_H = GRID * CELL;

    private final InteractionHand hand;
    private final byte[] pixels;                 // 256 bytes, index = DyeColor ordinal, 0 = empty
    private int selectedColor = 1;               // 1-16 = dye, 0 = eraser

    private DynamicTexture previewTexture;
    private ResourceLocation previewId;
    private NativeImage previewImage;
    private boolean textureDirty = true;

    private int canvasX, canvasY;                // top-left of the drawing canvas on screen
    private int palX, palY;                      // top-left of the palette

    private static final int[] DYE_ARGB = new int[16];
    static {
        for (DyeColor c : DyeColor.values()) {
            int col = c.getTextureDiffuseColor();
            DYE_ARGB[c.getId()] = 0xFF000000 | col;
        }
    }

    public DoodleBookScreen(InteractionHand hand, ItemStack stack) {
        super(Component.translatable("item.gtcai.doodle_book"));
        this.hand = hand;
        byte[] raw = DoodleBookItem.getPixels(stack);
        this.pixels = new byte[GRID * GRID];
        System.arraycopy(raw, 0, pixels, 0, Math.min(raw.length, pixels.length));
    }

    @Override
    protected void init() {
        int panelW = CANVAS_W + 4 + PAL_COLS * PAL_CELL + 6;
        int panelH = Math.max(CANVAS_H, 2 * GRID * PAL_CELL) + 30;
        canvasX = (width - panelW) / 2 + 2;
        canvasY = (height - panelH) / 2 + 2;
        palX = canvasX + CANVAS_W + 4;
        palY = canvasY;

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), btn -> save())
            .pos(canvasX + CANVAS_W / 2 - 50, canvasY + CANVAS_H + 4)
            .size(100, 20).build());

        // Build the preview texture
        previewImage = new NativeImage(NativeImage.Format.RGBA, GRID, GRID, false);
        previewTexture = new DynamicTexture(previewImage);
        previewId = ResourceLocation.fromNamespaceAndPath(GarrettMod.MOD_ID, "doodle_preview");
        assert minecraft != null;
        minecraft.getTextureManager().register(previewId, previewTexture);
        textureDirty = true;
    }

    @Override
    public void onClose() {
        save();
        super.onClose();
    }

    private void save() {
        ClientPlayNetworking.send(new DoodleBookSavePayload(pixels.clone()));
        assert minecraft != null;
        if (previewTexture != null) {
            minecraft.getTextureManager().release(previewId);
            previewTexture = null;
        }
        onClose();
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (tryPaint(mx, my)) return true;
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (tryPaint(mx, my)) return true;
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    private boolean tryPaint(double mx, double my) {
        // Check palette
        for (DyeColor c : DyeColor.values()) {
            int idx = c.getId();
            int col = idx % PAL_COLS;
            int row = idx / PAL_COLS;
            int sx = palX + col * PAL_CELL;
            int sy = palY + row * PAL_CELL;
            if (mx >= sx && mx < sx + PAL_CELL - 1 && my >= sy && my < sy + PAL_CELL - 1) {
                selectedColor = idx + 1;
                return true;
            }
        }
        // Eraser at bottom of palette
        int eraserX = palX;
        int eraserY = palY + (16 / PAL_COLS) * PAL_CELL + 2;
        if (mx >= eraserX && mx < eraserX + PAL_CELL * 2 && my >= eraserY && my < eraserY + PAL_CELL - 1) {
            selectedColor = 0;
            return true;
        }
        // Check canvas
        int gx = (int) ((mx - canvasX) / CELL);
        int gy = (int) ((my - canvasY) / CELL);
        if (gx >= 0 && gx < GRID && gy >= 0 && gy < GRID) {
            pixels[gy * GRID + gx] = (byte) selectedColor;
            textureDirty = true;
            return true;
        }
        return false;
    }

    private void updateTexture() {
        if (!textureDirty || previewImage == null) return;
        for (int y = 0; y < GRID; y++) {
            for (int x = 0; x < GRID; x++) {
                int v = pixels[y * GRID + x] & 0xFF;
                int argb;
                if (v == 0) {
                    argb = 0xFF_F5F5DC; // parchment
                } else {
                    DyeColor dc = DyeColor.byId(v - 1);
                    argb = dc != null ? DYE_ARGB[dc.getId()] : 0xFF_F5F5DC;
                }
                // NativeImage uses ABGR internally
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8)  & 0xFF;
                int b =  argb        & 0xFF;
                previewImage.setPixelRGBA(x, y, (a << 24) | (b << 16) | (g << 8) | r);
            }
        }
        previewTexture.upload();
        textureDirty = false;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float delta) {
        renderBackground(g, mx, my, delta);
        updateTexture();

        // Background panel
        g.fill(canvasX - 4, canvasY - 14, canvasX + CANVAS_W + 4 + PAL_COLS * PAL_CELL + 6, canvasY + CANVAS_H + 28, 0xC0_2A1A0A);
        g.drawCenteredString(font, title, canvasX + CANVAS_W / 2, canvasY - 12, 0xFFE8C880);

        // Draw canvas cells
        for (int y = 0; y < GRID; y++) {
            for (int x = 0; x < GRID; x++) {
                int v = pixels[y * GRID + x] & 0xFF;
                int color;
                if (v == 0) {
                    color = 0xFF_F5F5DC;
                } else {
                    DyeColor dc = DyeColor.byId(v - 1);
                    color = dc != null ? (0xFF000000 | dc.getTextureDiffuseColor()) : 0xFF_F5F5DC;
                }
                int px = canvasX + x * CELL;
                int py = canvasY + y * CELL;
                g.fill(px, py, px + CELL - 1, py + CELL - 1, color);
            }
        }
        // Canvas grid border
        g.renderOutline(canvasX - 1, canvasY - 1, CANVAS_W + 2, CANVAS_H + 2, 0xFF_806040);

        // Draw palette
        g.drawString(font, "Colors:", palX, palY - 10, 0xFF_E8C880, false);
        for (DyeColor c : DyeColor.values()) {
            int idx = c.getId();
            int col = idx % PAL_COLS;
            int row = idx / PAL_COLS;
            int sx = palX + col * PAL_CELL;
            int sy = palY + row * PAL_CELL;
            g.fill(sx, sy, sx + PAL_CELL - 1, sy + PAL_CELL - 1, 0xFF000000 | c.getTextureDiffuseColor());
            if (selectedColor == idx + 1) {
                g.renderOutline(sx - 1, sy - 1, PAL_CELL + 1, PAL_CELL + 1, 0xFF_FFFFFF);
            }
        }
        // Eraser
        int eraserX = palX;
        int eraserY = palY + (16 / PAL_COLS) * PAL_CELL + 2;
        g.fill(eraserX, eraserY, eraserX + PAL_CELL * 2 - 1, eraserY + PAL_CELL - 1, 0xFF_F5F5DC);
        g.drawString(font, "X", eraserX + 4, eraserY + 3, 0xFF_000000, false);
        if (selectedColor == 0) {
            g.renderOutline(eraserX - 1, eraserY - 1, PAL_CELL * 2 + 1, PAL_CELL + 1, 0xFF_FFFFFF);
        }

        super.render(g, mx, my, delta);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
