# community-src

#### 介绍


社交APP源代码

community-server 是后端主工程，包含：登录，发动态，动态排名，动态管理，评论，聊天等接口。


multdao.mybatis 是后端扩展组件，是基于mybatis底层实现了CURD的分布式事务，不修改任何业务逻辑情况下，导入该组件即可实现数据的CURD实时同步到mysql和mongodb，
双库数据一致。它的作用在DAO层实现了mongodb作为数据cache，写的时候写双库，读的时候读mongodb。目前支持：mapper方式的sql。


1.  不修改任何项目上的逻辑代码即可集成
2.  添删改查同时操作mysql和mongodb
3.  基于mybatis底层源码实现，也支持mybatis-plus
4.  组件保证了分布式事务的问题，数据一致性
5.  支持并发CURD，支持大数据量读写
6.  实现了读写分离：读mongodb，写mysql
7.  关系数据库支持：mysql,oracle.非关系数据库支持:mongodb
8.  支持sql92规范的语法，支持复杂的join查询


#### 软件架构
软件架构说明


![输入图片说明](https://images.gitee.com/uploads/images/2021/0417/211942_f796c180_488703.png "微信图片_20210417211754.png")
![输入图片说明](https://images.gitee.com/uploads/images/2021/0417/212107_a165bd9f_488703.png "444.png")

#### 安装教程
1.  multdao.mybatis进行maven编译打包
2.  将multdao.mybatis文件夹下的org文件夹内容复制到您的项目工程\src\main\java\下面
3.  在您的项目工程pom文件里加入如下依赖：

		<dependency>
			<groupId>com.hazelcast</groupId>
			<artifactId>hazelcast</artifactId>
		</dependency>
		<dependency>
			<groupId>com.lanyu</groupId>
			<artifactId>multdao.mybatis</artifactId>
			<version>1.0-SNAPSHOT</version>
		</dependency>
		
		
4.  您的工程文件application.properties添加如下： 

            mult.dao.mongodb.host=127.0.0.1:27017自己库IP
            mult.dao.mongodb.database=自己库名
            mult.dao.mongodb.username=root自己库账号
            mult.dao.mongodb.password=123456自己库密码

#### 使用说明

1.  xxxx
2.  xxxx
3.  xxxx

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


#### 码云特技

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  码云官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解码云上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是码云最有价值开源项目，是码云综合评定出的优秀开源项目
5.  码云官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  码云封面人物是一档用来展示码云会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)
