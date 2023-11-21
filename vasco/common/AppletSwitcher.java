package vasco.common;

import javax.swing.JApplet; // import java.applet.Applet;

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.awt.Dimension;
// import java.applet.AppletContext;
// import java.applet.AppletStub;
// import java.awt.Dimension;
import java.net.URL;
import java.util.TreeMap;

public class AppletSwitcher extends Thread implements AppletStub {
	JApplet app;
	Class<?> appToLoad; // Class appToLoad;
	boolean active=false;
	public JApplet realApplet;
	TreeMap<String,String> params=new TreeMap<>(); // TreeMap params=new TreeMap();
	
	public Applet getNewApplet() {
		return realApplet;
	}
	
	public AppletContext getAppletContext()
	{
		return app.getAppletContext();
	}
	
	public String getParameter(String s)
	{
		return params.get(s); // return (String)params.get(s);
	}
	
	public void appletResize(int height,int width)
	{
		app.resize(height,width);
		if(realApplet!=null)realApplet.resize(height,width);
	}
	
	public URL getCodeBase()
	{
		return app.getCodeBase();
	}
	
	public URL getDocumentBase()
	{
		return app.getDocumentBase();
	}
	
	
	public AppletSwitcher(JApplet app, Class<?> appToLoad,String[] paramNames,String[] params) // Class appToLoad,String[] paramNames,String[] params)
	{
		this.app=app;
		this.appToLoad=appToLoad;
		
		for(int i=0;i<paramNames.length && i<params.length;i++)
		{
			this.params.put(paramNames[i],params[i]);
		}
		
		try {	
		realApplet = (JApplet)appToLoad.newInstance();
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
		  //System.out.println( e );
		  }
	} 
	
	public boolean isActive()
	{
		if(active)return realApplet.isActive();
		return active;
	}
	
	public void run() {
	
	  Dimension s=app.getSize();
	  active=true;
	  realApplet.setStub(this);
	  app.removeAll();
	  //new Dialog(new Frame()).add(realApplet);
	  app.add(realApplet);
	  realApplet.setSize(s);
	  realApplet.init();
	  realApplet.start();
	  
	  
	  app.validate();
	}
}
