package nc.jzmobile.bill.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.logging.Logger;
import nc.jdbc.framework.processor.ArrayListProcessor;
import nc.jzmobile.handler.INCMobileServletHandler;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;
import nc.vo.pub.BusinessException;

/**
 * �鿴���ݸ����б�
 * @author mxx
 *
 */
public class FileListHandler implements INCMobileServletHandler {

	public Result handler(Map<String, String> map) throws Exception {
		Logger.info("==========FileListHandler start==========");
		Result result = Result.instance();
		try{
			String billid = map.get("billid");
			String billtype = map.get("billtype");
			result.setData(getBillFileList(billid, billtype));
		}catch (Exception e) {
			Logger.error(e);
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
		}
		Logger.info("==========FileListHandler end==========");
		return result;
	}

	@SuppressWarnings({ "rawtypes" })
	public List<Map<String, String>> getBillFileList(String billid, String billtype)
			throws BusinessException {
		BaseDAO dao = new BaseDAO();
		String sql = "";
		List<Map<String, String>> listMapInfo = new ArrayList<Map<String, String>>();
		sql = "select filelength,filepath, pk from sm_pub_filesystem  where filepath like '%"
				+ billid + "%' and isfolder ='n'";
		ArrayList list = (ArrayList) dao.executeQuery(sql,
				new ArrayListProcessor());
		for (int i = 0; i < list.size(); i++) {
			Map<String, String> fileMap = new HashMap<String, String>();
			Object[] os = (Object[]) list.get(i);
			String[] paths = ((String) os[1]).split("/");
			String filename = paths[paths.length - 1];
			String size = getFileSize(Integer.parseInt(String.valueOf(os[0])));
			fileMap.put("size", size);
			fileMap.put("filepath", (String) os[1]);
			fileMap.put("name", filename);
			fileMap.put("pk", (String) os[2]);
			listMapInfo.add(fileMap);
		}
		return listMapInfo;
	}

	private String getFileSize(int size) // ��������ת��Ϊ**K ����**M
	{
		int GB = 1024 * 1024 * 1024;// ����GB�ļ��㳣��
		int MB = 1024 * 1024;// ����MB�ļ��㳣��
		int KB = 1024;// ����KB�ļ��㳣��
		if (size / GB >= 1)// �����ǰByte��ֵ���ڵ���1GB
		{
			return String.valueOf(Math.round(size / (float) GB)) + "G";// ����ת����GB
		} else if (size / MB >= 1)// �����ǰByte��ֵ���ڵ���1MB
		{
			return String.valueOf(Math.round(size / (float) MB)) + "M";// ����ת����MB
		} else if (size / KB >= 1)// �����ǰByte��ֵ���ڵ���1KB
		{
			return String.valueOf(Math.round(size / (float) KB)) + "K";// ����ת����KGB
		} else {
			return String.valueOf(size) + "B";// ��ʾByteֵ
		}
	}
}
