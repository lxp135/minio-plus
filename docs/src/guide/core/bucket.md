# 桶策略

* 文档（document）：txt、rtf、ofd、doc、docx、xls、xlsx、ppt、pptx、pdf
* 压缩包（package）：zip、rar、7z、tar、wim、gz、bz2
* 音频（ audio ）：mp3、wav、flac、acc、ogg、aiff、m4a、wma、midi
* 视频（ video ）：mp4、avi、mov、wmv、flv、mkv、mpeg、mpg 、rmvb
* 图片 – 原始（ image ）：jpeg、jpg、png、bmp、webp、gif
* 图片 – 缩略图（ image-preview ）：默认按照宽度 300 像素压缩缩
* 其他（ other ） ：未在上述格式中的文件

其他规则：文件在桶中存储时，按照 /年/月 划分路径。用以规避Linux ext3文件系统下单个目录最多创建32000个目录的问题，参考了阿里云OSS的处理办法。