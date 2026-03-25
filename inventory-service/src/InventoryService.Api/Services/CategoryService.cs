using InventoryService.Api.Data;
using InventoryService.Api.Models;
using InventoryService.Api.Models.Dtos;
using Microsoft.EntityFrameworkCore;

namespace InventoryService.Api.Services;

public class CategoryService : ICategoryService
{
    private readonly InventoryDbContext _context;
    private readonly ILogger<CategoryService> _logger;

    public CategoryService(InventoryDbContext context, ILogger<CategoryService> logger)
    {
        _context = context;
        _logger = logger;
    }

    public async Task<IEnumerable<CategoryDto>> GetAllAsync()
    {
        return await _context.Categories
            .Include(c => c.Items)
            .OrderBy(c => c.Name)
            .Select(c => new CategoryDto(
                c.Id,
                c.Name,
                c.Description,
                c.IsActive,
                c.Items.Count,
                c.CreatedAt
            ))
            .ToListAsync();
    }

    public async Task<CategoryDto?> GetByIdAsync(int id)
    {
        var category = await _context.Categories
            .Include(c => c.Items)
            .FirstOrDefaultAsync(c => c.Id == id);

        if (category == null) return null;

        return new CategoryDto(
            category.Id,
            category.Name,
            category.Description,
            category.IsActive,
            category.Items.Count,
            category.CreatedAt
        );
    }

    public async Task<CategoryDto> CreateAsync(CreateCategoryDto dto)
    {
        var category = new Category
        {
            Name = dto.Name,
            Description = dto.Description,
            IsActive = true,
            CreatedAt = DateTime.UtcNow,
            UpdatedAt = DateTime.UtcNow
        };

        _context.Categories.Add(category);
        await _context.SaveChangesAsync();

        _logger.LogInformation("Created category {Name} with ID {Id}", category.Name, category.Id);

        return new CategoryDto(category.Id, category.Name, category.Description, category.IsActive, 0, category.CreatedAt);
    }

    public async Task<CategoryDto?> UpdateAsync(int id, UpdateCategoryDto dto)
    {
        var category = await _context.Categories.FindAsync(id);
        if (category == null) return null;

        if (dto.Name != null) category.Name = dto.Name;
        if (dto.Description != null) category.Description = dto.Description;
        if (dto.IsActive.HasValue) category.IsActive = dto.IsActive.Value;

        category.UpdatedAt = DateTime.UtcNow;

        await _context.SaveChangesAsync();

        _logger.LogInformation("Updated category {Id}", id);

        return await GetByIdAsync(id);
    }

    public async Task<bool> DeleteAsync(int id)
    {
        var category = await _context.Categories
            .Include(c => c.Items)
            .FirstOrDefaultAsync(c => c.Id == id);

        if (category == null) return false;

        if (category.Items.Any())
        {
            throw new InvalidOperationException("Cannot delete category with existing inventory items.");
        }

        _context.Categories.Remove(category);
        await _context.SaveChangesAsync();

        _logger.LogInformation("Deleted category {Id}", id);

        return true;
    }
}
