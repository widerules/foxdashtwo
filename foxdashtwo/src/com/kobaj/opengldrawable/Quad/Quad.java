package com.kobaj.opengldrawable.Quad;

//a lot of help from
//http://www.learnopengles.com/android-lesson-one-getting-started/
//https://developer.android.com/resources/tutorials/opengl/opengl-es20.html	

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.Matrix;

import com.kobaj.loader.GLBitmapReader;
import com.kobaj.loader.GLLoadedTexture;
import com.kobaj.math.Constants;
import com.kobaj.math.Functions;
import com.kobaj.math.RectFExtended;
import com.kobaj.opengldrawable.EnumDrawFrom;

public class Quad
{
	// and placed in the exact center of the quad
	public EnumDrawFrom currently_drawn = EnumDrawFrom.center;
	
	// these are in shader coordinates 0 to 1
	// anyway, dont be fooled, these are public to READ but not public to SET
	public double x_pos = 0.0;
	public double y_pos = 0.0;
	public double x_acc = 0.0;
	public double y_acc = 0.0;
	public double x_vel = 0.0;
	public double y_vel = 0.0;
	
	// this is new
	public int color = Color.WHITE;
	
	// z index doesnt have to specially be set.
	// objects will only collide if on the same z index plane.
	// this shouldn't really change much actually.
	public double z_pos = -1.0f;
	
	// other values
	public double scale_value = 1.0;
	public double degree = 0;
	
	// physics rectangle. An object can have multiple
	// rectangles so it has better 'resolution' when interacting with other quads
	// phys rect is stored in shader coordinates
	public ArrayList<RectFExtended> phys_rect_list = new ArrayList<RectFExtended>();
	
	// maximul AABB is calculated by the engine
	// used to determine if an object is on screen
	// also helpful in physics
	// stored in shader coordinates
	public RectFExtended best_fit_aabb = new RectFExtended();
	public RectFExtended unrotated_aabb = new RectFExtended();
	
	// begin by holding these
	// should be read only to outside classes...
	public int width;
	public int height;
	public double shader_width;
	public double shader_height;
	public int square;
	
	// data about the quad
	private float[] my_position_matrix = new float[18];
	protected FloatBuffer my_position;
	protected FloatBuffer my_tex_coord;
	
	// movement matrixes
	// transformation matrix to convert from object to world space
	public float[] my_model_matrix = new float[16];
	private float[] translation_matrix = new float[16];
	private float[] rotation_matrix = new float[16];
	private float[] scale_matrix = new float[16];
	private float[] my_rs_matrix = new float[16];
	
	// handle to texture
	protected int my_texture_data_handle = -1;
	protected int texture_resource = -1;
	
	// this is a temporary bitmap that holds onto a bitmap that is passed in
	// just long enough so that it gets loaded onto the gpu
	// then it gets disposed.
	private Bitmap nullify_me;
	
	// constructors
	protected Quad()
	{
		// do nothing. Assume whoever is extending knows what he/she is doing.
	}
	
	public Quad(int texture_resource, int width, int height)
	{
		// load dat texture.
		com.kobaj.loader.GLBitmapReader.loadTextureFromResource(texture_resource, false);
		onCreate(texture_resource, width, height);
	}
	
	public Quad(int texture_resource, Bitmap bmp, int width, int height)
	{
		nullify_me = bmp;
		com.kobaj.loader.GLBitmapReader.loadTextureFromBitmap(texture_resource, bmp);
		onCreate(texture_resource, width, height);
	}
	
	public void onUnInitialize()
	{
		GLBitmapReader.unloadTexture(texture_resource);
	}
	
	// method that will go and get the texture handle after it has been loaded
	// so that we can draw the texture!
	public boolean setTextureDataHandle()
	{
		if (my_texture_data_handle != -1)
			return true;
		
		if (texture_resource != -1)
		{
			GLLoadedTexture proposed_handle = GLBitmapReader.loaded_textures.get(texture_resource);
			if (proposed_handle != null)
			{
				if (nullify_me != null)
					nullify_me = null;
				
				my_texture_data_handle = proposed_handle.texture_id;
				return true;
			}
		}
		
		return false;
	}
	
	// actual constructor
	// width and height in screen coordinates 0 - 800
	protected void onCreate(int texture_resource, int width, int height)
	{
		// set our texture resource
		this.texture_resource = texture_resource;
		
		// width height data
		setWidthHeight(width, height);
		
		// texture data
		final int tr_square_x = com.kobaj.math.Functions.nearestPowerOf2(width);
		final int tr_square_y = com.kobaj.math.Functions.nearestPowerOf2(height);
		
		square = Math.max(tr_square_x, tr_square_y);
		
		final float tex_y = (float) com.kobaj.math.Functions.linearInterpolateUnclamped(0, square, height, 0, 1);
		final float tex_x = (float) com.kobaj.math.Functions.linearInterpolateUnclamped(0, square, width, 0, 1);
		
		simpleUpdateTexCoords(tex_x, tex_y);
		
		final float tr_x = (float) (this.shader_width / 2.0);
		final float tr_y = (float) (this.shader_height / 2.0);
		
		// up next setup phys rect list. Just a default. The user can
		// set/add/remove more rectangles as needed.
		if (phys_rect_list.isEmpty())
			phys_rect_list.add(new RectFExtended(-tr_x, tr_y, tr_x, -tr_y));
	
		// finally
		update_position_matrix(true);
	}
	
	// methods for calculating stuffs
	protected void simpleUpdateTexCoords(float tex_x, float tex_y)
	{
		complexUpdateTexCoords(0, tex_x, 0, tex_y);
	}
	
	// these are in shader coordinates. start_x, end_x, start_y, end_y
	protected void complexUpdateTexCoords(float one_x, float two_x, float one_y, float two_y)
	{
		// only time I use floats...
		float buffer = -0.005f;
		one_x -= buffer;
		two_x += buffer;
		one_y -= buffer;
		two_y += buffer;
		
		// S, T (or X, Y)
		// Texture coordinate data.
		final float[] cubeTextureCoordinateData = {
				// Front face
				one_x, -one_y,//
				one_x, -two_y,//
				two_x, -one_y,//
				
				one_x, -two_y,//
				two_x, -two_y,//
				two_x, -one_y };//
		
		if (my_tex_coord == null)
			my_tex_coord = ByteBuffer.allocateDirect(cubeTextureCoordinateData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		else
			my_tex_coord.clear();
		
		my_tex_coord.put(cubeTextureCoordinateData).position(0);
	}
	
	// this is a value between 1.0 and
	public void setScale(double scale_value)
	{
		this.scale_value = scale_value;
		
		// then the physics
		for (int i = phys_rect_list.size() - 1; i >= 0; i--)
			phys_rect_list.get(i).setScale(scale_value);
		
		this.update_position_matrix(true);
	}
	
	// rotate from the center
	public void setRotationZ(double degrees)
	{
		this.degree = degrees;
		this.update_position_matrix(true);
	}
	
	// width and height are in screen values 0 - 800
	// scale will override width and height if it is not 1.
	public void setWidthHeight(int width, int height)
	{
		// store these for our bounding rectangle
		this.width = width;
		this.height = height;
		
		// Define points for a cube.
		this.shader_width = Functions.screenWidthToShaderWidth(width);
		this.shader_height = Functions.screenHeightToShaderHeight(height);
		
		float pos_tr_x = (float) (this.shader_width / 2.0);
		float pos_tr_y = (float) (this.shader_height / 2.0f);
		
		float neg_tr_x = -pos_tr_x;
		float neg_tr_y = -pos_tr_y;
		
		final float z_buffer = 0.0f;
		
		// X, Y, Z
		my_position_matrix[0] = neg_tr_x;
		my_position_matrix[1] = pos_tr_y;
		my_position_matrix[2] = z_buffer;
		
		my_position_matrix[3] = neg_tr_x;
		my_position_matrix[4] = neg_tr_y;
		my_position_matrix[5] = z_buffer;
		
		my_position_matrix[6] = pos_tr_x;
		my_position_matrix[7] = pos_tr_y;
		my_position_matrix[8] = z_buffer;
		
		my_position_matrix[9] = neg_tr_x;
		my_position_matrix[10] = neg_tr_y;
		my_position_matrix[11] = z_buffer;
		
		my_position_matrix[12] = pos_tr_x;
		my_position_matrix[13] = neg_tr_y;
		my_position_matrix[14] = z_buffer;
		
		my_position_matrix[15] = pos_tr_x;
		my_position_matrix[16] = pos_tr_y;
		my_position_matrix[17] = z_buffer;
		
		// Initialize the buffers. and store the new coords
		if (my_position == null)
			my_position = ByteBuffer.allocateDirect(my_position_matrix.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		else
			my_position.clear();
		
		my_position.put(my_position_matrix).position(0);
		
		updateBestFitAABB();
	}
	
	private void updateBestFitAABB()
	{
		double x_maximul = Double.MIN_VALUE;
		double y_maximul = Double.MIN_VALUE;
		double x_minimul = Double.MAX_VALUE;
		double y_minimul = Double.MAX_VALUE;
		
		double un_x_max = Double.MIN_VALUE;
		double un_y_max = Double.MIN_VALUE;
		double un_x_min = Double.MAX_VALUE;
		double un_y_min = Double.MAX_VALUE;
		
		final double rads = (float) Math.toRadians(degree);
		final double cos_rads = Math.cos(rads);
		final double sin_rads = Math.sin(rads);
		
		// rotate and convert
		for (int i = 0; i < 18; i = i + 3)
		{
			// why not just do Matrix.mulitplyMV(our matrix, our vector).
			// because that doesn't work. Try it yourself.
			
			// apply transforms
			double tr_x1 = my_position_matrix[i] * scale_value;
			double tr_y1 = my_position_matrix[i + 1] * scale_value;
			
			final double tr_x2 = (tr_x1 * cos_rads - tr_y1 * sin_rads);
			final double tr_y2 = (tr_y1 * cos_rads + tr_x1 * sin_rads);
			
			// calculate max and min
			if (tr_x2 > x_maximul)
				x_maximul = tr_x2;
			if (tr_x2 < x_minimul)
				x_minimul = tr_x2;
			if (tr_y2 > y_maximul)
				y_maximul = tr_y2;
			if (tr_y2 < y_minimul)
				y_minimul = tr_y2;
			
			// calculate max and min
			if (tr_x1 > un_x_max)
				un_x_max = tr_x1;
			if (tr_x1 < un_x_min)
				un_x_min = tr_x1;
			if (tr_y1 > un_y_max)
				un_y_max = tr_y1;
			if (tr_y1 < un_y_min)
				un_y_min = tr_y1;
		}
		
		// set our maximul aabb
		best_fit_aabb.setExtendedRectF(x_minimul, y_maximul, x_maximul, y_minimul);
		best_fit_aabb.setPositionWithOffset(x_pos, y_pos);
		
		unrotated_aabb.setExtendedRectF(un_x_min, un_y_max, un_x_max, un_y_min);
		unrotated_aabb.setPositionWithOffset(x_pos, y_pos);
	}
	
	public void setZPos(double z)
	{
		this.z_pos = z;
		update_position_matrix(false);
	}
	
	// these x and y are in shader space 0 to 1
	public void setXYPos(double x, double y, EnumDrawFrom where)
	{
		currently_drawn = where;
		
		if (where == EnumDrawFrom.top_left)
		{
			// positive x
			// negative y
			this.x_pos = x + shader_width / 2.0;
			this.y_pos = y - shader_height / 2.0;
		}
		else if (where == EnumDrawFrom.top_right)
		{
			this.x_pos = x - shader_width / 2.0;
			this.y_pos = y - shader_height / 2.0;
		}
		else if (where == EnumDrawFrom.bottom_left)
		{
			this.x_pos = x + shader_width / 2.0;
			this.y_pos = y + shader_height / 2.0;
		}
		else if (where == EnumDrawFrom.bottom_right)
		{
			
			this.x_pos = x - shader_width / 2.0;
			this.y_pos = y + shader_height / 2.0;
		}
		else
		{
			x_pos = x;
			y_pos = y;
		}
		
		update_position_matrix(false);
		
		// set the rectangle
		unrotated_aabb.setPositionWithOffset(x_pos, y_pos);
		best_fit_aabb.setPositionWithOffset(x_pos, y_pos);
		for (int i = phys_rect_list.size() - 1; i >= 0; i--)
			phys_rect_list.get(i).setPositionWithOffset(x_pos, y_pos);
	}
	
	private void update_position_matrix(boolean also_update_scale_or_rotation)
	{
		// set the quad up
		if (also_update_scale_or_rotation)
		{
			Matrix.setIdentityM(my_model_matrix, 0);
			Matrix.setIdentityM(scale_matrix, 0);
			Matrix.scaleM(scale_matrix, 0, (float) this.scale_value, (float) this.scale_value, (float) this.scale_value);
			Matrix.setRotateEulerM(rotation_matrix, 0, 0.0f, 0.0f, (float) -degree);
			Matrix.multiplyMM(my_rs_matrix, 0, rotation_matrix, 0, scale_matrix, 0);
			
			// and our aabb
			updateBestFitAABB();
		}
		
		Matrix.setIdentityM(translation_matrix, 0);
		Matrix.translateM(translation_matrix, 0, (float) x_pos, (float) y_pos, (float) z_pos);
		
		Matrix.multiplyMM(this.my_model_matrix, 0, translation_matrix, 0, my_rs_matrix, 0);
	}
	
	// ouside calls, now just a nice wrapper...
	public void onDrawAmbient()
	{
		onDrawAmbient(Constants.my_vp_matrix, false);
	}
	
	public void onDrawAmbient(float[] my_vp_matrix, boolean skip_draw_check)
	{
		// If on screen, draw.
		QuadRenderShell.onDrawQuad(my_vp_matrix, skip_draw_check, Constants.ambient_light, this);
	}
}
