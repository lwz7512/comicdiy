/**
 * 
 */
package com.ybcx.comic.facade;

/**
 * 这里定义外部访问接口需要的常量字符串
 * @author lwz
 *
 */
public interface ExtVisitorInterface {
	
	//获取全部可用素材
	public static final String GETALLASSETS  = "getAllAssets";
	
	//根据标签搜索素材
	public static final String SEARCHBYLABEL = "searchByLabel";
	
	//上传一个素材
	public static final String CREATEASSET = "createAsset";
	
	//获取一个素材
	public static final String GETASSETBYID = "getAssetById";
	
	//删除一个素材
	public static final String DELETEASSETBYID = "deleteAssetById";
	
	//更新素材
	public static final String UPDATEASSETBYID = "updateAssetById";

	//获取所有分类
	public static final String GETALLCATEGORY = "getAllCategory";
    
	//自定义创建分类
	public static final String CREATECATEGORY = "createCategory";
	
	//获取素材
	public static final String GETASSETFILE ="getAssetFile";
	
	//获取素材缩略图
	public static final String GETTHUMBNAIL ="getThumbnail";
	
	//新建标签
	public static final String CREATELABEL = "createLabel";
	
	//删除某一标签
	public static final String DELETELABEL = "deleteLabel";
	
	//根据父标签删除其下所有子标签
	public static final String DELETELABELBYPARENT = "deleteLabelByParent";
	
	//获取所有父级标签
	public static final String GETALLPARENTLABEL = "getAllParentLabel";
	
	//根据父标签获取子标签
	public static final String GETLABELBYPARENT = "getLabelByParent";
	
	
	
	//-----------------------------与前台连通测试
	
	//素材接入
	public static final String GETSYSASSETSBY = "getSysAssetsBy";
	
	//动画载入播放
	public static final String GETANIMATIONBY = "getAnimationBy";
	
	//某用户的所胡diy动画
	public static final String GETANIMATIONSOF = "getAnimationsOf";
	
	//上传本地图片
	public static final String UPLOADLOCALIMAGE = "uploadLocalImage";
	
	//动画保存
	public static final String SAVEANIM = "saveAnim";
    
    //--------接微博相关方法
    
    //根据code获取access token
    public static final String GETACCESSTOKENBYCODE = "getAccessTokenByCode";
    
    //转发到微博
    public static final String FORWARDTOWEIBO = "forwardToWeibo";
    
    //完善资料(使用weibo账户登录，完善账号密码)
    public static final String IMPROVEWEIBOUSER = "improveWeiboUser";
    
    
}