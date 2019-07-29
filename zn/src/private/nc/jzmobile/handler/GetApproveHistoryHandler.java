package nc.jzmobile.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.jzmobile.IPFWorkflowQuery;
import nc.itf.uap.pf.IPFWorkflowQry;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;
import nc.vo.pub.pf.Pfi18nTools;
import nc.vo.pub.workflownote.WorkflownoteVO;
import nc.vo.pub.workflowqry.FlowAdminVO;
import nc.vo.wfengine.definition.WorkflowTypeEnum;

public class GetApproveHistoryHandler implements INCMobileServletHandler {

	public Result handler(Map<String, String> map) throws Exception {
		Logger.info("==========GetApproveHistoryHandler start==========");
		Result result = Result.instance();
		
		try{
			String billid = map.get("billid");
			String billtype = map.get("billtype");
			
			List<Map<String, String>> listMapInfo = new ArrayList<Map<String, String>>();
//			WorkflownoteVO[] vos1 = NCLocator.getInstance().lookup(IPFWorkflowQuery.class).queryAllCheckInfo(billid, billtype);
			WorkflownoteVO[] vos = NCLocator.getInstance().lookup(IPFWorkflowQry.class).queryWorkitems(billid, billtype, 0, 0);
//			WorknoteManager manager = new WorknoteManager();
//			WorkflownoteVO[] vos = manager.queryAllByBillId(billid, billtype,0,1);
			
			if(vos != null && vos.length > 0)
			{
				for(WorkflownoteVO vo : vos)
				{
					if(vo.getApprovestatus() != 0){
						vo.setCheckname(Pfi18nTools.getUserName(vo.getCheckman()));
					}
				}
			}
			
			//add by weixha 按照处理时间排序
			Arrays.sort(vos, new Comparator<WorkflownoteVO>() {
	
				@Override
				public int compare(WorkflownoteVO o1, WorkflownoteVO o2) {
					if(o1.getDealdate()==null)
						return 0;
					else if(o2.getDealdate()==null)
						return -1;
					else
						return o1.getDealdate().compareTo(o2.getDealdate());
				}
			});
			//end
			boolean isSub = true;
			if (vos != null && vos.length > 0) {
				for (WorkflownoteVO vo : vos) {
					if (isSub) {
						Map<String, String> wfMap = new HashMap<String, String>();
						String senderName = Pfi18nTools.getUserName(vo
								.getSenderman());
						if (senderName == null) {
							senderName = "";
						}
						wfMap.put("id", vo.getPk_checkflow());
						wfMap.put("checkman", senderName);
						wfMap.put("approveresult", "S");
						wfMap.put("checknote", "");
						wfMap.put("dealdate", vo.getSenddate() != null ? vo
								.getSenddate().toString() : vo.getSenddate()
								.toString());
						/**
						 * 
						 * 处理审批标志NC端和移动端不一致问题  NC端为作废 approveStatus=4
						 *//*
						wfMap.put("approveStatus",vo.getApprovestatus().toString() );*/
						listMapInfo.add(wfMap);
						isSub = false;
					}
//					if (vo.getApprovestatus() != 0) {
						Map<String, String> wfMap = new HashMap<String, String>();
						wfMap.put("id", vo.getPk_checkflow());
						wfMap.put("checkman", vo.getCheckname());
						/**
						 * 
						 * 处理审批标志NC端和移动端不一致问题  NC端为作废 approveStatus=4
						 */
						if(vo.getApprovestatus() == 4 && !"R".equals(vo.getApproveresult())){
							wfMap.put(
									"approveresult","D");
						}else{
							wfMap.put(
									"approveresult",
									vo.getApproveresult() == null ? "Q" : vo
											.getApproveresult());
						}
						wfMap.put("checknote",
								vo.getChecknote() == null ? "" : vo.getChecknote());
						wfMap.put("dealdate", vo.getDealdate() != null ? vo
								.getDealdate().toString() : vo.getSenddate()
								.toString());
						wfMap.put("approveStatus",vo.getApprovestatus().toString() );
						listMapInfo.add(wfMap);
//					}
				}
			}
			result.setData(listMapInfo);
		}catch (Exception e) {
			Logger.error(e);
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
		}
		Logger.info("==========GetApproveHistoryHandler end==========");
		return result;
	}

}
