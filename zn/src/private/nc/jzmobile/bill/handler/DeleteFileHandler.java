package nc.jzmobile.bill.handler;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.logging.Logger;
import nc.jzmobile.handler.INCMobileServletHandler;
import nc.vo.jzmobile.app.Result;

public class DeleteFileHandler implements INCMobileServletHandler{

	@Override
	public Result handler(Map<String, String> map) throws Exception {
		Result result = Result.instance();
		Logger.info("---BillSaveHandler  start---");
		
		String taskid = map.get("taskid");
		String filename = map.get("filename");
		
		String path = taskid+"/"+filename;
		
		BaseDAO dao = new BaseDAO();
		String sql = "delete from sm_pub_filesystem where filepath = '"+path+"'";
		try{
			dao.executeUpdate(sql);
			result.success().setData("É¾³ý³É¹¦!!!");
		}catch(Exception e){
			e.printStackTrace();
			result.fail().setData(e.getMessage());
		}
		return result;
	}
}

