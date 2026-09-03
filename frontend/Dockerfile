FROM node:20-alpine AS build
WORKDIR /app

# Copy package files and install dependencies
COPY package*.json ./
RUN npm install

# Copy source code and build
COPY . .
ARG VITE_GOOGLE_CLIENT_ID
ARG VITE_API_BASE_URL=/api/v1
ENV VITE_GOOGLE_CLIENT_ID=$VITE_GOOGLE_CLIENT_ID
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
RUN npm run build

# Use Nginx to serve the static files
FROM nginx:alpine
# Copy the built app from the previous stage
COPY --from=build /app/dist /usr/share/nginx/html
# Expose port 80
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]
