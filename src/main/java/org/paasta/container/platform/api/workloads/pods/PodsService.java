package org.paasta.container.platform.api.workloads.pods;

import org.paasta.container.platform.api.common.CommonService;
import org.paasta.container.platform.api.common.Constants;
import org.paasta.container.platform.api.common.PropertyService;
import org.paasta.container.platform.api.common.RestTemplateService;
import org.paasta.container.platform.api.common.model.ResultStatus;
import org.paasta.container.platform.api.customServices.CustomServices;
import org.paasta.container.platform.api.workloads.deployments.Deployments;
import org.paasta.container.platform.api.workloads.deployments.DeploymentsAdmin;
import org.paasta.container.platform.api.workloads.deployments.DeploymentsList;
import org.paasta.container.platform.api.workloads.deployments.DeploymentsListAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


/**
 * Pods Service 클래스
 *
 * @author hrjin
 * @version 1.0
 * @since 2020.09.09
 */
@Service
public class PodsService {
    private final RestTemplateService restTemplateService;
    private final CommonService commonService;
    private final PropertyService propertyService;

    /**
     * Instantiates a new Pods service
     *
     * @param restTemplateService the rest template service
     * @param commonService       the common service
     * @param propertyService     the property service
     */
    @Autowired
    public PodsService(RestTemplateService restTemplateService, CommonService commonService, PropertyService propertyService) {
        this.restTemplateService = restTemplateService;
        this.commonService = commonService;
        this.propertyService = propertyService;
    }

    /**
     * Pods 목록 조회(Get Pods list)
     *
     * @param namespace the namespace
     * @param limit the limit
     * @param continueToken the continueToken
     * @return the pods list
     */
    public PodsList getPodsList(String namespace, int limit, String continueToken) {
        String param = "";

        if (continueToken != null) {
            param = "&continue=" + continueToken;
        }

        HashMap responseMap = (HashMap) restTemplateService.send(Constants.TARGET_CP_MASTER_API,
                propertyService.getCpMasterApiListPodsListUrl()
                        .replace("{namespace}", namespace) + "?limit=" + limit + param
                , HttpMethod.GET, null, Map.class);

        return (PodsList) commonService.setResultModel(commonService.setResultObject(responseMap, PodsList.class), Constants.RESULT_STATUS_SUCCESS);
    }


    /**
     * Pods 목록 조회(Get Pods list)
     *(Admin Portal)
     *
     * @param namespace the namespace
     * @param limit the limit
     * @param continueToken the continueToken
     * @param searchParam the searchParam
     * @return the deployments list
     */
    public Object getPodsListAdmin(String namespace, int limit, String continueToken, String searchParam) {
        String param = "";
        HashMap responseMap = null;

        if (continueToken != null) {
            param = "&continue=" + continueToken;
        }

        Object response = restTemplateService.send(Constants.TARGET_CP_MASTER_API,
                propertyService.getCpMasterApiListPodsListUrl()
                        .replace("{namespace}", namespace) + "?limit=" + limit + param, HttpMethod.GET, null, Map.class);

        try {
            responseMap = (HashMap) response;
        } catch (Exception e) {
            return response;
        }

        return commonService.setResultModel(commonService.setResultObject(responseMap, PodsListAdmin.class), Constants.RESULT_STATUS_SUCCESS);
    }

    /**
     * Pods 목록 조회(Get Pods selector)
     *
     * @param namespace the namespace
     * @param selector  the selector
     * @return the pods list
     */
    PodsList getPodListWithLabelSelector(String namespace, String selector) {
        String requestSelector = "?labelSelector=" + selector;
        HashMap resultMap = (HashMap) restTemplateService.send(Constants.TARGET_CP_MASTER_API,
                propertyService.getCpMasterApiListPodsListUrl().replace("{namespace}", namespace) + requestSelector, HttpMethod.GET, null, Map.class);

        return (PodsList) commonService.setResultModel(commonService.setResultObject(resultMap, PodsList.class), Constants.RESULT_STATUS_SUCCESS);
    }

    /**
     * Pods 목록 조회(Get Pods node)
     *
     * @param namespace the namespace
     * @param nodeName the node name
     * @return the pods list
     */
    PodsList getPodListByNode(String namespace, String nodeName) {
        String requestURL = propertyService.getCpMasterApiListPodsListUrl().replace("{namespace}", namespace)
                + "/?fieldSelector=spec.nodeName=" + nodeName;

        HashMap resultMap = (HashMap) restTemplateService.send(Constants.TARGET_CP_MASTER_API, requestURL,
                HttpMethod.GET, null, Map.class);
        return (PodsList) commonService.setResultModel(commonService.setResultObject(resultMap, PodsList.class), Constants.RESULT_STATUS_SUCCESS);
    }

    /**
     * Pods 상세 조회(Get Pods detail)
     *
     * @param namespace the namespace
     * @param podsName the pods name
     * @return the pods detail
     */
    public Pods getPods(String namespace, String podsName) {
        HashMap responseMap = (HashMap) restTemplateService.send(Constants.TARGET_CP_MASTER_API,
                propertyService.getCpMasterApiListPodsGetUrl().replace("{namespace}", namespace).replace("{name}", podsName),
                HttpMethod.GET, null, Map.class);

        return (Pods) commonService.setResultModel(commonService.setResultObject(responseMap, Pods.class), Constants.RESULT_STATUS_SUCCESS);
    }

    /**
     * Pods 상세 조회(Get Pods detail)
     * (Admin Portal)
     *
     * @param namespace the namespace
     * @param podsName the pods name
     * @return the pods detail
     */
    public Object getPodsAdmin(String namespace, String podsName) {
        Object obj = restTemplateService.sendAdmin(Constants.TARGET_CP_MASTER_API,
                propertyService.getCpMasterApiListPodsGetUrl()
                        .replace("{namespace}", namespace)
                        .replace("{name}", podsName)
                , HttpMethod.GET, null, Map.class);
        HashMap responseMap;

        try {
            responseMap = (HashMap) obj;
        } catch (Exception e) {
            return obj;
        }

        return commonService.setResultModel(commonService.setResultObject(responseMap, PodsAdmin.class), Constants.RESULT_STATUS_SUCCESS);

    }

    /**
     * Pods YAML 조회(Get Pods yaml)
     *
     * @param namespace the namespace
     * @param podName the pods name
     * @param resultMap the result map
     * @return the pods yaml
     */
    public Pods getPodsYaml(String namespace, String podName, HashMap resultMap) {
        String resultString = restTemplateService.send(Constants.TARGET_CP_MASTER_API,
                propertyService.getCpMasterApiListPodsGetUrl().replace("{namespace}", namespace).replace("{name}", podName),
                HttpMethod.GET, null, String.class, Constants.ACCEPT_TYPE_YAML);
        //noinspection unchecked
        resultMap.put("sourceTypeYaml", resultString);

        return (Pods) commonService.setResultModel(commonService.setResultObject(resultMap, Pods.class), Constants.RESULT_STATUS_SUCCESS);
    }

    /**
     * Pods 생성(Create Pods)
     *
     * @param namespace the namespace
     * @param yaml the yaml
     * @return return is succeeded
     */
    public Object createPods(String namespace, String yaml) {
        Object map = restTemplateService.sendYaml(Constants.TARGET_CP_MASTER_API,
                propertyService.getCpMasterApiListPodsCreateUrl()
                        .replace("{namespace}", namespace), HttpMethod.POST, yaml, Object.class);

        return commonService.setResultModelWithNextUrl(commonService.setResultObject(map, Pods.class),
                Constants.RESULT_STATUS_SUCCESS, Constants.URI_WORKLOAD_PODS);
    }

    /**
     * Pods 삭제(Delete Pods)
     *
     * @param namespace the namespace
     * @param resourceName the resource name
     * @param resultMap the result map
     * @return return is succeeded
     */
    public ResultStatus deletePods(String namespace, String resourceName, HashMap resultMap) {
        ResultStatus resultStatus = restTemplateService.send(Constants.TARGET_CP_MASTER_API,
                propertyService.getCpMasterApiListPodsDeleteUrl()
                        .replace("{namespace}", namespace).replace("{name}", resourceName), HttpMethod.DELETE, null, ResultStatus.class);

        return (ResultStatus) commonService.setResultModelWithNextUrl(commonService.setResultObject(resultStatus, ResultStatus.class), Constants.RESULT_STATUS_SUCCESS, Constants.URI_WORKLOAD_PODS);
    }

    /**
     * Pods 수정(Update Pods)
     *
     * @param namespace the namespace
     * @param name the pods name
     * @param yaml the yaml
     * @return return is succeeded
     */
    public Object updatePods(String namespace, String name, String yaml) {
        Object map = restTemplateService.sendYaml(Constants.TARGET_CP_MASTER_API,
                propertyService.getCpMasterApiListPodsUpdateUrl()
                        .replace("{namespace}", namespace).replace("{name}", name), HttpMethod.PUT, yaml, Object.class);

        return commonService.setResultModelWithNextUrl(commonService.setResultObject(map, Pods.class),
                Constants.RESULT_STATUS_SUCCESS, Constants.URI_WORKLOAD_PODS_DETAIL.replace("{podName:.+}", name));
    }


}
