package nc.jzmobile.utils;

import nc.bs.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONObject;

/**
 * ���ö��̷߳�ʽ���з�����Ϣ
 * @author mxx
 *
 */
public class SendMessageThread implements Runnable{

	private String sendMessageUrl;
	private String from;
	private String appid;
	private String content;
	private String usercode;
	private String redirect;
	private String messageUrl;
	
	public SendMessageThread(String sendMessageUrl, String from, String appid,
			String content, String usercode, String redirect, String messageUrl) {
		super();
		this.sendMessageUrl = sendMessageUrl;
		this.from = from;
		this.appid = appid;
		this.content = content;
		this.usercode = usercode;
		this.redirect = redirect;
		this.messageUrl = messageUrl;
	}


	@Override
	public void run() {
		
		try {
			Logger.debug("�̷߳�����Ϣ��ʼ ================ " );
			sendMobileServerMessage(sendMessageUrl,from,appid,content,usercode,redirect,messageUrl);
			Logger.debug("�̷߳�����Ϣ���� ================ " );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	private void sendMobileServerMessage(String sendMessageUrl,String from,String appid,String content,String usercode,String redirect,String messageUrl) throws Exception{
		HttpPost httpPost = new HttpPost(sendMessageUrl);
		Logger.debug("maserverUrl = " + sendMessageUrl);
		JSONObject httpJson = new JSONObject();
		httpJson.put("content", content);
		httpJson.put("from", from);

		httpJson.put("to", usercode);
		httpJson.put("appid", appid);
		httpJson.put("redirect", redirect);
		httpJson.put("url", messageUrl);

		Logger.debug("�����б�params="+httpJson.toString());
		httpPost.addHeader(HTTP.CONTENT_TYPE, "application/json");
		StringEntity se = new StringEntity("params="+httpJson.toString(), "UTF-8");
		Logger.debug(EntityUtils.toString(se));
		httpPost.setEntity(se);
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(httpPost);
		String callback = EntityUtils.toString(response.getEntity(),"UTF-8");
		Logger.debug("�����ƶ�������Ϣ������Ϣ��"+callback);
		int status = response.getStatusLine().getStatusCode();
		if (status != 200) {
			Logger.error("�����ƶ�������Ϣ����ʧ�ܣ�");
		} else {// �ɹ�
			Logger.error("�����ƶ�������Ϣ���ѳɹ���");
		}
	}

}
