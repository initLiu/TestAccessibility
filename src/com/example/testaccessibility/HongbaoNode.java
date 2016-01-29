package com.example.testaccessibility;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

public class HongbaoNode {
	private String contentDes;
	private AccessibilityNodeInfo nodeInfo;
	private boolean hasEnter;
	private static HongbaoNode mInstance;

	private static Map<String, HongbaoNode> hongBaoNodes = new HashMap<String, HongbaoNode>();

	private HongbaoNode(String contentDes, AccessibilityNodeInfo nodeInfo) {
		this.contentDes = contentDes;
		this.nodeInfo = nodeInfo;
		hasEnter = false;
	}

	private HongbaoNode() {
	}

	public static HongbaoNode getInstace() {
		if (mInstance == null) {
			mInstance = new HongbaoNode();
		}
		return mInstance;
	}

	public void addHongbaoNode(AccessibilityNodeInfo node) {
		try {
			String des = node.getContentDescription().toString();
			String key = des.substring(0, des.indexOf(","));
			if (hongBaoNodes.containsKey(key)) {
				HongbaoNode hNode = hongBaoNodes.get(key);
				if (hNode.contentDes.equals(des) && hNode.hasEnter) {
					return;
				}
			}
			hongBaoNodes.put(key, new HongbaoNode(des, node));
		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.e("Test", "[HongbaoNode] addHongbaoNode hongbaoNodes.size="
				+ hongBaoNodes.size());
	}

	public HongbaoNode getHongbaoNode(String key) {
		if (hongBaoNodes.containsKey(key)) {
			if (!hongBaoNodes.get(key).hasEnter) {
				return hongBaoNodes.get(key);
			}
		}
		return null;
	}

	public void removeDirtyHongbaoNode() {
		Object[] keys = hongBaoNodes.keySet().toArray();
		for(int i=0;i<keys.length;i++){
			if(hongBaoNodes.get(keys[i]).hasEnter){
				hongBaoNodes.remove(keys[i]);
			}
		}
		Log.e("Test", "[HongbaoNode] removeDirtyHongbaoNode hongbaoNodes.size="
				+ hongBaoNodes.size());
	}

	public String hasValidHongbaoNode() {
		if (!hongBaoNodes.isEmpty()) {
			for (Entry<String, HongbaoNode> entry : hongBaoNodes.entrySet()) {
				if (!entry.getValue().hasEnter) {
					return entry.getKey();
				}
			}
		}
		return null;
	}

	public AccessibilityNodeInfo getHongbaoNodeInfo() {
		return nodeInfo;
	}

	public void setEnter(String key) {
		if (hongBaoNodes.containsKey(key)) {
			hongBaoNodes.get(key).hasEnter = true;
		}
	}
}
