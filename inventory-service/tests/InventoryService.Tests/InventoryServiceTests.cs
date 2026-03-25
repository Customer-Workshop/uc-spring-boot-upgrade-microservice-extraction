using FluentAssertions;
using InventoryService.Api.Data;
using InventoryService.Api.Models.Dtos;
using InventoryService.Api.Services;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using Moq;
using Xunit;

namespace InventoryService.Tests;

public class InventoryServiceTests : IDisposable
{
    private readonly InventoryDbContext _context;
    private readonly InventoryServiceImpl _service;

    public InventoryServiceTests()
    {
        var options = new DbContextOptionsBuilder<InventoryDbContext>()
            .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
            .Options;

        _context = new InventoryDbContext(options);
        _context.Database.EnsureCreated();

        var logger = Mock.Of<ILogger<InventoryServiceImpl>>();
        _service = new InventoryServiceImpl(_context, logger);
    }

    [Fact]
    public async Task GetAllAsync_ReturnsPagedResults()
    {
        var result = await _service.GetAllAsync(1, 10, null, null, null);

        result.Should().NotBeNull();
        result.Items.Should().NotBeEmpty();
        result.Page.Should().Be(1);
        result.PageSize.Should().Be(10);
    }

    [Fact]
    public async Task GetByIdAsync_WithValidId_ReturnsItem()
    {
        var result = await _service.GetByIdAsync(1);

        result.Should().NotBeNull();
        result!.Id.Should().Be(1);
        result.Sku.Should().Be("ELEC-001");
    }

    [Fact]
    public async Task GetByIdAsync_WithInvalidId_ReturnsNull()
    {
        var result = await _service.GetByIdAsync(999);
        result.Should().BeNull();
    }

    [Fact]
    public async Task GetBySkuAsync_WithValidSku_ReturnsItem()
    {
        var result = await _service.GetBySkuAsync("ELEC-001");

        result.Should().NotBeNull();
        result!.Sku.Should().Be("ELEC-001");
    }

    [Fact]
    public async Task CreateAsync_CreatesNewItem()
    {
        var dto = new CreateInventoryItemDto(
            "TEST-001",
            "Test Item",
            "Test description",
            1,
            100,
            10,
            19.99m,
            "Test Location"
        );

        var result = await _service.CreateAsync(dto, "test-user");

        result.Should().NotBeNull();
        result.Sku.Should().Be("TEST-001");
        result.Name.Should().Be("Test Item");
        result.QuantityOnHand.Should().Be(100);
    }

    [Fact]
    public async Task UpdateAsync_WithValidId_UpdatesItem()
    {
        var dto = new UpdateInventoryItemDto(
            "Updated Name",
            null, null, 200, null, null, null, null
        );

        var result = await _service.UpdateAsync(1, dto, "test-user");

        result.Should().NotBeNull();
        result!.Name.Should().Be("Updated Name");
        result.QuantityOnHand.Should().Be(200);
    }

    [Fact]
    public async Task DeleteAsync_WithValidId_ReturnsTrue()
    {
        var dto = new CreateInventoryItemDto(
            "DEL-001", "Delete Me", null, 1, 5, 1, 1.00m, null
        );
        var created = await _service.CreateAsync(dto, "test-user");

        var result = await _service.DeleteAsync(created.Id);
        result.Should().BeTrue();
    }

    [Fact]
    public async Task AdjustQuantityAsync_IncreasesStock()
    {
        var adjustDto = new AdjustQuantityDto(50, "Restock");
        var original = await _service.GetByIdAsync(1);

        var result = await _service.AdjustQuantityAsync(1, adjustDto, "test-user");

        result.Should().NotBeNull();
        result!.QuantityOnHand.Should().Be(original!.QuantityOnHand + 50);
    }

    [Fact]
    public async Task AdjustQuantityAsync_DecreaseBelowZero_ThrowsException()
    {
        var adjustDto = new AdjustQuantityDto(-99999, "Over-decrement");

        var action = () => _service.AdjustQuantityAsync(1, adjustDto, "test-user");

        await action.Should().ThrowAsync<InvalidOperationException>();
    }

    [Fact]
    public async Task GetLowStockItemsAsync_ReturnsItemsBelowReorderLevel()
    {
        var result = await _service.GetLowStockItemsAsync();
        result.Should().NotBeNull();
    }

    [Fact]
    public async Task GetAllAsync_WithSearchFilter_FiltersResults()
    {
        var result = await _service.GetAllAsync(1, 10, "Wireless", null, null);

        result.Should().NotBeNull();
        result.Items.Should().Contain(i => i.Name.Contains("Wireless"));
    }

    [Fact]
    public async Task GetAllAsync_WithCategoryFilter_FiltersResults()
    {
        var result = await _service.GetAllAsync(1, 10, null, 1, null);

        result.Should().NotBeNull();
        result.Items.Should().OnlyContain(i => i.CategoryId == 1);
    }

    public void Dispose()
    {
        _context.Dispose();
    }
}
