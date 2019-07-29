package nc.jzmobile.bs.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import nc.bs.framework.adaptor.IHttpServletAdaptor;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.comn.NetStreamContext;
import nc.bs.framework.server.ISecurityTokenCallback;
import nc.bs.logging.Logger;
import nc.jzmobile.handler.INCMobileServletHandler;
import nc.jzmobile.utils.MobilePropertiesLoader;
import nc.login.bs.LoginVerifyBean;
import nc.login.bs.LoginVerifyException;
import nc.vo.jzmobile.app.Result;
import nc.vo.jzmobile.app.ResultConsts;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

@SuppressWarnings("all")
public class NCMobileServlet extends HttpServlet implements IHttpServletAdaptor {
	private final static String SESSION_ID = "JSESSIONID";
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doAction(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doAction(req, resp);
	}

//	@SuppressWarnings("unchecked")
	@Override
	public void doAction(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Logger.info("==========NCMobileServlet start==========");
		Logger.info("start time ：" + new UFDate().toString());
		InvocationInfoProxy.getInstance().setUserDataSource(new MobilePropertiesLoader().getViewResolverProperties().getDataSource());
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		PrintWriter out = resp.getWriter();
		JSONObject json = getServletJson(req);
		String method = "";
		Map<String, String> map = new HashMap<String, String>();
//		Map<String, Object> returnMap = null;
		Result result = Result.instance();
		try {
			servletVerify(req, resp);
			method = json.getString("method");
			map = JSON.toJavaObject(json, Map.class);
			InvocationInfoProxy.getInstance().setUserId(map.get("userid"));
			String packageName = "nc.jzmobile.handler.";
			String className = "";
			if ("ncLoginCheck".equalsIgnoreCase(method)) {
				className = "NCLoginCheckHandler";
			} else if (method.equalsIgnoreCase("getTask")) // 获取任务详情	
			{
				className = "GetTaskHandler";
			} else if(method.equalsIgnoreCase("isTaskUnhandle")) //判断是否是代办任务
			{
				className = "TaskUnhandleHandler";
			}else if (method.equalsIgnoreCase("getTaskList")) // 获取任务列表
			{
				className = "GetTaskListHandler";
				//sclassName="GetTasksMakerAndOrgHandler";
			} else if (method.equalsIgnoreCase("getTaskListCount")) // 获取任务列表数量
			{
				className = "GetTaskListCountHandler";
			} 
			else if (method.equalsIgnoreCase("getBillFileCount")) // 获取附件数量
			{
				className = "GetBillFileCountHandler";
			} else if (method.equalsIgnoreCase("getBillFileList")) // 获取附件列表
			{
				className = "GetBillFileListHandler";
			} else if (method.equalsIgnoreCase("getWorkflow")) // 获取任务流程图
			{
				className = "GetWorkflowHandler";
			} else if (method.equalsIgnoreCase("getApproveHistory")) // 获取任务流程图
			{
				className = "GetApproveHistoryHandler";
			} else if (method.equalsIgnoreCase("getTaskBodyDetail")) // 获取任务单据表体详情
			{
				className = "GetTaskBodyDetailHandler";
			} else if (method.equalsIgnoreCase("dealTask")) // 处理任务， 同意、不同意、驳回
			{
				className = "DealTaskHandler";
			} else if (method.equalsIgnoreCase("getFileContent")) {
				className = "GetFileContentHandler";
			}
			else if (method.equalsIgnoreCase("getUser")) {
				className = "GetUserHandler";
			}
			else if (method.equalsIgnoreCase("getUserList")) {
				className = "GetUserListHandler";
			}
			else if (method.equalsIgnoreCase("getTaskUserList")) {
				className = "GetTaskUserListHandler";
			}
			else if(method.equalsIgnoreCase("getTasksMakerAndOrg")){//获取当前未审批单据的所有pk_org 和所有单据制单人
				className="GetTasksMakerAndOrgHandler";
				
			}else if(method.equalsIgnoreCase("sendMessagewarn")){//发送消息提醒功能
				className="SendMessageWarnHandler";
				
			}else if(method.equalsIgnoreCase("getApproveBillNum")){
				className="GetApproveBillNumHandler";
				
			}else if(method.equalsIgnoreCase("getEditProperties")){//获取审批过程中的可编辑属性
				className="GetEditPropertiesHandler";

			}else if(method.equals("getApproveOpt")){
				className = "GetApproveOptHandler"; //获取NC设置的默认审批批语,根据当前登录用户id
				
			}else if(method.equals("getRefData")){
				className = "GetApproveOptHandler"; //根据用户ID 获取对应的基本组织权,以及报表可见性
				
			} 
			if(StringUtils.isEmpty(className)){
				throw new ServletException("找不到method为" + method
						+ "的对应Handler处理类，请确认后重试！");
			}
			INCMobileServletHandler handler = null;
			handler = (INCMobileServletHandler) Class
					.forName(packageName + className).newInstance();
					
			result = handler.handler(map);
		} catch (Exception e) {
			Logger.error(e);
			result.setErrorCode(ResultConsts.CODE_SERVER_ERR);
			result.setErrorMessage(e.getMessage());
//			returnMap = createOutValue("1", e.getMessage(), "");
		}
		String resultStr = "";
		if (result != null) {
			resultStr = JSON.toJSONString(result,SerializerFeature.WriteMapNullValue,SerializerFeature.WriteNullStringAsEmpty,SerializerFeature.WriteNullNumberAsZero);
			try {
				String str = new String(resultStr.toString().getBytes("utf-8"), "ISO-8859-1");
				out.println(str);
				out.flush();
				out.close();
			} catch (JSONException e) {
				Logger.error("调用接口失败");
			}
			out.println(result);
		} else {
			Logger.error("调用接口失败");
		}
		out.flush();
		out.close();
		Logger.info("==========NCMobileServlet end==========");
		Logger.info("end time : " + new UFDate().toString());
	}

	private JSONObject getServletJson(HttpServletRequest req) {
		JSONObject json = null;
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(req.getInputStream(),
					"utf-8"));
			StringBuffer sb = new StringBuffer("");
			String temp;
			while ((temp = br.readLine()) != null) {
				sb.append(temp);
			}
			br.close();
			String acceptjson = sb.toString();
			if (acceptjson != "") {
				json = JSON.parseObject(acceptjson);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return json;
	}

	private void servletVerify(HttpServletRequest req, HttpServletResponse resp) throws IOException, LoginVerifyException, BusinessException {
		String jSessionId = null;
		if ( req.getCookies() == null ) {
			jSessionId = req.getSession().getId();
		} else {
			for ( Cookie cookie : req.getCookies() ) {
				if ( cookie.getName().equals(SESSION_ID) ) {
					jSessionId = cookie.getValue();
					break;
				}
			}
		}
		
		if ( jSessionId == null || jSessionId.length() == 0 ) {
			Cookie cookie = new Cookie(SESSION_ID, req.getSession().getId());
			resp.addCookie(cookie);
		}
		
		Object objToken = req.getSession().getAttribute("token");
		byte[] token = null;

		if ( objToken == null ) {
			token = createSecurityToken(req.getSession().getId());
			req.getSession().setAttribute("token", token);
		} else {
			token = (byte[])objToken;
			NetStreamContext.setToken(token);
		}
		
	}
	
	private byte[] createSecurityToken(String jSessionId) throws UnsupportedEncodingException, LoginVerifyException, BusinessException {
		LoginVerifyBean verifyBean = new LoginVerifyBean("0");
		verifyBean.setStaticPWDVerify(false);

		//注册认证结果
		ISecurityTokenCallback sc = NCLocator.getInstance().lookup(ISecurityTokenCallback.class);
		return sc.token(verifyBean.getSysID().getBytes("UTF-8"), jSessionId.getBytes("UTF-8"));
	}

	public static LinkedHashMap<String, Object> createOutValue(String flag,
			String desc, Object outObj) {
		LinkedHashMap<String, Object> outValue = new LinkedHashMap<String, Object>();
		outValue.put("flag", flag);
		outValue.put("desc", desc);
		outValue.put("data", outObj);
		return outValue;
	}
}
