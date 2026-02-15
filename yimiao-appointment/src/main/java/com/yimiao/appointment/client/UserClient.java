package com.yimiao.appointment.client;

import com.yimiao.api.vo.VaccineeVO;
import com.yimiao.common.core.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "yimiao-user", url = "${feign.user.url:http://localhost:8083}")
public interface UserClient {

    @GetMapping("/vaccinee/{id}")
    Result<VaccineeVO> getVaccinee(@PathVariable("id") Long id);
}
