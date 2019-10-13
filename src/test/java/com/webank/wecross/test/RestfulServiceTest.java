package com.webank.wecross.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

// To run with: gradle test --tests RestfulServiceTest

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class RestfulServiceTest {

    @Autowired private MockMvc mockMvc;

    @Test
    public void okTest() throws Exception {
        MvcResult rsp =
                this.mockMvc
                        .perform(get("/test"))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn();

        String result = rsp.getResponse().getContentAsString();
        System.out.println("####Respond: " + result);
    }

    @Test
    public void existTest() throws Exception {
        MvcResult rsp =
                this.mockMvc
                        .perform(get("/payment/bcos1/HelloWorldContract/exists"))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn();

        String result = rsp.getResponse().getContentAsString();
        System.out.println("####Respond: " + result);
    }

    @Test
    public void callTest() throws Exception {

        String json =
                "{\n"
                        + "\"version\":\"0.1\",\n"
                        + "\"uri\":\"payment.bcos1.HelloWorldContract\",\n"
                        + "\"method\":\"call\",\n"
                        + "\"sig\":\"\",\n"
                        + "\"data\": {\n"
                        + "\"to\":\"\",\n"
                        + "\"method\":\"get\",\n"
                        + "\"args\":[]\n"
                        + "}\n"
                        + "}";

        MvcResult rsp =
                this.mockMvc
                        .perform(
                                post("/payment/bcos1/HelloWorldContract/call")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(json))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn();

        String result = rsp.getResponse().getContentAsString();
        System.out.println("####Respond: " + result);
    }

    @Test
    public void sendTransactionTest() throws Exception {

        String json =
                "{\n"
                        + "\"version\":\"0.1\",\n"
                        + "\"uri\":\"payment.bcos1.HelloWorldContract\",\n"
                        + "\"method\":\"sendTransaction\",\n"
                        + "\"sig\":\"\",\n"
                        + "\"data\": {\n"
                        + "\"to\":\"\",\n"
                        + "\"method\":\"set\",\n"
                        + "\"args\":[\"aaaaa\"]\n"
                        + "}\n"
                        + "}";

        MvcResult rsp =
                this.mockMvc
                        .perform(
                                post("/payment/bcos1/HelloWorldContract/sendTransaction")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(json))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn();

        String result = rsp.getResponse().getContentAsString();
        System.out.println("####Respond: " + result);
    }

    @Before
    public void beforeTest() {
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
