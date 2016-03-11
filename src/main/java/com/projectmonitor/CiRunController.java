package com.projectmonitor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/")
public class CiRunController {

    @Value("${ciUrl}")
    private String ciUrl;

    @RequestMapping(method = RequestMethod.GET)
    public String execute(Model model) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "us");
        headers.set("Accept", "application/vnd.travis-ci.2+json");

        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);


        ResponseEntity<CIResponse> response = restTemplate.exchange(
                ciUrl + "repos/projectmonitor/projectmonitor-web/branches/master",
                HttpMethod.GET,
                entity,
                CIResponse.class
        );

        model.addAttribute("status", response.getBody().getBranch().getState());
        return "status";
    }
}
