package org.paasta.container.platform.api.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import org.paasta.container.platform.api.common.model.CommonStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Common Service 클래스
 *
 * @author hrjin
 * @version 1.0
 * @since 2020.08.26
 */
@Service
public class CommonService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonService.class);
    private final Gson gson;

    /**
     * Instantiates a new Common service.
     *
     * @param gson the gson
     */
    @Autowired
    public CommonService(Gson gson) {this.gson = gson;}


    /**
     * Sets result model.
     *
     * @param reqObject  the req object
     * @param resultCode the result code
     * @return the result model
     */
    public Object setResultModel(Object reqObject, String resultCode) {
        try {
            Class<?> aClass = reqObject.getClass();
            ObjectMapper oMapper = new ObjectMapper();
            Map map = oMapper.convertValue(reqObject, Map.class);

            Method methodSetResultCode = aClass.getMethod("setResultCode", String.class);
            Method methodSetResultMessage = aClass.getMethod("setResultMessage", String.class);
            Method methodSetHttpStatusCode = aClass.getMethod("setHttpStatusCode", Integer.class);
            Method methodSetDetailMessage = aClass.getMethod("setDetailMessage", String.class);

            if (Constants.RESULT_STATUS_FAIL.equals(map.get("resultCode"))) {
                methodSetResultCode.invoke(reqObject, map.get("resultCode"));
            } else {
                methodSetResultCode.invoke(reqObject, resultCode);
                methodSetResultMessage.invoke(reqObject, CommonStatusCode.OK.getMsg());
                methodSetHttpStatusCode.invoke(reqObject, CommonStatusCode.OK.getCode());
                methodSetDetailMessage.invoke(reqObject, CommonStatusCode.OK.getMsg());
            }

        } catch (NoSuchMethodException e) {
            LOGGER.error("NoSuchMethodException :: {}", e);
        } catch (IllegalAccessException e1) {
            LOGGER.error("IllegalAccessException :: {}", e1);
        } catch (InvocationTargetException e2) {
            LOGGER.error("InvocationTargetException :: {}", e2);
        }

        return reqObject;
    }

    /**
     * 생성/수정/삭제 후 페이지 이동을 위한 Sets result model
     *
     * @param reqObject
     * @param resultCode
     * @param nextActionUrl
     * @return
     */
    public Object setResultModelWithNextUrl(Object reqObject, String resultCode, String nextActionUrl) {
        try {
            Class<?> aClass = reqObject.getClass();
            ObjectMapper oMapper = new ObjectMapper();
            Map map = oMapper.convertValue(reqObject, Map.class);

            Method methodSetResultCode = aClass.getMethod("setResultCode", String.class);
            Method methodSetNextActionUrl = aClass.getMethod("setNextActionUrl", String.class);

            if (Constants.RESULT_STATUS_FAIL.equals(map.get("resultCode"))) {
                methodSetResultCode.invoke(reqObject, map.get("resultCode"));
            } else {
                methodSetResultCode.invoke(reqObject, resultCode);
            }

            if(nextActionUrl != null) {
                methodSetNextActionUrl.invoke(reqObject, nextActionUrl);
            }

        } catch (NoSuchMethodException e) {
            LOGGER.error("NoSuchMethodException :: {}", e);
        } catch (IllegalAccessException e1) {
            LOGGER.error("IllegalAccessException :: {}", e1);
        } catch (InvocationTargetException e2) {
            LOGGER.error("InvocationTargetException :: {}", e2);
        }

        return reqObject;
    }

    /**
     * Sets result object.
     *
     * @param <T>           the type parameter
     * @param requestObject the request object
     * @param requestClass  the request class
     * @return the result object
     */
    public <T> T setResultObject(Object requestObject, Class<T> requestClass) {
        return this.fromJson(this.toJson(requestObject), requestClass);
    }


    /**
     * To json string.
     *
     * @param requestObject the request object
     * @return the string
     */
    private String toJson(Object requestObject) {
        return gson.toJson(requestObject);
    }


    /**
     * From json t.
     *
     * @param <T>           the type parameter
     * @param requestString the request string
     * @param requestClass  the request class
     * @return the t
     */
    private <T> T fromJson(String requestString, Class<T> requestClass) {
        return gson.fromJson(requestString, requestClass);
    }

    /**
     * 서로 다른 객체를 매핑한다.
     *
     * @param instance
     * @param targetClass
     * @param <A>
     * @param <B>
     * @return
     * @throws Exception
     */
    public static <A,B> B convert(A instance, Class<B> targetClass) throws Exception {
        B target = targetClass.newInstance();

        for (Field targetField : targetClass.getDeclaredFields()) {
            Field[] instanceFields = instance.getClass().getDeclaredFields();

            for (Field instanceField : instanceFields) {
                if(targetField.getName().equals(instanceField.getName())) {
                    targetField.set(target, instance.getClass().getDeclaredField(targetField.getName()).get(instance));
                }
            }
        }
        return target;
    }


    /**
     * Token으로 Admin인지 판별
     *
     * @param user
     * @return
     */
    public static boolean isAdminPortal(User user) {
        return user.getAuthorities().contains(new SimpleGrantedAuthority(Constants.AUTH_CLUSTER_ADMIN));
    }



    /**
     * JsonPath를 통해 문자열 필터 처리
     *
     * @param responseMap
     * @param searchParam
     * @return
     */
    public HashMap searchKeywordForResourceName(HashMap responseMap, String searchParam) {
        List filterResourceItemList = JsonPath.parse(responseMap).read("$..items[?(@.metadata.name =~ /.*" + searchParam + ".*/i)]");
        responseMap.put("items", filterResourceItemList);
        return responseMap;
    }


    /**
     * offset & limit을 통한 리스트 가공 처리
     *
     * @param itemList
     * @param offset
     * @param limit
     * @return
     */
    public <T> List<T> listProcessingforLimit(List<T> itemList, Integer offset, Integer limit) {
        if (offset<0) throw new IllegalArgumentException("Offset must be >=0 but was "+offset+"!");
        List returnList = itemList.stream().skip(offset*limit).limit(limit).collect(Collectors.toList());
        return returnList;
    }
}
