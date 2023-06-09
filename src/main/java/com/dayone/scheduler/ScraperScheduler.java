package com.dayone.scheduler;

import com.dayone.Scraper.Scraper;
import com.dayone.model.Company;
import com.dayone.persist.entity.CompanyEntity;
import com.dayone.persist.entity.DividendEntity;
import com.dayone.persist.repository.CompanyRepository;
import com.dayone.persist.repository.DividendRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.dayone.model.constants.CacheKey.KEY_FINANCE;

@Slf4j // logging
@Component
@EnableCaching
@AllArgsConstructor //repository 초기화를 목적으로 함
public class ScraperScheduler {
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;
    private final Scraper yahooFinanceScraper;

    @CacheEvict(value = KEY_FINANCE,allEntries = true)//모든 값 지우는 걸로
    @Scheduled(cron = "${scheduler.scrap.yahoo}") // 배당금 정보가 중복으로 저장되는 것 막아주기
    public void yahooFinanceScheduling() {
        log.info("scraping scheduler is started");
        //저장된 회사 목록을 조회
        List<CompanyEntity> companies = this.companyRepository.findAll();
        //회사마다 배당금 정보를 새로 스크래핑
        //스크패링한 배당금 정보 중 데이터베스에 없는 값은 저장
        for (var company : companies) {
            log.info("scraping scheduler is started -> " + company.getName());
            this.yahooFinanceScraper.scrap(new Company(
                            company.getName(),
                            company.getTicker()))
                    .getDividends()
                    .stream()
                    //dividend model -> dividend entity mapping
                    .map(e -> new DividendEntity(company.getId(), e))
                    //element each other, dividend repository insert
                    .forEach(e -> {
                        boolean exists = this.dividendRepository.existsByCompanyIdAndDate(
                                e.getCompanyId(), e.getDate());
                        if (!exists) {
                            this.dividendRepository.save(e);
                            log.info("insert new dividend -> " + e.toString());
                        }
                    });
            //연속적인 요청으로 인한 데이터 손실 발생 방지를 위한 sleep
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }
}
