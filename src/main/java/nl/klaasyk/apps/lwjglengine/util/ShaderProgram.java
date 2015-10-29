package nl.klaasyk.apps.lwjglengine.util;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class representing an OpenGL shader program.
 * 
 * @author KlaasYK
 *
 */
public class ShaderProgram {

	private static final Logger l = LoggerFactory.getLogger(ShaderProgram.class);

	private int programID;
	private int vsID;
	private int fsID;

	private boolean init;

	/**
	 * Create a new shader program with given source files.
	 * 
	 * @param vsFilename
	 *            vertex source file
	 * @param fsFilename
	 *            fragment source file
	 */
	public ShaderProgram(String vsFilename, String fsFilename) {
		l.trace("Setting up shader program");
		init = false;
		try {
			programID = glCreateProgram();
			vsID = glCreateShader(GL_VERTEX_SHADER);
			fsID = glCreateShader(GL_FRAGMENT_SHADER);

			// TODO: make it possible to load from inside jar
			String vertexShaderSource = FileReader.readFile(vsFilename);
			String fragmentShaderSource = FileReader.readFile(fsFilename);

			glShaderSource(vsID, vertexShaderSource);
			glShaderSource(fsID, fragmentShaderSource);

			glCompileShader(vsID);
			if (glGetShaderi(vsID, GL_COMPILE_STATUS) == GL_FALSE) {
				throw new RuntimeException("Error creating vertex shader\n"
						+ glGetShaderInfoLog(vsID, glGetShaderi(vsID, GL_INFO_LOG_LENGTH)));
			}
			glCompileShader(fsID);
			if (glGetShaderi(fsID, GL_COMPILE_STATUS) == GL_FALSE) {
				throw new RuntimeException("Error creating fragment shader\n"
						+ glGetShaderInfoLog(fsID, glGetShaderi(fsID, GL_INFO_LOG_LENGTH)));
			}

			glAttachShader(programID, vsID);
			glAttachShader(programID, fsID);

			glLinkProgram(programID);
			if (glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE) {
				throw new RuntimeException("Unable to link shader program:");
			}
			init = true;
		} catch (IOException e) {
			l.error("IOException: {}", e.getMessage());
		}
	}

	/**
	 * Use the shading program.
	 */
	public void use() {
		if (!init) {
			throw new RuntimeException("Shader is not initialized!");
		}
		glUseProgram(programID);
	}

	/**
	 * Reset the shading program.
	 */
	public void unbind() {
		if (!init) {
			throw new RuntimeException("Shader is not initialized!");
		}
		glUseProgram(0);
	}

	/**
	 * Clear all resources used by this program.
	 */
	public void dispose() {
		l.trace("Shader program disposed.");
		if (!init) {
			throw new RuntimeException("Shader is not initialized!");
		}
		glUseProgram(0);
		glDetachShader(programID, vsID);
		glDetachShader(programID, fsID);
		glDeleteShader(vsID);
		glDeleteShader(fsID);
		glDeleteProgram(programID);
	}
}
