using System.Reflection;
using Microsoft.EntityFrameworkCore;
using Serilog;
using Prometheus;
using InventoryService.Data;
using InventoryService.Health;
using InventoryService.Middleware;
using InventoryService.Services;

// Configure Serilog
Log.Logger = new LoggerConfiguration()
    .MinimumLevel.Information()
    .WriteTo.Console()
    .CreateLogger();

try
{
    var builder = WebApplication.CreateBuilder(args);

    // Use Serilog
    builder.Host.UseSerilog();

    // Add controllers
    builder.Services.AddControllers();

    // Add Swagger/OpenAPI
    builder.Services.AddEndpointsApiExplorer();
    builder.Services.AddSwaggerGen(options =>
    {
        options.SwaggerDoc("v1", new Microsoft.OpenApi.Models.OpenApiInfo
        {
            Title = "Inventory Service API",
            Version = "v1",
            Description = "Microservice for inventory management - decomposed from the monolith application"
        });

        var xmlFilename = $"{Assembly.GetExecutingAssembly().GetName().Name}.xml";
        var xmlPath = Path.Combine(AppContext.BaseDirectory, xmlFilename);
        if (File.Exists(xmlPath))
            options.IncludeXmlComments(xmlPath);
    });

    // Add DbContext
    var connectionString = builder.Configuration.GetConnectionString("DefaultConnection");
    if (string.IsNullOrEmpty(connectionString))
    {
        // Use InMemory database for development
        builder.Services.AddDbContext<InventoryDbContext>(options =>
            options.UseInMemoryDatabase("InventoryDb"));
    }
    else
    {
        builder.Services.AddDbContext<InventoryDbContext>(options =>
            options.UseSqlServer(connectionString));
    }

    // Add services
    builder.Services.AddScoped<IInventoryService, InventoryServiceImpl>();

    // Add health checks
    builder.Services.AddHealthChecks()
        .AddCheck<InventoryHealthCheck>("inventory_db");

    // Add CORS
    builder.Services.AddCors(options =>
    {
        options.AddPolicy("AllowAll", policy =>
        {
            policy.AllowAnyOrigin()
                .AllowAnyMethod()
                .AllowAnyHeader();
        });
    });

    var app = builder.Build();

    // Ensure database is created with seed data
    using (var scope = app.Services.CreateScope())
    {
        var context = scope.ServiceProvider.GetRequiredService<InventoryDbContext>();
        context.Database.EnsureCreated();
    }

    // Configure middleware pipeline
    app.UseMiddleware<ExceptionMiddleware>();

    app.UseSwagger();
    app.UseSwaggerUI(options =>
    {
        options.SwaggerEndpoint("/swagger/v1/swagger.json", "Inventory Service API v1");
    });

    app.UseSerilogRequestLogging();
    app.UseCors("AllowAll");
    app.UseRouting();

    // Prometheus metrics
    app.UseHttpMetrics();
    app.MapMetrics();

    app.MapControllers();
    app.MapHealthChecks("/health");
    app.MapHealthChecks("/health/ready");
    app.MapHealthChecks("/health/live");

    Log.Information("Inventory Service starting on {Urls}", builder.Configuration["ASPNETCORE_URLS"] ?? "http://localhost:5000");
    app.Run();
}
catch (Exception ex)
{
    Log.Fatal(ex, "Application terminated unexpectedly");
}
finally
{
    Log.CloseAndFlush();
}
