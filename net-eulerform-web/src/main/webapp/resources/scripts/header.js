/**
 * 
 */

$(function($){        
    $('#userInfo').mouseleave(
        function(){hideUserMenu();}
    );
});

function searchBoxClick(id, value)
{
    var txtBox=document.getElementById(id);
    if (txtBox.value == "Input to search...") {
        txtBox.value = "";
    }else if(txtBox.value == "") {
        txtBox.value = "Input to search...";
    }
}

function userInfoClicked(){
    if($('#userMenu').css('display') == 'none')
        showUserMenu();
    else
        hideUserMenu();
}

function showUserMenu(){
    $('#userMenu').css('display', 'block');
//    $('#userMenu').css('height', '0px');
//    $("#userMenu").animate({
//        height:'+=131px'
//      },50);
}

function hideUserMenu(){
//    $("#userMenu").animate({
//        height:'-=131px'
//      },500);
    $('#userMenu').css('display', 'none');
}