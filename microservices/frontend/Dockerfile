# Build stage
FROM node:16-alpine AS builder
WORKDIR /app

# Copy package files from the monolith frontend directory
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci

# Copy frontend source
COPY frontend/ ./

# Set the API URL to go through the gateway (relative URL so it goes through nginx)
ENV NEXT_PUBLIC_API_URL=/api

RUN npm run build

# Production stage
FROM node:16-alpine AS runner
WORKDIR /app

ENV NODE_ENV=production

# Copy built application
COPY --from=builder /app/package.json ./
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public

EXPOSE 3000

CMD ["npm", "start"]
