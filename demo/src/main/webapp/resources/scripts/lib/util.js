$.fn.serializeJson=function(){ 
    var serializeObj={};
    var array=this.serializeArray();
    $(array).each(function(){
            if(serializeObj[this.name]){
                  if($.isArray(serializeObj[this.name])){ 
                      serializeObj[this.name].push(this.value); 
                  }else{
                      serializeObj[this.name]=[serializeObj[this.name],this.value]; 
                  } 
            }else{ 
                serializeObj[this.name]=this.value;
            } 
    }); 
    return serializeObj; 
};

$(function(){
    $(".search-table input,.search-table select").on('keyup',function(event){
        if(event.keyCode == "13"){
            $(".search-btn-table #search-btn").click();
        }
    });
});

function getDictData(code, all){
    code = getDictClone(code);
    var a = [], c;
    for(var i = 0; i < code.length; i++){
        c = code[i];
        a.push(c);
    }
    if('all' == all){
        a.push({
            key:"",
            value:'全部'
        });
    }
    return a;
}

function getDictClone(code){
    for(var d= [], a, c = 0; c < code.length; c++){
        a = code[c];
        d.push(clone(a));
    }
    return d;
}

function clone(data) {
    if("object" != typeof data || null == data){
        return data;
    }
    var d = {}, a;
    for(a in data){
        d[a] = clone(data[a]);
    }
    return d;
}

function setImgSrc(obj, width, height, src) {
    
    $(obj).width(width);
    $(obj).height(height);
    $(obj).attr("src", "");
    
    if(src == null || src == "")
        return;
    
    loadImage(src, function(img){
        var page_w = width;
        var page_h = height;
        var page_wh = page_w/page_h;
        var real_w = img.width;
        var real_h = img.height;
        var real_wh = real_w/real_h;
        if(real_wh >= page_wh) {
            $(obj).height(page_w/real_wh);
            $(obj).width(page_w);
        } else {
            $(obj).width(page_h*real_wh);
            $(obj).height(page_h);                      
        }
        $(obj).attr("src", src);
    });
    
}

function loadImage(url, callback){
    var img = new Image();
    img.src = url;
    // 如果图片被缓存，则直接返回缓存数据
    if(img.complete){
        callback(img);
    }else{
        // 完全加载完毕的事件
        img.onload = function(){
            callback(img);
        }
    }            
}

Date.prototype.Format = function (fmt) { //author: meizz 
    var o = {
        "M+": this.getMonth() + 1, //月份 
        "d+": this.getDate(), //日 
        "h+": this.getHours(), //小时 
        "m+": this.getMinutes(), //分 
        "s+": this.getSeconds(), //秒 
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度 
        "S": this.getMilliseconds() //毫秒 
    };
    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
    if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
}

function addTab(url, title){
    var html = '<iframe width="100%" height="100%" frameborder="no" border="0" marginwidth="0" marginheight="0" allowtransparency="yes" src="'+url+'"></iframe>';
    var exists = top.$('#main-content').tabs('exists', title);
    if(exists){
        top.$('#main-content').tabs('select', title);
        var tab = top.$('#main-content').tabs('getSelected');
        top.$('#main-content').tabs('update', {
            tab: tab,
            options: {
                title: title,
                content:html,
                //href: url  // the new content URL
            }
        });
        return;
    }     
    top.$('#main-content').tabs('add',{
        title:title,
        content:html,
        closable:true
    });
}

function getUrlParam(name) { 
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i"); 
    var r = window.location.search.substr(1).match(reg); 
    if (r != null) return unescape(r[2]); return null; 
}

function unixDatetimeFormatter(value, row, index) {
    return new Date(value).Format('yyyy-MM-dd hh:mm:ss');
}

function yesOrNoFormatter(value, row, index){
    if(typeof(value) == 'undefined')
        return '-';
    for(i in yesOrNo){
        if(yesOrNo[i].key == value+"")
            return yesOrNo[i].value;
    }    
}

function urlFormatter(value, row, index) {
    if(value == null || value == "")
        return '-';
    return '<a href="'+value+'" target="_Blank">'+value+'</a>';
}
