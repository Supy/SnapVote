package uct.snapvote.util;

/**
 * Created by Ben on 2013/09/01.
 */
public class FastMath {



    private static final float a = (float)(1+ Math.sqrt(4-2 * Math.sqrt(2)))/2;
    private static final float b = (float)Math.sqrt(0.5);

    public static int fastnorm(int x, int y)
    {
        if(x < 0) x = -x;
        if(y < 0) y = -y;

        float m2 = b * (x+y);

        if (x > y)
        {
            if(x > m2)
                return (int)a*x;
            else
                return (int)(a*m2);
        }
        else
        {
            if(y > m2)
                return (int)a*y;
            else
                return (int)(a*m2);
        }




    }



}
