package nc.jzmobile.handler;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.bs.pub.filesystem.IFileSystemService;
import nc.jdbc.framework.processor.ArrayListProcessor;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;
import nc.vo.pub.BusinessException;
import sun.misc.BASE64Encoder;

/**
 * 获取文件内容:根据附件id，获取附件内容和文件名
 * 
 * @author wss
 * 
 */
public class GetFileContentHandler implements INCMobileServletHandler {

	BaseDAO baseDAO = new BaseDAO();

	public Result handler(Map<String, String> map) throws Exception {
		Logger.info("==========GetFileContentHandler start==========");
		Result result = Result.instance();
		try{
			String pk_attchment = map.get("pk_attachment");
			Map<String, Object> outmap = queryAttachment(pk_attchment);
			result.setData(outmap);
		}catch (Exception e) {
			Logger.error(e);
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
		}
		Logger.info("==========GetBillFileListHandler end==========");
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
		map.put("attachment", attachment);
		map.put("filename", filename);

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
