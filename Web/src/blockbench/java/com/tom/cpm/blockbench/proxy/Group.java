package com.tom.cpm.blockbench.proxy;

import com.tom.cpm.blockbench.format.GroupData;
import com.tom.cpm.blockbench.proxy.Vectors.JsVec3;
import com.tom.ugwt.client.JsArrayE;

import elemental2.core.JsArray;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Group_$$")
public class Group extends OutlinerNode {

	@JsOverlay
	public static final Group ROOT = Js.uncheckedCast("root");

	public static Group selected;
	public static Group[] all;

	public boolean isOpen;
	public String name;
	public JsVec3 origin;
	public JsVec3 rotation;
	public Group parent;
	public boolean is_rotation_subgroup, export, visibility, is_catch_bone;
	public JsArrayE<OutlinerNode> children;

	@JsProperty(name = "cpm_dva")
	public boolean disableVanillaAnim;

	@JsProperty(name = "cpm_copy_transform")
	public String copyTransform;

	@JsProperty(name = "cpm_hidden")
	public boolean hidden;

	public Group(GroupProperties ctr) {}

	@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "$$ugwt_m_Object_$$")
	public static class GroupProperties {
		public String name;
		public JsVec3 origin, rotation;
	}

	public native void extend(GroupProperties ctr);
	public native void createUniqueName(JsArray<Group> all);
	public native void addTo(Group gr);
	public native Group init();
	public native void resolve();

	@JsProperty(name = "cpm_dataCache")
	private GroupData data;

	@JsOverlay
	public final GroupData getData() {
		if(data == null)data = new GroupData(this);
		return data;
	}
}
