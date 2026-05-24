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

                long no = (long) row.getCell(0).getNumericCellValue();
                String name = row.getCell(1).getStringCellValue();
                String deviceId = row.getCell(2).getStringCellValue();
                String pushType = row.getCell(3).getStringCellValue().toUpperCase();
                String categories = row.getCell(4).getStringCellValue();
                String dndTime = row.getCell(5).getStringCellValue();

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
