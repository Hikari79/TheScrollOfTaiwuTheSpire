package controller;

import DynamicVariables.*;
import Utils.Log;
import basemod.BaseMod;
import basemod.interfaces.*;
import cards.AbstractTaiwuCard;
import cards.AttackType;
import cards.CardColor;
import characters.Taiwu;
import com.badlogic.gdx.Gdx;
import com.evacipated.cardcrawl.modthespire.lib.SpireEnum;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.ui.buttons.ReturnToMenuButton;
import org.apache.logging.log4j.LogManager;
import relics.FuYuJianBing;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SpireInitializer
public class TheScrollOfTaiwuTheSpire implements EditCardsSubscriber, EditCharactersSubscriber, EditRelicsSubscriber,EditStringsSubscriber, EditKeywordsSubscriber
{

    public static TheScrollOfTaiwuTheSpire instance;
    public HashMap<String,String[]> cardsData;
    public static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(TheScrollOfTaiwuTheSpire.class.getName());
    public static TheScrollOfTaiwuTheSpire getInstance()
    {
        return instance;
    }
    public static final String[] dynamicV = new String[]{
            "D","B","M","C1","C2","S1","S2","S3","S4"
    };
    public TheScrollOfTaiwuTheSpire()
    {
        BaseMod.subscribe(this);
        BattleController battleController = new BattleController();
        CardColor.initalize();
    }

    public static boolean isDynamicV(String text,boolean mark)
    {
        if(!mark)
        {
            for(String s:dynamicV)
                if(text.equals(s))
                    return true;
        }
        else
        {
            for (String s:dynamicV)
            {
                if(text.equals("!"+s+"!"))
                    return true;
            }
        }
        return false;
    }



    /**
     * ???mod????????????????????????????????????
     */
    public static void initialize()
    {
        instance = new TheScrollOfTaiwuTheSpire();
    }

    private void loadCardData()
    {
        try
        {
            BufferedReader br = new BufferedReader(Gdx.files.internal("data/cards.CSV").reader("utf-8"));
            String[] data = new String[0];
            try
            {
                br.readLine();
                br.readLine();
                data = br.readLine().split(",",-1);
                logger.info(Arrays.toString(data));
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            cardsData = new HashMap<>();
            while (data.length>0&&!data[0].equals(""))
            {
                logger.info(Arrays.toString(data));
                cardsData.put(data[0],data);
                String temp = br.readLine();
                if(temp!=null)
                    data = temp.split(",",-1);
                else
                    data = new String[0];
            }
            br.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void receiveEditCards()
    {
        logger.info("start adding cards");

        for(Map.Entry<String,String[]> e : cardsData.entrySet())
        {
            AbstractTaiwuCard card = AbstractTaiwuCard.initCard(e.getKey(),e.getValue());
            if(card==null)
                Log.log("card "+e.getKey()+" not be added");
            else
            {
                BaseMod.addCard(card);
                logger.info("add card:"+e.getKey());
            }
        }
        BaseMod.addDynamicVariable(new S1());
        BaseMod.addDynamicVariable(new S2());
        BaseMod.addDynamicVariable(new S3());
        BaseMod.addDynamicVariable(new S4());
        BaseMod.addDynamicVariable(new C1());
        BaseMod.addDynamicVariable(new C2());
        logger.info("adding cards done");
    }

    @Override
    public void receiveEditCharacters()
    {
        BaseMod.addCharacter(new Taiwu(CardCrawlGame.playerName), "img/char/??????selectButton.png", "img/char/??????_portrait.png",TAIWU_CLASS);
    }

    @SpireEnum
    public static AbstractPlayer.PlayerClass TAIWU_CLASS;

    @Override
    public void receiveEditRelics()
    {
        logger.info("start adding relics");
        BaseMod.addRelicToCustomPool(new FuYuJianBing(),CardColor.QUANZHANG);
        logger.info("adding relics done");
    }

    @Override
    public void receiveEditStrings()
    {
        loadCardData();
        BaseMod.loadCustomStrings(CardStrings.class,setCardDescription());
        BaseMod.loadCustomStringsFile(RelicStrings.class,"taiwuLocalization/zhs/taiwuRelics.json");
        BaseMod.loadCustomStringsFile(PowerStrings.class,"taiwuLocalization/zhs/taiwuPowers.json");
    }
    private String setCardDescription()
    {
        HashMap<String,CardStrings> cardStrings = new HashMap<>();
        for(Map.Entry<String,String[]> e : cardsData.entrySet())
        {
            String id = e.getKey();
            String[] data  =e.getValue();
            CardStrings temp = new CardStrings();
            temp.NAME = id;
            temp.DESCRIPTION = getGongFaDescription(data[20],data[19],data[22]);
            temp.UPGRADE_DESCRIPTION = data[27].equals("")?temp.DESCRIPTION:getGongFaDescription(data[27],data[19],data[22]);
            temp.EXTENDED_DESCRIPTION = getGongFaDescriptionEX(data[23],data[22],data[28]);
            cardStrings.put(id,temp);
        }
        return BaseMod.gson.toJson(cardStrings);
    }

    private String[] getGongFaDescriptionEX(String src, String costAttackType,String updateDescription)
    {
        if(src.equals(""))
            return null;
        String[] result = new String[2];
        StringBuilder sb = new StringBuilder();
        sb.append(" ?????? ???");
        String[] attackTypes = costAttackType.split("&");
        for(int i=0;i<attackTypes.length;i++)
        {
            String[] attackType = attackTypes[i].split("\\*");
            for(int j=0;j<Log.getInt(attackType[1]);j++)
            {
                sb.append(" ");
                sb.append(attackType[0]);
            }
        }
        sb.append(" NL ");
        String[] tokens = src.split(" ");
        for(String token:tokens)
        {
            for(String dv:dynamicV)
            {
                if (token.equals(dv))
                {
                    token = "!" + token + "!";
                    break;
                }
            }
            sb.append(token);
            sb.append(' ');
        }
        result[0] = sb.toString();
        if(updateDescription.equals(""))
            result[1] = result[0];
        else
        {
            sb = new StringBuilder();
            sb.append(" ?????? ???");
            for(int i=0;i<attackTypes.length;i++)
            {
                String[] attackType = attackTypes[i].split("\\*");
                for(int j=0;j<Log.getInt(attackType[1]);j++)
                {
                    sb.append(" ");
                    sb.append(attackType[0]);
                }
            }
            sb.append(" NL ");
            tokens = updateDescription.split(" ");
            for(String token:tokens)
            {
                for(String dv:dynamicV)
                {
                    if (token.equals(dv))
                    {
                        token = "!" + token + "!";
                        break;
                    }
                }
                sb.append(token);
                sb.append(' ');
            }
            result[1] = sb.toString();
        }
        return result;
    }

    private String getGongFaDescription(String src,String getAttackType,String costAttackType)
    {
        String[] tokens = src.split(" ");
        StringBuilder sb = new StringBuilder();
        if(!getAttackType.equals(""))
        {
            sb.append("?????????");
            String[] attackTypes = getAttackType.split("&");
            for(int i=0;i<attackTypes.length;i++)
            {
                String[] attackType = attackTypes[i].split("\\*");
                for(int j=0;j<Log.getInt(attackType[1]);j++)
                {
                    sb.append(" ");
                    sb.append(attackType[0]);
                }

            }
            sb.append(" NL ");
        }
        for(String token:tokens)
        {
            for(String dv:dynamicV)
            {
                if (token.equals(dv))
                {
                    token = "!" + token + "!";
                    break;
                }
            }
            sb.append(token);
            sb.append(' ');
        }
        if(costAttackType.equals(""))
        {
            return sb.toString().trim();
        }
        else
        {
            sb.append("NL ");
            sb.append("?????? ???");
            String[] attackTypes = costAttackType.split("&");
            for(int i=0;i<attackTypes.length;i++)
            {
                String[] attackType = attackTypes[i].split("\\*");
                for(int j=0;j<Log.getInt(attackType[1]);j++)
                {
                    sb.append(" ");
                    sb.append(attackType[0]);
                }
            }
            sb.append(" ");
            return sb.toString();
        }
    }

    @Override
    public void receiveEditKeywords()
    {
        AttackType[] attackTypes = AttackType.values();
        for (AttackType attackType : attackTypes)
            if(!attackType.toString().equals("???"))
                BaseMod.addKeyword(new String[]{attackType.toString()}, "?????????????????????????????????????????????????????????????????????????????????????????????????????????");
            else
                BaseMod.addKeyword(new String[]{attackType.toString()}, "??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????1???");
        BaseMod.addKeyword(new String[]{"??????"},"???????????????????????????????????????????????????????????????????????????????????????Left-Ctrl??????????????????????????????");
        BaseMod.addKeyword(new String[]{"??????"},"????????? #y?????? ?????????????????? #y?????? ???");
        BaseMod.addKeyword(new String[]{"??????"},"??????????????????????????????????????????????????????????????? #b1 ????????????");
    }
}
