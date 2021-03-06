package nl.klaasyk.apps.lwjglengine;

import org.lwjgl.BufferUtils;
import org.lwjgl.Version;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import nl.klaasyk.apps.lwjglengine.util.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.awt.Color;
import java.io.IOException;
import java.nio.FloatBuffer;

public class App {

	// We need to strongly reference callback instances.
	private GLFWErrorCallback errorCallback;
	private GLFWKeyCallback keyCallback;

	// The window handle
	private long window;

	public void run() throws IOException {
		System.out.println("LWJGL version " + Version.getVersion());
		try {
			init();

			loop();

			// Release window and window callbacks
			glfwDestroyWindow(window);
			keyCallback.release();
		} finally {
			// Terminate GLFW and release the GLFWErrorCallback
			glfwTerminate();
			errorCallback.release();
		}
		System.out.println("test");
	}

	private void init() {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if (glfwInit() != GL11.GL_TRUE)
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure our window
		glfwDefaultWindowHints(); // optional, the current window hints are
									// already the default
		glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden
												// after creation
		glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); // the window will be resizable

		int WIDTH = 300;
		int HEIGHT = 300;

		// Create the window
		window = glfwCreateWindow(WIDTH, HEIGHT, "Hello World!", NULL, NULL);
		if (window == NULL)
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                    glfwSetWindowShouldClose(window, GLFW_TRUE); // We will detect this in our rendering loop
            }
        });

     // Get the resolution of the primary monitor
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // Center our window
        glfwSetWindowPos(
            window,
            (vidmode.width() - WIDTH) / 2,
            (vidmode.height() - HEIGHT) / 2
        );
        

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);

		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);
	}

	private void loop() throws IOException {
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();

		System.out.println("OpenGL version " + glGetString(GL_VERSION));
		System.out.println("GLSL version " + glGetString(GL_SHADING_LANGUAGE_VERSION));

		// Create and attach the shaders

		ShaderProgram p = new ShaderProgram("vertex.vert", "fragment.frag");

		// Generate and bind a Vertex Array
		final int vaoID = glGenVertexArrays();
		glBindVertexArray(vaoID);
		final float[] vertices = new float[] { +0.0f, +0.8f, // Top coordinate
				-0.8f, -0.8f, // Bottom-left coordinate
				+0.8f, -0.8f // Bottom-right coordinate
		};
		final float[] texcoords = new float[] { +0.0f, +0.0f, +1.0f, +0.0f, +1.0f, +1.0f };
		FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length);
		FloatBuffer textureBuffer = BufferUtils.createFloatBuffer(texcoords.length);
		verticesBuffer.put(vertices).flip();
		textureBuffer.put(texcoords).flip();
		final int vboID = glGenBuffers();
		final int vboTex = glGenBuffers();

		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
		glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);

		glBindBuffer(GL_ARRAY_BUFFER, vboTex);
		glBufferData(GL_ARRAY_BUFFER, textureBuffer, GL_STATIC_DRAW);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);

		// Load the texture
		final int texID = TextureLoader.loadTextureFromImage("texture.png");

		final TextRenderer norm = new TextRenderer("fonts/OpenSans-Regular.ttf",16, Color.BLUE);
		
		// Set the clear color
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		while (glfwWindowShouldClose(window) == GL_FALSE) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

			p.use();

			glBindVertexArray(vaoID);
			glEnableVertexAttribArray(0);
			glEnableVertexAttribArray(1);

			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			glBindTexture(GL_TEXTURE_2D, texID);

			//glDrawArrays(GL_TRIANGLES, 0, 3);
			
			glBindTexture(GL_TEXTURE_2D, 0);

			glDisableVertexAttribArray(0);
			glDisableVertexAttribArray(1);
			glBindVertexArray(0);

			norm.renderString(0, 0, "T");
			
			p.unbind();
			
			
			
			glfwSwapBuffers(window); // swap the color buffers

			// Poll for window events. The key callback above will only be
			// invoked during this call.
			glfwPollEvents();
		}

		// Clean up shaders
		p.dispose();

		glBindVertexArray(0);
		glDeleteVertexArrays(vaoID);

		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glDeleteBuffers(vboID);
		glDeleteBuffers(vboTex);

		TextureLoader.dispose(texID);
		norm.dispose();

	}

	public static void main(String[] args) throws IOException {
		new App().run();

	}

}
