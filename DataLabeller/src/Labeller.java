import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;

public class Labeller
{
    final static String DIRECTORY = "C:/Projects/SubimageMaker/output";

    public static void saveTrainingData(int[][] data)
    {
        BufferedWriter writer = null;
        try
        {
            File f = new File("trainingdata.json");
            System.out.println(f.getCanonicalPath());
            writer = new BufferedWriter(new FileWriter(f));
            writer.write("[");

            for (int j = 0; j < data.length; j++)
            {
                writer.write("[");
                for (int k = 0; k < data[j].length - 1; k++)
                {
                    writer.write(data[j][k] + ",");
                }
                writer.write(data[j][data[j].length - 1] + "],\n");
            }
            writer.write("]");
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                writer.close();
            } catch (Exception e)
            {
            }
        }
    }

    public static void main(String[] args)
    {
        // List<String> results = new ArrayList<String>();
        File[] files = new File(DIRECTORY).listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename)
            {
                return filename.endsWith(".png");
            }
        });
        System.out.println("Images found: " + files.length);
        new ImageBrowser(files);
    }

}
