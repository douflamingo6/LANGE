package com.lange.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lange.utils.AesCbcUtil;
import com.lange.utils.HttpRequest;

/**
 *
 * @Author linhao Hu
 * @Date
 *
 */
@Controller
@RequestMapping("winxin/")
public class WeixinController {
	/**
	 * @Title: decodeUserInfo       @author：lizheng     
	 * @Description: 解密用户敏感数据
	 * @param encryptedData 明文,加密数据
	 * 
	 * @param iv 加密算法的初始向量
	 * @param code 用户允许登录后，回调内容会带上 code（有效期五分钟），开发者需要将 code 发送到开发者服务器后台，使用code 换取 session_key api，将 code 换成 openid 和
	 *            session_key
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping(value = "decodeUserInfo", method = RequestMethod.POST)
	@ResponseBody
	public Map decodeUserInfo(HttpServletRequest request) {

		String encryptedData = request.getParameter("encryptedData");
		String iv = request.getParameter("iv");
		String code = request.getParameter("code");

		Map map = new HashMap();
		// 登录凭证不能为空
		if (code == null || code.length() == 0) {
			map.put("status", 0);
			map.put("msg", "code 不能为空");
			return map;
		}

		// 小程序唯一标识 (在微信小程序管理后台获取)
		String wxspAppid = "wx18385lalalala";
		// 小程序的 app secret (在微信小程序管理后台获取)
		String wxspSecret = "bef47459d81a6eflalalalala";
		// 授权（必填）
		String grant_type = "authorization_code";

		//////////////// 1、向微信服务器 使用登录凭证 code 获取 session_key 和 openid
		//////////////// 2、对encryptedData加密数据进行AES解密 ////////////////
		try {
			// 请求参数
			String params = "appid=" + wxspAppid + "&secret=" + wxspSecret + "&js_code=" + code + "&grant_type="
					+ grant_type;
			// 发送请求
			String sr = HttpRequest.sendGet("https://api.weixin.qq.com/sns/jscode2session", params);
			// 解析相应内容（转换成json对象）
			JSONObject json = new JSONObject(sr);
			// 获取会话密钥（session_key）
			String session_key = json.get("session_key").toString();
			// 用户的唯一标识（openid）
			String openid = (String) json.get("openid");
			String result = AesCbcUtil.decrypt(encryptedData, session_key, iv, "UTF-8");
			if (null != result && result.length() > 0) {
				map.put("status", 1);
				map.put("msg", "解密成功");

				JSONObject userInfoJSON = new JSONObject(result);
				Map userInfo = new HashMap();
				userInfo.put("openId", userInfoJSON.get("openId"));
				userInfo.put("nickName", userInfoJSON.get("nickName"));
				userInfo.put("gender", userInfoJSON.get("gender"));
				userInfo.put("city", userInfoJSON.get("city"));
				userInfo.put("province", userInfoJSON.get("province"));
				userInfo.put("country", userInfoJSON.get("country"));
				userInfo.put("avatarUrl", userInfoJSON.get("avatarUrl"));
				// 解密unionId & openId;

				userInfo.put("unionId", userInfoJSON.get("unionId"));
				map.put("userInfo", userInfo);
			} else {
				map.put("status", 0);
				map.put("msg", "解密失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
}
