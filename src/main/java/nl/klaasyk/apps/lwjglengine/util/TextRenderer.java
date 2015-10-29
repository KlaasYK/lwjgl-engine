package nl.klaasyk.apps.lwjglengine.util;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used for rendering text.
 * 
 * @author KlaasYK
 *
 */
public class TextRenderer {

	private static final Logger l = LoggerFactory.getLogger(TextRenderer.class);
	@SuppressWarnings("serial")
	private static final Map<Integer, String> CHARS = new HashMap<Integer, String>() {
		{
			put(0, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
			put(1, "abcdefghijklmnopqrstuvwxyz");
			put(2, "0123456789");
			put(3, "$+-*/=%\"'#@&_(),.;:?!\\|<>[]§`^~");
		}
	};

	private float DEFAULT_FONT_SIZE;

	private Font f;
	private FontMetrics fMet;

	private int vaID;
	private int vboID;
	
	
	private int texID;

	/**
	 * Creates a TextRenderer for the given ttf file.
	 * 
	 * @param ttfFilename
	 *            .ttf file
	 * @param fontsize
	 *            size of the font
	 */
	public TextRenderer(String ttfFilename, float fontsize) {
		DEFAULT_FONT_SIZE = fontsize;
		f = loadFont(ttfFilename);
		generateTexture();
	}

	private ByteBuffer generateByteBuffer(BufferedImage imageBuffer) {
		// Generate texture data
		int[] pixels = new int[imageBuffer.getWidth() * imageBuffer.getHeight()];
		imageBuffer.getRGB(0, 0, imageBuffer.getWidth(), imageBuffer.getHeight(), pixels, 0, imageBuffer.getWidth());
		ByteBuffer imageData = ByteBuffer.allocateDirect((imageBuffer.getWidth() * imageBuffer.getHeight() * 4));

		for (int y = 0; y < imageBuffer.getHeight(); y++) {
			for (int x = 0; x < imageBuffer.getWidth(); x++) {
				int pixel = pixels[y * imageBuffer.getWidth() + x];
				imageData.put((byte) ((pixel >> 16) & 0xFF)); // Red component
				imageData.put((byte) ((pixel >> 8) & 0xFF)); // Green component
				imageData.put((byte) (pixel & 0xFF)); // Blue component
				imageData.put((byte) ((pixel >> 24) & 0xFF)); // Alpha
																// component.
																// Only for RGBA
			}
		}
		imageData.flip();
		return imageData;
	}

	private void drawFontChars(BufferedImage imageBuffer) {
		// Draw the characters on our image
		Graphics2D imageGraphics = (Graphics2D) imageBuffer.getGraphics();
		imageGraphics.setFont(f);
		imageGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		imageGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// draw every CHAR by line...
		// FIXME, make color selectable
		imageGraphics.setColor(Color.WHITE);
		CHARS.keySet().stream()
				.forEach(i -> imageGraphics.drawString(CHARS.get(i), 0, fMet.getMaxAscent() + (getCharHeight() * i)));
	}

	private float getCharX(char c) {
		String originStr = CHARS.values().stream().filter(e -> e.contains("" + c)).findFirst().orElse("" + c);
		return (float) fMet.getStringBounds(originStr.substring(0, originStr.indexOf(c)), null).getWidth();
	}

	private float getCharY(char c) {
		float lineId = (float) CHARS.keySet().stream().filter(i -> CHARS.get(i).contains("" + c)).findFirst().orElse(0);
		return this.getCharHeight() * lineId;
	}

	private float getCharWidth(char c) {
		return fMet.charWidth(c);
	}

	private void generateTexture() {
		texID = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, texID);

		BufferedImage im = generateBufferedImage();
		drawFontChars(im);
		
		try {
			ImageIO.write(im, "png", new File("test.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ByteBuffer bb = generateByteBuffer(im);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, (int) getFontImageWidth(), (int) getFontImageHeight(), 0, GL_RGBA,
				GL_UNSIGNED_BYTE, bb);


		

		glBindTexture(GL_TEXTURE_2D, 0);
		
		vaID = glGenVertexArrays();
		glBindVertexArray(vaID);
		
		int l = 1;
		for (int i = 0; i < CHARS.size(); i++) {
			l += CHARS.get(i).length();
		}
		System.out.println(l);
		FloatBuffer fb = BufferUtils.createFloatBuffer(l*16);
		CHARS.keySet().stream().forEach(i -> {
			char[] ca = CHARS.get(i).toCharArray();
			for (char c : ca) {
				float x = getCharX(c);
				float y = getCharY(c);
				float w = getCharWidth(c);
				float h = getCharHeight();			
				fb.put(new float[] {0.0f,0.0f, 0.0f,0.0f,
						1.0f,0.0f, 1.0f,0.0f,
						1.0f,-1.0f, 1.0f,1.0f,
						0.0f,-1.0f, 0.0f,1.0f
				});
				
				/*fb.put(new float[] {0.0f,0.0f, 1f/x,1f/y,
						1.0f,0.0f, 1f/(x+w),1f/(y),
						1.0f,-1.0f, 1f/(x+w),1f/(y + h),
						0.0f,-1.0f, 1f/x,1f/(y + h)
				});*/
			}
		});
		fb.flip();
		vboID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferData(GL_ARRAY_BUFFER, fb, GL_STATIC_DRAW);
		glVertexAttribPointer(0, 2, GL_FLOAT, false, 4*4, 0);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 4*4, 2*4);
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}

	private float getFontImageWidth() {
		return (float) CHARS.values().stream().mapToDouble(e -> fMet.getStringBounds(e, null).getWidth()).max()
				.getAsDouble();
	}

	private float getFontImageHeight() {
		return (float) CHARS.keySet().size() * (this.getCharHeight());
	}

	private float getCharHeight() {
		return (float) (fMet.getMaxAscent() + fMet.getMaxDescent());
	}

	private BufferedImage generateBufferedImage() {
		// Configure
		GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration();
		Graphics2D graphics = gc.createCompatibleImage(1, 1, Transparency.TRANSLUCENT).createGraphics();
		graphics.setFont(f);
		fMet = graphics.getFontMetrics();
		return graphics.getDeviceConfiguration().createCompatibleImage((int) getFontImageWidth(),
				(int) getFontImageHeight(), Transparency.TRANSLUCENT);
	}

	/**
	 * Renders String <code>s</code> into an orthographic plane at coordinates
	 * <code>(x,y)</code>.
	 * 
	 * @param xPos
	 *            x coordinate
	 * @param yPos
	 *            y coordinate
	 * @param s
	 *            String to be rendered
	 */
	public void renderString(float xPos, float yPos, String s) {
		glBindVertexArray(vaID);;
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, texID);
		
		glDrawArrays(GL_QUADS, 0, 4);
		
		glBindTexture(GL_TEXTURE_2D, 0);
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glBindVertexArray(0);
	}

	// Loads the font from the file
	private Font loadFont(String ttfFilename) {
		Path file = FileSystems.getDefault().getPath(ttfFilename);
		l.trace("Loading Font: {}", file.toAbsolutePath().toString());
		try {
			return Font.createFont(Font.TRUETYPE_FONT, file.toFile()).deriveFont(DEFAULT_FONT_SIZE);
		} catch (FontFormatException e) {
			new RuntimeException("Invalid Font Format:");
		} catch (IOException e) {
			new RuntimeException("IO Exception:");
		}
		return null;
	}

	/**
	 * Clean up all resources hold by this TextRenderer.
	 */
	public void dispose() {
		glBindTexture(GL_TEXTURE_2D, 0);
		glDeleteTextures(texID);
		
		glBindVertexArray(0);
		glDeleteVertexArrays(vaID);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glDeleteBuffers(vboID);
	}

}
