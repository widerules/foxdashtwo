package com.kobaj.loader;

//http://blog.poweredbytoast.com/loading-opengl-textures-in-android

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;
import android.util.SparseArray;

import com.kobaj.foxdashtwo.FoxdashtwoActivity;
import com.kobaj.openglgraphics.ETC1Extended;

public class GLBitmapReader
{
	// key is the res id
	// just a nifty way of storing images we've already loaded
	// this might eventually be changed to a base class so that
	// it can contain sounds and other actually loaded resources.
	// but most likely not :/
	public static SparseArray<GLLoadedTexture> loaded_textures = new SparseArray<GLLoadedTexture>();
	
	// way of giving out resource id's to objects that dont have them natively
	private static int loaded_resource_id = 9;
	
	// in case context is lost
	public static void resetLoadedTextures()
	{
		// forced to use an iterator
		int[] temp = new int[loaded_textures.size()];
		for (int i = loaded_textures.size() - 1; i >= 0; i--)
			temp[i] = loaded_textures.valueAt(i).texture_id;
		
		if (temp.length > 0)
			GLES20.glDeleteTextures(loaded_textures.size(), temp, 0);
		loaded_textures = new SparseArray<GLLoadedTexture>();
	}
	
	// get a new unused resource id
	public static int newResourceID()
	{
		loaded_resource_id++;
		return loaded_resource_id;
	}
	
	// Get a new unused texture id:
	public static int newTextureID()
	{
		int[] temp = new int[1];
		GLES20.glGenTextures(1, temp, 0);
		return temp[0];
	}
	
	// Will load a texture out of a drawable resource file, and return an OpenGL
	// texture id
	public static void loadTextureFromResource(int resource, boolean is_compressed)
	{
		if (is_compressed)
			loadTextureFromETC1(resource);
		else
		{
			// This will tell the BitmapFactory to not scale based on the device's
			// pixel density:
			// (Thanks to Matthew Marshall for this bit)
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inScaled = false;
			opts.inPurgeable = true;
			
			// Load up, and flip the texture:
			final Bitmap temp = BitmapFactory.decodeResource(com.kobaj.math.Constants.resources, resource, opts);
			
			loadTextureFromBitmap(resource, temp);
		}
	}
	
	public static void loadTextureFromETC1(final int resource)
	{
		FoxdashtwoActivity.mGLView.queueEvent(new Runnable()
		{
			public void run()
			{
				// get our ETC1 system ready
				ETC1Extended my_etc1 = new ETC1Extended();
				
				// see if stuff is already loaded
				GLLoadedTexture loaded_item = loaded_textures.get(resource);
				if (loaded_item != null)
				{
					// see if its a duplicate
					if (loaded_item.bitmap_hash != my_etc1.getETC1Hash(resource))
						Log.e("loading_collision",
								": loadTextureFromBitmap There was a collision with texture. Resource ID: " + resource + " ... " + loaded_item.bitmap_hash + ": " + my_etc1.getETC1Hash(resource));
					
					// don't overwrite
					return;
				}
				
				// prepair our entry
				GLLoadedTexture load = new GLLoadedTexture();
				load.resource_id = resource;
				load.height = -1;
				load.width = -1;
				load.bitmap_hash = my_etc1.getETC1Hash(resource);
				
				load.texture_id = my_etc1.loadETC1(resource);
				
				// save in our sources
				loaded_textures.put(resource, load);
			}
		});
	}
	
	public static void loadTextureFromBitmap(final int resource, final Bitmap temp)
	{
		FoxdashtwoActivity.mGLView.queueEvent(new Runnable()
		{
			public void run()
			{
				// get an item from our loaded resources (to see if this is a duplicate entry and thus can be skipped).
				GLLoadedTexture loaded_item = loaded_textures.get(resource);
				int hash = temp.hashCode();
				if (loaded_item != null)
				{
					// see if its a duplicate
					if (loaded_item.width != temp.getWidth() || loaded_item.height != temp.getHeight() || loaded_item.bitmap_hash != hash)
						Log.e("loading_collision", ": loadTextureFromBitmap There was a collision with texture. Resource ID: " + resource + " ... " + loaded_item.bitmap_hash + ": " + hash);
					
					// don't overwrite
					return;
				}
				
				// flip it the right way around.
				Matrix flip = new Matrix();
				flip.postScale(1f, -1f);
				
				int original_width = temp.getWidth();
				int original_height = temp.getHeight();
				int original_hash = hash;
				
				Bitmap bmp1 = Bitmap.createBitmap(temp, 0, 0, original_width, original_height, flip, true);
				temp.recycle();
				
				int square_width = com.kobaj.math.Functions.nearestPowerOf2(bmp1.getWidth());
				int square_height = com.kobaj.math.Functions.nearestPowerOf2(bmp1.getHeight());
				
				int new_size = Math.max(square_width, square_height);
				
				Bitmap bmp = Bitmap.createBitmap(new_size, new_size, Bitmap.Config.ARGB_8888);
				Canvas temp_canvas = new Canvas(bmp);
				temp_canvas.drawBitmap(bmp1, 0, new_size - original_height, new Paint());
				
				bmp1.recycle();
				
				// make a loader
				// put out info into someplace safe.
				GLLoadedTexture load = new GLLoadedTexture();
				load.resource_id = resource;
				load.height = original_height;
				load.width = original_width;
				load.bitmap_hash = original_hash;
				
				// In which ID will we be storing this texture?
				int id = newTextureID();
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id);
				
				// Set all of our texture parameters:
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
				
				// Push the bitmap onto the GPU:
				GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
				
				// set the id
				load.texture_id = id;
				
				// save in our sources
				loaded_textures.put(resource, load);
				
				// cleanup!
				bmp.recycle();
			}
		});
	}
}