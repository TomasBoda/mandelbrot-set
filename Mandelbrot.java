// Mandelbrot set with zooming and using multi-processing
// by Tomáš Boďa

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Mandelbrot extends JPanel implements MouseListener {
	private static final long serialVersionUID = 1L;
	
	public static final int WIDTH = 1920;
	public static final int HEIGHT = 1080;
	
	public static int MAX_ITER = 50;
	
	public static double SCALE = HEIGHT / 4;
	public static double xMin = -WIDTH / SCALE / 2;
	public static double yMin = -2;
	
	private BufferedImage image;
	public static int[] COLORS;
	
	private long elapsedTime = 0;
	
	private int cpuCount = Runtime.getRuntime().availableProcessors();
	private enum PROCESSING { SINGLE_CORE, MULTI_CORE };
	private PROCESSING processing = PROCESSING.MULTI_CORE;
	
	public Mandelbrot() {
		// enhance graphics
		System.setProperty("sun.java2d.opengl","True");
		
		this.generateColors();
		this.render();
		this.createWindow();
	}
	
	private void createWindow() {
		// initialize window
		JFrame frame = new JFrame("Mandelbrot");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		this.addMouseListener(this);
		frame.add(this);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	private void render() {
		this.repaint();
		
		this.image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		
		// calculate the width of rendered image of one process based on CPU count
		int width = WIDTH / this.cpuCount;
		
		long startTime = System.nanoTime();
		
		// calculate the Mandelbrot set using single-core processing
		if (this.processing == PROCESSING.SINGLE_CORE) {
			Process p = new Process(0, 0, WIDTH, HEIGHT, image);
			p.start();
			
			try {
				p.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		// calculate the Mandelbrot set using multi-core processing
		} else if (this.processing == PROCESSING.MULTI_CORE) {
			Process[] processes = new Process[this.cpuCount];
			
			// create and start processes
			for (int i = 0; i < this.cpuCount; i++) {
				Process p = new Process(i * width, 0, width, HEIGHT, image);
				processes[i] = p;
				p.start();
			}
			
			// wait for processes to end
			for (Process p : processes) {
				try {
					p.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		long endTime = System.nanoTime();
		
		this.elapsedTime = (endTime - startTime) / 1000000;
		
		this.repaint();
	}
	
	private void generateColors() {
		COLORS = new int[MAX_ITER];
		
		// generate color palette based on max iterations
		for (int i = 0; i < MAX_ITER; i++) {
            COLORS[i] = Color.HSBtoRGB(i / 256f, 1, i / (i + 8f));
        }
	}
	
	private void zoom(int xMouse, int yMouse) {
		// calculate zoom using mouse coordinates
		double x = xMouse - (WIDTH / 3);
		double y = yMouse + (HEIGHT / 3);
		
		xMin = xMin + Math.floor(x) / SCALE;
		yMin = -Math.floor(y) / SCALE + HEIGHT / SCALE + yMin;
		SCALE *= 1.5;
		
		this.render();
	}
	
	private void increaseMaxIter(int iterations) {
		// increase or decrease max iterations and generate new colors
		MAX_ITER += iterations;
		this.generateColors();
		
		this.render();
	}
	
	private void reset() {
		// reset the the Mandelbrot set to default values
		
		SCALE = HEIGHT / 4;
		xMin = -WIDTH / SCALE / 2;
		yMin = -2;
		MAX_ITER = 50;
		
		this.generateColors();
		this.render();
	}
	
	// render the Mandelbrot set and controls onto the canvas
	public void paintComponent(Graphics g) {
		g.drawImage(image, 0, 0, WIDTH, HEIGHT, null);
		
		g.setColor(Color.BLACK);
		g.fillRect(20, 20, 240, 92);
		
		g.setColor(Color.WHITE);
		g.drawRect(20, 20, 240, 92);
		
		g.setColor(Color.WHITE);
		g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
		g.drawString("RESOLUTION:       " + WIDTH + "x" + HEIGHT, 30, 40);
		g.drawString("CPU_COUNT:        " + this.cpuCount, 30, 60);
		g.drawString("MAX_ITER:         " + MAX_ITER, 30, 80);
		g.drawString("RENDER TIME:      " + this.elapsedTime + "ms", 30, 100);
		
		g.setColor(this.processing == PROCESSING.SINGLE_CORE ? Color.WHITE : Color.BLACK);
		g.fillRect(this.singleCoreButton().x, this.singleCoreButton().y, this.singleCoreButton().width, this.singleCoreButton().height);
		g.setColor(Color.WHITE);
		g.drawRect(this.singleCoreButton().x, this.singleCoreButton().y, this.singleCoreButton().width, this.singleCoreButton().height);
		
		g.setColor(this.processing == PROCESSING.MULTI_CORE ? Color.WHITE : Color.BLACK);
		g.fillRect(this.multiCoreButton().x, this.multiCoreButton().y, this.multiCoreButton().width, this.multiCoreButton().height);
		g.setColor(Color.WHITE);
		g.drawRect(this.multiCoreButton().x, this.multiCoreButton().y, this.multiCoreButton().width, this.multiCoreButton().height);
		
		g.setColor(this.processing == PROCESSING.SINGLE_CORE ? Color.BLACK : Color.WHITE);
		g.drawString("SINGLE-CORE", 35, 145);
		
		g.setColor(this.processing == PROCESSING.MULTI_CORE ? Color.BLACK : Color.WHITE);
		g.drawString("MULTI-CORE", 165, 145);
		
		g.setColor(Color.BLACK);
		g.fillRect(this.decreaseMaxIterButton().x, this.decreaseMaxIterButton().y, this.decreaseMaxIterButton().width, this.decreaseMaxIterButton().height);
		g.setColor(Color.WHITE);
		g.drawRect(this.decreaseMaxIterButton().x, this.decreaseMaxIterButton().y, this.decreaseMaxIterButton().width, this.decreaseMaxIterButton().height);
		g.drawString("MAX_ITER - 10", 32, 195);
		
		g.setColor(Color.BLACK);
		g.fillRect(this.increaseMaxIterButton().x, this.increaseMaxIterButton().y, this.increaseMaxIterButton().width, this.increaseMaxIterButton().height);
		g.setColor(Color.WHITE);
		g.drawRect(this.increaseMaxIterButton().x, this.increaseMaxIterButton().y, this.increaseMaxIterButton().width, this.increaseMaxIterButton().height);
		g.drawString("MAX_ITER + 10", 157, 195);
		
		g.setColor(Color.BLACK);
		g.fillRect(this.resetButton().x, this.resetButton().y, this.resetButton().width, this.resetButton().height);
		g.setColor(Color.WHITE);
		g.drawRect(this.resetButton().x, this.resetButton().y, this.resetButton().width, this.resetButton().height);
		g.drawString("RESET", 120, 245);
	}
	
	private Rectangle singleCoreButton() {
		return new Rectangle(20, 120, 115, 40);
	}
	
	private Rectangle multiCoreButton() {
		return new Rectangle(145, 120, 115, 40);
	}
	
	private Rectangle decreaseMaxIterButton() {
		return new Rectangle(20, 170, 115, 40);
	}
	
	private Rectangle increaseMaxIterButton() {
		return new Rectangle(145, 170, 115, 40);
	}
	
	private Rectangle resetButton() {
		return new Rectangle(20, 220, 240, 40);
	}
	
	// handle mouse press
	public void mousePressed(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		Rectangle mouse = new Rectangle(x, y, 1, 1);
		
		if (mouse.intersects(this.singleCoreButton()) ) {
			this.processing = PROCESSING.SINGLE_CORE;
			this.render();
		} else if (mouse.intersects(this.multiCoreButton())) {
			this.processing = PROCESSING.MULTI_CORE;
			this.render();
		} else if (mouse.intersects(this.resetButton())) {
			this.reset();
		} else if (mouse.intersects(this.increaseMaxIterButton())) {
			this.increaseMaxIter(10);
		} else if (mouse.intersects(this.decreaseMaxIterButton())) {
			if (MAX_ITER > 0) {
				this.increaseMaxIter(-10);
			}
		} else {
			try {
				zoom(x, y);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	public void mouseClicked(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}
	
	public static void main(String[] args) {
        new Mandelbrot();
   }
}