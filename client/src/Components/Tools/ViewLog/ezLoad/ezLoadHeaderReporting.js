//window.$ = window.jQuery = require('jquery');
import $ from "jquery";

var idQueue = [];
var lastId = 1;
idQueue.push(lastId);

const openIcon = '<span class="toggler">+</span>';
const closeIcon = '<span class="toggler">-</span>';

function updateIcons(objDiv) {
    if (objDiv.hasClass('openedItem')) {
        objDiv.find('span.toggler').replaceWith(closeIcon);
    } else {
        objDiv.find('span.toggler').replaceWith(openIcon);
    }
}

function closeAll(objDiv) {
    var items = objDiv.parent().siblings().find('ul.submenu');
    items.each(function () {
        var ul = $(this).slideUp('slow').parent();
        ul.find('span.toggler').replaceWith(openIcon);
        ul.find('a').first().removeClass('openedItem');
    });
}

export function pushSection(elem){
    var parentId = idQueue[idQueue.length-1];
    lastId++;
    idQueue.push(lastId);
    $('#'+parentId).append("<li class='section'><div href='#'>"+elem+openIcon+"</div><ul class='submenu' id="+lastId+"></ul></li>");
    var a = $('#'+parentId).children("li").last().children("div").first();
    show(a);

    a.bind('click', function (e) {
                           // e.preventDefault();
                            openOrClose($(this));
                        });
}


export function add(elem, isError){
    var parentId = idQueue[idQueue.length-1];
    $('#'+parentId).append("<li class='log'><div href='#' "+(isError? "class='error'" : "")+">"+elem+"</div></li>");
    if (isError){
        for (let i = 0; i < idQueue.length; i++){
            $('#'+idQueue[i]).parent().children('div').first().addClass('error');
        }
    }
}

function openOrClose(objDiv){
        var leaveOpen = false;
    if (leaveOpen === false) {
        closeAll(objDiv);
    }

    if (objDiv.hasClass('openedItem')) {
        hide(objDiv);
    }
    else{
        show(objDiv);
    }

    updateIcons(objDiv);
}

function hide(objDiv){
    objDiv.removeClass('openedItem');
    objDiv.siblings('ul').first().slideUp();
    updateIcons(objDiv);
}

function show(objDiv){
    objDiv.addClass('openedItem');
    objDiv.siblings('ul').first().slideDown();
    updateIcons(objDiv);
}

export function popSection(){
    var sectionId = idQueue.pop();
    hide($('#'+sectionId).parent().children('div').first());
}

