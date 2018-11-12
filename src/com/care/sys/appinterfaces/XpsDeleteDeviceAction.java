package com.care.sys.appinterfaces;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.care.common.config.ServiceBean;
import com.care.common.http.BaseAction;
import com.care.common.lang.Constant;
import com.care.sys.appuserinfo.domain.AppUserInfo;
import com.care.sys.deviceactiveinfo.domain.DeviceActiveInfo;
import com.care.sys.deviceactiveinfo.domain.logic.DeviceActiveInfoFacade;
import com.care.sys.msginfo.domain.MsgInfo;
import com.care.sys.phoneinfo.domain.PhoneInfo;
import com.care.sys.relativecallinfo.domain.RelativeCallInfo;
import com.care.sys.safearea.domain.SafeArea;
import com.care.sys.settinginfo.domain.SettingInfo;
import com.care.sys.shareinfo.domain.ShareInfo;
import com.godoing.rose.lang.DataMap;
import com.godoing.rose.log.LogFactory;

public class XpsDeleteDeviceAction extends BaseAction{
	
	Log logger = LogFactory.getLog(XpsDeleteDeviceAction.class);
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		JSONObject json = new JSONObject();
		Date start = new Date();
		String href= request.getServletPath();
		String userId = "";
		String belongProject = "";
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try{
			ServletInputStream input = request.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			StringBuffer sb = new StringBuffer();
			String online = "";
			while((online = reader.readLine()) != null){
				sb.append(online);
			}
			JSONObject object = JSONObject.fromObject(sb.toString());
			userId = object.getString("user_id");
			String deviceImei = object.getString("device_imei");
			belongProject = object.getString("belong_project");			
			
			ServiceBean instance = ServiceBean.getInstance();
			DeviceActiveInfo vo = new DeviceActiveInfo();
			String did="";
			vo.setCondition("device_imei='"+deviceImei+"' and belong_project='"+belongProject+"'");
			List<DataMap> listDid=instance.getDeviceActiveInfoFacade().getDeviceActiveInfo(vo);

			ShareInfo shareInfo = new ShareInfo();
			RelativeCallInfo relativeCallInfo = new RelativeCallInfo();
			SafeArea safeArea = new SafeArea();
			SettingInfo settingInfo = new SettingInfo();
			if(listDid.size() > 0){
				String bindCount = "" + listDid.get(0).getAt("bind_count");
				
				if(Integer.parseInt(bindCount) > 1){//多人绑定
					
					vo.setBindCount(Integer.parseInt(bindCount) - 1);
					vo.setCondition("device_imei='"+deviceImei+"' and belong_project='"+belongProject+"'");		
					instance.getDeviceActiveInfoFacade().updateDeviceActiveInfo(vo);
					
					
				} else {
					did=listDid.get(0).get("id")+"";
					vo.setCondition("did='"+did+"'");
					instance.getDeviceActiveInfoFacade().deleteDeviceActiveClockInfo(vo);
					
					vo.setDeviceDisable("0");
					vo.setDeviceAge("2015-12-12");
					vo.setDeviceHead("0");
					vo.setDeviceHeight("170");
					vo.setDeviceName("0");
					vo.setDevicePhone("0");
					vo.setDeviceSex("0");
					vo.setDeviceWeight("170");
					vo.setUserId("0");	
					vo.setShowFirst("0");
					
					vo.setCondition("user_id='"+userId+"' and device_imei='"+deviceImei+"' and belong_project='"+belongProject+"'");		
					instance.getDeviceActiveInfoFacade().updateDeviceActiveInfo(vo);
					
					settingInfo.setCondition("serie_no ='"+deviceImei+"' and belong_project='"+belongProject+"'");
					instance.getSettingInfoFacade().deleteSettingInfo(settingInfo);
					
					String deletePath = request.getSession(true).getServletContext().getRealPath(Constant.DEVICE_SAVE) + deviceImei;
					Constant.deleteFile(deletePath);
					
					PhoneInfo phoneInfo = new PhoneInfo();
					phoneInfo.setStatus("3");
					phoneInfo.setRelativeCallStatus("1");
					phoneInfo.setCondition("serie_no='"+deviceImei+"' and belong_project='"+belongProject+"'");
					instance.getPhoneInfoFacade().updatePhoneInfo(phoneInfo);
					
				}
				AppUserInfo appUser = new AppUserInfo();
				appUser.setCondition("id='" + userId + "'");
				List<DataMap> userList = instance.getAppUserInfoFacade().getAppUserInfo(appUser);
				String userBindCount = "" + userList.get(0).get("bind_count");
				int bCount = Integer.parseInt(userBindCount) -1;
				appUser.setBindCount(String.valueOf(bCount));
				instance.getAppUserInfoFacade().updateAppUserInfo(appUser);
				
				vo.setCondition("user_id='" + userId + "' and device_id='" + deviceImei 
						+ "' and belong_project='" + belongProject + "'");
				instance.getDeviceActiveInfoFacade().deleteUserBindDevice(vo);
				
				relativeCallInfo.setCondition("user_id ='"+userId+"' and serie_no ='"+deviceImei+"' and belong_project='"+belongProject+"'");
				instance.getRelativeCallInfoFacade().deleteRelativeCallInfo(relativeCallInfo);
				
				safeArea.setCondition("user_id ='"+userId+"' and seri_no ='"+deviceImei+"' and belong_project='"+belongProject+"'");
				instance.getSafeAreaFacade().deleteSafeArea(safeArea);
				
				shareInfo.setCondition("user_id ='"+userId+"' and device_imei='"+deviceImei+"' and belong_project='"+belongProject+"'");
				instance.getShareInfoFacade().deleteShareInfo(shareInfo);
				
				MsgInfo msgInfo = new MsgInfo();
				msgInfo.setCondition("to_id ='"+userId+"' and belong_project ='"+belongProject+"' and msg_content like '%"+deviceImei+"%'");
				ServiceBean.getInstance().getMsgInfoFacade().deleteMsgInfo(msgInfo);
				
				List<DataMap> deviceList = instance.getDeviceActiveInfoFacade().getDeviceActiveInfo(vo);
				String deviceName = "";
				if(deviceList.size() > 0){
					deviceName = (String)deviceList.get(0).getAt("device_name");
				}
				
				msgInfo.setToId(userId);
				msgInfo.setFromId(userId);
				msgInfo.setIsHandler("0");
				msgInfo.setMsgLevel("1");
				msgInfo.setMsgContent("1@" + deviceImei + "@" + deviceName);
				msgInfo.setMsgHandlerDate(new Date());
				msgInfo.setBelongProject(belongProject);
				msgInfo.setMsgOccurDate(new Date());
				
				ServiceBean.getInstance().getMsgInfoFacade().insertMsgInfo(msgInfo);
				getMessageFrom(json, userId, belongProject);
				
				DeviceActiveInfoFacade facade = ServiceBean.getInstance().getDeviceActiveInfoFacade();
				DeviceActiveInfo da = new DeviceActiveInfo();
				da.setCondition("serie_no='"+deviceImei+" and user_id='" + userId + "' and belong_project='"+belongProject+"' and status='1' order by id desc");
				List<DataMap> das = facade.getDeviceActiveHistory(da);
				if(das.size() > 0){
					Integer id = (Integer)das.get(0).getAt("id");
					String unbindTime = das.get(0).getAt("unbind_time")+"";
					if(unbindTime == null || unbindTime.equals("")){
						da.setUnbindTime(new Date());
						da.setCondition("id='"+id+"'");
						facade.updateDeviceActiveHistory(da);
					}
				}
				
				DeviceActiveInfo v = new DeviceActiveInfo();
				v.setDeviceImei(deviceImei);
				v.setBelongProject(belongProject);
				v.setUserId(userId);
				v.setDeviceDisable("0");
				v.setDeviceUpdateTime(new Date());
				facade.insertDeviceActiveHistory(v);
			
				result = Constant.SUCCESS_CODE;
				json.put("request", href);
			}
			
		}catch(Exception e){
			e.printStackTrace();	
			StringBuffer sb = new StringBuffer();
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			Throwable cause = e.getCause();		
			while (cause != null) {
				cause.printStackTrace(printWriter);
				cause = cause.getCause();
			}
			printWriter.close();
			String resultSb = writer.toString();
			sb.append(resultSb);
			
			logger.error(e);
			result = Constant.EXCEPTION_CODE;
			json.put(Constant.EXCEPTION, sb.toString());
		}
		json.put(Constant.RESULTCODE, result);
	//	insertVisit(href,belongProject,userId,0,start,new Date(),json.toString().getBytes("utf-8").length);
//		insertVisitReason(href, belongProject, "解除绑定设备", userId, 0, start, new Date(),json.toString().getBytes("utf-8").length);
		response.setCharacterEncoding("UTF-8");	
		response.getWriter().write(json.toString());
		return null;	
		}

}
