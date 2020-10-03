package org.paasta.container.platform.api.users;

import org.paasta.container.platform.api.common.CommonService;
import org.paasta.container.platform.api.common.Constants;
import org.paasta.container.platform.api.common.PropertyService;
import org.paasta.container.platform.api.common.RestTemplateService;
import org.paasta.container.platform.api.common.model.ResultStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.paasta.container.platform.api.common.Constants.TARGET_COMMON_API;
import static org.paasta.container.platform.api.common.Constants.TARGET_CP_MASTER_API;

/**
 * User Service 클래스
 *
 * @author hrjin
 * @version 1.0
 * @since 2020.09.22
 **/
@Service
public class UsersService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsersService.class);

    private final CommonService commonService;
    private final PropertyService propertyService;
    private final RestTemplateService restTemplateService;

    @Autowired
    public UsersService(CommonService commonService, PropertyService propertyService, RestTemplateService restTemplateService) {
        this.commonService = commonService;
        this.propertyService = propertyService;
        this.restTemplateService = restTemplateService;
    }


    /**
     * 사용자를 등록한다. (회원가입)
     *
     * @param users  the users
     * @return       the result status
     */
    public ResultStatus createUsers(Users users) {
        // (1) ::: service account 생성. 타겟은 temp-namespace
        String saYaml = "apiVersion: v1\n" +
                "kind: ServiceAccount\n" +
                "metadata:\n" +
                " name: " + users.getUserId() + "\n" +
                " namespace: " + Constants.DEFAULT_NAMESPACE_NAME;
        Object saResult = restTemplateService.sendYaml(TARGET_CP_MASTER_API, propertyService.getCpMasterApiListUsersCreateUrl().replace("{namespace}", Constants.DEFAULT_NAMESPACE_NAME), HttpMethod.POST, saYaml, Object.class);

        ResultStatus rsK8s = (ResultStatus) commonService.setResultModelWithNextUrl(commonService.setResultObject(saResult, ResultStatus.class),
                Constants.RESULT_STATUS_SUCCESS, Constants.URI_INTRO_OVERVIEW);

        // (2) ::: service account 생성 완료 시 아래 Common API 호출
        if(Constants.RESULT_STATUS_FAIL.equals(rsK8s.getResultCode())) {
            return rsK8s;
        }

        users.setCpNamespace(Constants.DEFAULT_NAMESPACE_NAME);
        users.setServiceAccountName(users.getUserId());

        ResultStatus rsDb = restTemplateService.send(TARGET_COMMON_API, "/users", HttpMethod.POST, users, ResultStatus.class);

        // (3) ::: DB 커밋에 실패했을 경우 k8s 에 만들어진 service account 삭제
        if(Constants.RESULT_STATUS_FAIL.equals(rsDb.getResultCode())) {
            LOGGER.info("DATABASE EXECUTE IS FAILED. K8S SERVICE ACCOUNT WILL BE REMOVED...");
            restTemplateService.sendYaml(TARGET_CP_MASTER_API, propertyService.getCpMasterApiListUsersDeleteUrl().replace("{namespace}", Constants.DEFAULT_NAMESPACE_NAME).replace("{name}", users.getUserId()), HttpMethod.DELETE, null, Object.class);
        }

        return (ResultStatus) commonService.setResultModelWithNextUrl(commonService.setResultObject(rsDb, ResultStatus.class), Constants.RESULT_STATUS_SUCCESS, Constants.URI_INTRO_OVERVIEW);
    }

    public UsersList getUsersList() {
        return restTemplateService.send(TARGET_COMMON_API, "/users", HttpMethod.GET, null, UsersList.class);
    }


    /**
     * 등록된 사용자 이름 목록
     *
     * @return the List<String>
     */
    public List<String> getUsersNameList() {
        return restTemplateService.send(TARGET_COMMON_API, "/users", HttpMethod.GET, null, List.class);
    }
}