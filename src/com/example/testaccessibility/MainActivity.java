package com.example.testaccessibility;

import java.util.List;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;

public class MainActivity extends Activity {

	private Button btn;
	private boolean mServiceEnable = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
	protected void onResume() {
		super.onResume();
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
}
