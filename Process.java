import java.awt.image.BufferedImage;

public class Process extends Thread implements Runnable {
	
	private int xStart;
	private int yStart;
	private int width;
	private int height;
	
	private BufferedImage image;

	public Process(int xStart, int yStart, int width, int height, BufferedImage image) {
		this.xStart = xStart;
		this.yStart = yStart;
		this.width = width;
		this.height = height;
		this.image = image;
	}
	
	public void run() {
		calculate();
	}
	
	private void calculate() {
		// loop through every pixel of the screen
        for (int x = this.xStart; x < this.xStart + this.width; x++) {
            for (int y = this.yStart; y < this.yStart + this.height; y++) {
            	// calculate the Mandelbrot set
            	double cx = Mandelbrot.xMin + (x / Mandelbrot.SCALE);
            	double cy = Mandelbrot.yMin + (y / Mandelbrot.SCALE);
            	
                double zx = 0;
                double zy = 0;
                
                int iteration = 0;
                
                while (zx * zx + zy * zy < 4 && iteration < Mandelbrot.MAX_ITER) {
                    double xt = zx * zy;
                    zx = zx * zx - zy * zy + cx;
                    zy = 2 * xt + cy;
                    iteration++;
                }
                
                // get pixel coordinates and determine its color based on the number of iterations reached
                int xPixel = x;
                int yPixel = Mandelbrot.HEIGHT - y - 1;
                int pixelColor = iteration < Mandelbrot.MAX_ITER ? Mandelbrot.COLORS[iteration] : 0;
                
                // draw the pixel onto the image
                this.image.setRGB(xPixel, yPixel, pixelColor);
            }
        }
	}
}
