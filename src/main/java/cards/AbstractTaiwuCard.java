package cards;

import Utils.Log;
import basemod.abstracts.CustomCard;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import controller.BattleController;
import controller.TheScrollOfTaiwuTheSpire;

import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author 57680
 * @version 1.0
 * Create by 2021/9/9 16:49
 */
public abstract class AbstractTaiwuCard extends CustomCard
{
    protected String id;
    protected String[] data;
    protected CardStrings cardStrings;
    protected String name;
    protected int cost;
    protected String description;
    protected String imgPath;
    protected CardColor cardColor;
    protected CardType cardType;
    protected CardRarity cardRarity;
    protected CardTarget cardTarget;
    protected int damageUpdateValue;
    protected int blockUpdateValue;
    protected int magicUpdateValue;
    protected int customValue1;
    protected int customValue1Updated;
    protected int customValue2;
    protected int customValue2Updated;
    protected AttackType[] getShiTyp;//获得式的种类
    protected int[] getShiCount;//获得的式的数量
    protected AbstractGameAction.AttackEffect basicAttackEffect;
    protected AttackType[] costShiTyp;//获得式的种类
    protected int[] costShiCount;//获得的式的数量
    protected ArrayList<Integer> damageSection;
    protected ArrayList<Integer> damageHeavy;
    protected AbstractGameAction.AttackEffect updatedAttackEffect;
    protected String[] animationString;

    public AbstractTaiwuCard(String id, String name, String img, int cost, String rawDescription, CardType type, CardColor color, CardRarity rarity, CardTarget target)
    {
        super(id, name, img, cost, rawDescription, type, color, rarity, target);
        this.id = id;
        cardStrings = CardCrawlGame.languagePack.getCardStrings("id");
        this.cost = cost;
        imgPath = img;
        description = rawDescription;
        cardColor = color;
        cardType = type;
        cardRarity = rarity;
        cardTarget = target;
    }

    public static AbstractTaiwuCard initCard(String id)
    {
        String[] data = TheScrollOfTaiwuTheSpire.instance.cardsData.getOrDefault(id,null);
        if(data==null)
            return null;
        else
            return initCard(id,data);
    }
    public static AbstractTaiwuCard initCard(String id,String[] data)
    {
        try
        {
            CardStrings cardStrings = CardCrawlGame.languagePack.getCardStrings(id);
            String name = cardStrings.NAME;
            int cost = Integer.parseInt(data[3]);
            String description = cardStrings.DESCRIPTION;
            String imgPath = "img/cards/"+id+".png";
            CardColor cardColor = Enum.valueOf(CardColor.class,data[5]);
            CardType cardType = Enum.valueOf(CardType.class,data[6]);
            CardRarity cardRarity = Enum.valueOf(CardRarity.class,data[7]);
            CardTarget cardTarget = Enum.valueOf(CardTarget.class,data[8]);
            Class classType = Class.forName("cards."+data[1]);
            Class[] paramTypes = new Class[]{String.class,String.class,String.class,int.class,String.class,CardType.class, CardColor.class,CardRarity.class,CardTarget.class};
            Object[] params = new Object[]{id,name,imgPath,cost,description,cardType,cardColor,cardRarity,cardTarget};
            AbstractTaiwuCard card = (AbstractTaiwuCard) classType.getConstructor(paramTypes).newInstance(params);
            card.data = data;
            card.baseDamage = card.damage = getInt(data[9]);
            card.damageUpdateValue = getInt(data[10]);
            card.baseBlock = card.block = getInt(data[11]);
            card.blockUpdateValue = getInt(data[12]);
            card.baseMagicNumber = card.magicNumber = getInt(data[13]);
            card.magicUpdateValue = getInt(data[14]);
            card.customValue1 = getInt(data[15]);
            card.customValue1Updated = getInt(data[16]);
            card.customValue2 = getInt(data[17]);
            card.customValue2Updated = getInt(data[18]);
            if(data[19].equals(""))
            {
                card.getShiTyp = null;
                card.getShiCount = null;
            }
            else
            {
                String[] tokens = data[19].split("&");
                card.getShiTyp = new AttackType[tokens.length];
                card.getShiCount = new int[tokens.length];
                for(int i=0;i<tokens.length;i++)
                {
                    String[] token = tokens[i].split("\\*");

                    card.getShiTyp[i]=Enum.valueOf(AttackType.class,token[0]);
                    card.getShiCount[i] = getInt(token[1]);
                }
            }
            if(!data[21].equals(""))
                card.basicAttackEffect = Enum.valueOf(AbstractGameAction.AttackEffect.class,data[21]);
            else
                card.basicAttackEffect = null;

            if(data[22].equals(""))
            {
                card.costShiTyp = null;
                card.costShiCount = null;
            }
            else
            {
                String[] tokens = data[22].split("&");
                card.costShiTyp = new AttackType[tokens.length];
                card.costShiCount = new int[tokens.length];
                for(int i=0;i<tokens.length;i++)
                {
                    String[] token = tokens[i].split("\\*");

                    card.costShiTyp[i]=Enum.valueOf(AttackType.class,token[0]);
                    card.costShiCount[i] = getInt(token[1]);
                }
            }
            if(data[24].equals(""))
                card.damageSection = null;
            else
            {
                String[] values = data[24].split("\\|");
                card.damageSection = new ArrayList<Integer>();
                card.damageSection.add(getInt(values[0]));
                card.damageSection.add(getInt(values[1]));
                card.damageSection.add(getInt(values[2]));
                card.damageSection.add(getInt(values[3]));
                card.damageHeavy = (ArrayList<Integer>) integerAllocationAlgorithm(card.damage, card.damageSection);
            }
            if(data[25].equals(""))
                card.updatedAttackEffect = null;
            else
                card.updatedAttackEffect = Enum.valueOf(AbstractGameAction.AttackEffect.class,data[25]);
            if(data[26].equals(""))
                card.animationString = null;
            card.animationString = data[26].split("&");
            return card;
        } catch (Exception e)
        {
            Log.log("sorry, the card "+id+" is not complete");
            e.printStackTrace();
        }
        return null;
    }
    private static List<Integer> integerAllocationAlgorithm (Integer sum, List<Integer> percent) {
        int rest = sum;
        List<Integer> stepValue = new ArrayList<>();
        for (int i = 0; i < percent.size(); i++) {
            int value = 0;
            if (rest > 0) {
                value = (int)Math.floor(sum * percent.get(i) / 100);
                if (value == 0) {
                    value = 1;
                }
                if ((rest - value) <= 0) {
                    value = rest;
                    rest = 0;
                } else {
                    rest = rest - value;
                }
                if (i == (percent.size() - 1) && rest > 0) {
                    value = value + rest;
                }
            }
            stepValue.add(value);
        }
        return stepValue;
    }

    @Override
    public AbstractCard makeCopy() {
        return initCard(id,data);
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeDamage(damageUpdateValue);
            this.upgradeBlock(blockUpdateValue);
            this.upgradeMagicNumber(magicUpdateValue);
        }
    }

    @Override
    public boolean canUse(AbstractPlayer p, AbstractMonster m)
    {
        boolean result = super.canUse(p,m);
        if(isCtrlPressed())
        {
            return result&&hasEnoughShi(p,costShiTyp,costShiCount);
        }
        return result;
    }

    public boolean isCtrlPressed()
    {
        if(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT))
        {
            return true;
        }
        return false;
    }
    private boolean hasEnoughShi(AbstractPlayer p, AttackType[] costShiType, int[] costShiCount)
    {
        if(costShiType==null)
            return true;
        ArrayList<AttackType> needShiCollection = new ArrayList<>();
        for(int i=0;i<costShiType.length;i++)
            for(int j=0;j<costShiCount[i];j++)
                needShiCollection.add(costShiType[i]);
        Iterator<AttackType> i = needShiCollection.iterator();
        ArrayList<AttackType> allShi = BattleController.instance.getAllAttackType();
        while (i.hasNext())
        {
            AttackType e = i.next();
            if(allShi.contains(e))
            {
                allShi.remove(e);
            }
            else
            {
                cantUseMessage = "没有足够的式来施展这张牌！";
                return false;
            }
        }
        return true;

    }

    public static int getInt(String s)
    {
        if(s.equals(""))
            return 0;
        try
        {
            return Integer.parseInt(s);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }
    }
}