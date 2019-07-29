package nc.jzmobile.utils;

import java.io.IOException;

import nc.bs.dao.DAOException;
import nc.bs.logging.Logger;
import nc.bs.trade.business.HYPubBO;
import nc.jzmobile.app.impl.MobileBillDetailQueryImpl;
import nc.jzmobile.consts.MessageTypeConst;
import nc.uif.pub.exception.UifException;
import nc.vo.jzmobile.app.MobilePropModel;
import nc.vo.sm.UserVO;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONObject;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;

public class MobileMessageUtil {
	/**
	 * 给移动端发送消息（APP消息推送，微信消息推送）
	 * @param content 消息内容
	 * @param usercode 收消息人的code（支持多个，“,”分割）
	 * @param redirect 消息是否连接   0链接，1不连接
	 * @param messageUrl 消息的连接地址
	 * ***/
	public static void sendMobileMessage(String content,UserVO user,String redirect,String messageUrl) throws Exception{
		MobilePropModel propModel = new MobilePropertiesLoader().getViewResolverProperties();
		String messageType = propModel.getMessageType();
		String typeArray[] = messageType.split(",");
		for(String type:typeArray){
			
			if(MessageTypeConst.TYPE_MA.equals(type)){
				String maserverUrl = propModel.getMaServerURL()+propModel.getMaServerMessageURL();
//				if(messageUrl!=null&&!"".equals(messageUrl))
//					messageUrl = messageUrl.replaceFirst("&", "&srctype=0&");
				sendMobileServerMessage(maserverUrl, propModel.getAppShowName(), propModel.getMaAppId(), content, propModel.getImCodePrefix()+user.getUser_code(), redirect, messageUrl);
			}
			if(MessageTypeConst.TYPE_PLATFORM.equals(type)){
//				if(messageUrl!=null&&!"".equals(messageUrl))
//					messageUrl = messageUrl.replaceFirst("&", "&srctype=1&");
				String maserverUrl = propModel.getMaServerURL()+propModel.getMaServerMessageURL();
				sendMobileServerMessage(maserverUrl, propModel.getAppShowName(), propModel.getMaAppId(), content, propModel.getImCodePrefix()+user.getUser_code(), redirect, messageUrl);
			}
			if(MessageTypeConst.TYPE_WECHAT.equals(type)){
//				if(messageUrl!=null&&!"".equals(messageUrl))
//					messageUrl = messageUrl.replaceFirst("&", "&srctype=2&");
				String weChatUrl = propModel.getWeChatServerURL()+propModel.getWeChatServerMessageURL();
				sendMobileWeChatMessage(weChatUrl,user.getCuserid(),content,messageUrl);
			}
			
			if(MessageTypeConst.TYPE_ICOP.equals(type)){
				//针对许昌腾飞的icop平台进行消息服务对接
				String icopUrl = propModel.getMaServerURL()+propModel.getMaServerMessageURL();
				sendIcopMobileServerMessage(icopUrl, propModel.getAppShowName(), propModel.getMaAppId(), content, user.getUser_code(), redirect, messageUrl,propModel.getTenantid());
			}
			
			//轻推发送消息
			if(MessageTypeConst.TYPE_QINGTUI.equals(type)){
				
				String qingTuiUrl = propModel.getQingTuiServerURL()+propModel.getQingTuiServerMessageURL();
				sendMobileQingTuiMessage(qingTuiUrl,user.getCuserid(),content,messageUrl);
				
			}
		}
		
	}
	private static void sendIcopMobileServerMessage(String sendMessageUrl,String from,String appid,String content,String usercode,String redirect,String messageUrl,String tenantid) throws ParseException, IOException{
		HttpPost httpPost = new HttpPost(sendMessageUrl);
		Logger.debug("maserverUrl = " + sendMessageUrl);
		JSONObject paramJson = new JSONObject();
		paramJson.put("msgtype", "0");
		String[] strs={usercode};
		paramJson.put("target", strs);
		paramJson.put("from", "approve");
		paramJson.put("target_type", "users");
		paramJson.put("msg", content);
		paramJson.put("appid", appid);
		paramJson.put("redirect",redirect);
		paramJson.put("url", messageUrl);
		JSONObject ext = new JSONObject();
		ext.put("key1", "val111");
		paramJson.put("ext", ext);
		StringEntity se = new StringEntity(paramJson.toString(), "UTF-8");
		Logger.debug(EntityUtils.toString(se));
		httpPost.setEntity(se);
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpPost.addHeader("tenantid", tenantid);
		try{
			HttpResponse response = httpClient.execute(httpPost);
			String callback = EntityUtils.toString(response.getEntity(),"UTF-8");
			Logger.debug("发送移动审批消息返回信息："+callback);
			int status = response.getStatusLine().getStatusCode();
			if (status != 200) {
				Logger.error("发送移动审批消息提醒失败！");
			} else {// 成功
				Logger.error("发送移动审批消息提醒成功！");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
	}
	
	private static void sendMobileServerMessage(String sendMessageUrl,String from,String appid,String content,String usercode,String redirect,String messageUrl) throws Exception{
		HttpPost httpPost = new HttpPost(sendMessageUrl);
		Logger.debug("maserverUrl = " + sendMessageUrl);
		JSONObject httpJson = new JSONObject();
		httpJson.put("content", content);
		httpJson.put("from", from);

		httpJson.put("to", usercode);
		httpJson.put("appid", appid);
		httpJson.put("redirect", redirect);
		httpJson.put("url", messageUrl);

		Logger.debug("参数列表：params="+httpJson.toString());
		httpPost.addHeader(HTTP.CONTENT_TYPE, "application/json");
		StringEntity se = new StringEntity("params="+httpJson.toString(), "UTF-8");
		Logger.debug(EntityUtils.toString(se));
		httpPost.setEntity(se);
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(httpPost);
		String callback = EntityUtils.toString(response.getEntity(),"UTF-8");
		Logger.debug("发送移动审批消息返回信息："+callback);
		int status = response.getStatusLine().getStatusCode();
		if (status != 200) {
			Logger.error("发送移动审批消息提醒失败！");
		} else {// 成功
			Logger.error("发送移动审批消息提醒成功！");
		}
	}
	private static void sendMobileWeChatMessage(String sendMessageUrl,String userId,String content,String messageUrl) throws Exception{
		HttpPost httpPost = new HttpPost(sendMessageUrl);
		Logger.debug("WeChatUrl = " + sendMessageUrl);
		JSONObject httpJson = new JSONObject();
		httpJson.put("userId", userId);
		httpJson.put("message", content);
		httpJson.put("url", messageUrl.replace("?appid", "?srctype=2&appid"));
		httpJson.put("serviceType", "APPROVE");
		Logger.debug("参数列表：params="+httpJson.toString());
		httpPost.addHeader(HTTP.CONTENT_TYPE, "application/json");
		StringEntity se = new StringEntity(httpJson.toString(), "UTF-8");
		Logger.debug(EntityUtils.toString(se));
		httpPost.setEntity(se);
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(httpPost);
		String callback = EntityUtils.toString(response.getEntity(),"UTF-8");
		Logger.debug("发送微信消息返回信息："+callback);
		int status = response.getStatusLine().getStatusCode();
		if (status != 200) {
			Logger.error("发送微信消息提醒失败！");
		} else {// 成功
			Logger.error("发送微信消息提醒成功！");
		}
	}
	
	
	private static void sendMobileQingTuiMessage(String sendMessageUrl,String userId,String content,String messageUrl) throws Exception{
		HttpPost httpPost = new HttpPost(sendMessageUrl);
		Logger.debug("QingTuiUrl = " + sendMessageUrl);
		JSONObject httpJson = new JSONObject();
		httpJson.put("userId", userId);
		httpJson.put("message", content);
		httpJson.put("url", messageUrl.replace("?appid", "?srctype=4&appid"));
		httpJson.put("serviceType", "APPROVE");
		Logger.debug("参数列表：params="+httpJson.toString());
		httpPost.addHeader(HTTP.CONTENT_TYPE, "application/json");
		StringEntity se = new StringEntity(httpJson.toString(), "UTF-8");
		Logger.debug(EntityUtils.toString(se));
		httpPost.setEntity(se);
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(httpPost);
		String callback = EntityUtils.toString(response.getEntity(),"UTF-8");
		Logger.debug("发送轻推消息返回信息："+callback);
		int status = response.getStatusLine().getStatusCode();
		if (status != 200) {
			Logger.error("发送轻推消息提醒失败！");
		} else {// 成功
			Logger.error("发送轻推消息提醒成功！");
		}
	}
	
	
	public static String getOABillTempletPkByBillType(String billtype, String tprefix) throws DAOException {
		String pk_billtemplet = null;
		try {
			HYPubBO bo = new HYPubBO();
			String strwhere = " isnull(dr,0)=0 and bill_templetname = 'SYSTEM' and pk_billtypecode='"
					+ tprefix + billtype + "'";
				Object result = bo.findColValue("pub_billtemplet", "pk_billtemplet", strwhere);
				pk_billtemplet = result != null ? result.toString() : null;
		} catch (UifException e) {
			e.printStackTrace();
		}
		return pk_billtemplet;
	}
	
	public static boolean judgeIsOABill(String billtype)  {
		try {
			billtype = BillTypeModelTrans.getInstance().getModelByBillType(billtype).getBillTypeCode();
			String pk_billtemplet = getOABillTempletPkByBillType(billtype, MobileBillDetailQueryImpl.TEMPLATE_PREFIX);
			if (!(pk_billtemplet == null || "".equals(pk_billtemplet))) {
				return true;
			}
		} catch (Exception e) {
			Logger.error(e);
		}
		return false;
	}
	
	
	public static void main(String[] args) throws Exception {

        sendMobileWeChatMessage("http://127.0.0.1:8090/jzmobile-wechat-web/wechat/send_message", "1001C410000000000QEL", "来自用友建筑微信服务的问候！", "https://www.baidu.com/");

    }
	
	
}
