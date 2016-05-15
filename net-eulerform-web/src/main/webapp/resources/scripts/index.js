window.onload=initPage;

function initPage() {
    drawPage();
}

function drawPage() {
    //loadProgress();
}

function loadProgress() {
    $.ajax({
        url: "json/progress.json",
        type: "GET",
        dataType:'json',
        success:displayProgress,
        error:function(er){
        alert(er);}
    });
}

function displayProgress(data) {
    var blogContent = "";
    $.each(data.progress, function(i, item) {
        blogContent += '<p>';
        blogContent += item.content;
        blogContent += '</p>';
    });
    var frame = $("#progress");
    blogContent = frame.html()+blogContent;
    frame.html(blogContent);
}