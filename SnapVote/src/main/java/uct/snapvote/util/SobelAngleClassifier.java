package uct.snapvote.util;

/**
 * Created by Ben on 2013/09/02.
 */
public class SobelAngleClassifier {

    // TODO: Requires some experimental range optimisation
    
    static final int MAG = 400;
    static final int MAG2 = MAG*2;
    static byte[] angles;
    static byte[] magnitudes;

    public static void prepare()
    {
        angles = new byte[MAG2 * MAG2];
        magnitudes = new byte[MAG2 * MAG2];

        for(int i=0;i<MAG2;i++)
        {
            for(int j=0;j<MAG2;j++)
            {
                int Gx = j-MAG;
                int Gy = i-MAG;

                float angle = (float)(Math.atan2(Gy,Gx) * 180 / Math.PI);

                angle = (angle + 180) % 180;

                int acat = 0;
                if (angle < 45) acat = 0;
                else if (angle < 90) acat = 45;
                else if (angle < 135) acat = 90;
                else if (angle < 180) acat = 135;

                angles[i*MAG2 + j] = (byte)acat;

                int Gm = Gx * Gx + Gy * Gy;
                if (Gm > 65025)
                    Gm = 255;
                else
                    Gm = FastMath.fastnorm(Gx, Gy);

                magnitudes[i*MAG2 + j] = (byte)Gm;
            }
        }




    }

    public static byte classify(int gx, int gy)
    {
        int i = gy+MAG;
        int j = gx+MAG;

        return angles[i*MAG2 + j];
    }

    public static byte magnify(int gx, int gy)
    {
        int i = gy+MAG;
        int j = gx+MAG;

        return magnitudes[i*MAG2 + j];
    }


}
