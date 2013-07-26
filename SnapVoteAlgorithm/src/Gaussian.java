
public class Gaussian
{
	public static double[][] Make(int kernel_radius)
	{
		float sigma = kernel_radius / 2.0f;
		
		int size = kernel_radius*2 +1;

		double[][] out = new double[size][size];
		
		double [] hkernel = new double[size];
		for (int x =0;x<size;x+=1) hkernel[x] = funGaussian(x, kernel_radius, sigma);
		
		double kernelsum = 0;
		for (int y = 0;y< size;y++)
		{
			for (int x = 0;x< size;x++)
			{
				out[y][x] = hkernel[y] * hkernel[x];
				kernelsum += out[y][x];
			}
		}
		
		for (int y = 0;y< size;y++)
		{
			for (int x = 0;x< size;x++)
			{
				out[y][x] /= kernelsum;
			}
		}	
		return out;
	}
	
	private static double funGaussian(int x, int mu, float sigma)
	{
		return Math.exp( -(Math.pow((x-mu)/(sigma),2))/2.0);
	}
}
