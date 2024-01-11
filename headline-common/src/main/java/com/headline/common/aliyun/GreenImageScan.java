package com.headline.common.aliyun;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.green.model.v20180509.ImageSyncScanRequest;
import com.aliyuncs.http.FormatType;
import com.aliyuncs.http.HttpResponse;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.headline.common.aliyun.util.ClientUploader;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import com.aliyun.green20220302.Client;
import com.aliyun.green20220302.models.*;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import java.util.*;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "aliyun")
public class GreenImageScan {

    private String accessKeyId;
    private String secret;


    public Map imageScan(String imgUrl) throws Exception {

//        String url = null;
//        List<JSONObject> urlList = new ArrayList<JSONObject>();
//        for (byte[] bytes : imageList) {
//            url = clientUploader.uploadBytes(bytes);
//            JSONObject task = new JSONObject();
//            task.put("dataId", UUID.randomUUID().toString());
//            //设置图片链接为上传后的url
//            task.put("url", url);
//            task.put("time", new Date());
//            urlList.add(task);
//        }
//        httpBody.put("tasks", urlList);

        Config config = new Config();
        config.setAccessKeyId(accessKeyId);
        config.setAccessKeySecret(secret);
        config.setRegionId("cn-shanghai");
        config.setEndpoint("green-cip.cn-shanghai.aliyuncs.com");
        // 连接时超时时间，单位毫秒（ms）。
        config.setReadTimeout(6000);
        // 读取时超时时间，单位毫秒（ms）。
        config.setConnectTimeout(3000);
        // 设置http代理。
        // config.setHttpProxy("http://10.10.xx.xx:xxxx");
        // 设置https代理。
        //config.setHttpsProxy("https://10.10.xx.xx:xxxx");
        // 注意，此处实例化的client请尽可能重复使用，避免重复建立连接，提升检测性能。
        Client client = new Client(config);
        RuntimeOptions runtime = new RuntimeOptions();
        runtime.readTimeout = 10000;
        runtime.connectTimeout = 10000;
        Map<String, String> serviceParameters = new HashMap<>();
        serviceParameters.put("imageUrl",imgUrl);
        serviceParameters.put("dataId", UUID.randomUUID().toString());

        ImageModerationRequest request = new ImageModerationRequest();
        // 图片检测service: baselineCheck通用基线检测。
        request.setService("baselineCheck");
        request.setServiceParameters(JSON.toJSONString(serviceParameters));

        try {
            ImageModerationResponse response = client.imageModerationWithOptions(request, runtime);
            // 自动路由。
            if (response != null) {
                // 服务端错误，区域切换到cn-beijing。
                if (500 == response.getStatusCode() || (response.getBody() != null && 500 == (response.getBody().getCode()))) {
                    // 接入区域和地址请根据实际情况修改。
                    config.setRegionId("cn-beijing");
                    config.setEndpoint("green-cip.cn-beijing.aliyuncs.com");
                    client = new Client(config);
                    response = client.imageModerationWithOptions(request, runtime);
                }
            }
            // 打印检测结果。
            if (response != null) {
                if (response.getStatusCode() == 200) {
                    ImageModerationResponseBody body = response.getBody();
                    System.out.println("requestId=" + body.getRequestId());
                    System.out.println("code=" + body.getCode());
                    System.out.println("msg=" + body.getMsg());
                    if (body.getCode() == 200) {
                        ImageModerationResponseBody.ImageModerationResponseBodyData data = body.getData();
                        System.out.println("dataId=" + data.getDataId());
                        List<ImageModerationResponseBody.ImageModerationResponseBodyDataResult> results = data.getResult();
                        for (ImageModerationResponseBody.ImageModerationResponseBodyDataResult result : results) {
                            System.out.println("label=" + result.getLabel());
                            System.out.println("confidence=" + result.getConfidence());
                            return new GreenImageScan().checkPhotoStatus(result.getLabel());
                        }
                    } else {
                        System.out.println("image moderation not success. code:" + body.getCode());
                        return null;
                    }


                } else {
                    System.out.println("response not success. status:" + response.getStatusCode());
                    return null;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    public Map<String,String> checkPhotoStatus(String labels){
        HashMap<String, String> map = new HashMap<>();
        if (labels.equals("nonLabel") ){
            map.put("status",GreenTextScan.AUDIT_BY_PASS);
            return map;
        }
        map.put("status",GreenTextScan.AUDIT_BY_REJECT);
        return map;
    }
}