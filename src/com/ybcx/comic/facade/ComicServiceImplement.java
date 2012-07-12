package com.ybcx.comic.facade;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.util.Streams;

import com.sun.image.codec.jpeg.ImageFormatException;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.ybcx.comic.beans.Assets;
import com.ybcx.comic.beans.Cartoon;
import com.ybcx.comic.beans.Category;
import com.ybcx.comic.beans.Label;
import com.ybcx.comic.dao.DBAccessInterface;
import com.ybcx.comic.tools.ImageHelper;
import com.ybcx.comic.utils.ComicUtils;

@SuppressWarnings("restriction")
public class ComicServiceImplement implements ComicServiceInterface {


	// 由Spring注入
	private DBAccessInterface dbVisitor;
	
	private Properties systemConfigurer;

	public void setSystemConfigurer(Properties systemConfigurer) {
		this.systemConfigurer = systemConfigurer;
	}
	
	public void setDbVisitor(DBAccessInterface dbVisitor) {
		this.dbVisitor = dbVisitor;
	}

	//upload
	private String imagePath;
	
	@Override
	public void saveImagePath(String filePath) {
	//	this.imgProcessor.setImagePath(filePath);
		imagePath = filePath;
	}
	
	// 设定输出的类型
	private static final String GIF = "image/gif;charset=UTF-8";

	private static final String JPG = "image/jpeg;charset=UTF-8";

	private static final String PNG = "image/png;charset=UTF-8";
	
	private static final String SWF = "application/x-shockwave-flash;charset=UTF-8";

	@Override
	public List<Assets> getAllAssets() {
		List<Assets> assetsList = dbVisitor.getAllAssets();
		
		List<Assets> resultList = new ArrayList<Assets>();
		
		resultList = this.combinLabels(assetsList);
		
		return resultList;
	}
	
	//给素材取标签
	private List<Assets> combinLabels(List<Assets> assetList){
		List<Assets> resultList = new ArrayList<Assets>();
		for(int i=0 ; i<assetList.size() ; i++){
			Assets ast = assetList.get(i);
			String id = ast.getId();
			String labels = this.getLabelsById(id);
			ast.setLabel(labels);
			resultList.add(ast);
		}
		return resultList;
	}
	
	private String getLabelsById(String assetId) {
		StringBuffer labels = new StringBuffer();
		List<Label> labelList = dbVisitor.getAssetLabelsById(assetId);
		if (labelList.size() > 0) {
			for (int i = 0; i < labelList.size(); i++) {
				Label label = labelList.get(i);
				if (labels.length() > 0) {
					labels.append(" ");
				}
				labels.append(label.getName());
			}
		}
		return labels.toString().trim();
	}
	
	@Override
	public String createAsset(String name, String type, String price,
			String category, String label, String holiday, String assetPath,
			String thumbnailPath) {
		boolean flag = false;
		//新建素材，首先插入asset表，astcat_rel, astlab_rel, 更新category和label表的热度
		String assetId = ComicUtils.generateUID();
		
		Assets asset = this.generateAsset(assetId,name,type,price,holiday,assetPath,thumbnailPath);
		int insAsset = dbVisitor.createAsset(asset);
		
		int insCatRel = dbVisitor.createAstcatRel(ComicUtils.generateUID(),assetId,category);
		int updCatgory = dbVisitor.updateCategoryHeat(category);
		
		//这里前台传来的label是以逗号分隔的id，最多三个
		String[] labelArray =label.split(",");
		
		int insLabRel = this.createLabRel(assetId,labelArray);
		int updLabel = this.updateLabelHeat(labelArray);
		
		if(insAsset>0 && insCatRel>0  && updCatgory>0 && insLabRel>0  && updLabel>0){
			flag = true;
		}
		
		return String.valueOf(flag);
	}
	
	private int updateLabelHeat(String[] labelArray) {
		int rows = 0;
		//在这里将用空格分隔的labels转变成sql可识别的'','
		StringBuffer labelIds = new StringBuffer();
		if(labelArray.length > 0){
			 for (int i = 0; i < labelArray.length; i++) {
					if (!"".equals(labelArray[i].trim())) {
						if (labelIds.length() > 0) {
							labelIds.append(",");
						}
						labelIds.append("'");
						labelIds.append(labelArray[i]);
						labelIds.append("'");
					}
				}
			 
			 rows = dbVisitor.updateLabelHeat(labelIds.toString());
		}
		return rows;
	}

	private int createLabRel(String assetId, String[] labelArray) {
		int rows = 0;
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		if(labelArray.length > 0){
			 for (int i = 0; i < labelArray.length; i++) {
				Map<String, String>  labelAsset = new HashMap<String,String>();
				 String labelId = labelArray[i];
				 labelAsset.put(labelId, assetId);
				 list.add(labelAsset);
			 }
			 rows = dbVisitor.createLabRell(list);
		}
		return rows;
	}


	private Assets generateAsset(String assetId, String name, String type, String price,String holiday, String assetPath,
			String thumbnailPath) {
		Assets asset = new Assets();
		asset.setId(assetId);
		asset.setName(name);
		asset.setType(type);
		asset.setPrice(Float.parseFloat(price));
		asset.setHoliday(holiday);
		asset.setPath(assetPath);
		asset.setThumbnail(thumbnailPath);
		asset.setUploadTime(ComicUtils.getFormatNowTime());
		asset.setHeat(0);
		asset.setEnable(1);
		return asset;
	}

	@Override
	public Assets getAssetById(String assetId) {
		Assets asset = dbVisitor.getAssetById(assetId);
		return asset;
	}
	
	@Override
	public String deleteAssetById(String assetId) {
		boolean flag = true;
		int rows = dbVisitor.deleteAssetById(assetId);
		if(rows < 1){
			flag = false;
		}
		return String.valueOf(flag);
	}
	
	@Override
	public String updateAssetById(String assetId, String name, String price, String holiday) {
		boolean flag = true;
		int rows = dbVisitor.updateAssetById(assetId,name,price,holiday);
		if(rows < 1){
			flag = false;
		}
		return String.valueOf(flag);
	}

	@Override
	public List<Assets> searchByLabel(String labels) {
		List<Assets> resList = new ArrayList<Assets>();
		List<Assets> andList = new ArrayList<Assets>();
		List<Assets> orList = new ArrayList<Assets>();
		 String[] labelArr =labels.split(" ");
		 StringBuffer labelOr = new StringBuffer();
		 //在这里将用空格分隔的labels转变成sql可识别的'','
		 if(labelArr.length > 0){
			 for (int i = 0; i < labelArr.length; i++) {
					if (!"".equals(labelArr[i].trim())) {
						if (labelOr.length() > 0) {
							labelOr.append(",");
						}
						labelOr.append("'");
						labelOr.append(labelArr[i]);
						labelOr.append("'");
					}
				}
			 
			 andList = dbVisitor.searchByLabelAnd(labelArr);
			 orList = dbVisitor.searchByLabelOr(labelOr.toString());
		 }
		 
		 resList = combinResult(andList, orList);
		 
		List<Assets> resultList = new ArrayList<Assets>();
		
		resultList = this.combinLabels(resList);
		
		return resultList;
		 
	}
	
	private List<Assets> combinResult(List<Assets> andList, List<Assets> orList){
		for(int m=0; m<andList.size(); m++){
			Assets asset = andList.get(m);
			String id = asset.getId();
			for(int n=0; n <orList.size(); n++){
				Assets asse = orList.get(n);
				if(id.equals(asse.getId())){
					orList.remove(n);
				}
			}
		}
		andList.addAll(orList);
		return andList;
	}

	@Override
	public List<Category> getAllCategory() {
		List<Category> cateList = dbVisitor.getAllCategory();
		return cateList;
	}

	@Override
	public String createCategory(String name) {
		String id = ComicUtils.generateUID();
		int rows = dbVisitor.createCategory(id,name);
		if(rows<1){
			id = "";
		}
		return id;
	}

	@Override
	public void getThumbnailFile(String relativePath, HttpServletResponse res) {
		try {
			//默认
			File defaultImg = new File(imagePath + File.separator
					+ "asset" + File.separator + File.separator + "default.png");
			InputStream defaultIn = new FileInputStream(defaultImg);
			
			String type = relativePath.substring(relativePath.lastIndexOf(".") + 1);
			File file = new File(imagePath+relativePath);
			
			if (file.exists()) {
				InputStream imageIn = new FileInputStream(file);
				if (type.toLowerCase().equals("jpg") || type.toLowerCase().equals("jpeg")) {
					writeJPGImage(imageIn, res);
				} else if (type.toLowerCase().equals("png")) {
					writePNGImage(imageIn, res);
				} else if (type.toLowerCase().equals("gif")) {
					writeGIFImage(imageIn, res);
				} else {
					writePNGImage(defaultIn, res);
				}
			} else {
				writePNGImage(defaultIn, res);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void writeJPGImage(InputStream imageIn, HttpServletResponse res) {
		try {
			res.setContentType(JPG);
			JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(imageIn);
			// 得到编码后的图片对象
			BufferedImage image = decoder.decodeAsBufferedImage();
			// 得到输出的编码器
			OutputStream out = res.getOutputStream();
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
			encoder.encode(image);
			// 对图片进行输出编码
			imageIn.close();
			// 关闭文件流
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ImageFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writePNGImage(InputStream imageIn, HttpServletResponse res) {
		res.setContentType(PNG);
		getOutInfo(imageIn, res);
	}

	private void writeGIFImage(InputStream imageIn, HttpServletResponse res) {
		res.setContentType(GIF);
		getOutInfo(imageIn, res);
	}

	private void getOutInfo(InputStream imageIn, HttpServletResponse res) {
		try {
			OutputStream out = res.getOutputStream();
			BufferedInputStream bis = new BufferedInputStream(imageIn);
			// 输入缓冲流
			BufferedOutputStream bos = new BufferedOutputStream(out);
			// 输出缓冲流
			byte data[] = new byte[4096];
			// 缓冲字节数
			int size = 0;
			size = bis.read(data);
			while (size != -1) {
				bos.write(data, 0, size);
				size = bis.read(data);
			}
			bis.close();
			bos.flush();
			// 清空输出缓冲流
			bos.close();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	private void writeSWF(InputStream imageIn, HttpServletResponse res) {
		res.setContentType(SWF);
		getOutInfo(imageIn, res);
	}

	@Override
	public void getAssetFile(String relativePath, HttpServletResponse res) {
		try {
			//默认
			File defaultImg = new File(imagePath + File.separator
					+ "asset" + File.separator + File.separator + "default.png");
			InputStream defaultIn = new FileInputStream(defaultImg);
			
			String type = relativePath.substring(relativePath.lastIndexOf(".") + 1);
			File file = new File(imagePath+relativePath);
			
			if (file.exists()) {
				InputStream imageIn = new FileInputStream(file);
				if(type.toLowerCase().equals("swf")){
					writeSWF(imageIn,res);
				}else if (type.toLowerCase().equals("jpg") || type.toLowerCase().equals("jpeg")) {
					writeJPGImage(imageIn, res);
				} else if (type.toLowerCase().equals("png")) {
					writePNGImage(imageIn, res);
				} else if (type.toLowerCase().equals("gif")) {
					writeGIFImage(imageIn, res);
				} else {
					writePNGImage(defaultIn, res);
				}
			} else {
				writePNGImage(defaultIn, res);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<Label> getAllParentLabel() {
		List<Label> resList = dbVisitor.getAllParentLabel();
		return resList;
	}

	@Override
	public List<Label> getLabelByParent(String parentId) {
		List<Label> resList = dbVisitor.getLabelByParent(parentId);
		return resList;
	}


	@Override
	public String createLabel(String name, String parent) {
		boolean flag = true;
		String id = ComicUtils.generateUID();
		int rows = dbVisitor.createLabel(id,name,parent);
		if(rows<1){
			flag = false;
		}
		return String.valueOf(flag);
	}

	@Override
	public String deleteLabel(String labelId) {
		boolean flag = false;
		int rows = dbVisitor.deleteLabel(labelId);
		if(rows>0){
			flag = true;
		}
		return String.valueOf(flag);
	}

	@Override
	public String deleteLabelByParent(String parentId) {
		boolean flag = false;
		//删除父标签时，将其下子标签也删掉
		List<Label> list = dbVisitor.getLabelByParent(parentId);
		if(list.size()>0){
			int rows = dbVisitor.deleteLabelByParent(parentId);
			if(rows>0){
				flag = true;
			}
		}
		return String.valueOf(flag);
	}

	@Override
	public List<Assets> getSysAssetsBy(String type, int page) {
		int pageSize = Integer.parseInt(systemConfigurer.getProperty("pageSize"));
		List<Assets> assetList = dbVisitor.getAsssetsByType(type,page,pageSize);
		
		List<Assets> resultList = new ArrayList<Assets>();
		resultList = this.combinLabels(assetList);
		
		return resultList;
	}

	@Override
	public Cartoon getAnimationBy(String userId, String animId) {
		Cartoon cartoon = dbVisitor.getAnimationBy(userId,animId);
		return cartoon;
	}

	@Override
	public List<Cartoon> getAnimationsOf(String userId) {
		List<Cartoon> cartoonList = dbVisitor.getAnimationsOf(userId);
		return cartoonList;
	}

	@Override
	public String createLocalImage(FileItem imgData) {
		String fileName = imgData.getName();

		String path = imagePath + File.separator + "asset" + File.separator + fileName;
		try {
			BufferedInputStream in = new BufferedInputStream(imgData.getInputStream());
			// 获得文件输入流
			BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(new File(path)));// 获得文件输出流
			Streams.copy(in, outStream, true);// 开始把文件写到你指定的上传文件夹
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//上传成功
		if (new File(path).exists()) {
			return path;
		}
	
		return "false";
	}

	@Override
	public String createAnimation(FileItem shotData, String userId, String name,
			String content) {
		boolean flag = true;
		
		String thumbnail = this.saveThumbnailOf(shotData);
		
		Cartoon cartoon = generateCartoon(userId,name,content,thumbnail);
		
		int rows = dbVisitor.saveAnimation(cartoon);
		if(rows < 1){
			flag = false;
		}
		return String.valueOf(flag);
	}
	
	private Cartoon generateCartoon(String userId, String name, String content,
			String thumbnail){
		Cartoon cartoon = new Cartoon();
		cartoon.setId(ComicUtils.generateUID());
		cartoon.setContent(content);
		cartoon.setOwner(userId);
		cartoon.setName(name);
		cartoon.setThumbnail(thumbnail);
		cartoon.setCreateTime( ComicUtils.getFormatNowTime());
		return cartoon;
	}

	//将diy成品的截屏缩小并保存
	private String saveThumbnailOf(FileItem imgData) {
		String fileName = imgData.getName();
		String filePath = imagePath + File.separator + "asset" + File.separator + fileName;
		try {
			ImageHelper.handleImage(imgData, 100, 100, filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return filePath;
	}




}