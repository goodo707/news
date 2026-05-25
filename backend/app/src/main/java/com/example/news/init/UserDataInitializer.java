package com.example.news.init;

import com.example.news.core.domain.Category;
import com.example.news.core.domain.User;
import com.example.news.core.domain.UserCategory;
import com.example.news.core.domain.UserCategoryId;
import com.example.news.core.repository.CategoryRepository;
import com.example.news.core.repository.UserCategoryRepository;
import com.example.news.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Excel 의 사용자 100명을 최초 부팅 시 1회 적재한다.
 *
 * <p>RSS 수집({@code RssInitialFetchRunner} @Order(2))이 카테고리를 찾을 때
 * 이 Runner 가 적재한 category 행을 기대하므로 @Order(1) 로 선행 실행.
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class UserDataInitializer implements ApplicationRunner {

    private static final String EXCEL_FILE = "데이터.xlsx";

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final UserCategoryRepository userCategoryRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        // 앱 재시작 시 Excel 을 다시 읽지 않도록 가드 — DB 가 SSOT
        if (userRepository.count() > 0) {
            log.info("사용자 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        log.info("사용자 데이터 초기화를 시작합니다.");

        ClassPathResource resource = new ClassPathResource(EXCEL_FILE);
        try (Workbook workbook = new XSSFWorkbook(resource.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // Excel 의 No 컬럼을 users.id 로 그대로 사용 (auto-increment 미사용)
                long no = (long) row.getCell(0).getNumericCellValue();
                String name = row.getCell(1).getStringCellValue();
                String deviceId = row.getCell(2).getStringCellValue();
                // Excel 에는 "APNs" / "FCM" 으로 적혀 있으므로 대문자로 정규화 (DispatchService 의 switch 가 "APNS" 기대)
                String pushType = row.getCell(3).getStringCellValue().toUpperCase();
                String categories = row.getCell(4).getStringCellValue();
                String dndTime = row.getCell(5).getStringCellValue();

                // "-" = 방해 금지 시간 미설정 → 두 컬럼 모두 NULL 로 저장 → DndChecker 는 항상 false 반환
                String dndStart = null;
                String dndEnd = null;
                if (!"-".equals(dndTime)) {
                    String[] parts = dndTime.split("-");
                    dndStart = parts[0];
                    dndEnd = parts[1];
                }

                userRepository.save(
                    new User(no, name, deviceId, pushType, dndStart, dndEnd)
                );

                // 카테고리는 여기서 동적으로 생성 — Excel 에 등장하는 5개가 곧 시스템 전체 카테고리.
                // RSS 수집 시 RssCollectorService 가 findByName 으로 이 행들을 참조.
                for (String catName : categories.split(",")) {
                    Category category = categoryRepository.findByName(catName.trim())
                        .orElseGet(() -> categoryRepository.save(new Category(catName.trim())));

                    userCategoryRepository.save(
                        new UserCategory(new UserCategoryId(no, category.getId()))
                    );
                }
            }
        }

        log.info("사용자 데이터 초기화 완료: {}명", userRepository.count());
    }
}
