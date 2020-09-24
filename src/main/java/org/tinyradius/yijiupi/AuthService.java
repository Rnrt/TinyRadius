package org.tinyradius.yijiupi;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;
import java.util.Map;

public class AuthService {

    private static final HttpClient httpClient = HttpClients.createDefault();
    private static final Gson gson = new Gson();
    public static final String OA_BASE_URL = "https://ua2.yijiupi.com/himalaya-ApiService-UA2/user/jwt/login";
    public static final String APP_CODE = "AC_ROUTER_AUTH";

    public static AuthResult oaAuth(String username, String password) {
        try {
            HttpPost httpPost = new HttpPost(OA_BASE_URL);
            httpPost.setEntity(new StringEntity(createOaLoginData(username, password), ContentType.APPLICATION_JSON));
            HttpResponse response = httpClient.execute(httpPost);
            Map<String, Object> data = gson.fromJson(EntityUtils.toString(response.getEntity()), new TypeToken<Map<String, Object>>() {
            }.getType());
            boolean success = (Boolean) data.get("success");
            return success ? AuthResult.create(true, "用户 " + username + " 登录成功") : AuthResult.fail("账号验证失败");
        } catch (Exception e) {
            e.printStackTrace();
            return AuthResult.fail(e.getMessage());
        }
    }

    public static  AuthResult tempAccountAuth(){
        return AuthResult.success();
    }
    private static String createOaLoginData(String username, String password) {
        Map<String, String> data = new HashMap<>();
        data.put("appCode", APP_CODE);
        data.put("mobileNo", username);
        data.put("password", password);
        return gson.toJson(data);
    }

}
