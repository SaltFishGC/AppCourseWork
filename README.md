# AppCourseWork

简单的app制作：考研英语单词背诵，学习时长记录，本地数据同步服务器部署

## 运行说明：

### 配置

#### app：

sdk位置（local.properties）可能需要更改（预设为D盘下AndroidSDK文件夹），同步后（Sync）即可启动

#### app server：

本地数据库，需要预准备mysql数据库，这里准备了一个sql文件（myapp_data文件夹下myapp_server.sql文件）其包括了数据库以及表和测试数据的建立以及导入，使用该文件即可导入。

数据库连接需要修改src文件夹下resources的application.yml配置文件，将数据库ip，username，password修改为你的配置。

完成配置后maven重新加载即可运行。

推荐使用jetbrain的IDEA编译器。

SpringBoot version：3.1.2

MySQL version：5.7

### 运行

在不部署服务器的情况下，app也支持本地运行无需登录，但是无法同步数据，运行后授权即可使用！背诵记录以及学习时长记录会存储至本地sqlite的db文件中。

部署服务器的情况下，只需修改登录界面的“修改服务器信息”完成配置即可连接，部分服务器（或本地设备）还需要开启对应端口请注意！
