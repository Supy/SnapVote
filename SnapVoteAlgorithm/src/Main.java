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
		int gaussiansize = 2;
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
				
				int Gm = ((int) Math.sqrt(Gx*Gx + Gy*Gy));	

				double angle = Math.atan2(Gy,Gx);	
				
				float degrees = Math.abs((float) (((angle * 180) / Math.PI) + 22.5f));
				int acat = 0;
				if (degrees < 180) acat = 135;
				if (degrees < 135) acat = 90;
				if (degrees < 90) acat = 45;
				if (degrees < 45) acat = 0;

				if (Gm > 255) Gm = 255;
				edgegradientdata.setPixel(x,y, (acat << 24) | Gm);			
			}
		}
		
		final int HIGH = 150;
		final int LOW = 70;
		
		for(int y = 1; y < edgegradientdata.height-1; y++){
			for(int x = 1; x < edgegradientdata.width-1; x++){
				int pixel = edgegradientdata.getPixel(x, y);
				int gradient = pixel & 0xFF;
				int peak = 0;

				int angle = (pixel >> 24) & 0xFF;
				int x1 = x;
				int x2 = x;
				int y1 = y;
				int y2 = y;
				
				if(angle == 90){
					x1--;
					x2++;						
				}else if(angle == 45){
					x1--;
					x2++;
					y1--;
					y2++;						
				}else if(angle == 0){
					y1--;
					y2++;
				}else{
					x1--;
					x2++;
					y1++;
					y2--;
				}
				
				int px1 = edgegradientdata.getPixel(x1, y1);
				int px2 = edgegradientdata.getPixel(x2, y2);
				int finalpeak = 0;
				
				if(gradient > (px1 & 0xFF) && gradient > (px2 & 0xFF)){
					peak = gradient;
					// High threshold
					if(peak > HIGH){
						finalpeak = 255;
						peak = 0;
					}else if(peak < LOW){
						peak = 0;
					}
				}
				
				edgegradientdata.setPixel(x, y, (255 << 24) | (peak << 8) | finalpeak);
			}
				
		}
		BufferedImageVis.BufferedImageView img = new BufferedImageVis.BufferedImageView(edgegradientdata.toImage());
		// Low threshold
		boolean more;
		do{
			more = false;
			for(int y = 1; y < edgegradientdata.height-1; y++){
				for(int x = 1; x < edgegradientdata.width-1; x++){
					
					int pixel = edgegradientdata.getPixel(x, y);
					
					if((pixel & 0xFF) == 255){
						for(int p = -1; p < 1; p++){
							for(int q = -1; q < 1; q++){
								int neighbour = edgegradientdata.getPixel(x+p, y+q);
								
								if(((neighbour >> 8) & 0xFF) > 0){
									neighbour = (255 << 24) | 255;
									edgegradientdata.setPixel(x+p, y+q, neighbour);
									more = true;
								}
							}
						}
					}
				}
			}
		}while(more);
		
				
		for(int y = 1; y < edgegradientdata.height-1; y++){
			for(int x = 1; x < edgegradientdata.width-1; x++){
				
				int pixel = edgegradientdata.getPixel(x, y);
				
				if((pixel & 0xFF) != 0){
					for(int p = -1; p < 1; p++)
						for(int q = -1; q < 1; q++)	
							edgegradientdata.setPixel(x+p, y+q, (255 << 24) | 255);
				}else{
					edgegradientdata.setPixel(x, y, (255 << 24) | 0);		
				}
			}
		}
		
		System.out.println("Canny complete");
		
		img.setImage(edgegradientdata.toImage());	
		
	}

}
