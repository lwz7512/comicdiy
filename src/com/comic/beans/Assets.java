package com.comic.beans;

import org.apache.commons.fileupload.FileItem;

public class Assets {

	private String id;
	private String name;
	private String type;
	private String category;
	private String label;
	private String path;
	private String thumbnail;
	private String uploadTime;
	private int heat;
	private int enable;
	
	private FileItem sourceData;
	
	public FileItem getSourceData() {
		return sourceData;
	}
	public void setSourceData(FileItem sourceData) {
		this.sourceData = sourceData;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getThumbnail() {
		return thumbnail;
	}
	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}
	public String getUploadTime() {
		return uploadTime;
	}
	public void setUploadTime(String uploadTime) {
		this.uploadTime = uploadTime;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getHeat() {
		return heat;
	}
	public void setHeat(int heat) {
		this.heat = heat;
	}
	public int getEnable() {
		return enable;
	}
	public void setEnable(int enable) {
		this.enable = enable;
	}
	
	
}
