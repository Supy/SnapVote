package processors;

import java.awt.image.BufferedImage;

public class ImageProcessor
{
	public static BufferedImage Process(BufferedImage bi)
	{
	    BufferedImage convertedImg = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB);
	    convertedImg.getGraphics().drawImage(bi, 0, 0, null);
		return convertedImg;
	}
}
