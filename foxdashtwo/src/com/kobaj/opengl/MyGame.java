package com.kobaj.opengl;

import android.graphics.Color;
import android.opengl.GLES20;

import com.kobaj.foxdashtwo.R;
import com.kobaj.math.Constants;
import com.kobaj.math.Functions;
import com.kobaj.opengldrawable.EnumDrawFrom;
import com.kobaj.opengldrawable.Quad.QuadRenderTo;
import com.kobaj.screen.BaseScreen;
import com.kobaj.screen.BlankScreen;
import com.kobaj.screen.EnumScreenState;

public class MyGame extends MyGLRender
{	
	//test screen
	//private SinglePlayerScreen single_player_screen;
	private BaseScreen currently_active_screen;
	private BaseScreen next_active_screen;
	
	public boolean draw_fps= true;
	
	//dont touch the variables below this line
	//final drawable.
	private QuadRenderTo scene;

    public MyGame()
    {
    	//single_player_screen = new SinglePlayerScreen();
    	currently_active_screen = new BlankScreen(); //single_player_screen;
    }
    
    
    public void onChangeScreen(BaseScreen next_active_screen)
    {
			this.next_active_screen = next_active_screen;
    }
	
	//for the record this is called everytime the screen is reset/paused/resumed
	//all graphics are destroyed (dunno about sounds >.>).
	@Override
	protected void onInitialize()
	{
		//begin by aligning our functions
		Functions.adjustConstantsToScreen();
		
		currently_active_screen.onInitialize();
		
		//dont touch below this line.
		if(scene == null)
			scene = new QuadRenderTo();
		scene.onInitialize();
        
        System.gc();
	}
	
	@Override
	protected void onUpdate(double delta)
	{		
		//screen swap
		if(next_active_screen != null)
		{
			currently_active_screen = next_active_screen;
			next_active_screen = null;
			currently_active_screen.onInitialize();
		}
		
		//update as usual
		if(currently_active_screen.current_state == EnumScreenState.running)
			currently_active_screen.onUpdate(delta);
	}
	
	@Override
	protected void onDraw()
	{	
		if(currently_active_screen.current_state == EnumScreenState.running)
			onRunningDraw();
		else if(currently_active_screen.current_state == EnumScreenState.loading)
			onLoadingDraw();
		
		if(draw_fps)
		{
			//fps
			int fps_color = Color.BLUE;
			if(fps.fps < 60)
				fps_color = Color.GREEN;
			if(fps.fps < 45)
				fps_color = Color.YELLOW;
			if(fps.fps < 30)
				fps_color = Color.RED;
			
			int oos_color = Color.RED;
			if(Constants.objects_drawn_screen < 50)
				oos_color = Color.YELLOW;
			if(Constants.objects_drawn_screen < 35)
				oos_color = Color.GREEN;
			if(Constants.objects_drawn_screen < 20)
				oos_color = Color.BLUE;
			
			double x_pos = Functions.screenXToShaderX(100);
			double y_pos = Functions.screenYToShaderY((int)Functions.fix_y(50));
			
			Constants.text.drawText(R.string.fps, x_pos, y_pos, EnumDrawFrom.bottom_right);
			Constants.text.drawNumber(fps.fps, x_pos, y_pos, EnumDrawFrom.bottom_left, fps_color);
			
			Constants.text.drawText(R.string.oos, x_pos, y_pos, EnumDrawFrom.top_right);
			Constants.text.drawNumber(Constants.objects_drawn_screen, x_pos, y_pos, EnumDrawFrom.top_left, oos_color);
			Constants.objects_drawn_screen = 0;
		}
	}
	
	private void onLoadingDraw()
	{
		currently_active_screen.onDrawLoading(fps.getDelta());
	}
	
	private void onRunningDraw()
	{	
		//regular objects
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA); // no see thru
		if(scene.beginRenderToTexture())
		{	
			//put opaque items here
			currently_active_screen.onDrawObject();
		}
		scene.endRenderToTexture();
	
		//lights
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_SRC_ALPHA); // cheap lights

		//put translucent (lights) here
		currently_active_screen.onDrawLight();
		
		//final scene
		GLES20.glBlendFunc(GLES20.GL_DST_COLOR, GLES20.GL_ZERO); // masking
		scene.onDrawAmbient(Constants.identity_matrix, Constants.my_proj_matrix, Color.WHITE, true);
		
		//text below this line
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA); // no see thru
		currently_active_screen.onDrawConstant();	
	}

	@Override
	protected void onPause()
	{
		
	}
}