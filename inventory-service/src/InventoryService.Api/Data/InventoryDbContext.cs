using InventoryService.Api.Models;
using Microsoft.EntityFrameworkCore;

namespace InventoryService.Api.Data;

public class InventoryDbContext : DbContext
{
    public InventoryDbContext(DbContextOptions<InventoryDbContext> options)
        : base(options)
    {
    }

    public DbSet<InventoryItem> InventoryItems => Set<InventoryItem>();
    public DbSet<Category> Categories => Set<Category>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        modelBuilder.Entity<Category>(entity =>
        {
            entity.HasIndex(e => e.Name).IsUnique();
            entity.HasMany(e => e.Items)
                  .WithOne(e => e.Category)
                  .HasForeignKey(e => e.CategoryId)
                  .OnDelete(DeleteBehavior.Restrict);
        });

        modelBuilder.Entity<InventoryItem>(entity =>
        {
            entity.HasIndex(e => e.Sku).IsUnique();
            entity.HasIndex(e => e.Name);
            entity.HasIndex(e => e.CategoryId);
        });

        // Seed data
        modelBuilder.Entity<Category>().HasData(
            new Category { Id = 1, Name = "Electronics", Description = "Electronic devices and components", IsActive = true },
            new Category { Id = 2, Name = "Office Supplies", Description = "General office supplies and stationery", IsActive = true },
            new Category { Id = 3, Name = "Raw Materials", Description = "Raw materials for manufacturing", IsActive = true },
            new Category { Id = 4, Name = "Finished Goods", Description = "Completed products ready for sale", IsActive = true }
        );

        modelBuilder.Entity<InventoryItem>().HasData(
            new InventoryItem { Id = 1, Sku = "ELEC-001", Name = "Wireless Mouse", Description = "Ergonomic wireless mouse with USB receiver", CategoryId = 1, QuantityOnHand = 150, ReorderLevel = 25, UnitPrice = 29.99m, Location = "Warehouse A - Shelf 1", IsActive = true },
            new InventoryItem { Id = 2, Sku = "ELEC-002", Name = "USB-C Hub", Description = "7-port USB-C hub with HDMI output", CategoryId = 1, QuantityOnHand = 75, ReorderLevel = 15, UnitPrice = 49.99m, Location = "Warehouse A - Shelf 2", IsActive = true },
            new InventoryItem { Id = 3, Sku = "OFFC-001", Name = "A4 Paper Ream", Description = "500 sheets of 80gsm white A4 paper", CategoryId = 2, QuantityOnHand = 500, ReorderLevel = 100, UnitPrice = 5.99m, Location = "Warehouse B - Shelf 1", IsActive = true },
            new InventoryItem { Id = 4, Sku = "OFFC-002", Name = "Ballpoint Pens (Box)", Description = "Box of 50 blue ballpoint pens", CategoryId = 2, QuantityOnHand = 200, ReorderLevel = 50, UnitPrice = 12.99m, Location = "Warehouse B - Shelf 2", IsActive = true },
            new InventoryItem { Id = 5, Sku = "RAW-001", Name = "Steel Sheet (1m x 2m)", Description = "1mm thick cold-rolled steel sheet", CategoryId = 3, QuantityOnHand = 30, ReorderLevel = 10, UnitPrice = 89.99m, Location = "Warehouse C - Bay 1", IsActive = true },
            new InventoryItem { Id = 6, Sku = "FIN-001", Name = "Assembled Circuit Board", Description = "Pre-assembled PCB for Model X controller", CategoryId = 4, QuantityOnHand = 45, ReorderLevel = 20, UnitPrice = 149.99m, Location = "Warehouse A - Shelf 5", IsActive = true }
        );
    }
}
