package processors;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;

public class SobelProcessor extends ImageProcessor
{


	public static BufferedImage Process(BufferedImage source_img, int threshold)
	{
		// Unfortunately we need 2 images because the source image can't be changed
		BufferedImage current_img = ImageProcessor.Process(source_img);
		BufferedImage output_img = ImageProcessor.Process(source_img);
		
		int width = current_img.getWidth();
		int height = current_img.getHeight();
		
		int[]  pixeldata = ((DataBufferInt)current_img.getRaster().getDataBuffer()).getData();
		int[]  outpixeldata = ((DataBufferInt)output_img.getRaster().getDataBuffer()).getData();
		
		// Clamp threshold to 0-255
		threshold &=0xFF;
		
		// LOOP through all pixels x and y
		for (int y = 1; y< height-1; y++ )
		{
			for (int x = 1; x< width-1; x++ )
			{
				
				int Gx = -(pixeldata[(x-1) + (y-1)*width] & 0xFF) - 2*(pixeldata[(x) + (y-1)*width] & 0xFF) -(pixeldata[(x+1) + (y-1)*width] & 0xFF) +
						(pixeldata[(x-1) + (y+1)*width] & 0xFF) + 2*(pixeldata[(x) + (y+1)*width] & 0xFF) +(pixeldata[(x+1) + (y+1)*width] & 0xFF);
				
				int Gy = -(pixeldata[(x-1) + (y-1)*width] & 0xFF) - 2*(pixeldata[(x-1) + (y)*width] & 0xFF) -(pixeldata[(x-1) + (y+1)*width] & 0xFF) +
						(pixeldata[(x+1) + (y-1)*width] & 0xFF) + 2*(pixeldata[(x+1) + (y)*width] & 0xFF) +(pixeldata[(x+1) + (y+1)*width] & 0xFF);
				
				int Gm = ((int) Math.sqrt(Gx*Gx + Gy*Gy)) & 0xFF;
				
				if (Gm < threshold) Gm = 0;
				
				outpixeldata[x + y*width] = 255 << 24 | Gm << 16 | Gm << 8 | Gm;
				
			}
		}
		
		
		return output_img;
	}

}
