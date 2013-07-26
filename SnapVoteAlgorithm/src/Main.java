import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.imageio.ImageIO;



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
	    
	    System.out.printf("Loaded Image: '%s' %dx%d\n", configFile.getProperty("inputfile"), inputimg.getWidth(), inputimg.getHeight());

		FastImage inputdata = new FastImage(inputimg);
		FastImage gaussiandata = new FastImage(inputdata.width, inputdata.height, null);
		
		// GO GREYSCALE
		for(int y=0;y<inputdata.height;y++)
		{
			for(int x=0;x<inputdata.width;x++)
			{
				int c = inputdata.getSafePixel(x, y);
				int r = c >> 16 & 0xFF;
				int g = c >> 8 & 0xFF;
				int b = c & 0xFF;
				int grey = (int) ((r+g+b)/3.0f);
				inputdata.setPixel(x, y, 255 << 24 | grey << 16 | grey << 8 | grey);				
			}
		}
		
		
		// APPLY GAUSSIAN BLUR
		int gaussiansize = 3;
		double [][] gaussian = Gaussian.Make(gaussiansize);
		
		for(int y=0;y<inputdata.height;y++)
		{
			for(int x=0;x<inputdata.width;x++)
			{
				double tot = 0;
				for(int dy = -gaussiansize;dy<gaussiansize;dy++)
				{
					for(int dx = -gaussiansize;dx<gaussiansize;dx++)
					{
						int c = inputdata.getSafePixel(x+dx, y+dy);						
						tot += gaussian[gaussiansize+dy][gaussiansize+dx] * (c & 0xFF);
					}
				}
				
				int g = (int) tot;
				gaussiandata.setPixel(x, y, 255 << 24 | g << 16 | g << 8 | g);				
			}
		}

		System.out.println("Gaussian Blur complete ");
		
		FastImage edgegradientdata = new FastImage(gaussiandata.width, gaussiandata.height, null);
		
		// APPLY SOBEL
		for (int y = 1; y < gaussiandata.height-1; y++)
		{
			for (int x = 1; x < gaussiandata.width-1; x++)
			{
				
				int Gx = -(gaussiandata.getPixel(x-1, y-1) & 0xFF) - 2*(gaussiandata.getPixel(x, y-1) & 0xFF) -(gaussiandata.getPixel(x+1, y-1) & 0xFF) +
						(gaussiandata.getPixel(x-1, y+1) & 0xFF) + 2*(gaussiandata.getPixel(x, y+1) & 0xFF) +(gaussiandata.getPixel(x+1, y+1) & 0xFF);
				
				int Gy = -(gaussiandata.getPixel(x-1, y-1) & 0xFF) - 2*(gaussiandata.getPixel(x-1, y) & 0xFF) -(gaussiandata.getPixel(x-1, y+1) & 0xFF) +
						(gaussiandata.getPixel(x+1, y-1) & 0xFF) + 2*(gaussiandata.getPixel(x+1, y) & 0xFF) +(gaussiandata.getPixel(x+1, y+1) & 0xFF);
				
				int Gm = ((int) Math.sqrt(Gx*Gx + Gy*Gy)) & 0xFF;	
				
				//minimum threshold
				if(Gm < 100) Gm = 0;				
				
				double angle = Math.atan2(Gy,Gx);					
				int degrees = (int) ((angle * 180) / Math.PI);					
				System.out.println(degrees);
				
				
				edgegradientdata.setPixel(x,y, 255 << 24 | Gm << 16 | Gm << 8 |  Gm);				
			}
		}
		
		System.out.println("Sobel complete");				

		
		
		new BufferedImageVis.BufferedImageView(edgegradientdata.toImage());
		

	}

}
