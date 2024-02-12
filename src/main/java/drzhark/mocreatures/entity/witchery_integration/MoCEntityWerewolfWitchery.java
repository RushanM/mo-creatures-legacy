package drzhark.mocreatures.entity.witchery_integration;

import drzhark.mocreatures.MoCTools;
import drzhark.mocreatures.MoCreatures;
import drzhark.mocreatures.achievements.MoCAchievements;
import drzhark.mocreatures.entity.MoCEntityMob;
import drzhark.mocreatures.entity.monster.MoCEntityWerewolf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class MoCEntityWerewolfWitchery extends MoCEntityMob {
    private boolean isTransforming;
    private boolean isHunched;
    private int transformCounter;
    private int villagerProfession = 0;
    private int attackDamage = worldObj.difficultySetting.getDifficultyId() + 3;

    public MoCEntityWerewolfWitchery(World world)
    {
        super(world);
        setSize(0.9F, 1.6F);
        isTransforming = false;
        transformCounter = 0;
    }
    
    public MoCEntityWerewolfWitchery(World world, int villagerProfession, int werewolfType)
    {
        super(world);
        setSize(0.9F, 1.6F);
        isTransforming = false;
        transformCounter = 0;
        this.villagerProfession = villagerProfession;
        setType(werewolfType);
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(40.0D);
    }
    
    @Override
    public boolean isPredator()
    {
    	return true;
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        dataWatcher.addObject(23, Byte.valueOf((byte) 0)); //hunched
    }
    
    @Override
    public void selectType()
    {
    	if (getType() == 0)
    	{	
            int chance = rand.nextInt(100);
            if (chance <= 28)
            {
            	setType(1);
            }
            else if (chance <= 56)
            {
            	setType(2);
            }
            else
            {
            	setType(3);
            }
    	}
    }

    @Override
    public ResourceLocation getTexture()
    {
    	switch (getType())
        {
	        case 1:
	            return MoCreatures.proxy.getTexture("wolfblack.png");
	        case 2:
	            return MoCreatures.proxy.getTexture("wolfbrown.png");
	        case 3:
	            return MoCreatures.proxy.getTexture("wolftimber.png");
	        default:
	            return MoCreatures.proxy.getTexture("wolfbrown.png");
        }
    }

    public boolean getIsHumanForm()
    {
        return false;
    }

    public void setHumanForm(boolean flag)
    {
    }

    public boolean getIsHunched()
    {
        return (dataWatcher.getWatchableObjectByte(23) == 1);
    }

    public void setHunched(boolean flag)
    {
        byte input = (byte) (flag ? 1 : 0);
        dataWatcher.updateObject(23, Byte.valueOf(input));
    }

    @Override
    protected void attackEntity(Entity entity, float distanceToEntity)
    {
        if ((distanceToEntity > 2.0F) && (distanceToEntity < 6F) && (rand.nextInt(15) == 0))
        {
        	if (MoCreatures.isWitcheryLoaded && entity instanceof EntityPlayer)
            {
            	if (MoCTools.isPlayerInWolfForm((EntityPlayer) entity) || MoCTools.isPlayerInWerewolfForm((EntityPlayer) entity)) //don't hunt player if is in wolf or werewolf form
            	{	
            		entityToAttack = null;
            		return;
            	}
            }
        	
            if (onGround)
            {
                setHunched(true);
                double xDistance = entity.posX - posX;
                double zDistance = entity.posZ - posZ;
                float overallHorizontalDistanceSquared = MathHelper.sqrt_double((xDistance * xDistance) + (zDistance * zDistance));
                motionX = ((xDistance / overallHorizontalDistanceSquared) * 0.5D * 0.80000001192092896D) + (motionX * 0.20000000298023221D);
                motionZ = ((zDistance / overallHorizontalDistanceSquared) * 0.5D * 0.80000001192092896D) + (motionZ * 0.20000000298023221D);
                motionY = 0.40000000596046448D;
            }
        }
        else
        {
            if (attackTime <= 0 && (distanceToEntity < 2.5D) && (entity.boundingBox.maxY > boundingBox.minY) && (entity.boundingBox.minY < boundingBox.maxY))
            {
                attackTime = 20;
                entity.attackEntityFrom(DamageSource.causeMobDamage(this), attackDamage);
            }
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource damageSource, float damageTaken)
    {
        Entity entityThatAttackedThisCreature = damageSource.getEntity();
        if ((entityThatAttackedThisCreature != null) && (entityThatAttackedThisCreature instanceof EntityPlayer))
        {
        	if (
        			(MoCTools.isPlayerInWerewolfForm((EntityPlayer) entityThatAttackedThisCreature))
        			|| (MoCTools.isPlayerInWolfForm((EntityPlayer) entityThatAttackedThisCreature))
        		)
        	{
        		damageSource = DamageSource.generic; //don't fight back if attacked by a player werewolf
        	}
        	
            EntityPlayer entityPlayer = (EntityPlayer) entityThatAttackedThisCreature;
            ItemStack itemstack = entityPlayer.getCurrentEquippedItem();
            
            damageTaken = MoCEntityWerewolf.calculateWerewolfDamageTaken(damageSource, damageTaken, itemstack);
        }
        return super.attackEntityFrom(damageSource, damageTaken);
    }

    @Override
    protected Entity findPlayerToAttack()
    {
        
        EntityPlayer entityPlayer = worldObj.getClosestVulnerablePlayerToEntity(this, 16D);
        
        EntityLivingBase entityLiving = getClosestEntityLiving(this, 16D);
        
        if ((entityPlayer != null) && canEntityBeSeen(entityPlayer))
        {
        	if (MoCTools.isPlayerInWolfForm(entityPlayer) || MoCTools.isPlayerInWerewolfForm(entityPlayer)) //don't hunt player if is in wolf or werewolf form
        	{	
        		if (
            			(MoCTools.isPlayerInWerewolfForm(entityPlayer) && entityPlayer.getMaxHealth() == 60) //checks for max level werewolf
            			|| (MoCTools.isPlayerInWolfForm(entityPlayer) && entityPlayer.getMaxHealth() == 32) //checks for max level werewolf
            		)
            	{
            		EntityLivingBase entityThatAttackedMaxLevelWerewolfPlayer = entityPlayer.getAITarget();
        			
        			if (entityThatAttackedMaxLevelWerewolfPlayer != null)
        			{
        				return entityThatAttackedMaxLevelWerewolfPlayer; //defend the max level werewolf player from the entity that attacked them
        			}
            	}
        		
        		return null;
        	}
        	else {return entityPlayer;}
        }
        
        else if ((entityLiving != null) && !(entityLiving instanceof EntityPlayer) && canEntityBeSeen(entityLiving))
        {
        	return entityLiving;
        }
        
        else
        {
            return null;
        }
    }
    
    @Override
    public boolean shouldEntityBeIgnored(Entity entity)
    {
        return 
        	(
        		!((entity instanceof EntityVillager) && !(entity instanceof MoCEntityWerewolfVillagerWitchery))
        	);
    }

    @Override
    protected String getDeathSound()
    {
        return "mocreatures:werewolfdying";
    }

    @Override
    protected Item getDropItem()
    {
        int randomNumber = rand.nextInt(12);
        
        switch (randomNumber)
        {
	        case 0: // '\0'
	            return Items.iron_hoe;
	
	        case 1: // '\001'
	            return Items.iron_shovel;
	
	        case 2: // '\002'
	            return Items.iron_axe;
	
	        case 3: // '\003'
	            return Items.iron_pickaxe;
	
	        case 4: // '\004'
	            return Items.iron_sword;
	
	        case 5: // '\005'
	            return Items.stone_hoe;
	
	        case 6: // '\006'
	            return Items.stone_shovel;
	
	        case 7: // '\007'
	            return Items.stone_axe;
	
	        case 8: // '\b'
	            return Items.stone_pickaxe;
	
	        case 9: // '\t'
	            return Items.stone_sword;
        }
        return Items.golden_apple;
    }

    @Override
    protected String getHurtSound()
    {
    	return "mocreatures:werewolfhurt";
    }

    public boolean getIsUndead()
    {
        return true;
    }

    @Override
    protected String getLivingSound()
    {
        return "mocreatures:werewolfgrunt";
    }

    public boolean IsNight()
    {
        return !worldObj.isDaytime();
    }

    @Override
    public void moveEntityWithHeading(float f, float f1)
    {
        if (onGround)
        {
            motionX *= 1.2D;
            motionZ *= 1.2D;
        }
        super.moveEntityWithHeading(f, f1);
    }

    @Override
    public void onDeath(DamageSource damageSource)
    {
        Entity entity = damageSource.getEntity();
        if ((scoreValue > 0) && (entity != null))
        {
            entity.addToPlayerScore(this, scoreValue);
        }
        if (entity != null)
        {
            entity.onKillEntity(this);
        }
        

        if ((damageSource.getEntity() != null) && (damageSource.getEntity() instanceof EntityPlayer))
        {
        	EntityPlayer player = (EntityPlayer)damageSource.getEntity();
            if (player != null) {player.addStat(MoCAchievements.kill_werewolf, 1);}
        }

        if (!worldObj.isRemote)
        {
            for (int index = 0; index < 2; index++)
            {
                Item item = getDropItem();
                if (item != null)
                {
                    dropItem(item, 1);
                }
            }

        }
    }
    
    @Override
    public void onLivingUpdate()
    {
        super.onLivingUpdate();
        if (!worldObj.isRemote)
        {
            if (!IsNight() && (rand.nextInt(250) == 0) && !isTransforming)
            {
                isTransforming = true;
            }
            if ((entityToAttack != null) && ((entityToAttack.posX - posX) > 3D) && ((entityToAttack.posZ - posZ) > 3D))
            {
                setHunched(true);
            }
            if (getIsHunched() && (rand.nextInt(50) == 0))
            {
                setHunched(false);
            }
            if (isTransforming && (rand.nextInt(3) == 0))
            {
                transformCounter++;
                if ((transformCounter % 2) == 0)
                {
                    posX += 0.29999999999999999D;
                    posY += transformCounter / 30;
                    attackEntityFrom(DamageSource.causeMobDamage(this), 0);
                }
                if ((transformCounter % 2) != 0)
                {
                    posX -= 0.29999999999999999D;
                }
                if (transformCounter > 30)
                {
                    Transform();
                    transformCounter = 0;
                    isTransforming = false;
                }
            }
            if (rand.nextInt(300) == 0)
            {
                entityAge -= 100 * worldObj.difficultySetting.getDifficultyId();
                if (entityAge < 0)
                {
                    entityAge = 0;
                }
            }
        }
    }

    private void Transform()
    {
        if (deathTime > 0) { return; }

        
        float healthForHumanForm = Math.round((getHealth() / getMaxHealth()) * 20.0D);
        
        isTransforming = false;
        
        MoCEntityWerewolfVillagerWitchery werewolfVillager = new MoCEntityWerewolfVillagerWitchery(worldObj, getType());
        werewolfVillager.copyLocationAndAnglesFrom((Entity) this);
        werewolfVillager.setProfession(villagerProfession);
        setDead();
        
        werewolfVillager.setHealth(healthForHumanForm);
        werewolfVillager.worldObj.spawnEntityInWorld((Entity) werewolfVillager); 
    }

    @Override
    protected void updateEntityActionState()
    {
        if (!isTransforming)
        {
            super.updateEntityActionState();
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbtTagCompound)
    {
        super.readEntityFromNBT(nbtTagCompound);
        setHumanForm(nbtTagCompound.getBoolean("HumanForm"));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbtTagCompound)
    {
        super.writeEntityToNBT(nbtTagCompound);
        nbtTagCompound.setBoolean("HumanForm", getIsHumanForm());
    }

    @Override
    public float getMoveSpeed()
    {
        if (getIsHunched()) { return 0.9F; }
        return 0.7F;
    }
}