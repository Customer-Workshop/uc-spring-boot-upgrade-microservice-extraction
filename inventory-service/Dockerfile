# Stage 1: Build the .NET application
FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src

# Copy project file and restore dependencies
COPY InventoryService.csproj .
RUN dotnet restore

# Copy remaining source code and build
COPY . .
RUN dotnet publish -c Release -o /app/publish --no-restore

# Stage 2: Build Angular frontend
FROM node:20-alpine AS frontend-build
WORKDIR /app

COPY ClientApp/package*.json ./
RUN npm ci --legacy-peer-deps

COPY ClientApp/ .
RUN npm run build -- --configuration production

# Stage 3: Runtime image
FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS runtime
WORKDIR /app

# Add non-root user for security
RUN addgroup --system --gid 1001 appgroup && \
    adduser --system --uid 1001 --ingroup appgroup appuser

# Copy published app
COPY --from=build /app/publish .

# Copy Angular build output to wwwroot
COPY --from=frontend-build /app/dist/inventory-client/browser ./wwwroot

# Set environment variables
ENV ASPNETCORE_URLS=http://+:8080
ENV ASPNETCORE_ENVIRONMENT=Production
ENV DOTNET_RUNNING_IN_CONTAINER=true

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=15s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# Switch to non-root user
USER appuser

ENTRYPOINT ["dotnet", "InventoryService.dll"]
