package org.paasta.container.platform.api.common.util;

import org.paasta.container.platform.api.common.Constants;
import org.paasta.container.platform.api.common.PropertyService;
import org.paasta.container.platform.api.common.RestTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author hrjin
 * @version 1.0
 * @since 2020-08-26
 **/
public class InspectionUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(InspectionUtil.class);


    /**
     * Bean을 찾아서 주입
     *
     * @param bean
     * @return
     */
    public static Object getBean(String bean) {
        ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        return applicationContext.getBean(bean);
    }



    /**
     * 필드 명으로 propertyService의 create, update 메소드 명 생성
     *
     * @param fieldName
     * @param suffix
     * @return
     */
    public static String makeMethodName(String fieldName, String suffix) {
        return "getCpMasterApiList" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1) + "s" + suffix;
    }


    /**
     *
     * yaml에서 쓰는 kind(Resource)에 맞게 이름 생성
     * 예시) deployments -> Deployment
     *
     * @param resourceName
     * @return
     */
    public static String makeResourceName(String resourceName) {
        resourceName = resourceName.substring(0, 1).toUpperCase()+resourceName.substring(1).toLowerCase();
        return resourceName.substring(0, resourceName.length()-1);
    }


    /**
     * requestURI 파싱  /namespaces/cp-namespace/deployments    , /namespaces/cp-namespace/deployments/deploymentsName
     */
    public static String[] parsingRequestURI(String requestURI) {

        String[] pathArray = requestURI.split("/");

        return pathArray;
    }


    /**
     * dryRun 체크를 위한 동적 API URL Call 메서드 조회
     *
     * @param methodType
     * @param kind
     * @return
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static String verifyMethodCall(String methodType, String kind) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String methodName = makeMethodName(kind.trim(), methodType);
        PropertyService propertyService = (PropertyService) getBean("propertyService");

        // 해당 Resource의 method 이름
        Method method = propertyService.getClass().getDeclaredMethod(methodName);
        LOGGER.info("Method Name >>> " + methodName);

        // 동적 K8s API Endpoint
        Object recursiveObj = method.invoke(propertyService, null);
        String finalUrl = recursiveObj.toString();
        LOGGER.info("K8s API Endpoint >>> " + finalUrl);

        return finalUrl;
    }

    /**
     * dryRun 체크 (create/update)
     *
     * @param namespace
     * @param kind
     * @param yaml
     * @return
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static Object resourceDryRunCheck(String methodType, String namespace, String kind, String yaml, String resourceName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        RestTemplateService restTemplateService = (RestTemplateService) getBean("restTemplateService");
        String finalUrl = verifyMethodCall(methodType, kind);

        if(resourceName == null) {
            return restTemplateService.sendYaml(Constants.TARGET_CP_MASTER_API,  finalUrl.replace("{namespace}", namespace) + "?dryRun=All", HttpMethod.POST, yaml, Map.class);
        } else {
            return restTemplateService.sendYaml(Constants.TARGET_CP_MASTER_API,  finalUrl.replace("{namespace}", namespace).replace("{name}", resourceName) + "?dryRun=All", HttpMethod.PUT, yaml, Map.class);
        }

    }


}