window.onload = initPage;
function initPage() {
    loadFile();
}

function loadFile() {
    $.ajax({
        url: "json/files.json",
        type: "GET",
        dataType:'json',
        success:displayFiles,
        error:function(er){
        alert(er);}
    });
}

function displayFiles(data) {
    var blogContent = "";
    var line = 1;
    $.each(data.files, function(i, item) {
        blogContent += '<tr class="';
        if ((line++)%2 != 0) {
            blogContent += 'odd';
        } else {
            blogContent += 'even';
        }
        blogContent += '"><td>';
        blogContent += item.fileName;
        blogContent += '</td><td>';
        blogContent += item.uploadDate;
        blogContent += '</td><td>';
        blogContent += item.fileSize;
        blogContent += '</td><td>';
        blogContent += '<a href="' + item.fileUrl + '">Download</a>';
        blogContent += '</td><tr>';
    });
    var frame = $("#disk_fileTable");
    blogContent = frame.html()+blogContent;
    frame.html(blogContent);
}