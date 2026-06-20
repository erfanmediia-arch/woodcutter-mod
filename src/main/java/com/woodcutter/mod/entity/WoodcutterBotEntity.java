package com.woodcutter.mod.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class WoodcutterBotEntity extends PathfinderMob {

    private static final int SEARCH_RADIUS = 12;
    private static final int BREAK_INTERVAL = 25; // ticks between breaks
    private static final int WALK_RANGE = 20;

    private BlockPos targetBlock = null;
    private int breakTimer = 0;
    private int breakProgress = 0;
    private int idleTimer = 0;
    private BotState state = BotState.SEARCHING;

    private enum BotState {
        SEARCHING, WALKING_TO_TARGET, BREAKING, IDLE
    }

    private static final List<Block> BREAKABLE_BLOCKS = new ArrayList<>();

    static {
        // Wood logs
        BREAKABLE_BLOCKS.add(Blocks.OAK_LOG);
        BREAKABLE_BLOCKS.add(Blocks.BIRCH_LOG);
        BREAKABLE_BLOCKS.add(Blocks.SPRUCE_LOG);
        BREAKABLE_BLOCKS.add(Blocks.JUNGLE_LOG);
        BREAKABLE_BLOCKS.add(Blocks.ACACIA_LOG);
        BREAKABLE_BLOCKS.add(Blocks.DARK_OAK_LOG);
        BREAKABLE_BLOCKS.add(Blocks.MANGROVE_LOG);
        BREAKABLE_BLOCKS.add(Blocks.CHERRY_LOG);
        // Leaves
        BREAKABLE_BLOCKS.add(Blocks.OAK_LEAVES);
        BREAKABLE_BLOCKS.add(Blocks.BIRCH_LEAVES);
        BREAKABLE_BLOCKS.add(Blocks.SPRUCE_LEAVES);
        // Grass
        BREAKABLE_BLOCKS.add(Blocks.GRASS_BLOCK);
        BREAKABLE_BLOCKS.add(Blocks.SHORT_GRASS);
        BREAKABLE_BLOCKS.add(Blocks.TALL_GRASS);
        BREAKABLE_BLOCKS.add(Blocks.FERN);
        BREAKABLE_BLOCKS.add(Blocks.LARGE_FERN);
    }

    public WoodcutterBotEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setCustomName(Component.literal("§aWoodcutter Bot"));
        this.setCustomNameVisible(true);
        // Give it an axe in hand
        this.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                new ItemStack(Items.IRON_AXE));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;

        switch (state) {
            case SEARCHING -> handleSearching();
            case WALKING_TO_TARGET -> handleWalking();
            case BREAKING -> handleBreaking();
            case IDLE -> handleIdle();
        }
    }

    private void handleSearching() {
        targetBlock = findNearestTarget();
        if (targetBlock != null) {
            // Navigate to the block
            this.getNavigation().moveTo(
                    targetBlock.getX() + 0.5,
                    targetBlock.getY(),
                    targetBlock.getZ() + 0.5,
                    1.0D
            );
            state = BotState.WALKING_TO_TARGET;
            updateNameTag("§eWalking to target...");
        } else {
            // Wander randomly
            state = BotState.IDLE;
            idleTimer = 40;
            updateNameTag("§7Looking around...");
        }
    }

    private void handleWalking() {
        if (targetBlock == null || !isBlockBreakable(level().getBlockState(targetBlock))) {
            state = BotState.SEARCHING;
            return;
        }

        double dist = this.distanceToSqr(
                targetBlock.getX() + 0.5,
                targetBlock.getY() + 0.5,
                targetBlock.getZ() + 0.5
        );

        if (dist < 9.0D) { // Within 3 blocks
            this.getNavigation().stop();
            this.getLookControl().setLookAt(
                    targetBlock.getX() + 0.5,
                    targetBlock.getY() + 0.5,
                    targetBlock.getZ() + 0.5
            );
            state = BotState.BREAKING;
            breakProgress = 0;
            breakTimer = 0;
        } else if (this.getNavigation().isDone()) {
            // Can't reach, try another target
            state = BotState.SEARCHING;
        }
    }

    private void handleBreaking() {
        if (targetBlock == null) {
            state = BotState.SEARCHING;
            return;
        }

        BlockState blockState = level().getBlockState(targetBlock);
        if (!isBlockBreakable(blockState)) {
            state = BotState.SEARCHING;
            breakProgress = 0;
            return;
        }

        breakTimer++;

        // Look at the target
        this.getLookControl().setLookAt(
                targetBlock.getX() + 0.5,
                targetBlock.getY() + 0.5,
                targetBlock.getZ() + 0.5
        );

        // Swing arm animation
        if (breakTimer % 5 == 0) {
            this.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        }

        // Play digging sound
        if (breakTimer % 8 == 0) {
            float hardness = blockState.getDestroySpeed(level(), targetBlock);
            int ticksNeeded = Math.max(10, (int)(hardness * 20));

            // Send break progress to clients
            level().destroyBlockProgress(this.getId(), targetBlock,
                    (int)((breakProgress / (float) ticksNeeded) * 10));

            breakProgress++;

            if (breakProgress >= ticksNeeded) {
                // Break the block!
                level().destroyBlock(targetBlock, true, this);
                this.playSound(SoundEvents.WOOD_BREAK, 1.0F, 1.0F);
                level().destroyBlockProgress(this.getId(), targetBlock, -1);

                targetBlock = null;
                state = BotState.IDLE;
                idleTimer = 20;
                updateNameTag("§aBlock broken! Searching...");
            }
        }
    }

    private void handleIdle() {
        idleTimer--;
        if (idleTimer <= 0) {
            state = BotState.SEARCHING;
        }
    }

    private BlockPos findNearestTarget() {
        BlockPos myPos = this.blockPosition();
        BlockPos nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
            for (int y = -4; y <= 8; y++) {
                for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                    BlockPos pos = myPos.offset(x, y, z);
                    BlockState state = level().getBlockState(pos);

                    if (isBlockBreakable(state)) {
                        double dist = pos.distSqr(myPos);
                        if (dist < nearestDist) {
                            nearestDist = dist;
                            nearest = pos;
                        }
                    }
                }
            }
        }
        return nearest;
    }

    private boolean isBlockBreakable(BlockState state) {
        return BREAKABLE_BLOCKS.contains(state.getBlock());
    }

    private void updateNameTag(String text) {
        this.setCustomName(Component.literal(text + " §7[Bot]"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return true;
    }
}
