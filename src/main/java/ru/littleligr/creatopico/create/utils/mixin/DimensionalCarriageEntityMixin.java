package ru.littleligr.creatopico.create.utils.mixin;


import com.simibubi.create.content.trains.entity.Carriage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.littleligr.creatopico.create.utils.CreatopicoCreateUtils;

import java.util.Optional;

@Mixin(Carriage.DimensionalCarriageEntity.class)
public class DimensionalCarriageEntityMixin {

    @Redirect(method = "createEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityType;getEntityFromNbt(Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/world/World;)Ljava/util/Optional;"))
    public Optional<Entity> createEntityHead(NbtCompound nbt, World world) {
        try {
            return EntityType.getEntityFromNbt(nbt, world);
        }catch (Exception e) {
            CreatopicoCreateUtils.LOGGER.error("DimensionalCarriageEntityMixin.createEntityHead error: " + e.getMessage());
        }

        return Optional.empty();
    }

    @Redirect(method = "createEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;refreshPositionAfterTeleport(Lnet/minecraft/util/math/Vec3d;)V"))
    public void createEntity(Entity instance, Vec3d pos) {
       try {
           instance.refreshPositionAfterTeleport(pos);
       }
       catch (Exception e) {
           CreatopicoCreateUtils.LOGGER.error("DimensionalCarriageEntityMixin.createEntity error: " + e.getMessage());
       }
    }

}
