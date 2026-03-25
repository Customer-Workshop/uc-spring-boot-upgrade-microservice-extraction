using Microsoft.EntityFrameworkCore;
using InventoryService.Data;
using InventoryService.Models;
using InventoryService.Models.DTOs;

namespace InventoryService.Services;

public class InventoryServiceImpl : IInventoryService
{
    private readonly InventoryDbContext _context;
    private readonly ILogger<InventoryServiceImpl> _logger;

    public InventoryServiceImpl(InventoryDbContext context, ILogger<InventoryServiceImpl> logger)
    {
        _context = context;
        _logger = logger;
    }

    public async Task<PagedResult<InventoryItemDto>> GetItemsAsync(InventorySearchParams searchParams)
    {
        var query = _context.InventoryItems.AsQueryable();

        if (!string.IsNullOrWhiteSpace(searchParams.SearchTerm))
        {
            var term = searchParams.SearchTerm.ToLower();
            query = query.Where(i =>
                i.Name.ToLower().Contains(term) ||
                i.Sku.ToLower().Contains(term) ||
                (i.Description != null && i.Description.ToLower().Contains(term)));
        }

        if (!string.IsNullOrWhiteSpace(searchParams.Category))
        {
            query = query.Where(i => i.Category == searchParams.Category);
        }

        if (searchParams.IsActive.HasValue)
        {
            query = query.Where(i => i.IsActive == searchParams.IsActive.Value);
        }

        if (searchParams.LowStock == true)
        {
            query = query.Where(i => i.Quantity <= i.ReorderLevel);
        }

        var totalCount = await query.CountAsync();

        query = searchParams.SortBy.ToLower() switch
        {
            "sku" => searchParams.SortDirection.ToLower() == "desc"
                ? query.OrderByDescending(i => i.Sku)
                : query.OrderBy(i => i.Sku),
            "quantity" => searchParams.SortDirection.ToLower() == "desc"
                ? query.OrderByDescending(i => i.Quantity)
                : query.OrderBy(i => i.Quantity),
            "unitprice" => searchParams.SortDirection.ToLower() == "desc"
                ? query.OrderByDescending(i => i.UnitPrice)
                : query.OrderBy(i => i.UnitPrice),
            "category" => searchParams.SortDirection.ToLower() == "desc"
                ? query.OrderByDescending(i => i.Category)
                : query.OrderBy(i => i.Category),
            "createdat" => searchParams.SortDirection.ToLower() == "desc"
                ? query.OrderByDescending(i => i.CreatedAt)
                : query.OrderBy(i => i.CreatedAt),
            _ => searchParams.SortDirection.ToLower() == "desc"
                ? query.OrderByDescending(i => i.Name)
                : query.OrderBy(i => i.Name)
        };

        var items = await query
            .Skip((searchParams.Page - 1) * searchParams.PageSize)
            .Take(searchParams.PageSize)
            .Select(i => MapToDto(i))
            .ToListAsync();

        return new PagedResult<InventoryItemDto>(
            items,
            totalCount,
            searchParams.Page,
            searchParams.PageSize,
            (int)Math.Ceiling(totalCount / (double)searchParams.PageSize)
        );
    }

    public async Task<InventoryItemDto?> GetItemByIdAsync(Guid id)
    {
        var item = await _context.InventoryItems.FindAsync(id);
        return item == null ? null : MapToDto(item);
    }

    public async Task<InventoryItemDto?> GetItemBySkuAsync(string sku)
    {
        var item = await _context.InventoryItems.FirstOrDefaultAsync(i => i.Sku == sku);
        return item == null ? null : MapToDto(item);
    }

    public async Task<InventoryItemDto> CreateItemAsync(CreateInventoryItemDto dto)
    {
        var item = new InventoryItem
        {
            Sku = dto.Sku,
            Name = dto.Name,
            Description = dto.Description,
            Category = dto.Category,
            Quantity = dto.Quantity,
            UnitPrice = dto.UnitPrice,
            ReorderLevel = dto.ReorderLevel,
            WarehouseLocation = dto.WarehouseLocation,
            Supplier = dto.Supplier,
            CreatedBy = "system",
            UpdatedBy = "system"
        };

        _context.InventoryItems.Add(item);

        // Record initial stock movement
        if (dto.Quantity > 0)
        {
            _context.StockMovements.Add(new StockMovement
            {
                InventoryItemId = item.Id,
                MovementType = "IN",
                Quantity = dto.Quantity,
                PreviousQuantity = 0,
                NewQuantity = dto.Quantity,
                Reference = "INITIAL_STOCK",
                Notes = "Initial inventory creation",
                CreatedBy = "system"
            });
        }

        await _context.SaveChangesAsync();

        _logger.LogInformation("Created inventory item {Sku} with quantity {Quantity}", item.Sku, item.Quantity);
        return MapToDto(item);
    }

    public async Task<InventoryItemDto?> UpdateItemAsync(Guid id, UpdateInventoryItemDto dto)
    {
        var item = await _context.InventoryItems.FindAsync(id);
        if (item == null) return null;

        if (dto.Name != null) item.Name = dto.Name;
        if (dto.Description != null) item.Description = dto.Description;
        if (dto.Category != null) item.Category = dto.Category;
        if (dto.UnitPrice.HasValue) item.UnitPrice = dto.UnitPrice.Value;
        if (dto.ReorderLevel.HasValue) item.ReorderLevel = dto.ReorderLevel.Value;
        if (dto.WarehouseLocation != null) item.WarehouseLocation = dto.WarehouseLocation;
        if (dto.Supplier != null) item.Supplier = dto.Supplier;
        if (dto.IsActive.HasValue) item.IsActive = dto.IsActive.Value;

        item.UpdatedAt = DateTime.UtcNow;
        item.UpdatedBy = "system";

        await _context.SaveChangesAsync();

        _logger.LogInformation("Updated inventory item {Id}", id);
        return MapToDto(item);
    }

    public async Task<bool> DeleteItemAsync(Guid id)
    {
        var item = await _context.InventoryItems.FindAsync(id);
        if (item == null) return false;

        _context.InventoryItems.Remove(item);
        await _context.SaveChangesAsync();

        _logger.LogInformation("Deleted inventory item {Id}", id);
        return true;
    }

    public async Task<InventoryItemDto?> AdjustStockAsync(Guid id, StockAdjustmentDto dto)
    {
        var item = await _context.InventoryItems.FindAsync(id);
        if (item == null) return null;

        var previousQuantity = item.Quantity;
        int newQuantity;

        switch (dto.MovementType.ToUpper())
        {
            case "IN":
                newQuantity = item.Quantity + dto.Quantity;
                break;
            case "OUT":
                newQuantity = item.Quantity - dto.Quantity;
                if (newQuantity < 0)
                {
                    _logger.LogWarning("Stock adjustment would result in negative quantity for item {Id}", id);
                    return null;
                }
                break;
            case "ADJUSTMENT":
                newQuantity = dto.Quantity;
                break;
            default:
                _logger.LogWarning("Invalid movement type: {MovementType}", dto.MovementType);
                return null;
        }

        item.Quantity = newQuantity;
        item.UpdatedAt = DateTime.UtcNow;
        item.UpdatedBy = "system";

        _context.StockMovements.Add(new StockMovement
        {
            InventoryItemId = item.Id,
            MovementType = dto.MovementType.ToUpper(),
            Quantity = dto.Quantity,
            PreviousQuantity = previousQuantity,
            NewQuantity = newQuantity,
            Reference = dto.Reference,
            Notes = dto.Notes,
            CreatedBy = "system"
        });

        await _context.SaveChangesAsync();

        _logger.LogInformation(
            "Stock adjusted for item {Id}: {PreviousQty} -> {NewQty} ({MovementType})",
            id, previousQuantity, newQuantity, dto.MovementType);

        return MapToDto(item);
    }

    public async Task<IEnumerable<StockMovementDto>> GetStockMovementsAsync(Guid itemId, int limit = 50)
    {
        return await _context.StockMovements
            .Where(m => m.InventoryItemId == itemId)
            .OrderByDescending(m => m.CreatedAt)
            .Take(limit)
            .Select(m => new StockMovementDto(
                m.Id,
                m.InventoryItemId,
                m.MovementType,
                m.Quantity,
                m.PreviousQuantity,
                m.NewQuantity,
                m.Reference,
                m.Notes,
                m.CreatedAt,
                m.CreatedBy
            ))
            .ToListAsync();
    }

    public async Task<IEnumerable<InventoryItemDto>> GetLowStockItemsAsync()
    {
        return await _context.InventoryItems
            .Where(i => i.IsActive && i.Quantity <= i.ReorderLevel)
            .OrderBy(i => i.Quantity)
            .Select(i => MapToDto(i))
            .ToListAsync();
    }

    public async Task<IEnumerable<string>> GetCategoriesAsync()
    {
        return await _context.InventoryItems
            .Select(i => i.Category)
            .Distinct()
            .OrderBy(c => c)
            .ToListAsync();
    }

    private static InventoryItemDto MapToDto(InventoryItem item)
    {
        return new InventoryItemDto(
            item.Id,
            item.Sku,
            item.Name,
            item.Description,
            item.Category,
            item.Quantity,
            item.UnitPrice,
            item.ReorderLevel,
            item.WarehouseLocation,
            item.Supplier,
            item.IsActive,
            item.CreatedAt,
            item.UpdatedAt
        );
    }
}
