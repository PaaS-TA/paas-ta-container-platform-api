package org.paasta.container.platform.api.clusters.limitRanges;

import org.paasta.container.platform.api.clusters.limitRanges.support.LimitRangesItem;
import org.paasta.container.platform.api.common.CommonService;
import org.paasta.container.platform.api.common.Constants;
import org.paasta.container.platform.api.common.PropertyService;
import org.paasta.container.platform.api.common.RestTemplateService;
import org.paasta.container.platform.api.common.model.ResultStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LimitRanges Service 클래스
 *
 * @author kjhoon
 * @version 1.0
 * @since 2020.10.22
 */
@Service
public class LimitRangesService {
    private final RestTemplateService restTemplateService;
    private final CommonService commonService;
    private final PropertyService propertyService;

    /**
     * Instantiates a new LimitRanges service
     *
     * @param restTemplateService the rest template service
     * @param commonService       the common service
     * @param propertyService     the property service
     */
    @Autowired
    public LimitRangesService(RestTemplateService restTemplateService, CommonService commonService, PropertyService propertyService) {
        this.restTemplateService = restTemplateService;
        this.commonService = commonService;
        this.propertyService = propertyService;
    }


    /**
     * LimitRanges Admin 목록 조회(Get LimitRanges Admin list)
     *
     * @param namespace the namespace
     * @param limit the limit
     * @param continueToken the continueToken
     * @param  searchParam the searchParam
     * @return the limitRanges admin list
     */
    public Object getLimitRangesListAdmin(String namespace, int limit, String continueToken, String searchParam) {
        String param = "";
        HashMap responseMap = null;

        if (continueToken != null) {
            param = "&continue=" + continueToken;
        }

        Object response = restTemplateService.sendAdmin(Constants.TARGET_CP_MASTER_API,
                propertyService.getCpMasterApiListLimitRangesListUrl()
                        .replace("{namespace}", namespace) + "?limit=" + limit + param, HttpMethod.GET, null, Map.class);

        try {
            responseMap = (HashMap) response;
        } catch (Exception e) {
            return response;
        }

        return commonService.setResultModel(commonService.setResultObject(responseMap, LimitRangesListAdmin.class), Constants.RESULT_STATUS_SUCCESS);
    }


    /**
     * LimitRanges Admin 상세 조회(Get LimitRanges Admin detail)
     *
     * @param namespace the namespace
     * @param resourceName the resource name
     * @return the limitRanges admin detail
     */
    public Object getLimitRangesAdmin(String namespace, String resourceName) {

        HashMap responseMap = null;

        Object response = restTemplateService.sendAdmin(Constants.TARGET_CP_MASTER_API,
                propertyService.getCpMasterApiListLimitRangesGetUrl()
                        .replace("{namespace}", namespace)
                        .replace("{name}", resourceName),
                HttpMethod.GET, null, Map.class);

        try {
            responseMap = (HashMap) response;
        } catch (Exception e) {
            return response;
        }

        return commonService.setResultModel(commonService.setResultObject(responseMap, LimitRangesAdmin.class), Constants.RESULT_STATUS_SUCCESS);
    }


    /**
     * LimitRanges YAML 조회(Get LimitRanges yaml)
     *
     * @param namespace the namespace
     * @param resourceName the resource name
     * @param resultMap the result map
     * @return the limitRanges yaml
     */
    public LimitRanges getLimitRangesYaml(String namespace, String resourceName, HashMap resultMap) {

        String resultString = restTemplateService.send(Constants.TARGET_CP_MASTER_API,
                propertyService.getCpMasterApiListLimitRangesGetUrl()
                        .replace("{namespace}", namespace)
                        .replace("{name}", resourceName),
                HttpMethod.GET, null, String.class, Constants.ACCEPT_TYPE_YAML);

        resultMap.put("sourceTypeYaml", resultString);

        return (LimitRanges) commonService.setResultModel(commonService.setResultObject(resultMap, LimitRanges.class), Constants.RESULT_STATUS_SUCCESS);

    }


    /**
     * LimitRanges 생성(Create LimitRanges)
     *
     * @param namespace the namespace
     * @param yaml the yaml
     * @return return is succeeded
     */
    public Object createLimitRanges(String namespace, String yaml) {

        Object map = restTemplateService.sendYaml(Constants.TARGET_CP_MASTER_API,
                propertyService.getCpMasterApiListLimitRangesCreateUrl().replace("{namespace}", namespace),
                HttpMethod.POST, yaml, Object.class);

        return commonService.setResultModelWithNextUrl(commonService.setResultObject(map, ResultStatus.class), Constants.RESULT_STATUS_SUCCESS, Constants.URI_LIMITRANGES);
    }


    /**
     * LimitRanges 삭제(Delete LimitRanges)
     *
     * @param namespace the namespace
     * @param resourceName the resource name
     * @return return is succeeded
     */
    public ResultStatus deleteLimitRanges(String namespace, String resourceName) {
        ResultStatus resultStatus = restTemplateService.sendAdmin(Constants.TARGET_CP_MASTER_API,
                propertyService.getCpMasterApiListLimitRangesDeleteUrl().replace("{namespace}", namespace).replace("{name}", resourceName),
                HttpMethod.DELETE, null, ResultStatus.class);

        return (ResultStatus) commonService.setResultModelWithNextUrl(commonService.setResultObject(resultStatus, ResultStatus.class), Constants.RESULT_STATUS_SUCCESS, Constants.URI_LIMITRANGES);
    }


    /**
     * LimitRanges 수정(Update LimitRanges)
     *
     * @param namespace the namespace
     * @param resourceName the resource name
     * @param yaml the yaml
     * @return return is succeeded
     */
    public ResultStatus updateLimitRanges(String namespace, String resourceName, String yaml) {
        ResultStatus resultStatus = restTemplateService.sendYaml(Constants.TARGET_CP_MASTER_API,
                propertyService.getCpMasterApiListLimitRangesUpdateUrl().replace("{namespace}", namespace).replace("{name}", resourceName),
                HttpMethod.PUT, yaml, ResultStatus.class);

        return (ResultStatus) commonService.setResultModelWithNextUrl(commonService.setResultObject(resultStatus, ResultStatus.class),
                Constants.RESULT_STATUS_SUCCESS, Constants.URI_LIMITRANGES_DETAIL.replace("{limitRangeName:.+}", resourceName));
    }


    /**
     * LimitRanges Template 목록 조회(Get LimitRanges Template list)
     *
     * @param namespace the namespace
     * @return the limitRanges template list
     */
    public Object getLimitRangesTemplateList(String namespace) {
        LimitRangesListAdmin limitRangesList = (LimitRangesListAdmin) getLimitRangesListAdmin(namespace, 0, null, null);
        LimitRangesDefaultList defaultList = restTemplateService.send(Constants.TARGET_COMMON_API, "/limitRanges", HttpMethod.GET, null, LimitRangesDefaultList.class);

        List<LimitRangesListAdminItem> adminItems = limitRangesList.getItems();
        List<LimitRangesTemplateItem> serversItemList = new ArrayList();

        LimitRangesTemplateList serverList = new LimitRangesTemplateList();

        for (LimitRangesDefault limitRangesDefault : defaultList.getItems()) {
            serversItemList.add(getLimitRangesDb(limitRangesDefault));
        }

        if(adminItems.size() > 0) {
            for (LimitRangesListAdminItem i : adminItems) {
                List<LimitRangesItem> list = new ArrayList<>();
                LimitRangesTemplateItem serversItem = new LimitRangesTemplateItem();
                for (LimitRangesItem item : i.getSpec().getLimits()) {
                    list.add(item);
                }
                serversItem.setName(i.getName());
                serversItem.setLimits(list);

                serversItemList.add(serversItem);
            }

            serverList.setItems(serversItemList);
            return commonService.setResultModel(serverList, Constants.RESULT_STATUS_SUCCESS);
        }

        serverList.setItems(serversItemList);
        return commonService.setResultModel(serverList, Constants.RESULT_STATUS_SUCCESS);
    }


    /**
     * LimitRanges DB Template 형식 맞춤(Set LimitRanges DB template)
     *
     * @param limitRangesDefault the limitRangesDefault
     * @return the limitRanges template item
     */
    public LimitRangesTemplateItem getLimitRangesDb(LimitRangesDefault limitRangesDefault) {
        LimitRangesItem map = new LimitRangesItem();
        List<LimitRangesItem> list = new ArrayList<>();
        LimitRangesTemplateItem item = new LimitRangesTemplateItem();

        map.setDefaultRequest(limitRangesDefault.getDefaultRequest());
        map.setMin(limitRangesDefault.getMin());
        map.setMax(limitRangesDefault.getMax());
        map.setType(limitRangesDefault.getType());
        map.setDefaultLimit(limitRangesDefault.getDefaultLimit());

        list.add(map);

        item.setName(limitRangesDefault.getName());
        item.setLimits(list);

        return item;
    }
}
