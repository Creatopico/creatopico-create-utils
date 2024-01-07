package ru.littleligr.creatopico.create.utils.mixin;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.GlobalRailwayManager;
import com.simibubi.create.content.trains.RailwaySavedData;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.signal.SignalBoundary;
import com.simibubi.create.content.trains.signal.SignalEdgeGroup;
import com.simibubi.create.foundation.utility.NBTHelper;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.littleligr.creatopico.create.utils.CreatopicoCreateUtils;
import ru.littleligr.creatopico.create.utils.redis.RedisManager;

import java.util.HashMap;
import java.util.UUID;

@Mixin(RailwaySavedData.class)
public class RailwaySavedDataMixin {

    @Inject(method = "writeNbt", at = @At("HEAD"), cancellable = true)
    public void writeNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        GlobalRailwayManager railways = Create.RAILWAYS;
        DimensionPalette dimensions = new DimensionPalette();

        for (TrackGraph graph : railways.trackNetworks.values())
            CreatopicoCreateUtils.REDIS_MANAGER.write(RedisManager.Type.track, graph.id, graph.write(dimensions));

        for(SignalEdgeGroup signalEdgeGroup : railways.signalEdgeGroups.values()) {
            NbtCompound signalNbt = null;
            if (!signalEdgeGroup.fallbackGroup && railways.trackNetworks.containsKey(signalEdgeGroup.id))
                signalNbt = signalEdgeGroup.write();
            CreatopicoCreateUtils.REDIS_MANAGER.write(RedisManager.Type.signal, signalEdgeGroup.id, signalNbt);
        }

        for (Train train : railways.trains.values())
            CreatopicoCreateUtils.REDIS_MANAGER.write(RedisManager.Type.train, train.id, train.write(dimensions));

        dimensions.write(nbt);
        cir.setReturnValue(nbt);
    }

    @Inject(method = "load(Lnet/minecraft/nbt/NbtCompound;)Lcom/simibubi/create/content/trains/RailwaySavedData;", at = @At("HEAD"), cancellable = true)
    private static void readNbt(NbtCompound nbt, CallbackInfoReturnable<RailwaySavedData> cir) {

    }
}
