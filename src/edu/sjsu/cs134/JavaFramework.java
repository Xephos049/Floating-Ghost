/**
 * Created by jared on 2/9/16.
 */
package edu.sjsu.cs134;

//import javax.media.nativewindow.WindowClosingProtocol;
//import javax.media.opengl.*;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.nativewindow.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class JavaFramework {
	// Set this to true to force the game to exit.
	private static boolean shouldExit;

	// The previous frame's keyboard state.
	private static boolean kbPrevState[] = new boolean[256];

	// The current frame's keyboard state.
	private static boolean kbState[] = new boolean[256];

	// Position of the sprite.
	private static int[] spritePos = new int[2];

	// Texture for the sprite.
	private static int background, wall;
	private static int[] spriteidleR = new int[2];
	private static int[] spriteidleL = new int[2];
	private static int[] fireR = new int[2];
	private static int[] fireL = new int[2];
	public static int changedir = 0;
	static Sprite spr;
	static int parallexFrame;
	static int xwindow, ywindow, xbg, ybg, xmax, ymax;
	static int time, increase = 0;
	static int[][] tiles;
	static int staticPos1, staticPos0;
	static ArrayList<AI> ais;

	// Size of the sprite.
	private static int[] spriteSize = new int[] { 50, 50 };

	static GL2 gl;
	static Background para, bac;
	static Camera cam;
	static AI ai;
	static Projectile fire;

	static// Direction sprite is facing
	boolean moveright = true, faceRight = true;

	public static void main(String[] args) {
		ais = new ArrayList<AI>();
		fire = new Projectile();
		GLProfile gl2Profile;
		fire.active = false;
		xmax = 1280;
		cam = new Camera();
		bac = new Background();
		para = new Background();
		spr = new Sprite();
		ai = new AI();
		ai.x = 0;
		ai.tempx = 0;
		ai.camx = 0;
		time = 50;
		cam.x = 0;
		cam.y = 0;
		bac.w = 40;
		bac.y = -120;
		xwindow = 640;
		ywindow = 480;
		tiles = new int[50][50];
		spr.x = (xwindow / 2) - (bac.w / 2);
		spr.y = 420;
		fire.camx = spr.camx + 22;
		fire.camy = spr.y + 20;
		fire.x = 0;
		spr.camy = spr.y;
		spr.camx = spr.x;

		staticPos0 = (xwindow / 2) - (bac.w / 2);
		spritePos[0] = staticPos0;
		staticPos1 = 405;
		spritePos[1] = staticPos1;

		try {
			// Make sure we have a recent version of OpenGL
			gl2Profile = GLProfile.get(GLProfile.GL2);
		} catch (GLException ex) {
			System.out.println("OpenGL max supported version is too low.");
			System.exit(1);
			return;
		}

		// Create the window and OpenGL context.
		GLWindow window = GLWindow.create(new GLCapabilities(gl2Profile));
		window.setSize(640, 480);
		window.setTitle("Java Framework");
		window.setVisible(true);
		window.setDefaultCloseOperation(WindowClosingProtocol.WindowClosingMode.DISPOSE_ON_CLOSE);
		window.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent keyEvent) {
				kbState[keyEvent.getKeyCode()] = true;
			}

			@Override
			public void keyReleased(KeyEvent keyEvent) {
				kbState[keyEvent.getKeyCode()] = false;
			}
		});

		// Setup OpenGL state.
		window.getContext().makeCurrent();
		gl = window.getGL().getGL2();
		gl.glViewport(cam.x, cam.y, 640, 480);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glOrtho(0, 640, 480, 0, 0, 100);
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

		// Load the texture

		ai = new AI();
		ai.spr = glTexImageTGAFile(gl, "turret.tga", new int[] { 59, 55 });

		ais.add(ai);
		ais.get(0).x = 600;
		ais.get(0).y = 550;
		ais.get(0).w = 50;
		ais.get(0).h = 50;
		ais.get(0).active = true;

		spriteidleR[0] = glTexImageTGAFile(gl, "ghostr1.tga", new int[] { 1000, 640 });
		spriteidleR[1] = glTexImageTGAFile(gl, "ghostr2.tga", new int[] { 1000, 640 });

		spriteidleL[0] = glTexImageTGAFile(gl, "ghostl1.tga", new int[] { 1000, 640 });
		spriteidleL[1] = glTexImageTGAFile(gl, "ghostl2.tga", new int[] { 1000, 640 });
		background = glTexImageTGAFile(gl, "starry_night.tga", new int[] { bac.w, bac.w });
		wall = glTexImageTGAFile(gl, "wall.tga", new int[] { bac.w, bac.w });

		fireR[0] = glTexImageTGAFile(gl, "firer1.tga", new int[] { 30, 10 });
		fireR[1] = glTexImageTGAFile(gl, "firer2.tga", new int[] { 30, 10 });
		fireL[0] = glTexImageTGAFile(gl, "firel1.tga", new int[] { 30, 10 });
		fireL[1] = glTexImageTGAFile(gl, "firel2.tga", new int[] { 30, 10 });

		for (int i = 0; i < tiles.length; i++)
			for (int j = 0; j < tiles[i].length; j++) {
				if (i == 1 || i == 41)
					tiles[i][j] = 1;
				else
					tiles[i][j] = 0;
			}

		// glDrawSprite(gl, spriteTex, spritePos[0], spritePos[1],
		// spriteSize[0], spriteSize[1]);

		// The game loop
		while (!shouldExit) {
			System.arraycopy(kbState, 0, kbPrevState, 0, kbState.length);

			// Actually, this runs the entire OS message pump.
			window.display();
			if (!window.isVisible()) {
				shouldExit = true;
				break;
			}

			// Game logic.
			if (kbState[KeyEvent.VK_ESCAPE]) {
				shouldExit = true;
			}

			if (kbState[KeyEvent.VK_LEFT]) {
				// for (int i = 0; i < tiles.length; i++)
				// Acting as if we are running a loop for existing walls
				int i = 1;
				if (!((double) ((spr.x - 1) / 40) <= i - 1) && tiles[i][0] == 1)
					// System.out.println(spr.x);
					if (bac.x + cam.x < 0 && spr.camx == staticPos0) {
						para.x += 3;
						bac.x += 5;
						spr.x -= 5;
						if (fire.active)
							fire.camx += 5;
					} else if (spr.camx > 0) {
						spr.camx -= 5;
						spr.x -= 5;
					}
				faceRight = false;
			}

			if (kbState[KeyEvent.VK_RIGHT]) {
				// for (int i = 0; i < tiles.length; i++)
				// Acting as if we are running a loop for existing walls
				int i = 41;
				if (!((double) ((spr.x) / 40) >= i - 2 && tiles[i][0] == 1))
					if ((bac.x + cam.x > -1000 && spr.camx == staticPos0)) {
						para.x -= 3;
						bac.x -= 5;
						spr.x += 5;
						if (fire.active)
							fire.camx -= 5;
					} else if (spr.camx < 590) {
						spr.camx += 5;
						spr.x += 5;
					}
				faceRight = true;
			}

			if (kbState[KeyEvent.VK_W]) {
				if (cam.y < 400) {
					para.y++;
					cam.y++;
					spr.camy++;
					ais.get(0).camy++;
					if (fire.active)
						fire.camy++;
				}
			}

			if (kbState[KeyEvent.VK_S]) {
				if (cam.y > 0) {
					para.y--;
					cam.y--;
					spr.camy--;
					ais.get(0).camy--;
					if (fire.active)
						fire.camy--;
				}
			}
			if (kbState[KeyEvent.VK_A]) {
				if (cam.x + bac.x < 0) {
					para.x++;
					cam.x++;
					spr.camx++;
					ais.get(0).camx++;
					if (fire.active)
						fire.camx++;
				}
			}
			if (kbState[KeyEvent.VK_D]) {
				if (cam.x + bac.x > -750) {
					para.x--;
					cam.x--;
					spr.camx--;
					ais.get(0).camx--;
					if (fire.active)
						fire.camx--;
				}
			}

			if (kbState[KeyEvent.VK_SPACE]) {
				// drawProjectile(spr.x + 50, spr.y + 10, faceRight, time, ais);
				if (!fire.active) {
					fire.camx = spr.camx + 20;
					// fire.x = spr.camx + 20;
					fire.active = true;
				}
			}

			gl.glClearColor(0, 0, 0, 1);
			gl.glClear(GL2.GL_COLOR_BUFFER_BIT);

			/* Draw sprite */

			// Determine direction sprite is facing and update frame depending
			// on time
			if (faceRight) {
				updateSpr(time, 0);
			} else {
				updateSpr(time, 1);
			}

			// Reset Timer
			if (time == 0)
				time = 50;
			else
				time--;
		}
		System.exit(0);
	}

	public static void updateSpr(int time, int dir) {
		// Draw background
		int xmin, ymin, xinc = 18, yinc = 14;
		xmin = (int) Math.floor(0 - (cam.x + bac.x) / bac.w);
		ymin = (int) Math.floor(0 - (cam.y + bac.y) / bac.w);
		for (int i = xmin; i < xmin + xinc; i++) {
			for (int j = ymin; j < ymin + yinc; j++) {
				if (tiles[i][j] == 0)
					glDrawSprite(gl, background, (i * 40) + cam.x + bac.x - 40, (j * 40) + bac.y + cam.y - 40, bac.w,
							bac.w);
				else
					glDrawSprite(gl, wall, (i * 40) + cam.x + bac.x - 40, (j * 40) + bac.y + cam.y - 40, bac.w, bac.w);
			}
		}

		// draw turret here and automatically fire towards left

		ais.get(0).x = ai.camx + bac.x + 600;
		if (time % 2 == 0) {
			if (moveright) {
				if (ai.tempx <= 100) {
					ais.get(0).x++;
					ai.tempx++;
					ai.camx++;
				} else
					moveright = false;
			} else {
				if (ai.tempx >= 0) {
					ais.get(0).x--;
					ai.tempx--;
					ai.camx--;
				} else
					moveright = true;
			}
		}
		if (ais.get(0).active) {
			glDrawSprite(gl, ais.get(0).spr, ais.get(0).x, ai.camy + bac.y + 550, 50, 50);
		}

		// Draw player sprite

		if (spr.camx > -35 && spr.camy > -35 && spr.camy < 470 && spr.camx < 630) {
			if (dir == 0) {
				if (time == 0) {
					if (changedir == 0) {
						changedir = 1;
					} else
						changedir = 0;
				}
				glDrawSprite(gl, spriteidleR[changedir], spr.camx, spr.camy, spriteSize[0], spriteSize[1]);
			} else {
				if (time == 0) {
					if (changedir == 0) {
						changedir = 1;
					} else
						changedir = 0;
				}
				glDrawSprite(gl, spriteidleL[changedir], spr.camx, spr.camy, spriteSize[0], spriteSize[1]);
			}
		}

		// Draw projectile

		// TODO fix bugs

		if (!fire.active)
			fire.right = faceRight;
		if (fire.active) {

			if (fire.right) {
				fire.x += 2;
				glDrawSprite(gl, fireR[0], fire.camx + fire.x, fire.y + fire.camy, 30, 10);
			} else {
				fire.x -= 2;
				glDrawSprite(gl, fireL[0], fire.camx + fire.x - 50, fire.y + fire.camy, 30, 10);
			}
			increase++;
			if ((fire.camx + fire.x < -60 || fire.camx + fire.x > 630)) {
				fire.active = false;
				increase = 0;
				fire.x = 0;
			} else if ((fire.x + fire.camx + 30 >= ais.get(0).x && fire.right)
					|| (fire.x + fire.camx - 50 <= ais.get(0).x + ais.get(0).w) && !fire.right) {
				fire.active = false;
				fire.x = 0;
				increase = 0;
			}
		}
	}

	// Load a file into an OpenGL texture and return that texture.
	public static int glTexImageTGAFile(GL2 gl, String filename, int[] out_size) {
		final int BPP = 4;

		DataInputStream file = null;
		try {
			// Open the file.
			file = new DataInputStream(new FileInputStream(filename));
		} catch (FileNotFoundException ex) {
			System.err.format("File: %s -- Could not open for reading.", filename);
			return 0;
		}

		try {
			// Skip first two bytes of data we don't need.
			file.skipBytes(2);

			// Read in the image type. For our purposes the image type
			// should be either a 2 or a 3.
			int imageTypeCode = file.readByte();
			if (imageTypeCode != 2 && imageTypeCode != 3) {
				file.close();
				System.err.format("File: %s -- Unsupported TGA type: %d", filename, imageTypeCode);
				return 0;
			}

			// Skip 9 bytes of data we don't need.
			file.skipBytes(9);

			int imageWidth = Short.reverseBytes(file.readShort());
			int imageHeight = Short.reverseBytes(file.readShort());
			int bitCount = file.readByte();
			file.skipBytes(1);

			// Allocate space for the image data and read it in.
			byte[] bytes = new byte[imageWidth * imageHeight * BPP];

			// Read in data.
			if (bitCount == 32) {
				for (int it = 0; it < imageWidth * imageHeight; ++it) {
					bytes[it * BPP + 0] = file.readByte();
					bytes[it * BPP + 1] = file.readByte();
					bytes[it * BPP + 2] = file.readByte();
					bytes[it * BPP + 3] = file.readByte();
				}
			} else {
				for (int it = 0; it < imageWidth * imageHeight; ++it) {
					bytes[it * BPP + 0] = file.readByte();
					bytes[it * BPP + 1] = file.readByte();
					bytes[it * BPP + 2] = file.readByte();
					bytes[it * BPP + 3] = -1;
				}
			}

			file.close();

			// Load into OpenGL
			int[] texArray = new int[1];
			gl.glGenTextures(1, texArray, 0);
			int tex = texArray[0];
			gl.glBindTexture(GL2.GL_TEXTURE_2D, tex);
			gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA, imageWidth, imageHeight, 0, GL2.GL_BGRA,
					GL2.GL_UNSIGNED_BYTE, ByteBuffer.wrap(bytes));
			gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
			gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);

			out_size[0] = imageWidth;
			out_size[1] = imageHeight;
			return tex;
		} catch (IOException ex) {
			System.err.format("File: %s -- Unexpected end of file.", filename);
			return 0;
		}
	}

	public static void glDrawSprite(GL2 gl, int tex, int x, int y, int w, int h) {
		gl.glBindTexture(GL2.GL_TEXTURE_2D, tex);
		gl.glBegin(GL2.GL_QUADS);
		{
			gl.glColor3ub((byte) -1, (byte) -1, (byte) -1);
			gl.glTexCoord2f(0, 1);
			gl.glVertex2i(x, y);
			gl.glTexCoord2f(1, 1);
			gl.glVertex2i(x + w, y);
			gl.glTexCoord2f(1, 0);
			gl.glVertex2i(x + w, y + h);
			gl.glTexCoord2f(0, 0);
			gl.glVertex2i(x, y + h);
		}
		gl.glEnd();
	}
}

class Camera {
	public int x;
	public int y;
}

class Background {
	public int x;
	public int y;
	public int w;
}

class Sprite {
	public int x;
	public int camx;
	public int y;
	public int camy;
}

class AI {
	public int spr;
	public int x;
	public int y;
	public int camx;
	public int camy;
	public int w;
	public int h;
	public boolean active;
	public int tempx;
}

class Projectile {
	public int x;
	public int y;
	public int camx;
	public int camy;
	public boolean active;
	public boolean right;
}