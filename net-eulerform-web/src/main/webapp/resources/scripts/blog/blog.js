window.onload = initPage;
function initPage() {
    loadBlog();
}

function loadBlog() {
    $.ajax({
        url: "./loadBlog",
        type: "GET",
        dataType:'json',
        success:displayBlogs,
        error:function(er){
        alert(er);}
    });
}
    
    
function displayBlogs(data) {
    var blogContent = "";
    $.each(data.tags, function(i, item) {
        var tags = item.tags;                        
        var tag = tags.split("|,");
        for(var tempTag in tag) {
            blogContent += '<li><a href="javascript:void(0);">'+ tag[tempTag] +'</a></li>';
        }
    });
    var blog_sidebar_tag = $("#blog_sidebar_tag");
    blog_sidebar_tag.html(blogContent); 
    blogContent = "";
    $.each(data.dates, function(i, item) {
        var dates = item.dates;                        
        var date = dates.split(",");
        for(var temp in date) {
            blogContent += '<li><a href="javascript:void(0);">'+ date[temp] +'</a></li>';
        }
    });
    var blog_sidebar_time = $("#blog_sidebar_time");
    blog_sidebar_time.html(blogContent); 
    blogContent = "";
    $.each(data.blogs, function(i, item) {
        blogContent += '<div class="blog_content">';
        blogContent +=
            '<div class="blog_title">' + item.title + '</div>' +
            '<hr style="height:1px;border:none;border-top:1px solid #555555;" />'+ 
            '<div class="blog_info">' + item.date + '&nbsp;&nbsp;&nbsp;Tag:&nbsp;';
        var tags = item.tags;                        
        var tag = tags.split("|,");
        for(var tempTag in tag) {
            blogContent += '<a href="javascript:void(0);">'+ tag[tempTag] +'</a>&nbsp;';
        }
        blogContent += '</div>';
        $.ajax({
            url: item.body,
            type: "GET",
            async: false,
            dataType:'text',
            success:function(data) {
                item.body=data;
            },
            error:function(er){
            alert(er);}
        });
        blogContent += '<div class="blog_body">' + item.body + '</div>';
        blogContent += '<div class="blog_footer">&diams;&nbsp;' + item.author + '</div></div>';
    });
    var frame = $("#blog");
    frame.html(blogContent);
}