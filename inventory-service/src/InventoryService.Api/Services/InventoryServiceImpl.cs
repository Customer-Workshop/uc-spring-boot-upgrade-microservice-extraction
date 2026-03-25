using InventoryService.Api.Data;
using InventoryService.Api.Models;
using InventoryService.Api.Models.Dtos;
using Microsoft.EntityFrameworkCore;

namespace InventoryService.Api.Services;

public class InventoryServiceImpl : IInventoryService
{
    private readonly InventoryDbContext _context;
    private readonly ILogger<InventoryServiceImpl> _logger;

    public InventoryServiceImpl(InventoryDbContext context, ILogger<InventoryServiceImpl> logger)
    {
        _context = context;
        _logger = logger;
    }

    public async Task<InventoryItemPagedResponse> GetAllAsync(
        int page, int pageSize, string? search, int? categoryId, bool? isActive)
    {
        var query = _context.InventoryItems
            .Include(i => i.Category)
            .AsQueryable();

        if (!string.IsNullOrWhiteSpace(search))
        {
            query = query.Where(i =>
                i.Name.Contains(search) ||
                i.Sku.Contains(search) ||
                (i.Description != null && i.Description.Contains(search)));
        }

        if (categoryId.HasValue)
        {
            query = query.Where(i => i.CategoryId == categoryId.Value);
        }

        if (isActive.HasValue)
        {
            query = query.Where(i => i.IsActive == isActive.Value);
        }

        var totalCount = await query.CountAsync();
        var totalPages = (int)Math.Ceiling((double)totalCount / pageSize);

        var items = await query
            .OrderBy(i => i.Name)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .Select(i => MapToDto(i))
            .ToListAsync();

        return new InventoryItemPagedResponse(items, totalCount, page, pageSize, totalPages);
    }

    public async Task<InventoryItemDto?> GetByIdAsync(int id)
    {
        var item = await _context.InventoryItems
            .Include(i => i.Category)
            .FirstOrDefaultAsync(i => i.Id == id);

        return item != null ? MapToDto(item) : null;
    }

    public async Task<InventoryItemDto?> GetBySkuAsync(string sku)
    {
        var item = await _context.InventoryItems
            .Include(i => i.Category)
            .FirstOrDefaultAsync(i => i.Sku == sku);

        return item != null ? MapToDto(item) : null;
    }

    public async Task<InventoryItemDto> CreateAsync(CreateInventoryItemDto dto, string? userId)
    {
        var item = new InventoryItem
        {
            Sku = dto.Sku,
            Name = dto.Name,
            Description = dto.Description,
            CategoryId = dto.CategoryId,
            QuantityOnHand = dto.QuantityOnHand,
            ReorderLevel = dto.ReorderLevel,
            UnitPrice = dto.UnitPrice,
            Location = dto.Location,
            IsActive = true,
            CreatedAt = DateTime.UtcNow,
            UpdatedAt = DateTime.UtcNow,
            CreatedBy = userId,
            UpdatedBy = userId
        };

        _context.InventoryItems.Add(item);
        await _context.SaveChangesAsync();

        _logger.LogInformation("Created inventory item {Sku} with ID {Id}", item.Sku, item.Id);

        return await GetByIdAsync(item.Id) ?? MapToDto(item);
    }

    public async Task<InventoryItemDto?> UpdateAsync(int id, UpdateInventoryItemDto dto, string? userId)
    {
        var item = await _context.InventoryItems.FindAsync(id);
        if (item == null) return null;

        if (dto.Name != null) item.Name = dto.Name;
        if (dto.Description != null) item.Description = dto.Description;
        if (dto.CategoryId.HasValue) item.CategoryId = dto.CategoryId.Value;
        if (dto.QuantityOnHand.HasValue) item.QuantityOnHand = dto.QuantityOnHand.Value;
        if (dto.ReorderLevel.HasValue) item.ReorderLevel = dto.ReorderLevel.Value;
        if (dto.UnitPrice.HasValue) item.UnitPrice = dto.UnitPrice.Value;
        if (dto.Location != null) item.Location = dto.Location;
        if (dto.IsActive.HasValue) item.IsActive = dto.IsActive.Value;

        item.UpdatedAt = DateTime.UtcNow;
        item.UpdatedBy = userId;

        await _context.SaveChangesAsync();

        _logger.LogInformation("Updated inventory item {Id}", id);

        return await GetByIdAsync(id);
    }

    public async Task<bool> DeleteAsync(int id)
    {
        var item = await _context.InventoryItems.FindAsync(id);
        if (item == null) return false;

        _context.InventoryItems.Remove(item);
        await _context.SaveChangesAsync();

        _logger.LogInformation("Deleted inventory item {Id}", id);

        return true;
    }

    public async Task<InventoryItemDto?> AdjustQuantityAsync(int id, AdjustQuantityDto dto, string? userId)
    {
        var item = await _context.InventoryItems.FindAsync(id);
        if (item == null) return null;

        var newQuantity = item.QuantityOnHand + dto.Adjustment;
        if (newQuantity < 0)
        {
            throw new InvalidOperationException(
                $"Cannot adjust quantity by {dto.Adjustment}. Current stock is {item.QuantityOnHand}.");
        }

        item.QuantityOnHand = newQuantity;
        item.UpdatedAt = DateTime.UtcNow;
        item.UpdatedBy = userId;

        await _context.SaveChangesAsync();

        _logger.LogInformation(
            "Adjusted inventory item {Id} quantity by {Adjustment}. Reason: {Reason}. New quantity: {NewQuantity}",
            id, dto.Adjustment, dto.Reason, newQuantity);

        return await GetByIdAsync(id);
    }

    public async Task<IEnumerable<InventoryItemDto>> GetLowStockItemsAsync()
    {
        return await _context.InventoryItems
            .Include(i => i.Category)
            .Where(i => i.IsActive && i.QuantityOnHand <= i.ReorderLevel)
            .OrderBy(i => i.QuantityOnHand)
            .Select(i => MapToDto(i))
            .ToListAsync();
    }

    private static InventoryItemDto MapToDto(InventoryItem item)
    {
        return new InventoryItemDto(
            item.Id,
            item.Sku,
            item.Name,
            item.Description,
            item.CategoryId,
            item.Category?.Name,
            item.QuantityOnHand,
            item.ReorderLevel,
            item.UnitPrice,
            item.Location,
            item.IsActive,
            item.CreatedAt,
            item.UpdatedAt
        );
    }
}
