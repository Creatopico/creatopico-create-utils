package ru.littleligr.creatopico.create.utils.mixin;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.*;
import com.simibubi.create.content.contraptions.glue.SuperGlueEntity;
import com.simibubi.create.content.contraptions.pulley.PulleyBlock;
import com.simibubi.create.content.contraptions.pulley.PulleyBlockEntity;
import com.simibubi.create.content.decoration.slidingDoor.SlidingDoorBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.NBTProcessors;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.state.property.Properties;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.littleligr.creatopico.create.utils.CreatopicoCreateUtils;

import java.util.List;
import java.util.Map;

@Mixin(Contraption.class)
public class ContraptionMixin {

//    @Redirect(method = "addBlocksToWorld", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"))
//    public Collection<StructureTemplate.StructureBlockInfo>  test(Map<BlockPos, StructureTemplate.StructureBlockInfo>  instance) {
//        StructureTransform transform;
//        Collection<StructureTemplate.StructureBlockInfo> collection = instance.values();
//        collection.stream().filter(structureBlockInfo -> {
//            StructureTransformStorage.map.put()
//        });
//        BlockState blockState = world.getBlockState(pos);
//        return !blockState.isAir();
//    }

    @Shadow
    public boolean disassembled;

    @Shadow
    protected Map<BlockPos, StructureTemplate.StructureBlockInfo> blocks;

    @Shadow
    protected boolean customBlockPlacement(WorldAccess world, BlockPos pos, BlockState state) {
        return false;
    }

    @Shadow
    protected MountedStorageManager storage;

    @Shadow
    protected List<Box> superglue;

    @Shadow
    protected boolean shouldUpdateAfterMovement(StructureTemplate.StructureBlockInfo info) {
        if (PointOfInterestTypes.getTypeForState(info.state())
                .isPresent())
            return false;
        if (info.state().getBlock() instanceof SlidingDoorBlock)
            return false;
        return true;
    }

//    @Redirect(method = "addBlocksToWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getDestroySpeed(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"))
//    public float test2(BlockState instance, BlockGetter blockGetter, BlockPos blockPos) {
//        instance.
//        return -1;
//    }


    @Shadow protected ContraptionWorld world;

    @Redirect(method = "addBlocksToWorld", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/contraptions/Contraption;customBlockPlacement(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z"))
    public boolean test(Contraption instance, WorldAccess world, BlockPos pos, BlockState state) {
        BlockState blockState = world.getBlockState(pos);
        return !blockState.isAir();
    }

//    boolean breakBlock = CreatopicoCreateUtils.breakBlocks.contains(worldBlock);
//    boolean inAWorld = CreatopicoCreateUtils.dimensionsWorksIn.stream().map(location::equals).reduce(Boolean::logicalOr).orElse(false);
//
//                if (
//                        blockState.getDestroySpeed(world, targetPos) == -1
//            || (!breakBlock && !inAWorld)
//            || (
//            state.getCollisionShape(world, targetPos).isEmpty()
//                                && !blockState.getCollisionShape(world, targetPos).isEmpty())
//            )
//    {
//        if (targetPos.getY() == world.getMinBuildHeight())
//            targetPos = targetPos.above();
//        world.levelEvent(2001, targetPos, Block.getId(state));
//        Block.dropResources(state, world, targetPos, null);
//        continue;
//    }

    @Inject(method = "addBlocksToWorld", at = @At("HEAD"), cancellable = true)
    public void addBlocksToWorld(World world, StructureTransform transform, CallbackInfo ci) {
        if (disassembled)
            return;
        disassembled = true;

        for (boolean nonBrittles : Iterate.trueAndFalse) {
            for (StructureTemplate.StructureBlockInfo block : blocks.values()) {
                if (nonBrittles == BlockMovementChecks.isBrittle(block.state()))
                    continue;

                BlockPos targetPos = transform.apply(block.pos());
                BlockState state = transform.apply(block.state());

                if (customBlockPlacement(world, targetPos, state))
                    continue;

                if (nonBrittles)
                    for (Direction face : Iterate.directions)
                        state = state.getStateForNeighborUpdate(face, world.getBlockState(targetPos.offset(face)), world, targetPos,
                                targetPos.offset(face));

                BlockState blockState = world.getBlockState(targetPos);
                Block worldBlock = blockState.getBlock();
                Identifier location = world.getDimensionKey().getValue();

                boolean breakBlock = CreatopicoCreateUtils.breakBlocks.contains(worldBlock);
                boolean inAWorld = CreatopicoCreateUtils.dimensionsWorksIn.stream().map(location::equals).reduce(Boolean::logicalOr).orElse(false);

                if (
                        blockState.getHardness(world, targetPos) == -1
                                || (state.getCollisionShape(world, targetPos).isEmpty() && !blockState.getCollisionShape(world, targetPos).isEmpty())
                                || (!breakBlock && !inAWorld)
                ) {
                    if (targetPos.getY() == world.getBottomY())
                        targetPos = targetPos.up();
                    world.syncWorldEvent(2001, targetPos, Block.getRawIdFromState(state));
                    Block.dropStacks(state, world, targetPos, null);
                    continue;
                }

                if (state.getBlock() instanceof Waterloggable
                        && state.contains(Properties.WATERLOGGED)) {
                    FluidState FluidState = world.getFluidState(targetPos);
                    state = state.with(Properties.WATERLOGGED, FluidState.getFluid() == Fluids.WATER);
                }

                world.breakBlock(targetPos, true);

                if (AllBlocks.SHAFT.has(state))
                    state = ShaftBlock.pickCorrectShaftType(state, world, targetPos);
                if (state.contains(SlidingDoorBlock.VISIBLE))
                    state = state.with(SlidingDoorBlock.VISIBLE, !state.get(SlidingDoorBlock.OPEN))
                            .with(SlidingDoorBlock.POWERED, false);
                // Stop Sculk shriekers from getting "stuck" if moved mid-shriek.
                if(state.isOf(Blocks.SCULK_SHRIEKER)){
                    state = Blocks.SCULK_SHRIEKER.getDefaultState();
                }

                world.setBlockState(targetPos, state, Block.MOVED | Block.NOTIFY_ALL);

                boolean verticalRotation = transform.rotationAxis == null || transform.rotationAxis.isHorizontal();
                verticalRotation = verticalRotation && transform.rotation != BlockRotation.NONE;
                if (verticalRotation) {
                    if (state.getBlock() instanceof PulleyBlock.RopeBlock || state.getBlock() instanceof PulleyBlock.MagnetBlock
                            || state.getBlock() instanceof DoorBlock)
                        world.breakBlock(targetPos, true);
                }

                BlockEntity blockEntity = world.getBlockEntity(targetPos);

                NbtCompound tag = block.nbt();

                // Temporary fix: Calling load(CompoundTag tag) on a Sculk sensor causes it to not react to vibrations.
                if(state.isOf(Blocks.SCULK_SENSOR) || state.isOf(Blocks.SCULK_SHRIEKER))
                    tag = null;

                if (blockEntity != null)
                    tag = NBTProcessors.process(blockEntity, tag, false);
                if (blockEntity != null && tag != null) {
                    tag.putInt("x", targetPos.getX());
                    tag.putInt("y", targetPos.getY());
                    tag.putInt("z", targetPos.getZ());

                    if (verticalRotation && blockEntity instanceof PulleyBlockEntity) {
                        tag.remove("Offset");
                        tag.remove("InitialOffset");
                    }

                    if (blockEntity instanceof IMultiBlockEntityContainer && tag.contains("LastKnownPos"))
                        tag.put("LastKnownPos", NbtHelper.fromBlockPos(BlockPos.ORIGIN.down(Integer.MAX_VALUE - 1)));

                    blockEntity.readNbt(tag);
                    storage.addStorageToWorld(block, blockEntity);
                }

                transform.apply(blockEntity);
            }
        }

        for (StructureTemplate.StructureBlockInfo block : blocks.values()) {
            if (!shouldUpdateAfterMovement(block))
                continue;
            BlockPos targetPos = transform.apply(block.pos());
            world.markAndNotifyBlock(targetPos, world.getWorldChunk(targetPos), block.state(), block.state(),
                    Block.MOVED | Block.NOTIFY_ALL, 512);
        }

        for (Box box : superglue) {
            box = new Box(transform.apply(new Vec3d(box.minX, box.minY, box.minZ)),
                    transform.apply(new Vec3d(box.maxX, box.maxY, box.maxZ)));
            if (!world.isClient)
                world.spawnEntity(new SuperGlueEntity(world, box));
        }

        storage.clear();

        ci.cancel();
    }
}
