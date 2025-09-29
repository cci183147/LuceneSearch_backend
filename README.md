##  获取本项目
> `git clone https://github.com/cci183147/LuceneSearch_backend.git`
## 数据准备
准备好pdf文件和grobid
推荐使用**docker**
`docker run --rm --init --ulimit core=0 -p 8070:8070 grobid/grobid:0.8.1`
访问`localhost:8070`
见到如下画面说明启动成功
<img width="1200" height="574" alt="Grobid" src="https://github.com/user-attachments/assets/bac685c6-a8a9-4cf7-bc40-b177f04fefe4" />

`python3 pdfs.py`
处理数据
最后将pdf文件和得到的xml文件放在项目的resources下
## 运行

运行index.java
运行完成后可见`Indexing completed!`说明索引生成成功

最后启动LuceneSearchApplication即可

##### 注:
本项目默认运行在localhost:80，若有其他程序占用端口可能导致启动失败
可以在application.properties中修改运行端口
