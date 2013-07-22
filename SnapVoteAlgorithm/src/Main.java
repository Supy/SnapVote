import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.imageio.ImageIO;


/*
 * This main class should only be used for ImageIO and for testing algorithms.
 * Needs to be rather modular so we can switch out algorithm pieces and needs 
 * to be able to time and collect performance data. (mem usage)
 * Should also provide parameter initialisation.
 */

public class Main
{

	public static void main(String[] args)
	{
	    Properties configFile = new Properties();
	    BufferedImage inputimg = null;
	    try
		{
			configFile.load(new InputStreamReader(new FileInputStream(new File("config.properties"))));
			inputimg = ImageIO.read(new File(configFile.getProperty("inputfile")));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	    
	    System.out.printf("Loaded Image: '%s' %dx%d", configFile.getProperty("inputfile"), inputimg.getWidth(), inputimg.getHeight());
	    
	    
		
		
		
	}

}
