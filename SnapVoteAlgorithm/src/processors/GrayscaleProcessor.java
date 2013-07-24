package processors;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class GrayscaleProcessor extends ImageProcessor
{

	public enum GrayscaleAlgorithm {
		AVERAGE,
		LIGHTNESS,
		LUMINOSITY
	}
	
	public static BufferedImage Process(BufferedImage source_img, GrayscaleAlgorithm algorithm)
	{
		BufferedImage current_img = ImageProcessor.Process(source_img);
		int[]  pixeldata = ((DataBufferInt)current_img.getRaster().getDataBuffer()).getData();
				
		switch (algorithm)
		{
		case AVERAGE:
			for (int i = 0;i<pixeldata.length;i++)
			{				
				int r = (pixeldata[i] >> 16) & 0xFF;
				int g = (pixeldata[i] >> 8) & 0xFF;
				int b = (pixeldata[i] >> 0) & 0xFF;
							
				int gray = ((r+g+b)/3) & 0xFF;
				
				pixeldata[i] = 255 << 24 | gray << 16 | gray << 8 | gray;
			}	
			break;
		case LUMINOSITY:
			for (int i = 0;i<pixeldata.length;i++)
			{
				int r = (pixeldata[i] >> 16) & 0xFF;
				int g = (pixeldata[i] >> 8) & 0xFF;
				int b = (pixeldata[i] >> 0) & 0xFF;
							
				int gray = (int) (0.21f*r + 0.71f*g + 0.07f*b);
				
				pixeldata[i] = 255 << 24 | gray << 16 | gray << 8 | gray;	
			}	
		case LIGHTNESS:
			for (int i = 0;i<pixeldata.length;i++)
			{
				int r = (pixeldata[i] >> 16) & 0xFF;
				int g = (pixeldata[i] >> 8) & 0xFF;
				int b = (pixeldata[i] >> 0) & 0xFF;
				
				int gray = (int) (Math.max(r, Math.max(g,b))/2);
				pixeldata[i] = 255 << 24 | gray << 16 | gray << 8 | gray;			
			}	
		}
		
		
		return current_img;
	}

}
