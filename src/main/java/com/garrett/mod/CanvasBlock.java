package com.garrett.mod;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CanvasBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WAXED = BooleanProperty.create("waxed");

    private static final VoxelShape SHAPE_NORTH = Block.box(0, 0, 14, 16, 16, 16);
    private static final VoxelShape SHAPE_SOUTH = Block.box(0, 0, 0, 16, 16, 2);
    private static final VoxelShape SHAPE_EAST  = Block.box(0, 0, 0, 2, 16, 16);
    private static final VoxelShape SHAPE_WEST  = Block.box(14, 0, 0, 16, 16, 16);

    public final DyeColor color;     // null for transparent variant
    public final boolean transparent;

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(p -> new CanvasBlock(color, transparent, p));
    }

    public CanvasBlock(DyeColor color, BlockBehaviour.Properties properties) {
        this(color, false, properties);
    }

    public CanvasBlock(DyeColor color, boolean transparent, BlockBehaviour.Properties properties) {
        super(properties);
        this.color = color;
        this.transparent = transparent;
        registerDefaultState(stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(WAXED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WAXED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST  -> SHAPE_EAST;
            case WEST  -> SHAPE_WEST;
            default    -> SHAPE_NORTH;
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        if (!face.getAxis().isHorizontal()) return null;
        BlockState state = defaultBlockState().setValue(FACING, face).setValue(WAXED, false);
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CanvasBlockEntity(pos, state);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                              BlockPos pos, Player player, InteractionHand hand,
                                              BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof CanvasBlockEntity canvas)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        // Wax with honeycomb
        if (stack.is(Items.HONEYCOMB) && !state.getValue(WAXED)) {
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(WAXED, true), 3);
                if (!player.isCreative()) stack.shrink(1);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        // Blend color: brush (main hand) + dye (offhand) + right-click
        if (!state.getValue(WAXED) && stack.is(Items.BRUSH)) {
            ItemStack offhand = player.getOffhandItem();
            if (offhand.getItem() instanceof DyeItem dye) {
                int pixel = hitToPixel(state.getValue(FACING), hit.getLocation(), pos);
                if (pixel >= 0) {
                    if (!level.isClientSide()) {
                        int current = canvas.getPixels()[pixel];
                        canvas.setPixel(pixel, blendColors(current, dye.getDyeColor(), this.color));
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide());
                }
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private static int blendColors(int existingRgb, DyeColor newColor, DyeColor bgColor) {
        int base = (existingRgb == CanvasBlockEntity.UNPAINTED)
                ? (bgColor != null ? bgColor.getTextureDiffuseColor() & 0xFFFFFF
                                   : newColor.getTextureDiffuseColor() & 0xFFFFFF)
                : existingRgb;
        int n = newColor.getTextureDiffuseColor() & 0xFFFFFF;
        int r = (((base >> 16) & 0xFF) + ((n >> 16) & 0xFF)) / 2;
        int g = (((base >>  8) & 0xFF) + ((n >>  8) & 0xFF)) / 2;
        int b = ((base & 0xFF) + (n & 0xFF)) / 2;
        return (r << 16) | (g << 8) | b;
    }

    public static DyeColor nearestDyeColor(int rgb) {
        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
        DyeColor nearest = DyeColor.WHITE;
        int minDist = Integer.MAX_VALUE;
        for (DyeColor dye : DyeColor.values()) {
            int d = dye.getTextureDiffuseColor();
            int dr = r - ((d >> 16) & 0xFF), dg = g - ((d >> 8) & 0xFF), db = b - (d & 0xFF);
            int dist = dr*dr + dg*dg + db*db;
            if (dist < minDist) { minDist = dist; nearest = dye; }
        }
        return nearest;
    }

    public int hitToPixel(Direction facing, Vec3 hitPos, BlockPos blockPos) {
        double lx = hitPos.x - blockPos.getX();
        double ly = hitPos.y - blockPos.getY();
        double lz = hitPos.z - blockPos.getZ();

        double u, v;
        switch (facing) {
            case NORTH -> { u = 1.0 - lx; v = 1.0 - ly; }
            case SOUTH -> { u = lx;       v = 1.0 - ly; }
            case EAST  -> { u = 1.0 - lz; v = 1.0 - ly; }
            case WEST  -> { u = lz;       v = 1.0 - ly; }
            default    -> { return -1; }
        }

        int px = (int) Math.min(15, Math.max(0, u * 16));
        int py = (int) Math.min(15, Math.max(0, v * 16));
        return py * 16 + px;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CanvasBlockEntity canvas) {
            ItemStack drop = CanvasBlockItem.createFilledStack(this, canvas);
            ItemEntity entity = new ItemEntity(level,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
            entity.setDefaultPickUpDelay();
            level.addFreshEntity(entity);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public boolean hasDynamicShape() { return true; }
}
