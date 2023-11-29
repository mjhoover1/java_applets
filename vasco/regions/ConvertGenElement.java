package vasco.regions;

import vasco.common.DrawingTarget;
import vasco.common.GenElement;

public class ConvertGenElement implements GenElement {
	protected boolean mCopy;

	public ConvertGenElement() {
		mCopy = true;
	}

	public boolean makeCopy() {
		return mCopy;
	}

	@Override
	public void fillElementFirst(DrawingTarget g) {
	}

	@Override
	public void fillElementNext(DrawingTarget g) {
	}

	@Override
	public void drawElementFirst(DrawingTarget g) {
	}

	@Override
	public void drawElementNext(DrawingTarget g) {
	}

	@Override
	public int pauseMode() {
		return 0;
	}

}
