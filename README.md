**在项目根目录下，执行以下命令，上传tibrvj.jar到本地仓库**  
```mvn install:install-file -Dfile=lib/tibrvj.jar -DgroupId=com.tibco -DartifactId=tibrvj -Dversion=8.5 -Dpackaging=jar```

**在项目根目录下，执行以下命令，打包**  
```mvn clean package -DskipTests```

**在项目根目录下，执行以下命令，解压target/streamsets-tibcorv-1.0-SNAPSHOT.tar.gz到sdc的user-libs目录下**  
```tar -xvf target/streamsets-tibcorv-1.0-SNAPSHOT.tar.gz -C $SDC_DIST/user-libs```

**重启sdc，在Origins中即可看到TibcorvSubscriber组件**
