package com.gmail.sneakdevs.carnivoroussponges.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WetSpongeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.Shapes;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Random;

@Mixin(WetSpongeBlock.class)
public class WetSpongeBlockMixin extends Block {

    public WetSpongeBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isRandomlyTicking(BlockState blockState) {
        return true;
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (randomSource.nextInt(8) == 0) {
            //movement
            BlockPos blockPos1 = new BlockPos(blockPos.offset(randomSource.nextInt(3) - 1, randomSource.nextInt(3) - 1, randomSource.nextInt(3) - 1));
            if (serverLevel.getFluidState(blockPos1).is(Fluids.WATER) && serverLevel.getBlockState(blockPos1).getCollisionShape(serverLevel, blockPos1).equals(Shapes.empty())) {
                serverLevel.setBlockAndUpdate(blockPos1, Blocks.WET_SPONGE.defaultBlockState());
                serverLevel.setBlockAndUpdate(blockPos, Blocks.WATER.defaultBlockState());
            }
        } else {
            //reproduction
            if (randomSource.nextInt(21) == 0) {
                BlockPos blockPos1 = new BlockPos(blockPos.offset(randomSource.nextInt(9) * (randomSource.nextInt(3) - 1), randomSource.nextInt(4) * (randomSource.nextInt(3) - 1), randomSource.nextInt(9) * (randomSource.nextInt(3) - 1)));
                if (serverLevel.getBlockState(blockPos1).equals(Blocks.WATER.defaultBlockState())) {
                    serverLevel.setBlockAndUpdate(blockPos1, Blocks.WET_SPONGE.defaultBlockState());
                }
            } else {
                //sink
                BlockPos blockPos1 = blockPos.below();
                if (serverLevel.getFluidState(blockPos1).is(Fluids.WATER) && serverLevel.getBlockState(blockPos1).getCollisionShape(serverLevel, blockPos1).equals(Shapes.empty())) {
                    serverLevel.setBlockAndUpdate(blockPos1, Blocks.WET_SPONGE.defaultBlockState());
                    serverLevel.setBlockAndUpdate(blockPos, Blocks.WATER.defaultBlockState());
                } else {
                    //die
                    int surroundingCount = 0;
                    int waterCount = 0;
                    for (int i = -2; i < 3; i++) {
                        for (int j = -2; j < 3; j++) {
                            for (int k = -2; k < 3; k++) {
                                if (!serverLevel.getFluidState(blockPos.offset(i, j, k)).is(Fluids.WATER)) {
                                    surroundingCount++;
                                    if (serverLevel.getBlockState(blockPos.offset(i, j, k)).getBlock() == Blocks.WET_SPONGE) surroundingCount += 7;
                                } else {
                                    waterCount++;
                                }
                            }
                        }
                    }
                    if (waterCount > 3 && randomSource.nextInt((int) Math.pow(surroundingCount, 0.85)) > 35) {
                        serverLevel.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
    }

    public void stepOn(Level level, BlockPos blockPos, BlockState blockState, Entity entity) {
        //consume meat
        Random random = new Random();
        if (random.nextInt(18) == 0) {
            if (entity instanceof LivingEntity && entity.isUnderWater()) {
                if (!level.isClientSide()) {
                    float fl = 1.0f;
                    BlockPos pos1 = blockPos.offset(1, 1, 0);
                    if (level.getFluidState(pos1).is(Fluids.WATER) && level.getBlockState(pos1).getCollisionShape(level, pos1).equals(Shapes.empty())) {
                        level.setBlockAndUpdate(pos1, Blocks.WET_SPONGE.defaultBlockState());
                    } else {
                        fl++;
                    }
                    BlockPos pos2 = blockPos.offset(-1, 1, 0);
                    if (level.getFluidState(pos2).is(Fluids.WATER) && level.getBlockState(pos2).getCollisionShape(level, pos2).equals(Shapes.empty())) {
                        level.setBlockAndUpdate(pos2, Blocks.WET_SPONGE.defaultBlockState());
                    } else {
                        fl++;
                    }
                    BlockPos pos3 = blockPos.offset(0, 1, 1);
                    if (level.getFluidState(pos3).is(Fluids.WATER) && level.getBlockState(pos3).getCollisionShape(level, pos3).equals(Shapes.empty())) {
                        level.setBlockAndUpdate(pos3, Blocks.WET_SPONGE.defaultBlockState());
                    } else {
                        fl++;
                    }
                    BlockPos pos4 = blockPos.offset(0, 1, -1);
                    if (level.getFluidState(pos4).is(Fluids.WATER) && level.getBlockState(pos4).getCollisionShape(level, pos4).equals(Shapes.empty())) {
                        level.setBlockAndUpdate(pos4, Blocks.WET_SPONGE.defaultBlockState());
                    } else {
                        fl++;
                    }
                    BlockPos pos5 = blockPos.offset(0, 2, 0);
                    if (level.getFluidState(pos5).is(Fluids.WATER) && level.getBlockState(pos5).getCollisionShape(level, pos5).equals(Shapes.empty())) {
                        level.setBlockAndUpdate(pos5, Blocks.WET_SPONGE.defaultBlockState());
                    } else {
                        fl++;
                    }
                    entity.hurt(DamageSource.STARVE, fl);
                    ((LivingEntity)entity).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 140, 255, true, false, false));
                    ((LivingEntity)entity).addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 140, 1, false, false, false));
                }
            }
        }
        super.stepOn(level, blockPos, blockState, entity);
    }
}