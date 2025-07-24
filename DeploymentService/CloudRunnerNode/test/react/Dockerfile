FROM node:18-alpine

# Create app directory
WORKDIR /usr/src/app

COPY ../../../NodeContainers/ReactJs/package.json ./
COPY ../../../NodeContainers/ReactJs/public ./public
COPY ../../../NodeContainers/ReactJs/src ./src

RUN npm install --legacy-peer-deps

# Build the app
RUN npm run build

# Serve the app with serve
RUN npm install -g serve

EXPOSE 3000
CMD ["serve", "-s", "build", "-l", "3000"]
