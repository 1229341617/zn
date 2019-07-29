package nc.jzmobile.bill.handler;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.bs.pub.filesystem.IFileSystemService;
import nc.jdbc.framework.processor.ArrayListProcessor;
import nc.jzmobile.handler.INCMobileServletHandler;
import nc.vo.am.common.util.StringUtils;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;
import nc.vo.pub.BusinessException;
import sun.misc.BASE64Encoder;

/**
 * 查看单据附件详情
 * @author mxx
 *
 */
public class FileContentHandler implements INCMobileServletHandler {

	BaseDAO baseDAO = new BaseDAO();

	public Result handler(Map<String, String> map) throws Exception {
		Logger.info("==========FileContentHandler start==========");
		Result result = Result.instance();
		try{
			String pk_attchments = map.get("pk_attachments");
			
			if(StringUtils.isEmpty(pk_attchments)){
				throw new BusinessException("附件标识不能为空！");
			}
			
			String[] pk_attchmentArray = pk_attchments.split("#");
			List<Map<String, Object>> outfiles = new ArrayList<Map<String, Object>>();
			for(int i = 0 ;i<pk_attchmentArray.length;i++){
				Map<String, Object> outmap = queryAttachment(pk_attchmentArray[i]);
				outfiles.add(outmap);
			}
			result.setData(outfiles);
			
		}catch (Exception e) {
			Logger.error(e);
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
		}
		Logger.info("==========FileContentHandler end==========");
		return result;
	}

	public Map<String, Object> queryAttachment(String strPk_Attachment)
			throws BusinessException {
		Map<String, Object> map = new HashMap<String, Object>();

		String filePath = this.getFilePath(strPk_Attachment);
		if (filePath == null) {
			return null;
		}
		String[] paths = filePath.split("/");
		String filename = paths[paths.length - 1];
		String attachment = this.getFileAttatchment(filePath);
		map.put("src", attachment);
		map.put("name", filename);
		return map;
	}

	/**
	 * 获取文件附件
	 * 
	 * @return
	 */
	private String getFileAttatchment(String filePath) {
		if (filePath == null || filePath.equals("")) {
			return null;
		}
		String fileAttatchment = "";
		ByteArrayOutputStream output = null;

		IFileSystemService service = NCLocator.getInstance().lookup(
				IFileSystemService.class);
		output = new ByteArrayOutputStream();
		try {
			service.downLoadFile(filePath, output);
			byte[] fileBts = output.toByteArray();
			fileAttatchment = new BASE64Encoder().encode(fileBts);
		} catch (BusinessException e) {
			Logger.error(e.getMessage(), e);
		} finally {
			try {
				output.close();
			} catch (Exception e) {
				Logger.error(e.getMessage(), e);
			}
		}

		return fileAttatchment;
	}

	/**
	 * 获取文件路径
	 * 
	 * @param pkAttatch
	 * @return
	 */
	private String getFilePath(String pkAttatch) {

		String filePath = null;

		String sql = "select filepath from sm_pub_filesystem where pk='"
				+ pkAttatch + "'";
		try {
			ArrayList list = (ArrayList) baseDAO.executeQuery(sql,
					new ArrayListProcessor());
			if (list.size() == 0) {
				return null;
			} else {
				Object[] fileAttatchment = (Object[]) list.get(0);
				String filepath = (String) fileAttatchment[0];
				return filepath;
			}
		} catch (DAOException e) {
			Logger.error(e.getMessage(), e);
		}

		return filePath;
	}
}
