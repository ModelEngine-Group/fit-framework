#!/bin/bash
node -v
npm -v

pwd
cd ./portal/fit-elsa
pwd

npm install
npm run build


echo 'start publish-------------------------'
pwd
ls -al
npm config set strict-ssl false

# 通过流水线上配置的全局参数传入
npm publish