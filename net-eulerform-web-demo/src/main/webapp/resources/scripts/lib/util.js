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


function unixDateFormatter(value, row, index) {
    return new Date(value).Format('yyyy-MM-dd hh:mm:ss');
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

function getQueryString(name) { 
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i"); 
    var r = window.location.search.substr(1).match(reg); 
    if (r != null) return unescape(r[2]); return null; 
}
