using Microsoft.EntityFrameworkCore;
using InventoryService.Models;

namespace InventoryService.Data;

public class InventoryDbContext : DbContext
{
    public InventoryDbContext(DbContextOptions<InventoryDbContext> options)
        : base(options)
    {
    }

    public DbSet<InventoryItem> InventoryItems => Set<InventoryItem>();
    public DbSet<StockMovement> StockMovements => Set<StockMovement>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        modelBuilder.Entity<InventoryItem>(entity =>
        {
            entity.HasIndex(e => e.Sku).IsUnique();
            entity.HasIndex(e => e.Category);
            entity.HasIndex(e => e.IsActive);
            entity.Property(e => e.UnitPrice).HasPrecision(18, 2);
        });

        modelBuilder.Entity<StockMovement>(entity =>
        {
            entity.HasIndex(e => e.InventoryItemId);
            entity.HasIndex(e => e.CreatedAt);
            entity.HasOne(e => e.InventoryItem)
                .WithMany()
                .HasForeignKey(e => e.InventoryItemId)
                .OnDelete(DeleteBehavior.Cascade);
        });

        SeedData(modelBuilder);
    }

    private static void SeedData(ModelBuilder modelBuilder)
    {
        var now = new DateTime(2024, 1, 1, 0, 0, 0, DateTimeKind.Utc);

        modelBuilder.Entity<InventoryItem>().HasData(
            new InventoryItem
            {
                Id = Guid.Parse("a1b2c3d4-e5f6-7890-abcd-ef1234567890"),
                Sku = "WDG-001",
                Name = "Standard Widget",
                Description = "A standard widget for general use",
                Category = "Widgets",
                Quantity = 150,
                UnitPrice = 9.99m,
                ReorderLevel = 20,
                WarehouseLocation = "A1-01",
                Supplier = "Acme Corp",
                IsActive = true,
                CreatedAt = now,
                UpdatedAt = now,
                CreatedBy = "system",
                UpdatedBy = "system"
            },
            new InventoryItem
            {
                Id = Guid.Parse("b2c3d4e5-f6a7-8901-bcde-f12345678901"),
                Sku = "WDG-002",
                Name = "Premium Widget",
                Description = "A premium widget with enhanced features",
                Category = "Widgets",
                Quantity = 75,
                UnitPrice = 24.99m,
                ReorderLevel = 15,
                WarehouseLocation = "A1-02",
                Supplier = "Acme Corp",
                IsActive = true,
                CreatedAt = now,
                UpdatedAt = now,
                CreatedBy = "system",
                UpdatedBy = "system"
            },
            new InventoryItem
            {
                Id = Guid.Parse("c3d4e5f6-a7b8-9012-cdef-123456789012"),
                Sku = "GDG-001",
                Name = "Basic Gadget",
                Description = "An entry-level gadget",
                Category = "Gadgets",
                Quantity = 200,
                UnitPrice = 14.50m,
                ReorderLevel = 30,
                WarehouseLocation = "B2-01",
                Supplier = "TechParts Inc",
                IsActive = true,
                CreatedAt = now,
                UpdatedAt = now,
                CreatedBy = "system",
                UpdatedBy = "system"
            },
            new InventoryItem
            {
                Id = Guid.Parse("d4e5f6a7-b8c9-0123-defa-234567890123"),
                Sku = "GDG-002",
                Name = "Advanced Gadget",
                Description = "A feature-rich gadget for power users",
                Category = "Gadgets",
                Quantity = 5,
                UnitPrice = 49.99m,
                ReorderLevel = 10,
                WarehouseLocation = "B2-02",
                Supplier = "TechParts Inc",
                IsActive = true,
                CreatedAt = now,
                UpdatedAt = now,
                CreatedBy = "system",
                UpdatedBy = "system"
            },
            new InventoryItem
            {
                Id = Guid.Parse("e5f6a7b8-c9d0-1234-efab-345678901234"),
                Sku = "CMP-001",
                Name = "Micro Component",
                Description = "Miniaturized electronic component",
                Category = "Components",
                Quantity = 500,
                UnitPrice = 2.25m,
                ReorderLevel = 100,
                WarehouseLocation = "C3-01",
                Supplier = "MicroElectro Ltd",
                IsActive = true,
                CreatedAt = now,
                UpdatedAt = now,
                CreatedBy = "system",
                UpdatedBy = "system"
            }
        );
    }
}
