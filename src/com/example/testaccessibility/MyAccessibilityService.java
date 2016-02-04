package com.example.testaccessibility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Vibrator;
import android.provider.Settings;
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

	private boolean mIsInMsgPage = false;
	private boolean mNeedBackToRoot = false;

	private HongbaoMsg mHongbaoMsg = new HongbaoMsg();

	private HongbaoNode mHongbaoNode = HongbaoNode.getInstace();

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

		// printfNode(rootNodeInfo);
		checkNodeInfo();

		// 消息页收到红包消息
		String validKey = null;
		if ((validKey = mHongbaoNode.hasValidHongbaoNode()) != null) {
			Log.e("Test", "消息页收到红包消息");
			HongbaoNode hNode = mHongbaoNode.getHongbaoNode(validKey);
			hNode.setEnter(validKey);
			AccessibilityNodeInfo node = hNode.getHongbaoNodeInfo();
			node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
		}

		// 收到红包还没点开
		if (mLuckyMoneyReceived && !mLuckyMoneyPicked && mReceivedNode != null) {
			Log.e("Test", "收到红包还没点开");
			mReceivedNode.getParent().performAction(
					AccessibilityNodeInfo.ACTION_CLICK);
			mIsInMsgPage = true;
			mLuckyMoneyReceived = false;
			mLuckyMoneyPicked = true;
		}

		// 点开红包还没领取
		if (mNeedUnpack && mUnpackNode != null) {
			Log.e("Test", "点开红包还没领取");
			mUnpackNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
			mNeedUnpack = false;
		}

		if (mNeedBack) {
			Log.e("Test", "返回");
			performGlobalAction(GLOBAL_ACTION_BACK);
			mNeedBack = false;
		}

		if (mNeedBackToRoot) {
			Log.e("Test", "返回消息页");
			performGlobalAction(GLOBAL_ACTION_BACK);
			mNeedBackToRoot = false;
			mIsInMsgPage = false;
		}
		List<AccessibilityNodeInfo> node4 = rootNodeInfo
				.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/cer");
		AccessibilityNodeInfo bakNode = null;
		if (!node4.isEmpty()) {
			bakNode = node4.get(node4.size() - 1);
			if (bakNode.getContentDescription().equals("返回")
					&& bakNode.getClassName()
							.equals("android.widget.ImageView")) {
				Log.e("Test", "返回消息页");
				performGlobalAction(GLOBAL_ACTION_BACK);
				mNeedBackToRoot = false;
				mIsInMsgPage = false;
			}
		}
	}

	private synchronized void checkNodeInfo() {
		if (rootNodeInfo == null) {
			return;
		}

		// 消息页收到红包消息
		List<AccessibilityNodeInfo> node1 = rootNodeInfo
				.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/cd");
		if (!node1.isEmpty()) {
			boolean canRemove = false;
			for (AccessibilityNodeInfo node : node1) {
				if (node.getText().toString().contains("[微信红包]")) {
					Log.e("Test", "checkNodeInfo 消息页收到红包消息 desc="
							+ node.getParent().getContentDescription());
					canRemove = true;
					mHongbaoNode.addHongbaoNode(node.getParent());
				}
			}
			if (canRemove) {
				// mHongbaoNode.removeDirtyHongbaoNode();
			}
			return;
		}

		// 收到红包还没有点开
		List<AccessibilityNodeInfo> nodeInfos = findAccessibilityNodeInfosByText(
				rootNodeInfo, new String[] { "领取红包" });

		if (!nodeInfos.isEmpty()) {
			AccessibilityNodeInfo targetNode = nodeInfos
					.get(nodeInfos.size() - 1);
			if (mHongbaoMsg.generateHongbaoMsg(targetNode)) {
				Log.e("Test", "checkNodeInfo 收到红包还没有点开 与上次不同");
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
			Log.e("Test", "checkNodeInfo 红包已经点开，还没有领取钱");
			mUnpackNode = node2;
			mNeedUnpack = true;
			return;
		}

		if (mLuckyMoneyPicked) {
			List<AccessibilityNodeInfo> node3 = findAccessibilityNodeInfosByText(
					rootNodeInfo, new String[] { "手慢了", "红包详情", "红包已失效" });
			if (!node3.isEmpty()) {
				Log.e("Test", "checkNodeInfo 手慢了");
				mLuckyMoneyPicked = false;
				mNeedBack = true;
			}
			return;
		}

		// 红包领取完，需要返回到消息页
		if (mIsInMsgPage) {
			Log.e("Test", "checkNodeInfo 红包领取完，需要返回到消息页");
			mNeedBackToRoot = true;
		}
	}

	private void printfNode(AccessibilityNodeInfo node) {
		Log.e("Test", "ClassName" + node.getClassName());
		Log.e("Test", "Text" + node.getText());
		Log.e("Test", "Desc" + node.getContentDescription());
		if (node.getChildCount() == 0) {
			return;
		}
		int count = node.getChildCount();
		for (int i = 0; i < count; i++) {
			printfNode(node.getChild(i));
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
		return new ArrayList<AccessibilityNodeInfo>();
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

		Intent intent2 = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
		intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent2);

		return super.onUnbind(intent);
	}

}
