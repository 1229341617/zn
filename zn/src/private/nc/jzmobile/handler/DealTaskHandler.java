package nc.jzmobile.handler;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.uap.pf.IPFMobileAppService;
import nc.jzmobile.utils.JZMobileAppUtils;
import nc.jzmobile.utils.MobileMessageUtil;
import nc.jzmobile.utils.MobilePropertiesLoader;
import nc.vo.jzmobile.app.MobilePropModel;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;
import nc.vo.jzpm.ctrl.pubenum.JZBmCtrModeEnum;
import nc.vo.pf.mobileapp.ITaskType;
import nc.vo.pf.mobileapp.MobileAppUtil;
import nc.vo.pf.mobileapp.MobileAppUtils;
import nc.vo.pf.mobileapp.TaskMetaData;
import nc.vo.pf.mobileapp.query.TaskQuery;
import nc.vo.pub.jzpm.budgetctrl.BudgetCtrlException;
import nc.vo.pub.pf.AssignableInfo;
import nc.vo.pub.workflownote.WorkflownoteVO;
import nc.vo.sm.UserVO;

/**
 * ���ݴ�����������id���û�id���������ͣ�������� ���������񵥾�
 * 
 * @author wss
 * 
 */
public class DealTaskHandler implements INCMobileServletHandler {
	private final static String ACTION_AGREE = "agree";
	private final static String ACTION_DISAGREE = "disagree";
	private final static String ACTION_REJECT = "reject";
	private final static String REDO_DISAGREE = "redoagree";
	private final static String ACTION_ADDASSIGN = "addassign";//��ǩ
	private final static String ACTION_REASSIGN = "transfer";//ָ��
	private final static String ACTION_UNAPPROVE = "unapprove";//����

	public Result handler(Map<String, String> map) throws Exception {
		Result result = Result.instance();
		Logger.info("==========DealTaskHandler start==========");
		String userid = map.get("userid");
		String taskid = map.get("taskid");
		String note = map.get("note");
		String action = map.get("action");
		String nodeid = map.get("nodeid");
		String userIds = map.get("targetUserlist");
		String msgSenders = map.get("msgSenders");
		List<String> userList = null;
		if(userIds!=null&&userIds.length()>0){
			String ids[] = userIds.split(";");
			userList=Arrays.asList(ids);
		}
		List<String> senders = null;
		if(msgSenders!=null&&msgSenders.length()>0){
			String ids[] = msgSenders.split(";");
			senders=Arrays.asList(ids);
		}
		Logger.info("userid��" + userid + ",taskid:" + taskid + ",note:" + note + ",action:" + action+" ,userlist="+userIds);
//		InvocationInfoProxy.getInstance().setUserCode(userid);
//		InvocationInfoProxy.getInstance().setUserId(userid);
		InvocationInfoProxy.getInstance().setGroupId(JZMobileAppUtils.getPkGroupByUserId(userid));
		IPFMobileAppService service = NCLocator.getInstance().lookup(IPFMobileAppService.class);

		if (action != null) {
			try {
				/**
				 * 
				 * �����������������һ��������Ϊָ��ʱ��Ĺ���
				 * 
				 * 2018/03/17 mxx
				 */
				ITaskType taskType = JZMobileAppUtils.getTaskType("ishandled", "unhandled");
				TaskQuery query = taskType.createNewTaskQuery();
				// ��ѯ������������
				TaskMetaData tmd = null;
				if (action.equalsIgnoreCase(ACTION_AGREE)) {
					tmd=query.queryTaskMetaData(taskid);
					WorkflownoteVO worknoteVO=MobileAppUtil.checkWorkflow(tmd);
					if(worknoteVO!=null&&userIds==null&&this.isExistAssignableInfoWhenPass(worknoteVO)){
						//���ں����
						result.setErrorCode(1000);
						result.setErrorMessage("���ں���ָ����Ա");
					}else{
						tmd=null;
						tmd = service.doAgree("", userid, taskid, note, userList,senders);
					}
				} else if (action.equalsIgnoreCase(ACTION_DISAGREE)) {
					tmd = service.doDisAgree("", userid, taskid, note, userList,senders);
				} else if (action.equalsIgnoreCase(ACTION_REJECT)) {
					/**
					 * wss 20160801
					 */
					InvocationInfoProxy.getInstance().setUserId(userid);
					tmd = service.doReject("", userid, taskid, note, nodeid,senders);
				}
				else if(action.equalsIgnoreCase(ACTION_ADDASSIGN)){//��ǩ
					if(userList==null&&userList.size()==0){
						result.setErrorCode(ResultConsts.CODE_ERR);
						result.setErrorMessage("��ǩ��Ա�б�Ϊ�գ�");
					}else
						tmd = service.doAddApprover("", userid, taskid, note, userList,senders);
				}
				else if(action.equalsIgnoreCase(ACTION_REASSIGN)){//ָ��
					if(userList==null&&userList.size()==0){
						result.setErrorCode(ResultConsts.CODE_ERR);
						result.setErrorMessage("ָ����ԱΪ�գ�");
					}
					else{
						tmd = service.doReassign("", userid, taskid, note, userList.get(0),senders);
					}
				}
				else if (action.equalsIgnoreCase(REDO_DISAGREE)) {
//					ѯ�ʺ���������
					tmd = service.redoAgree("", userid, taskid, note);
				}
				else if(action.equalsIgnoreCase(ACTION_UNAPPROVE)){
					tmd = service.doBack( userid, taskid);
				}
				else {
//					result = "false";
//					desc = "action��������ȷ��";
					result.setErrorCode(ResultConsts.CODE_404);
					result.setErrorMessage("action��������ȷ��");
				}
				if(tmd!=null)
					result.setData(tmd.getPk_checkflow());
				    sengMessage(userList,senders,userid,tmd);
			} catch (BudgetCtrlException e) {
//				modify by weixha �ṩ��Ԥ������쳣�Ĵ���20161213
				JZBmCtrModeEnum alarmEnum = JZBmCtrModeEnum.getValue(e.getAlarmtype());

				switch (alarmEnum) {
				case flexibelctrl:
//					desc = e.getCtrlmessage() + "�Ƿ������";
//					result = "ask";
					result.setErrorCode(ResultConsts.CODE_REDIRECT);
					result.setErrorMessage(e.getCtrlmessage() + "�Ƿ������");
					break;
				case flowctrl:
//					desc = e.getCtrlmessage() + "��ҵ�񵥾ݶ�Ӧ��[�����������̵���]û�н���������";
//					result = "false";
					result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
					result.setErrorMessage(e.getCtrlmessage() + "��ҵ�񵥾ݶ�Ӧ��[�����������̵���]û�н���������");
					break;
				case rigidctrl:
//					desc = e.getCtrlmessage() + "��ҵ�񵥾�Ԥ�㳬֧���������ύ������";
//					result = "false";
					result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
					result.setErrorMessage(e.getCtrlmessage() + "��ҵ�񵥾�Ԥ�㳬֧���������ύ������");
				}
				Logger.error(e);
			}
			catch (Exception e) {
				Logger.error(e);
				result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
				result.setErrorMessage("����������������ο�NC����־��"+e.getMessage());
			}
		}
		Logger.info("==========DealTaskHandler end==========");
		return result;
	}
	private boolean isExistAssignableInfoWhenPass(WorkflownoteVO worknoteVO) {
		if (worknoteVO.getActiontype().endsWith(
				WorkflownoteVO.WORKITEM_ADDAPPROVER_SUFFIX))
			return false;

		@SuppressWarnings("unchecked")
		Vector<AssignableInfo> assignInfos = worknoteVO.getTaskInfo()
				.getAssignableInfos();
		if (assignInfos != null && assignInfos.size() > 0) {
			String strCriterion = null;
			for (AssignableInfo ai : assignInfos) {
				strCriterion = ai.getCheckResultCriterion();
				if (AssignableInfo.CRITERION_NOTGIVEN.equals(strCriterion)
						|| AssignableInfo.CRITERION_PASS.equals(strCriterion))
					return true;
			}
		}
		return false;
	}
	
private void sengMessage(List<String> userIds,List<String> senders,String userId,TaskMetaData tmd) throws Exception{
		
		MobilePropModel propModel = new MobilePropertiesLoader().getViewResolverProperties();
		
		StringBuffer messageUrl = new StringBuffer(propModel.getMobileApproveURL());
		messageUrl.append("?appid="+propModel.getMaAppId());
		messageUrl.append("&statuskey=ishandled");
		messageUrl.append("&statuscode=unhandled");
		messageUrl.append("&taskid="+tmd.getPk_checkflow());
		messageUrl.append(propModel.getMobileApproveHtml());
		
		String userName=MobileAppUtils.getUserNameById(userId);
		
		//��ǩ������Ϣ
		if(userIds!=null&&userIds.size()>0){
			for(String id:userIds){
				UserVO user=new UserVO();
				user.setCuserid(id);
				user.setUser_code(MobileAppUtils.getUserCodeById(id));
			
				MobileMessageUtil.sendMobileMessage(userName+" ��ǩ,���ݺ�:"+tmd.getBillNo(), user, "0", messageUrl.toString());
			}
			
			
		}
		
		//���Ͳ�����Ϣ
		if(senders != null && senders.size() >0 ){
			for(String id:senders){
				UserVO user=new UserVO();
				user.setCuserid(id);
				user.setUser_code(MobileAppUtils.getUserCodeById(id));
			
				MobileMessageUtil.sendMobileMessage(userName+" ���� ����ͨ�� ���ݺ�:"+tmd.getBillNo(), user, "0", messageUrl.toString());
			}
		}
	}
	

}
