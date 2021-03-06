package com.codemover.xplanner.Security.Controller;

import com.codemover.xplanner.DAO.JAccountUserRepository;
import com.codemover.xplanner.DAO.PlannerStoreRepository;
import com.codemover.xplanner.DAO.RoleRepository;
import com.codemover.xplanner.Model.Entity.JAccountUser;
import com.codemover.xplanner.Model.Entity.Plannerstore;
import com.codemover.xplanner.Model.Entity.Role;
import com.codemover.xplanner.Security.Config.ConstConfig;
import com.codemover.xplanner.Security.Exception.ParseProfileJsonException;
import com.codemover.xplanner.Security.Util.UserFactory;
import com.codemover.xplanner.Service.Impl.JAccountParseService;
import com.codemover.xplanner.Service.ScheduleService;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAuthzResponse;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.token.OAuthToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping(value = "/api/auth")
public class JAccountLogin {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ConstConfig constConfig;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private PlannerStoreRepository plannerStoreRepository;

    @Autowired
    JAccountUserRepository jAccountUserRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    JAccountParseService jAccountParseService;

    @ResponseBody
    @RequestMapping(value = "/getJAccountLoginUrl", method = RequestMethod.GET)
    HashMap<String, Object> loginByJAccount() {
        HashMap<String, Object> response = new HashMap<>();


        try {
            System.out.println(constConfig.authorizationUrl);
            OAuthClientRequest request = OAuthClientRequest
                    .authorizationLocation(constConfig.authorizationUrl)
                    .setClientId(constConfig.clientID)
                    .setRedirectURI(constConfig.redirectUrl)
                    .setResponseType("code")
                    .setState("033tZm1o07f6or1yMBZn05VA1o0tZm1K")
                    .setScope(constConfig.scope)
                    .buildQueryMessage();
            String UrlForGetCode = request.getLocationUri();
            logger.info("build redirectUrl: '{}'", UrlForGetCode);
            response.put("errMsg", "loginByJAccount:ok");
            response.put("redirectUrl", UrlForGetCode);
            return response;
        } catch (OAuthSystemException e) {
            logger.error("error occured when building url for JAccount authorization", e);
            response.put("errMsg", "loginByJAccount:fail");
            return response;
        }
    }

    @ResponseBody
    @RequestMapping(value = "/authorize", method =
            {RequestMethod.POST, RequestMethod.GET})
    HashMap<String, Object> getCodeFromRedirectUrl(HttpServletRequest servletRequest,
                                                   HttpServletResponse servletResponse) {
        HashMap<String, Object> responseToFrontEnd = new HashMap<>();


        try {
            OAuthAuthzResponse oar = OAuthAuthzResponse.oauthCodeAuthzResponse(servletRequest);
            String code = oar.getCode();
            String state = oar.getState();

            logger.info(code);
            OAuthClientRequest request = OAuthClientRequest
                    .tokenLocation(constConfig.accessTokenUrl)
                    .setGrantType(GrantType.AUTHORIZATION_CODE)
                    .setClientId(constConfig.clientID)
                    .setClientSecret(constConfig.clientSecret)
                    .setRedirectURI(constConfig.redirectUrl)
                    .setCode(code)
                    .buildQueryMessage();

            logger.info(request.getLocationUri());

            OAuthClient clientForAccessToken = new OAuthClient(new URLConnectionClient());
            OAuthJSONAccessTokenResponse jsonAccessTokenResponse = clientForAccessToken.accessToken(request, OAuthJSONAccessTokenResponse.class);
            logger.info("Access token: '{}'", jsonAccessTokenResponse.getBody());


            OAuthToken oAuthToken = jsonAccessTokenResponse.getOAuthToken();
            String accessToken = oAuthToken.getAccessToken();
            String refreshToken = oAuthToken.getRefreshToken();
            Long expiresIn = oAuthToken.getExpiresIn();


            OAuthClientRequest bearerClientRequest =
                    new OAuthBearerClientRequest(constConfig.profileUrl)
                            .setAccessToken(accessToken).buildQueryMessage();

            //Get the profile of the user
            OAuthClient clientToGetInfo = new OAuthClient(new URLConnectionClient());
            OAuthResourceResponse authResourceResponse = clientToGetInfo.resource(bearerClientRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);


            OAuthClientRequest bearerClientRequest2 =
                    new OAuthBearerClientRequest(constConfig.lessonsUrl)
                            .setAccessToken(accessToken).buildQueryMessage();

            //Get the lessons of the user
            OAuthClient clientToGetLessons = new OAuthClient(new URLConnectionClient());
            OAuthResourceResponse lesson = clientToGetLessons.resource(bearerClientRequest2, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
            String lessonJson = lesson.getBody();


            logger.info("JAccount user profile: '{}'", authResourceResponse.getBody());


            JAccountUser jAccountUser = UserFactory.createJAccountUser(authResourceResponse.getBody());
            jAccountUser.setAccessToken(accessToken);

            List<Role> roles = roleRepository.findAll();
            jAccountUser.setRoles(roles);


            jAccountUser.setEnabled(true);
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            String defaultPassword = bCryptPasswordEncoder.encode(jAccountUser.getUniqueId());
            jAccountUser.setUserPassword(defaultPassword);
            List<Plannerstore> plannerstores = plannerStoreRepository.findAll();
            jAccountUser.setPlannerstores(plannerstores);


            jAccountUser.setOpenId(state);


            jAccountUserRepository.save(jAccountUser);


            jAccountParseService.JAccountLesson(lessonJson, jAccountUser);


            logger.info("用户扫码后获取用户信息成功，用户名: '{}'", jAccountUser.getjAccountName());
            responseToFrontEnd.put("errMsg", "loginByJAccount:ok");
            return responseToFrontEnd;
        } catch (OAuthSystemException | OAuthProblemException e) {
            logger.error("error occurred in authorize", e);
            responseToFrontEnd.put("errMsg", "loginByJAccount:fail");
            return responseToFrontEnd;
        } catch (ParseException | ParseProfileJsonException e) {
            logger.error("error occurred in handling the profile response", e);
            responseToFrontEnd.put("errMsg", "loginByJAccount:fail");
            return responseToFrontEnd;
        } catch (Exception e) {
            logger.error("error occurred in handling the profile response", e);
            responseToFrontEnd.put("errMsg", "loginByJAccount:fail");
            return responseToFrontEnd;
        }
    }

}
