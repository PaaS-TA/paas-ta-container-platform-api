package org.paasta.container.platform.api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.HttpServletResponse;

/**
 * GlobalController Exception Handler 클래스
 *
 * @author hrjin
 * @version 1.0
 * @since 2020.08.25
 **/
@ControllerAdvice
public class GlobalControllerExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

    @Autowired
    public MessageSource messageSource;

    // Rest API Template Error Exception
    @ExceptionHandler(HttpClientErrorException.class)
    @ResponseBody
    public void httpClientErrorException(HttpClientErrorException ex, HttpServletResponse response) throws Exception {

        LOGGER.error("HttpClientErrorException : " + ex );
        //LOGGER.info("ex >>> rawCode : " + ex.getRawStatusCode() + " getMessage : " + ex.getMessage() + ", localizedMessage : " + ex.getLocalizedMessage()+ ", getResponseBodyAsString : "  + ex.getResponseBodyAsString()+ ", getCause : " + ex.getCause());

        String message =
                "{\"resultMessage\":\""+ex.getStatusText()+"\"" +
                //",\"resultCode\":\""+ex.getStatusCode()+"\"}";
                ",\"resultCode\":\""+"FAIL"+"\"}";
        // errorCode를 받지만, 에러를 "FAIL"로 코드 통일

        // CONTAINER-PLATFORM-API 통신 성공을 위한 SC_OK(200) 및 kubernetes API 에러코드 및 메시지 전달
        response.resetBuffer();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Content-Type", "application/json;charset=utf-8");
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().print(message);
        response.flushBuffer();
    }

}

