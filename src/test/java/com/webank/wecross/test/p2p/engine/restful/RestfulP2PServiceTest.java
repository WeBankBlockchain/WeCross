package com.webank.wecross.test.p2p.engine.restful;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class RestfulP2PServiceTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void requestSeqTest() throws Exception {
        try {
            String json = "{\n" +
                    "  \"version\": \"0.1\",\n" +
                    "  \"seq\": \"100\",\n" +
                    "  \"method\": \"requestSeq\",\n" +
                    "  \"data\": {}\n" +
                    "}";

            MvcResult rsp =
                    this.mockMvc
                            .perform(
                                    post("/p2p/requestSeq")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(json))
                            .andDo(print())
                            .andExpect(status().isOk())
                            .andReturn();

            String result = rsp.getResponse().getContentAsString();
            System.out.println("####Respond: " + result);

            String expectRsp = "{\"version\":\"0.1\",\"seq\":100,\"result\":0,\"message\":\"request requestSeq method success\",\"data\":{\"seq\":";

            Assert.assertTrue(result.contains(expectRsp));
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
    }

    @Test
    public void seqTest() throws Exception {
        try {
            String json = "{\n" +
                    "  \"version\": \"0.1\",\n" +
                    "  \"seq\": 100,\n" +
                    "  \"method\": \"seq\",\n" +
                    "  \"data\": {\n" +
                    "    \"seq\": 1415412187\n" +
                    "  }\n" +
                    "}";

            MvcResult rsp =
                    this.mockMvc
                            .perform(
                                    post("/p2p/seq")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(json))
                            .andDo(print())
                            .andExpect(status().isOk())
                            .andReturn();

            String result = rsp.getResponse().getContentAsString();
            System.out.println("####Respond: " + result);

            String expectRsp = "{\"version\":\"0.1\",\"seq\":100,\"result\":0,\"message\":\"request seq method success\",\"data\":null}";

            Assert.assertTrue(result.contains(expectRsp));
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
    }

    @Test
    public void requestPeerInfoTest() throws Exception {
        try {
            String json = "{\n" +
                    "  \"version\": \"0.1\",\n" +
                    "  \"seq\": \"100\",\n" +
                    "  \"method\": \"requestPeerInfo\",\n" +
                    "  \"data\": {}\n" +
                    "}";

            MvcResult rsp =
                    this.mockMvc
                            .perform(
                                    post("/p2p/requestPeerInfo")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(json))
                            .andDo(print())
                            .andExpect(status().isOk())
                            .andReturn();

            String result = rsp.getResponse().getContentAsString();
            System.out.println("####Respond: " + result);

            String expectRsp = "\"resources\":[\"test-network.test-stub.test-resource\"";

            Assert.assertTrue(result.contains(expectRsp));
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
    }

    @Test
    public void peerInfoTest() throws Exception {
        try {
            String json = "{\n" +
                    "  \"version\": \"0.1\",\n" +
                    "  \"seq\": 100,\n" +
                    "  \"result\": 0,\n" +
                    "  \"message\": \"request requestPeerInfo method success\",\n" +
                    "  \"data\": {\n" +
                    "    \"seq\": 769518166,\n" +
                    "    \"resources\": [\n" +
                    "      \"test-network.test-stub.test-resource\",\n" +
                    "      \"payment.bcos2.HelloWorldContract\",\n" +
                    "      \"payment.bcos1.HelloWorldContract\"\n" +
                    "    ]\n" +
                    "  }\n" +
                    "}";

            MvcResult rsp =
                    this.mockMvc
                            .perform(
                                    post("/p2p/peerInfo")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(json))
                            .andDo(print())
                            .andExpect(status().isOk())
                            .andReturn();

            String result = rsp.getResponse().getContentAsString();
            System.out.println("####Respond: " + result);

            String expectRsp = "{\"version\":\"0.1\",\"seq\":100,\"result\":0,\"message\":\"request peerInfo method success\",\"data\":null}";

            Assert.assertTrue(result.contains(expectRsp));
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
    }

    @Test
    public void callTest() throws Exception {
        try {
            String json = "{\n" +
                    "  \"version\": \"0.1\",\n" +
                    "  \"seq\": 100,\n" +
                    "  \"method\": \"/test-network/test-stub/test-resource/call\",\n" +
                    "  \"data\": {\n" +
                    "    \"sig\": \"\",\n" +
                    "    \"method\": \"get\",\n" +
                    "    \"args\": []\n" +
                    "  }\n" +
                    "}";

            MvcResult rsp =
                    this.mockMvc
                            .perform(
                                    post("/p2p/test-network/test-stub/test-resource/call")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(json))
                            .andDo(print())
                            .andExpect(status().isOk())
                            .andReturn();

            String result = rsp.getResponse().getContentAsString();
            System.out.println("####Respond: " + result);

            String expectRsp = "{\"version\":\"0.1\",\"seq\":100,\"result\":0,\"message\":null,\"data\":{\"errorCode\":0,\"errorMessage\":\"Call test resource success\",\"hash\":\"010157f4\",\"result\":[{\"sig\":\"\",\"method\":\"get\",\"args\":[]}]}}";

            Assert.assertTrue(result.contains(expectRsp));
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
    }

    @Test
    public void sendTransactionTest() throws Exception {
        try {
            String json = "{\n" +
                    "  \"version\": \"0.1\",\n" +
                    "  \"seq\": 100,\n" +
                    "  \"method\": \"/test-network/test-stub/test-resource/sendTransaction\",\n" +
                    "  \"data\": {\n" +
                    "    \"sig\": \"\",\n" +
                    "    \"method\": \"set\",\n" +
                    "    \"args\": [\"HelloWorld\"]\n" +
                    "  }\n" +
                    "}";

            MvcResult rsp =
                    this.mockMvc
                            .perform(
                                    post("/p2p/test-network/test-stub/test-resource/sendTransaction")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(json))
                            .andDo(print())
                            .andExpect(status().isOk())
                            .andReturn();

            String result = rsp.getResponse().getContentAsString();
            System.out.println("####Respond: " + result);

            String expectRsp = "{\"version\":\"0.1\",\"seq\":100,\"result\":0,\"message\":null,\"data\":{\"errorCode\":0,\"errorMessage\":\"sendTransaction test resource success\",\"hash\":\"010157f4\",\"result\":[{\"sig\":\"\",\"method\":\"set\",\"args\":[\"HelloWorld\"]}]}}";
            Assert.assertTrue(result.contains(expectRsp));
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
    }


    @Test
    public void exceptionTest1() {
        try {
            String json = "{\n" +
                    "  \"version\": \"0.1\",\n" +
                    "  \"seq\": 100,\n" +
                    "  \"method\": \"mock\",\n" +
                    "  \"data\": {}\n" +
                    "}";

            MvcResult rsp =
                    this.mockMvc
                            .perform(
                                    post("/p2p/notExistMethod")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(json))
                            .andDo(print())
                            .andExpect(status().isOk())
                            .andReturn();

            String result = rsp.getResponse().getContentAsString();
            System.out.println("####Respond: " + result);

            String expectRsp = "{\"version\":\"0.1\",\"seq\":100,\"result\":-1,\"message\":\"Unsupported method: notExistMethod\",\"data\":null}";
            Assert.assertTrue(result.contains(expectRsp));
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
    }

    @Test
    public void exceptionTest2() {
        try {
            String json = "{\n" +
                    "  \"version\": \"0.1\",\n" +
                    "  \"seq\": 100,\n" +
                    "  \"method\": \"/test-network/test-stub/test-resource/sendTransaction\",\n" +
                    "  \"data\": {\n" +
                    "    \"sig\": \"\",\n" +
                    "    \"method\": \"set\",\n" +
                    "    \"args\": [\"HelloWorld\"]\n" +
                    "  }\n" +
                    "}";

            MvcResult rsp =
                    this.mockMvc
                            .perform(
                                    post("/p2p/test-network/test-stub/test-resource/notExistMethod")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(json))
                            .andDo(print())
                            .andExpect(status().isOk())
                            .andReturn();

            String result = rsp.getResponse().getContentAsString();
            System.out.println("####Respond: " + result);

            String expectRsp = "{\"version\":\"0.1\",\"seq\":100,\"result\":-1,\"message\":\"Unsupported method: notExistMethod\",\"data\":null}";
            Assert.assertTrue(result.contains(expectRsp));
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
    }

}
