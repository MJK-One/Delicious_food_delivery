package com.dfdt.delivery.deploy_test.controller;

import com.dfdt.delivery.deploy_test.DpRepository;
import com.dfdt.delivery.deploy_test.entity.Dp;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DeployTestController {
    @Autowired
    private final DpRepository dpRepo;

    @GetMapping("/test_1")
    public String getTest01() {
        return "test_01";
    }

    // 데이터 저장 테스트
    @PostMapping("/test/save")
    public String saveMember(@RequestParam String name, @RequestParam String email) {
        Dp member = new Dp();
        member.setName(name);
        member.setEmail(email);
        dpRepo.save(member);
        return "저장 성공! 이름: " + name;
    }
    // 데이터 조회 테스트
    @GetMapping("/test/all")
    public List<Dp> getAllMembers() {
        return dpRepo.findAll();
    }
}