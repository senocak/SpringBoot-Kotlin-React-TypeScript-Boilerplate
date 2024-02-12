FROM node:14.16
WORKDIR /app
ADD package.json /app
COPY . /app
RUN yarn install
RUN yarn global add serve
RUN yarn run build
RUN pwd
CMD ["serve", "-l", "3232", "-s", "build"]
