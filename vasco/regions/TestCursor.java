package vasco.regions;

import java.awt.Color;
import java.awt.Rectangle;

import vasco.common.DrawingTarget;

public class TestCursor implements CursorStyleInterface {
	protected Color color;
	protected Rectangle rect;

	public TestCursor(Rectangle rect, Color color) {
		this.rect = rect;
		this.color = color;
	}

	@Override
	public void display(DrawingTarget dt) {
		// dt.setColor(color);
		dt.directFillRect(color, rect.x, rect.y, rect.width, rect.height);
	}

	@Override
	public boolean equals(Object obj) {
		return false;
	}
}
