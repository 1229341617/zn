package nc.jzmobile.bill.handler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.bs.pub.filesystem.IFileSystemService;
import nc.jzmobile.bill.model.ImageFileModel;
import nc.jzmobile.handler.INCMobileServletHandler;
import nc.jzmobile.utils.JZMobileAppUtils;
import nc.pub.fa.common.util.StringUtils;
import nc.vo.jzmobile.app.Result;
import nc.vo.pub.BusinessException;
import nc.vo.pub.filesystem.NCFileNode;
import sun.misc.BASE64Decoder;

import com.alibaba.fastjson.JSONObject;

public class UploadFileHandler implements INCMobileServletHandler{

	@Override
	public Result handler(Map<String, String> map) throws Exception {
		
		Result result = Result.instance();
		Logger.info("---BillSaveHandler  start---");
		
		String userId = map.get("userid");
		if (userId == null) {
			throw new BusinessException("用户信息不能为空！");
		}
		String billType = map.get("billtype");
		if (billType == null) {
			throw new BusinessException("单据类型不能为空！");
		}
		
		String pk_group = JZMobileAppUtils.getPkGroupByUserId(userId);
		
		InvocationInfoProxy.getInstance().setGroupId(pk_group);
		
		String file = map.get("file");
		String taskId = map.get("taskid");
		if(!StringUtils.isEmpty(file)){
			try{
				uploadImage(file,userId,taskId);
				return result.success().setData("上传成功!!!");
			}catch(Exception e){
				Logger.info("上传附件失败=========="+e.getMessage());
				result.fail().setErrorMessage(e.getMessage());
			}
		}
		
		return result;
		
	}
	
	private void uploadImage(String file,String userid,String pk) throws IOException, BusinessException{
		
		IFileSystemService service = NCLocator.getInstance().lookup(
				IFileSystemService.class);
		
		JSONObject json = JSONObject.parseObject(file);
		ImageFileModel image = (ImageFileModel)
                JSONObject.toJavaObject(json,ImageFileModel.class);
		//获取文件名以及src
        String filename = image.getName();

        String src = image.getSrc();
        byte[] content = null;
        BASE64Decoder decoder = new BASE64Decoder();
        content = decoder.decodeBuffer(src);
        InputStream is = new ByteArrayInputStream(content);
        
        Long l = new Long(content.length);
        
        NCFileNode node = service.createNewFileNodeWithStream(pk, filename, userid, is, l);
        Logger.info("node========="+node.getName());
		
	}

}
