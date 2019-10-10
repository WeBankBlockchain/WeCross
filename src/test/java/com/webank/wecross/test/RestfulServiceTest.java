package com.webank.wecross.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.webank.wecross.restserver.RestfulService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

// To run with: gradle test --tests RestfulServiceTest

@RunWith(SpringRunner.class)
@WebMvcTest(RestfulService.class)
// @ContextConfiguration(locations = "classpath:WeCrossContext.xml")
public class RestfulServiceTest {
    @Autowired private MockMvc mockMvc;

    @Test
    public void existTest() throws Exception {
        MvcResult rsp =
                this.mockMvc
                        .perform(get("/payment/bcos/HelloWorldContract/exists"))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn();

        String result = rsp.getResponse().getContentAsString();
        System.out.println("####Respond: " + result);
    }

    @Test
    public void shouldReturnDefaultMessage() throws Exception {

        String json =
                "{\n"
                        + "\"version\":\"0.1\",\n"
                        + "\"uri\":\"payment.bcos.HelloWorldContract\",\n"
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
                                post("/payment/bcos/HelloWorldContract/call")
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
