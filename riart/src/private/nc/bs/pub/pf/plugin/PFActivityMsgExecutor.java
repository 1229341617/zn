package nc.bs.pub.pf.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.bs.pub.pf.MobileApproveTools;
import nc.bs.pub.pf.PfMessageUtil;
import nc.bs.pub.pf.tx.TxCompletionChecker;
import nc.bs.pub.wfengine.impl.ActionEnvironment;
import nc.bs.uap.pf.async.ScheduleEngineExecutor;
import nc.bs.wfengine.engine.WFActivityContext;
import nc.bs.wfengine.engine.ext.TaskTopicBusiVarCalculator;
import nc.bs.wfengine.engine.ext.TaskTopicResolver;
import nc.itf.uap.IUAPQueryBS;
import nc.itf.uap.pf.IPFMessageMetaService;
import nc.message.templet.bs.IMsgVarCalculater;
import nc.message.templet.bs.MsgContentCreator;
import nc.message.util.IDefaultMsgConst;
import nc.message.util.MessageCenter;
import nc.message.vo.MessageVO;
import nc.message.vo.NCMessage;
import nc.pubitf.rbac.IUserPubService;
import nc.vo.bd.psn.PsndocVO;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.pf.mobileapp.IPushableMessageConst;
import nc.vo.pf.mobileapp.PushableMessage;
import nc.vo.pf.msg.MessageMetaType;
import nc.vo.pf.msg.MessageMetaVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.compiler.PfParameterVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.pf.InvocationInfoCarrier;
import nc.vo.pub.pf.Pfi18nTools;
import nc.vo.pub.pf.plugin.ActivityMsgConfigVO;
import nc.vo.pub.pf.plugin.PfPluginConditionTypes;
import nc.vo.pub.pf.plugin.PfPluginTypes;
import nc.vo.pub.pf.plugin.ReceiverVO;
import nc.vo.pub.workflownote.WorkflownoteVO;
import nc.vo.uap.scheduler.TimeConfigVO;
import nc.vo.wfengine.core.activity.Activity;
import nc.vo.wfengine.core.util.CoreUtilities;
import nc.vo.wfengine.definition.WorkflowTypeEnum;
import nc.vo.wfengine.pub.WFTask;
import nc.vo.wfengine.pub.WorkitemMsgContext;

import com.thoughtworks.xstream.XStream;

public class PFActivityMsgExecutor {

	private Activity act = null;
	private WFTask task = null;
	private PfParameterVO paraVO = null;
	private WorkitemMsgContext context = null;
	
	private String pk_group = InvocationInfoProxy.getInstance().getGroupId();
	
	public PFActivityMsgExecutor(WFActivityContext actContext, Activity act, WFTask task) {
		this.act = act;
		this.task = task;
		paraVO = ActionEnvironment.getInstance().getParaVo(task.getBillversionPK());

		if (act == null || task == null) {
			return;
		}
		
		context = new WorkitemMsgContext();
		
		int actType = act.getActivityType();
		boolean isWorkflow = WorkflowTypeEnum.Workflow.getIntValue() == task.getWorkflowType();
		String actionType = TaskTopicResolver.getActionType(actType, isWorkflow);
		String result = TaskTopicResolver.getResult(isWorkflow, paraVO.m_workFlow.getActiontype(), act, task, task.getApproveResult()); 
		
		context.setActionType(actionType);
		context.setAgent(task.getApproveAgent());
		context.setBillid(task.getBillID());
		context.setBillno(task.getBillNO());
		context.setBillType(task.getBillType());
		context.setBusiObj(paraVO.m_preValueVo);
		context.setCheckman(task.getOperator());
		context.setCheckNote(paraVO.m_workFlow.getChecknote());
		context.setSender(task.getSenderman());
		context.setResult(result);
	}

	public void execute() throws BusinessException {
		try {
			
			if (act == null || task == null) {
				return;
			}
			
			Collection<ActivityMsgConfigVO> col = CoreUtilities.getMsgConfigs(act);

			if (col == null || col.size() == 0) {
				return;
			}

			PfPluginConditionTypes result = getApproveResult();
			
			
			List<ActivityMsgConfigVO> ncList = new ArrayList<ActivityMsgConfigVO>();
			List<ActivityMsgConfigVO> emailList = new ArrayList<ActivityMsgConfigVO>();
			List<ActivityMsgConfigVO> smsList = new ArrayList<ActivityMsgConfigVO>();

			for (ActivityMsgConfigVO config : col) {
				int cond = config.getCondition();

				if (cond != PfPluginConditionTypes.NONE_INT && cond != result.getValue()) {
					continue;
				}

				int type = config.getType();

				switch (type) {
				case PfPluginTypes.MSG_INT:
					ncList.add(config);
//					executeNCMsg(config);
					break;

				case PfPluginTypes.MAIL_INT:
					emailList.add(config);
//					executeEmailMsg(config);
					break;

				case PfPluginTypes.SMS_INT:
					smsList.add(config);
//					executeSMS(config);
					break;

				default:
					break;
				}
			}
			
			executeNCMsg(ncList);
			executeEmailMsg(emailList);
			executeSMS(smsList);
			
			
		} catch (Exception e) {
			// 消息发送成功与否不要影响流程
			Logger.error(e.getMessage(), e);
		}
	}

	private void executeNCMsg(List<ActivityMsgConfigVO> configs) throws BusinessException {
		
		// ncmsg需要push
		PushableMessage pm = new PushableMessage();
		
		List<NCMessage> toBeSent = new ArrayList<NCMessage>();
		List<MessageMetaVO> metaList = new ArrayList<MessageMetaVO>();
		
		
		for (ActivityMsgConfigVO config : configs) {
			Map<String, List<String>> rcvMap = classifyReceiverIdByLang(config);

			if (rcvMap.keySet().size() == 0)
				return;

			IMsgVarCalculater calculator = new TaskTopicBusiVarCalculator(context);
			NCMessage tempNCMsg = new NCMessage();
			MsgContentCreator creator = new MsgContentCreator();
			creator.setAttachname(context.getBillno() + ".html");

			// bugfix for msgContentCreator not resetting origin langcode
			String originLangcode = InvocationInfoProxy.getInstance().getLangCode();
			Map<String, NCMessage> msgMap = creator.createMessageUsingTemp2(config.getMsgTempcode(), pk_group, rcvMap
					.keySet().toArray(new String[0]), tempNCMsg, calculator, context.getBusiObj(), context.buildBusiObjDs());
			InvocationInfoProxy.getInstance().setLangCode(originLangcode);


			for (Iterator<String> it = msgMap.keySet().iterator(); it.hasNext();) {
				String langcode = it.next();
				NCMessage ncmsg = msgMap.get(langcode);

				fillCommonField(ncmsg, langcode);

				MessageVO msgVO = ncmsg.getMessage();
				
				
				List<String> rcvList = rcvMap.get(langcode);
				
				msgVO.setReceiver(getStrSequenctFromList(rcvList));
				msgVO.setMsgtype("nc");
				
				msgVO.setSubcolor(PfMessageUtil.getMessageColor(task.getBillType(), task.getBillID()));

				if (config.isNeedCheck()) {
					msgVO.setMsgsourcetype(WorkflownoteVO.NCMSG_TYPE_MSGNEEDCHECK);
					msgVO.setContenttype(WorkflownoteVO.FLOWMSG_NEED_CHECK);
				} else {
					msgVO.setMsgsourcetype(IDefaultMsgConst.NOTICE);
					msgVO.setContenttype(WorkflownoteVO.FLOWMSG_AUTO);
				}
				
				if (config.isPushable()) {
					String message = msgVO.getSubject();
					for (String receiver : rcvList) {
						pm.add(message, receiver, UUID.randomUUID().toString(), IPushableMessageConst.TYPE_MESSAGE);
					}
				}
				
				toBeSent.add(ncmsg);
				
				List<MessageMetaVO> metavos = createMeta(msgVO, rcvList, MessageMetaType.NC_NOTICE);
				metaList.addAll(metavos);
			}
		}
		
		sendMsgSync(toBeSent.toArray(new NCMessage[0]));
		MobileApproveTools.sendMsgSyncToMobileApp(toBeSent, task);
		NCLocator.getInstance().lookup(IPFMessageMetaService.class).insert(metaList);
		
		pm.pushAsyncWithTxCheck();
		
		//TODO: sign by liuhm 发送消息的时候需要设置传移动平台发消息
	}
	
	/**
	 * 不包含msgtype和receiver
	 * @param msgVO
	 * @return
	 */
	private List<MessageMetaVO> createMeta(MessageVO msgVO, List<String> receiverList, String messageType) {
		MessageMetaVO metavo = new MessageMetaVO();
		
		metavo.setPk_group(paraVO.m_pkGroup);
		metavo.setMessage_type(messageType);
		metavo.setBilltype(paraVO.m_billType);
		metavo.setBillid(paraVO.m_billVersionPK);
		metavo.setBillno(paraVO.m_billNo);
		metavo.setPk_checkflow(null);
		metavo.setPk_message(null);
		metavo.setTitle(msgVO.getSubject());
		metavo.setSenddate(msgVO.getSendtime());
		metavo.setActivity_id(task.getActivityID());
		metavo.setPk_wf_instance(task.getWfProcessInstancePK());
		
		List<MessageMetaVO> metaList = new ArrayList<MessageMetaVO>();
		for (String receiver : receiverList) {
			MessageMetaVO cloned = (MessageMetaVO) metavo.clone();
			cloned.setReceiver(receiver);
			
			metaList.add(cloned);
		}
		
		return metaList;
	}
	
	private void executeSMS(List<ActivityMsgConfigVO> configs) throws BusinessException {
		List<NCMessage> toBeSent = new ArrayList<NCMessage>();
		final List<MessageMetaVO> metaList = new ArrayList<MessageMetaVO>();
		
		for (ActivityMsgConfigVO config : configs) {
			Map<String, List<String>> rcvMap = classifyReceiverIdByLang(config);

			if (rcvMap.keySet().size() == 0)
				return;

			IMsgVarCalculater calculator = new TaskTopicBusiVarCalculator(context);
			NCMessage tempNCMsg = new NCMessage();
			MsgContentCreator creator = new MsgContentCreator();
			creator.setAttachname(context.getBillno() + ".html");

			Map<String, NCMessage> msgMap = creator.createMessageUsingTemp(config.getMsgTempcode(), pk_group, rcvMap
					.keySet().toArray(new String[0]), tempNCMsg, calculator, context.getBusiObj(), context.buildBusiObjDs());

			for (Iterator<String> it = msgMap.keySet().iterator(); it.hasNext();) {
				String langcode = it.next();
				NCMessage ncmsg = msgMap.get(langcode);
				List<String> receiverList = rcvMap.get(langcode);

				fillCommonField(ncmsg, langcode);

				MessageVO msgVO = ncmsg.getMessage();
				msgVO.setReceiver(getStrSequenctFromList(receiverList));
				msgVO.setMsgtype("sms");

				toBeSent.add(ncmsg);
				
				
				List<MessageMetaVO> metas = createMeta(msgVO, receiverList, MessageMetaType.SMS_NOTICE);
				metaList.addAll(metas);
			}
		}
		
		sendMsgAsync(toBeSent.toArray(new NCMessage[0]), new AsyncSendCallback() {
			@Override
			public void doCallback() throws BusinessException {
				NCLocator.getInstance().lookup(IPFMessageMetaService.class).insert(metaList);
			}
		});
	}
	
	private void executeEmailMsg(List<ActivityMsgConfigVO> configs) throws BusinessException {
		List<NCMessage> toBeSent = new ArrayList<NCMessage>();
		final List<MessageMetaVO> metaList = new ArrayList<MessageMetaVO>();
		
		for (ActivityMsgConfigVO config : configs) {
			Map<String, List<String>> rcvMap = classifyReceiverIdByLang(config);

			if (rcvMap.keySet().size() == 0)
				return;

			IMsgVarCalculater calculator = new TaskTopicBusiVarCalculator(context);
			NCMessage tempNCMsg = new NCMessage();
			MsgContentCreator creator = new MsgContentCreator();
			creator.setAttachname(context.getBillno() + ".html");

			Map<String, NCMessage> msgMap = creator.createMessageUsingTemp(config.getMsgTempcode(), pk_group, rcvMap
					.keySet().toArray(new String[0]), tempNCMsg, calculator, context.getBusiObj(), context.buildBusiObjDs());

			for (Iterator<String> it = msgMap.keySet().iterator(); it.hasNext();) {
				String langcode = it.next();
				NCMessage ncmsg = msgMap.get(langcode);
				List<String> receiverList = rcvMap.get(langcode);

				fillCommonField(ncmsg, langcode);

				MessageVO msgVO = ncmsg.getMessage();
				msgVO.setReceiver(getStrSequenctFromList(receiverList));
				msgVO.setMsgtype("email");

				toBeSent.add(ncmsg);
				
				List<MessageMetaVO> metas = createMeta(msgVO, receiverList, MessageMetaType.EMAIL_NOTICE);
				metaList.addAll(metas);
			}
		}
		
		sendMsgAsync(toBeSent.toArray(new NCMessage[0]), new AsyncSendCallback() {
			
			@Override
			public void doCallback() throws BusinessException {
				NCLocator.getInstance().lookup(IPFMessageMetaService.class).insert(metaList);
			}
		});
	}
	

	private void fillCommonField(NCMessage ncmsg, String langcode) {
		MessageVO msgVO = ncmsg.getMessage();
		msgVO.setSendtime(new UFDateTime());
		msgVO.setIshandled(UFBoolean.valueOf(false));
		msgVO.setSendstate(UFBoolean.valueOf(true));
		msgVO.setPk_org(task.getPk_org());
		msgVO.setPk_detail(task.getTaskPK());
		msgVO.setPk_group(InvocationInfoProxy.getInstance().getGroupId());
		msgVO.setDetail(task.getBillID() + "@" + task.getBillType() + "@" + task.getBillNO());
		msgVO.setSender(PfMessageUtil.DEFAULT_SENDER);
		msgVO.setDomainflag(PfMessageUtil.getModuleOfBilltype(task.getBillType()));
		
		String currLangcode = InvocationInfoProxy.getInstance().getLangCode();
		{
			InvocationInfoProxy.getInstance().setLangCode(langcode);

			msgVO.setSubject(nc.vo.pub.msg.MessageVO.getMessageNoteAfterI18N(msgVO.getSubject()));
			msgVO.setContent(nc.vo.pub.msg.MessageVO.getMessageNoteAfterI18N(msgVO.getContent()));

			InvocationInfoProxy.getInstance().setLangCode(currLangcode);
		}
	}

	private Map<String, List<String>> classifyReceiverIdByLang(ActivityMsgConfigVO config) throws BusinessException {
		String[] cuserids = getReceivers(config);

		if (cuserids == null || cuserids.length == 0) {
			return new HashMap<String, List<String>>();
		} else {
			return Pfi18nTools.classifyUsersByLangcode(cuserids);
		}
	}

	private Map<String, List<String>> classifyReceiverEmailByLang(ActivityMsgConfigVO config) throws BusinessException {
		Map<String, List<String>> rcvMap = new HashMap<String, List<String>>();

		String[] cuserids = getReceivers(config);

		if (cuserids == null || cuserids.length == 0) {
			return rcvMap;
		}

		for (String cuserid : cuserids) {
			String langcode = Pfi18nTools.getLangcodeOfUser(cuserid);
			String email = getEmailOfUser(cuserid);

			if (StringUtil.isEmptyWithTrim(email))
				continue;

			List<String> list = rcvMap.get(langcode);
			if (list == null) {
				list = new ArrayList<String>();
				rcvMap.put(langcode, list);
			}

			list.add(email);
		}

		return rcvMap;
	}
	
	private Map<String, List<String>> classifyReceiverMobileByLang(ActivityMsgConfigVO config) throws BusinessException {
		Map<String, List<String>> rcvMap = new HashMap<String, List<String>>();

		String[] cuserids = getReceivers(config);

		if (cuserids == null || cuserids.length == 0) {
			return rcvMap;
		}

		for (String cuserid : cuserids) {
			String langcode = Pfi18nTools.getLangcodeOfUser(cuserid);
			String email = getPhoneNumOfUser(cuserid);

			if (StringUtil.isEmptyWithTrim(email))
				continue;

			List<String> list = rcvMap.get(langcode);
			if (list == null) {
				list = new ArrayList<String>();
				rcvMap.put(langcode, list);
			}

			list.add(email);
		}

		return rcvMap;
	}

	private String getStrSequenctFromList(List<String> list) {
		if (list == null || list.size() == 0)
			return "";

		StringBuffer sb = new StringBuffer();

		for (String str : list) {
			sb.append(",");
			sb.append(str);
		}

		return sb.substring(1);
	}

	private String[] getReceivers(ActivityMsgConfigVO config) throws BusinessException {
		ReceiverVO[] rcvs = null;
		try {
			rcvs = (ReceiverVO[]) new XStream().fromXML(config.getReceiver());
		} catch (Exception e) {
			// yk+ 若获取receivers时出现了异常（在只配置了消息没有配置接收人时会出现这样的情况），
			// 不可以影响流程正常运转
			// 因此只log，不抛出
			Logger.error("Workflow Platform: error occured while acquiring receivers: " + e.getMessage(), e);
			return null;
		}

		return new MsgReceiverUtil().getMessageReceivers(rcvs, task);
	}

	private String getEmailOfUser(String cuserid) throws BusinessException {

		IUserPubService userService = NCLocator.getInstance().lookup(IUserPubService.class);
		String pk_psndoc = userService.queryPsndocByUserid(cuserid);

		if (StringUtil.isEmptyWithTrim(pk_psndoc)) {
			Logger.error("用户" + cuserid + "未关联人员档案", new Throwable());
			return null;
		}

		IUAPQueryBS uapQry = NCLocator.getInstance().lookup(IUAPQueryBS.class);
		PsndocVO psndoc = (PsndocVO) uapQry.retrieveByPK(PsndocVO.class, pk_psndoc);
		String email = psndoc == null ? null : psndoc.getEmail();

		return email;
	}
	
	private String getPhoneNumOfUser(String cuserid) throws BusinessException {
		IUserPubService userService = NCLocator.getInstance().lookup(IUserPubService.class);
		String pk_psndoc = userService.queryPsndocByUserid(cuserid);

		if (StringUtil.isEmptyWithTrim(pk_psndoc)) {
			Logger.error("用户" + cuserid + "未关联人员档案", new Throwable());
			return null;
		}

		IUAPQueryBS uapQry = NCLocator.getInstance().lookup(IUAPQueryBS.class);
		PsndocVO psndoc = (PsndocVO) uapQry.retrieveByPK(PsndocVO.class, pk_psndoc);
		String phone = psndoc == null ? null : psndoc.getMobile();
		
		return phone;
	}

	private PfPluginConditionTypes getApproveResult() {
		PfPluginConditionTypes resultInCond = null;
		String result = task.getApproveResult();
		
		if (task == null || result == null) {	// 制单环节发消息时result为null
			resultInCond = PfPluginConditionTypes.NONE;
		} else if (result.equals("Y")) {
			resultInCond = PfPluginConditionTypes.CHECKPASS;
		} else if (result.equals("N")) {
			resultInCond = PfPluginConditionTypes.NOPASS;
		} else if (result.equals("R")) {
			resultInCond = PfPluginConditionTypes.REJECT;
		} else {
			resultInCond = PfPluginConditionTypes.NONE;
		}

		return resultInCond;
	}

	private void sendMsgAsync(final NCMessage[] ncmsgs,
			final AsyncSendCallback callback) throws BusinessException {
		final TimeConfigVO config = new TimeConfigVO();
		config.setJustInTime(true);

		final InvocationInfoCarrier infoCarrier = new InvocationInfoCarrier();
		final String txMark = TxCompletionChecker.getInstance().generateTxMark();

		new ScheduleEngineExecutor().execute(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(10 * 1000L);
					infoCarrier.loadInvocationInfo();
					
					if (!TxCompletionChecker.getInstance().isTxCompleted(txMark)) {
						return;
					}
					
					sendMsgSync(ncmsgs);

					if (callback != null) {
						callback.doCallback();
					}
				} catch (Exception e) {
					Logger.error(e.getMessage(), e);
				}
			}
		});

	}

	private void sendMsgSync(NCMessage[] ncmsgs) throws BusinessException {
		try {
			MessageCenter.sendMessage(ncmsgs,true);
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			throw new BusinessException(e);
		}
	}
	
	
	private interface AsyncSendCallback {
		void doCallback() throws BusinessException;
	}

}
