import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ImageBrowser extends JFrame
{

    private static final long serialVersionUID = 8335126557118273030L;
    static final int samplesPerDimension = 20;
    File[] files;
    BufferedImage currentImage;
    int currentIndex;
    ImagePanel imagePanel;
    int[][] trainingData;
    boolean done = false;

    public ImageBrowser(File[] files)
    {
        this.files = files;
        this.currentIndex = -1;
        this.trainingData = new int[files.length][(samplesPerDimension * samplesPerDimension) + 5];

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);
        getContentPane().setBackground(Color.DARK_GRAY);
        imagePanel = new ImagePanel();
        add(imagePanel);
        addKeyListener(new KeyListener(this));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        if (files.length > 0)
            readNextImage();
    }

    public void labelImage(int colour)
    {
        if (colour == -1)
        {
            if (currentIndex != 0)
            {
                currentIndex--;
                for (int i = 0; i < 5; i++)
                    trainingData[currentIndex][(samplesPerDimension * samplesPerDimension) + i] = 0;
                currentIndex--;
                readNextImage();
            }
        } else
        {
            if (currentIndex < files.length - 1)
            {
                trainingData[currentIndex][(samplesPerDimension * samplesPerDimension) + colour] = 1;
                readNextImage();
            } else if (!done)
            {
                done = true;
                imagePanel.setImage(null);
                trainingData[currentIndex][(samplesPerDimension * samplesPerDimension) + colour] = 1;
                Labeller.saveTrainingData(trainingData);
            }
        }
    }

    private void readNextImage()
    {
        try
        {
            currentIndex++;
            currentImage = ImageIO.read(files[currentIndex]);
            // Set image panel to draw image
            imagePanel.setImage(currentImage);
            int panelX = (getWidth() - imagePanel.getWidth() - getInsets().left - getInsets().right) / 2;
            int panelY = ((getHeight() - imagePanel.getHeight() - getInsets().top - getInsets().bottom) / 2);
            imagePanel.setLocation(panelX, panelY);
            // Gather samples
            int sampleIndex = 0;
            float gradientX = currentImage.getWidth() * 1.0f / samplesPerDimension;
            float gradientY = currentImage.getHeight() * 1.0f / samplesPerDimension;
            System.out.println("Image dimensions: " + currentImage.getWidth() + "x" + currentImage.getHeight() + " Gradients: " + gradientX
                    + ", " + gradientY);
            float accY = gradientY / 2;
            for (int y = 0; y < currentImage.getHeight(); y++)
            {
                accY++;
                if (accY >= gradientY)
                {
                    float accX = gradientX / 2;
                    for (int x = 0; x < currentImage.getWidth(); x++)
                    {
                        accX++;
                        if (accX >= gradientX)
                        {
                            trainingData[currentIndex][sampleIndex] = currentImage.getRGB(x, y);
                            sampleIndex++;
                            accX -= gradientX;
                        }
                    }
                    accY -= gradientY;
                }
            }
        } catch (IOException e)
        {
            readNextImage();
        }
    }

    @Override
    public Dimension getPreferredSize()
    {
        return new Dimension(1000, 800);
    }

    private class ImagePanel extends JPanel
    {
        private static final long serialVersionUID = -5050775582262815497L;
        BufferedImage image;

        public void setImage(BufferedImage image)
        {
            this.image = image;
            if (image != null)
                this.setSize(image.getWidth(), image.getHeight());
            this.repaint();
        }

        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            if (image != null)
            {
                g.drawImage(image, 0, 0, this);
            }
        }
    }

    private class KeyListener extends KeyAdapter
    {
        ImageBrowser browser;

        public KeyListener(ImageBrowser browser)
        {
            this.browser = browser;
        }

        public void keyPressed(KeyEvent event)
        {
            switch (event.getKeyCode())
            {
                case KeyEvent.VK_R:
                    browser.labelImage(0);
                    break;
                case KeyEvent.VK_G:
                    browser.labelImage(1);
                    break;
                case KeyEvent.VK_B:
                    browser.labelImage(2);
                    break;
                case KeyEvent.VK_K:
                    browser.labelImage(3);
                    break;
                case KeyEvent.VK_SPACE:
                    browser.labelImage(4);
                    break;
                case KeyEvent.VK_LEFT:
                    browser.labelImage(-1);
                    break;
            }
        }
    }
}