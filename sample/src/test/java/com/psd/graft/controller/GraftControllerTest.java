package com.psd.graft.controller;

import com.psd.graft.GraftSpringBootApplication;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * graft测试
 *
 * @author Created by gold on 2019/10/11 09:42
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {GraftSpringBootApplication.class})
@AutoConfigureMockMvc
public class GraftControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Test
    public void graftTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/graft/get/success"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void graftFailureTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/graft/get/failure"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

}