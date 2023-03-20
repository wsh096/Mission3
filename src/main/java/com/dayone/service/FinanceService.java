package com.dayone.service;

import com.dayone.model.Company;
import com.dayone.model.Dividend;
import com.dayone.model.ScrapedResult;
import com.dayone.persist.entity.CompanyEntity;
import com.dayone.persist.entity.DividendEntity;
import com.dayone.persist.repository.CompanyRepository;
import com.dayone.persist.repository.DividendRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.dayone.model.constants.CacheKey.KEY_FINANCE;

@Service
@AllArgsConstructor
public class FinanceService {
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    //요청이 자주 들어오는가?
    //자주 변경되는 데이터인가? //cahce 적합성 판단을 위한 질문들
    @Cacheable(key = "#companyName", value = KEY_FINANCE)
    public ScrapedResult getDividendByCompanyName(String companyName) {
        //1. 회사명을 기준으로 회사 정보를 조회
        CompanyEntity company = this.companyRepository.findByName(companyName)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회사명입니다."));

        //2. 조회된 회사 ID로 배당금 정보 조회
        List<DividendEntity> dividendEntities =
                this.dividendRepository.findAllByCompanyId(company.getId());

        //3. 결과 조합 후 반환
//        List<Dividend> dividends = new ArrayList<>();
//        for (var entity : dividendEntities) {
//            dividends.add(Dividend.builder()
//                    .date(entity.getDate())
//                    .dividend(entity.getDividend())
//                    .build());
//        }
//
        List<Dividend> dividends = dividendEntities.stream()
                .map(e -> new Dividend(e.getDate(), e.getDividend()))
                .collect(Collectors.toList());

        return new ScrapedResult(
                new Company(company.getTicker()
                        , company.getName()
                            ), dividends);
    }
}
