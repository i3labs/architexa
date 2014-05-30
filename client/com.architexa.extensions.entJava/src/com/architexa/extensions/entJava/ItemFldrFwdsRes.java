package com.architexa.extensions.entJava;

import java.util.List;

import org.openrdf.model.Resource;

public class ItemFldrFwdsRes {
	public Resource itemRes;
	public Resource fldrRes;
	public List<Resource> forwards;
	public ItemFldrFwdsRes(Resource _itemRes, Resource _fldrRes, List<Resource>_forwards) {
		this.itemRes = _itemRes;
		this.fldrRes = _fldrRes;
		this.forwards = _forwards;
	}
}
