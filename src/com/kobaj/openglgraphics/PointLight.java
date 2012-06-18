package com.kobaj.openglgraphics;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.kobaj.foxdashtwo.R;

public class PointLight extends BaseLight
{
	// light stuffs
	private final float[] my_light_model_space = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
	
	private final float[] my_light_world_space = new float[4];
	
	public final float[] my_light_eye_space = new float[4];
	
	private float[] my_light_matrix = new float[16];
	public int my_light_pos_handle;
	
	public PointLight()
	{
		onInitializeShaders(R.raw.vertex_shader, R.raw.fragment_shader);
		my_light_pos_handle = GLES20.glGetUniformLocation(my_shader, "u_LightPos");
	}
	
	public void onUpdateFrame(double delta, float[] my_view_matrix)
	{
		//TODO gotta change this to work with delta.
		// light
		// Calculate position of the light. Rotate and then push into the
		// distance.
		// Do a complete rotation every 10 seconds.
		long time = SystemClock.uptimeMillis() % 10000L;
		float angleInDegrees = (360.0f / 10000.0f) * ((int) time);
		
		Matrix.setIdentityM(my_light_matrix, 0);
		Matrix.translateM(my_light_matrix, 0, 0.0f, 0.0f, -5.0f);
		Matrix.rotateM(my_light_matrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
		Matrix.translateM(my_light_matrix, 0, 0.0f, 0.0f, 2.0f);
		
		Matrix.multiplyMV(my_light_world_space, 0, my_light_matrix, 0, my_light_model_space, 0);
		Matrix.multiplyMV(my_light_eye_space, 0, my_view_matrix, 0, my_light_world_space, 0);
	}
}
