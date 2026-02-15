package com.yimiao.appointment.client;

import com.yimiao.api.vo.VaccineeVO;
import com.yimiao.api.vo.VaccineVO;
import com.yimiao.common.core.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "yimiao-vaccine", url = "${feign.vaccine.url:http://localhost:8085}")
public interface VaccineClient {

    @GetMapping("/vaccine/{id}")
    Result<VaccineVO> getVaccine(@PathVariable("id") Long id);

    @GetMapping("/slot/decrement/{id}")
    Result<Boolean> decrementSlot(@PathVariable("id") Long id);

    @GetMapping("/slot/increment/{id}")
    Result<Boolean> incrementSlot(@PathVariable("id") Long id);

    @GetMapping("/stock/decrement/{vaccineId}/{siteId}/{quantity}")
    Result<Boolean> decrementStock(@PathVariable("vaccineId") Long vaccineId,
                                   @PathVariable("siteId") Long siteId,
                                   @PathVariable("quantity") Integer quantity);

    @GetMapping("/stock/increment/{vaccineId}/{siteId}/{quantity}")
    Result<Boolean> incrementStock(@PathVariable("vaccineId") Long vaccineId,
                                   @PathVariable("siteId") Long siteId,
                                   @PathVariable("quantity") Integer quantity);
}
