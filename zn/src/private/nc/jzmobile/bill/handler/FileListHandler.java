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
 * 查看单据附件列表
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

	private String getFileSize(int size) // 附件长度转化为**K 或者**M
	{
		int GB = 1024 * 1024 * 1024;// 定义GB的计算常量
		int MB = 1024 * 1024;// 定义MB的计算常量
		int KB = 1024;// 定义KB的计算常量
		if (size / GB >= 1)// 如果当前Byte的值大于等于1GB
		{
			return String.valueOf(Math.round(size / (float) GB)) + "G";// 将其转换成GB
		} else if (size / MB >= 1)// 如果当前Byte的值大于等于1MB
		{
			return String.valueOf(Math.round(size / (float) MB)) + "M";// 将其转换成MB
		} else if (size / KB >= 1)// 如果当前Byte的值大于等于1KB
		{
			return String.valueOf(Math.round(size / (float) KB)) + "K";// 将其转换成KGB
		} else {
			return String.valueOf(size) + "B";// 显示Byte值
		}
	}
}
