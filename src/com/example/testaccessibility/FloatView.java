package com.example.testaccessibility;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

public class FloatView extends View {
	private LayoutParams params;
	private WindowManager windowManager;
	private float lastX, lastY, curX, curY, mTouchStartX, mTouchStartY;
	long[] mClicks = new long[2];

	public FloatView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		windowManager = (WindowManager) getContext().getApplicationContext()
				.getSystemService(Context.WINDOW_SERVICE);
	}

	public FloatView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FloatView(Context context) {
		this(context, null);
	}

	public void setWindowLayoutParams(LayoutParams params) {
		this.params = params;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (params == null) {
			return false;
		}
		Rect outRect = new Rect();
		getWindowVisibleDisplayFrame(outRect);
		int statusBarHeight = outRect.top;

		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			lastX = event.getRawX();
			lastY = event.getRawY() - statusBarHeight;

			curX = event.getRawX();
			curY = event.getRawY() - statusBarHeight;

			mTouchStartX = event.getX();
			mTouchStartY = event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			curX = event.getRawX();
			curY = event.getRawY() - statusBarHeight;
			updateFloatViewPosition();
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (curX == lastX && curY == lastY) {
				// click
				System.arraycopy(mClicks, 1, mClicks, 0, mClicks.length - 1);
				mClicks[mClicks.length - 1] = SystemClock.uptimeMillis();
				if (mClicks[0] >= (SystemClock.uptimeMillis() - 500)) {
					moveTaskFront();
				}
			}
			mTouchStartX = mTouchStartY = 0;
			break;
		default:
			break;
		}
		return true;
	}

	private void moveTaskFront() {
		int taskId = -1;
		ActivityManager activityManager = (ActivityManager) getContext()
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> taskInfos = activityManager.getRunningTasks(10);
		for (RunningTaskInfo taskInfo : taskInfos) {
			if (taskInfo.topActivity.getPackageName().equals(
					getContext().getPackageName())) {
				taskId = taskInfo.id;
			}
		}
		if (taskId != -1) {
			activityManager.moveTaskToFront(taskId,
					ActivityManager.MOVE_TASK_WITH_HOME);
		}
	}

	private void updateFloatViewPosition() {
		params.x = (int) (curX - mTouchStartX);
		params.y = (int) (curY - mTouchStartY);
		windowManager.updateViewLayout(this, params);
	}
}
