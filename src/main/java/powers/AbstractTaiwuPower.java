package powers;

import Utils.Log;
import cards.AbstractTaiwuCard;
import cards.AttackType;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.StrengthPower;

/**
 * @author 57680
 * @version 1.0
 * Create by 2021/9/15 23:38
 */
public abstract class AbstractTaiwuPower extends AbstractPower
{
    protected PowerStrings powerStrings;
    protected boolean autoDecreaseAfterTurn;
    protected boolean autoDecreaseBeforeTurn;
    public AbstractTaiwuPower(String id, AbstractCreature owner, int amt) {
        powerStrings = CardCrawlGame.languagePack.getPowerStrings(id);
        name = powerStrings.NAME;
        this.ID = id;
        this.owner = owner;
        this.amount = amt;
        this.autoDecreaseAfterTurn = false;
        this.autoDecreaseBeforeTurn = false;
        this.updateDescription();
        this.region128 = new TextureAtlas.AtlasRegion(ImageMaster.loadImage("img/powers/"+id+"_84.png"), 0, 0, 84, 84);
        this.region48 = new TextureAtlas.AtlasRegion(ImageMaster.loadImage("img/powers/"+id+"_32.png"), 0, 0, 32, 32);
    }
    public AbstractTaiwuPower(String id, AbstractCreature owner) {
        this(id,owner,1);
    }
    public AttackType beforeGetAttackType(AttackType src){return src;}

    public static AbstractPower initPower(String id,AbstractCreature owner)
    {
        try
        {
            Class classType = Class.forName("powers."+id+"Power");
            Class[] paramTypes = new Class[]{String.class,AbstractCreature.class};
            Object[] params = new Object[]{id,owner};
            AbstractTaiwuPower power = (AbstractTaiwuPower) classType.getConstructor(paramTypes).newInstance(params);
            Log.log(power.name);
            return power;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new StrengthPower(owner,1);
        }
    }

    public static AbstractPower initPower(String id,AbstractCreature owner,int amount)
    {
        try
        {
            Class classType = Class.forName("powers."+id+"Power");
            Class[] paramTypes = new Class[]{String.class,AbstractCreature.class,int.class};
            Object[] params = new Object[]{id,owner,amount};
            AbstractTaiwuPower power = (AbstractTaiwuPower) classType.getConstructor(paramTypes).newInstance(params);
            Log.log(power.name);
            return power;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new StrengthPower(owner,1);
        }
    }

    public AttackType onAttackTypeGet(AttackType attackType){return attackType;}

    @Override
    public void atEndOfTurn(boolean isPlayer)
    {
        if(!autoDecreaseAfterTurn||!isPlayer)
        {
           return;
        }
        addToTop(new ReducePowerAction(owner,owner,ID,1));
    }

    @Override
    public void atStartOfTurn()
    {
        if(!autoDecreaseBeforeTurn)
            return;
        addToTop(new ReducePowerAction(owner,owner,ID,1));
    }
}
