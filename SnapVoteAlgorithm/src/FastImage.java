
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

public class FastImage
{
	public static final String[] ImageTypeName = new String[] {
		 "TYPE_CUSTOM",
		 "TYPE_INT_RGB",
		 "TYPE_INT_ARGB",
		 "TYPE_INT_ARGB_PRE", 
		 "TYPE_INT_BGR",
		 "TYPE_3BYTE_BGR",
		 "TYPE_4BYTE_ABGR",
		 "TYPE_4BYTE_ABGR_PRE",
		 "TYPE_USHORT_565_RGB", 
		 "TYPE_USHORT_555_RGB", 
		 "TYPE_BYTE_GRAY",
		 "TYPE_USHORT_GRAY",
		 "TYPE_BYTE_BINARY", 
		 "TYPE_BYTE_INDEXED"
	};
	
	private int[][] data;
	public int width;
	public int height;
	
	public FastImage(BufferedImage bi)
	{
		width = bi.getWidth();
		height = bi.getHeight();
		
		data = new int[height][width];
		
		// Load data depending on bufferedimagetype
		switch (bi.getType())
		{
		case BufferedImage.TYPE_3BYTE_BGR:
			byte[] bytedata = ((DataBufferByte)bi.getRaster().getDataBuffer()).getData();
			for(int y=0;y<height;y++)
			{
				for(int x=0;x<width;x++)
				{
					int pi = x*3 + y*3*width;
					data[y][x] = 255 << 24 | (bytedata[pi+2] & 0xFF) << 16 | (bytedata[pi+1] & 0xFF) << 8 | (bytedata[pi] & 0xFF);
				}
			}
			break;
		case BufferedImage.TYPE_INT_ARGB:
			for(int y = 0;y<height;y++)
			{
				bi.getRaster().getPixels(0, y, width, 1, data[y]);
			}
			break;
		default:
			System.out.println("Cannot handle image type: " + ImageTypeName[ bi.getType() ]);
		}
	}
	
	public FastImage(int _width, int _height, Integer fillvalue)
	{
		width = _width;
		height = _height;
		data = new int[height][width];
		
		if (fillvalue != null)
		{
			int v = fillvalue.intValue();
			
			for(int y = 0;y<height;y++)
			{
				Arrays.fill(data[y], v);
			}
		}		
	}
	
	public int getPixel(int x, int y)
	{
		return data[y][x];	
	}
	
	public int getSafePixel(int x, int y)
	{
		if(x < 0)
			x = -x;
		else if (x >=width)
			x = width + width - x -1;
		if(y < 0)
			y = -y;
		else if (y >=height)
			y = height + height - y -1;
		return data[y][x];
	}
	
	public BufferedImage toImage()
	{
		BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		int[] pixels = ((DataBufferInt)out.getRaster().getDataBuffer()).getData();
		
		for(int y=0;y<height;y++) System.arraycopy(data[y], 0, pixels, y*width, width);
		
		return out;		
	}

	public void setPixel(int x, int y, int i)
	{
		data[y][x] = i;
	}
	
	
	
}
