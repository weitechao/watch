package com.care.sys.appinterfaces;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
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
import com.care.sys.deviceactiveinfo.domain.DeviceActiveInfo;
import com.care.sys.directiveinfo.domain.DirectiveInfo;
import com.care.sys.directiveinfo.domain.logic.DirectiveInfoFacade;
import com.godoing.rose.lang.DataMap;
import com.godoing.rose.log.LogFactory;

public class getSleepAction extends BaseAction {
	Log logger = LogFactory.getLog(getSleepAction.class);

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		String href = request.getServletPath();
		Date start = new Date();
		JSONObject json = new JSONObject();
		String user_id = "";
		String belongProject = "";
		try {
			ServletInputStream input = request.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					input));
			StringBuffer sb = new StringBuffer();
			String online = "";
			while ((online = reader.readLine()) != null) {
				sb.append(online);
			}
			JSONObject object = JSONObject.fromObject(sb.toString());
			user_id = object.has("user_id") ? object
					.getString("user_id") : "-1";
			String serial_number = object.getString("serie_no");
			belongProject = object.getString("belong_project");

			if (user_id.equals("-1")) {
				DeviceActiveInfo deviceActiveInfo = new DeviceActiveInfo();
				deviceActiveInfo.setCondition("device_imei ='" + serial_number
						+ "' and belong_project='" + belongProject + "'"); // 判定条件
				List<DataMap> deviceList = ServiceBean.getInstance()
						.getDeviceActiveInfoFacade()
						.getDeviceActiveInfo(deviceActiveInfo);
				int size = deviceList.size();
				if (size > 0) {
					user_id = "" + deviceList.get(0).getAt("user_id");
				}
			}

			DirectiveInfo vo = new DirectiveInfo();
			vo.setCondition("serie_no = '" + serial_number
					+ "' and belong_project='" + belongProject + "'");
			DirectiveInfoFacade facade = ServiceBean.getInstance()
					.getDirectiveInfoFacade();
			json.put("request", href);
			List<DataMap> list = facade.getDirectiveInfo(vo);
			if (list.size() > 0) {
				String clock = (String) list.get(0).getAt("sleep");
				if (clock != null && !"".equals(clock) && !" ".equals(clock)) {
					json.put("clock", clock);
					result = 1;
				} else {
					result = 0;
				}
			} else {
				result = 0;
			}
			
		} catch (Exception e) {
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
		insertVisit(href,belongProject,user_id,0,start,new Date(),json.toString().getBytes("utf-8").length);
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json.toString());
		return null;
	}

}
