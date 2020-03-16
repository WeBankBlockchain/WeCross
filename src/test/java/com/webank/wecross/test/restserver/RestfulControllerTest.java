package com.webank.wecross.test.restserver;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.account.AccountManager;
import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.resource.Path;
import com.webank.wecross.resource.Resource;
import com.webank.wecross.restserver.RestRequest;
import com.webank.wecross.restserver.RestfulController;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.zone.ZoneManager;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

// To run with: gradle test --tests RestfulServiceTest

@RunWith(SpringRunner.class)
// @SpringBootTest
@WebMvcTest(RestfulController.class)
// @AutoConfigureMockMvc
// @TestExecutionListeners( { DependencyInjectionTestExecutionListener.class })
// @ContextConfiguration(classes=RestfulServiceTestConfig.class)
public class RestfulControllerTest {
    @Autowired private MockMvc mockMvc;
    
    ObjectMapper objectMapper = new ObjectMapper();

    @MockBean(name = "newWeCrossHost")
    private WeCrossHost weCrossHost;
    
    @MockBean
    private AccountManager accountManager;

    @Test
    public void okTest() throws Exception {
        try {
            MvcResult rsp =
                    this.mockMvc
                            .perform(get("/test"))
                            .andDo(print())
                            .andExpect(status().isOk())
                            .andReturn();

            String result = rsp.getResponse().getContentAsString();
            System.out.println("####Respond: " + result);

            String expectRsp = "OK!";
            Assert.assertEquals(expectRsp, result);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
    }
    
    @Test
    public void stateTest() throws Exception {
    	try {
    		MvcResult rsp = mockMvc.perform(get("/state"))
    				.andDo(print())
    				.andExpect(status().isOk())
    				.andReturn();
    		String result = rsp.getResponse().getContentAsString();
	    } catch (Exception e) {
	        Assert.assertTrue(e.getMessage(), false);
	    }
    }

    @Test
    public void statusTest() throws Exception {
        try {
            Mockito.when(weCrossHost.getResource(Mockito.any())).thenReturn(new Resource());

            MvcResult rsp =
                    this.mockMvc
                            .perform(get("/test-network/test-stub/test-resource/status"))
                            .andDo(print())
                            .andExpect(status().isOk())
                            .andReturn();

            String result = rsp.getResponse().getContentAsString();
            System.out.println("####Respond: " + result);

            String expectRsp =
                    "{\"version\":\"1\",\"result\":0,\"message\":\"Success\",\"data\":\"exists\"}";
            Assert.assertEquals(expectRsp, result);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
    }

    @Test
    public void listTest() throws Exception {
        try {
            ZoneManager mockZoneManager = Mockito.mock(ZoneManager.class);
            Mockito.when(mockZoneManager.getAllResources(Mockito.anyBoolean()))
                    .thenReturn(new ArrayList<Resource>());

            Mockito.when(weCrossHost.getZoneManager()).thenReturn(mockZoneManager);

            String json =
                    "{\n"
                            + "\"version\":\"1\",\n"
                            + "\"path\":\"\",\n"
                            + "\"method\":\"list\",\n"
                            + "\"data\": {\n"
                            + "\"ignoreRemote\":true\n"
                            + "}\n"
                            + "}";

            MvcResult rsp =
                    this.mockMvc
                            .perform(
                                    post("/list")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(json))
                            .andDo(print())
                            .andExpect(status().isOk())
                            .andReturn();

            String result = rsp.getResponse().getContentAsString();
            System.out.println("####Respond: " + result);

            String expectRsp =
                    "{\"version\":\"1\",\"result\":0,\"message\":\"Success\",\"data\":{\"errorCode\":0,\"errorMessage\":\"\",\"resources\"";
            Assert.assertTrue(result.contains(expectRsp));
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
    }

    @Test
    public void callTest() throws Exception {
        try {
            TransactionResponse transactionResponse = new TransactionResponse();
            transactionResponse.setErrorCode(0);
            transactionResponse.setErrorMessage("call test resource success");
            transactionResponse.setHash("010157f4");

            Resource resource = Mockito.mock(Resource.class);
            Mockito.when(resource.call(Mockito.any(), Mockito.any()))
                    .thenReturn(transactionResponse);

            Mockito.when(weCrossHost.getResource(Mockito.isA(Path.class))).thenReturn(resource);
            
            Mockito.when(accountManager.getAccount("demo")).thenReturn(null);
            
            RestRequest<TransactionRequest> request = new RestRequest<TransactionRequest>();
            request.setVersion("1");
            request.setPath("test-network.test-stub.test-resource");
            request.setMethod("call");
            request.setAccount("demo");
            request.setData(new TransactionRequest());
            
            request.getData().setMethod("get");
            request.getData().setArgs(new Object[] {});
            
            MvcResult rsp =
                    this.mockMvc
                            .perform(
                                    post("/test-network/test-stub/test-resource/call")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(objectMapper.writeValueAsString(request)))
                            .andDo(print())
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.version").value("1"))
                            .andExpect(jsonPath("$.result").value(0))
                            .andExpect(jsonPath("$.message").value("Success"))
                            .andExpect(jsonPath("$.data.errorCode").value(0))
                            .andExpect(jsonPath("$.data.errorMessage").value("call test resource success"))
                            .andExpect(jsonPath("$.data.hash").value("010157f4"))
                            .andReturn();

            String result = rsp.getResponse().getContentAsString();
            System.out.println("####Respond: " + result);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
    }

    @Test
    public void sendTransactionTest() throws Exception {
        try {
            TransactionResponse transactionResponse = new TransactionResponse();
            transactionResponse.setErrorCode(0);
            transactionResponse.setErrorMessage("sendTransaction test resource success");
            transactionResponse.setHash("010157f4");

            Resource resource = Mockito.mock(Resource.class);
            Mockito.when(resource.sendTransaction(Mockito.any(), Mockito.any()))
                    .thenReturn(transactionResponse);

            Mockito.when(weCrossHost.getResource(Mockito.isA(Path.class))).thenReturn(resource);
            
            Mockito.when(accountManager.getAccount("demo")).thenReturn(null);

            RestRequest<TransactionRequest> request = new RestRequest<TransactionRequest>();
            request.setVersion("1");
            request.setPath("test-network.test-stub.test-resource");
            request.setMethod("sendTransaction");
            request.setAccount("demo");
            request.setData(new TransactionRequest());
            
            request.getData().setMethod("set");
            request.getData().setArgs(new Object[] {"aaaaa"});
            
            MvcResult rsp =
                    this.mockMvc
                            .perform(
                                    post("/test-network/test-stub/test-resource/sendTransaction")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(objectMapper.writeValueAsString(request)))
                            .andDo(print())
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.version").value("1"))
                            .andExpect(jsonPath("$.result").value(0))
                            .andExpect(jsonPath("$.message").value("Success"))
                            .andExpect(jsonPath("$.data.errorCode").value(0))
                            .andExpect(jsonPath("$.data.errorMessage").value("sendTransaction test resource success"))
                            .andExpect(jsonPath("$.data.hash").value("010157f4"))
                            .andReturn();

            String result = rsp.getResponse().getContentAsString();
            System.out.println("####Respond: " + result);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
    }

    @Test
    public void exceptionTest() {
        try {
            String json =
                    "{\n"
                            + "\"version\":\"1\",\n"
                            + "\"path\":\"test-network.test-stub.test-resource\",\n"
                            + "\"method\":\"sendTransaction\",\n"
                            + "\"data\": {\n"
                            + "\"method\":\"set\",\n"
                            + "\"args\":[\"aaaaa\"]\n"
                            + "}\n"
                            + "}";

            MvcResult rsp =
                    this.mockMvc
                            .perform(
                                    post("/test-network/test-stub/test-resource/notExistMethod")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(json))
                            .andDo(print())
                            .andExpect(status().isOk())
                            .andReturn();

            String result = rsp.getResponse().getContentAsString();
            System.out.println("####Respond: " + result);

            String expectRsp =
                    "{\"version\":\"1\",\"result\":20001,\"message\":\"Unsupported method: notExistMethod\",\"data\":null}";
            Assert.assertTrue(result.contains(expectRsp));
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
    }

    @Before
    public void beforeTest() {
        // mockMvc = MockMvcBuilders.standaloneSetup(RestfulController.class).build();
        System.out.println("------------------------Test begin------------------------");
    }

    @After
    public void afterTest() {
        System.out.println("-------------------------Test end-------------------------");
    }

    @BeforeClass
    public static void beforeClassTest() {
        System.out.println("beforeClassTest");
    }

    @AfterClass
    public static void afterClassTest() {
        System.out.println("afterClassTest");
    }
}
