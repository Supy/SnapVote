package processors;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class CannyEdgeProcessor extends ImageProcessor
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
		
		
		
		
		return output_img;
	}
}
