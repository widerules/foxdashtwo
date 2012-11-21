package com.kobaj.level;

import java.util.ArrayList;
import java.util.Collections;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import android.graphics.Color;

import com.kobaj.level.LevelEventTypes.LevelEventTransportPlayer;
import com.kobaj.level.LevelTypeLight.LevelAmbientLight;
import com.kobaj.level.LevelTypeLight.LevelBloomLight;
import com.kobaj.level.LevelTypeLight.LevelCustomLight;
import com.kobaj.level.LevelTypeLight.LevelPointLight;
import com.kobaj.level.LevelTypeLight.LevelSpotLight;
import com.kobaj.math.Constants;
import com.kobaj.math.Functions;
import com.kobaj.opengldrawable.Quad.QuadColorShape;

public class Level
{
	@Element
	public int backdrop_color;
	private QuadColorShape my_backdrop;
	
	@Element
	public int left_limit;
	@Element
	public int top_limit;
	@Element
	public int right_limit;
	@Element
	public int bottom_limit;
	
	public double left_shader_limit;
	public double top_shader_limit;
	public double right_shader_limit;
	public double bottom_shader_limit;
	
	@ElementList
	public ArrayList<LevelObject> object_list;
	
	@ElementList
	public ArrayList<LevelAmbientLight> light_list; // all lights including blooms
	
	ArrayList<LevelBloomLight> bloom_light_list = new ArrayList<LevelBloomLight>(); // only blooms
	
	@Element
	public LevelObject player;
	
	@ElementList
	public ArrayList<LevelEvent> event_list;
	
	// no constructor
	
	public void onInitialize()
	{
		// backdrop
		if(backdrop_color != Color.TRANSPARENT)
		{
			my_backdrop = new QuadColorShape(1,1, backdrop_color, 0);
			my_backdrop.setWidthHeight(Constants.width, Constants.height);
			my_backdrop.z_pos -= (10.0 * Constants.z_modifier);
		}
		
		// pre-player
		double x_player = Functions.screenXToShaderX(player.x_pos);
		double y_player = Functions.screenYToShaderY(player.y_pos);
		
		bloom_light_list.clear();
		
		// setup general objects
		for (int i = object_list.size() - 1; i >= 0; i--)
			object_list.get(i).onInitialize();
		
		//sort the objects
		 Collections.sort(object_list, new ObjectDrawSort());
		
		// setup lights
		for (int i = light_list.size() - 1; i >= 0; i--)
		{
			// store bloom lights in another array for easy use later
			light_list.get(i).onInitialize();
			if (LevelPointLight.class.isAssignableFrom(light_list.get(i).getClass()))
			{
				LevelPointLight temp = LevelPointLight.class.cast(light_list.get(i));
				if (temp.is_bloom)
					bloom_light_list.add(temp);
			}
			else if(LevelCustomLight.class.isAssignableFrom(light_list.get(i).getClass()))
			{
				LevelCustomLight temp = LevelCustomLight.class.cast(light_list.get(i));
				if(temp.is_bloom)
					bloom_light_list.add(temp);
			}
		}
		
		// setup events
		for (int i = event_list.size() - 1; i >= 0; i--)
		{
			event_list.get(i).onInitialize();
			if (event_list.get(i).this_event == EnumLevelEvent.send_to_start)
				LevelEventTransportPlayer.class.cast(event_list.get(i).my_possible_event).setTransportTo(x_player, y_player);
			else if(event_list.get(i).this_event == EnumLevelEvent.invisible_wall)
			{
				LevelEvent original = event_list.get(i);
				
				LevelObject temp = new LevelObject();
				temp.active = false;
				temp.degree = 0;
				temp.scale = 1;
				temp.this_object = EnumLevelObject.transparent;
				temp.id = original.id;
				temp.x_pos = original.x_pos;
				temp.y_pos = original.y_pos;
				temp.width = original.width;
				temp.height = original.height;
				temp.z_plane = 5;
				
				temp.onInitialize();
				
				object_list.add(temp);
				event_list.remove(i);
			}
		}
		
		// setup player
		player.quad_object = new com.kobaj.opengldrawable.Quad.QuadColorShape(0, 64, 64, 0, Color.GRAY, 0);
		player.quad_object.z_pos -= (5 /* player.z_plane */* Constants.z_modifier);
		player.quad_object.setPos(x_player, y_player, player.draw_from);
		
		// set widths and heights for the camera
		left_shader_limit = (Functions.screenXToShaderX(left_limit) + Constants.ratio);
		right_shader_limit = (Functions.screenXToShaderX(right_limit) - Constants.ratio);
		
		top_shader_limit = Functions.screenYToShaderY(top_limit) - Constants.shader_height / 2.0;
		bottom_shader_limit = Functions.screenYToShaderY(bottom_limit) + Constants.shader_height / 2.0;
	}
	
	public void onUpdate(double delta)
	{
		for (int i = light_list.size() - 1; i >= 0; i--)
			light_list.get(i).onUpdate(delta);
		
		for (int i = event_list.size() - 1; i >= 0; i--)
			event_list.get(i).onUpdate(delta, player.quad_object);
	}
	
	public void onDrawObject()
	{
		//backdrop
		if(backdrop_color != Color.TRANSPARENT)
			my_backdrop.onDrawAmbient(Constants.identity_matrix, Constants.my_proj_matrix, Color.WHITE, true);
		
		//draw sorted
		for (int i = object_list.size() - 1; i >= 0; i--)
			object_list.get(i).onDrawObject();
		
		// player
		player.quad_object.onDrawAmbient();
		
		// bloom lights
		for (int i = bloom_light_list.size() - 1; i >= 0; i--)
			bloom_light_list.get(i).onDrawObject();
	}
	
	public void onDrawLight()
	{
		for (int i = light_list.size() - 1; i >= 0; i--)
			light_list.get(i).onDrawLight();
	}
	
	public void onDrawConstant()
	{
		for (int i = event_list.size() - 1; i >= 0; i--)
			event_list.get(i).onDraw();
	}
	
	// this method will be deleted.
	public void writeOut()
	{
		player = new LevelObject();
		player.this_object = EnumLevelObject.test;
		player.x_pos = 0;
		player.y_pos = 100;
		player.z_plane = 5;
		player.active = true;
		
		// initialize everything
		object_list = new ArrayList<LevelObject>();
		light_list = new ArrayList<LevelAmbientLight>();
		event_list = new ArrayList<LevelEvent>();
		
		// make everything
		LevelObject temp = new LevelObject();
		temp.this_object = EnumLevelObject.test;
		temp.x_pos = 200;
		temp.y_pos = 200;
		temp.z_plane = 5;
		temp.active = true;
		
		LevelSpotLight templ = new LevelSpotLight();
		templ.is_bloom = false;
		templ.radius = 100;
		templ.color = Color.WHITE;
		templ.degree = 0;
		templ.x_pos = 0;
		templ.y_pos = 0;
		templ.blur_amount = 0;
		templ.close_width = 10;
		templ.far_width = 100;
		templ.active = true;
		
		LevelPointLight templ2 = new LevelPointLight();
		templ2.is_bloom = false;
		templ2.radius = 100;
		templ2.color = Color.WHITE;
		templ2.x_pos = 0;
		templ2.y_pos = 0;
		templ2.blur_amount = 0;
		templ2.active = true;
		
		LevelAmbientLight templ3 = new LevelAmbientLight();
		templ3.color = Color.WHITE;
		templ3.active = true;
		
		LevelEvent tempe = new LevelEvent();
		tempe.height = 200;
		tempe.width = 600;
		tempe.affected_object_strings = new ArrayList<String>();
		tempe.affected_object_strings.add("empty");
		tempe.x_pos = 0;
		tempe.y_pos = 0;
		
		// fill everything in
		object_list.add(temp);
		light_list.add(templ);
		light_list.add(templ2);
		light_list.add(templ3);
		event_list.add(tempe);
	}
}
