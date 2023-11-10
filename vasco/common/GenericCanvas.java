package vasco.common;

import java.awt.Panel;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public abstract class GenericCanvas extends GeneralCanvas implements RebuildTree, MouseListener, MouseMotionListener {
	public GenericCanvas(DRectangle can, DrawingTarget dt, DrawingTarget overview, Panel m, TopInterface ti) {
		super(can, dt, overview, m, ti);
	}

	public abstract void initStructs();
}
