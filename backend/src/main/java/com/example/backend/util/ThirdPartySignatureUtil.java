package com.example.backend.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Component
public class ThirdPartySignatureUtil {

    private static final String CHARSET = "UTF-8";
    private static final String ALGORITHM_HMAC_SHA1 = "HmacSHA1";
    private static final String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";
    private static final String TIME_ZONE = "Asia/Shanghai";

    public String generateTimestamp() {
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT_STRING);
        df.setTimeZone(java.util.TimeZone.getTimeZone(TIME_ZONE));
        return df.format(new Date());
    }

    public String specialUrlEncode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, CHARSET).replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
    }

    public String buildSignString(String url, String method, Map<String, Object> parasMap, String appKey, String timestamp) throws UnsupportedEncodingException {
        TreeMap<String, Object> sortParasMap = new TreeMap<>(parasMap);
        sortParasMap.put("appKey", appKey);
        sortParasMap.put("timestamp", timestamp);

        StringBuilder sortQueryStringTmp = new StringBuilder();
        Iterator<String> it = sortParasMap.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            sortQueryStringTmp.append("&").append(specialUrlEncode(key)).append("=").append(specialUrlEncode(String.valueOf(sortParasMap.get(key))));
        }
        String sortedQueryString = sortQueryStringTmp.substring(1);

        return method.toUpperCase() + "&" + specialUrlEncode(url) + "&" + specialUrlEncode(sortedQueryString);
    }

    public String sign(String appSecret, String stringToSign) throws Exception {
        SecretKeySpec signingKey = new SecretKeySpec((appSecret + "&").getBytes(CHARSET), ALGORITHM_HMAC_SHA1);
        Mac mac = Mac.getInstance(ALGORITHM_HMAC_SHA1);
        mac.init(signingKey);
        byte[] signData = mac.doFinal(stringToSign.getBytes(CHARSET));
        return java.util.Base64.getEncoder().encodeToString(signData);
    }

    public String generateSignature(String httpMethod, String path, Map<String, Object> queryParams, String appKey, String appSecret, String timestamp) throws Exception {
        String signString = buildSignString(path, httpMethod, queryParams, appKey, timestamp);
        return sign(appSecret, signString);
    }
}
