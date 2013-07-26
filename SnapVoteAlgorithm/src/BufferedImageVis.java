import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class BufferedImageVis
{
	
	
	public static class BufferedImageView implements ActionListener
	{
		private ImagePanel imgpanl;
		public BufferedImageView(BufferedImage input)
		{
			JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            imgpanl = new ImagePanel(input);
            frame.add(imgpanl, BorderLayout.CENTER);
            JButton btn = new JButton();
            btn.setText("Save");
            btn.addActionListener(this);
            
            frame.add(btn, BorderLayout.NORTH);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
            
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			try
			{
				ImageIO.write(imgpanl.img, "png", new File(String.format("output-%d.png", System.currentTimeMillis())));
				JOptionPane.showMessageDialog(null, String.format("Saved to: output-%d.jpg", System.currentTimeMillis()), "Saved", JOptionPane.INFORMATION_MESSAGE);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			
		}
		
	}
	
	public static class ImagePanel extends JPanel implements MouseMotionListener
	{
		private static final long serialVersionUID = 4007214516797055040L;
		BufferedImage img;
		int width;
		int height;
		int mx, my;
		float scalefactor = 1;
		
		public ImagePanel(BufferedImage bi)
		{
			 ColorModel cm = bi.getColorModel();
			 boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
			 WritableRaster raster = bi.copyData(null);
			 img = new BufferedImage(cm, raster, isAlphaPremultiplied, null);	
			 width = Math.min(img.getWidth(), 1600);
			 height = (int) (((float)img.getHeight() / (float)img.getWidth()) * width);
			 
			 scalefactor = (float)width / img.getWidth();
			 this.addMouseMotionListener(this);
			 mx = 0;
			 my = 0;
		}
		
		@Override
        public Dimension getPreferredSize() {
            return new Dimension(width, height);
        }
		
		@Override		
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);		
			
			
			g.drawImage(img, 0, 0, width, height, this);		
			
			
			g.drawImage(img, 5, 5, 205, 205, mx-100, my-100, mx+100, my+100, this);

			g.setColor(Color.white);
			g.drawRect(5, 5, 200, 200);
			
			
		}		

		@Override
		public void mouseDragged(MouseEvent e)
		{
			// TODO Auto-generated method stub
			
			
		}

		@Override
		public void mouseMoved(MouseEvent e)
		{
			// TODO Auto-generated method stub
			mx = (int) (e.getX()/scalefactor);
			my = (int) (e.getY()/scalefactor);
			this.repaint();
			
		}
	}
	
	
}
