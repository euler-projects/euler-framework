var browserName = navigator.appName;

//var strFullPath=window.document.location.href;
//var strPath=window.document.location.pathname;
//var pos=strFullPath.indexOf(strPath);
//var prePath=strFullPath.substring(0,pos);
//var postPath=strPath.substring(0,strPath.substr(1).indexOf('/')+1);
//alert(prePath+postPath);

if(browserName != "Netscape") {
    window.location.href= prePath+postPath+"/nohtml5.html";
}

function setStyle(id, styleType, style) {
    var item = document.getElementById(id);
    if(styleType == 'clear') {
        item.style.cssText = null;
    }else if(styleType == 'inline') {
        item.style.cssText = style;
    }
}