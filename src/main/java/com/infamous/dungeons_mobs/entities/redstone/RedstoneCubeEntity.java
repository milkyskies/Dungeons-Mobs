package com.infamous.dungeons_mobs.entities.redstone;

import com.infamous.dungeons_mobs.mod.ModEntityTypes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.potion.Effects;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;

import java.util.EnumSet;

public class RedstoneCubeEntity extends MonsterEntity {
    protected int rollingDuration;

    private static final DataParameter<Boolean> IS_ROLLING = EntityDataManager.createKey(RedstoneCubeEntity.class, DataSerializers.BOOLEAN);

    public RedstoneCubeEntity(World worldIn) {
        super(ModEntityTypes.REDSTONE_CUBE.get(), worldIn);
    }

    public RedstoneCubeEntity(EntityType<? extends RedstoneCubeEntity> type, World worldIn) {
        super(type, worldIn);
        this.moveController = new RedstoneCubeEntity.MoveHelperController(this);
        this.stepHeight = 1.0F;
    }

    public static AttributeModifierMap.MutableAttribute setCustomAttributes() {
        return MonsterEntity.func_234295_eP_()
                .createMutableAttribute(Attributes.MAX_HEALTH, 4.0D * 4.0D)
                .createMutableAttribute(Attributes.MOVEMENT_SPEED, (double)(0.2F + 0.1F * (float)2.0D * 0.5D))
                .createMutableAttribute(Attributes.ATTACK_DAMAGE, 4.0D);
    }

    @Override
    protected void registerData() {
        super.registerData();
        this.dataManager.register(IS_ROLLING, false);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(2, new RedstoneCubeEntity.AttackGoal(this));
        this.goalSelector.addGoal(3, new RedstoneCubeEntity.FaceRandomGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, 10, true, false, (entity) -> Math.abs(entity.getPosY() - this.getPosY()) <= 4.0D));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolemEntity.class, true));
    }

    /**
     * Applies a velocity to the entities, to push them away from eachother.
     */
    public void applyEntityCollision(Entity entityIn) {
        super.applyEntityCollision(entityIn);
        if (entityIn instanceof IronGolemEntity && this.canDamagePlayer()) {
            this.dealDamage((IronGolemEntity)entityIn);
        }
    }

    /**
     * Called by a player entity when they collide with an entity
     */
    public void onCollideWithPlayer(PlayerEntity entityIn) {
        if (this.canDamagePlayer()) {
            this.dealDamage(entityIn);
        }

    }

    protected void dealDamage(LivingEntity entityIn) {
        if (this.isAlive()) {
            int i = 2; // Using biggest slime size
            if (this.getDistanceSq(entityIn) < 0.6D * (double)i * 0.6D * (double)i && this.canEntityBeSeen(entityIn) && entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), this.getAttackDamageAmount())) {
                this.playSound(SoundEvents.BLOCK_STONE_HIT, 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
                this.applyEnchantments(this, entityIn);
            }
        }

    }

    protected float getAttackDamageAmount() {
        return (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    protected float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn) {
        return 0.625F * sizeIn.height;
    }

    /**
     * Indicates weather the slime is able to damage the player (based upon the slime's size)
     */
    protected boolean canDamagePlayer() {
        return this.isServerWorld();
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.BLOCK_STONE_HIT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.BLOCK_STONE_BREAK;
    }

    /**
     * The speed it takes to move the entityliving's rotationPitch through the faceEntity method. This is only currently
     * use in wolves.
     */
    public int getVerticalFaceSpeed() {
        return 0;
    }


    static class AttackGoal extends Goal {
        private final RedstoneCubeEntity redstoneCubeEntity;
        private int growTieredTimer;

        AttackGoal(RedstoneCubeEntity cubeIn) {
            this.redstoneCubeEntity = cubeIn;
            this.setMutexFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean shouldExecute() {
            LivingEntity livingentity = this.redstoneCubeEntity.getAttackTarget();
            if (livingentity == null) {
                return false;
            } else if (!livingentity.isAlive()) {
                return false;
            } else {
                return (!(livingentity instanceof PlayerEntity) || !((PlayerEntity) livingentity).abilities.disableDamage);
            }
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void startExecuting() {
            this.growTieredTimer = 300;
            super.startExecuting();
        }

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        public boolean shouldContinueExecuting() {
            LivingEntity livingentity = this.redstoneCubeEntity.getAttackTarget();
            if (livingentity == null) {
                return false;
            } else if (!livingentity.isAlive()) {
                return false;
            } else if (livingentity instanceof PlayerEntity && ((PlayerEntity)livingentity).abilities.disableDamage) {
                return false;
            } else {
                return --this.growTieredTimer > 0;
            }
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void tick() {
            this.redstoneCubeEntity.faceEntity(this.redstoneCubeEntity.getAttackTarget(), 10.0F, 10.0F);
            ((RedstoneCubeEntity.MoveHelperController)this.redstoneCubeEntity.getMoveHelper()).setDirection(this.redstoneCubeEntity.rotationYaw, this.redstoneCubeEntity.canDamagePlayer());

        }
    }

    static class FaceRandomGoal extends Goal {
        private final RedstoneCubeEntity redstoneCubeEntity;
        private float chosenDegrees;
        private int nextRandomizeTime;

        public FaceRandomGoal(RedstoneCubeEntity cubeIn) {
            this.redstoneCubeEntity = cubeIn;
            this.setMutexFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean shouldExecute() {
            return this.redstoneCubeEntity.getAttackTarget() == null && (this.redstoneCubeEntity.isOnGround() || this.redstoneCubeEntity.isInWater() || this.redstoneCubeEntity.isInLava() || this.redstoneCubeEntity.isPotionActive(Effects.LEVITATION));
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void tick() {
            if (--this.nextRandomizeTime <= 0) {
                this.nextRandomizeTime = 40 + this.redstoneCubeEntity.getRNG().nextInt(60);
                this.chosenDegrees = (float)this.redstoneCubeEntity.getRNG().nextInt(360);
            }

            ((RedstoneCubeEntity.MoveHelperController)this.redstoneCubeEntity.getMoveHelper())
                    .setDirection(this.chosenDegrees, false);
        }
    }

    static class MoveHelperController extends MovementController {
        private float yRot;
        private final RedstoneCubeEntity redstoneCubeEntity;
        private boolean isAggressive;

        public MoveHelperController(RedstoneCubeEntity cubeIn) {
            super(cubeIn);
            this.redstoneCubeEntity = cubeIn;
            this.yRot = 180.0F * cubeIn.rotationYaw / (float)Math.PI;
        }

        public void setDirection(float yRotIn, boolean aggressive) {
            this.yRot = yRotIn;
            this.isAggressive = aggressive;
        }

        public void setSpeed(double speedIn) {
            this.speed = speedIn;
            this.action = MovementController.Action.MOVE_TO;
        }

        public void tick() {
            this.mob.rotationYaw = this.limitAngle(this.mob.rotationYaw, this.yRot, 90.0F);
            this.mob.rotationYawHead = this.mob.rotationYaw;
            this.mob.renderYawOffset = this.mob.rotationYaw;
            if (this.action == Action.WAIT) {
                this.mob.setMoveForward(0.0F);
            } else if(this.action == Action.MOVE_TO) {
                this.action = MovementController.Action.WAIT;
                if (this.mob.isOnGround() || this.mob.isInWater()) {
                    this.mob.setAIMoveSpeed((float) (this.speed * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                    if(!this.redstoneCubeEntity.isRolling() && this.redstoneCubeEntity.shouldRoll()){
                        this.redstoneCubeEntity.startRolling(20);
                    }
                }
            }
        }
    }

    @Override
    public void livingTick() {
        super.livingTick();
        if(this.isRolling()){
            if(this.world.isRemote){
                this.world.addParticle(RedstoneParticleData.REDSTONE_DUST,
                        this.getPosXRandom(0.5D) + 1.0D,
                        this.getPosYRandom() - 0.25D + 1.0D,
                        this.getPosZRandom(0.5D) + 1.0D,
                        (this.rand.nextDouble() - 0.5D) * 2.0D,
                        -this.rand.nextDouble(),
                        (this.rand.nextDouble() - 0.5D) * 2.0D);
            }
        }
        if (this.rollingDuration > 0) {
            --this.rollingDuration;
            this.updateRoll();
        }
        else{
            this.stopRolling();
        }
    }

    public void startRolling(int timeIn) {
        this.rollingDuration = timeIn;
        if (!this.world.isRemote) {
            this.setIsRolling(true);
        }
    }

    public void stopRolling() {
        this.rollingDuration = 0;
        if (!this.world.isRemote) {
            this.setIsRolling(false);
        }
    }

    public boolean shouldRoll(){
        return (this.isOnGround() || this.isInWater());
    }

    protected void updateRoll() {
        if (!this.shouldRoll()) {
            this.rollingDuration = 0;
        }

        if (!this.world.isRemote && this.rollingDuration <= 0) {
            this.setIsRolling(false);
        }
    }

    public void setIsRolling(boolean isRolling){
        this.dataManager.set(IS_ROLLING, isRolling);
    }

    public boolean isRolling() {
        return this.dataManager.get(IS_ROLLING);
    }

}
