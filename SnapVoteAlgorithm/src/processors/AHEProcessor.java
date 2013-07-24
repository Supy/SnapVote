package processors;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class AHEProcessor extends ImageProcessor
{
	public static BufferedImage Process(BufferedImage img_before)
	{
		BufferedImage img_after = ImageProcessor.Process(img_before);
		
		byte[]  pixeldata = ((DataBufferByte)img_after.getRaster().getDataBuffer()).getData();
		
		int width = img_after.getWidth();
		int height = img_after.getHeight();
		
		int binsize = 30;
		
		for(int y = 0; y < height; y++)
		{
			int ystart = Math.max(0, y-binsize);
			int yend = Math.min(height, y+binsize);
			
			for(int x = 0; x < width; x++)
			{
				int xstart = Math.max(0, x-binsize);
				int xend = Math.min(width, x+binsize);
				
				int pixelindex = x*3 + y*3*width;				
				
				int centerpixel = pixeldata[pixelindex] & 0xFF;
				
				int [] buckets = new int[256];
				int mini = 255;
				int maxi = 0;
				
				for ( int yy = ystart; yy<yend; yy++)
				{
					for ( int xx = xstart; xx<xend; xx++)
					{
						int innerpixelindex = xx*3 + yy*3*width;
						int value = pixeldata[innerpixelindex] & 0xFF;
						
						buckets[value] += 1;
						if (value > maxi) maxi = value;
						if (value < mini) mini = value;
					}
				}
				
				int lt = 0;
				int [] cdf = new int[256];
				for (int i=0;i<256;i++)
				{
					cdf[i] = lt + buckets[i];
					lt = cdf[i];
				}
				
				float f1 = (cdf[centerpixel] - cdf[mini]);
				float f2 = (((yend-ystart)*(xend-xstart)) - cdf[mini]);
				float f3 = f1/f2;
				
				float p = f3  * 255;
				byte v = (byte) p;
				
				pixeldata[pixelindex]  = v;
				pixeldata[pixelindex+1]  = v;
				pixeldata[pixelindex+2]  = v;
				
				
			}
		}

		
		
		return img_after;
	}
	
	
	
}
