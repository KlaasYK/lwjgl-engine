package nl.klaasyk.apps.util;

import static org.lwjgl.opengl.GL11.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL12;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load OpenGL Textures.
 * 
 * @author KlaasYK
 *
 */
public class TextureLoader {

	private static final Logger l = LoggerFactory.getLogger(TextureLoader.class);
	private static final int BYTES_PER_PIXEL = 4; // RGBA

	/**
	 * Loads a texture into graphical memory from an image.
	 * 
	 * @param filename
	 *            of the image used for the texture.
	 * @return textureID for use with OpenGL texture functions.
	 * @throws IOException
	 *             on failure to read the image.
	 */
	public static int loadTextureFromImage(String filename) throws IOException {
		l.trace("Loading texture: {}", filename);
		BufferedImage img = ImageIO.read(new File(filename));
		// TODO: check if power of 2 and image is square
		int[] pixels = new int[img.getWidth() * img.getHeight()];
		img.getRGB(0, 0, img.getWidth(), img.getHeight(), pixels, 0, img.getWidth());
		ByteBuffer buf = BufferUtils.createByteBuffer(img.getWidth() * img.getHeight() * BYTES_PER_PIXEL);
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				int pixel = pixels[y * img.getWidth() + x];
				buf.put((byte) ((pixel >> 16) & 0xFF)); // Red component
				buf.put((byte) ((pixel >> 8) & 0xFF)); // Green component
				buf.put((byte) (pixel & 0xFF)); // Blue component
				buf.put((byte) ((pixel >> 24) & 0xFF)); // Alpha component.
			}
		}
		buf.flip();
		int textureID = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, textureID);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, img.getWidth(), img.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
		glBindTexture(GL_TEXTURE_2D, 0);
		return textureID;
	}

	/**
	 * Disposes all resources taken up by the given texture.
	 * 
	 * @param texID
	 *            OpenGL texture id.
	 */
	public static void dispose(int texID) {
		glBindTexture(GL_TEXTURE_2D, 0);
		glDeleteTextures(texID);
	}

}
