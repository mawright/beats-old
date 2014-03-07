package edu.berkeley.path.beats.actuator;

/**
 * Created with IntelliJ IDEA.
 * User: gomes
 * Date: 3/6/14
 * Time: 5:24 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class NEMA {

    public static enum ID {NULL,_1,_2,_3,_4,_5,_6,_7,_8};

    public static ID string_to_nema(String str){
        if(str==null)
            return ID.NULL;
        if(str.isEmpty())
            return ID.NULL;
        if(!str.startsWith("_"))
            str = "_"+str;
        ID nema;
        try{
            nema = ID.valueOf(str);
        }
        catch(IllegalArgumentException  e){
            nema = ID.NULL;
        }
        return nema;
    }

    public static boolean is_compatible(SignalPhase pA, SignalPhase pB){
        ID nemaA = pA.getNEMA();
        ID nemaB = pB.getNEMA();

        if(nemaA.compareTo(nemaB)==0)
            return true;

        if( !pA.isProtected() || !pB.isProtected() )
            return true;

        switch(nemaA){
            case _1:
            case _2:
                if(nemaB.compareTo(ID._5)==0 || nemaB.compareTo(ID._6)==0)
                    return true;
                else
                    return false;
            case _3:
            case _4:
                if(nemaB.compareTo(ID._7)==0 || nemaB.compareTo(ID._8)==0 )
                    return true;
                else
                    return false;
            case _5:
            case _6:
                if(nemaB.compareTo(ID._1)==0 || nemaB.compareTo(ID._2)==0 )
                    return true;
                else
                    return false;
            case _7:
            case _8:
                if(nemaB.compareTo(ID._3)==0 || nemaB.compareTo(ID._4)==0 )
                    return true;
                else
                    return false;
            case NULL:
                break;
            default:
                break;
        }
        return false;
    }

    public static ID int_to_nema(int x){
        switch(x){
            case 1:
                return ID._1;
            case 2:
                return ID._2;
            case 3:
                return ID._3;
            case 4:
                return ID._4;
            case 5:
                return ID._5;
            case 6:
                return ID._6;
            case 7:
                return ID._7;
            case 8:
                return ID._8;
            default:
                return ID.NULL;
        }
    }
}
