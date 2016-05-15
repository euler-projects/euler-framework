window.onload = initPage;
function initPage() {
    loadCode();
}

function loadCode() {
    $.ajax({
        url: "codes/disk.js",
        type: "GET",
        dataType:'text',
        success:function(date) {
            date = date.replace(/\</g,"&lt");
            date = date.replace(/\>/g,"&gt");
            var frame = $("#code_content");
            frame.html(date);
        },
        error:function(er){
        alert(er);}
    });
}