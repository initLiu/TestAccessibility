package com.example.testaccessibility;

import java.util.List;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityManager.AccessibilityStateChangeListener;
import android.widget.Button;

public class MainActivity extends Activity {

	private Button btn;
	private boolean mServiceEnable = false;
	private FloatView floatView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);
		btn = (Button) findViewById(R.id.hongbao);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(
						Settings.ACTION_ACCESSIBILITY_SETTINGS);
				startActivity(intent);
			}
		});
	}

	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
		if (floatView == null) {
			WindowManager manager = (WindowManager) getApplicationContext()
					.getSystemService(WINDOW_SERVICE);
			LayoutParams lp = new LayoutParams();
			lp.width = 200;
			lp.height = 200;
			lp.type = LayoutParams.TYPE_SYSTEM_ALERT;
			lp.flags = LayoutParams.FLAG_KEEP_SCREEN_ON
					| LayoutParams.FLAG_DISMISS_KEYGUARD
					| LayoutParams.FLAG_NOT_FOCUSABLE;
			lp.format = PixelFormat.RGBA_8888;
			lp.x = 0;
			lp.y = 0;
			lp.gravity = Gravity.LEFT | Gravity.TOP;

			floatView = new FloatView(this);
			floatView.setBackgroundResource(R.drawable.hongbao);
			floatView.setAlpha(0.8f);
			floatView.setWindowLayoutParams(lp);
			manager.addView(floatView, lp);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (floatView != null) {
			WindowManager manager = (WindowManager) getApplicationContext()
					.getSystemService(WINDOW_SERVICE);
			manager.removeView(floatView);
			floatView = null;
		}
		mServiceEnable = false;
		AccessibilityManager manager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
		List<AccessibilityServiceInfo> infos = manager
				.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
		for (AccessibilityServiceInfo info : infos) {
			if (info.getId().equals(
					getPackageName() + "/.MyAccessibilityService")) {
				mServiceEnable = true;
			}
		}
		if (mServiceEnable) {
			btn.setText(getString(R.string.hongbao_opened));
		} else {
			btn.setText(getString(R.string.hongbao_closed));
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		String str = intent.getExtras().getString("name");
		if (str != null && str.equals("unbind")) {
			btn.setText(getString(R.string.hongbao_closed));
		}
	}
}
