package com.student.expensetracker.config;

import com.student.expensetracker.entity.Category;
import com.student.expensetracker.repository.CategoryRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Preloads the seven default categories from PRD 4.3 (user_id NULL = global).
 * Idempotent: safe to run on every boot, existing rows are left untouched.
 */
@Configuration
public class DefaultCategorySeeder {

    private static final Logger log = LoggerFactory.getLogger(DefaultCategorySeeder.class);

    /**
     * Category colours are *data*, not UI chrome -- they intentionally extend
     * beyond the deliberately monochrome Apple UI palette, because a pie chart
     * needs mutually distinguishable hues. All seven are Apple system colours.
     */
    static final List<Category> DEFAULTS = List.of(
            Category.builder().name("Food & Dining").colorHex("#FF9500").build(),
            Category.builder().name("Rent & Housing").colorHex("#5856D6").build(),
            Category.builder().name("Books & Academics").colorHex("#0071E3").build(),
            Category.builder().name("Transportation").colorHex("#30B0C7").build(),
            Category.builder().name("Entertainment").colorHex("#FF2D55").build(),
            Category.builder().name("Utilities").colorHex("#34C759").build(),
            Category.builder().name("Personal Care").colorHex("#AF52DE").build());

    @Bean
    ApplicationRunner seedDefaultCategories(CategoryRepository categoryRepository) {
        return args -> seed(categoryRepository);
    }

    @Transactional
    void seed(CategoryRepository categoryRepository) {
        List<Category> missing = DEFAULTS.stream()
                .filter(template -> categoryRepository.countGlobalByName(template.getName()) == 0)
                .map(template -> Category.builder()
                        .name(template.getName())
                        .colorHex(template.getColorHex())
                        .user(null)
                        .build())
                .toList();

        if (missing.isEmpty()) {
            log.info("Default categories already present ({} total)", DEFAULTS.size());
            return;
        }
        categoryRepository.saveAll(missing);
        log.info("Seeded {} default categories: {}", missing.size(),
                missing.stream().map(Category::getName).toList());
    }
}