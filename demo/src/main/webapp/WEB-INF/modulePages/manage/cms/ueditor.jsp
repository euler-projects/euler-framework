<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">

    <title></title>

</head>

<body>

    <script src="${contextPath}/resources/scripts/lib/ueditor/ueditor.config.js"></script>
    <script src="${contextPath}/resources/scripts/lib/ueditor/ueditor.all.min.js"> </script>
    <script src="${contextPath}/resources/scripts/lib/ueditor/lang/zh-cn/zh-cn.js"></script>
    <script id="editor" type="text/plain" style="width:90%;height:1000px;"></script>
    <script>
    var ue = UE.getEditor(
            'editor',
            {
                toolbars: [
                    [
                        'source', //源代码
                        'preview', //预览
                        'print', //打印
                        '|',
                        'undo', //撤销
                        'redo', //重做
                        '|',
                        'inserttable', //插入表格
                        'insertrow', //前插入行
                        'insertcol', //前插入列
                        'deleterow', //删除行
                        'deletecol', //删除列
                        'mergeright', //右合并单元格
                        'mergedown', //下合并单元格
                        'mergecells', //合并多个单元格
                        'splittorows', //拆分成行
                        'splittocols', //拆分成列
                        'splittocells', //完全拆分单元格
                        'inserttitle', //插入标题
                        'deletecaption', //删除表格标题
                        'deletetable', //删除表格
                        'insertparagraphbeforetable',//"表格前插入行"
                        'edittable', //表格属性
                        'edittd', //单元格属性
                    ],
                    [
                        'formatmatch', //格式刷
                        'horizontal', //分隔线
                        'searchreplace', //查询替换
                        '|',
                        'paragraph', //段落格式
                        'fontfamily', //字体
                        'fontsize', //字号
                        '|',
                        'bold', //加粗
                        'italic', //斜体
                        'underline', //下划线
                        'strikethrough', //删除线
                        'subscript', //下标
                        'superscript', //上标
                        'fontborder', //字符边框
                        '|',
                        'forecolor', //字体颜色
                        'backcolor', //背景色
                        '|',
                        'justifyleft', //居左对齐
                        'justifyright', //居右对齐
                        'justifycenter', //居中对齐
                        'justifyjustify', //两端对齐
                        '|',
                        'link', //超链接
                        'unlink', //取消链接
                        'simpleupload', //单图上传
                        'map', //Baidu地图
                        'attachment' //附件
                    ]
                ]
            });
    </script>
</body>

</html>