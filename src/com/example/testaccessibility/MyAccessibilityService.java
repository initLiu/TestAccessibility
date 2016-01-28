package com.example.testaccessibility;

import java.util.ArrayList;
import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class MyAccessibilityService extends AccessibilityService {

	private AccessibilityNodeInfo rootNodeInfo;

	private boolean mLuckyMoneyReceived = false;
	private AccessibilityNodeInfo mReceivedNode = null;

	private AccessibilityNodeInfo mUnpackNode = null;
	private boolean mNeedUnpack = false;

	private boolean mLuckyMoneyPicked = false;
	private boolean mNeedBack = false;

	private HongbaoMsg mHongbaoMsg = new HongbaoMsg();

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		Log.e("Test", "onAccessibilityEvent");
		if (mHongbaoMsg == null) {
			return;
		}
		rootNodeInfo = event.getSource();

		if (rootNodeInfo == null) {
			return;
		}
		mReceivedNode = null;
		mUnpackNode = null;

		checkNodeInfo();

		// 收到红包还没点开
		if (mLuckyMoneyReceived && !mLuckyMoneyPicked && mReceivedNode != null) {
			mReceivedNode.getParent().performAction(
					AccessibilityNodeInfo.ACTION_CLICK);
			mLuckyMoneyReceived = false;
			mLuckyMoneyPicked = true;
		}

		// 点开红包还没领取
		if (mNeedUnpack && mUnpackNode != null) {
			mUnpackNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
			mNeedUnpack = false;
		}

		if (mNeedBack) {
			performGlobalAction(GLOBAL_ACTION_BACK);
			mNeedBack = false;
		}
	}

	private void checkNodeInfo() {
		if (rootNodeInfo == null) {
			return;
		}

		// 收到红包还没有点开
		List<AccessibilityNodeInfo> nodeInfos = findAccessibilityNodeInfosByText(
				rootNodeInfo, new String[] { "领取红包" });

		if (!nodeInfos.isEmpty()) {
			AccessibilityNodeInfo targetNode = nodeInfos
					.get(nodeInfos.size() - 1);
			if (mHongbaoMsg.generateHongbaoMsg(targetNode)) {
				mLuckyMoneyReceived = true;
				mReceivedNode = targetNode;
			}
			return;
		}

		// 红包已经点开，还没有领取钱
		AccessibilityNodeInfo node2 = rootNodeInfo.getChildCount() > 3 ? rootNodeInfo
				.getChild(3) : null;
		if (node2 != null
				&& node2.getClassName().equals("android.widget.Button")) {
			mUnpackNode = node2;
			mNeedUnpack = true;
			return;
		}

		if (mLuckyMoneyPicked) {
			List<AccessibilityNodeInfo> node3 = findAccessibilityNodeInfosByText(
					rootNodeInfo, new String[] { "手慢了", "红包详情", "红包已失效" });
			if (!node3.isEmpty()) {
				mLuckyMoneyPicked = false;
				mNeedBack = true;
			}
		}
	}

	private List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText(
			AccessibilityNodeInfo root, String[] texts) {
		for (String text : texts) {
			if (text == null)
				continue;

			List<AccessibilityNodeInfo> nodes = root
					.findAccessibilityNodeInfosByText(text);
			if (!nodes.isEmpty()) {
				return nodes;
			}
		}
		return new ArrayList<>();
	}

	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub
		Log.e("Test", "onInterrupt");
	}

	@Override
	protected void onServiceConnected() {
		// TODO Auto-generated method stub
		super.onServiceConnected();
		Log.e("Test", "onServiceConnected");
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		Log.e("Test", "onUnbind");
		return super.onUnbind(intent);
	}

}
