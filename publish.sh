#!/bin/sh
# 发布到本地库, 用于检查
gradle publishToMavenLocal -x test

# 发布到指定的maven仓库
gradle publish -x test