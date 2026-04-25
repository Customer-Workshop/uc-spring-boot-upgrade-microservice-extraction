package com.sure.budget.service;

import com.sure.budget.dto.*;
import com.sure.budget.entity.Budget;
import com.sure.budget.entity.Category;
import com.sure.budget.entity.Tag;
import com.sure.budget.repository.BudgetRepository;
import com.sure.budget.repository.CategoryRepository;
import com.sure.budget.repository.TagRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BudgetService {

    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;
    private final TagRepository tagRepository;

    public BudgetService(
            CategoryRepository categoryRepository,
            BudgetRepository budgetRepository,
            TagRepository tagRepository) {
        this.categoryRepository = categoryRepository;
        this.budgetRepository = budgetRepository;
        this.tagRepository = tagRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories(String familyId) {
        return categoryRepository.findByFamilyId(familyId).stream()
                .map(this::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryDto createCategory(String familyId, CreateCategoryRequest request) {
        Category category = Category.builder()
                .familyId(familyId)
                .name(request.name())
                .color(request.color())
                .icon(request.icon())
                .parentId(request.parentId())
                .classification(request.classification() != null ? request.classification() : "expense")
                .build();
        category = categoryRepository.save(category);
        return toCategoryDto(category);
    }

    @Transactional
    public void deleteCategory(String categoryId) {
        categoryRepository.deleteById(categoryId);
    }

    @Transactional(readOnly = true)
    public List<BudgetDto> getBudgets(String familyId) {
        return budgetRepository.findByFamilyId(familyId).stream()
                .map(this::toBudgetDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BudgetDto createBudget(String familyId, CreateBudgetRequest request) {
        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId()).orElse(null);
        }

        Budget budget = Budget.builder()
                .familyId(familyId)
                .name(request.name())
                .amount(request.amount())
                .currency(request.currency() != null ? request.currency() : "USD")
                .category(category)
                .periodType(request.periodType() != null ? request.periodType() : "monthly")
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();
        budget = budgetRepository.save(budget);
        return toBudgetDto(budget);
    }

    @Transactional
    public void deleteBudget(String budgetId) {
        budgetRepository.deleteById(budgetId);
    }

    @Transactional(readOnly = true)
    public List<TagDto> getTags(String familyId) {
        return tagRepository.findByFamilyId(familyId).stream()
                .map(this::toTagDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public TagDto createTag(String familyId, CreateTagRequest request) {
        Tag tag = Tag.builder()
                .familyId(familyId)
                .name(request.name())
                .color(request.color())
                .build();
        tag = tagRepository.save(tag);
        return toTagDto(tag);
    }

    @Transactional
    public void deleteTag(String tagId) {
        tagRepository.deleteById(tagId);
    }

    private CategoryDto toCategoryDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getFamilyId(),
                category.getName(),
                category.getColor(),
                category.getIcon(),
                category.getParentId(),
                category.getClassification());
    }

    private BudgetDto toBudgetDto(Budget budget) {
        return new BudgetDto(
                budget.getId(),
                budget.getFamilyId(),
                budget.getCategory() != null ? budget.getCategory().getId() : null,
                budget.getCategory() != null ? budget.getCategory().getName() : null,
                budget.getName(),
                budget.getAmount(),
                budget.getCurrency(),
                budget.getPeriodType(),
                budget.getStartDate(),
                budget.getEndDate());
    }

    private TagDto toTagDto(Tag tag) {
        return new TagDto(tag.getId(), tag.getFamilyId(), tag.getName(), tag.getColor());
    }
}
