package com.garrett.mod.client;

import com.garrett.mod.DoodleBookItem;
import com.garrett.mod.DoodleBookSavePayload;
import com.garrett.mod.GarrettMod;
import com.mojang.blaze3d.platform.NativeImage;
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

import java.util.ArrayDeque;
import java.util.List;

/** A multi-page 64x64 drawing screen with MS-Paint-style tools (brush, fill, eyedropper, line). */
public class DoodleBookScreen extends Screen {
    private static final int GRID = DoodleBookItem.GRID;  // 64
    private static final int CANVAS_PX = 256;             // on-screen canvas size
    private static final int CELL = CANVAS_PX / GRID;     // 4 px per drawn pixel
    private static final int PAL_CELL = 14;
    private static final int PAL_COLS = 4;
    private static final int PARCHMENT = 0xF5F5DC;

    private static final int BRUSH = 0, FILL = 1, EYEDROPPER = 2, LINE = 3;
    private static final int TOOL_CELL = 16;
    private static final ResourceLocation[] TOOL_ICONS = {
        toolIcon("brush"), toolIcon("fill"), toolIcon("eyedropper"), toolIcon("line")
    };

    private static ResourceLocation toolIcon(String name) {
        return ResourceLocation.fromNamespaceAndPath(GarrettMod.MOD_ID, "textures/gui/" + name + ".png");
    }

    private final InteractionHand hand;
    private final List<byte[]> pages;
    private int page = 0;
    private int selectedColor = 1;                        // 0 = eraser, 1-16 = dye id + 1
    private int tool = BRUSH;

    // Line tool drag state (grid coords).
    private boolean drawingLine = false;
    private int lineStartX, lineStartY;

    private NativeImage image;
    private DynamicTexture texture;
    private ResourceLocation textureId;
    private boolean dirty = true;
    private boolean closed = false;

    private int canvasX, canvasY, palX, palY;

    private static final int[] DYE_RGB = new int[16];
    static {
        for (DyeColor c : DyeColor.values()) DYE_RGB[c.getId()] = c.getTextureDiffuseColor() & 0xFFFFFF;
    }

    public DoodleBookScreen(InteractionHand hand, ItemStack stack) {
        super(Component.translatable("item.gtcai.doodle_book"));
        this.hand = hand;
        this.pages = DoodleBookItem.getPages(stack);
    }

    private byte[] cur() {
        return pages.get(page);
    }

    private int eraserY() {
        return palY + (16 / PAL_COLS) * PAL_CELL + 2;
    }

    private int toolY() {
        return eraserY() + PAL_CELL + 6;
    }

    private int displayColor(int v) {
        return (v >= 1 && v <= 16) ? (0xFF000000 | DYE_RGB[v - 1]) : (0xFF000000 | PARCHMENT);
    }

    @Override
    protected void init() {
        int blockW = CANVAS_PX + 14 + PAL_COLS * PAL_CELL;
        int blockH = CANVAS_PX + 60;
        canvasX = (width - blockW) / 2 + 4;
        canvasY = (height - blockH) / 2 + 16;
        palX = canvasX + CANVAS_PX + 6;
        palY = canvasY;

        int by = canvasY + CANVAS_PX + 6;
        addRenderableWidget(Button.builder(Component.literal("<"), b -> prevPage()).pos(canvasX, by).size(20, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
            .pos(canvasX + CANVAS_PX / 2 - 40, by).size(80, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), b -> nextPage()).pos(canvasX + CANVAS_PX - 20, by).size(20, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Clear"), b -> clearPage())
            .pos(palX, toolY() + TOOL_CELL + 6).size(Math.max(60, PAL_COLS * PAL_CELL), 16).build());

        image = new NativeImage(NativeImage.Format.RGBA, GRID, GRID, false);
        texture = new DynamicTexture(image);
        textureId = ResourceLocation.fromNamespaceAndPath(GarrettMod.MOD_ID, "doodle_preview");
        assert minecraft != null;
        minecraft.getTextureManager().register(textureId, texture);
        dirty = true;
    }

    private void clearPage() {
        java.util.Arrays.fill(cur(), (byte) 0);
        dirty = true;
    }

    private void persistCurrent() {
        ClientPlayNetworking.send(new DoodleBookSavePayload(page, cur().clone()));
    }

    private void prevPage() {
        if (page <= 0) return;
        persistCurrent();
        page--;
        dirty = true;
    }

    private void nextPage() {
        persistCurrent();
        if (page < pages.size() - 1) {
            page++;
        } else if (pages.size() < DoodleBookItem.MAX_PAGES) {
            pages.add(new byte[DoodleBookItem.PAGE_BYTES]);
            page++;
        }
        dirty = true;
    }

    @Override
    public void onClose() {
        if (!closed) {
            closed = true;
            persistCurrent();
            assert minecraft != null;
            if (texture != null) {
                minecraft.getTextureManager().release(textureId);
                texture = null;
            }
        }
        super.onClose();
    }

    // --- input ---

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (uiHit(mx, my)) return true;
        int idx = canvasIndex(mx, my);
        if (idx >= 0) {
            switch (tool) {
                case FILL -> floodFill(idx);
                case EYEDROPPER -> selectedColor = cur()[idx] & 0xFF;
                case LINE -> {
                    lineStartX = idx % GRID;
                    lineStartY = idx / GRID;
                    drawingLine = true;
                }
                default -> {
                    cur()[idx] = (byte) selectedColor;
                    dirty = true;
                }
            }
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (tool == BRUSH) {
            int idx = canvasIndex(mx, my);
            if (idx >= 0) {
                cur()[idx] = (byte) selectedColor;
                dirty = true;
                return true;
            }
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (drawingLine) {
            drawingLine = false;
            int[] end = clampCell(mx, my);
            bresenham(lineStartX, lineStartY, end[0], end[1], (x, y) -> cur()[y * GRID + x] = (byte) selectedColor);
            dirty = true;
            return true;
        }
        return super.mouseReleased(mx, my, button);
    }

    /** Palette / eraser / tool-button hit tests; returns true if a selection changed. */
    private boolean uiHit(double mx, double my) {
        for (DyeColor c : DyeColor.values()) {
            int idx = c.getId();
            int sx = palX + (idx % PAL_COLS) * PAL_CELL;
            int sy = palY + (idx / PAL_COLS) * PAL_CELL;
            if (mx >= sx && mx < sx + PAL_CELL - 1 && my >= sy && my < sy + PAL_CELL - 1) {
                selectedColor = idx + 1;
                return true;
            }
        }
        int ey = eraserY();
        if (mx >= palX && mx < palX + PAL_CELL * 2 && my >= ey && my < ey + PAL_CELL - 1) {
            selectedColor = 0;
            return true;
        }
        int ty = toolY();
        for (int t = 0; t < TOOL_ICONS.length; t++) {
            int sx = palX + t * (TOOL_CELL + 2);
            if (mx >= sx && mx < sx + TOOL_CELL && my >= ty && my < ty + TOOL_CELL) {
                tool = t;
                return true;
            }
        }
        return false;
    }

    private int canvasIndex(double mx, double my) {
        int gx = (int) ((mx - canvasX) / CELL);
        int gy = (int) ((my - canvasY) / CELL);
        return (gx >= 0 && gx < GRID && gy >= 0 && gy < GRID) ? gy * GRID + gx : -1;
    }

    private int[] clampCell(double mx, double my) {
        int gx = Math.max(0, Math.min(GRID - 1, (int) ((mx - canvasX) / CELL)));
        int gy = Math.max(0, Math.min(GRID - 1, (int) ((my - canvasY) / CELL)));
        return new int[]{gx, gy};
    }

    private void floodFill(int idx) {
        byte[] p = cur();
        byte target = p[idx];
        byte replacement = (byte) selectedColor;
        if (target == replacement) return;
        ArrayDeque<Integer> stack = new ArrayDeque<>();
        stack.push(idx);
        while (!stack.isEmpty()) {
            int i = stack.pop();
            if (p[i] != target) continue;
            p[i] = replacement;
            int x = i % GRID, y = i / GRID;
            if (x > 0) stack.push(i - 1);
            if (x < GRID - 1) stack.push(i + 1);
            if (y > 0) stack.push(i - GRID);
            if (y < GRID - 1) stack.push(i + GRID);
        }
        dirty = true;
    }

    @FunctionalInterface
    private interface PointOp { void at(int x, int y); }

    private static void bresenham(int x0, int y0, int x1, int y1, PointOp op) {
        int dx = Math.abs(x1 - x0), dy = -Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
        int err = dx + dy;
        while (true) {
            op.at(x0, y0);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 >= dy) { err += dy; x0 += sx; }
            if (e2 <= dx) { err += dx; y0 += sy; }
        }
    }

    // --- rendering ---

    private void refreshTexture() {
        if (!dirty || image == null) return;
        byte[] p = cur();
        for (int y = 0; y < GRID; y++) {
            for (int x = 0; x < GRID; x++) {
                int v = p[y * GRID + x] & 0xFF;
                int rgb = (v >= 1 && v <= 16) ? DYE_RGB[v - 1] : PARCHMENT;
                int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
                image.setPixelRGBA(x, y, 0xFF000000 | (b << 16) | (g << 8) | r); // ABGR
            }
        }
        texture.upload();
        dirty = false;
    }

    // Plain dim instead of the vanilla/Blur-mod blurred background.
    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float delta) {
        g.fill(0, 0, width, height, 0x66_101010);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float delta) {
        renderBackground(g, mx, my, delta);
        refreshTexture();

        g.fill(canvasX - 4, canvasY - 14, palX + PAL_COLS * PAL_CELL + 2, canvasY + CANVAS_PX + 30, 0xFF_2A1A0A);
        g.drawCenteredString(font, title, canvasX + CANVAS_PX / 2, canvasY - 12, 0xFFE8C880);

        // 64x64 page scaled up to the canvas area
        g.blit(textureId, canvasX, canvasY, CANVAS_PX, CANVAS_PX, 0f, 0f, GRID, GRID, GRID, GRID);
        g.renderOutline(canvasX - 1, canvasY - 1, CANVAS_PX + 2, CANVAS_PX + 2, 0xFF_806040);

        // Line preview (not yet committed)
        if (drawingLine) {
            int[] end = clampCell(mx, my);
            int color = displayColor(selectedColor);
            bresenham(lineStartX, lineStartY, end[0], end[1], (x, y) -> {
                int px = canvasX + x * CELL, py = canvasY + y * CELL;
                g.fill(px, py, px + CELL, py + CELL, color);
            });
        }

        // Palette
        for (DyeColor c : DyeColor.values()) {
            int idx = c.getId();
            int sx = palX + (idx % PAL_COLS) * PAL_CELL;
            int sy = palY + (idx / PAL_COLS) * PAL_CELL;
            g.fill(sx, sy, sx + PAL_CELL - 1, sy + PAL_CELL - 1, 0xFF000000 | c.getTextureDiffuseColor());
            if (selectedColor == idx + 1) g.renderOutline(sx - 1, sy - 1, PAL_CELL + 1, PAL_CELL + 1, 0xFFFFFFFF);
        }
        int ey = eraserY();
        g.fill(palX, ey, palX + PAL_CELL * 2 - 1, ey + PAL_CELL - 1, 0xFF000000 | PARCHMENT);
        g.drawString(font, "X", palX + 4, ey + 3, 0xFF000000, false);
        if (selectedColor == 0) g.renderOutline(palX - 1, ey - 1, PAL_CELL * 2 + 1, PAL_CELL + 1, 0xFFFFFFFF);

        // Tool buttons
        int ty = toolY();
        for (int t = 0; t < TOOL_ICONS.length; t++) {
            int sx = palX + t * (TOOL_CELL + 2);
            g.fill(sx, ty, sx + TOOL_CELL, ty + TOOL_CELL, 0xFF_504030);
            g.blit(TOOL_ICONS[t], sx, ty, 0f, 0f, TOOL_CELL, TOOL_CELL, TOOL_CELL, TOOL_CELL);
            if (tool == t) g.renderOutline(sx - 1, ty - 1, TOOL_CELL + 2, TOOL_CELL + 2, 0xFFFFFFFF);
        }

        g.drawString(font, "Page " + (page + 1) + " / " + pages.size(), palX, ty + TOOL_CELL + 28, 0xFFE8C880, false);

        super.render(g, mx, my, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
