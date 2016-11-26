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
    var blogContent = '<div id="disk_fileTableDiv">' +
                          '<table id="disk_fileTable" cellspacing="0px">' +
                          '<tr><th id="disk_fileName">File Name</th>' +
                                '<th id="disk_uploadDate">Upload Date</th>' +
                                '<th id="disk_fileSize">File Size</th>' +
                                '<th id="disk_download">Download</th></tr>';
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
    blogContent += '</table></div>';
    var frame = $("#__frame__");
    frame.html(blogContent);
}